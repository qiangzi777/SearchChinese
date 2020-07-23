package com.neusoft.qiangzi.search.data;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class NewWord {
    @PrimaryKey(autoGenerate = true)
    public int id;
    @ColumnInfo
    public String chinese;
    @ColumnInfo
    public String pinyin;
    @ColumnInfo(name = "pinyin_en")
    public String pinyinEnglish;
    @ColumnInfo
    public int counter;
//    @ColumnInfo
//    public int userid;
    @ColumnInfo(name = "add_time")
    public Date addTime;
    @ColumnInfo(name = "update_time")
    public Date updateTime;

    public NewWord() {
        this.counter = 1;
        this.addTime = new Date(System.currentTimeMillis());
        this.updateTime = new Date(System.currentTimeMillis());
    }
    public void setUpdateTimeToNow(){
        this.updateTime = new Date(System.currentTimeMillis());
    }

    public String getUpdateTimeString(){
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd", Locale.CHINA);
        return format.format(updateTime);
    }
    public String getUpdateTimeAgo(){
        return timeAgo(updateTime);
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
}
