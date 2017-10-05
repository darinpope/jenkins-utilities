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
        String jobType = args[argIndex++];
        if(topLevelDirectory.endsWith("/")) {
            topLevelDirectory = topLevelDirectory.substring(0,topLevelDirectory.length()-1);
        }
        if(targetDirectory.endsWith("/")) {
            targetDirectory = targetDirectory.substring(0,targetDirectory.length()-1);
        }
        if(orphansDirectory.endsWith("/")) {
            orphansDirectory = orphansDirectory.substring(0,orphansDirectory.length()-1);
        }
        jobType = jobType.toLowerCase();
        work.start(topLevelDirectory,targetDirectory,orphansDirectory,jobType);
    }

    private void start(final String topLevelDirectory, final String targetDirectory, final String orphansDirectory, final String jobType) throws Exception {
        File[] files = new File(topLevelDirectory).listFiles();
        showFiles(files,topLevelDirectory,targetDirectory,orphansDirectory,jobType);
    }

    private void showFiles(File[] files,String topLevelDirectory,String targetDirectory,String orphansDirectory,String jobType) throws Exception {
        List<String> topLevelParts = new ArrayList<>(Arrays.asList(topLevelDirectory.split("/")));
//        System.out.println("topLevelSize = " + topLevelParts.size());
        for (File file : files) {
            if (file.isDirectory()) {
                showFiles(file.listFiles(),topLevelDirectory,targetDirectory,orphansDirectory,jobType);
            } else {
//                if(file.getAbsolutePath().contains("/modules/")) {
//                    continue;
//                }
                if("config.xml".equalsIgnoreCase(file.getName())) {
//                    System.out.println(file.getAbsolutePath());
                    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                    Document doc = dBuilder.parse(file);
                    doc.getDocumentElement().normalize();
                    String fileJobType = doc.getDocumentElement().getNodeName().trim();
                    List<String> actualParts = new ArrayList<>(Arrays.asList(file.getAbsolutePath().split("/")));
                    String jobName = actualParts.get(actualParts.size()-2);
                    System.out.println("jobName: " + jobName);
                    String[] fileSplit = file.getAbsolutePath().split(topLevelDirectory+"/jobs/");
                    if("com.cloudbees.hudson.plugins.folder.Folder".equalsIgnoreCase(fileJobType)) {
                        if(file.getAbsolutePath().equalsIgnoreCase(topLevelDirectory+"/config.xml")) {
                            String orphanFile = orphansDirectory + "/jobs/config.xml";
                            String orphanPath = orphanFile.substring(0,orphanFile.lastIndexOf("/"));
                            System.out.println("*********");
                            System.out.println("type: " + fileJobType);
                            System.out.println("from: " + file.getAbsolutePath());
                            System.out.println("  or: " + orphanFile);
                            System.out.println("*********");
                            Files.createDirectories(Paths.get(orphanPath));
                            Files.copy(file.toPath(),(new File(orphanFile)).toPath());
                            continue;
                        }
                        // always create the folder in orphans
                        String orphanFile = orphansDirectory + "/jobs/" + fileSplit[1];
                        String orphanPath = orphanFile.substring(0,orphanFile.lastIndexOf("/"));
                        System.out.println("*********");
                        System.out.println("type: " + fileJobType);
                        System.out.println("from: " + file.getAbsolutePath());
                        System.out.println("  or: " + orphanFile);
                        System.out.println("*********");
                        Files.createDirectories(Paths.get(orphanPath));
                        Files.copy(file.toPath(),(new File(orphanFile)).toPath());

                        // is this a top level folder?
//                        System.out.println("actualPartsSize = " + actualParts.size());
                        if(topLevelParts.size() + 3 == actualParts.size()) {
                            String specificTargetDirectory = targetDirectory + "";
                            System.out.println("*********");
                            System.out.println("type: " + fileJobType);
                            System.out.println("from: " + file.getAbsolutePath());
                            System.out.println("  to: " + specificTargetDirectory + "/jobs/" + jobName + "/" +file.getName());
                            System.out.println("*********");
                            Files.createDirectories(Paths.get(specificTargetDirectory + "/jobs/" + jobName));
                            Files.copy(file.toPath(),(new File(specificTargetDirectory + "/jobs/" + jobName + "/" +file.getName())).toPath());
                        }
                    } else {
                        if ("p".equalsIgnoreCase(jobType)) {
                            if (!"flow-definition".equalsIgnoreCase(fileJobType)) {
                                continue;
                            }
                        } else {
                            if ("flow-definition".equalsIgnoreCase(fileJobType)) {
                                continue;
                            }
                        }
//                        System.out.println("postsplit: " + fileSplit[1]);
                        List<String> postsplitList = new ArrayList<>(Arrays.asList(fileSplit[1].split("/")));
                        String specificTargetDirectory = targetDirectory + "";
                        if(postsplitList.size() >= 4) {
                            //need to flatten
//                            System.out.println("####  size = " + postsplitList.size() + "; 0 = " + postsplitList.get(0));
                            specificTargetDirectory = targetDirectory + "/jobs/" + postsplitList.get(0);
                        }
                        System.out.println("*********");
                        System.out.println("type: " + fileJobType);
                        System.out.println("from: " + file.getAbsolutePath());
                        System.out.println("  to: " + specificTargetDirectory + "/jobs/" + jobName + "/" +file.getName());
                        System.out.println("*********");
                        try {
                            Files.createDirectories(Paths.get(specificTargetDirectory + "/jobs/" + jobName));
                            Files.copy(file.toPath(), (new File(specificTargetDirectory + "/jobs/" + jobName + "/" + file.getName())).toPath());
                        } catch (java.nio.file.FileAlreadyExistsException e) {
                            specificTargetDirectory = orphansDirectory + "";
                            System.out.println("*********");
                            System.out.println("type: " + fileJobType);
                            System.out.println("from: " + file.getAbsolutePath());
                            System.out.println("fail: " + specificTargetDirectory + "/jobs/" + fileSplit[1]);
                            System.out.println("*********");
                            String orphanFileName = specificTargetDirectory + "/jobs/" + fileSplit[1];
                            String orphanPath = orphanFileName.substring(0,orphanFileName.lastIndexOf("/"));
                            Files.createDirectories(Paths.get(orphanPath));
                            Files.copy(file.toPath(), (new File(orphanFileName)).toPath());
                        }
                    }
                }
            }
        }
    }
}