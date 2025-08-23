package Meta;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

import static Meta.TsMuxer.analyzeWithTsMuxer;

public class MetaBuilder {

    //default meta params
    private static final String MUXOPT =
            "MUXOPT --no-pcr-on-video-pid --new-audio-pes --vbr --vbv-len=500";


    /** Build and write a .meta next to the output .ts. Returns the meta path. */
    public static Path buildAndWriteMeta(Path tsMuxerExe, Path inputMkv, Path outputDir,
                                         boolean includeSubtitle) throws Exception {
        // 1) analyze
        List<Track> tracks = analyzeWithTsMuxer(tsMuxerExe, inputMkv);

        // 2) choose tracks
        Track video = tracks.stream()
                .filter(t -> t.streamId != null && t.streamId.contains("V_MPEGH/ISO/HEVC"))
                .min(Comparator.comparingInt(t -> t.id))
                .orElseThrow(() -> new IllegalStateException("No HEVC video track found"));

        System.out.println(video);

        Track audio = pickPreferredAudio(tracks);
        if (audio == null) throw new IllegalStateException("No audio track found");
        System.out.println(audio);

        Track sub = includeSubtitle ? pickTextSubtitle(tracks) : null;

        // 3) build meta text
        String metaText = renderMeta(inputMkv, video, audio, sub);

        // 4) write meta file (same name as TS, .meta extension)
        Path metaPath = outputDir.resolve(replaceExt(inputMkv, ".meta").getFileName());
        Files.createDirectories(metaPath.getParent());
        Files.writeString(metaPath, metaText, StandardCharsets.US_ASCII);
        return metaPath;
    }

    // ---- render the .meta text ----
    private static String renderMeta(Path inputMkv, Track video, Track audio, Track sub) {
        String in = inputMkv.toString().replace("\"", "\\\""); // quote-safe
        StringBuilder sb = new StringBuilder();
        String vLang = (video.lang != null && !video.lang.isBlank()) ? video.lang : "und";
        sb.append(MUXOPT).append("\n");
        sb.append(String.format("%s, \"%s\", track=%d, lang=%s%n",video.streamId, in, video.id, vLang));

        // If you resolved language from tsMuxeR/MediaInfo, use it; else default to "eng" or "und"
        String aLang = (audio.lang != null && !audio.lang.isBlank()) ? audio.lang : "eng";
        sb.append(String.format("%s, \"%s\", track=%d, lang=%s%n", audio.streamId, in, audio.id, aLang));

        if (sub != null) {
            String sLang = (sub.lang != null && !sub.lang.isBlank()) ? sub.lang : "eng";
            // Warning: SRT→PGS rasterization slows muxing
            sb.append(String.format(
                    "S_TEXT/UTF8, \"%s\", font-name=\"Arial\", font-size=65, font-color=0xffffffff, " +
                            "bottom-offset=24, font-border=5, text-align=center, video-width=1920, " +
                            "video-height=1080, fps=24, track=%d, lang=%s%n", in, sub.id, sLang));
        }
        return sb.toString();
    }


    // ---- selection helpers ----
    private static Track pickPreferredAudio(List<Track> tracks) {
        String[] pref = {"E-AC3", "AC3", "DTS", "AAC"};
        for (String p : pref) {
            for (Track t : tracks) {
                if ((t.type != null && t.type.startsWith(p)) ||
                        (t.streamId != null && t.streamId.startsWith("A_"))) {
                    return t;
                }
            }
        }
        for (Track t : tracks) if (t.streamId != null && t.streamId.startsWith("A_")) return t;
        return null;
    }

    private static Track pickTextSubtitle(List<Track> tracks) {
        for (Track t : tracks) {
            if ((t.streamId != null && t.streamId.equals("S_TEXT/UTF8")) ||
                    (t.type != null && t.type.toUpperCase().contains("SRT"))) {
                return t;
            }
        }
        return null;
    }

    public static Path replaceExt(Path p, String newExt) {
        String name = p.getFileName().toString();
        int dot = name.lastIndexOf('.');
        String base = (dot > 0) ? name.substring(0, dot) : name;
        return p.getParent().resolve(base + newExt);
    }
}
