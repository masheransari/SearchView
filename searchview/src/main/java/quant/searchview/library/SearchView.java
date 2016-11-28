package quant.searchview.library;

import android.animation.LayoutTransition;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.lang.reflect.Field;

import quant.searchview.library.observer.MyAdapterDataObserver;

public class SearchView extends FrameLayout implements View.OnClickListener,TextWatcher {

    private static final String TAG = "SearchView";
    private static int mIconColor = Color.BLACK;
    private static int mTextColor = Color.BLACK;
    private static int mTextHighlightColor = Color.BLACK;
    private static Typeface mTextFont = Typeface.DEFAULT;

    private SearchDrawable mSearchArrow = null;
    private SearchAdapter mAdapter = null;
    private OnQueryTextListener mOnQueryChangeListener = null;
    private OnOpenCloseListener mOnOpenCloseListener = null;
    private OnMenuClickListener mOnMenuClickListener = null;
    private RecyclerView mRecyclerView;
    private View mShadowView;
    private View mDividerView;
    private LinearLayout container;
    private SearchEditText mSearchEditText;
    private ProgressBar mProgressBar;
    private ImageView mBackImageView;
    private ImageView mEmptyImageView;
    private LinearLayout mLinearLayout;
    private SearchDivider searchDivider;
    private MyAdapterDataObserver adapterDataObserver;
    private int backGroundColor;
    private int backgroundCorners;
    private int mAnimationDuration;
    private float mIsSearchArrowHamburgerState = SearchDrawable.STATE_ANIM_NONE;
    private boolean mArrow = false;
    private boolean mIsSearchOpen = false;
    private boolean mShouldClearOnOpen = false;
    private boolean mShouldClearOnClose = false;
    private boolean mShouldHideOnKeyboardClose = true;
    private boolean filterLocal;

    // ---------------------------------------------------------------------------------------------
    public SearchView(Context context) {
        this(context, null,R.attr.SearchViewAttr);
    }

    public SearchView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,R.attr.SearchViewAttr);
    }

    public SearchView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
        initStyle(context,attrs,defStyleAttr);
    }

    // ---------------------------------------------------------------------------------------------
    public static int getIconColor() {
        return mIconColor;
    }

    public void setIconColor(@ColorInt int color) {
        mIconColor = color;
        ColorFilter colorFilter = new PorterDuffColorFilter(mIconColor, PorterDuff.Mode.SRC_IN);

        mBackImageView.setColorFilter(colorFilter);
        mEmptyImageView.setColorFilter(colorFilter);
    }

    public static int getTextColor() {
        return mTextColor;
    }

    public void setTextColor(@ColorInt int color) {
        mTextColor = color;
        mSearchEditText.setTextColor(mTextColor);
    }

    public static int getTextHighlightColor() {
        return mTextHighlightColor;
    }

    public void setTextHighlightColor(@ColorInt int color) {
        mTextHighlightColor = color;
    }

    public static Typeface getTextFont() {
        return mTextFont;
    }

    // ---------------------------------------------------------------------------------------------
    private void initView(Context context) {
        LayoutInflater.from(context).inflate((R.layout.search_view), this, true);

        mLinearLayout = (LinearLayout) findViewById(R.id.linearLayout);
        container = (LinearLayout) findViewById(R.id.search_container);

        // TODO
        searchDivider=new SearchDivider(context);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView_result);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setVisibility(View.GONE);
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setLayoutTransition(getRecyclerViewLayoutTransition());
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                     mRecyclerView.setLayoutTransition(null);
                } else {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        mRecyclerView.setLayoutTransition(getRecyclerViewLayoutTransition());
                    }
                }
            }
        });

        mShadowView = findViewById(R.id.view_shadow);
        mShadowView.setOnClickListener(this);

        mDividerView = findViewById(R.id.view_divider);
        mDividerView.setVisibility(View.GONE);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.GONE);

        mEmptyImageView = (ImageView) findViewById(R.id.imageView_clear);
        mEmptyImageView.setImageResource(R.drawable.ic_clear_black_24dp);
        mEmptyImageView.setOnClickListener(this);
        mEmptyImageView.setVisibility(View.GONE);

        mSearchEditText = (SearchEditText) findViewById(R.id.searchEditText_input);
        mSearchEditText.setSearchView(this);
        mSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mEmptyImageView.setVisibility(TextUtils.isEmpty(charSequence)?View.GONE:View.VISIBLE);
                onQueryText(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        mSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                onSubmitQuery();
                return true;
            }
        });
        mSearchEditText.clearFocus();
        mSearchEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Log.e(TAG,"onFocusChange:"+hasFocus);
                if (hasFocus) {
                    addFocus();
                } else {
                    removeFocus();
                }
            }
        });
        mSearchArrow = new SearchDrawable(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,2,getResources().getDisplayMetrics()));
        mBackImageView = (ImageView) findViewById(R.id.imageView_arrow_back);
        mBackImageView.setImageDrawable(mSearchArrow);
        mBackImageView.setOnClickListener(this);
    }

    private void initStyle(Context context,AttributeSet attrs, int defStyleAttr) {
        final TypedArray attr = context.obtainStyledAttributes(attrs, R.styleable.SearchView, defStyleAttr, R.style.DefaultSearchStyle);
        setSearchLayoutHeight((int) attr.getDimension(R.styleable.SearchView_search_layout_height, 0));
        setSearchLayoutMargin((int) attr.getDimension(R.styleable.SearchView_search_layout_margin, 0));
        setIconColor(attr.getColor(R.styleable.SearchView_search_icon_color, 0));
        setSearchBackgroundColor(attr.getColor(R.styleable.SearchView_search_backgroundColor,Color.WHITE));
        setSearchBackgroundCorners((int) attr.getDimension(R.styleable.SearchView_search_backgroundCorners,0));
        setTextColor(attr.getColor(R.styleable.SearchView_search_text_color, 0));
        setTextHighlightColor(attr.getColor(R.styleable.SearchView_search_text_highlight_color, 0));
        setTextSize(attr.getDimensionPixelSize(R.styleable.SearchView_search_text_size,0));
        setHint(attr.getString(R.styleable.SearchView_search_hint));
        setHintColor(attr.getColor(R.styleable.SearchView_search_hint_color, 0));
        setListDivider(attr.getBoolean(R.styleable.SearchView_search_listDivider,true));
        setUnderDividerDrawable(attr.getDrawable(R.styleable.SearchView_search_underDividerDrawable));
        setAnimationDuration(attr.getInteger(R.styleable.SearchView_search_animation_duration, 0));
        setShadowColor(attr.getColor(R.styleable.SearchView_search_shadow_color, 0));
        setShouldClearOnOpen(attr.getBoolean(R.styleable.SearchView_search_clear_on_open, false));
        setShouldClearOnClose(attr.getBoolean(R.styleable.SearchView_search_clear_on_close, true));
        setShouldHideOnKeyboardClose(attr.getBoolean(R.styleable.SearchView_search_hide_on_keyboard_close, true));
        setCursorDrawable(attr.getResourceId(R.styleable.SearchView_search_cursor_drawable, 0));
        setFilterLocal(attr.getBoolean(R.styleable.SearchView_search_filter_local,false));
        attr.recycle();
    }

    private void setSearchBackgroundColor(int color) {
        this.backGroundColor=color;
        setBackgroundCompat(mLinearLayout,getGradientDrawable(backgroundCorners, backgroundCorners, backgroundCorners, backgroundCorners, color));
        setBackgroundCompat(mRecyclerView,getGradientDrawable(0, 0, backgroundCorners, backgroundCorners, color));
    }

    private void setSearchBackgroundCorners(int corner) {
        this.backgroundCorners=corner;
        setBackgroundCompat(mLinearLayout,getGradientDrawable(backgroundCorners, backgroundCorners, backgroundCorners, backgroundCorners, backGroundColor));
        setBackgroundCompat(mRecyclerView,getGradientDrawable(0, 0, backgroundCorners, backgroundCorners, backGroundColor));
    }

    private void setBackgroundCompat(View view,Drawable drawable) {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN){
            view.setBackground(drawable);
        } else {
            view.setBackgroundDrawable(drawable);
        }
    }

    private GradientDrawable getGradientDrawable(int ltCorner,int rtCorner,int rbCorner,int lbCorner,int color){
        GradientDrawable drawable=new GradientDrawable();
        drawable.setCornerRadii(new float[]{ltCorner, ltCorner,
                rtCorner, rtCorner,
                rbCorner, rbCorner,
                lbCorner, lbCorner});
        drawable.setColor(color);
        return drawable;
    }

    // ---------------------------------------------------------------------------------------------
    public void setTextOnly(CharSequence text) {
        mSearchEditText.setText(text);
    }

    public CharSequence getTextOnly() {
        return mSearchEditText.getText();
    }

    public void setTextOnly(@StringRes int text) {
        mSearchEditText.setText(text);
    }

    public void setQuery(CharSequence query, boolean submit) {
        setQueryWithoutSubmitting(query);
        if (submit && !TextUtils.isEmpty(query)) {
            onSubmitQuery();
        }
    }

    public void setQuery(@StringRes int query, boolean submit) {
        setQuery(String.valueOf(query), submit);
    }

    public CharSequence getQuery() {
        return mSearchEditText.getText();
    }

    public void setHint(@Nullable CharSequence hint) {
        mSearchEditText.setHint(hint);
    }

    @Nullable
    public CharSequence getHint() {
        return mSearchEditText.getHint();
    }

    public void setHint(@StringRes int hint) {
        mSearchEditText.setHint(hint);
    }

    public int getImeOptions() {
        return mSearchEditText.getImeOptions();
    }

    public void setImeOptions(int imeOptions) {
        mSearchEditText.setImeOptions(imeOptions);
    }

    public int getInputType() {
        return mSearchEditText.getInputType();
    }

    public void setInputType(int inputType) {
        mSearchEditText.setInputType(inputType);
    }

    public RecyclerView.Adapter getAdapter() {
        return mRecyclerView.getAdapter();
    }

    public void setUnderDividerDrawable(Drawable drawable) {
        if(null!=drawable){
            setBackgroundCompat(mDividerView,drawable);
        }
    }

    private void setFilterLocal(boolean filterLocal) {
        this.filterLocal=filterLocal;
    }

    public void setAdapter(final SearchAdapter adapter) {
        if(null==adapterDataObserver){
            adapterDataObserver=new MyAdapterDataObserver(this);
        } else if(null!=mAdapter&&null!=adapterDataObserver){
            mAdapter.unregisterAdapterDataObserver(adapterDataObserver);
        }
        adapter.registerAdapterDataObserver(adapterDataObserver);
        mRecyclerView.setAdapter(mAdapter = adapter);
        adapter.addOnItemClickListener(new SearchAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                mAdapter.insertHistoryItem(adapter.getItem(position));
                close(false);
            }
        });
    }

    public boolean getShouldClearOnClose() {
        return mShouldClearOnClose;
    }

    public void setShouldClearOnClose(boolean shouldClearOnClose) {
        mShouldClearOnClose = shouldClearOnClose;
    }

    public boolean getShouldClearOnOpen() {
        return mShouldClearOnOpen;
    }

    public void setShouldClearOnOpen(boolean shouldClearOnOpen) {
        mShouldClearOnOpen = shouldClearOnOpen;
    }

    public boolean getShouldHideOnKeyboardClose() {
        return mShouldHideOnKeyboardClose;
    }

    public void setShouldHideOnKeyboardClose(boolean shouldHideOnKeyboardClose) {
        mShouldHideOnKeyboardClose = shouldHideOnKeyboardClose;
    }

    public void setSearchLayoutHeight(int height) {
        ViewGroup.LayoutParams params = mLinearLayout.getLayoutParams();
        params.height = height;
        params.width = LinearLayout.LayoutParams.MATCH_PARENT;
        mLinearLayout.requestLayout();
    }

    public void setSearchLayoutMargin(int value) {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(value,value,value,value);
        container.setLayoutParams(params);
    }


    public void setTextSize(float size) {
        mSearchEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX,size);
    }

    public void setHintColor(@ColorInt int color) {
        mSearchEditText.setHintTextColor(color);
    }

    public void setListDivider(boolean divider) {
        if (divider) {
            mRecyclerView.addItemDecoration(searchDivider);
        } else {
            mRecyclerView.removeItemDecoration(searchDivider);
        }
    }

    public void setAnimationDuration(int animationDuration) {
        mAnimationDuration = animationDuration;
    }

    public void setShadowColor(@ColorInt int color) {
        mShadowView.setBackgroundColor(color);
    }

    @Override
    public void setBackgroundColor(@ColorInt int color) {
        container.setBackgroundColor(color);
    }


    // http://stackoverflow.com/questions/11554078/set-textcursordrawable-programatically
    public void setCursorDrawable(@DrawableRes int drawable) {
        try {
            Field f = TextView.class.getDeclaredField("mCursorDrawableRes");
            f.setAccessible(true);
            try {
                f.set(mSearchEditText, drawable);
            } catch (IllegalAccessException e1) {
                e1.printStackTrace();
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    // ---------------------------------------------------------------------------------------------
    public void showSuggestions() {
        if (mAdapter != null && mAdapter.getItemCount() > 0) { // ||
            if(View.GONE==mRecyclerView.getVisibility()){
                mDividerView.setVisibility(View.VISIBLE);
                SearchAnimator.fadeIn(mRecyclerView, mAnimationDuration);
            } else {
                mRecyclerView.requestLayout();
            }
            setBackgroundCompat(mLinearLayout,getGradientDrawable(backgroundCorners, backgroundCorners, 0, 0, backGroundColor));
            Log.e(TAG,"showSuggestions-setBackgroundCompat");
        } else {
            mDividerView.setVisibility(View.GONE);
            SearchAnimator.fadeOut(mRecyclerView, mAnimationDuration);
            setBackgroundCompat(mLinearLayout,getGradientDrawable(backgroundCorners, backgroundCorners, backgroundCorners, backgroundCorners, backGroundColor));
        }
    }

    public void hideSuggestions() {
        Log.e(TAG,"hideSuggestions");
        if (mAdapter != null) {
            mDividerView.setVisibility(View.GONE);
            SearchAnimator.fadeOut(mRecyclerView, mAnimationDuration);
            setBackgroundCompat(mLinearLayout,getGradientDrawable(backgroundCorners, backgroundCorners, backgroundCorners, backgroundCorners, backGroundColor));
            Log.e(TAG,"hideSuggestions-setBackgroundCompat");
        }
    }

    public void open(boolean animate) {
        if (mShouldClearOnOpen && mSearchEditText.length() > 0) {
            mSearchEditText.getText().clear();
        }
        mSearchEditText.requestFocus();
    }

    public void close(boolean animate) {
        if (mShouldClearOnClose && mSearchEditText.length() > 0) {
            mSearchEditText.getText().clear();
        }
        mSearchEditText.clearFocus();
    }

    public void addFocus() {
        mIsSearchOpen = true;
        if (mArrow) {
            mIsSearchArrowHamburgerState = SearchDrawable.STATE_ANIM_START;
        } else {
            setArrow();
        }
        if(View.GONE==mShadowView.getVisibility()){
            mShadowView.setVisibility(View.VISIBLE);
        }
        SearchAnimator.fadeIn(mShadowView, mAnimationDuration);
        postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mOnOpenCloseListener != null) {
                    mOnOpenCloseListener.onOpen();
                }
            }
        }, mAnimationDuration);
        showKeyboard();
        showSuggestions();
    }

    public void removeFocus() {
        mIsSearchOpen = false;
        if (mArrow) {
            mIsSearchArrowHamburgerState = SearchDrawable.STATE_ANIM_STOP;
        } else {
            setHamburger();
        }
        SearchAnimator.fadeOut(mShadowView, mAnimationDuration);
        postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mOnOpenCloseListener != null) {
                    mOnOpenCloseListener.onClose();
                }
            }
        }, mAnimationDuration);
        hideKeyboard();
        hideSuggestions();
    }

    public boolean isSearchOpen() {
        return mIsSearchOpen; // getVisibility();
    }

    public void showKeyboard() {
        if (!isInEditMode()) {
            InputMethodManager inputManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.showSoftInput(mSearchEditText, 0);
            inputManager.showSoftInput(this, 0);
        }
    }

    public void hideKeyboard() {
        if (!isInEditMode()) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getWindowToken(), 0);
        }
    }

    public void showProgress() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgress() {
        mProgressBar.setVisibility(View.GONE);
    }

    public boolean isShowingProgress() {
        return mProgressBar.getVisibility() == View.VISIBLE;
    }

    // ---------------------------------------------------------------------------------------------
    private void onQueryText(final CharSequence newText) {
        if (filterLocal&&null!=mAdapter) {
            mAdapter.getFilter().filter(newText);
        }
        if (mOnQueryChangeListener != null) {
            mOnQueryChangeListener.onQueryTextChange(newText.toString());
        }
    }

    private void setQueryWithoutSubmitting(CharSequence query) {
        mSearchEditText.setText(query);
        if (query != null) {
            mSearchEditText.setSelection(mSearchEditText.length());
        } else {
            mSearchEditText.getText().clear(); // mSearchEditText.setText("");
        }
    }

    private void setArrow() {
        if (mSearchArrow != null) {
            mSearchArrow.startAnim();
            mIsSearchArrowHamburgerState = SearchDrawable.STATE_ANIM_START;
        }
    }

    private void setHamburger() {
        if (mSearchArrow != null) {
            mSearchArrow.resetAnim();
            mIsSearchArrowHamburgerState = SearchDrawable.STATE_ANIM_STOP;
        }
    }

    private LayoutTransition getRecyclerViewLayoutTransition() {
        LayoutTransition layoutTransition = new LayoutTransition();
        layoutTransition.setDuration(200);
        return layoutTransition;
    }

    private void onSubmitQuery() {
        CharSequence query = mSearchEditText.getText();
        if (mOnQueryChangeListener == null || mOnQueryChangeListener.onQueryTextSubmit(query.toString())) {
            if (!TextUtils.isEmpty(query) && TextUtils.getTrimmedLength(query) > 0) {
                mAdapter.insertHistoryItem(new SearchItem(query.toString(),SearchItem.HISTORY_ITEM));
                close(true);
            }
        }
    }
    // ---------------------------------------------------------------------------------------------
    @Override
    public void onClick(View v) {
        if (v == mBackImageView) {
            if (mSearchArrow != null && mIsSearchArrowHamburgerState == SearchDrawable.STATE_ANIM_START) {
                close(true);
            } else {
                if (mOnMenuClickListener != null) {
                    mOnMenuClickListener.onMenuClick();
                }
            }
        } else if (v == mEmptyImageView) {
            if (mSearchEditText.length() > 0) {
                mSearchEditText.getText().clear();
            }
        } else if (v == mShadowView) {
            close(true);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);

        ss.isSearchOpen = mIsSearchOpen;

        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState ss = (SavedState) state;
        if (ss.isSearchOpen) {
            open(true);
            mSearchEditText.requestFocus();
        }
        super.onRestoreInstanceState(ss.getSuperState());
        requestLayout();
    }

    // ---------------------------------------------------------------------------------------------
    public void setOnQueryTextListener(OnQueryTextListener listener) {
        mOnQueryChangeListener = listener;
    }

    public void setOnOpenCloseListener(OnOpenCloseListener listener) {
        mOnOpenCloseListener = listener;
    }

    public void setOnMenuClickListener(OnMenuClickListener listener) {
        mOnMenuClickListener = listener;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    // ---------------------------------------------------------------------------------------------
    @SuppressWarnings({"UnusedReturnValue", "SameReturnValue", "UnusedParameters"})
    public interface OnQueryTextListener {
        boolean onQueryTextSubmit(String query);

        void onQueryTextChange(String newText);
    }

    @SuppressWarnings({"UnusedReturnValue", "SameReturnValue"})
    public interface OnOpenCloseListener {
        boolean onClose();

        boolean onOpen();
    }

    public interface OnMenuClickListener {
        void onMenuClick();
    }

    // ---------------------------------------------------------------------------------------------
    private static class SavedState extends BaseSavedState {

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        String query;
        boolean isSearchOpen;

        SavedState(Parcelable superState) {
            super(superState);
        }

        SavedState(Parcel source) {
            super(source);
            this.query = source.readString();
            this.isSearchOpen = source.readInt() == 1;
        }

        @TargetApi(24)
        SavedState(Parcel source, ClassLoader loader) {
            super(source, loader);
            this.query = source.readString();
            this.isSearchOpen = source.readInt() == 1;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeString(query);
            out.writeInt(isSearchOpen ? 1 : 0);
        }

    }

}
