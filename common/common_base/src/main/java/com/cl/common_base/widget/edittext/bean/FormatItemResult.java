package com.cl.common_base.widget.edittext.bean;


import android.os.Parcel;
import android.os.Parcelable;

public class FormatItemResult implements Parcelable {
    private int fromIndex;

    private int length;

    private String id;

    private String name;

    private String abbyId;

    private String picture;

    public FormatItemResult() {
    }

    protected FormatItemResult(Parcel in) {
        fromIndex = in.readInt();
        length = in.readInt();
        id = in.readString();
        name = in.readString();
        picture = in.readString();
        abbyId = in.readString();
    }

    public static final Creator<FormatItemResult> CREATOR = new Creator<FormatItemResult>() {
        @Override
        public FormatItemResult createFromParcel(Parcel in) {
            return new FormatItemResult(in);
        }

        @Override
        public FormatItemResult[] newArray(int size) {
            return new FormatItemResult[size];
        }
    };

    public int getFromIndex() {
        return fromIndex;
    }

    public void setFromIndex(int fromIndex) {
        this.fromIndex = fromIndex;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getPicture() {
        return this.picture;
    }

    public void setAbbyId(String abbyId) {
        this.abbyId = abbyId;
    }

    public String getAbbyId() {
        return this.abbyId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(fromIndex);
        dest.writeInt(length);
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(picture);
        dest.writeString(abbyId);
    }
}