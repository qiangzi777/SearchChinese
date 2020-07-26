package com.neusoft.qiangzi.search.data;

import android.app.Application;

import java.util.List;

import androidx.annotation.NonNull;
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

    public LiveData<List<NewWord>> searchBy(){
        return newWordRepository.searchBy();
    }

    public void setOrderFilter(NewWordRepository.ORDER_TYPE orderType){
        newWordRepository.setOrderFilter(orderType);
    }
    public void setSearchFilter(String chinese, String pinyin){
        newWordRepository.setSearchFilter(chinese,pinyin);
    }
    public void setSearchFilter(String s){
        newWordRepository.setSearchFilter(s);
    }
}
