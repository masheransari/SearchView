package quant.searchview.library.observer;

import android.support.v7.widget.RecyclerView;

import quant.searchview.library.SearchView;

/**
 * Created by cz on 11/28/16.
 */

public class MyAdapterDataObserver extends RecyclerView.AdapterDataObserver {
    private final SearchView searchView;

    public MyAdapterDataObserver(SearchView searchView) {
        this.searchView = searchView;
    }

    @Override
    public void onChanged() {
        super.onChanged();
        searchView.showSuggestions();
    }

    @Override
    public void onItemRangeChanged(int positionStart, int itemCount) {
        super.onItemRangeChanged(positionStart, itemCount);
        searchView.showSuggestions();
    }

    @Override
    public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
        super.onItemRangeChanged(positionStart, itemCount, payload);
    }

    @Override
    public void onItemRangeInserted(int positionStart, int itemCount) {
        super.onItemRangeInserted(positionStart, itemCount);
        searchView.showSuggestions();
    }

    @Override
    public void onItemRangeRemoved(int positionStart, int itemCount) {
        super.onItemRangeRemoved(positionStart, itemCount);
        searchView.showSuggestions();
    }

    @Override
    public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
        super.onItemRangeMoved(fromPosition, toPosition, itemCount);
        searchView.showSuggestions();
    }
}
