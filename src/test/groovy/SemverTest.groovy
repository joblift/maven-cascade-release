import com.github.zafarkhaja.semver.Version
import org.junit.Test

class SemverTest {

	@Test
	void proofOfConceptMavenSnapshot() {
		Version v1 = Version.valueOf("1.2.3-SNAPSHOT")
		Version v2 = v1.incrementPatchVersion()
		assert v2.toString() == "1.2.4"
	}

}
