package cascade.util

/** Simple logging */
class Log {

	private static boolean levelDebug
	private static Map<String, Integer> levelColor = [
		DEBUG: 90,
		INFO: 94,
		WARN: 93,
		ERROR: 91,
	]
	private static Map<String, Integer> textColor = [
		DEBUG: 90,
		INFO: 0,
		WARN: 0,
		ERROR: 0,
	]

	private final static Map<String, Integer> COLORS = [
		RED: 91,
		GREEN: 92
	]


	static String colored(String color, String text) {
		//return (char)27 + "[${COLORS[color]}m${text}" + (char)27 + "[0m"
		return colored(COLORS[color], text)
	}


	static String colored(int color, String text) {
		return "" + (char)27 + "[${color}m${text}" + (char)27 + "[0m"
	}


	static String red(String text) {
		return colored(COLORS.RED, text)
	}


	static String green(String text) {
		return colored(COLORS.GREEN, text)
	}


	static void debug(String message) {
		if (levelDebug) {
			println format(message, "DEBUG")
		}
	}


	static void info(String message) {
		println format(message, "INFO")
	}


	static void warn(String message) {
		println format(message, "WARN")
	}


	static void error(String message) {
		println format(message, "ERROR")
	}


	static boolean ask(String question, String ok, String no) {
		def message = format(question + " (${ok}/${no.toUpperCase()}): ", "INFO")
		println message //TODO print does not seem to work with intellij
		Scanner scanner = new Scanner(System.in)
		String input = scanner.nextLine()
		return input?.toLowerCase() == ok.toLowerCase()
		//return System.console().readLine(message)?.toLowerCase() == ok.toLowerCase()
	}


	private static String format(String message, String level) {
		return ts + (char)27 + "[" + levelColor[level] + "m" + level + (char)27 + "[" + textColor[level] + "m ${message}"
	}


	static void section(String title) {
		blankline()
		println "${ts}_____ ${title} ________________________"
	}


	private static String getTs() {
		String result = ""
		if (levelDebug) {
			String ts = new Date().format("yyyy-MM-dd-HH:mm:ss.SSS")
			result = "" + (char)27 + "[2m" + ts + (char)27 + "[0m ";
		}
		return result
	}


	static void blankline() {
		println ""
	}


	static void enableDebug(boolean enable) {
		levelDebug = enable
	}

}
