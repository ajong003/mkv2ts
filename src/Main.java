import Meta.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;


//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

    public static void main(String[] args) throws Exception {
        Properties config = new Properties();
        //check if config exists

        Path root= Paths.get(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
        Path cfgFile = root.resolve("app.properties");
        if(Files.exists(cfgFile)){
            try(InputStream input = Files.newInputStream(cfgFile)){
                config.load(input);
            } catch (IOException e) {
                throw new IOException(e);
            }
        }else{
            //generate default config file
            Properties defaultConfig = new Properties();
            defaultConfig.setProperty("tsMuxerPath", root.resolve("tsmuxer.exe").toString());
            defaultConfig.setProperty("ffprobePath", root.resolve("ffprobe.exe").toString());
            defaultConfig.store(Files.newOutputStream(cfgFile)," mkv2ts Application cfg");
            config = defaultConfig;
        }

        Path tsMuxerPath = Paths.get(config.getProperty("tsMuxerPath"));
        Path ffprobePath = Paths.get(config.getProperty("ffprobePath"));
        //input and output can reference file or subdirectory
        Path input = Paths.get(args[0]);
        Path output = Paths.get(args[1]);
        //
        if(Files.isRegularFile(output)){
            output = output.getParent();
        }


        System.out.println("outputDir=" + output + " writable=" + Files.isWritable(output));


        //needs revision if input is file
        List<Path> mkvList = Utilities.findMkv(input);

        //add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("JVM shutting down, destroying process...");

        }));

        for(int i = 0; i < mkvList.size(); i++){

            Path current = mkvList.get(i);
            System.out.println("Now converting: "+ current);

            int dvProfile = FFProbe.ffProbe(ffprobePath, current);
            System.out.println("DV Profile: " + dvProfile);

            List<Track> trackList = TsMuxer.analyzeWithTsMuxer(tsMuxerPath,
                    current);
            System.out.println(trackList);

            Path metaPath = MetaBuilder.buildAndWriteMeta(tsMuxerPath,
                    current,
                    output, true);

            Path outputFile = output.resolve(MetaBuilder.replaceExt(current, ".ts").getFileName());
            TsMuxer.convert(tsMuxerPath, metaPath, outputFile);
        }


        //Path ffProbePath = Paths.get("C:\\Users\\Alex\\Desktop\\ffprobe.exe");

        //Path input = Paths.get("E:\\Plex\\Series\\Snowpiercer.S04E05.HDR.2160p.WEB.h265-Ginnungagap\\snowpiercer.s04e05.hdr.2160p.web.h265-ginnungagap.mkv");
        //Path outputDir = Paths.get("E:/Plex/Testoutput");

        //Path outputFile = outputDir.resolve(MetaBuilder.replaceExt(input,".ts").getFileName());

        //Path tsMuxeR = Paths.get("C:/Users/Alex/Desktop/tsMuxeR.exe");






    }


}