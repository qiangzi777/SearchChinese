package com.neusoft.qiangzi.search.pinyin;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.baidu.ocr.sdk.model.WordSimple;
import com.neusoft.qiangzi.search.R;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class PinyinUtils {

    private static final char[][] toneCharArray = {
            {'a', 'ā'},
            {'a', 'á'},
            {'a', 'ǎ'},
            {'a', 'à'},
            {'e', 'ē'},
            {'e', 'é'},
            {'e', 'ě'},
            {'e', 'è'},
            {'i', 'ī'},
            {'i', 'í'},
            {'i', 'ǐ'},
            {'i', 'ì'},
            {'o', 'ō'},
            {'o', 'ó'},
            {'o', 'ǒ'},
            {'o', 'ò'},
            {'u', 'ū'},
            {'u', 'ú'},
            {'u', 'ǔ'},
            {'u', 'ù'},
            {'v', 'ǜ'},
            {'v', 'ǘ'},
            {'v', 'ǚ'},
            {'v', 'ǜ'},
    };
    public static char toneCharToChar(char c){
        for(int i=0;i<toneCharArray.length;i++){
            if(toneCharArray[i][1]==c)
                return toneCharArray[i][0];
        }
        return c;
    }
    public static char charToToneChar(char c, int tone){
        if(tone < 1 || tone > 4)return c;
        for(int i=0;i<toneCharArray.length;i++){
            if(toneCharArray[i][0]==c)
                return toneCharArray[i+tone-1][1];
        }
        return c;
    }
    public static String toneStringToString(String str){
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<str.length();i++){
            sb.append(toneCharToChar(str.charAt(i)));
        }
        return sb.toString();
    }

    public static String[] getSpellString(String character) {
        if (character != null && character.length() > 0) {
            String[] pinyin = new String[character.length()];
            HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
            format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
            format.setToneType(HanyuPinyinToneType.WITH_TONE_MARK);
            format.setVCharType(HanyuPinyinVCharType.WITH_U_UNICODE);
            for (int index = 0; index < character.length(); index++) {
                char c = character.charAt(index);
                try {
                    String[] pinyinUnit = PinyinHelper.toHanyuPinyinStringArray(c, format);
                    if (pinyinUnit == null) {
                        pinyin[index] = "  ";
                    } else {
                        pinyin[index] = pinyinUnit[0];
                    }
                } catch (BadHanyuPinyinOutputFormatCombination badHanyuPinyinOutputFormatCombination) {
                    badHanyuPinyinOutputFormatCombination.printStackTrace();
                }

            }
            return pinyin;
        } else {
            return null;
        }
    }

    public static View getCharView(Context context, char c){
        SpellTextView tv = new SpellTextView(context);
        tv.setId(View.generateViewId());
        tv.setChineseString(String.valueOf(c));
        tv.setBackgroundResource(R.drawable.tv_bg_selector);
        tv.setFocusable(true);
        tv.setFocusableInTouchMode(true);
        tv.setClickable(true);

        return tv;
    }

    public static View getWordView(Context context, WordSimple w){
        SpellTextView tv = new SpellTextView(context);
        tv.setId(View.generateViewId());
        tv.setChineseString(w.getWords());
        tv.setBackgroundResource(R.drawable.tv_bg_selector);
        tv.setFocusable(true);
        tv.setClickable(true);
        return tv;
    }

    public static String[] getPinyinString(String hanzi) {
        if (hanzi != null && hanzi.length() > 0) {
            String[] pinyin = new String[hanzi.length()];
            HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
            format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
            format.setToneType(HanyuPinyinToneType.WITH_TONE_NUMBER);
            for (int index = 0; index < hanzi.length(); index++) {
                char c = hanzi.charAt(index);
                try {
                    String[] pinyinUnit = PinyinHelper.toHanyuPinyinStringArray(c, format);
                    if (pinyinUnit == null) {
                        pinyin[index] = "null";  // 非汉字字符，如标点符号
                        continue;
                    } else {
                        pinyin[index] = formatCenterUnit(pinyinUnit[0].substring(0, pinyinUnit[0].length() - 1)) +
                                pinyinUnit[0].charAt(pinyinUnit[0].length() - 1);  // 带音调且长度固定为7个字符长度,,拼音居中,末尾优先
                        Log.e("pinyin", pinyin[index]);
                    }
                } catch (BadHanyuPinyinOutputFormatCombination badHanyuPinyinOutputFormatCombination) {
                    badHanyuPinyinOutputFormatCombination.printStackTrace();
                }

            }
            return pinyin;
        } else {
            return null;
        }
    }

    // 每个拼音单元长度以7个字符长度为标准,拼音居中,末尾优先
    private static String formatCenterUnit(String unit) {
        String result = unit;
        switch(unit.length()) {
            case 1:
                result = "   " + result + "   ";
                break;
            case 2:
                result = "  " + result + "   ";
                break;
            case 3:
                result = "  " + result + "  ";
                break;
            case 4:
                result = " " + result + "  ";
                break;
            case 5:
                result = " " + result + " ";
                break;
            case 6:
                result = result + " ";
                break;
        }
        return result;
    }

    public static String[] getFormatHanzi(String hanzi) {
        if (hanzi != null && hanzi.length() > 0) {
            char[] c = hanzi.toCharArray();
            String[] result = new String[c.length];
            for (int index = 0; index < c.length; index++) {
                result[index] = c[index] + "";
            }
            return result;
        } else {
            return null;
        }
    }

    /**
     * 判断是否为汉字
     * @param string
     * @return
     */
    public static boolean isChinese(String string) {
        char n = 0;
        if(string == null || string.length()==0)
            return false;
        for (int i = 0; i < string.length(); i++) {
            n = string.charAt(i);
            if (!isChinese(n)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isChinese(char c) {
        return c >= 0x4E00 && c <= 0x9FA5;// 根据字节码判断
    }
    /**
     * 1.判断字符串是否仅为数字:
     * @param str
     * @return
     */
    public static boolean isNumeric(String str) {
        for (int i = str.length(); --i >= 0; ) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
