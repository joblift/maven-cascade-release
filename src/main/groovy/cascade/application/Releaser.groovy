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


	void release(ReleaseContext context, List<String> updateOnlyGroupIds) {
		for (OrderedProject project : context.projects) {
			if (project.isReleased()) {
				File workingDirectory = new File(context.getProjectsDirectory(), project.getDirectoryName())
				updateDependencies(context, project, workingDirectory)
				if (!updateOnlyGroupIds.contains(project.groupId)) {
					releaseProject(context, project, workingDirectory)
				}
				project.released = true
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


	private void updateDependencies(ReleaseContext context, OrderedProject project, File workingDirectory) {
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
			if (line.trim() == "<parent>") {
				inParent = true
				dependency = new Dependency()
			}
			else if (line.trim() == "</parent>") {
				inParent = false
				parent = dependency
				dependency = null
			}
			else if (line.trim() == "<dependencies>") {
				inDependencies = true
			}
			else if (line.trim() == "</dependencies>") {
				inDependencies = false
			}
			else if (inDependencies && line.trim() == "<dependency>") {
				inDependency = true
				dependency = new Dependency()
			}
			else if (inDependencies && line.trim() == "</dependency>") {
				inDependency = false
				dependencies.add(dependency)
				dependency = null
			}
			else if (line.trim() == "<exclusions>") {
				inExclusions = true
			}
			else if (line.trim() == "</exclusions>") {
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

			shell.executeInline("git add pom.xml", workingDirectory)
			shell.executeInline("git ci -m 'Updated dependencies for release ${project.versionNew()}'", workingDirectory)
			shell.executeInline("git push", workingDirectory)
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
