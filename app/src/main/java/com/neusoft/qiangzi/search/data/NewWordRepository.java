package com.neusoft.qiangzi.search.data;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.neusoft.qiangzi.search.pinyin.PinyinUtils;

import java.util.List;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

public class NewWordRepository {

    public enum ORDER_TYPE{
        ORDER_BY_ADD_TIME_ASC,
        ORDER_BY_ADD_TIME_DESC,
        ORDER_BY_PINYIN_ASC,
        ORDER_BY_PINYIN_DESC,
        ORDER_BY_COUNTER_ASC,
        ORDER_BY_COUNTER_DESC,
    }
    private static final String TAG = "NewWordRepository";
    private NewWordDao newWordDao;
    private LiveData<List<NewWord>> allNewWords;
    private LiveData<List<NewWord>> searchByNewWords;
    private NewWord filterNewWord;
    private MutableLiveData<NewWord> filterLiveData;
    private MutableLiveData<ORDER_TYPE> filterOrderType;

    public NewWordRepository(Context context) {
        NewWordDatabase database = NewWordDatabase.getInstance(context);
        newWordDao = database.getNewWordDao();
        filterOrderType = new MutableLiveData<>();
        allNewWords = Transformations.switchMap(filterOrderType,
                new Function<ORDER_TYPE, LiveData<List<NewWord>>>() {
                    @Override
                    public LiveData<List<NewWord>> apply(ORDER_TYPE input) {
                        switch (input){
                            case ORDER_BY_ADD_TIME_ASC:
                                return newWordDao.getAllOrderByAddTimeAsc();
                            case ORDER_BY_ADD_TIME_DESC:
                                return newWordDao.getAllOrderByAddTimeDesc();
                            default:
                            case ORDER_BY_PINYIN_ASC:
                                return newWordDao.getAllOrderByPinyinAsc();
                            case ORDER_BY_PINYIN_DESC:
                                return newWordDao.getAllOrderByPinyinDesc();
                            case ORDER_BY_COUNTER_ASC:
                                return newWordDao.getAllOrderByCounterAsc();
                            case ORDER_BY_COUNTER_DESC:
                                return newWordDao.getAllOrderByCounterDesc();
                        }
                    }
                });
        filterNewWord = new NewWord();
        filterLiveData = new MutableLiveData<>(filterNewWord);
        searchByNewWords = Transformations.switchMap(filterLiveData,
                new Function<NewWord, LiveData<List<NewWord>>>() {
                    @Override
                    public LiveData<List<NewWord>> apply(NewWord v) {
                        return newWordDao.searchByWordLiveData(v.chinese, v.pinyin);
                    }
                });
    }

    LiveData<List<NewWord>> getAllNewWords() {
        setOrderFilter(ORDER_TYPE.ORDER_BY_PINYIN_ASC);
        return allNewWords;
    }
    void insertNewWords(NewWord... words){
        new InsertNewWordsAsyncTask(newWordDao).execute(words);
    }

    void updateNewWords(NewWord... words){
        new UpdateNewWordsAsyncTask(newWordDao).execute(words);
    }

    void deleteNewWords(NewWord... words){
        new DeleteNewWordsAsyncTask(newWordDao).execute(words);
    }

    void deleteAllNewWords(){
        new DeleteAllNewWordsAsyncTask(newWordDao).execute();
    }

    LiveData<List<NewWord>> searchBy(){
        return searchByNewWords;
    }

    void setOrderFilter(ORDER_TYPE orderType){
        filterOrderType.setValue(orderType);
    }
    void setSearchFilter(String chinese, String pinyin){
        filterNewWord.chinese = chinese;
        filterNewWord.pinyin = pinyin;
    }
    void setSearchFilter(String s){
        Log.d(TAG, "setFilter: s="+s);
        if(PinyinUtils.isChinese(s)){
            filterNewWord.chinese = s;
            filterNewWord.pinyin = "";
        }else {
            filterNewWord.chinese = "";
            filterNewWord.pinyin = s;
        }
        filterLiveData.setValue(filterNewWord);
    }

    public void saveNewWord(String chinese){
        new SaveNewWordAsyncTask(newWordDao).execute(chinese);
    }
    public void appendZuci(String chinese, String zuci){
        new appendZuciAsyncTask(newWordDao).execute(chinese, zuci);
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

    static class SaveNewWordAsyncTask extends AsyncTask<String,Void,Void>{
        private NewWordDao newWordDao;

        SaveNewWordAsyncTask(NewWordDao newWordDao) {
            this.newWordDao = newWordDao;
        }

        @Override
        protected Void doInBackground(String... str) {
            String chinese = str[0];
            NewWord newWord = newWordDao.findByChinese(chinese);
            if(newWord == null){
                newWord = new NewWord();
                newWord.chinese = chinese;
                newWord.pinyin = PinyinUtils.getSpellString(chinese)[0];
                newWord.pinyin_en = PinyinUtils.toneStringToString(newWord.pinyin);
                newWordDao.insert(newWord);
            }else{
                newWord.counter++;
                newWordDao.update(newWord);
            }
            Log.d(TAG, "SaveNewWordAsyncTask:doInBackground: word="+newWord.toString());
            return null;
        }
    }

    static class appendZuciAsyncTask extends AsyncTask<String,Void,Void>{
        private NewWordDao newWordDao;

        appendZuciAsyncTask(NewWordDao newWordDao) {
            this.newWordDao = newWordDao;
        }

        @Override
        protected Void doInBackground(String... str) {
            String chinese = str[0];
            String zuci = str[1];
            NewWord newWord = newWordDao.findByChinese(chinese);
            if(newWord == null){
                return null;
            }else{
                newWord.appendZuci(zuci);
                newWordDao.update(newWord);
            }
            Log.d(TAG, "appendZuciAsyncTask:doInBackground: word="+newWord.toString());
            return null;
        }
    }
}
