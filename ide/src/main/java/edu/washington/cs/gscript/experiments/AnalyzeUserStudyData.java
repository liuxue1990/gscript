package edu.washington.cs.gscript.experiments;

import edu.washington.cs.gscript.models.Project;
import edu.washington.cs.gscript.models.ProjectAnalyzer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class AnalyzeUserStudyData {
    public static void main(String[] args) {

        try {
            for (int i = 1; i <= 4; ++i) {
                String fileName = String.format("data/user_study/study%d.gscript", i);
                analyze(fileName);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    public static void analyze(String fileName) throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName));
        Project project = ((Project)in.readObject());
        in.close();

        ProjectAnalyzer.analyze(project);
    }
}
