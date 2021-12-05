package sample.model;

import javafx.beans.property.SimpleFloatProperty;

public class Panels {
    private SimpleFloatProperty[] values;

    public Panels() {
        values = new SimpleFloatProperty[5];
        for (int i = 0; i < 5; i++) {
            values[i] = new SimpleFloatProperty();
        }
    }

    public SimpleFloatProperty[] getValues() {
        return values;
    }

    public SimpleFloatProperty getProperty(int index) {
        return values[index];
    }

    public void setValues(float altitude, float speed, float pitch, float roll) {
        this.values[0].set(altitude);
        this.values[1].set(speed);
        this.values[2].set(pitch);
        this.values[3].set(roll);
    }
    public float getValue(int index) {
        return this.values[index].get();
    }
}
