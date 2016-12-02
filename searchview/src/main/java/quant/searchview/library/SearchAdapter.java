package quant.searchview.library;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.annotation.IdRes;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.SparseArray;
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


public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.BaseViewHolder> implements Filterable {
    private static final String TAG = "SearchAdapter";
    private final List<SearchItem> queryList;
    private final List<SearchItem> historyList;
    private final List<SearchItem> originalList;
    private final SearchHistoryTable historyDatabase;
    private List<OnItemClickListener> mItemClickListeners;
    public final String searchKey;
    private String queryWord;
    private SearchFilter filter;
    private boolean showHistory;

    public SearchAdapter(Context context,List<SearchItem> suggestionsList) {
        this(context,suggestionsList,SearchHistoryTable.DEFAULT_KEY,true);
    }

    public SearchAdapter(Context context, List<SearchItem> items,String key){
        this(context,items,key,true);
    }

    public SearchAdapter(Context context, List<SearchItem> items,String key,boolean showHistory) {
        this.searchKey=key;
        this.showHistory=showHistory;
        this.queryList =new ArrayList<>();
        this.historyList =new ArrayList<>();
        this.originalList=new ArrayList<>();
        this.historyDatabase = new SearchHistoryTable(context);
        List<SearchItem> allItems = historyDatabase.getAllItems(key);
        if (showHistory&&!allItems.isEmpty()) {
            this.historyList.addAll(allItems);
        }
        if(null!= items) {
            this.queryList.addAll(items);
        }
        updateHistoryItems();
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
    public BaseViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;
        if(SearchItem.CLEAR_ITEM==viewType){
            view = inflater.inflate(R.layout.clear_history_item,parent,false);
        } else {
            view = inflater.inflate(R.layout.search_item, parent, false);
        }
        return new BaseViewHolder(view,viewType);
    }

    @Override
    public void onBindViewHolder(BaseViewHolder viewHolder, int position) {
        SearchItem item = originalList.get(position);
        if(SearchItem.CLEAR_ITEM!=item.type){
            initSearchItem(viewHolder, item);
        } else {
            TextView textView= (TextView) viewHolder.view(R.id.textView_item_text);
            textView.setTypeface(SearchView.getTextFont());
            textView.setTextSize(SearchView.getIconColor());
            textView.setTextColor(SearchView.getTextColor());
        }
    }

    private void initSearchItem(BaseViewHolder viewHolder, SearchItem item) {
        ImageView imageView = (ImageView) viewHolder.view(R.id.imageView_item_icon_left);
        if(SearchItem.HISTORY_ITEM==item.type) {
            imageView.setImageResource(R.drawable.ic_history_black_24dp);
        } else {
            imageView.setImageResource(R.drawable.ic_search_black_24dp);
        }
        imageView.setColorFilter(SearchView.getIconColor(), PorterDuff.Mode.SRC_IN);

        TextView textView= (TextView) viewHolder.view(R.id.textView_item_text);
        textView.setTypeface(SearchView.getTextFont());
        textView.setTextSize(SearchView.getIconColor());
        textView.setTextColor(SearchView.getTextColor());

        String itemText = item.text;
        String itemTextLower = itemText.toLowerCase(Locale.getDefault());

        if (!TextUtils.isEmpty(queryWord)&&itemTextLower.contains(queryWord) && !queryWord.isEmpty()) {
            SpannableString s = new SpannableString(itemText);
            s.setSpan(new ForegroundColorSpan(SearchView.getTextHighlightColor()), itemTextLower.indexOf(queryWord), itemTextLower.indexOf(queryWord) + queryWord.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            textView.setText(s, TextView.BufferType.SPANNABLE);
        } else {
            textView.setText(item.text);
        }
    }

    @Override
    public int getItemCount() {
        return originalList.size();
    }

    @Override
    public int getItemViewType(int position) {
        SearchItem item = getItem(position);
        return item.type;
    }


    public void addOnItemClickListener(OnItemClickListener listener) {
        if (mItemClickListeners == null){
            mItemClickListeners = new ArrayList<>();
        }
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
        if(showHistory){
            int previousSize = originalList.size();
            originalList.clear();
            originalList.addAll(historyList);
            if(!originalList.isEmpty()){
                originalList.add(new SearchItem(null,SearchItem.CLEAR_ITEM));
                notifyItemRangeInserted(0,getItemCount());
            } else if(0<previousSize){
                notifyItemRangeRemoved(0,previousSize);
            }
        }
    }


    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    protected List<SearchItem> queryItems(List<SearchItem> items,CharSequence key) {
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
            int previousSize=originalList.size();
            if(!TextUtils.isEmpty(constraint)) {
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
            } else if(showHistory){
                //展示所有
                updateHistoryItems();
            } else if(0<previousSize){
                originalList.clear();
                notifyItemRangeRemoved(0,previousSize);
            }
        }

    }


    public class BaseViewHolder extends RecyclerView.ViewHolder {

        private final SparseArray<View> cacheViews;

        public BaseViewHolder(View itemView,int type) {
            super(itemView);
            this.cacheViews=new SparseArray<>();
            cacheView(itemView);
            if(SearchItem.CLEAR_ITEM!=type){
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mItemClickListeners != null) {
                            int size = mItemClickListeners.size();
                            for(int i=size-1;i>=0;i--){
                                mItemClickListeners.get(i).onItemClick(v, getLayoutPosition());
                            }
                        }
                    }
                });
            } else {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int size = originalList.size();
                        historyList.clear();
                        originalList.clear();
                        historyDatabase.clearDatabase(searchKey);
                        notifyItemRangeRemoved(0,size);
                    }
                });
            }
        }

        private void cacheView(View itemView) {
            if(itemView instanceof ViewGroup){
                ViewGroup layout = (ViewGroup) itemView;
                this.cacheViews.append(layout.getId(),layout);
                for(int i=0;i<layout.getChildCount();i++){
                    View childView = layout.getChildAt(i);
                    if(childView instanceof ViewGroup){
                        cacheView(childView);
                    } else {
                        this.cacheViews.append(childView.getId(),childView);
                    }
                }
            } else {
                this.cacheViews.append(itemView.getId(),itemView);
            }
        }
        public View view(@IdRes int id){
            return this.cacheViews.get(id);
        }
    }

}