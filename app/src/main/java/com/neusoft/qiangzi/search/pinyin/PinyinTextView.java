package com.neusoft.qiangzi.search.pinyin;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ScrollView;

import java.util.ArrayList;

public class PinyinTextView extends androidx.appcompat.widget.AppCompatTextView {


    private static final String TAG = "PinyinTextView";
    private final int fontSize = 72;
    private String[] pinyin;

    private String[] hanzi;

    private int color = Color.rgb(99, 99, 99);

    private int[] colors = new int[]{Color.rgb(0x3d, 0xb1, 0x69), Color.rgb(99, 99, 99)};
    private TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

    private Paint.FontMetrics fontMetrics;
    private final int paddingTop = 20;
    private final int lestHeight = 141;
    private int snot = 0;
    private ScrollView scrollView;
    private ArrayList<String> dots = new ArrayList<>(); // 统计标点长度

    private ArrayList<Integer> indexList = new ArrayList<>();    // 存储每行首个String位置
    int comlum = 1;
    float density;

    private boolean isScrollEnable = false;
    private Paint.FontMetricsInt fontMetricsInt;
    private float fontSizeChinese = ConvertUtils.dp2px(getContext(), 18);

    public PinyinTextView(Context context) {
        this(context, null);
    }

    public PinyinTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PinyinTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

//        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PinyinTextView);
//        color = typedArray.getColor(R.styleable.PinyinTextView_textColor, Color.BLACK);
//
//        typedArray.recycle();

        initTextPaint();
    }

    public void initTextPaint() {
        textPaint.setColor(color);
        float denity = getResources().getDisplayMetrics().density;
        textPaint.setStrokeWidth(denity * 2);
        textPaint.setTextSize(fontSizeChinese);
        fontMetrics = textPaint.getFontMetrics();
        fontMetricsInt = textPaint.getFontMetricsInt();

        density = getResources().getDisplayMetrics().density;
    }

    //设置文字大小
    public void setFontSize(float chineseFontSize) {
        this.fontSizeChinese = ConvertUtils.dp2px(getContext(),chineseFontSize);
        initTextPaint();
    }

    public void setPinyin(String[] pinyin) {
        this.pinyin = pinyin;
    }

    public void setHanzi(String[] hanzi) {
        this.hanzi = hanzi;
    }

    public void setColor(int color) {
        this.color = color;
        snot = 0;
        if (textPaint != null) {
            textPaint.setColor(color);
        }
    }

    public void setScrollEnable(boolean isScrollEnable) {

        Log.e(TAG, "isScrollEnable == " + isScrollEnable);
        this.isScrollEnable = isScrollEnable;
        if (isScrollEnable) {
            setMovementMethod(ScrollingMovementMethod.getInstance());
        } else {
            setMovementMethod(null);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 需要根据文本测量高度
        int widthMode, heightMode;
        int width = 0, height = 0;
        indexList.clear();
        widthMode = MeasureSpec.getMode(widthMeasureSpec);
        heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (widthMode == MeasureSpec.EXACTLY) {
            width = MeasureSpec.getSize(widthMeasureSpec);
        }
        if (heightMode == MeasureSpec.EXACTLY) {
            height = MeasureSpec.getSize(heightMeasureSpec);
        } else if (heightMode == MeasureSpec.AT_MOST) {
            if (textPaint != null) {
                if (pinyin != null && pinyin.length != 0) {
                    height = (int) ((pinyin.length / 10 + 1) * 2 * (fontMetrics.bottom - fontMetrics.top) + paddingTop);
                } else if (hanzi != null) {
                    height = (int) ((fontMetrics.bottom - fontMetrics.top) + paddingTop);
                }
            }
        } else if (height == MeasureSpec.UNSPECIFIED) {
            if (textPaint != null) {
                if (pinyin != null && pinyin.length != 0) {
                    float pinyinWidth = 0;
                    int comlumTotal = 1;
                    for (int index = 0; index < pinyin.length; index++) {
                        if (TextUtils.equals(pinyin[index], "null")) {
                            pinyinWidth = pinyinWidth + textPaint.measureText(hanzi[index]);
                        } else {
                            pinyinWidth = pinyinWidth + textPaint.measureText(pinyin[index].substring(0, pinyin[index].length() - 1));
                        }
                        if (pinyinWidth > width) {
                            indexList.add(index);
                            comlumTotal++;
                            pinyinWidth = (TextUtils.equals(pinyin[index], "null") ?
                                    textPaint.measureText(pinyin[index]) : textPaint.measureText(pinyin[index].substring(0, pinyin[index].length() - 1)));
                        }
                    }
                    height = (int) Math.ceil((comlumTotal * 2) * (textPaint.getFontSpacing() + density * 1));
                } else if (hanzi != null) {
                    height = (int) textPaint.getFontSpacing();
                }
            }
        }
        height = height < lestHeight ? lestHeight : height;
        setMeasuredDimension(width, height);
    }

    private int snotMark = 0;

    private void scrollByUser(int snot, boolean isByUser) {
        if (snotMark != snot && !isByUser && scrollView != null) {
            scrollView.smoothScrollBy(0, (int) ((fontMetrics.bottom - fontMetrics.top) * 2) + 10);
            dots.clear();
        }
        this.snotMark = snot;
    }

    public void startScrolling(int snot) {
        if (snotMark != snot && scrollView != null) {
            scrollView.smoothScrollTo(0, 0);
            snot = 0;
            dots.clear();
        }
        this.snotMark = snot;
    }

    private int snotDrawMark = 0;
    private float pinyinWidth = 0;

    @Override
    protected void onDraw(Canvas canvas) {
        float widthMesure = 0f;
        if (indexList.isEmpty()) {
            // 单行数据处理
            if (pinyin != null && pinyin.length > 0) {
                widthMesure = (getWidth() - textPaint.measureText(combinePinEnd(0, pinyin.length))) / 2;
                Log.e("jacky", "widthMesure1 === " + widthMesure);
            } else if (hanzi != null && hanzi.length > 0) {
                widthMesure = (getWidth() - textPaint.measureText(combineHanziEnd(0, hanzi.length))) / 2;
            }
        }
        int count = 0;
        pinyinWidth = 0;
        comlum = 1;
        if (pinyin != null && pinyin.length > 0) {
            for (int index = 0; index < pinyin.length; index++) {
                if (snot != 0 && snot >= index) {
                    textPaint.setColor(colors[0]);
                    if (indexList.contains(snot)) {
                        scrollByUser(snot, false);
                    }
                } else {
                    textPaint.setColor(colors[1]);
                }
                if (!TextUtils.equals(pinyin[index], "null") && !TextUtils.equals(pinyin[index], " ")) {
                    pinyinWidth = widthMesure + textPaint.measureText(pinyin[index].substring(0, pinyin[index].length() - 1));
                    if (pinyinWidth > getWidth()) {
                        comlum++;
                        widthMesure = 0;
                        // 多行考虑最后一行居中问题
                        if (indexList.size() > 1 && indexList.get(indexList.size() - 1) == index) {
                            // 最后一行
                            widthMesure = (getWidth() - textPaint.measureText(combinePinEnd(index, pinyin.length))) / 2;
                        }
                    }
                    Log.e(TAG, "widthmeasure2 === " + widthMesure);
                    canvas.drawText(pinyin[index].substring(0, pinyin[index].length() - 1), widthMesure, (comlum * 2 - 1) * (textPaint.getFontSpacing()), textPaint);
                    String tone = " ";
                    switch (pinyin[index].charAt(pinyin[index].length() - 1)) {
                        case '1':
                            tone = "ˉ";
                            break;
                        case '2':
                            tone = "ˊ";
                            break;
                        case '3':
                            tone = "ˇ";
                            break;
                        case '4':
                            tone = "ˋ";
                            break;
                    }
                    int toneIndex = pinyin[index].length() - 3;  // 去掉数字和空格符
                    int stateIndex = -1;
                    for (; toneIndex >= 0; toneIndex--) {
                        if (pinyin[index].charAt(toneIndex) == 'a' || pinyin[index].charAt(toneIndex) == 'e'
                                || pinyin[index].charAt(toneIndex) == 'i' || pinyin[index].charAt(toneIndex) == 'o'
                                || pinyin[index].charAt(toneIndex) == 'u' || pinyin[index].charAt(toneIndex) == 'v') {
                            if (stateIndex == -1 || pinyin[index].charAt(toneIndex) < pinyin[index].charAt(stateIndex)) {
                                stateIndex = toneIndex;
                            }
                        }
                    }
                    // iu同时存在规则
                    if (pinyin[index].contains("u") && pinyin[index].contains("i") && !pinyin[index].contains("a") && !pinyin[index].contains("o") && !pinyin[index].contains("e")) {
                        stateIndex = pinyin[index].indexOf("u") > pinyin[index].indexOf("i") ? pinyin[index].indexOf("u") : pinyin[index].indexOf("i");
                    }
                    Log.e(TAG, "stateIndex === " + stateIndex);
                    if (stateIndex != -1) {
                        // 没有声母存在时，stateIndex一直为-1 （'嗯' 转成拼音后变成 ng,导致没有声母存在，stateIndex一直为-1，数组越界crash）
                        canvas.drawText(tone, widthMesure + textPaint.measureText(pinyin[index].substring(0, stateIndex)) + (textPaint.measureText(pinyin[index].charAt(stateIndex) + "") - textPaint.measureText(tone + "")) / 2, (comlum * 2 - 1) * (textPaint.getFontSpacing()), textPaint);
                    }
                    canvas.drawText(hanzi[index], widthMesure + (textPaint.measureText(pinyin[index].substring(0, pinyin[index].length() - 1)) - textPaint.measureText(hanzi[index])) / 2 - moveHalfIfNeed(pinyin[index].substring(0, pinyin[index].length() - 1), textPaint), (comlum * 2) * (textPaint.getFontSpacing()), textPaint);  // 由于拼音长度固定，采用居中显示策略，计算拼音实际长度不需要去掉拼音后面空格
                    if (index + 1 < pinyin.length && TextUtils.equals("null", pinyin[index + 1])) {
                        widthMesure = widthMesure + textPaint.measureText(pinyin[index].substring(0, pinyin[index].length() - 1));
                    } else {
                        widthMesure = widthMesure + textPaint.measureText(pinyin[index].substring(0, pinyin[index].length() - 1));    // 下个字符为拼音
                    }
                    if (index % 10 == 0 && index >= 10 && textPaint.getColor() == colors[1]) {
                    }
                    count = count + 1; // 有效拼音
                } else if (TextUtils.equals(pinyin[index], "null")) {  //   (count / 10) * 100 + 80   之前高度

                    if (!dots.isEmpty()) {
                        float hanziWidth = widthMesure + textPaint.measureText(hanzi[index]);
                        if (hanziWidth > getWidth()) {
                            comlum++;
                            widthMesure = 0;
                        }
                        canvas.drawText(hanzi[index], widthMesure, (comlum * 2) * textPaint.getFontSpacing(), textPaint);
                        widthMesure = widthMesure + textPaint.measureText(hanzi[index]);
                    } else {
                        float hanziWidth = widthMesure + textPaint.measureText(hanzi[index]);
                        if (hanziWidth > getWidth()) {
                            comlum++;
                            widthMesure = 0;
                        }
                        canvas.drawText(hanzi[index], widthMesure, (comlum * 2) * textPaint.getFontSpacing(), textPaint);
                        widthMesure = widthMesure + textPaint.measureText(hanzi[index]);
                    }
                    count = count + 1;
                }
            }
        } else {

        }
        snotDrawMark = snot;
        super.onDraw(canvas);
    }

    private float moveHalfIfNeed(String pinyinUnit, TextPaint paint) {

        if (pinyinUnit.trim().length() % 2 == 0) {
            return paint.measureText(" ") / 2;
        } else {
            return 0;
        }
    }

    private String combinePinEnd(int index, int length) {
        StringBuilder sb = new StringBuilder();
        for (int subIndex = index; subIndex < length; subIndex++) {
            String pendString = pinyin[subIndex].substring(0, pinyin[subIndex].length() - 1);
            sb.append(pendString);
        }
        return sb.toString();
    }

    private String combineHanziEnd(int index, int length) {
        StringBuilder sb = new StringBuilder();
        for (int subIndex = index; subIndex < length; subIndex++) {
            sb.append(hanzi[subIndex]);
        }
        return sb.toString();
    }
}
