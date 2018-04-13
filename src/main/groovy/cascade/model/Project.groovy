package cascade.model

import cascade.application.ReleaseException

class Project {

	String directoryName
	String projectName

	transient String pomContent
	transient def pomXml

	// the single optional parent
	transient Project parent
	// dependants from a parent pom (other parents as well as projects)
	transient List<Project> childs = []

	// dependencies from this project
	transient List<Project> dependencies = []
	// projects that depend from this project
	transient List<Project> dependants = []

	transient boolean traversed


	Project(String directoryName, String pomContent, def pomXml) {
		this.directoryName = directoryName
		this.pomContent = pomContent
		this.pomXml = pomXml
		projectName = parseProjectName()
	}


	String parseParentDependencyName() {
		String groupId = pomXml?.parent?.groupId
		String artifactId = pomXml?.parent?.artifactId
		if (groupId && artifactId) {
			return "$groupId:$artifactId"
		}
		return null
	}


	private String parseProjectName() {
		String groupId = getGroupId()
		String artifactId = getArtifactId()
		return "$groupId:$artifactId"
	}


	List<String> parseDependencyNames() {
		List<String> result = []
		pomXml?.dependencies?.dependency.forEach {dep ->
			String name = "${dep?.groupId}:${dep?.artifactId}"
			result.add(name)
		}
		return result
	}


	String getGroupId() {
		String groupId = pomXml?.groupId
		if (!groupId) {
			groupId = pomXml?.parent?.groupId
		}
		if (!groupId) {
			throw new ReleaseException("Unable to determine groupId for project-directory $directoryName")
		}
		return groupId
	}


	String getVersion() {
		String version = pomXml?.version
		if (!version) {
			throw new ReleaseException("Unable to determine version for project-directory $directoryName")
		}
		return version
	}


	String getArtifactId() {
		String artifactId = pomXml?.artifactId
		if (!artifactId) {
			throw new ReleaseException("Unable to determine artifactId for project-directory $directoryName")
		}
		return artifactId
	}


	boolean parseIsParent() {
		pomXml?.packaging == 'pom'
	}


	void addDependency(Project project) {
		dependencies.add(project)
	}


	void addDependant(Project project) {
		dependants.add(project)
	}


	void addChild(Project project) {
		childs.add(project)
	}


	String toString() {
		return getProjectName()
	}

}
