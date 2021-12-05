package sample.view_model;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;
import sample.model.JoystickHandler;
import sample.model.Model;
import sample.model.Panels;
import sample.model.Properties;
import sample.model.anomaly_detection.AnomalyReport;
import sample.model.anomaly_detection.CorrelatedFeatures;
import sample.view.View;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import java.util.List;
import sample.view_model.Essentials;


public class ViewModel implements Observer {
    Model model;
    File timeSeries;
    Properties flight_values;
    Panels panels;
    JoystickHandler joystick;
    List <AnomalyReport> reports;
    View view;
    String drawFeature, correlationFeature;
    int isCorrelated;
    int CSVs;
    String algo;

    SimpleBooleanProperty isStopped;

    HashMap<String, Integer> featureMap;
    HashMap<String, String> correlationMap;
    //public final Runnable play, pause, stop;

    public ViewModel() {
        this.model = new Model();
        this.model.addObserver(this);
        this.flight_values = new Properties();
        this.panels = new Panels();
        this.joystick = new JoystickHandler();
        this.isStopped = new SimpleBooleanProperty(true);
        this.featureMap = new HashMap<>();
        this.isCorrelated = 0;
        this.correlationFeature = "";
        this.CSVs = 0;
        this.reports = null;

        initMap();
    }

    public void initView(View view) {
        this.view = view;
    }

    public void initMap() {
        String[] features = {"aileron", "elevator", "rudder", "laps", "slats", "speedbrake", "throttle"	, "throttle", "engine-pump", "engine-pump",	"electric-pump", "electric-pump", "external-power",	"APU-generator", "latitude-deg", "longitude-deg", "altitude-ft", "roll-deg", "pitch-deg", "heading-deg", "side-slip-deg", "airspeed-kt", "glideslope",	"vertical-speed-fps", "airspeed-indicator_indicated-speed-kt", "altimeter_indicated-altitude-ft", "altimeter_pressure-alt-ft", "attitude-indicator_indicated-pitch-deg", "attitude-indicator_indicated-roll-deg", "attitude-indicator_internal-pitch-deg", "attitude-indicator_internal-roll-deg", "encoder_indicated-altitude-ft", "encoder_pressure-alt-ft", "gps_indicated-altitude-ft", "gps_indicated-ground-speed-kt", "gps_indicated-vertical-speed", "indicated-heading-deg", "magnetic-compass_indicated-heading-deg", "slip-skid-ball_indicated-slip-skid", "turn-indicator_indicated-turn-rate",	"vertical-speed-indicator_indicated-speed-fpm", "engine_rpm"};
        System.out.println(features.length);
        for (int i = 0; i < features.length; i++) {
            this.featureMap.put(features[i], i);
             System.out.println(features[i] + ", " + i);
        }
    }

    public SimpleBooleanProperty getSimpleBooleanProperty() {
        return isStopped;
    }

    public SimpleFloatProperty getSimpleFloatProperty(int index) {
        return flight_values.getValue(index);
    }

    public boolean passCSV(File selectedFile) {
        if (!selectedFile.getName().substring(selectedFile.getName().lastIndexOf(".") + 1).equals("csv")) {
            return false;
        } else {
            if (CSVs == 0) {
                this.model.setCSV(selectedFile);
                this.correlationMap = model.getCorrelatedFeatures();
                CSVs++;
                return false;
            }
            this.model.setCSV(selectedFile);
            this.view.setProgressBarLimit(Essentials.getTotalLines(selectedFile));
            this.algo = view.getCurrentAlgorithm();
            this.reports = model.getReports();
            return true;
        }
    }

    public boolean Connect() {
        if(checkFlightGearProcess()) {
            return this.model.Connect();
        } else {
            return false;
        }
    }

    public boolean checkFlightGearProcess() {
        String line;
        String pidInfo ="";
        Process p = null;

        try {
            p = Runtime.getRuntime().exec(System.getenv("windir") +"\\system32\\"+"tasklist.exe");
            BufferedReader input =  new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = input.readLine()) != null) {
                pidInfo+=line;
            }
            input.close();

            if(pidInfo.contains("fgfs")) {
                System.out.println("Found FlightGear process.");
                return true;
            } else {
                System.out.println("Couldn't find FlightGear process");
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void stop() {
        this.model.stop();
        this.isStopped.set(true);
    }

    public Properties getProperties() {
        return this.flight_values;
    }

    public void setProperties(Properties properties) {
        this.flight_values = properties;
    }

    public void setSleepTime(int SleepTime) {
        this.model.setSleepTime(SleepTime);
    }

    public void setDrawFeature(String feature) {
        this.drawFeature = feature;
    }
    public void setCorrelationFeature (String feature) {this.correlationFeature = feature;}

    @Override
    public void update(Observable o, Object arg) {
        if(arg.getClass() == flight_values.getClass()) {
            this.setProperties((Properties) arg);
            boolean isCorrelated = false;
            if (drawFeature != null)
                this.view.paint(String.valueOf(model.getTimeStep()), flight_values.getValue(featureMap.get(drawFeature)).get());

            setCorrelationFeature(correlationMap.get(drawFeature));
            if (correlationFeature != null) {
                this.view.paintCor(String.valueOf(model.getTimeStep()), flight_values.getValue(featureMap.get(correlationFeature)).get());
            }
            this.view.progress();
        } else if (arg.getClass() == panels.getClass()) {
            this.panels = (Panels) arg;
            this.view.updateClocks(this.panels);
        } else if (arg.getClass() == joystick.getClass()){
            this.view.updateJoystick(this.joystick);
        } else {
            this.view.paintAnomaly(reports.get(model.getTimeStep()).description, model.getTimeStep());
        }
    }

    public boolean isCorrelated(String feature) {
        return this.correlationMap.containsKey(feature);
    }


    public void play() {
        System.out.println(correlationMap.get(drawFeature));
        if (this.isStopped.getValue()) {
            this.isStopped.set(false);
            this.model.play();
        }
        System.out.println("test");
    }
}
