import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Flattener {
    public static void main(final String... args) throws Exception {
        Flattener work = new Flattener();
        int argIndex = 0;
        String topLevelDirectory = args[argIndex++];
        String targetDirectory = args[argIndex++];
        String orphansDirectory = args[argIndex++];
        if(topLevelDirectory.endsWith("/")) {
            topLevelDirectory = topLevelDirectory.substring(0,topLevelDirectory.length()-1);
        }
        if(targetDirectory.endsWith("/")) {
            targetDirectory = targetDirectory.substring(0,targetDirectory.length()-1);
        }
        if(orphansDirectory.endsWith("/")) {
            orphansDirectory = orphansDirectory.substring(0,orphansDirectory.length()-1);
        }
        work.start(topLevelDirectory,targetDirectory,orphansDirectory);
    }

    private void start(final String topLevelDirectory, final String targetDirectory, final String orphansDirectory) throws Exception {
        File[] files = new File(topLevelDirectory).listFiles();
        showFiles(files,topLevelDirectory,targetDirectory,orphansDirectory);
    }

    private void showFiles(File[] files,String topLevelDirectory,String targetDirectory,String orphansDirectory) throws Exception {
        List<String> topLevelParts = new ArrayList<>(Arrays.asList(topLevelDirectory.split("/")));
//        System.out.println("topLevelSize = " + topLevelParts.size());
        for (File file : files) {
            if (file.isDirectory()) {
                showFiles(file.listFiles(),topLevelDirectory,targetDirectory,orphansDirectory);
            } else {
                if("config.xml".equalsIgnoreCase(file.getName())) {
                    if(file.getAbsolutePath().equalsIgnoreCase(topLevelDirectory+"/config.xml")) {
                        continue;
                    }
//                    System.out.println(file.getAbsolutePath());
                    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                    Document doc = dBuilder.parse(file);
                    doc.getDocumentElement().normalize();
                    List<String> actualParts = new ArrayList<>(Arrays.asList(file.getAbsolutePath().split("/")));
                    String jobName = actualParts.get(actualParts.size()-2);
//                    System.out.println("jobName: " + jobName);
                    if("com.cloudbees.hudson.plugins.folder.Folder".equalsIgnoreCase(doc.getDocumentElement().getNodeName())) {
                        // always create the folder in orphans
                        System.out.println("*********");
                        System.out.println("type: " + doc.getDocumentElement().getNodeName());
                        System.out.println("from: " + file.getAbsolutePath());
                        System.out.println("  or: " + orphansDirectory + "/jobs/" + jobName + "/" +file.getName());
                        System.out.println("*********");
                        Files.createDirectories(Paths.get(orphansDirectory + "/jobs/" + jobName));
                        Files.copy(file.toPath(),(new File(orphansDirectory + "/jobs/" + jobName + "/" +file.getName())).toPath());

                        // is this a top level folder?
//                        System.out.println("actualPartsSize = " + actualParts.size());
                        if(topLevelParts.size() + 3 == actualParts.size()) {
                            String specificTargetDirectory = targetDirectory + "";
                            System.out.println("*********");
                            System.out.println("type: " + doc.getDocumentElement().getNodeName());
                            System.out.println("from: " + file.getAbsolutePath());
                            System.out.println("  to: " + specificTargetDirectory + "/jobs/" + jobName + "/" +file.getName());
                            System.out.println("*********");
                            Files.createDirectories(Paths.get(specificTargetDirectory + "/jobs/" + jobName));
                            Files.copy(file.toPath(),(new File(specificTargetDirectory + "/jobs/" + jobName + "/" +file.getName())).toPath());
                        }
                    } else {
                        String[] fileSplit = file.getAbsolutePath().split(topLevelDirectory+"/jobs/");
//                        System.out.println("postsplit: " + fileSplit[1]);
                        List<String> postsplitList = new ArrayList<>(Arrays.asList(fileSplit[1].split("/")));
                        String specificTargetDirectory = targetDirectory + "";
                        if(postsplitList.size() >= 4) {
                            //need to flatten
//                            System.out.println("####  size = " + postsplitList.size() + "; 0 = " + postsplitList.get(0));
                            specificTargetDirectory = targetDirectory + "/jobs/" + postsplitList.get(0);
                        }
                        System.out.println("*********");
                        System.out.println("type: " + doc.getDocumentElement().getNodeName());
                        System.out.println("from: " + file.getAbsolutePath());
                        System.out.println("  to: " + specificTargetDirectory + "/jobs/" + jobName + "/" +file.getName());
                        System.out.println("*********");
                        Files.createDirectories(Paths.get(specificTargetDirectory + "/jobs/" + jobName));
                        try {
                            Files.copy(file.toPath(), (new File(specificTargetDirectory + "/jobs/" + jobName + "/" + file.getName())).toPath());
                        } catch (java.nio.file.FileAlreadyExistsException e) {
                            specificTargetDirectory = orphansDirectory + "";
                            System.out.println("*********");
                            System.out.println("type: " + doc.getDocumentElement().getNodeName());
                            System.out.println("from: " + file.getAbsolutePath());
                            System.out.println("fail: " + specificTargetDirectory + "/jobs/" + jobName + "/" +file.getName());
                            System.out.println("*********");
                        }
                    }
                }
            }
        }
    }
}