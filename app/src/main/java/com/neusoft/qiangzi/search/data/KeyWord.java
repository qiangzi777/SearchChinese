package com.neusoft.qiangzi.search.data;

import java.util.Date;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class KeyWord {
    @PrimaryKey(autoGenerate = true)
    public int id;
    @ColumnInfo
    public String keyword;
    @ColumnInfo
    public String type;
    @ColumnInfo
    public int counter;
    @ColumnInfo(name = "update_time")
    public Date updateTime;

    KeyWord() {
        this.counter = 1;
        this.updateTime = new Date(System.currentTimeMillis());
    }
}
