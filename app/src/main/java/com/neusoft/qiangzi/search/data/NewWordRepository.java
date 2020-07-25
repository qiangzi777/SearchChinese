package com.neusoft.qiangzi.search.data;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.neusoft.qiangzi.search.pinyin.PinyinUtils;

import java.util.List;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;

public class NewWordRepository {

    private static final String TAG = "NewWordRepository";
    private NewWordDao newWordDao;
    private LiveData<List<NewWord>> allNewWords;
    private LiveData<List<NewWord>> searchByNewWords;
    private NewWord filterNewWord;
    private MutableLiveData<NewWord> filterLiveData;
    private Context context;

    public NewWordRepository(Context context) {
        this.context = context;
        NewWordDatabase database = NewWordDatabase.getInstance(context);
        newWordDao = database.getNewWordDao();
        allNewWords = newWordDao.getAll();
        filterNewWord = new NewWord();
        filterLiveData = new MutableLiveData(filterNewWord);
        searchByNewWords = Transformations.switchMap(filterLiveData,
                new Function<NewWord, LiveData<List<NewWord>>>() {
                    @Override
                    public LiveData<List<NewWord>> apply(NewWord v) {
                        return newWordDao.searchByWordLiveData(v.chinese, v.pinyin);
                    }
                });
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

    public LiveData<List<NewWord>> searchBy(){
        return searchByNewWords;
    }

    public void setFilter(String chinese, String pinyin){
        filterNewWord.chinese = chinese;
        filterNewWord.pinyin = pinyin;
    }
    public void setFilter(String s){
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
    public void insertOrUpdateNewWord1(final String chinese){
        Log.d(TAG, "insertOrUpdateNewWord: chinese="+chinese);
        setFilter(chinese, "");
        searchByNewWords.observe((LifecycleOwner) this.context, new Observer<List<NewWord>>() {
            @Override
            public void onChanged(List<NewWord> newWords) {
                searchByNewWords.removeObservers((LifecycleOwner) context);
                NewWord newWord = (newWords==null || newWords.size()==0)?null:newWords.get(0);
                if(newWord == null){
                    newWord = new NewWord();
                    newWord.chinese = chinese;
                    newWord.pinyin = PinyinUtils.getSpellString(chinese)[0];
                    newWord.pinyinEnglish = PinyinUtils.toneStringToString(newWord.pinyin);
                    insertNewWords(newWord);
                }else{
                    newWord.counter++;
                    newWord.setUpdateTimeToNow();
                    updateNewWords(newWord);
                }
                Log.d(TAG, "insertOrUpdateNewWord:onChanged: word="+newWord.toString());
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
                newWord.pinyinEnglish = PinyinUtils.toneStringToString(newWord.pinyin);
                newWordDao.insert(newWord);
            }else{
                newWord.counter++;
                newWord.setUpdateTimeToNow();
                newWordDao.update(newWord);
            }
            Log.d(TAG, "SaveNewWordAsyncTask:doInBackground: word="+newWord.toString());
            return null;
        }
    }
}
