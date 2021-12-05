package sample.view_model;

import java.io.*;
import java.net.Socket;

public class Essentials {

    public static int getTotalLines(File csv) {
        int lines = 0;

        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(csv.getAbsolutePath()));
            String line;
            line = in.readLine();

            while (((line = in.readLine()) != null)) {
                lines++;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }
}
