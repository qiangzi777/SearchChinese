package com.neusoft.qiangzi.search.data;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class NewWord {
    private static final int ZUCI_MAX_LENGTH = 8;

    @PrimaryKey(autoGenerate = true)
    public int id;
    @ColumnInfo
    public String chinese;
    @ColumnInfo
    public String pinyin;
    @ColumnInfo(name = "pinyin_en")
    public String pinyin_en;
    @ColumnInfo
    public int counter;
//    @ColumnInfo
//    public int userid;
    @ColumnInfo(name = "add_time")
    public Date addTime;
    @ColumnInfo(name = "zuci")
    String[] zuci;

    NewWord() {
        this.counter = 1;
        this.addTime = new Date(System.currentTimeMillis());
    }

    void appendZuci(String zuci) {
        if(zuci==null || zuci.isEmpty())return;
        if(this.zuci ==null) {
            this.zuci = new String[1];
            this.zuci[0] = zuci;
        }else {
            for (String str:this.zuci
                 ) {
                if(str.equals(zuci))return;
            }
            String[] arr = new String[this.zuci.length+1];
            System.arraycopy(this.zuci, 0, arr, 0, this.zuci.length);
            arr[this.zuci.length] = zuci;
            this.zuci = arr;
        }
    }

    public String getZuciString(){
        if(zuci==null)return "还没看过组词";
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<zuci.length;i++) {
            sb.append(zuci[i]);
            if(i<zuci.length-1)
                sb.append(",");
        }
        return sb.toString();
    }
    private String getZuciEllipsis(){
        if(zuci==null)return "";
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<zuci.length;i++) {
            sb.append(zuci[i]);
            if(i<zuci.length-1)
                sb.append(",");
            if(sb.length() > ZUCI_MAX_LENGTH){
                return sb.substring(0, ZUCI_MAX_LENGTH-1)+"...";
            }
        }
        return sb.toString();
    }

    public String getAddTimeAgo(){
        return timeAgo(addTime);
    }
    private String timeAgo(Date createdTime) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd", Locale.CHINA);
        if (createdTime != null) {
            long agoTimeInMin = (new Date(System.currentTimeMillis()).getTime() - createdTime.getTime()) / 1000 / 60;
            //如果在当前时间以前一分钟内
            if (agoTimeInMin <= 1) {
                return "刚刚";
            } else if (agoTimeInMin <= 60) {
                //如果传入的参数时间在当前时间以前10分钟之内
                return agoTimeInMin + "分钟前";
            } else if (agoTimeInMin <= 60 * 24) {
                return agoTimeInMin / 60 + "小时前";
            } else if (agoTimeInMin <= 60 * 24 * 2) {
                return agoTimeInMin / (60 * 24) + "天前";
            } else {
                return format.format(createdTime);
            }
        } else {
            return format.format(new Date(0));
        }
    }

    @Override
    public String toString(){
        return chinese +
                " " + pinyin +
                " " + pinyin_en +
                " " + getZuciEllipsis() +
                " c=" + counter;
    }
}
