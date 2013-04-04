package edu.washington.cs.gscript.models;

public class ProjectAnalyzer {
    public static void analyze(Project project) {

        int numOfCategories = project.getNumOfCategories();
        System.out.println("# of categories : " + project.getNumOfCategories());

        int totalNumOfSamples = 0;
        int totalNumOfSynthesis = 0;
        for (int categoryIndex = 0; categoryIndex < numOfCategories; ++categoryIndex) {
            Category category = project.getCategory(categoryIndex);
            int numOfSamples = category.getNumOfSamples();
            totalNumOfSamples += numOfSamples;
            for (int sampleIndex = 0; sampleIndex < numOfSamples; ++sampleIndex) {
                Gesture sample = category.getSample(sampleIndex);
                if (sample.isSynthesized()) {
                    ++totalNumOfSynthesis;
                }
            }
        }
        System.out.println("# of samples : " + totalNumOfSamples);
        System.out.println("# of synthesized samples : " + totalNumOfSynthesis);

        int numOfScripts = 0;
        int numOfLinesInScripts = 0;
        for (int categoryIndex = 0; categoryIndex < numOfCategories; ++categoryIndex) {
            Category category = project.getCategory(categoryIndex);
            String scriptText = category.getScriptTextProperty().getValue();
            String[] lines = scriptText.split("\n");
            int numOfLines = 0;
            for (String line : lines) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                ++numOfLines;
            }

            if (numOfLines > 0) {
                ++numOfScripts;
            }

            numOfLinesInScripts += numOfLines;
        }

        System.out.println("# of scripts : " + numOfScripts);
        System.out.println("# of lines in scripts : " + numOfLinesInScripts);

        System.out.println("# of parts : " + project.getParts().size());

        int numOfUserTemplates = 0;
        for (Part part : project.getParts()) {
            if (part.getUserTemplate() != null) {
                ++numOfUserTemplates;
            }
        }
        System.out.println("# of provided shapes : " + numOfUserTemplates);

        int numOfUserBreaks = 0;
        for (int categoryIndex = 0; categoryIndex < numOfCategories; ++categoryIndex) {
            Category category = project.getCategory(categoryIndex);
            int numOfSamples = category.getNumOfSamples();
            for (int sampleIndex = 0; sampleIndex < numOfSamples; ++sampleIndex) {
                Gesture sample = category.getSample(sampleIndex);
                numOfUserBreaks += sample.getNumOfUserLabeledBreaks();
            }
        }
        System.out.println("# of user labeled breaks : " + numOfUserTemplates);
    }
}
