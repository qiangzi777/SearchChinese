package com.neusoft.qiangzi.search.data;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface KeyWordDao {
    @Query("SELECT * FROM keyword WHERE type='zuci' ORDER BY update_time DESC")
    LiveData<List<KeyWord>> getAllZuciDesc();
    @Query("SELECT * FROM keyword WHERE type='baike' ORDER BY update_time DESC")
    LiveData<List<KeyWord>> getAllBaikeDesc();
    @Query("SELECT * FROM keyword ORDER BY update_time DESC")
    LiveData<List<KeyWord>> getAllDesc();

    @Insert
    void insert(KeyWord... words);

    @Update
    void update(KeyWord... words);

    @Delete
    void delete(KeyWord... words);

    @Query("DELETE FROM keyword")
    void deleteAll();

    @Query("DELETE FROM keyword WHERE type=:type")
    void deleteByType(String type);

    @Query("DELETE FROM keyword WHERE keyWord=:keyword AND type=:type")
    void deleteByKeywordAndType(String keyword, String type);

    @Query("SELECT * FROM keyword WHERE keyWord=:word AND type=:type LIMIT 1")
    KeyWord findByKeyword(String word, String type);
}
