package cascade.application

class Options {

	File projectsDirectory
	String projectStartDirectory
	String versionIncrement
	String message
	Boolean mr
	Boolean mrAutomerge
	String mrUsername
	Boolean verbose
	Boolean skipVerify
	Boolean skipPostVerificationQuestion
	Boolean skipInterruptedQuestion
	List<String> additionalGroupIds
	List<String> updateOnlyGroupIds
	List<String> excludedDirectories

	public String prefixMessage() {
		message ? message + " " : ""
	}

}
