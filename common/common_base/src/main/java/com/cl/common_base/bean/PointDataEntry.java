package com.cl.common_base.bean;

import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.ParcelFormatException;
import android.os.Parcelable;

import com.github.mikephil.charting.data.BaseEntry;
import com.github.mikephil.charting.utils.Utils;

public class PointDataEntry  extends BaseEntry implements Parcelable {
    private float x = 0.0F;
    public static final Parcelable.Creator<PointDataEntry> CREATOR = new Parcelable.Creator<PointDataEntry>() {
        public PointDataEntry createFromParcel(Parcel source) {
            return new PointDataEntry(source);
        }

        public PointDataEntry[] newArray(int size) {
            return new PointDataEntry[size];
        }
    };

    private String time;

    public void setTime(String time) {
        this.time = time;
    }

    public String getTime() {
        return time;
    }

    public PointDataEntry() {
    }

    public PointDataEntry(float x, float y) {
        super(y);
        this.x = x;
    }

    public PointDataEntry(float x, float y, Object data) {
        super(y, data);
        this.x = x;
    }

    public PointDataEntry(float x, float y, Drawable icon) {
        super(y, icon);
        this.x = x;
    }

    public PointDataEntry(float x, float y, Drawable icon, Object data) {
        super(y, icon, data);
        this.x = x;
    }

    public float getX() {
        return this.x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public PointDataEntry copy() {
        PointDataEntry e = new PointDataEntry(this.x, this.getY(), this.getData());
        return e;
    }

    public boolean equalTo(PointDataEntry e) {
        if (e == null) {
            return false;
        } else if (e.getData() != this.getData()) {
            return false;
        } else if (Math.abs(e.x - this.x) > Utils.FLOAT_EPSILON) {
            return false;
        } else {
            return !(Math.abs(e.getY() - this.getY()) > Utils.FLOAT_EPSILON);
        }
    }

    public String toString() {
        return "PointDataEntry, x: " + this.x + " y: " + this.getY();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(this.x);
        dest.writeFloat(this.getY());
        if (this.getData() != null) {
            if (!(this.getData() instanceof Parcelable)) {
                throw new ParcelFormatException("Cannot parcel an PointDataEntry with non-parcelable data");
            }

            dest.writeInt(1);
            dest.writeParcelable((Parcelable)this.getData(), flags);
        } else {
            dest.writeInt(0);
        }

    }

    protected PointDataEntry(Parcel in) {
        this.x = in.readFloat();
        this.setY(in.readFloat());
        if (in.readInt() == 1) {
            this.setData(in.readParcelable(Object.class.getClassLoader()));
        }

    }
}
