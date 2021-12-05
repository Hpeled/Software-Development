package sample.model.anomaly_detection.essentials;

public class StatLib {

    public static float avg(float[] x){
        float sum = 0;
        for(int i = 0; i < x.length; sum += x[i], i++);
        return sum/x.length;
    }

    public static float var(float[] x){
        float av = avg(x);
        float sum = 0;
        for(int i = 0; i < x.length; i++){
            sum += x[i]*x[i];
        }
        return sum/x.length - av*av;
    }

    public static float cov(float[] x, float[] y){
        float sum = 0;
        for(int i = 0; i < x.length; i++){
            sum += x[i] * y[i];
        }
        sum /= x.length;

        return sum - avg(x)*avg(y);
    }

    public static float pearson(float[] x, float[] y) {
        return (float) (cov(x,y)/(Math.sqrt(var(x))*Math.sqrt(var(y))));
    }

    public static Line linear_reg(Point[] points){
        float x[] = new float[points.length];
        float y[] = new float[points.length];
        for(int i = 0; i < points.length; i++){
            x[i] = points[i].x;
            y[i] = points[i].y;
        }
        float a = cov(x,y) / var(x);
        float b = avg(y) - a*(avg(x));

        return new Line(a,b);
    }

    public static float dev(Point p,Point[] points){
        Line l = linear_reg(points);
        return dev(p,l);
    }

    public static float dev(Point p,Line l){
        return Math.abs(p.y-l.f(p.x));
    }

}