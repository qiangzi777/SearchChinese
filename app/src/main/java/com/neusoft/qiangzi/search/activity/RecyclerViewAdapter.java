package com.neusoft.qiangzi.search.activity;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.neusoft.qiangzi.search.R;
import com.neusoft.qiangzi.search.data.NewWord;
import com.neusoft.qiangzi.search.data.NewWordViewModel;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

    public static final int LIST_DISPLAY_TYPE_NORMAL = 0;
    public static final int LIST_DISPLAY_TYPE_CARD = 1;
    public static final int LIST_DISPLAY_TYPE_MAX = 2;

    private static final String TAG = "RecyclerViewAdapter";
    private List<NewWord> allNewWords = new ArrayList<>();
    private NewWordViewModel newWordViewModel;
    private int list_display_type = LIST_DISPLAY_TYPE_NORMAL;

    public RecyclerViewAdapter(int dspType, NewWordViewModel newWordViewModel) {
        this.newWordViewModel = newWordViewModel;
        this.list_display_type = dspType;
    }
    public void setAllNewWords(List<NewWord> allNewWords) {
        this.allNewWords = allNewWords;
    }
    public NewWord getDataByPosition(int position){
        return allNewWords.get(position);
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = null;
        if(list_display_type==LIST_DISPLAY_TYPE_NORMAL)
            view = inflater.inflate(R.layout.item_new_word,parent,false);
        else if(list_display_type == LIST_DISPLAY_TYPE_CARD){
            view = inflater.inflate(R.layout.item_new_word_card,parent,false);
        }
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
//        Log.d(TAG, "onBindViewHolder: pos="+position);
        holder.bindData(position);
    }

    @Override
    public int getItemCount() {
        return allNewWords.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        private NewWord word;
        TextView textViewPosition,textViewChinese,textViewPinyin,textViewUpdateTime;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewPosition = itemView.findViewById(R.id.textViewPosition);
            textViewChinese = itemView.findViewById(R.id.textViewChinese);
            textViewPinyin = itemView.findViewById(R.id.textViewPinyin);
            textViewUpdateTime = itemView.findViewById(R.id.textViewUpdateTime);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    /* 打开百度汉语 */
                    Intent i = new Intent(view.getContext(),WebSearchActivity.class);
                    i.putExtra("word", word.chinese);
                    view.getContext().startActivity(i);
                    /* 跟新点击条目数据 */
                    word.counter++;
                    word.setUpdateTimeToNow();
                    newWordViewModel.updateNewWords(word);
                }
            });
        }
        public void bindData(int position) {
            this.word = allNewWords.get(position);
            textViewPosition.setText(String.valueOf(position + 1));
            textViewChinese.setText(word.chinese);
            textViewPinyin.setText(word.pinyin);
            textViewUpdateTime.setText(word.getUpdateTimeAgo());
        }
    }
}
