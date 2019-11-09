package c.arp.gaitauth.Preprocessor;

import com.anychart.chart.common.dataentry.DataEntry;

import java.util.ArrayList;
import java.util.List;

public class AccData {
    List<Long> time = new ArrayList<Long>();
    List<Float> x = new ArrayList<Float>();
    List<Float> y = new ArrayList<Float>();
    List<Float> z = new ArrayList<Float>();

    public void setToSeriesData(List<DataEntry> seriesData) {
        for (int i = 0; i < x.size(); i++) {
            seriesData.add(new CustomDataEntry(i + "", x.get(i), y.get(i), z.get(i)));
        }
    }

    public void resample() {

    }
}
