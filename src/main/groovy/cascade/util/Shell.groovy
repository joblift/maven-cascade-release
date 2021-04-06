package cascade.util

class Shell {

	/** Executes the command and returns the completed output */
	String execute(String command) {
		return execute(command, new File(System.properties.'user.dir'))
	}


	/** Executes the command and returns the completed output */
	String execute(String command, File workingDir) throws ShellException {
		Log.debug("Performing command: '${command}'") // in '${workingDir}'
		def process = new ProcessBuilder(['sh', '-c', command])
				.directory(workingDir)
				.start()

		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))
		StringBuilder sb = new StringBuilder()

		def line = ""
		while ((line = reader.readLine()) != null) {
			sb.append(line)
		}

		process.waitFor()

		def returnCode = process.exitValue()
		if (returnCode != 0) {
			String stderr = process.getErrorStream().text.trim()
			throw new ShellException(returnCode, command, workingDir, stderr)
		}

		return sb.toString()
	}


	/** Executes the comand and writes the output from the process directly to System out/err */
	void executeInline(String command) {
		executeInline(command, new File(System.properties.'user.dir'))
	}


	/** Executes the comand and writes the output from the process directly to System out/err */
	void executeInline(String command, File workingDir) throws ShellException {
		Log.debug("Performing command: '${command}'") // in '${workingDir}'")
		def process = new ProcessBuilder(['sh', '-c', command])
				.directory(workingDir)
				.start()

		process.consumeProcessOutputStream(System.out)
		process.consumeProcessErrorStream(System.err)

		process.waitFor();

		def returnCode = process.exitValue()
		if (returnCode != 0) {
			String stderr = process.getErrorStream().text.trim()
			throw new ShellException(returnCode, command, workingDir, stderr)
		}
	}

}
