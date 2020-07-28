package com.neusoft.qiangzi.search.data;

import java.util.Date;

import androidx.room.TypeConverter;

@SuppressWarnings("ALL")
class Converters {
    @TypeConverter
    public Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public Long dateToTimestamp(Date date) {
        if (date == null) {
            return null;
        } else {
            return date.getTime();
        }
    }

    @TypeConverter
    public String[] stringToArray(String zuci){
        if(zuci==null) return null;
        return zuci.split(",");
    }

    @TypeConverter
    public String arrayToString(String[] arr){
        if(arr == null) return null;
        StringBuilder sb = new StringBuilder();
        for (String str:arr
             ) {
            sb.append(str);
            sb.append(",");
        }
        return sb.toString();
    }

}
