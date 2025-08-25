package Meta;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
public class Config {

    private static final Properties props = new Properties();

    public Config(Path configLocation){
        try(InputStream input = Files.newInputStream(configLocation)) {
            props.load(input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    public static String get(String key){
        return props.getProperty(key);
    }

    public static String tsMuxerlocation(){
        return props.getProperty("tsMuxerLocation");
    }
    public static String ffprobeLocation(){
        return props.getProperty("ffprobeLocatione");
    }

}
