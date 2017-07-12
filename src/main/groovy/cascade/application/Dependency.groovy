package cascade.application

/** Contains the dependency information during parsing the pom.xml in the Releaser */
class Dependency {

	String groupId
	String artifactId
	String version

	String versionLine
	int versionIndex

}
