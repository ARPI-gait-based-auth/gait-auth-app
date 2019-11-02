package c.arp.gaitauth.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import c.arp.gaitauth.R;
import c.arp.gaitauth.StaticStore;

import android.os.Bundle;
import com.opencsv.CSVReader;

import java.io.File;
import java.io.IOException;
import java.io.FileReader;
import android.widget.Toast;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Line;
import com.anychart.data.Mapping;
import com.anychart.data.Set;
import com.anychart.enums.Anchor;
import com.anychart.enums.MarkerType;
import com.anychart.enums.TooltipPositionMode;
import com.anychart.graphics.vector.Stroke;
import java.util.ArrayList;
import java.util.List;

public class IdentifyUserFragment extends Fragment {

    List<DataEntry> seriesData = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_identify_user, container, false);
        initData();
        initChart(root);
        return root;
    }

    void initData() {
        if (StaticStore.selectedFile == null) {
            return;
        }
        try {
            CSVReader reader = new CSVReader(new FileReader(StaticStore.selectedFile));
            String[] nextLine;
            int c = -1;
            while ((nextLine = reader.readNext()) != null) {
                if (c == -1) { c++; continue; }
                // nextLine[] is an array of values from the line
                System.out.println(nextLine[0] + nextLine[1] + "etc...");
                String id = nextLine[0];
                String time = nextLine[1];
                float x = Float.parseFloat(nextLine[2]);
                float y = Float.parseFloat(nextLine[3]);
                float z = Float.parseFloat(nextLine[4]);
                seriesData.add(new CustomDataEntry((c++) + "", x, y, z));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "The specified file was not found", Toast.LENGTH_SHORT).show();
        }

    }

    void initChart(View fragmentView ) {
        AnyChartView anyChartView = fragmentView.findViewById(R.id.any_chart_view);
        anyChartView.setProgressBar(fragmentView.findViewById(R.id.progress_bar));

        Cartesian cartesian = AnyChart.line();

        cartesian.animation(true);

        cartesian.padding(10d, 20d, 5d, 20d);

        cartesian.crosshair().enabled(true);
        cartesian.crosshair()
                .yLabel(true)
                // TODO ystroke
                .yStroke((Stroke) null, null, null, (String) null, (String) null);

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT);

        cartesian.title("Walking");

        cartesian.yAxis(0).title("Acc");
        cartesian.xAxis(0).labels().padding(5d, 5d, 5d, 5d);



        Set set = Set.instantiate();
        set.data(seriesData);
        Mapping series1Mapping = set.mapAs("{ x: 'x', value: 'value' }");
        Mapping series2Mapping = set.mapAs("{ x: 'x', value: 'value2' }");
        Mapping series3Mapping = set.mapAs("{ x: 'x', value: 'value3' }");

        Line series1 = cartesian.line(series1Mapping);
        series1.name("X");
        series1.hovered().markers().enabled(true);
        series1.hovered().markers()
                .type(MarkerType.CIRCLE)
                .size(4d);
        series1.tooltip()
                .position("right")
                .anchor(Anchor.LEFT_CENTER)
                .offsetX(5d)
                .offsetY(5d);

        Line series2 = cartesian.line(series2Mapping);
        series2.name("Y");
        series2.hovered().markers().enabled(true);
        series2.hovered().markers()
                .type(MarkerType.CIRCLE)
                .size(4d);
        series2.tooltip()
                .position("right")
                .anchor(Anchor.LEFT_CENTER)
                .offsetX(5d)
                .offsetY(5d);

        Line series3 = cartesian.line(series3Mapping);
        series3.name("Z");
        series3.hovered().markers().enabled(true);
        series3.hovered().markers()
                .type(MarkerType.CIRCLE)
                .size(4d);
        series3.tooltip()
                .position("right")
                .anchor(Anchor.LEFT_CENTER)
                .offsetX(5d)
                .offsetY(5d);

        cartesian.legend().enabled(true);
        cartesian.legend().fontSize(13d);
        cartesian.legend().padding(0d, 0d, 10d, 0d);

        anyChartView.setChart(cartesian);
    }

    private class CustomDataEntry extends ValueDataEntry {

        CustomDataEntry(String x, Number value, Number value2, Number value3) {
            super(x, value);
            setValue("value2", value2);
            setValue("value3", value3);
        }

    }
}