package com.neusoft.qiangzi.search.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

//singleton
@Database(entities = {NewWord.class, KeyWord.class},version = 4,exportSchema = false)
@TypeConverters(Converters.class)
public abstract class NewWordDatabase extends RoomDatabase {
    private static NewWordDatabase instance = null;
    public abstract NewWordDao getNewWordDao();
    public abstract KeyWordDao getKeyWordDao();

    static synchronized NewWordDatabase getInstance(Context context) {
        if(instance==null){
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    NewWordDatabase.class, "new_words")
//                    .allowMainThreadQueries()
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
//                    .fallbackToDestructiveMigration()//数据库更新时删除数据重新创建
                    .build();
        }
        return instance;
    }
//    private static final Migration MIGRATION_1_2 = new Migration(1,2) {
//        @Override
//        public void migrate(@NonNull SupportSQLiteDatabase database) {
//            database.execSQL("ALTER TABLE newword ADD COLUMN pinyin_en TEXT");
//            Cursor c = database.query("SELECT * FROM newword");
//            while (c.moveToNext()){
//                int id = c.getInt(c.getColumnIndex("id"));
//                String pinyin = c.getString(c.getColumnIndex("pinyin"));
//                String pinyinEn = PinyinUtils.toneStringToString(pinyin);
//                ContentValues values = new ContentValues();
//                values.put("pinyin_en", pinyinEn);
//                database.update("newword",0,values,
//                        "id=?", new String[]{String.valueOf(id)});
//            }
//        }
//    };
    private static final Migration MIGRATION_2_3 = new Migration(2,3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE newword_temp (" +
                    "id  INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    "chinese TEXT, pinyin TEXT, pinyin_en TEXT, " +
                    "counter INTEGER NOT NULL DEFAULT 1," +
                    "add_time INTEGER, zuci TEXT)");
            database.execSQL("INSERT INTO newword_temp (chinese,pinyin,pinyin_en,counter,add_time)" +
                    "SELECT chinese,pinyin,pinyin_en,counter,add_time FROM newword");
            database.execSQL("DROP TABLE newword");
            database.execSQL("ALTER TABLE newword_temp RENAME TO newword");
        }
    };
    private static final Migration MIGRATION_3_4 = new Migration(3,4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE keyword (" +
                    "id  INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    "type TEXT," +
                    "keyword TEXT," +
                    "counter INTEGER NOT NULL," +
                    "update_time INTEGER)");
        }
    };
}
