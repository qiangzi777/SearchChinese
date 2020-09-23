package com.neusoft.qiangzi.search.data;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.neusoft.qiangzi.search.pinyin.PinyinUtils;

import java.util.Date;
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
    public enum KEYWORD_TYPE{
        ZUCI,
        BAIKE,
    }
    private static final String TAG = "NewWordRepository";
    private NewWordDao newWordDao;
    private KeyWordDao keyWordDao;
    private LiveData<List<NewWord>> allNewWords;
    private LiveData<List<NewWord>> searchByNewWords;
    private NewWord filterNewWord;
    private MutableLiveData<NewWord> filterLiveData;
    private MutableLiveData<ORDER_TYPE> filterOrderType;
    private LiveData<List<KeyWord>> allKeyWords;
    private MutableLiveData<String> keyWordType;

    public NewWordRepository(Context context) {
        NewWordDatabase database = NewWordDatabase.getInstance(context);
        newWordDao = database.getNewWordDao();
        keyWordDao = database.getKeyWordDao();
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
        keyWordType = new MutableLiveData<>("zuci");
        allKeyWords = Transformations.switchMap(keyWordType,
                new Function<String, LiveData<List<KeyWord>>>() {
                    @Override
                    public LiveData<List<KeyWord>> apply(String input) {
                        if(input.equals("baike"))
                            return keyWordDao.getAllBaikeDesc();
                        else if(input.equals("zuci"))
                            return keyWordDao.getAllZuciDesc();
                        else
                            return keyWordDao.getAllZuciDesc();
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

    void deleteKeyWords(KeyWord... words){
        new DeleteKeyWordsAsyncTask(keyWordDao).execute(words);
    }

    public void deleteKeyWordsByType(KEYWORD_TYPE type){
        switch (type) {

            case ZUCI:
                new DeleteKeyWordsByTypeAsyncTask(keyWordDao).execute("zuci");
                break;
            case BAIKE:
                new DeleteKeyWordsByTypeAsyncTask(keyWordDao).execute("baike");
                break;
        }

    }
    void deleteAllNewWords(){
        new DeleteAllNewWordsAsyncTask(newWordDao).execute();
    }

    void deleteAllKeyWords(){
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

    public void setKeyWordType(KEYWORD_TYPE type) {
        switch (type) {
            case ZUCI:
                keyWordType.setValue("zuci");
                break;
            case BAIKE:
                keyWordType.setValue("baike");
                break;
        }
    }

    public LiveData<List<KeyWord>> getAllKeyWords() {
        return allKeyWords;
    }

    public void saveNewWord(String chinese){
        new SaveNewWordAsyncTask(newWordDao).execute(chinese);
    }

    public void saveKeyWord(String keyword, KEYWORD_TYPE type) {
        switch (type) {
            case ZUCI:
                new SaveKeyWordAsyncTask(keyWordDao).execute(keyword, "zuci");
                break;
            case BAIKE:
                new SaveKeyWordAsyncTask(keyWordDao).execute(keyword, "baike");
                break;
        }
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

    static class DeleteKeyWordsAsyncTask extends AsyncTask<KeyWord,Void,Void>{
        private KeyWordDao keyWordDao;

        DeleteKeyWordsAsyncTask(KeyWordDao keyWordDao) {
            this.keyWordDao = keyWordDao;
        }

        @Override
        protected Void doInBackground(KeyWord... words) {
            this.keyWordDao.delete(words);
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
    static class DeleteAllKeyWordsAsyncTask extends AsyncTask<Void,Void,Void>{
        private KeyWordDao keyWordDao;

        DeleteAllKeyWordsAsyncTask(KeyWordDao keyWordDao) {
            this.keyWordDao = keyWordDao;
        }

        @Override
        protected Void doInBackground(Void... v) {
            this.keyWordDao.deleteAll();
            return null;
        }
    }
    static class DeleteKeyWordsByTypeAsyncTask extends AsyncTask<String,Void,Void>{
        private KeyWordDao keyWordDao;

        DeleteKeyWordsByTypeAsyncTask(KeyWordDao keyWordDao) {
            this.keyWordDao = keyWordDao;
        }

        @Override
        protected Void doInBackground(String... v) {
            String type = v[0];
            this.keyWordDao.deleteByType(type);
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
                newWord.pinyin = PinyinUtils.getSpellArray(chinese)[0];
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
    static class SaveKeyWordAsyncTask extends AsyncTask<String,Void,Void>{
        private KeyWordDao keyWordDao;

        SaveKeyWordAsyncTask(KeyWordDao keyWordDao) {
            this.keyWordDao = keyWordDao;
        }

        @Override
        protected Void doInBackground(String... str) {
            String keyword = str[0];
            String type = str[1];
            KeyWord keyWord = keyWordDao.findByKeyword(keyword, type);
            if(keyWord == null){
                keyWord = new KeyWord();
                keyWord.keyWord = keyword;
                keyWord.type = type;
                keyWordDao.insert(keyWord);
            }else{
                keyWord.counter++;
                keyWord.updateTime =  new Date(System.currentTimeMillis());
                keyWordDao.update(keyWord);
            }
            Log.d(TAG, "SaveKeyWordAsyncTask:doInBackground: word="+keyWord.toString());
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
