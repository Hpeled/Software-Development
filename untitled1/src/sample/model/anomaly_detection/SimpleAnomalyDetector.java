package sample.model.anomaly_detection;

import sample.model.anomaly_detection.essentials.Line;
import sample.model.anomaly_detection.essentials.Point;
import sample.model.anomaly_detection.essentials.StatLib;

import java.util.ArrayList;
import java.util.List;

public class SimpleAnomalyDetector implements TimeSeriesAnomalyDetector {

    float threshold=(float)0.9;


    public void setThreshold(float threshold) {
        this.threshold = threshold;
    }

    ArrayList<CorrelatedFeatures> correlates = new ArrayList<CorrelatedFeatures>(); // correlates of the table
    @Override
    public void learnNormal(TimeSeries ts) {

        float [] arr;
        float pear, maxPear=-1;
        int index=-1;
        for (int i=0;i<ts.getFeatures().length;i++) {
            arr= ts.ArrFloats(ts.getFeatures()[i].getParameters());
            for (int j=i+1;j<ts.getFeatures().length;j++) {
                pear = Math.abs(StatLib.pearson(arr, ts.ArrFloats(ts.getFeatures()[j].getParameters())));
                if (pear>maxPear && pear>threshold)
                {
                    maxPear=pear;
                    index=j;
                }
            }
            if (maxPear > 0) {
                Point[] points = ts.arrPoints(arr, ts.ArrFloats(ts.getFeatures()[index].getParameters()));
                Line l = StatLib.linear_reg(points);

                float maxThres = 0;

                for (int j = 0; j < points.length; j++) {
                    if (maxThres< StatLib.dev(points[j], l))
                        maxThres= StatLib.dev(points[j], l);
                }
                correlates.add(new CorrelatedFeatures(ts.getFeatures()[i].getName(), ts.getFeatures()[index].getName(), maxPear, l,maxThres+(float)0.0389));
            }
            index=0;
            maxPear=-1;
        }
    }


    @Override
    public List<AnomalyReport> detect(TimeSeries ts)
    {

        List<AnomalyReport> liveReports = new ArrayList<AnomalyReport>();

        for (int i = 0; i < correlates.size(); i++)
        {
            String fe1 = correlates.get(i).feature1;
            String fe2 = correlates.get(i).feature2;
            ArrayList <Float> float1 = new ArrayList<Float>();
            ArrayList <Float> float2 = new ArrayList<Float>();

            for(int j = 0;j < ts.getFeatures().length; j++)
            {
                if (ts.getFeatures()[j].getName().equals(fe1)) {
                    float1 = ts.getFeatures()[j].getParameters();
                }
                if (ts.getFeatures()[j].getName().equals(fe2)) {
                    float2 = ts.getFeatures()[j].getParameters();
                }
            }

            float [] f1 = ts.ArrFloats(float1);
            float [] f2 = ts.ArrFloats(float2);
            Point[] arrPoints = ts.arrPoints(f1, f2);

            for (int j = 0; j < arrPoints.length; j++)
            {
                if (StatLib.dev(arrPoints[j],correlates.get(i).lin_reg) > correlates.get(i).threshold) // checks if there is a detection
                {
                    AnomalyReport report= new AnomalyReport(fe1+"-"+fe2,j+1); //add a new detected object
                    liveReports.add(report);
                }
            }

        }
        return liveReports;
    }

    @Override
    public Runnable paint() {
        return null;
    }

    public List<CorrelatedFeatures> getNormalModel(){
        return correlates;
    }
}
