package quant.searchview.library;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import quant.searchview.library.db.SearchHistoryTable;


public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ResultViewHolder> implements Filterable {
    private static final String TAG = "SearchAdapter";
    private final List<SearchItem> queryList;
    private final List<SearchItem> historyList;
    private final List<SearchItem> originalList;
    private final SearchHistoryTable historyDatabase;
    private List<OnItemClickListener> mItemClickListeners;
    public final String searchKey;
    private String queryWord;
    private SearchFilter filter;

    public SearchAdapter(Context context,List<SearchItem> suggestionsList) {
        this(context,suggestionsList,SearchHistoryTable.DEFAULT_KEY);
    }

    public SearchAdapter(Context context, List<SearchItem> items,String key) {
        searchKey=key;
        queryList =new ArrayList<>();
        historyList =new ArrayList<>();
        originalList=new ArrayList<>();
        historyDatabase = new SearchHistoryTable(context);
        List<SearchItem> allItems = historyDatabase.getAllItems(key);
        if (!allItems.isEmpty()) {
            historyList.addAll(allItems);
            originalList.addAll(allItems);
        }
        if(null!= items) {
            queryList.addAll(items);
        }
    }

    @Override
    public SearchFilter getFilter() {
        if(null==filter) {
            filter = new SearchFilter();
        }
        return filter;
    }

    public void setHistorySize(int historySize) {
        historyDatabase.setHistorySize(historySize);
    }

    @Override
    public ResultViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View view = inflater.inflate(R.layout.search_item, parent, false);
        return new ResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ResultViewHolder viewHolder, int position) {
        SearchItem item = originalList.get(position);
        if(SearchItem.HISTORY_ITEM==item.type) {
            viewHolder.icon_left.setImageResource(R.drawable.ic_history_black_24dp);
        } else {
            viewHolder.icon_left.setImageResource(R.drawable.ic_search_black_24dp);
        }
        viewHolder.icon_left.setColorFilter(SearchView.getIconColor(), PorterDuff.Mode.SRC_IN);
        viewHolder.text.setTypeface(SearchView.getTextFont());
        viewHolder.text.setTextSize(SearchView.getIconColor());
        viewHolder.text.setTextColor(SearchView.getTextColor());

        String itemText = item.text;
        String itemTextLower = itemText.toLowerCase(Locale.getDefault());

        if (!TextUtils.isEmpty(queryWord)&&itemTextLower.contains(queryWord) && !queryWord.isEmpty()) {
            SpannableString s = new SpannableString(itemText);
            s.setSpan(new ForegroundColorSpan(SearchView.getTextHighlightColor()), itemTextLower.indexOf(queryWord), itemTextLower.indexOf(queryWord) + queryWord.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            viewHolder.text.setText(s, TextView.BufferType.SPANNABLE);
        } else {
            viewHolder.text.setText(item.text);
        }
    }

    @Override
    public int getItemCount() {
        return originalList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }


    public void addOnItemClickListener(OnItemClickListener listener) {
        if (mItemClickListeners == null)
            mItemClickListeners = new ArrayList<>();
        else
            mItemClickListeners.add(listener);
    }

    public void swapItems(List<SearchItem> newItems,String word) {
        getFilter().publishItems(word,newItems);
    }

    public SearchItem getItem(int position) {
        return this.originalList.get(position);
    }

    public void insertHistoryItem(SearchItem item){
        historyList.remove(item);
        SearchItem newItem=new SearchItem(item.text,SearchItem.HISTORY_ITEM);
        historyList.add(0,newItem);
        historyDatabase.addItem(newItem,searchKey);
        updateHistoryItems();
    }

    private void updateHistoryItems(){
        int previousSize = originalList.size();
        originalList.clear();
        originalList.addAll(historyList);
        if(!originalList.isEmpty()){
            notifyItemRangeInserted(0,getItemCount());
        } else if(0<previousSize){
            notifyItemRangeRemoved(0,previousSize);
        }
    }


    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    private List<SearchItem> queryItems(List<SearchItem> items,CharSequence key) {
        List<SearchItem> resultItems=new ArrayList<>();
        if(null!=items) {
            for (SearchItem item : items) {
                if(!TextUtils.isEmpty(item.text)) {
                    if (TextUtils.isEmpty(key)||item.text.toLowerCase(Locale.getDefault()).contains(key.toString())) {
                        resultItems.add(item);
                    }
                }
            }
        }
        return resultItems;
    }

    class SearchFilter  extends Filter{
        @Override
        protected FilterResults performFiltering(CharSequence word) {
            FilterResults filterResults=new FilterResults();
            List<SearchItem> resultItems=new ArrayList<>();
            resultItems.addAll(queryItems(historyList, word));
            if (!TextUtils.isEmpty(word)) {
                resultItems.addAll(queryItems(queryList, word));
            }
            filterResults.count=resultItems.size();
            filterResults.values=resultItems;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            publishItems(constraint, (List<SearchItem>) results.values);
        }

        private void publishItems(CharSequence constraint, List<SearchItem> resultItems) {
            queryWord=!TextUtils.isEmpty(constraint)?constraint.toString():null;
            if(!TextUtils.isEmpty(constraint)) {
                int previousSize=originalList.size();
                if(null!=resultItems) {
                    originalList.clear();
                    int nextSize = resultItems.size();
                    originalList.addAll(resultItems);
                    if (previousSize == nextSize && nextSize != 0)
                        notifyItemRangeChanged(0, previousSize);
                    else if (previousSize > nextSize) {
                        if (nextSize == 0)
                            notifyItemRangeRemoved(0, previousSize);
                        else {
                            notifyItemRangeChanged(0, nextSize);
                            notifyItemRangeRemoved(nextSize, previousSize-nextSize);
                        }
                    } else {
                        if(0<previousSize){
                            notifyItemRangeChanged(0, previousSize);
                        }
                        notifyItemRangeInserted(previousSize, nextSize - previousSize);
                    }
                }
            } else {
                //展示所有
                updateHistoryItems();
            }
        }

    }


    public class ResultViewHolder extends RecyclerView.ViewHolder {

        final ImageView icon_left;
        final TextView text;

        public ResultViewHolder(View view) {
            super(view);
            icon_left = (ImageView) view.findViewById(R.id.imageView_item_icon_left);
            text = (TextView) view.findViewById(R.id.textView_item_text);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemClickListeners != null) {
                        for (OnItemClickListener listener : mItemClickListeners)
                            listener.onItemClick(v, getLayoutPosition());
                    }
                }
            });
        }

    }

}