import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainClass {

    private static Pattern reginclude = Pattern
            .compile("[ ]*include::([a-zA-Z0-9_\\.\\/]*)[\\[][\\s\\S]*tag[ ]*=[ ]*[\"]([a-z][a-zA-Z0-9_]*)[\"][\\s\\S]*[\\]]");

    private static Pattern regendinclude = Pattern.compile("[ ]*endinclude[\\[][ ]*[\\]]");

    public static void main(String[] args) {

        String sourceDir = args[0];;
        String preprocessedDir = args[1];

        try {

            if(new File(sourceDir).isDirectory()) {
                for (File file : new File(sourceDir).listFiles()) {

                    //if (!(new File(new File(preprocessedDir), file.getName())).exists()) {

                        byte[] dataArr = FileUtils.readFileToByteArray(file);
                        String data = new String(dataArr, StandardCharsets.UTF_8);
                        String lines[] = data.split("\\r\\n");

                        if(!lines[0].contains("no-preproc")) {

                            String fileToProcessPath = file.getParentFile().getAbsolutePath();

                            FileWriter fw = new FileWriter(new File(preprocessedDir + "/" + file.getName()));

                            String readState = "normal";

                            for (String line : lines) {
                                if (readState.equals("normal")) {
                                    fw.append(line + "\n");
                                    Matcher parseline = reginclude.matcher(line);
                                    if (parseline.find()) {
                                        outputInclude(fw, fileToProcessPath + '/' + parseline.group(1),
                                                parseline.group(2));
                                        readState = "include";
                                    }
                                } else if (readState.equals("include")) {
                                    Matcher parseline = regendinclude.matcher(line);
                                    if (parseline.find()) {
                                        readState = "normal";
                                        fw.append(line + "\n");
                                    }
                                }
                            }

                            fw.flush();
                            fw.close();

                            //addHashToFile((new File(new File(preprocessedDir), file.getName())));
                        }
                    //}
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private static void outputInclude(FileWriter fw, String fileName, String tag) throws Exception {
        byte[] dataArr = FileUtils.readFileToByteArray(new File(fileName));
        String data = new String(dataArr, StandardCharsets.UTF_8);
        String lines[] = data.split("\\r\\n");

        String readStateDest = "normal";

        for (String line : lines) {

            if (readStateDest.equals("normal")) {
                if (line.indexOf("tag::" + tag + "[]") != -1) {
                    readStateDest = "tagged";
                }
            } else if (readStateDest.equals("tagged")) {
                if (line.indexOf("end::" + tag + "[]") != -1) {
                    readStateDest = "normal";
                } else {
                    fw.append(line + "\n");
                }
            }
        }
    }

    private static void addHashToFile(File file) throws Exception {
        try (InputStream is = Files.newInputStream(Paths.get(file.getAbsolutePath()))) {
            String md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(is);

            BufferedReader read= new BufferedReader(new FileReader(file));
            ArrayList list = new ArrayList();

            String dataRow = read.readLine();
            while (dataRow != null){
                list.add(dataRow);
                dataRow = read.readLine();
            }

            FileWriter writer = new FileWriter(file); //same as your file name above so that it will replace it
            writer.append("# hash: " + md5);

            for (int i = 0; i < list.size(); i++){
                writer.append("\n" + list.get(i));
            }

            writer.flush();
            writer.close();
        }
    }

    private static boolean checkHashForFile(String fileName) throws Exception {
        byte[] dataArr = FileUtils.readFileToByteArray(new File(fileName));
        String data = new String(dataArr, StandardCharsets.UTF_8);
        String lines[] = data.split("\\r\\n");

        int i = 0;
        if(lines[i].contains("hash:")) {
            i = 1;

            String allLinesExceptFirst = "";

            for (int j = i; j < lines.length; j++) {
                allLinesExceptFirst = allLinesExceptFirst + lines[j] + "\n";
            }

            try (InputStream is = IOUtils.toInputStream(allLinesExceptFirst, StandardCharsets.UTF_8)) {
                String md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(is);

                String oldHash = lines[0].substring(lines[0].indexOf("hash:") + 6);
                if (oldHash.equals(md5)) {
                    return true;
                } else {
                    return false;
                }
            }
        }

        return false;
    }
}
