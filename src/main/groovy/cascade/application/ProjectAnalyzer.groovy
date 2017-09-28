package cascade.application

import cascade.model.OrderedProject
import cascade.model.Project

import java.util.stream.Collectors


/** Determines the dependency graph and order of affected projects */
class ProjectAnalyzer {

	List<Project> collect(String directory, List<String> excludedDirectories) {
		List<Project> result = []
		new File(directory).eachFile {
			if (it.isDirectory() && !excludedDirectories.contains(it.name)) {
				def filePom = new File(it, "pom.xml")
				if (filePom.exists()) {
					String pomContent = filePom.getText("UTF-8")
					def pomXml = new XmlSlurper().parseText(pomContent)
					result << new Project(it.name, pomContent, pomXml)
				}
			}
		}
		return result
	}


	void analyzeDependencies(List<Project> projects) {
		// collect projects by group/artifact
		Map<String, Project> lookup = [:]
		projects.forEach {project ->
			String projectName = project.getProjectName()
			if (lookup.containsKey(projectName)) {
				throw new ReleaseException("Project '${projectName}' found twice")
			}
			lookup.put(projectName, project)
		}

		// collect dependencies/dependants
		projects.forEach {project ->
			project.parseDependencyNames().forEach {dependencyName ->
				Project dependencyProject = lookup.get(dependencyName)
				if (dependencyProject) {
					project.addDependency(dependencyProject)
					dependencyProject.addDependant(project)
				}
			}
			Project parent = lookup.get(project.parseParentDependencyName())
			if (parent) {
				project.setParent(parent)
				parent.addChild(project)
			}
		}
	}


	Project determineStartProjects(List<Project> projects, String projectDirectoryStart) {
		Project result = projects.find {it.getDirectoryName() == projectDirectoryStart}
		if (!result) {
			throw new ReleaseException("Could not find start-project")
		}
		return result
	}


	List<String> filterProjects(List<Project> projects, Project projectStart, List<String> additionalGroupIds) {
		List<String> validGroupIds = [projectStart.groupId] + additionalGroupIds
		return projects.stream()
			.filter {project -> validGroupIds.contains(project.groupId)}
			.collect(Collectors.toList())
	}


	List<OrderedProject> generateReleaseOrder(Project projectStart, String versionIncrement, List<String> updateOnlyGroupIds) {
		List<Project> resultParents = []
		traverseParents(resultParents, projectStart)

		List<Project> resultDependencies = []
		traverseDependencies(resultDependencies, projectStart)

		List<Project> projects = resultParents + resultDependencies.reverse()

		return projects.stream()
			.map {p ->
			new OrderedProject(directoryName: p.directoryName, projectName: p.projectName, groupId: p.groupId, artifactId: p.artifactId,
				version: p.version, versionIncrement: versionIncrement, updateOnly: updateOnlyGroupIds.contains(p.groupId))
		}
		.collect(Collectors.toList())
	}


	protected void traverseParents(List<String> result, Project project) {
		if (project.parseIsParent()) {
			result.add(project)
			project.setTraversed(true)
			for (Project child : project.getChilds()) {
				traverseParents(result, child)
			}
		}
	}


	protected void traverseDependencies(List<String> result, Project project) {
		if (project.parseIsParent()) {
			for (Project child : project.getChilds()) {
				traverseDependency(result, child)
			}
		}
		else {
			for (Project dependant : project.getDependants()) {
				traverseDependency(result, dependant)
			}
			result.add(project)
		}
	}


	private traverseDependency(List<String> result, Project project) {
		if (!project.isTraversed()) {
			project.setTraversed(true)
			traverseDependencies(result, project)
		}
	}

}
