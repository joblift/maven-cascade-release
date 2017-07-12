package cascade.application

import cascade.model.OrderedProject
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

class ReleaseContext {

	public static final String FILE_NAME = "cascade-release.json"

	String projectsDirectory
	String projectStartDirectory
	List<OrderedProject> projects = []


	static ReleaseContext fromFile(File file) {
		return new JsonSlurper().parse(file, "UTF-8")
	}


	void store() {
		String json = JsonOutput.toJson(this)
		new File(projectsDirectory, FILE_NAME).write(json, "UTF-8")
	}


	void cleanup() {
		new File(projectsDirectory, FILE_NAME).delete()
	}


	void setProjects(List<OrderedProject> projects) {
		this.projects = projects
	}


	String getVersionNew(String groupId, String artifactId) {
		for (OrderedProject project : projects) {
			if (project.groupId == groupId && project.artifactId == artifactId) {
				return project.versionNew()
			}
		}
		return null
	}

}
