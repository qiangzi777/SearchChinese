package com.neusoft.qiangzi.search.data;

import android.app.Application;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

public class NewWordViewModel extends ViewModel {

    private NewWordRepository newWordRepository;

//    public NewWordViewModel() {
//    }

    public NewWordViewModel(@NonNull Application application) {
        newWordRepository = new NewWordRepository(application);
    }
    public LiveData<List<NewWord>> getAllNewWords() {
        return newWordRepository.getAllNewWords();
    }
    public void insertNewWords(NewWord... words){
        newWordRepository.insertNewWords(words);
    }

    public void updateNewWords(NewWord... words){
        newWordRepository.updateNewWords(words);
    }

    public void deleteNewWords(NewWord... words){
        newWordRepository.deleteNewWords(words);
    }

    public void deleteAllNewWords(){
        newWordRepository.deleteAllNewWords();
    }

    public LiveData<List<NewWord>> findNewWordByChinese(String chinese){
        return newWordRepository.findNewWordByChinese(chinese);
    }
    public LiveData<List<NewWord>> findNewWordsByPinyin(String pinyin){
        return newWordRepository.findNewWordsByPinyin(pinyin);
    }
}