package edu.washington.cs.gscript.helpers;

import edu.washington.cs.gscript.models.Part;

import java.util.ArrayList;

public class Parser {
    public static ArrayList<Part> parseScript(String scriptText) {
        ArrayList<Part> parts = new ArrayList<Part>();

        for (String line : scriptText.split("\n")) {
            if (line.trim().isEmpty()) {
                continue;
            }

            line = line.trim();

            Part part;
            if (line.endsWith("*")) {
                part = new Part(line.substring(0, line.length() - 1), true);
            } else {
                part = new Part(line, false);

                if (line.contains(" ")) {
                    return null;
                }
            }

            parts.add(part);
        }

        return parts;
    }

    public static ArrayList<Part> parseScript(String scriptText, String defaultName) {
        ArrayList<Part> parts = parseScript(scriptText);

        if (parts == null || parts.size() == 0) {
            parts.add(new Part(defaultName, false));
        }

        return parts;
    }

}
