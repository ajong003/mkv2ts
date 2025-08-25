package Meta;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



//
public class TsMuxer {

    public static List<Track> analyzeWithTsMuxer(Path tsMuxerExe, Path inputMkv)
            throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
                tsMuxerExe.toString(),
                inputMkv.toString()
        );

        pb.redirectErrorStream(true);
        Process p = pb.start();

        //blocking
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            String s;
            while ((s = br.readLine()) != null) lines.add(s);
        }
        int code = p.waitFor();


        if (code != 0) throw new IOException("tsMuxeR analyze failed, exit " + code);

        return parseTracks(lines);
    }

    private static List<Track> parseTracks(List<String> lines) {
        List<Track> out = new ArrayList<>();
        Track cur = null;
        Pattern pid  = Pattern.compile("^\\s*Track ID:\\s+(\\d+)");
        Pattern ptyp = Pattern.compile("^\\s*Stream type:\\s+(.+)$");
        Pattern psid = Pattern.compile("^\\s*Stream ID:\\s+(.+)$");
        Pattern plang= Pattern.compile("^\\s*Stream lang:\\s+([a-zA-Z]{2,3})");

        for (String ln : lines) {
            Matcher m1 = pid.matcher(ln);
            Matcher m2 = ptyp.matcher(ln);
            Matcher m3 = psid.matcher(ln);
            Matcher m4 = plang.matcher(ln);

            if (m1.find()) {
                if (cur != null) out.add(cur);
                cur = new Track();
                cur.id = Integer.parseInt(m1.group(1));
            } else if (m2.find() && cur != null) {
                cur.type = m2.group(1).trim();
            } else if (m3.find() && cur != null) {
                cur.streamId = m3.group(1).trim();
            } else if (m4.find() && cur != null) {
                cur.lang = m4.group(1).toLowerCase(Locale.ROOT);
            }
        }
        if (cur != null) out.add(cur);
        return out;
    }


    public static void convert(Path tsMuxerEXE, Path metaFile, Path output) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(tsMuxerEXE.toString(),
                metaFile.toString(),
                output.toString());
        pb.redirectErrorStream(true);

        Path logPath = metaFile.resolveSibling("tsmuxer.log");
        pb.redirectOutput(logPath.toFile()); // or a temp file

        Process p = pb.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (p.isAlive()) {
                p.destroy();
                try {
                    if (!p.waitFor(2, java.util.concurrent.TimeUnit.SECONDS)) {
                        p.destroyForcibly();
                    }
                } catch (InterruptedException ignored) {}
            }
        }));

        try { p.getOutputStream().close(); } catch (Exception ignore) {}
        int code = p.waitFor();

        if (code != 0) {
            throw new IOException("tsMuxeR exited with code " + code +
                    " (see log: " + logPath + ")");
        }

//        long timeOut = 20000;
//
//
//        InputStream inputStream = p.getInputStream();
//        byte[] buf = new byte[8192];
//
//        long start = System.currentTimeMillis();
//        long lastActivity = start;

//        while (true) {
//
//
//            if (inputStream.available() > 0) {
//                while (inputStream.available() > 0) {
//                    int n = inputStream.read(buf, 0, Math.min(inputStream.available(), buf.length));
//                    if (n < 0) break; // EOF
//                }
//                lastActivity = System.currentTimeMillis();
//
//            }
//            long now = System.currentTimeMillis();
//            if (now - lastActivity > timeOut) {
//                kill(p);
//                throw new IOException("Idle timeout: no output for " + timeOut + " milliseconds");
//            }
//
//
//            if (p.waitFor(5, TimeUnit.SECONDS)) {
//                int code = p.exitValue();
//                if (code != 0) throw new IOException("tsMuxeR exited with code " + code);
//                return;
//            }
//        }


    }

    static void kill(Process p) {
        try { p.destroy(); } catch (Exception ignored) {}
        try { Thread.sleep(300); } catch (InterruptedException ignored) {}
        if (p.isAlive()) try { p.destroyForcibly(); } catch (Exception ignored) {}
    }
}

