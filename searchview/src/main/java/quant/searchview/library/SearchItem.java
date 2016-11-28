package quant.searchview.library;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by cz on 11/25/16.
 */
public class SearchItem implements Parcelable {
    public static final int HISTORY_ITEM=0;
    public static final int QUERY_ITEM=1;
    public static final int CLEAR_ITEM=2;
    public final String text;
    public final int type;
    public long ct;

    public SearchItem(String text) {
        this(text,QUERY_ITEM);
    }

    public SearchItem(String text, int type) {
        this.text = text;
        this.type = type;
        this.ct=System.currentTimeMillis();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchItem that = (SearchItem) o;
        return text.equals(that.text);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.text);
        dest.writeInt(this.type);
        dest.writeLong(this.ct);
    }

    protected SearchItem(Parcel in) {
        this.text = in.readString();
        this.type = in.readInt();
        this.ct = in.readLong();
    }

    public static final Creator<SearchItem> CREATOR = new Creator<SearchItem>() {
        @Override
        public SearchItem createFromParcel(Parcel source) {
            return new SearchItem(source);
        }

        @Override
        public SearchItem[] newArray(int size) {
            return new SearchItem[size];
        }
    };
}
