package com.neusoft.qiangzi.search.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import com.neusoft.qiangzi.search.R;
import com.neusoft.qiangzi.search.data.NewWord;
import com.neusoft.qiangzi.search.data.NewWordViewModel;

import java.lang.reflect.Method;
import java.util.List;

public class NewWordListActivity extends AppCompatActivity {

    private static final String TAG = "NewWordListActivity";
    RecyclerView recyclerView;
    NewWordViewModel viewModel;
    RecyclerViewAdapter recyclerViewAdapter;

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
        recyclerViewAdapter = new RecyclerViewAdapter(viewModel);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(recyclerViewAdapter);

        viewModel.getAllNewWords().observe(this, new Observer<List<NewWord>>() {
            @Override
            public void onChanged(List<NewWord> newWords) {
                Log.d(TAG, "getAllNewWords:onChanged");
                updateRecyclerViewData(newWords);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.new_word_list_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.menu_search_new_word);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                viewModel.setFilter(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                viewModel.setFilter(s);
                return false;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                Log.d(TAG, "searchView:onClose");
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
        recyclerViewAdapter.setAllNewWords(newWords);
        recyclerViewAdapter.notifyDataSetChanged();
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
                viewModel.deleteAllNewWords();
                break;
            case R.id.menu_sort_add_time:
            case R.id.menu_sort_update_time:
            case R.id.menu_sort_count:
                Toast.makeText(this,"强子努力开发中，敬请期待。。。",Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
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
