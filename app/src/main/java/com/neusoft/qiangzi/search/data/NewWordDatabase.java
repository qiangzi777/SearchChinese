package com.neusoft.qiangzi.search.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.neusoft.qiangzi.search.pinyin.PinyinUtils;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

//singleton
@Database(entities = {NewWord.class},version = 2,exportSchema = false)
@TypeConverters(Converters.class)
public abstract class NewWordDatabase extends RoomDatabase {
    private static NewWordDatabase instance = null;
    public abstract NewWordDao getNewWordDao();

    public static synchronized NewWordDatabase getInstance(Context context) {
        if(instance==null){
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    NewWordDatabase.class, "new_words")
//                    .allowMainThreadQueries()
                    .addMigrations(MIGRATION_1_2)
                    .build();
        }
        return instance;
    }
    static final Migration MIGRATION_1_2 = new Migration(1,2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE newword ADD COLUMN pinyin_en TEXT");
            Cursor c = database.query("SELECT * FROM newword");
            while (c.moveToNext()){
                int id = c.getInt(c.getColumnIndex("id"));
                String pinyin = c.getString(c.getColumnIndex("pinyin"));
                String pinyinEn = PinyinUtils.toneStringToString(pinyin);
                ContentValues values = new ContentValues();
                values.put("pinyin_en", pinyinEn);
                database.update("newword",0,values,
                        "id=?", new String[]{String.valueOf(id)});
            }
        }
    };
}
