package com.neusoft.qiangzi.search.data;

import android.app.Application;

import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class NewWordViewModel extends AndroidViewModel {

    private NewWordRepository newWordRepository;

    public NewWordViewModel(@NonNull Application application) {
        super(Objects.requireNonNull(application));
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
//    public void setSearchFilter(String chinese, String pinyin){
//        newWordRepository.setSearchFilter(chinese,pinyin);
//    }
    public void setSearchFilter(String s){
        newWordRepository.setSearchFilter(s);
    }
}
