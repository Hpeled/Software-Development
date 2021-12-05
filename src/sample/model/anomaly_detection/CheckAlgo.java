package sample.model.anomaly_detection;

import java.util.List;

public class CheckAlgo {


    public static void main(String[] args) {
        TimeSeries ts1=new TimeSeries("anomalyTrain.csv");
        TimeSeries ts2=new TimeSeries("anomaly_flight.csv");
        TimeSeriesAnomalyDetector zScore = new ZScoreAlgo();
        zScore.learnNormal(ts1);


        List<AnomalyReport> reports = zScore.detect(ts2);

        System.out.println("number of reports:"+ reports.size());
        for(AnomalyReport ar: reports)
            System.out.println(ar);

        System.out.println("done!");

    }
}

