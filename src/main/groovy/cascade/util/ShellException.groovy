package cascade.util

class ShellException extends Exception {

	int returnCode
	String command
	File workingDir
	String stderr


	ShellException(int returnCode, String command, File workingDir, String stderr) {
		super("Command exited with code ${returnCode}: '${command}' in '${workingDir}'")
		this.returnCode = returnCode
		this.command = command
		this.workingDir = workingDir
		this.stderr = stderr
	}

}
