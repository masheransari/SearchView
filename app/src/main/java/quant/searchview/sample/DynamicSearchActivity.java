package quant.searchview.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
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
 * 动态搜索示例,用于做在线搜索数据,或者一些自定义逻辑搜索
 */
public class DynamicSearchActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        toolbar.setTitle(R.string.local_search);
        setSupportActionBar(toolbar);
        final SearchView searchView = (SearchView) findViewById(R.id.search_view);
        //动态搜索,这里不必传入任何检测列表
        final SearchAdapter searchAdapter = new SearchAdapter(this, null);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //这里,返回true,会将query不为空的检测值,存入local_key内,当SearchAdapter不传入key时,会存入统一的默认key内,返回false则不会存入历史表内
                return true;
            }

            @Override
            public void onQueryTextChange(String newText) {
                //动态检测,复写业务逻辑
                searchAdapter.swapItems(queryTextItems(newText),newText);
            }
        });
        searchAdapter.addOnItemClickListener(new SearchAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                TextView textView = (TextView) view.findViewById(R.id.textView_item_text);
                String query = textView.getText().toString();
                Toast.makeText(DynamicSearchActivity.this, "Click:"+query, Toast.LENGTH_SHORT).show();
            }
        });
        searchView.setAdapter(searchAdapter);
    }

    private List<SearchItem> queryTextItems(String newText) {
        List<SearchItem> resultItems=new ArrayList<>();
        for(int i=0;i<DataProvider.ITEMS.length;i++){
            if(TextUtils.isEmpty(newText)||DataProvider.ITEMS[i].contains(newText)){
                resultItems.add(new SearchItem(DataProvider.ITEMS[i]));
            }
        }
        return resultItems;
    }
}
