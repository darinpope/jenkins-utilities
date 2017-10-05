import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Flattener {
    public static void main(final String... args) throws Exception {
        Flattener work = new Flattener();
        int argIndex = 0;
        String topLevelDirectory = args[argIndex++];
        String targetDirectory = args[argIndex++];
        work.start(topLevelDirectory,targetDirectory);
    }

    private void start(final String topLevelDirectory, final String targetDirectory) throws Exception {
        File[] files = new File(topLevelDirectory).listFiles();
        showFiles(files,topLevelDirectory,targetDirectory);
    }

    private void showFiles(File[] files,String topLevelDirectory,String targetDirectory) throws Exception {
        List<String> topLevelParts = new ArrayList<>(Arrays.asList(topLevelDirectory.split("/")));
//        System.out.println("topLevelSize = " + topLevelParts.size());
        for (File file : files) {
            if (file.isDirectory()) {
                showFiles(file.listFiles(),topLevelDirectory,targetDirectory);
            } else {
                if("config.xml".equalsIgnoreCase(file.getName())) {
                    if(file.getAbsolutePath().equalsIgnoreCase(topLevelDirectory+"/config.xml")) {
                        continue;
                    }
                    System.out.println(file.getAbsolutePath());
                    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                    Document doc = dBuilder.parse(file);
                    doc.getDocumentElement().normalize();
                    List<String> actualParts = new ArrayList<>(Arrays.asList(file.getAbsolutePath().split("/")));
                    String jobName = actualParts.get(actualParts.size()-2);
                    System.out.println("jobName: " + jobName);
                    if("com.cloudbees.hudson.plugins.folder.Folder".equalsIgnoreCase(doc.getDocumentElement().getNodeName())) {
                        // is this a top level folder?
//                        System.out.println("actualPartsSize = " + actualParts.size());
                        if(topLevelParts.size() + 3 == actualParts.size()) {
                            String specificTargetDirectory = targetDirectory + "";
//                            Files.copy(file.toPath(),(new File(specificTargetDirectory + file.getName())).toPath());
                            System.out.println("*********");
                            System.out.println("type: " + doc.getDocumentElement().getNodeName());
                            System.out.println("from: " + file.getAbsolutePath());
                            System.out.println("  to: " + specificTargetDirectory + "/jobs/" + jobName + "/" +file.getName());
                            System.out.println("*********");
                        }
                    } else {
                        String[] fileSplit = file.getAbsolutePath().split(topLevelDirectory+"/jobs/");
                        System.out.println("postsplit: " + fileSplit[1]);
                        String specificTargetDirectory = targetDirectory + "";
//                        Files.copy(file.toPath(),(new File(specificTargetDirectory + file.getName())).toPath());
                        System.out.println("*********");
                        System.out.println("type: " + doc.getDocumentElement().getNodeName());
                        System.out.println("from: " + file.getAbsolutePath());
                        System.out.println("  to: " + targetDirectory + "/jobs/" + jobName + "/" +file.getName());
                        System.out.println("*********");
                    }
                }
            }
        }
    }
}