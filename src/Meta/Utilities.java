package Meta;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class Utilities {

    public static List<Path> findMkv(Path dir) throws FileNotFoundException {

        if(Files.isDirectory(dir)){
            try (Stream<Path> files = Files.list(dir)) {
                return files
                        .filter(p -> Files.isRegularFile(p))
                        .filter(p -> p.toString().toLowerCase().endsWith(".mkv"))
                        .toList();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }else if(Files.isRegularFile(dir)){
            return Collections.singletonList(dir);
        }
        throw new FileNotFoundException();

    }


}
