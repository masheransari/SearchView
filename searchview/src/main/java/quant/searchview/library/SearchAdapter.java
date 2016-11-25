package quant.searchview.library;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
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


public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ResultViewHolder> implements Filterable {
    private static final String TAG = "SearchAdapter";
    private final List<SearchItem> queryList;
    private final List<SearchItem> historyList;
    private final List<SearchItem> originalList;
    private final SearchHistoryTable historyDatabase;
    private List<OnItemClickListener> mItemClickListeners;
    private String queryKey;
    private SearchFilter filter;

    public SearchAdapter(Context context,List<SearchItem> suggestionsList) {
        this(context,suggestionsList,SearchHistoryTable.DEFAULT_KEY);
    }

    public SearchAdapter(Context context, List<SearchItem> items,String key) {
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

    public void filter(CharSequence text,Runnable completeAction){
        getFilter().filter(text,completeAction);
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

        if (!TextUtils.isEmpty(queryKey)&&itemTextLower.contains(queryKey) && !queryKey.isEmpty()) {
            SpannableString s = new SpannableString(itemText);
            s.setSpan(new ForegroundColorSpan(SearchView.getTextHighlightColor()), itemTextLower.indexOf(queryKey), itemTextLower.indexOf(queryKey) + queryKey.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
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


    @Deprecated
    public void setOnItemClickListener(OnItemClickListener mItemClickListener) {
        addOnItemClickListener(mItemClickListener);
    }

    public void addOnItemClickListener(OnItemClickListener listener) {
        addOnItemClickListener(listener, null);
    }

    private void addOnItemClickListener(OnItemClickListener listener, Integer position) {
        if (mItemClickListeners == null)
            mItemClickListeners = new ArrayList<>();
        if (position == null)
            mItemClickListeners.add(listener);
        else
            mItemClickListeners.add(position, listener);
    }

    public void swapItems(List<SearchItem> data) {
        if(null==data) return;
        if (queryList.isEmpty()) {
            queryList.addAll(data);
            notifyDataSetChanged();
        } else {
            int previousSize = queryList.size();
            int nextSize = data.size();
            queryList.clear();
            queryList.addAll(data);
            if (previousSize == nextSize && nextSize != 0)
                notifyItemRangeChanged(0, previousSize);
            else if (previousSize > nextSize) {
                if (nextSize == 0)
                    notifyItemRangeRemoved(0, previousSize);
                else {
                    notifyItemRangeChanged(0, nextSize);
                    notifyItemRangeRemoved(nextSize - 1, previousSize);
                }
            } else {
                notifyItemRangeChanged(0, previousSize);
                notifyItemRangeInserted(previousSize, nextSize - previousSize);
            }
        }
    }



    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public class ResultViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final ImageView icon_left;
        final TextView text;

        public ResultViewHolder(View view) {
            super(view);
            icon_left = (ImageView) view.findViewById(R.id.imageView_item_icon_left);
            text = (TextView) view.findViewById(R.id.textView_item_text);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListeners != null) {
                for (OnItemClickListener listener : mItemClickListeners)
                    listener.onItemClick(v, getLayoutPosition());
            }
        }
    }

    class SearchFilter extends Filter {
        private Runnable completeAction;

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            queryKey=!TextUtils.isEmpty(constraint)?constraint.toString().toLowerCase(Locale.getDefault()):"";
            if (!TextUtils.isEmpty(queryKey)) {
                List<SearchItem> resultItems=new ArrayList<>();
                resultItems.addAll(queryItems(historyList,queryKey));
                resultItems.addAll(queryItems(queryList,queryKey));
                filterResults.count=resultItems.size();
                filterResults.values=resultItems;
            }
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if(!TextUtils.isEmpty(constraint)) {
                originalList.clear();
                if(null!=results.values) {
                    List<SearchItem> resultItems= (List<SearchItem>) results.values;
                    originalList.addAll(resultItems);
                }
                notifyItemRangeInserted(0,getItemCount());
            } else {
                //展示所有
                originalList.clear();
                originalList.addAll(historyList);
                notifyItemRangeInserted(0,getItemCount());
            }
            Log.e(TAG,"publishResults:"+getItemCount());
            if(null!=completeAction){
                completeAction.run();
            }
        }

        private List<SearchItem> queryItems(List<SearchItem> items,String key) {
            List<SearchItem> resultItems=new ArrayList<>();
            if(null!=items) {
                for (SearchItem item : items) {
                    if(!TextUtils.isEmpty(item.text)) {
                        if (item.text.toLowerCase(Locale.getDefault()).contains(key)) {
                            resultItems.add(item);
                        }
                    }
                }
            }
            return resultItems;
        }

        public void filter(CharSequence text,Runnable completeAction){
            super.filter(text);
            this.completeAction=completeAction;
        }
    };

}