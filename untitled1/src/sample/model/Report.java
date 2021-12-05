package sample.model;

import sample.model.anomaly_detection.AnomalyReport;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Report {
    File reportFile;

    public Report(List<AnomalyReport> list) {
        try {
            reportFile = new File("FlightReport.txt");
            if (reportFile.createNewFile()) {
                System.out.println("File created: " + reportFile.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        try {
            FileWriter myWriter = new FileWriter(this.reportFile.getAbsolutePath());
            myWriter.write("Total anomalies: " + list.size() + "\n\n");
            for (int i = 0; i < list.size(); i++) {
                myWriter.write("Report #" + i + "\n");
                myWriter.write("Feature: " + list.get(i).description + ", " + "TimeStep: " + list.get(i).timeStep + "\n\n");
            }
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        } try {
            if(!Desktop.isDesktopSupported())
            {
                System.out.println("not supported");
                return;
            }
            Desktop desktop = Desktop.getDesktop();
            if(this.reportFile.exists())
                desktop.open(this.reportFile);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}

