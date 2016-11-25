package quant.searchview.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import quant.searchview.library.SearchAdapter;
import quant.searchview.library.SearchHistoryTable;
import quant.searchview.library.SearchItem;
import quant.searchview.library.SearchView;

public class MainActivity extends AppCompatActivity {
    private SearchHistoryTable mHistoryDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar= (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        mHistoryDatabase = new SearchHistoryTable(this);
        final SearchView searchView= (SearchView) findViewById(R.id.search_view);
        List<SearchItem> suggestionsList = new ArrayList<>();
        for(int i=0;i<100;i++){
            suggestionsList.add(new SearchItem("search"+(i+1)));
        }

        SearchAdapter searchAdapter = new SearchAdapter(this, suggestionsList);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mHistoryDatabase.addItem(new SearchItem(query));
                searchView.close(false);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        searchAdapter.addOnItemClickListener(new SearchAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                TextView textView = (TextView) view.findViewById(R.id.textView_item_text);
                String query = textView.getText().toString();
                mHistoryDatabase.addItem(new SearchItem(query));
                searchView.close(false);
            }
        });
        searchView.setAdapter(searchAdapter);
    }
}
