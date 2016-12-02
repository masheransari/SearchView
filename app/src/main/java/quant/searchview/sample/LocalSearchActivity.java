package quant.searchview.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import quant.searchview.library.SearchAdapter;
import quant.searchview.library.SearchItem;
import quant.searchview.library.SearchView;

import static quant.searchview.sample.DataProvider.ITEMS;

/**
 * Created by czz on 2016/11/26.
 * 本地数据集搜索示例,用于检索一些固定数据集列
 */
public class LocalSearchActivity extends AppCompatActivity {
    private static final String LOCAL_KEY="local";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        toolbar.setTitle(R.string.local_search);
        setSupportActionBar(toolbar);
        final SearchView searchView = (SearchView) findViewById(R.id.search_view);
        List<SearchItem> suggestionsList = new ArrayList<>();
        for (int i = 0; i < ITEMS.length; i++) {
            suggestionsList.add(new SearchItem(ITEMS[i]));
        }
        final SearchAdapter searchAdapter = new SearchAdapter(this, suggestionsList,LOCAL_KEY,false);
        searchAdapter.setHistorySize(100);//设置历史条目最多展示个数,默认为5个,以最新使用为主
        searchView.showProgress();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //这里,返回true,会将query不为空的检测值,存入local_key内,当SearchAdapter不传入key时,会存入统一的默认key内,返回false则不会存入历史表内
                return true;
            }

            @Override
            public void onQueryTextChange(String newText) {
                //这里如果不动态检测,不必复写任何逻辑
            }
        });
        searchAdapter.addOnItemClickListener(new SearchAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                TextView textView = (TextView) view.findViewById(R.id.textView_item_text);
                String query = textView.getText().toString();
                Toast.makeText(LocalSearchActivity.this, "Click:"+query, Toast.LENGTH_SHORT).show();
            }
        });
        searchView.setAdapter(searchAdapter);
    }
}
