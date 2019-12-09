package cascade.application

class Options {

	File projectsDirectory
	String projectStartDirectory
	String versionIncrement
	Boolean verbose
	Boolean skipVerify
	Boolean skipPostVerificationQuestion
	Boolean skipInterruptedQuestion
	List<String> additionalGroupIds
	List<String> updateOnlyGroupIds
	List<String> excludedDirectories

}
