package cascade.application

import com.moandjiezana.toml.Toml

class LabSupport {


    private final static File CONFIG_FILE = new File("${System.getProperty("user.home")}/.config/lab/lab.toml")


    public static boolean configExists() {
        return CONFIG_FILE.exists()
    }

    public static String getUsername() {
        Toml toml = new Toml().read(CONFIG_FILE)
        return toml.getTable("core").getString("user")
    }

}
