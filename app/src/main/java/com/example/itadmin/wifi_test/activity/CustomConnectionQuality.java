package com.example.itadmin.wifi_test.activity;

import android.os.Parcel;
import android.os.Parcelable;

import com.facebook.network.connectionclass.ConnectionQuality;

/**
 * Created by itadmin on 9/1/16.
 */
public class CustomConnectionQuality implements Parcelable {
    public ConnectionQuality wifiQuality;
    public ConnectionQuality internetQuality;

    public CustomConnectionQuality()
    {
        wifiQuality = ConnectionQuality.UNKNOWN;
        internetQuality = ConnectionQuality.UNKNOWN;
    }


    public CustomConnectionQuality(ConnectionQuality pWifiQuality,ConnectionQuality pInternetQuality)
    {
        wifiQuality = pWifiQuality;
        internetQuality = pInternetQuality;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.wifiQuality == null ? -1 : this.wifiQuality.ordinal());
        dest.writeInt(this.internetQuality == null ? -1 : this.internetQuality.ordinal());
    }

    protected CustomConnectionQuality(Parcel in) {
        int tmpWifiQuality = in.readInt();
        this.wifiQuality = tmpWifiQuality == -1 ? null : ConnectionQuality.values()[tmpWifiQuality];
        int tmpInternetQuality = in.readInt();
        this.internetQuality = tmpInternetQuality == -1 ? null : ConnectionQuality.values()[tmpInternetQuality];
    }

    public static final Parcelable.Creator<CustomConnectionQuality> CREATOR = new Parcelable.Creator<CustomConnectionQuality>() {
        @Override
        public CustomConnectionQuality createFromParcel(Parcel source) {
            return new CustomConnectionQuality(source);
        }

        @Override
        public CustomConnectionQuality[] newArray(int size) {
            return new CustomConnectionQuality[size];
        }
    };
}
