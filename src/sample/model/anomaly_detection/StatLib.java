package sample.model.anomaly_detection;


import sample.model.anomaly_detection.Line;
import sample.model.anomaly_detection.Point;

public class StatLib {

	// simple average
	public static float avg(float[] x){
		float avg=0;
		for(int i=0; i<x.length; i++){
			avg+=x[i];
		}
		return (avg/x.length);
	}

	// returns the variance of X and Y
	public static float var(float[] x){
		float variance=0, count=0, countP= 0;
		float length=x.length;
		for(int i=0; i<x.length; i++){
			count+=x[i];
			countP+= (x[i]*x[i]);
		}
		count = count /length;
		countP = countP/length;
		count = count * count;
		variance = countP - count;
		return variance;
	}

	// returns the covariance of X and Y
	public static float cov(float[] x, float[] y){
		float avgX = avg(x);
		float avgY = avg(y);
		float sum=0;
		for(int i=0, j=0 ; i< x.length && j <y.length; i++, j++){
			sum+=(x[i]-avgX) * (y[j]-avgY);
		}
		return (sum/x.length);
	}


	// returns the Pearson correlation coefficient of X and Y
	public static float pearson(float[] x, float[] y){
		float Cov = cov(x,y);
		float varX = (float) Math.sqrt((double)(var(x)));
		float varY = (float) Math.sqrt((double)(var(y)));

		return (Cov / (varX*varY));
	}

	// performs a linear regression and returns the line equation
	public static Line linear_reg(Point[] points){
		float alpha;
		float [] X = new float[points.length];
		float [] Y = new float[points.length];
		for (int i=0; i< points.length; i++){
			X[i] = points[i].x;
			Y[i] = points[i].y;
		}
		alpha = cov(X,Y) / var(X);
		float beta = avg(Y)- (alpha * avg(X));
		Line L = new Line(alpha,beta);
		return L;
	}

	// returns the deviation between point p and the line equation of the points
	public static float dev(Point p,Point[] points){
		Line L = linear_reg(points);
		float dev = dev(p,L);
		return dev;
	}

	// returns the deviation between point p and the line
	public static float dev(Point p,Line l){
		float Dev = Math.abs(p.y-(l.a)*(p.x)-l.b);
		return Dev;
	}
	
}
