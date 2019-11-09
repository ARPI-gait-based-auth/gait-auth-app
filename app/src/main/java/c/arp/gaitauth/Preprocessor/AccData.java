package c.arp.gaitauth.Preprocessor;

import com.anychart.chart.common.dataentry.DataEntry;

import java.util.ArrayList;
import java.util.List;

public class AccData {
    List<Long> time = new ArrayList<Long>();
    List<Double> x = new ArrayList<Double>();
    List<Double> y = new ArrayList<Double>();
    List<Double> z = new ArrayList<Double>();

    public void setToSeriesData(List<DataEntry> seriesData) {
        for (int i = 0; i < time.size(); i++) {
            seriesData.add(new CustomDataEntry(i + "", x.get(i), y.get(i), z.get(i)));
        }
    }

    public void resample() {
        double[] fixedTime = new double[time.size()];
        for (int i = 0; i < time.size(); i++) {
            fixedTime[i] = time.get(i);
        }
        fixedTime = Interpolation.prepareForInterpolation(fixedTime, 120);

        double[] xx = new double[time.size()];
        double[] yy = new double[time.size()];
        for (int i = 0; i < time.size(); i++) {
            xx[i] = time.get(i);
            yy[i] = y.get(i);
        }
        double[] xxx = Interpolation.interpLinear(xx, yy, fixedTime);

        for (int i = 0; i < time.size(); i++) {
            z.set(i, xxx[i]);
        }
    }
}
