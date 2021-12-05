package sample.model.anomaly_detection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ZScoreAlgo implements TimeSeriesAnomalyDetector {

    HashMap<String, Float> colThresholdMap = new HashMap<>();

    public void learnNormal(TimeSeries ts){
        TimeSeries.col[] learnFeatures = ts.getFeatures();
        float maxZScore, currentZScore;
        for(int i = 0; i < learnFeatures.length; i++){
            maxZScore = Float.MIN_VALUE;
            for(int j = 0; i < learnFeatures[i].parameters.size(); j++){
                currentZScore = calculateZScore(learnFeatures[i].getParameters(), j);
                if(currentZScore > maxZScore){
                    maxZScore = currentZScore;
                }
            }
            colThresholdMap.put(learnFeatures[i].getName(), maxZScore);
        }
    }
    public List<AnomalyReport> detect(TimeSeries ts){
        TimeSeries.col[] detectFeatures = ts.getFeatures();
        List<AnomalyReport> liveReports = new ArrayList<AnomalyReport>();
        float maxZScore, currentZScore;
        for(int i = 0; i < detectFeatures.length; i++){
            maxZScore = Float.MIN_VALUE;
            for(int j = 0; i < detectFeatures[i].parameters.size(); j++){
                currentZScore = calculateZScore(detectFeatures[i].getParameters(), j);
                if(currentZScore > maxZScore){
                    maxZScore = currentZScore;
                }
            }
            if(colThresholdMap.containsKey(detectFeatures[i].getName())){
                if(colThresholdMap.get(detectFeatures[i].getName()) > maxZScore){
                    AnomalyReport report= new AnomalyReport("Anomaly at: " + detectFeatures[i].getName(),
                            i+1); //add a new detected object
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

    public float calculateZScore(ArrayList<Float> parameters, int index){
        float mean  = calculateMean(parameters, index);
        float sd = calculateSD(parameters,mean,index);
        float ZScore = Math.abs(parameters.get(index) - mean) / sd;
        return ZScore;
    }

    public float calculateSD(ArrayList<Float> parameters, float mean, int index ) {
        float sum = 0, standardDeviation = 0;

        for (int i = 0; i < index; i++) {
            standardDeviation += Math.pow(parameters.get(i) - mean, 2);
        }
        return standardDeviation;
    }

    public float calculateMean(ArrayList<Float> parameters, int index){
        float sum = 0, mean;
        for(int i = 0; i < index; i++){
            sum += parameters.get(i);
        }
        mean = sum / index+1;
        return mean;
    }
}

