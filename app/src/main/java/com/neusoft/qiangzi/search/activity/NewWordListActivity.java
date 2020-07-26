package com.neusoft.qiangzi.search.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;

import com.google.android.material.snackbar.Snackbar;
import com.neusoft.qiangzi.search.R;
import com.neusoft.qiangzi.search.data.NewWord;
import com.neusoft.qiangzi.search.data.NewWordRepository;
import com.neusoft.qiangzi.search.data.NewWordViewModel;

import java.lang.reflect.Method;
import java.util.List;

import static com.neusoft.qiangzi.search.activity.RecyclerViewAdapter.LIST_DISPLAY_TYPE_CARD;
import static com.neusoft.qiangzi.search.activity.RecyclerViewAdapter.LIST_DISPLAY_TYPE_MAX;
import static com.neusoft.qiangzi.search.activity.RecyclerViewAdapter.LIST_DISPLAY_TYPE_NORMAL;

public class NewWordListActivity extends AppCompatActivity {

    private static final String TAG = "NewWordListActivity";
    RecyclerView recyclerView;
    NewWordViewModel viewModel;
    RecyclerViewAdapter recyclerViewAdapter;
    RecyclerViewAdapter recyclerViewAdapterNormal;
    RecyclerViewAdapter recyclerViewAdapterCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_word_list);

        setTitle(getString(R.string.newWordListActivityTitle));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = findViewById(R.id.recyclerView);
        viewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new NewWordViewModel(getApplication());
            }
        }).get(NewWordViewModel.class);
        SharedPreferences shp = getSharedPreferences(getString(R.string.shp_settings_name),MODE_PRIVATE);
        int dspType = shp.getInt(getString(R.string.shp_display_type), LIST_DISPLAY_TYPE_NORMAL);
        NewWordRepository.ORDER_TYPE orderType = NewWordRepository.ORDER_TYPE.values()[shp.getInt(getString(R.string.shp_order_type), 0)];
        recyclerViewAdapterNormal = new RecyclerViewAdapter(LIST_DISPLAY_TYPE_NORMAL,viewModel);
        recyclerViewAdapterCard = new RecyclerViewAdapter(LIST_DISPLAY_TYPE_CARD,viewModel);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        setRecyclerViewAdapter(dspType);

        viewModel.getAllNewWords().observe(this, new Observer<List<NewWord>>() {
            @Override
            public void onChanged(List<NewWord> newWords) {
                Log.d(TAG, "getAllNewWords:onChanged");
                updateRecyclerViewData(newWords);
            }
        });
        viewModel.setOrderFilter(orderType);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                final NewWord word = recyclerViewAdapter.getDataByPosition(viewHolder.getAdapterPosition());
                viewModel.deleteNewWords(word);
                Snackbar.make(NewWordListActivity.this.findViewById(R.id.newWordListLayout),"删除了一个生字",Snackbar.LENGTH_LONG)
                        .setAction("撤销", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                viewModel.insertNewWords(word);
                            }
                        })
                        .show();
            }
        }).attachToRecyclerView(recyclerView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.new_word_list_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.menu_search_new_word);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewModel.getAllNewWords().removeObservers(NewWordListActivity.this);
                viewModel.searchBy().observe(NewWordListActivity.this, new Observer<List<NewWord>>() {
                    @Override
                    public void onChanged(List<NewWord> newWords) {
                        Log.d(TAG, "searchBy:onChanged");
                        updateRecyclerViewData(newWords);
                    }
                });
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                viewModel.setSearchFilter(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                viewModel.setSearchFilter(s);
                return false;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                Log.d(TAG, "searchView:onClose");
                viewModel.searchBy().removeObservers(NewWordListActivity.this);
                viewModel.getAllNewWords().observe(NewWordListActivity.this, new Observer<List<NewWord>>() {
                    @Override
                    public void onChanged(List<NewWord> newWords) {
                        Log.d(TAG, "getAllNewWords:onChanged");
                        updateRecyclerViewData(newWords);
                    }
                });
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private void updateRecyclerViewData(List<NewWord> newWords){
        recyclerViewAdapterNormal.setAllNewWords(newWords);
        recyclerViewAdapterNormal.notifyDataSetChanged();
        recyclerViewAdapterCard.setAllNewWords(newWords);
        recyclerViewAdapterCard.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:// back button
                this.finish();
                return true;
            case R.id.menu_delete_selected:
                break;
            case R.id.menu_delete_all:
                new AlertDialog.Builder(this)
                        .setTitle("删除确认")
                        .setMessage("确定要清空生词本吗？")
                        .setNegativeButton("取消", null)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                viewModel.deleteAllNewWords();
                            }
                        })
                        .show();
                break;
            case R.id.menu_sort_add_time:
                setNewWordsOrderType(NewWordRepository.ORDER_TYPE.ORDER_BY_ADD_TIME_DESC);
                break;
            case R.id.menu_sort_pinyin:
                setNewWordsOrderType(NewWordRepository.ORDER_TYPE.ORDER_BY_PINYIN_ASC);
                break;
            case R.id.menu_sort_count:
                setNewWordsOrderType(NewWordRepository.ORDER_TYPE.ORDER_BY_COUNTER_DESC);
                break;
            case R.id.menu_list_display_type:
                SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shp_settings_name),MODE_PRIVATE);
                int dspType = sharedPreferences.getInt(getString(R.string.shp_display_type), LIST_DISPLAY_TYPE_NORMAL);
                dspType = (dspType+1)%LIST_DISPLAY_TYPE_MAX;
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(getString(R.string.shp_display_type), dspType);
                editor.apply();
                setRecyclerViewAdapter(dspType);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setNewWordsOrderType(NewWordRepository.ORDER_TYPE orderType){
        viewModel.setOrderFilter(orderType);
        SharedPreferences shp = getSharedPreferences(getString(R.string.shp_settings_name), MODE_PRIVATE);
        SharedPreferences.Editor editor = shp.edit();
        editor.putInt(getString(R.string.shp_order_type), orderType.ordinal());
        editor.apply();
    }
    private void setRecyclerViewAdapter(int dspType) {
        if(dspType== LIST_DISPLAY_TYPE_NORMAL){
            Log.d(TAG, "setRecyclerViewAdapter: normal type");
            recyclerViewAdapter = recyclerViewAdapterNormal;
        }else if(dspType==LIST_DISPLAY_TYPE_CARD){
            Log.d(TAG, "setRecyclerViewAdapter: card type");
            recyclerViewAdapter = recyclerViewAdapterCard;
        }
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    /**
     * 为了显示menu的图标icon
     * @param featureId
     * @param menu
     * @return
     */
    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (menu != null) {
            if (menu.getClass().getSimpleName().equalsIgnoreCase("MenuBuilder")) {
                try {
                    Method method = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    method.setAccessible(true);
                    method.invoke(menu, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return super.onMenuOpened(featureId, menu);
    }
}
