package sample.model;

import sample.model.anomaly_detection.*;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;

public class Model extends Observable {
    public Stream stream;
    public File CSVreg, CSVanomaly;
    int timeStep, sleepTime;
    boolean stopPlayer;
    TimeSeries ts;
    Thread player;
    Properties properties;
    TimeSeriesAnomalyDetector algo;
    List<AnomalyReport> reports;
    List<CorrelatedFeatures> correlatedFeatures;
    HashMap<String, String> correlationMap;
    Panels panels;
    JoystickHandler joystick;

    SimpleAnomalyDetector test;



    public void setSleepTime(int sleepTime) {
        this.sleepTime = sleepTime;
    }

    public int getSleepTime() {
        return this.sleepTime;
    }

    public Model() {
        this.stream = new Stream();
        this.CSVreg = null;
        this.CSVanomaly = null;

        this.timeStep = 0;
        this.stopPlayer = false;
        this.properties = new Properties();
        this.panels = new Panels();
        this.joystick = new JoystickHandler();
        this.algo = new SimpleAnomalyDetector();
        this.correlationMap = new HashMap<>();
        this.correlatedFeatures = new ArrayList<>();
        this.test = new SimpleAnomalyDetector();

        Runnable play = () -> {
            //System.out.println("test");
            //this.stream.playStream();
            Socket fg = null;
            try {
                fg = new Socket("localhost", 5400);
                BufferedReader in = new BufferedReader(new FileReader(this.CSVanomaly.getAbsolutePath()));
                PrintWriter out = new PrintWriter(fg.getOutputStream());

                String line;
                line = in.readLine();

                while (((line = in.readLine()) != null)) {
                    if (getStopPlayer()) {
                        freeze();
                    }

                    updateProperties(line);
                    out.println(line);
                    out.flush();
                    if (checkTimeStep(timeStep) != null) {
                        System.out.println("Report detected: " + checkTimeStep(timeStep));
                        setChanged();
                        notifyObservers(checkTimeStep(timeStep));
                    }

                    timeStep++;

                   // System.out.println("Step: " + step);

                    try {
                        Thread.sleep(getSleepTime());
                        //System.out.println("Sleep time: " + getSleepTime());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
                out.close();
                in.close();
                fg.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        this.player = new Thread(play);
    }


    public void setStopPlayer(boolean stopPlayer) {
        this.stopPlayer = stopPlayer;
    }

    public boolean getStopPlayer() {
        return stopPlayer;
    }
    public int getTimeStep() {return this.timeStep;}
    public void updateProperties(String line) {
        this.properties.setValues(line);
        this.panels.setValues(properties.getValue(25).getValue(), properties.getValue(21).getValue(), properties.getValue(18).getValue(), properties.getValue(17).getValue());
        this.joystick.setValues(properties.getValue(2).getValue(), properties.getValue(6).getValue());
        setChanged();
        notifyObservers(properties);
        setChanged();
        notifyObservers(panels);
        setChanged();
        notifyObservers(joystick);

    }

    public AnomalyReport checkTimeStep(int timeStep) {
        for (int i = 0; i < reports.size(); i++) {
            if (reports.get(i).timeStep == timeStep)
                return reports.get(i);
        }
        return null;
    }

    public void createReport() {
        Report report = new Report(reports);
    }

    public HashMap<String, String> getCorrelatedFeatures() {
        for (int i = 0; i < correlatedFeatures.size(); i++) {
            correlationMap.put(correlatedFeatures.get(i).feature1, correlatedFeatures.get(i).feature2);
            correlationMap.put(correlatedFeatures.get(i).feature2, correlatedFeatures.get(i).feature1);
        }

        return this.correlationMap;
    }

    public void freeze() {
        System.out.println("Thread stopped");
        while (getStopPlayer()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void setCSV(File CSV) {

        if (this.CSVreg == null) {
            this.CSVreg = CSV;
            this.ts = new TimeSeries(CSVreg.getAbsolutePath());
            test.learnNormal(ts);
            this.correlatedFeatures = test.getNormalModel();
            System.out.println(correlatedFeatures);

        } else {
            this.CSVanomaly = CSV;
            this.ts = new TimeSeries(CSVanomaly.getAbsolutePath());
            this.reports = test.detect(ts);
            System.out.println("Total correlations: " + test.getNormalModel().size());
            System.out.println("Total reports: " + reports.size());
        }
    }

    public List<AnomalyReport> getReports() { return this.reports;}
    public boolean Connect() {
        return this.stream.Connect();
    }

    public void play() {
        if(!getStopPlayer())
            this.player.start();
        else
            setStopPlayer(false);
    }

    public void stop() {
        setStopPlayer(true);
    }


}
