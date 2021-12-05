package sample.model;

import javafx.beans.property.SimpleFloatProperty;

public class JoystickHandler {

    SimpleFloatProperty attributes[];


    public JoystickHandler() {
        attributes = new SimpleFloatProperty[2];
        for (int i = 0; i < 2; i++) {
            attributes[i] = new SimpleFloatProperty();
        }
    }

    public SimpleFloatProperty getProperty (int index) {
        return this.attributes[index];
    }

    public float getValue(int index) {
        return this.attributes[index].get();
    }

    public void setValues(float rudder, float throttle) {
        this.attributes[0].set(rudder);
        this.attributes[1].set(throttle);
    }
}
