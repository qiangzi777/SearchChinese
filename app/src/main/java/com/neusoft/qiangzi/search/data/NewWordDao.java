package com.neusoft.qiangzi.search.data;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface NewWordDao {

    @Query("SELECT * FROM newword ORDER BY add_time ASC")
    LiveData<List<NewWord>> getAllOrderByAddTimeAsc();
    @Query("SELECT * FROM newword ORDER BY add_time DESC")
    LiveData<List<NewWord>> getAllOrderByAddTimeDesc();
    @Query("SELECT * FROM newword ORDER BY pinyin_en ASC")
    LiveData<List<NewWord>> getAllOrderByPinyinAsc();
    @Query("SELECT * FROM newword ORDER BY pinyin_en DESC")
    LiveData<List<NewWord>> getAllOrderByPinyinDesc();
    @Query("SELECT * FROM newword ORDER BY counter ASC")
    LiveData<List<NewWord>> getAllOrderByCounterAsc();
    @Query("SELECT * FROM newword ORDER BY counter DESC")
    LiveData<List<NewWord>> getAllOrderByCounterDesc();

    @Insert
    void insert(NewWord... words);

    @Update
    void update(NewWord... words);

    @Delete
    void delete(NewWord... words);

    @Query("DELETE FROM newword")
    void deleteAll();

    @Query("SELECT * FROM newword WHERE chinese=:chinese LIMIT 1")
    NewWord findByChinese(String chinese);

    @Query("SELECT * FROM newword WHERE pinyin_en LIKE :pinyin || '%' ORDER BY pinyin_en ASC")
    LiveData<List<NewWord>> findByPinyin(String pinyin);

    @Query("SELECT * FROM newword WHERE chinese LIKE :chinese || '%' " +
            "AND pinyin_en LIKE :pinyin || '%' ORDER BY pinyin ASC")
    LiveData<List<NewWord>> searchByWordLiveData(String chinese, String pinyin);

}
