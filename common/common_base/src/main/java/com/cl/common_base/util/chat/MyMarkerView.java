
package com.cl.common_base.util.chat;

import static com.cl.common_base.ext.Metric2InchConversionKt.temperatureConversionTwo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.TextView;
import android.widget.Toast;

import com.cl.common_base.R;
import com.cl.common_base.bean.PlantData;
import com.cl.common_base.constants.Constants;
import com.cl.common_base.ext.CommonExtKt;
import com.cl.common_base.util.Prefs;
import com.cl.common_base.widget.toast.ToastUtil;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Custom implementation of the MarkerView.
 *
 * @author Philipp Jahoda
 */
@SuppressLint("ViewConstructor")
public class MyMarkerView extends MarkerView {
    // if (!Prefs.getBoolean(Constants.My.KEY_MY_WEIGHT_UNIT, false)) "inch" else "mefric",

    final SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm", Locale.US);
    private SimpleDateFormat timeFormat = new SimpleDateFormat("MM-dd", Locale.US);

    private LineChart chart; // 添加图表引用

    private final TextView tvContent;

    public MyMarkerView(Context context, int layoutResource, LineChart chart) {
        super(context, layoutResource);

        this.chart = chart; // 保存图表引用
        tvContent = findViewById(R.id.tvContent);
    }

    private String type;

    public void setType(String type) {
        this.type = type;
    }

    private List<Entry> entries = new ArrayList<>();
    private List<PlantData.DataPoint> dataPoints = new ArrayList<>();

    public void setEntries(List<Entry> entries, List<PlantData.DataPoint> dataPoints) {
        this.entries = entries;
        this.dataPoints = dataPoints;
    }

    // runs every time the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        if (entries.isEmpty()) return;
        if (dataPoints.isEmpty()) return;
       /* if (e instanceof CandleEntry) {

            CandleEntry ce = (CandleEntry) e;

            tvContent.setText(Utils.formatNumber(ce.getHigh(), 0, true));
        } else {

            // tvContent.setText(Utils.formatNumber(e.getY(), 0, true));
            // 格式化时间
            String value = Utils.formatNumber(e.getY(), 0, true);
            String time =  sdf.format(new Date((long) (CommonExtKt.safeToFloat(String.format("%.0f", e.getX())) * 1000L)));
            tvContent.setText(time + "  " + value);
        }*/

        String legendName = "";
        int dataSetIndex = highlight.getDataSetIndex();

        // 获取数据集并从中提取图例名字
        ILineDataSet dataSet = chart.getLineData().getDataSetByIndex(dataSetIndex);
        if (dataSet != null) {
            legendName = dataSet.getLabel();
        }

        String value = Utils.formatNumber(e.getY(), 1, true);

        String format = String.format("%.0f", e.getX());
        long l = (CommonExtKt.safeToLong(format) + originalValue) * 1000L;
        String time = sdf.format(new Date(l));
        String times = timeFormat.format(new Date(l));
        boolean aBoolean = Prefs.getBoolean(Constants.My.KEY_MY_WEIGHT_UNIT, false);
        switch (type) {
            case "ph":
                tvContent.setText(times + "\n" + legendName + ": " + value);
                break;
            case "temp":
                // false 摄氏度， true 华氏度
                String unit = aBoolean ? "°C" : "°F";
                tvContent.setText(time + "\n" + legendName + ": " + value + unit);
                break;
            case "humidity":
                tvContent.setText(time + "\n" + legendName + ": " + value + "%");
                break;
        }

        /*float l = CommonExtKt.safeToLong(String.format("%.0f", e.getX())) / 1000f;
        final float EPSILON = 0.1f;  // 定义一个合适的容差
       *//* for (Entry point : entries) {
            if (Math.abs(point.getX() / 1000.0f - l) < EPSILON) {  // 将每个数据点的 x 值除以 1000 并比较
                tvContent.setText(point.getInfo());  // 显示匹配数据点的信息
                super.refreshContent(e, highlight);
                return;
            }
        }*//*

        for (PlantData.DataPoint dataPoint : dataPoints) {
            if (Math.abs(CommonExtKt.safeToFloat(dataPoint.getDateTime()) / 1000.0f - l) < EPSILON) {  // 将每个数据点的 x 值除以 1000 并比较
                String time = sdf.format(new Date((long) (CommonExtKt.safeToLong(dataPoint.getDateTime()) * 1000L)));
                String times = timeFormat.format(new Date((long) (CommonExtKt.safeToLong(dataPoint.getDateTime()) * 1000L)));
                boolean aBoolean = Prefs.getBoolean(Constants.My.KEY_MY_WEIGHT_UNIT, false);
                if (type.equals("ph")) {
                    tvContent.setText(times + "\n" + legendName + ": " + value);
                } else if (type.equals("temp")) {
                    // false 摄氏度， true 华氏度
                    String unit = aBoolean ? "°C" :"°F";
                    tvContent.setText(time + "\n" + legendName + ": " + temperatureConversionTwo(CommonExtKt.safeToFloat(value), aBoolean) + unit);
                } else if (type.equals("humidity")) {
                    tvContent.setText(time + "\n" + legendName + ": " +  value + "%");
                }
            }
        }*/
        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2), -getHeight());
    }

    public long formatTimestamp(float timestamp) {
        // 四舍五入到最接近的整数
        long roundedTimestamp = Math.round(timestamp);
        return roundedTimestamp;
    }

    private long originalValue;
    public void setOriginalValue(long originalValue) {
        this.originalValue = originalValue;
    }
}
