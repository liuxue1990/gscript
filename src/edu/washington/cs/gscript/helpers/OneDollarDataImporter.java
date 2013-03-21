package edu.washington.cs.gscript.helpers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.SAXParserFactory;

import edu.washington.cs.gscript.models.Gesture;
import edu.washington.cs.gscript.models.Project;
import edu.washington.cs.gscript.models.XYT;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class OneDollarDataImporter {

    private static class OneDollarXMLHandler extends DefaultHandler {

        private ArrayList<XYT> points = new ArrayList<XYT>();

        @Override
        public void startElement(String uri, String lName, String qName, Attributes attributes)
                throws SAXException {

            if (qName.equalsIgnoreCase("Point")) {

                double x = Double.parseDouble(attributes.getValue("X"));
                double y = Double.parseDouble(attributes.getValue("Y"));
                long t = Long.parseLong(attributes.getValue("T"));

                points.add(XYT.xyt(x, y, t));
            }
        }
    }

    public static Project importDiretory(String dirName) {
        Project project = new Project();

        try {
            File dir = new File(dirName);
            for (String fileName : dir.list()) {

                if (!fileName.endsWith(".xml")) {
                    continue;
                }

                String name = fileName.substring(0, fileName.length() - 6);

                OneDollarXMLHandler handler = new OneDollarXMLHandler();
                SAXParserFactory.newInstance().newSAXParser().parse(
                        new File(dir.getPath() + File.separator + fileName), handler);

                if (!handler.points.isEmpty()) {
                    project.addCategoryIfNotExist(name);
                    project.addSample(
                            project.getCategory(project.findCategoryIndexByName(name)),
                            new Gesture(handler.points));
                }
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }

        return project;
    }

    public static Project importTemplate(String fileName) {
        Project project = new Project();

        try {
            BufferedReader in = new BufferedReader(new FileReader(fileName));

            while (true) {
                String line = in.readLine();

                if (line == null || line.trim().isEmpty()) {
                    break;
                }

                String name = line.trim();

                ArrayList<XYT> points = new ArrayList<XYT>();

                String[] values = in.readLine().split(",");
                for (int i = 0; i < values.length; i += 2) {
                    points.add(XYT.xyt(Double.parseDouble(values[i]), Double.parseDouble(values[i+1]), -1));
                }

                project.addCategoryIfNotExist(name);
                project.addSample(project.getCategory(project.findCategoryIndexByName(name)), new Gesture(points));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return project;
    }
}
