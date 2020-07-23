package com.neusoft.qiangzi.search.activity;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.neusoft.qiangzi.search.R;
import com.neusoft.qiangzi.search.data.NewWord;
import com.neusoft.qiangzi.search.data.NewWordDao;
import com.neusoft.qiangzi.search.data.NewWordDatabase;
import com.neusoft.qiangzi.search.data.NewWordRepository;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

    public void setAllNewWords(List<NewWord> allNewWords) {
        this.allNewWords = allNewWords;
    }

    private List<NewWord> allNewWords = new ArrayList<>();

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_new_word,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final NewWord word = allNewWords.get(position);
        holder.textViewPosition.setText(String.valueOf(position + 1));
        holder.textViewChinese.setText(word.chinese);
        holder.textViewPinyin.setText(word.pinyin);
        holder.textViewUpdateTime.setText(word.getUpdateTimeAgo());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /* 打开百度汉语 */
                Intent i = new Intent(view.getContext(),WebSearchActivity.class);
                i.putExtra("word", word.chinese);
                view.getContext().startActivity(i);
                /* 跟新点击条目数据 */
                NewWordRepository repository = new NewWordRepository(view.getContext());
                word.counter++;
                word.setUpdateTimeToNow();
                repository.updateNewWords(word);
            }
        });
    }

    @Override
    public int getItemCount() {
        return allNewWords.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{

        TextView textViewPosition,textViewChinese,textViewPinyin,textViewUpdateTime;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewPosition = itemView.findViewById(R.id.textViewPosition);
            textViewChinese = itemView.findViewById(R.id.textViewChinese);
            textViewPinyin = itemView.findViewById(R.id.textViewPinyin);
            textViewUpdateTime = itemView.findViewById(R.id.textViewUpdateTime);

        }
    }
}
