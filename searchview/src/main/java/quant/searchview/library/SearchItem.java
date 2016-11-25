package quant.searchview.library;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by cz on 11/25/16.
 */
public class SearchItem implements Parcelable {
    public static final int HISTORY_ITEM=0;
    public static final int QUERY_ITEM=1;
    public final String text;
    public final int type;

    public SearchItem(String text) {
        this(text,QUERY_ITEM);
    }

    public SearchItem(String text, int type) {
        this.text = text;
        this.type = type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.text);
        dest.writeInt(this.type);
    }

    protected SearchItem(Parcel in) {
        this.text = in.readString();
        this.type = in.readInt();
    }

    public static final Parcelable.Creator<SearchItem> CREATOR = new Parcelable.Creator<SearchItem>() {
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
