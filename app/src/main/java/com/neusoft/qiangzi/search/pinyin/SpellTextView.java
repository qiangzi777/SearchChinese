package com.neusoft.qiangzi.search.pinyin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import com.neusoft.qiangzi.search.R;

@SuppressLint("AppCompatCustomView")
public class SpellTextView extends TextView {
    private static final String TAG = "SpellTextView";
    private String[] pinyin;
    private String[] chinese;

    private TextPaint textPaintSpell = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private TextPaint textPaintChinese = new TextPaint(Paint.ANTI_ALIAS_FLAG);


    private int fontSizeSpell = ConvertUtils.dp2px(getContext(), getResources().getInteger(R.integer.PinyinFontSize));
    private int fontSizeChinese = ConvertUtils.dp2px(getContext(),getResources().getInteger(R.integer.HanziFontSize));
    private boolean isWrapWords = true;
    private int colorSpell = getContext().getColor(R.color.pinyinColor);
    private int colorChinese = getContext().getColor(R.color.hanziColor);
    public SpellTextView(Context context) {
        super(context);
    }

    public SpellTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SpellTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initTextPaint();
    }

    public void initTextPaint() {
        float denity = getResources().getDisplayMetrics().density;
        textPaintSpell.setStrokeWidth(denity);
        textPaintChinese.setStrokeWidth(denity);
        textPaintSpell.setTextAlign(Paint.Align.LEFT);
        textPaintChinese.setTextAlign(Paint.Align.LEFT);
        //设置字体大小
        textPaintSpell.setTextSize(fontSizeSpell);
        textPaintChinese.setTextSize(fontSizeChinese);
        textPaintSpell.setColor(colorSpell);
        textPaintChinese.setColor(colorChinese);
        setBackgroundResource(R.drawable.tv_bg_selector);
//        setBackground(getResources().getDrawable(R.drawable.tv_bg_selector));

    }
    public int getFontSizeSpell() {
        return ConvertUtils.px2dp(getContext(), fontSizeSpell);
    }

    public void setFontSizeSpell(int dipFontSize) {
        this.fontSizeSpell = ConvertUtils.dp2px(getContext(), dipFontSize);
    }

    public int getFontSizeChinese() {
        return ConvertUtils.px2dp(getContext(), fontSizeChinese);
    }

    public void setFontSizeChinese(int dipFontSize) {
        this.fontSizeChinese = ConvertUtils.dp2px(getContext(), dipFontSize);
    }

    protected int getMesureWidth(){
        float widthMesure = 0f;
        if (pinyin != null && pinyin.length > 0) {
            for (int index = 0; index < pinyin.length; index++) {
                widthMesure += textPaintSpell.measureText(pinyin[index]);
            }
        }
        widthMesure += textPaintChinese.getFontSpacing()/2;
        Log.d(TAG, "getMesureWidth: w="+widthMesure);
        return (int)widthMesure;
    }
    protected int getMesureHeight(){
        int h = (int)(textPaintChinese.getFontSpacing()*2.5);
        Log.d(TAG, "getMesureHeight: h="+h);
        return h;
    }
    @Override
    protected void onDraw(Canvas canvas) {
        float widthMesure = 0f;
        int comlum = 1;
        float pinyinWidth;
        if (pinyin != null && pinyin.length > 0) {
            for (int index = 0; index < pinyin.length; index++) {
                pinyinWidth = widthMesure + textPaintSpell.measureText(pinyin[index]);
                if (pinyinWidth > getWidth()) {
                    comlum++;
                    widthMesure = 0;
                }
                canvas.drawText(pinyin[index], widthMesure, (comlum * 2 - 1) * (textPaintChinese.getFontSpacing()), textPaintSpell);
                canvas.drawText(chinese[index],
                        widthMesure + (textPaintSpell.measureText(pinyin[index]) - textPaintChinese.measureText(chinese[index])) / 2,
                        (comlum * 2) * (textPaintChinese.getFontSpacing()), textPaintChinese);
                if (index + 1 < pinyin.length) {
                    widthMesure = widthMesure + textPaintSpell.measureText(pinyin[index] + 1);
                } else {
                    widthMesure = widthMesure + textPaintSpell.measureText(pinyin[index]);
                }
            }
        }
    }

    //拼音和汉字的资源
    public void setSpellAndChinese(String[] pinYin, String[] chinese) {
        this.pinyin = pinYin;
        this.chinese = chinese;
    }

    //设置文字资源
    public void setChineseString(String string) {
        initTextPaint();
        String[] spellArray = PinyinUtils.getSpellString(string);
//        StringBuilder stringBuilder = new StringBuilder();
//        for (String s : spellArray){
//            stringBuilder.append(s);
//            stringBuilder.append(" ");
//        }
        setText(string);//?
        char[] chars = string.toCharArray();
        String[] chineseArray = new String[chars.length];
        for (int i = 0; i < chars.length;i++){
            chineseArray[i] = String.valueOf(chars[i]);
        }
        setSpellAndChinese(spellArray,chineseArray);
        if(isWrapWords){
            setWidth(getMesureWidth());
            setHeight(getMesureHeight());
        }
    }
    public String getChineseString(){
        return getText().toString();
    }

    //设置文字颜色
    public void setStringColor(int spellColor,int chineseColor) {
        textPaintSpell.setColor(spellColor);
        textPaintChinese.setColor(chineseColor);
    }

    //设置文字大小
    public void setFontSize(float spellFontSize,float chineseFontSize) {
        textPaintSpell.setTextSize(ConvertUtils.dp2px(getContext(), spellFontSize));
        textPaintChinese.setTextSize(ConvertUtils.dp2px(getContext(), chineseFontSize));
    }
}
