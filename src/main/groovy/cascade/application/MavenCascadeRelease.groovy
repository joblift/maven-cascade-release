package cascade.application

import cascade.model.OrderedProject
import cascade.model.Project
import cascade.util.Log
import cascade.util.Shell


/**
 * General process:
 * - check inputs
 * - check stored progress
 * - determine all directories with pom
 * - generate graph
 * - determine affected graph
 * - store progress file
 * - print affected graph/next versions
 * - start releases in order, for each
 *   - update pom.xml (if dependencies changed)
 *   - release maven project
 *   - store progress
 * - cleanup progress
 */
class MavenCascadeRelease {

	static void main(String[] args) {
		Options options
		try {
			options = new OptionParser().parse(args)
			checkPath()
			ReleaseContext context = prepareContextFile(options)
			verifyProjects(context, options.skipVerify)
			if (Log.ask("Continue with release?", "y", "n")) {
				new Releaser().release(context, options.updateOnlyGroupIds)
				context.cleanup()
				printReleaseOrder(context.projects, "Finished releasing projects")
			}
		}
		catch (Exception ex) {
			println ""
			Log.error("Failed releasing: $ex.message\n")
			if (!options || options?.verbose) {
				throw ex
			}
		}
	}


	static void verifyProjects(ReleaseContext context, Boolean skipVerify) {
		Shell shell = new Shell()
		Log.info("Verifying projects up-to-date and not dirty")
		if (skipVerify) {
			Log.info("skipped ...")
			return
		}
		for (OrderedProject project : context.projects) {
			if (project.isVerified()) {
				print '*'
			}
			else {
				print '.'
				File workingDirectory = new File(context.projectsDirectory, project.directoryName)
				// check branch is master
				String branch = shell.execute('git rev-parse --abbrev-ref HEAD', workingDirectory)
				if (branch != 'master') {
					throw new ReleaseException("Project ${project.directoryName} is not in master (${branch})")
				}

				// check dirty
				String outputDirty = shell.execute('git status --porcelain', workingDirectory)
				if (outputDirty || !outputDirty.isEmpty()) {
					throw new ReleaseException("Project ${project.directoryName} has uncommited changes")
				}

				// check behind
				shell.execute('git fetch -q', workingDirectory)
				Integer behind = shell.execute('git rev-list HEAD...origin/master --count', workingDirectory) as Integer
				if (behind > 0) {
					throw new ReleaseException("Project ${project.directoryName} is ${behind} commits behind")
				}
				project.verified = true
			}
			context.store()
		}
		println ''
	}


	static ReleaseContext prepareContextFile(Options options) {
		ReleaseContext result
		File contextFile = new File(options.projectsDirectory, ReleaseContext.FILE_NAME)
		if (contextFile.exists() && Log.ask("Found interupted cascade-release context. Continue with started release?", "y", "n")) {
			result = ReleaseContext.fromFile(contextFile)
		}
		else {
			result = new ReleaseContext(projectsDirectory: options.projectsDirectory.getAbsolutePath(), projectStartDirectory: options.projectStartDirectory)
			ProjectAnalyzer analyzer = new ProjectAnalyzer()
			List<Project> projects = analyzer.collect(result.projectsDirectory, options.excludedDirectories)
			Project projectStart = analyzer.determineStartProjects(projects, options.projectStartDirectory)
			List<Project> projectsFiltered = analyzer.filterProjects(projects, projectStart, options.additionalGroupIds)
			analyzer.analyzeDependencies(projectsFiltered)
			List<OrderedProject> projectsOrdered = analyzer.generateReleaseOrder(projectStart, options.versionIncrement, options.updateOnlyGroupIds)
			result.setProjects(projectsOrdered)
			result.store()
		}
		printReleaseOrder(result.getProjects(), "Projects will be released in the following order")
		return result
	}


	static void printReleaseOrder(List<OrderedProject> projects, String message) {
		if (message) {
			Log.info("${message}:")
		}
		for (OrderedProject project : projects) {
			String flag = project.isReleased() ? "✔" : " "
			String name = project.getDirectoryName()
			String versionNew = project.versionNew() ?: "-"
			println "[${Log.green(flag)}] ${name} > ${versionNew}"
		}
		println ""
	}


	static void checkPath() {
		try {
			new Shell().execute('git --version')
			new Shell().execute('mvn --version')
		}
		catch (Exception ex) {
			throw new ReleaseException("The executables for git and mvn must be available in the PATH. Failed verification. Current PATH=${System.getenv("PATH")}")
		}
	}

}
