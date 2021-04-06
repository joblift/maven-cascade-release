package cascade.application

import org.apache.commons.cli.Option

class OptionParser {

	Options parse(String[] args) {
		Map<String, Object> map = readArguments(args)

		File projectsDirectory = determineProjectsDirectory(map)
		String projectDirectoryStart = determineProjectDirectoryStart(map)
		String versionIncrement = determineVersionIncrement(map)
		List<String> additionalGroupIds = determineGroupIds(map?.g)
		List<String> updateOnlyGroupIds = determineGroupIds(map?.u)
		List<String> excludedDirectories = determineExcludedDirectories(map?.e)
		Boolean skipInterruptedQuestion = map?.r ? map.r == 'y' ? true : map.r == 'n' ? false : null : null;

		return new Options(projectsDirectory: projectsDirectory, projectStartDirectory: projectDirectoryStart, versionIncrement: versionIncrement,
			verbose: map.v, additionalGroupIds: additionalGroupIds, updateOnlyGroupIds: updateOnlyGroupIds, excludedDirectories: excludedDirectories,
			skipVerify: map.x, skipPostVerificationQuestion: map.q, skipInterruptedQuestion: skipInterruptedQuestion,
			message: map.m, mr: map.t)
	}


	private Map<String, Object> readArguments(String[] args) {
		def cli = new CliBuilder(usage: 'cascade-release [options] <projects-directory> <start-project>', header: 'Options:')
		cli.p(longOpt: 'projects-directory', 'The directory where all repositories are.', required: true, args: 1)
		cli.s(longOpt: 'start-project', 'The project-directory from which the release-graph should start.', required: true, args: 1)
		cli.m(longOpt: 'message', 'Message, that will be used as prefix in commits.', required: false, args: 1)
		cli.t(longOpt: 'mr', 'Will create a merge-request for projects that are updated only (instead of pushing to master). Uses https://github.com/zaquestion/lab.', required: false)

		cli.i(longOpt: 'increment-version', 'Available options: major, minor, patch (default).', required: false, args: 1)
		cli.g(longOpt: 'additional-groupids',
			'Only projects with the groupId of the start-project will be analyzed, additional groupIds can be passed comma-separated using this argument.',
			required: false, args: 1)
		cli.u(longOpt: 'update-only-groupid',
			'The pom.xml file of matching projects with the passed comnma-separated groupIds will only be updated (not released).', required: false, args: 1)

		cli.e(longOpt: 'exclude', 'Comma separated list of directory-names, that should be ignored', required: false, args: 1)

		cli.h(longOpt: 'help', 'usage information', required: false)
		cli.v(longOpt: 'verbose', 'Verbose logging', required: false)

		cli.x(longOpt: 'skip-verify', 'Will skip the git project verification', required: false)
		cli.q(longOpt: 'skip-post-verification-question', 'Will skip the continue-question after the projects have been validated', required: false)
		cli.r(longOpt: 'skip-interrupted-question', 'Will skip the interrupted-question if a cacade-release.json has been found.', required: false, args: 1)


		try {
			OptionAccessor opts = cli.parse(args)
			if (!opts) {
				System.exit(2)
			}
			if (opts.h) {
				cli.usage()
				System.exit(3)
			}
			if (opts.v) {
				Log.enableDebug(true)
			}
			
			Map<String, String> collectedOptions = cli.options.getOptions()
				.findAll({opts[it.key]})
				.collectEntries({
				def value = opts[it.key]
				if (it.args && it.args == Option.UNLIMITED_VALUES) {
					def items = opts["${it.key}s"]
					value = [:]
					for (i in items) {
						String[] kv = i.split("=")
						value.put(kv[0], kv[1])
					}
				}
				return [it.key, value]
			}).asImmutable()

			return collectedOptions
		}
		catch (Exception ex) {
			throw new ReleaseException("Unable to parse arguments")
		}
	}


	private File determineProjectsDirectory(Map<String, String> opts) {
		File result = new File(opts.p)
		if (!result.exists() || !result.isDirectory()) {
			throw new ReleaseException("invalid projects-directory argument")
		}
		return result
	}


	private String determineProjectDirectoryStart(Map<String, String> opts) {
		File projectDirectoryStart = determineProjectsDirectory(opts)
		if (!new File(projectDirectoryStart, opts.s).isDirectory()) {
			throw new ReleaseException("invalid start-project argument")
		}
		return opts.s
	}


	private String determineVersionIncrement(Map<String, String> opts) {
		if (opts.i) {
			String inc = opts.i.toLowerCase()
			if (inc == "major" || inc == "minor" || inc == "patch") {
				return inc
			}
		}
		return null
	}


	private List<String> determineGroupIds(String text) {
		return splitByComma(text)
	}


	private determineExcludedDirectories(String text) {
		return splitByComma(text)
	}


	private List<String> splitByComma(String text) {
		List<String> result = []
		if (text) {
			text.tokenize(',').forEach {result.add(it)}
		}
		return result
	}

}
