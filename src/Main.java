import Meta.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

    public static void main(String[] args) throws Exception {

        // args
        Path tsmux = Paths.get(args[0]);
        Path ffprobe = Paths.get(args[1]);
        Path inputDir = Paths.get(args[2]);
        Path outputDir = Paths.get(args[3]);



        System.out.println("outputDir=" + outputDir + " writable=" + Files.isWritable(outputDir));

        List<Path> mkvList = Utilities.findMkv(inputDir);

        for(int i = 0; i < mkvList.size(); i++){

            Path current = mkvList.get(i);
            System.out.println("Now converting: "+ current);

            int dvProfile = FFProbe.ffProbe(ffprobe, current);
            System.out.println("DV Profile: " + dvProfile);

            List<Track> trackList = TsMuxer.analyzeWithTsMuxer(tsmux,
                    current);
            System.out.println(trackList);

            Path metaPath = MetaBuilder.buildAndWriteMeta(tsmux,
                    current,
                    outputDir, true);

            Path output = outputDir.resolve(MetaBuilder.replaceExt(current, ".ts").getFileName());
            TsMuxer.convert(tsmux, metaPath, output);
        }


        //Path ffProbePath = Paths.get("C:\\Users\\Alex\\Desktop\\ffprobe.exe");

        //Path input = Paths.get("E:\\Plex\\Series\\Snowpiercer.S04E05.HDR.2160p.WEB.h265-Ginnungagap\\snowpiercer.s04e05.hdr.2160p.web.h265-ginnungagap.mkv");
        //Path outputDir = Paths.get("E:/Plex/Testoutput");

        //Path outputFile = outputDir.resolve(MetaBuilder.replaceExt(input,".ts").getFileName());

        //Path tsMuxeR = Paths.get("C:/Users/Alex/Desktop/tsMuxeR.exe");






    }


}