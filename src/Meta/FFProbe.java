package Meta;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;



public class FFProbe {
    public static int ffProbe(Path ffProbe, Path inputFile) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
                ffProbe.toString(),
                "-v", "error",
                "-select_streams", "v:0",
                "-show_entries", "stream_side_data=dv_profile",
                "-of", "default=noprint_wrappers=1:nokey=1",
                inputFile.toString()
        );

        //ffprobe -v error -select_streams v:0 -show_entries stream_side_data=dv_profile -of default=noprint_wrappers=1:nokey=1

        pb.redirectErrorStream(true);
        Process p = pb.start();
        StringBuilder sb = new StringBuilder();
        try(BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))){

            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }

        }

        int n = p.waitFor();
        if (n != 0) {
            throw new IOException("ffprobe exited with code " + n);
        }

        return (!sb.isEmpty())? Integer.parseInt(sb.toString().trim()) : 0;


    }
}
