package sample.model.anomaly_detection;

import sample.model.anomaly_detection.essentials.Circle;
import sample.model.anomaly_detection.essentials.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class HybridAlgo implements TimeSeriesAnomalyDetector{

    public ArrayList<Circle> circles = new ArrayList<>();
    public ArrayList<AnomalyReport> detections = new ArrayList<>();

    public List<AnomalyReport> HybridAlgorithm(TimeSeries timeSeries1, TimeSeries timeSeries2) {

        //linearReg
        int flag;
        SimpleAnomalyDetector linearReg = new SimpleAnomalyDetector();
        linearReg.setThreshold(0.95f);
        linearReg.learnNormal(timeSeries1);
        List <AnomalyReport> linearReg_detect = linearReg.detect(timeSeries2);

        for (AnomalyReport report :linearReg_detect) {
            detections.add(report);
        }

        //Hybrid
        SimpleAnomalyDetector linearReg_hybrid = new SimpleAnomalyDetector();
        linearReg_hybrid.setThreshold(0);
        linearReg_hybrid.learnNormal(timeSeries1);
        List <CorrelatedFeatures> f = linearReg_hybrid.getNormalModel();
        List <CorrelatedFeatures> temp = new ArrayList<CorrelatedFeatures>();

        for (CorrelatedFeatures feature : f) {
            temp.add(feature);
        }

        f.clear();
        for (CorrelatedFeatures feature : temp) {
            if (Math.abs(feature.correlation) >= 0.5)
                f.add(feature);
        }

        this.learnNormal(timeSeries1);
        List <AnomalyReport> Hybrid_detect = this.detect(timeSeries2);

        for (AnomalyReport report :Hybrid_detect) {
            flag = 0;
            for (AnomalyReport detection: detections) {
                String[] arr = detection.description.split("-");
                if (report.description.contains(arr[0]) || report.description.contains(arr[1]))
                    flag = 1;
            }
            if (flag == 0)
                detections.add(report);
        }

        ZScoreAlgo zScore = new ZScoreAlgo();
        zScore.learnNormal(timeSeries1);
        for (AnomalyReport report :zScore.detect(timeSeries2)) {
            flag = 0;
            for (AnomalyReport detection: detections) {
                if (detection.description.contains(report.description))
                    flag = 1;
            }
            if (flag == 0)
                detections.add(report);
        }
        return detections;
    }

    @Override
    public void learnNormal(TimeSeries timeSeries) {
        String[] str = new String[timeSeries.getFeatures().length];

        for (int i = 0; i < timeSeries.getFeatures().length; i++)
            str[i] = timeSeries.getFeatures()[i].getName();

        for (int i = 0; i < str.length; i++) {
            ArrayList<Float> column_i = timeSeries.getFeatures()[i].getParameters();

            for (int j = i + 1; j < str.length; j++) {

                if (str[i] != str[j]) {
                    ArrayList<Float> column_j = timeSeries.getFeatures()[j].getParameters();
                    Vector<Point> pointsVector = new Vector<Point>();
                    for (int t = 0; t < timeSeries.getFeatures()[j].getParameters().size(); t++)
                    {
                        pointsVector.add(new Point(column_i.get(t), column_j.get(t)));
                    }
                    Circle circle = Circle.FindMinCircle(pointsVector);
                    circles.add(circle);
                }
            }
        }
    }

    @Override
    public List<AnomalyReport> detect(TimeSeries timeSeries) {
        List<AnomalyReport> anomalyReports = new ArrayList<AnomalyReport>();
        String[] str = new String[timeSeries.getFeatures().length];

        for (int i = 0; i < timeSeries.getFeatures().length; i++)
            str[i] = timeSeries.getFeatures()[i].getName();

        int index = 0;
        int k = 0;
        for (int i = 0; i < str.length; i++) {
            ArrayList<Float> column_i = timeSeries.getFeatures()[i].getParameters();

            for (int j = i + 1; j < str.length; j++) {

                if (str[i] != str[j]) {
                    ArrayList<Float> column_j = timeSeries.getFeatures()[j].getParameters();
                    Vector<Point> pointsVector = new Vector<Point>();
                    for (int t = 0; t < timeSeries.getFeatures()[j].getParameters().size(); t++)
                    {
                        pointsVector.add(new Point(column_i.get(t), column_j.get(t)));
                    }

                    for (int h = 0; h < pointsVector.size(); h++)
                    {

                        if (!circles.get(k).containsPoint(pointsVector.get(h)))
                        {
                            anomalyReports.add(new AnomalyReport(str[i] + "-" + str[j], h + 1));
                        }
                    }

                }
                k++;
            }

        }

        return anomalyReports;
    }

    @Override
    public Runnable paint() {
        return null;
    }
}
