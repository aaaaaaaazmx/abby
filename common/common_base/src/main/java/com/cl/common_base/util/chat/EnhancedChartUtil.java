package com.cl.common_base.util.chat;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

import com.cl.common_base.R;
import com.cl.common_base.bean.PlantData;
import com.cl.common_base.constants.Constants;
import com.cl.common_base.ext.CommonExtKt;
import com.cl.common_base.util.Prefs;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.github.mikephil.charting.formatter.ValueFormatter;

public class EnhancedChartUtil {
    public void setupEnhancedLineChart(LineChart lineChart, List<PlantData.DataPoint> dataPoints, String type, String lableName) {
        boolean aBoolean = Prefs.getBoolean(Constants.My.KEY_MY_WEIGHT_UNIT, false);

        // 没有数据
        if (dataPoints == null || dataPoints.isEmpty()) {
            // 设置无数据时的提示文本
            lineChart.setNoDataText("No data available.");

            // 设置无数据文本的颜色（可选）
            lineChart.setNoDataTextColor(Color.GRAY);
            lineChart.invalidate();
            return;
        }

        List<Entry> entries = new ArrayList<>();
        final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd", Locale.US);
        final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.US);

        for (PlantData.DataPoint dataPoint : dataPoints) {
            entries.add(new Entry(CommonExtKt.safeToFloat(dataPoint.getDateTime()), CommonExtKt.safeToFloat(dataPoint.getCodeValue())));
        }

        LineDataSet dataSet = new LineDataSet(entries, lableName);
        // 设置为曲线模式
        dataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        // 设置不显示值
        dataSet.setDrawValues(false);

        // 对虚拟数据点的样式进行设置，使其不可见
        if (type.equals("temp")) {
            // 温度
            dataSet.setColor(Color.parseColor("#006241")); // 设置线条颜色
            dataSet.setCircleColor(Color.parseColor("#006241"));
        }
        if (type.equals("humidity")) {
            // 湿度
            dataSet.setColor(Color.parseColor("#70D9FF"));
            dataSet.setCircleColor(Color.parseColor("#70D9FF"));
        }

        if (type.equals("ph")) {
            // ph
            dataSet.setColor(Color.parseColor("#4CD964"));
            dataSet.setCircleColor(Color.parseColor("#4CD964"));
            // 创建渐变Drawable
            GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{Color.parseColor("#4CD964"), Color.TRANSPARENT}); // 定义渐变颜色

            // 将渐变设置为填充
            dataSet.setFillDrawable(gradientDrawable);
            dataSet.setDrawFilled(true);        }

        //dataSet.setValueTextColor(...); // 设置数据点文本颜色
        dataSet.setHighLightColor(Color.rgb(244, 117, 117)); // 设置高亮颜色
        // dataSet.setCircleColor(Color.WHITE);
        dataSet.setDrawCircles(true); // 确保绘制圆点
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(3f);
        dataSet.setFillAlpha(65);
        dataSet.setDrawCircleHole(false);

        LineData lineData = new LineData(dataSet);
        // 设置背景颜色
        // lineChart.setBackgroundColor(Color.parseColor("#D9D9D9"));
        // 显示标记框
        MyMarkerView mv = new MyMarkerView(lineChart.getContext(), R.layout.custom_marker_view, lineChart);
        mv.setType(type);
        mv.setEntries(entries, dataPoints);
        mv.setChartView(lineChart); // For bounds control
        lineChart.setMarker(mv); // Set the marker to the chart


        // X轴设置
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false); // 不显示网格线
        // xAxis.setLabelCount(2, true); // 设置标签数量
        xAxis.setLabelRotationAngle(-45f); // 将标签旋转45度
        xAxis.setAxisMinimum(lineData.getXMin() - 100f); // 主要目的就是设置间隔，让其与Y轴有一些距离。更加美观。
        /*xAxis.setValueFormatter(new IndexAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value == (lineData.getXMin()) - 100f) {
                    return "";
                }
                return sdf.format(new Date((long) value * 1000));
            }
        });*/
        xAxis.setValueFormatter(new MyXAxisFormatter(type, entries));

        // Y轴设置
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setDrawGridLines(true); // 显示网格线
        leftAxis.setStartAtZero(false); // 不从0开始

        // 右边Y轴
        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false); // 不显示右侧Y轴


        lineChart.setScaleEnabled(true); // 启用图表的缩放功能
        lineChart.setPinchZoom(false); // 启用双指缩放
        lineChart.setDragEnabled(true); // 启用拖动
        lineChart.getDescription().setEnabled(false); // 不显示图表描述
        lineChart.getLegend().setEnabled(true); // 显示图例
        lineChart.setTouchEnabled(true); // 启用触摸
        // 单独启用 X 轴的缩放
        lineChart.setScaleXEnabled(true);
        lineChart.setScaleYEnabled(false);  // 禁用 Y 轴的缩放
        lineChart.setExtraOffsets(0, 0, 0, 10f); // 在图表的底部增加10dp的额外偏移

        // 图例设置
        Legend legend = lineChart.getLegend();
        // legend.setXEntrySpace(45f); // 设置图例水平间距
        // legend.setYEntrySpace(10f); // 设置图例垂直间距

        // 或者调整图例的Y轴偏移量
        // legend.setYOffset(45f);

        // 图列样式
        // 设置图例的垂直对齐方式为底部
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        // 设置图例的水平对齐方式为中心
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        // 设置图例的方向为水平
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        // 设置图例的形状为线，这是可选的，取决于你想要的图例样式
        legend.setForm(Legend.LegendForm.CIRCLE);


        /*if (!entries.isEmpty()) {
            // 获取最后一个数据点的x值
            float lastXValue = entries.get(entries.size() - 1).getX();

            // 移动视图窗口到最后一个点
            // 注意：这里可能需要根据你的数据和需求调整，以确保最后一个点显示在合适的位置
            lineChart.moveViewToX(lastXValue);

            // 放大图表
            // 这里的缩放级别（scaleX和scaleY）根据你的需求自行调整
            // x和y通常可以设置为焦点在图表中的坐标位置，这里我们简单地使用0，意味着从图表的起始位置开始缩放
            lineChart.zoom(2f, 2f, 0, 0);

            // 刷新图表以应用更改
            // lineChart.invalidate();
        }*/
        lineChart.setData(lineData);
        lineChart.invalidate(); // 刷新图表

        // 移动到最后一个点
        /*if (!entries.isEmpty()) {
            float x = entries.get(entries.size() - 1).getX();
            float y = entries.get(entries.size() -1).getY();
            lineChart.centerViewToAnimated(x, y, lineChart.getData().getDataSetByIndex(0)
                    .getAxisDependency(), 500);
        }*/

    }

    public class MyXAxisFormatter extends ValueFormatter {
        private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd", Locale.US);
        private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.US);

        private String type;
        private List<Entry> entries = new ArrayList<>();

        public MyXAxisFormatter(String type, List<Entry> entries) {
            this.type = type;
            this.entries = entries;
        }

        @Override
        public String getFormattedValue(float value) {
            // todo float 精度缺失
            // Assuming value is in milliseconds
            long millis = (long) value * 1000;
            Date date = new Date(millis);
            if (type.equals("ph")) {
                return dateFormat.format(date);
            }
            return dateFormat.format(date) + " \n " + timeFormat.format(date);  // Combine two formats with line break
        }
    }
}

