package cascade.model

import cascade.application.ReleaseException
import com.github.zafarkhaja.semver.Version

class OrderedProject {

	String directoryName
	String projectName
	String groupId
	String artifactId
	String version

	String versionIncrement

	boolean released
	boolean updateOnly

	/** State if project is on master, not dirty and not behind. */
	boolean verified


	String versionNew() {
		if (updateOnly) {
			return null
		}
		Version v
		try {
			v = Version.valueOf(version)
		}
		catch (Exception ex) {
			throw new ReleaseException("Invalid semver for ${directoryName} (${version})")
		}

		if (versionIncrement == 'major') {
			v = v.incrementMajorVersion()
		}
		else if (versionIncrement == 'minor') {
			v = v.incrementMinorVersion()
		}
		else if (!versionIncrement || versionIncrement == 'patch') {
			// special handling for maven 'before release' notation
			if (v.preReleaseVersion == 'SNAPSHOT') {
				v = Version.valueOf(v.normalVersion)
			}
			else {
				v = v.incrementPatchVersion()
			}
		}
		else {
			throw new ReleaseException("Version could not be increased: ${version}")
		}
		return v.toString()
	}

}
