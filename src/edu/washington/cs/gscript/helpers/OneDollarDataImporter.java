package edu.washington.cs.gscript.helpers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.SAXParserFactory;

import edu.washington.cs.gscript.models.Category;
import edu.washington.cs.gscript.models.Gesture;
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

    public static ArrayList<Category> importDiretory(String dirName) {
        ArrayList<Category> categories = new ArrayList<Category>();

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
                    Category category = null;

                    for (Category c : categories) {
                        if (name.equals(c.getNameProperty().getValue())) {
                            category = c;
                            break;
                        }
                    }

                    if (category == null) {
                        category = new Category(name);
                        categories.add(category);
                    }

                    category.addSample(new Gesture(handler.points.toArray(new XYT[handler.points.size()])));
                }
            }

            return categories;

        } catch (Throwable e) {
            e.printStackTrace();
        }

        return categories;
    }

    public static ArrayList<Category> importTemplate(String fileName) {
        ArrayList<Category> categories = new ArrayList<Category>();

        try {
            BufferedReader in = new BufferedReader(new FileReader(fileName));

            while (true) {
                String line = in.readLine();

                if (line == null || line.trim().isEmpty()) {
                    break;
                }

                String name = line.trim();

                Category category = null;
                for (Category c : categories) {
                    if (name.equals(c.getNameProperty().getValue())) {
                        category = c;
                        break;
                    }
                }

                if (category == null) {
                    category = new Category(name);
                    categories.add(category);
                }

                ArrayList<XYT> points = new ArrayList<XYT>();

                String[] values = in.readLine().split(",");
                for (int i = 0; i < values.length; i += 2) {
                    points.add(XYT.xyt(Double.parseDouble(values[i]), Double.parseDouble(values[i+1]), -1));
                }

                category.addSample(new Gesture(
                        points.toArray(new XYT[points.size()])));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return categories;
    }
}
