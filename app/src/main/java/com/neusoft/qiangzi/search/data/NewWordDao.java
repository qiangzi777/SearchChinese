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

    @Query("SELECT * FROM newword ORDER BY add_time DESC")
    LiveData<List<NewWord>> getAll();

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

    @Query("SELECT * FROM newword WHERE pinyin_en LIKE :pinyin || '%' ORDER BY pinyin ASC")
    LiveData<List<NewWord>> findByPinyin(String pinyin);

    @Query("SELECT * FROM newword WHERE chinese LIKE :chinese || '%' " +
            "AND pinyin_en LIKE :pinyin || '%' ORDER BY pinyin ASC")
    LiveData<List<NewWord>> searchByWordLiveData(String chinese, String pinyin);

}
