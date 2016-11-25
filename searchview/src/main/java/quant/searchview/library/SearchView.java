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
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Filterable;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class SearchView extends FrameLayout implements View.OnClickListener {

    private static int mIconColor = Color.BLACK;
    private static int mTextColor = Color.BLACK;
    private static int mTextHighlightColor = Color.BLACK;
    private static Typeface mTextFont = Typeface.DEFAULT;

    private final Runnable mShowImeRunnable = new Runnable() {
        @Override
        public void run() {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        }
    };
    private SearchDrawable mSearchArrow = null;
    private RecyclerView.Adapter mAdapter = null;
    private List<Boolean> mSearchFiltersStates = null;
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
    private LinearLayout mFiltersContainer;
    private LinearLayout mLinearLayout;
    private CharSequence mOldQueryText;
    private CharSequence mUserQuery = "";
    private int mAnimationDuration;
    private float mIsSearchArrowHamburgerState = SearchDrawable.STATE_ANIM_NONE;
    private boolean mShadow = true;
    private boolean mArrow = false;
    private boolean mIsSearchOpen = false;
    private boolean mShouldClearOnOpen = false;
    private boolean mShouldClearOnClose = false;
    private boolean mShouldHideOnKeyboardClose = true;

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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SearchView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
        initStyle(context,attrs, defStyleAttr);
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
        for (int i = 0, n = mFiltersContainer.getChildCount(); i < n; i++) {
            View child = mFiltersContainer.getChildAt(i);
            if (child instanceof AppCompatCheckBox)
                ((AppCompatCheckBox) child).setTextColor(mTextColor);
        }
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
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView_result);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mRecyclerView.setNestedScrollingEnabled(false);
        //mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setVisibility(View.GONE);
        //mRecyclerView.setItemAnimator(null);
        //mRecyclerView.setLayoutTransition(getRecyclerViewLayoutTransition());
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    // mRecyclerView.setLayoutTransition(null);
                    hideKeyboard();
                } else {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        //   mRecyclerView.setLayoutTransition(getRecyclerViewLayoutTransition());
                    }
                }
            }
        });

        mShadowView = findViewById(R.id.view_shadow);
        mShadowView.setOnClickListener(this);
        mShadowView.setVisibility(View.GONE);

        mDividerView = findViewById(R.id.view_divider);
        mDividerView.setVisibility(View.GONE);

        mFiltersContainer = (LinearLayout) findViewById(R.id.filters_container);
        mFiltersContainer.setVisibility(View.GONE);

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
                SearchView.this.onTextChanged(charSequence);
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
        mSearchEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
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
        if (attr != null) {
            if (attr.hasValue(R.styleable.SearchView_search_layout_height)) {
                setSearchLayoutHeight((int) attr.getDimension(R.styleable.SearchView_search_layout_height, 0));
            }
            if (attr.hasValue(R.styleable.SearchView_search_layout_margin)) {
                setSearchLayoutMargin((int) attr.getDimension(R.styleable.SearchView_search_layout_margin, 0));
            }
            if (attr.hasValue(R.styleable.SearchView_search_icon_color)) {
                setIconColor(attr.getColor(R.styleable.SearchView_search_icon_color, 0));
            }
            if (attr.hasValue(R.styleable.SearchView_search_background_color)) {
                setBackgroundColor(attr.getColor(R.styleable.SearchView_search_background_color, 0));
            }
            if (attr.hasValue(R.styleable.SearchView_search_text_color)) {
                setTextColor(attr.getColor(R.styleable.SearchView_search_text_color, 0));
            }
            if (attr.hasValue(R.styleable.SearchView_search_text_highlight_color)) {
                setTextHighlightColor(attr.getColor(R.styleable.SearchView_search_text_highlight_color, 0));
            }
            if (attr.hasValue(R.styleable.SearchView_search_text_size)) {
                setTextSize(attr.getDimensionPixelSize(R.styleable.SearchView_search_text_size,0));
            }
            if (attr.hasValue(R.styleable.SearchView_search_hint)) {
                setHint(attr.getString(R.styleable.SearchView_search_hint));
            }
            if (attr.hasValue(R.styleable.SearchView_search_hint_color)) {
                setHintColor(attr.getColor(R.styleable.SearchView_search_hint_color, 0));
            }
            if (attr.hasValue(R.styleable.SearchView_search_divider)) {
                setDivider(attr.getBoolean(R.styleable.SearchView_search_divider, false));
            }
            if (attr.hasValue(R.styleable.SearchView_search_animation_duration)) {
                setAnimationDuration(attr.getInteger(R.styleable.SearchView_search_animation_duration, 0));
            }
            if (attr.hasValue(R.styleable.SearchView_search_shadow)) {
                setShadow(attr.getBoolean(R.styleable.SearchView_search_shadow, true));
            }
            if (attr.hasValue(R.styleable.SearchView_search_shadow_color)) {
                setShadowColor(attr.getColor(R.styleable.SearchView_search_shadow_color, 0));
            }
            if (attr.hasValue(R.styleable.SearchView_search_clear_on_open)) {
                setShouldClearOnOpen(attr.getBoolean(R.styleable.SearchView_search_clear_on_open, false));
            }
            if (attr.hasValue(R.styleable.SearchView_search_clear_on_close)) {
                setShouldClearOnClose(attr.getBoolean(R.styleable.SearchView_search_clear_on_close, true));
            }
            if (attr.hasValue(R.styleable.SearchView_search_hide_on_keyboard_close)) {
                setShouldHideOnKeyboardClose(attr.getBoolean(R.styleable.SearchView_search_hide_on_keyboard_close, true));
            }
            if (attr.hasValue(R.styleable.SearchView_search_cursor_drawable)) {
                setCursorDrawable(attr.getResourceId(R.styleable.SearchView_search_cursor_drawable, 0));
            }
            attr.recycle();
        }
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

        if (!TextUtils.isEmpty(mUserQuery)) {
            mEmptyImageView.setVisibility(View.GONE);
        }

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

    public void setAdapter(RecyclerView.Adapter adapter) {
        mAdapter = adapter;
        mRecyclerView.setAdapter(mAdapter);
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

    public void setFilters(@Nullable List<SearchFilter> filters) {
        mFiltersContainer.removeAllViews();
        if (filters == null) {
            mSearchFiltersStates = null;
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mFiltersContainer.getLayoutParams();
            params.topMargin = 0;
            params.bottomMargin = 0;
            mFiltersContainer.setLayoutParams(params);
        } else {
            mSearchFiltersStates = new ArrayList<>();
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mFiltersContainer.getLayoutParams();
            params.topMargin = getResources().getDimensionPixelSize(R.dimen.filter_margin_top);
            params.bottomMargin = params.topMargin / 2;
            mFiltersContainer.setLayoutParams(params);
            for (SearchFilter filter : filters) {
                AppCompatCheckBox checkBox = new AppCompatCheckBox(getContext());
                checkBox.setText(filter.getTitle());
                checkBox.setTextSize(12);
                checkBox.setTextColor(mTextColor);
                checkBox.setChecked(filter.isChecked());
                mFiltersContainer.addView(checkBox);

                boolean isChecked = filter.isChecked();
                mSearchFiltersStates.add(isChecked);
            }
        }
    }

    public List<Boolean> getFiltersStates() {
        return mSearchFiltersStates;
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
        mSearchEditText.setTextSize(size);
    }

    public void setHintColor(@ColorInt int color) {
        mSearchEditText.setHintTextColor(color);
    }

    public void setDivider(boolean divider) {
        if (divider) {
            mRecyclerView.addItemDecoration(new SearchDivider(getContext()));
        } else {
            mRecyclerView.removeItemDecoration(new SearchDivider(getContext()));
        }
    }

    public void setAnimationDuration(int animationDuration) {
        mAnimationDuration = animationDuration;
    }

    public void setShadow(boolean shadow) {
        if (shadow) {
            mShadowView.setVisibility(View.VISIBLE);
        } else {
            mShadowView.setVisibility(View.GONE);
        }
        mShadow = shadow;
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
            mDividerView.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.VISIBLE);
            SearchAnimator.fadeIn(mRecyclerView, mAnimationDuration);
        }

        if (mFiltersContainer.getChildCount() > 0 && mFiltersContainer.getVisibility() == View.GONE) {
            mDividerView.setVisibility(View.VISIBLE);
            mFiltersContainer.setVisibility(View.VISIBLE);
        }
    }

    public void hideSuggestions() {
        if (mAdapter != null) {
            mDividerView.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.GONE);
            SearchAnimator.fadeOut(mRecyclerView, mAnimationDuration);
        }

        if (mFiltersContainer.getVisibility() == View.VISIBLE) {
            mDividerView.setVisibility(View.GONE);
            mFiltersContainer.setVisibility(View.GONE);
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

        if (!TextUtils.isEmpty(mUserQuery)) {
            mEmptyImageView.setVisibility(View.VISIBLE);
        }
        if (mArrow) {
            mIsSearchArrowHamburgerState = SearchDrawable.STATE_ANIM_START;
        } else {
            setArrow();
        }
        if (mShadow) {
            SearchAnimator.fadeIn(mShadowView, mAnimationDuration);
        }
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

        if (!TextUtils.isEmpty(mUserQuery)) {
            mEmptyImageView.setVisibility(View.GONE);
        }
        if (mArrow) {
            mIsSearchArrowHamburgerState = SearchDrawable.STATE_ANIM_STOP;
        } else {
            setHamburger();
        }
        if (mShadow) {
            SearchAnimator.fadeOut(mShadowView, mAnimationDuration);
        }
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
    private void onTextChanged(CharSequence newText) {
        if (newText.equals(mOldQueryText)) {
            return;
        }
        CharSequence text = mSearchEditText.getText();
        mUserQuery = text;

        if (mAdapter != null && mAdapter instanceof Filterable) {
            ((Filterable) mAdapter).getFilter().filter(text);
        }

        if (!TextUtils.isEmpty(text)) {
            showSuggestions();
            mEmptyImageView.setVisibility(View.VISIBLE);
        } else {
            hideSuggestions();
            mEmptyImageView.setVisibility(View.GONE);
        }

        if (mOnQueryChangeListener != null && !TextUtils.equals(newText, mOldQueryText)) {
            dispatchFilters();
            mOnQueryChangeListener.onQueryTextChange(newText.toString());
        }
        mOldQueryText = newText.toString();
    }

    private void setQueryWithoutSubmitting(CharSequence query) {
        mSearchEditText.setText(query);
        if (query != null) {
            mSearchEditText.setSelection(mSearchEditText.length());
            mUserQuery = query;
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
        if (query != null && TextUtils.getTrimmedLength(query) > 0) {
            dispatchFilters();
            if (mOnQueryChangeListener == null || !mOnQueryChangeListener.onQueryTextSubmit(query.toString())) {
                mSearchEditText.setText(query);
            }
        }
    }

    private int getCenterX(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        return location[0] + view.getWidth() / 2;
    }

    private void restoreFiltersState(List<Boolean> states) {
        mSearchFiltersStates = states;
        for (int i = 0, j = 0, n = mFiltersContainer.getChildCount(); i < n; i++) {
            View view = mFiltersContainer.getChildAt(i);
            if (view instanceof AppCompatCheckBox) {
                ((AppCompatCheckBox) view).setChecked(mSearchFiltersStates.get(j++));
            }
        }
    }

    private void dispatchFilters() {
        if (mSearchFiltersStates != null) {
            for (int i = 0, j = 0, n = mFiltersContainer.getChildCount(); i < n; i++) {
                View view = mFiltersContainer.getChildAt(i);
                if (view instanceof AppCompatCheckBox)
                    mSearchFiltersStates.set(j++, ((AppCompatCheckBox) view).isChecked());
            }
        }
    }


    /*private int getPreferredWidth() {
        return getContext().getResources().getDimensionPixelSize(android.support.v7.appcompat.R.dimen.abc_search_view_preferred_width);
    }

    private int getPreferredHeight() {
        return getContext().getResources().getDimensionPixelSize(android.support.v7.appcompat.R.dimen.abc_search_view_preferred_height);
    }*/

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

        ss.query = mUserQuery != null ? mUserQuery.toString() : null;
        ss.isSearchOpen = mIsSearchOpen;
        dispatchFilters();
        ss.searchFiltersStates = mSearchFiltersStates;

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
            setQueryWithoutSubmitting(ss.query); // TODO
            mSearchEditText.requestFocus();
        }

        restoreFiltersState(ss.searchFiltersStates);
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

    // ---------------------------------------------------------------------------------------------
    @SuppressWarnings({"UnusedReturnValue", "SameReturnValue", "UnusedParameters"})
    public interface OnQueryTextListener {
        boolean onQueryTextSubmit(String query);

        boolean onQueryTextChange(String newText);
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
        List<Boolean> searchFiltersStates;

        SavedState(Parcelable superState) {
            super(superState);
        }

        SavedState(Parcel source) {
            super(source);
            this.query = source.readString();
            this.isSearchOpen = source.readInt() == 1;
            source.readList(searchFiltersStates, List.class.getClassLoader());
        }

        @TargetApi(24)
        SavedState(Parcel source, ClassLoader loader) {
            super(source, loader);
            this.query = source.readString();
            this.isSearchOpen = source.readInt() == 1;
            source.readList(searchFiltersStates, List.class.getClassLoader());
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeString(query);
            out.writeInt(isSearchOpen ? 1 : 0);
            out.writeList(searchFiltersStates);
        }

    }

}
