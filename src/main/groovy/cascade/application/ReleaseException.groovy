package cascade.application

class ReleaseException extends RuntimeException {

	ReleaseException(String message) {
		super(message)
	}


	ReleaseException(String message, Throwable cause) {
		super(message, cause)
	}


	ReleaseException(Throwable cause) {
		super(cause)
	}

}
