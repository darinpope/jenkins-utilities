import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class Flattener {
    public static void main(final String... args) throws Exception {
        Flattener work = new Flattener();
        int argIndex = 0;
        String topLevelDirectory = args[argIndex++];
        String targetDirectory = args[argIndex++];
        work.start(topLevelDirectory,targetDirectory);
    }

    private void start(final String topLevelDirectory, final String targetDirectory) throws Exception {
        System.out.println(topLevelDirectory);
        File[] files = new File(topLevelDirectory).listFiles();
        showFiles(files,targetDirectory);
    }

    private void showFiles(File[] files,String targetDirectory) throws Exception {
        for (File file : files) {
            if (file.isDirectory()) {
                showFiles(file.listFiles(),targetDirectory);
            } else {
                if("config.xml".equalsIgnoreCase(file.getName())) {
                    System.out.println(file.getAbsolutePath());
                    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                    Document doc = dBuilder.parse(file);
                    doc.getDocumentElement().normalize();
                    System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
                }
            }
        }
    }
}