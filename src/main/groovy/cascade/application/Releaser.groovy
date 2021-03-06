package cascade.application

import cascade.model.OrderedProject
import cascade.util.Log
import cascade.util.Shell

import java.util.regex.Matcher
import java.util.regex.Pattern


/** Updates the pom and releases the actual maven project */
class Releaser {

	static final Pattern PATTERN_VERSION = ~/([ \t]*<version>)(.*)(<\/version>[ \t]*)/
	static final Pattern PATTERN_GROUPID = ~/[ \t]*<groupId>(.*)<\/groupId>[ \t]*/
	static final Pattern PATTERN_ARTIFACTID = ~/[ \t]*<artifactId>(.*)<\/artifactId>[ \t]*/

	Shell shell = new Shell()
	String branchName = new Date().format("yyyy-MM-dd_HH-mm-ss") + "_" + System.getProperty("user.name").replaceAll("/[^A-Za-z0-9]/", "");



	void release(ReleaseContext context, List<String> updateOnlyGroupIds, String message, Boolean mr, Boolean mrAutomerge, String mrUsername) {
		for (OrderedProject project : context.projects) {
			if (!project.isReleased()) {
				project.verified = false // toggle because release might change the state
				context.store()

				boolean updateOnly = updateOnlyGroupIds.contains(project.groupId)

				File workingDirectory = new File(context.getProjectsDirectory(), project.getDirectoryName())
				updateDependencies(context, project, workingDirectory, message, updateOnly && mr, mrAutomerge, mrUsername)
				if (!updateOnly) {
					releaseProject(context, project, workingDirectory)
				}

				project.released = true
				project.verified = true
				context.store()
			}
		}
	}


	void releaseProject(ReleaseContext context, OrderedProject project, File workingDirectory) {
		Log.info("Preparing release ${project.directoryName} : ${project.versionNew()}")
		shell.executeInline("mvn release:prepare --batch-mode -DreleaseVersion=${project.versionNew()}", workingDirectory)

		Log.info("Releasing ${project.directoryName} : ${project.versionNew()}")
		shell.executeInline("mvn release:perform", workingDirectory)

		shell.executeInline("git pull", workingDirectory) // mvn/git cleanup

		Log.info("Released ${project.directoryName} : ${project.versionNew()}")
		println ""
	}


	private void updateDependencies(ReleaseContext context, OrderedProject project, File workingDirectory, String message, Boolean mrCreate, Boolean mrAutomerge, String mrUsername) {
		File filePom = new File(workingDirectory, "pom.xml")

		boolean inParent
		boolean inDependencies
		boolean inDependency
		boolean inExclusions
		Dependency dependency
		Dependency parent
		List<Dependency> dependencies = []

		List<String> pomLines = filePom.readLines("UTF-8")
		for (int index = 0; index < pomLines.size(); index++) {
			String line = pomLines.get(index)
			if (line.contains('<parent>')) {
				inParent = true
				dependency = new Dependency()
			}
			else if (line.contains('</parent>')) {
				inParent = false
				parent = dependency
				dependency = null
			}
			else if (line.contains('<dependencies>')) {
				inDependencies = true
			}
			else if (line.contains('</dependencies>')) {
				inDependencies = false
			}
			else if (inDependencies && line.contains('<dependency>')) {
				inDependency = true
				dependency = new Dependency()
			}
			else if (inDependencies && line.contains('</dependency>')) {
				inDependency = false
				dependencies.add(dependency)
				dependency = null
			}
			else if (line.contains('<exclusions>')) {
				inExclusions = true
			}
			else if (line.contains('</exclusions>')) {
				inExclusions = false
			}
			else if (!inExclusions) {
				if (inParent || inDependency) {
					Matcher matcherVersion = PATTERN_VERSION.matcher(line)
					Matcher matcherGroupId = PATTERN_GROUPID.matcher(line)
					Matcher matcherArtifactId = PATTERN_ARTIFACTID.matcher(line)
					if (matcherVersion.matches()) {
						dependency.setVersion(matcherVersion.group(2))
						dependency.setVersionIndex(index)
						dependency.setVersionLine(line)
					}
					else if (matcherGroupId.matches()) {
						dependency.setGroupId(matcherGroupId.group(1))
					}
					else if (matcherArtifactId.matches()) {
						dependency.setArtifactId(matcherArtifactId.group(1))
					}
				}
			}
		}

		boolean dirty
		if (parent) {
			dirty |= replaceVersionLine(context, parent, pomLines)
		}

		for (Dependency dep : dependencies) {
			dirty |= replaceVersionLine(context, dep, pomLines)
		}

		if (dirty) {
			// generate and store file
			filePom.write(pomLines.join('\n') + '\n')

			String ref = mrCreate ? "cascade/${branchName}" : "master"
			String argMr = ""
			if (mrCreate) {
				shell.executeInline("git checkout -b \"${ref}\"", workingDirectory)
				argMr += "-o merge_request.create -o merge_request.remove_source_branch -o merge_request.title=\"${message}cascade-release\""
				if (mrUsername) {
					argMr += " -o merge_request.assign=\"${mrUsername}\""
				}
				if (mrAutomerge) {
					argMr += " -o merge_request.merge_when_pipeline_succeeds"
				}
			}

			shell.executeInline("git add pom.xml", workingDirectory)
			shell.executeInline("git commit -m '${message}Updated dependencies for release ${project.versionNew() ?: 'from upstream'}'", workingDirectory)
			shell.executeInline("git push origin ${ref} ${argMr}", workingDirectory)
			
			if (mrCreate) {
				shell.executeInline("git checkout master", workingDirectory)
			}

			Log.info("Updated pom-file dependencies in ${project.directoryName}")
		}
		else {
			Log.info("Not updated pom-file dependencies in ${project.directoryName}")
		}
	}


	private boolean replaceVersionLine(ReleaseContext context, Dependency dependency, List<String> pomLines) {
		boolean changed
		String versionReplacement = context.getVersionNew(dependency.getGroupId(), dependency.getArtifactId())
		if (versionReplacement) {
			if (!dependency.artifactId || !dependency.groupId || !dependency.version) {
				throw new ReleaseException("parent element in ${dependency.groupId}:${dependency.artifactId} is not valid")
			}
			if (versionReplacement != dependency.version) {
				Matcher matcher = PATTERN_VERSION.matcher(dependency.versionLine)
				matcher.matches()
				String newVersionLine = matcher.group(1) + versionReplacement + matcher.group(3)
				pomLines.set(dependency.versionIndex, newVersionLine)
				changed = true
			}
		}
		return changed
	}

}
