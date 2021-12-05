package sample.view;

import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.GaugeBuilder;
import eu.hansolo.medusa.Section;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.chart.BubbleChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import sample.model.JoystickHandler;
import sample.model.Panels;
import sample.model.Properties;
import sample.model.anomaly_detection.AnomalyReport;
import sample.view_model.Essentials;
import sample.view_model.ViewModel;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class View implements Initializable {

    @FXML
    Label fileLabel, connectionLabel, currentFeatureLabel, errorLabel, corLabel;
    @FXML
    Button fileButton, connectButton, play, stop, backwards, forward, exportButton;
    @FXML
    Slider rudder, throttle, progressBar;
    @FXML
    LineChart featureLineChart, correlatedLineChart, anomalyChart;
    @FXML
    Canvas featureCanvas;
    @FXML
    ComboBox<String> comboBox;
    @FXML
    ListView<String> featureList;
    @FXML
    ListView<String> algorithmList;
    @FXML
    TextField speedText, altitude, rpm, roll, pitch;
    @FXML
    Gauge altimeterClock, airspeedClock, rollClock, pitchClock;
    @FXML
    Text anomalyText;

    Thread painter;
    int sleepTime;

    float rudderVal, throttleVal;

    String[] features = {"aileron", "elevator", "rudder", "laps", "slats", "speedbrake", "throttle"	, "throttle", "engine-pump", "engine-pump",	"electric-pump", "electric-pump", "external-power",	"APU-generator", "latitude-deg", "longitude-deg", "altitude-ft", "roll-deg", "pitch-deg", "heading-deg", "side-slip-deg", "airspeed-kt", "glideslope",	"vertical-speed-fps", "airspeed-indicator_indicated-speed-kt", "altimeter_indicated-altitude-ft", "altimeter_pressure-alt-ft", "attitude-indicator_indicated-pitch-deg", "attitude-indicator_indicated-roll-deg", "attitude-indicator_internal-pitch-deg", "attitude-indicator_internal-roll-deg", "encoder_indicated-altitude-ft", "encoder_pressure-alt-ft", "gps_indicated-altitude-ft", "gps_indicated-ground-speed-kt", "gps_indicated-vertical-speed", "indicated-heading-deg", "magnetic-compass_indicated-heading-deg", "slip-skid-ball_indicated-slip-skid", "turn-indicator_indicated-turn-rate",	"vertical-speed-indicator_indicated-speed-fpm", "engine_rpm"};
    String[] algorithms = {"Simple", "Hybrid", "ZScore"};
    String currentFeature;
    String currentAlgorithm;


    private String[] speeds = {"0.25", "0.5", "0.75", "1", "1.25", "1.5", "1.75", "2"};
    ViewModel viewModel;
    File CSV;
    XYChart.Series<String, Number> series;
    XYChart.Series<String, Number> seriesCor;
    XYChart.Series<String, Number> seriesAno;

    SimpleFloatProperty latitude_deg;
    SimpleFloatProperty[] values;
    SimpleBooleanProperty isStopped;

    Properties flight_values;

    public View() {
        this.viewModel = new ViewModel();
        this.isStopped = new SimpleBooleanProperty(true);
        this.isStopped.bind(viewModel.getSimpleBooleanProperty());
        this.sleepTime = 100;
        this.viewModel.setSleepTime(sleepTime);
        this.flight_values = new Properties();
        this.viewModel.initView(this);
        this.series = new XYChart.Series<String, Number>();
        this.seriesCor = new XYChart.Series<String, Number>();
        this.seriesAno = new XYChart.Series<String, Number>();
    }

    public void speedUp(ActionEvent actionEvent) {
        System.out.println(sleepTime);
        if (sleepTime > 50 && !isStopped.getValue()) {
            speedText.setText(String.valueOf(Float.parseFloat(speedText.getText()) + 0.25));
            sleepTime -= 25;
            this.viewModel.setSleepTime(sleepTime);
        }
    }

    public void speedDown(ActionEvent actionEvent) {
        System.out.println(sleepTime);
        if (sleepTime < 150 && !isStopped.getValue()) {
            speedText.setText(String.valueOf(Float.parseFloat(speedText.getText()) - 0.25));
            sleepTime += 25;

            this.viewModel.setSleepTime(sleepTime);
        }
    }
    public void chooseFile(ActionEvent actionEvent) {
        FileChooser fc = new FileChooser();
        CSV = fc.showOpenDialog(null);
        if (CSV != null) {
            if (viewModel.passCSV(CSV)) {
                fileLabel.setText("Opened file " + CSV.getName());
                play.setDisable(false);
                stop.setDisable(false);
                backwards.setDisable(false);
                forward.setDisable(false);
                exportButton.setDisable(false);
            } else {
                fileLabel.setText("Enter anomaly file.");
                try {
                    Thread.sleep(3);
                    fileLabel.setText("");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {
            fileLabel.setText("Error, null file.");
            try {
                Thread.sleep(3);
                fileLabel.setText("");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void bindValues() {
        for (int i = 0; i < 41; i++) {
            this.flight_values.getValue(i).bind(viewModel.getSimpleFloatProperty(i));
        }
    }

    public void stop() {
        this.viewModel.stop();
        System.out.println(this.isStopped.get());
    }

    public void play() {
        if (currentAlgorithm == null)
            printError("<- Choose algorithm");
        else {
            if (this.viewModel.checkFlightGearProcess()) {
                fileLabel.setText("");
                this.viewModel.play();
                this.bindValues();
            } else {
                fileLabel.setText("FlightGear isn't running.");
            }
        }
    }

    public void paint(String x, float y) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                series.getData().add(new XYChart.Data<>(x, y));
//                XYChart.Series<String, Number> series = new XYChart.Series<String, Number>();
//                series.getData().add(new XYChart.Data(x, y));
//                featureLineChart.getData().add(series);
            }
        });
    }

    public void setLabel(String text) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                corLabel.setText(text);
            }
        });
    }

    public void paintCor(String x, float y) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                //XYChart.Series<String, Number> series = new XYChart.Series<String, Number>();
                seriesCor.getData().add(new XYChart.Data(x, y));
                //correlatedLineChart.getData().add(series);
            }
        });
    }

    public void paintAnomaly(String x, float y) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                //XYChart.Series<String, Number> series = new XYChart.Series<String, Number>();
                seriesAno.getData().add(new XYChart.Data(x, y));
                //anomalyText.setText("");
                //anomalyChart.getData().add(series);
            }
        });
    }

    public void updateClocks(Panels panel) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                altimeterClock.setValue(panel.getValue(0));
                airspeedClock.setValue(panel.getValue(1));
                rollClock.setValue(panel.getValue(2));
                pitchClock.setValue(panel.getValue(3));
//                altitude.setText(String.valueOf(panel.getValue(0)));
//                rpm.setText(String.valueOf(panel.getValue(1)));
//                pitch.setText(String.valueOf(panel.getValue(2)));
//                roll.setText(String.valueOf(panel.getValue(3)));
            }
        });
    }

    public void progress() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                progressBar.adjustValue(progressBar.getValue() + 1);
            }
        });
    }

    public void updateJoystick(JoystickHandler joystick) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                throttle.adjustValue(joystick.getValue(0));
                rudder.adjustValue(joystick.getValue(1));
            }
        });
    }

    public void createReport(ActionEvent actionEvent) {
        this.viewModel.createReport();
    }

    public void setProgressBarLimit(int limit) {
        progressBar.setMax(limit);
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        progressBar.adjustValue(0);
        featureList.getItems().addAll(features);
        algorithmList.getItems().addAll(algorithms);

        this.featureLineChart.getData().add(series);
        this.correlatedLineChart.getData().add(seriesCor);
        this.anomalyChart.getData().add(seriesAno);

        altimeterClock.setTitle("Altimeter");


        speedText.setText("1");
        featureList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                if (featureList.getSelectionModel().getSelectedItem() != null) {
                    currentFeature = featureList.getSelectionModel().getSelectedItem();
                    viewModel.setDrawFeature(currentFeature);
                    currentFeatureLabel.setText(currentFeature);
                }
            }
        });
        algorithmList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                if (algorithmList.getSelectionModel().getSelectedItem() != null) {
                    currentAlgorithm = algorithmList.getSelectionModel().getSelectedItem();
                    errorLabel.setText("");
                }
            }
        });

    }

    public String getCurrentAlgorithm() {
        return currentAlgorithm;
    }

    public void printError(String error) {
        errorLabel.setText(error);
    }



}

