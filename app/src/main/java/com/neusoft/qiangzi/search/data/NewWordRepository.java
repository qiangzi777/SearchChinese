package com.neusoft.qiangzi.search.data;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import com.neusoft.qiangzi.search.activity.ChineseResultActivity;
import com.neusoft.qiangzi.search.pinyin.PinyinUtils;

import java.util.List;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

public class NewWordRepository {

    private NewWordDao newWordDao;
    private LiveData<List<NewWord>> allNewWords;
    private Context context;

    public NewWordRepository(Context context) {
        this.context = context;
        NewWordDatabase database = NewWordDatabase.getInstance(context);
        newWordDao = database.getNewWordDao();
        allNewWords = newWordDao.getAll();
    }

    public LiveData<List<NewWord>> getAllNewWords() {
        return allNewWords;
    }
    public void insertNewWords(NewWord... words){
        new InsertNewWordsAsyncTask(newWordDao).execute(words);
    }

    public void updateNewWords(NewWord... words){
        new UpdateNewWordsAsyncTask(newWordDao).execute(words);
    }

    public void deleteNewWords(NewWord... words){
        new DeleteNewWordsAsyncTask(newWordDao).execute(words);
    }

    public void deleteAllNewWords(){
        new DeleteAllNewWordsAsyncTask(newWordDao).execute();
    }

    LiveData<List<NewWord>> findNewWordByChinese(String chinese){
        return newWordDao.findByChinese(chinese);
    }

    public LiveData<List<NewWord>> findNewWordsByChinese(String chinese){
        return newWordDao.findByChinese(chinese);
    }

    public LiveData<List<NewWord>> findNewWordsByPinyin(String pinyin){
        return newWordDao.findByPinyin(pinyin);
    }

    public void saveNewWord(final String chinese, final String pinyin){
        findNewWordsByChinese(chinese).observe((LifecycleOwner) this.context, new Observer<List<NewWord>>() {
            @Override
            public void onChanged(List<NewWord> newWords) {
                NewWord newWord = (newWords==null || newWords.size()==0)?null:newWords.get(0);
                if(newWord == null){
                    newWord = new NewWord();
                    newWord.chinese = chinese;
                    newWord.pinyin = pinyin;
                    newWord.pinyinEnglish = PinyinUtils.toneStringToString(newWord.pinyin);
                    insertNewWords(newWord);
                }else{
                    newWord.counter++;
                    newWord.setUpdateTimeToNow();
                    updateNewWords(newWord);
                }
            }
        });
    }
    static class InsertNewWordsAsyncTask extends AsyncTask<NewWord,Void,Void>{
        private NewWordDao newWordDao;

        InsertNewWordsAsyncTask(NewWordDao newWordDao) {
            this.newWordDao = newWordDao;
        }

        @Override
        protected Void doInBackground(NewWord... words) {
            this.newWordDao.insert(words);
            return null;
        }
    }

    static class UpdateNewWordsAsyncTask extends AsyncTask<NewWord,Void,Void>{
        private NewWordDao newWordDao;

        UpdateNewWordsAsyncTask(NewWordDao newWordDao) {
            this.newWordDao = newWordDao;
        }

        @Override
        protected Void doInBackground(NewWord... words) {
            this.newWordDao.update(words);
            return null;
        }
    }

    static class DeleteNewWordsAsyncTask extends AsyncTask<NewWord,Void,Void>{
        private NewWordDao newWordDao;

        DeleteNewWordsAsyncTask(NewWordDao newWordDao) {
            this.newWordDao = newWordDao;
        }

        @Override
        protected Void doInBackground(NewWord... words) {
            this.newWordDao.delete(words);
            return null;
        }
    }

    static class DeleteAllNewWordsAsyncTask extends AsyncTask<Void,Void,Void>{
        private NewWordDao newWordDao;

        DeleteAllNewWordsAsyncTask(NewWordDao newWordDao) {
            this.newWordDao = newWordDao;
        }

        @Override
        protected Void doInBackground(Void... v) {
            this.newWordDao.deleteAll();
            return null;
        }
    }
}
