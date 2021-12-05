package sample.model;

import sample.model.anomaly_detection.TimeSeries;
import sample.model.anomaly_detection.TimeSeriesAnomalyDetector;

import java.util.Timer;

// Wraps the relevant connection and ts objects.

public class Stream {

    private Connection connection;
    private TimeSeries ts;
    private TimeSeriesAnomalyDetector algorithm;
    private int timeStep;
    Timer timer;

    public Stream() {
        this.ts = null;
        this.connection = new Connection();
        this.algorithm = null;
        this.timeStep = 0; // Holds ts' line number.
    }

    public boolean Connect() {
        return connection.Connect();
    }

    public void setConnection(Connection connection) { this.connection = connection; }

    public void setTs(TimeSeries ts) {
        this.ts = ts;
        if (this.algorithm != null){
            this.algorithm.learnNormal(this.ts);
            this.algorithm.detect(this.ts);
        }

        System.out.print("Set ts!");
    }

    public void setTimeStep(int value) {
        this.timeStep = value;
    }

    public int getTimeStep() {
        return this.timeStep;
    }

    public void setAlgo(TimeSeriesAnomalyDetector algorithm) {
        this.algorithm = algorithm;
        if (this.ts != null) {
            this.algorithm.learnNormal(this.ts);
            this.algorithm.detect(this.ts);
        }
    }

    public void playStream() {

    }
}
