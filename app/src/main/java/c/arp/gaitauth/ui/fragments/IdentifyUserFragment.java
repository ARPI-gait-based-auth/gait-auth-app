package c.arp.gaitauth.ui.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import c.arp.gaitauth.Preprocessor.CustomDataEntry;
import c.arp.gaitauth.Preprocessor.Preprocessor;
import c.arp.gaitauth.R;
import c.arp.gaitauth.StaticStore;

import android.widget.Button;
import android.widget.TextView;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Line;
import com.anychart.core.utils.OrdinalZoom;
import com.anychart.data.Mapping;
import com.anychart.data.Set;
import com.anychart.enums.Anchor;
import com.anychart.enums.MarkerType;
import com.anychart.enums.TooltipPositionMode;
import com.anychart.graphics.vector.Stroke;

import java.util.ArrayList;
import java.util.List;

public class IdentifyUserFragment extends Fragment {
    public static final int PICKFILE_RESULT_CODE = 1;
    private Button btnSelectFile;
    private TextView textFile;
    List<DataEntry> seriesData = new ArrayList<>();
    View root;
    AnyChartView anyChartView;
    Set set;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_identify_user, container, false);


        btnSelectFile = (Button) root.findViewById(R.id.select_file);
        textFile = (TextView) root.findViewById(R.id.text_dashboard_selected_file);

        anyChartView = root.findViewById(R.id.any_chart_view);
        anyChartView.setProgressBar(root.findViewById(R.id.progress_bar));

        btnSelectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                chooseFile.setType("*/*");
                chooseFile = Intent.createChooser(chooseFile, "Choose a file");
                startActivityForResult(chooseFile, PICKFILE_RESULT_CODE);
            }
        });

        seriesData.add(new CustomDataEntry("", 0, 0, 0));
        initData();
        initChart();

        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PICKFILE_RESULT_CODE:
                if (resultCode == -1) {
                    Uri fileUri = data.getData();
                    StaticStore.selectedFile = fileUri.getLastPathSegment().split(":")[1];

                    initData();
                    set.data(seriesData);
                }
                break;
        }
    }

    void initData() {
        if (StaticStore.selectedFile == null) {
            return;
        }
        textFile.setText(StaticStore.selectedFile);
        seriesData.removeAll(new ArrayList<>());

        Preprocessor p = new Preprocessor(StaticStore.selectedFile);
        p.data.resample();
        p.data.setToSeriesData(seriesData);
    }

    void initChart() {
        Cartesian cartesian = AnyChart.line();

        cartesian.xScroller(true);

        OrdinalZoom xZoom = cartesian.xZoom();

        xZoom.setToPointsCount(6, false, null);

        xZoom.getStartRatio();
        xZoom.getEndRatio();

        cartesian.animation(true);

        cartesian.padding(10d, 20d, 20d, 20d);

        cartesian.crosshair().enabled(true);
        cartesian.crosshair()
                .yLabel(true)
                // TODO ystroke
                .yStroke((Stroke) null, null, null, (String) null, (String) null);

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT);

        cartesian.title("Walking");

        cartesian.yAxis(0).title("Acc");
        cartesian.xAxis(0).labels().padding(5d, 5d, 5d, 5d);


        set = Set.instantiate();

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
}