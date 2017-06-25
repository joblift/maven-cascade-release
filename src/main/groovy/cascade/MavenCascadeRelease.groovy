package cascade

class MavenCascadeRelease {

	public static main(String[] args) {
		// check projects-dir
		// check project-dir
		// check stored progress
		// determine all directories with pom > map<dirname, parsed pom>
		// generate graph
		// determine affected graph
		// check if projects are clean/commited, pulled
		// print affected graph with new version number
		// print release order
		// store progress in file
		// ask continue

		// start release in release order
		// for each release
		// - update pom.xml
		// - mvn release..
		// - store progress

		// cleanup progress file

		File projectsDirectory = determineProjectsDirectory(args)
		File projectStart = determineProjectStart(args)
		println "finished '$projectsDirectory', '$projectStart'"

		/*
		File projectDirectory = new File(args[0])
		File outputDirectory = new File(args[1])
		String[] RELATED_GROUP_IDS = args[2].tokenize(",")
		String[] FILTERED_PROJECTS = args.size() > 3 ? args[3]?.tokenize(",") : []


		def poms = []

		// collect poms
		projectDirectory.eachFile {
			if (it.isDirectory()) {
				def filePom = new File(it, "pom.xml");
				if (filePom.exists()) {
					poms << filePom
				}
			}
		}

		def assocs = []
		for (it in poms) {
			def project = new XmlSlurper().parse(it)
			if ((project?.parent?.groupId in RELATED_GROUP_IDS) && !(project?.artifactId in FILTERED_PROJECTS)) {
				println(it)
				boolean isService = false
				// check service (dropwizard indicator)
				project?.build?.plugins?.plugin?.each {
					if (it?.artifactId.text() == 'maven-shade-plugin') {
						isService = true
					}
				}

				def projectArtifact = project.artifactId.text()
				//def projectPackaging = project?.packaging.text() ?: 'jar'
				def color = isService ? "[shape=box,color=green]" : "[shape=ellipse,color=red]"
				//def version = project.version.text() ?: 'jar'
				assocs << "    \"${projectArtifact}\" ${color}"

				// iterate thru dependencies
				project?.dependencies?.dependency.findAll {it.groupId.text() in RELATED_GROUP_IDS}.each {
					def depArtifact = it.artifactId.text()
					//def depVersion = it?.version.text() ?: 'jar'
					def dep = "    \"${projectArtifact}\"  -> \"${depArtifact}\""
					assocs << dep
				}
			}
			println ''
		}


		String today = new Date().format("yyyy-MM-dd")
		def dotFile = new File(outputDirectory, "graph-${today}.dot");
		if (dotFile.exists()) {
			dotFile.delete();
		}

		dotFile << 'digraph dependencies {\n'
		assocs.each {
			dotFile << it + ';\n'
		}
		dotFile << '}'
		*/
	}


	static File determineProjectsDirectory(String[] args) {
		File result = new File(args[0])
		if (!result.exists() || !result.isDirectory()) {
			throw new ReleaseException("invalid projects-directory argument")
		}
		return result
	}


	static File determineProjectStart(String[] args) {
		File result = new File(args[1])
		File projectsDirectory = determineProjectsDirectory(args)

		if (!result.exists() || !result.isDirectory() || !projectsDirectory.listFiles().contains(result)) {
			throw new ReleaseException("invalid start-project argument")
		}
		return result
	}

}
