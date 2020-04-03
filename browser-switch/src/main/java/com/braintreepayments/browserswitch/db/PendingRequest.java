package com.braintreepayments.browserswitch.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "pending_request")
public class PendingRequest {

    @PrimaryKey
    @ColumnInfo(name = "id")
    private long id;

    @ColumnInfo(name = "request_code")
    private int requestCode;

    @ColumnInfo(name = "success")
    private int didFinish;

    @ColumnInfo(name = "url")
    private String url;

    public PendingRequest(long id, int requestCode, String url, int didFinish) {
        this.id = id;
        this.requestCode = requestCode;
        this.url = url;
        this.didFinish = didFinish;
    }

    public void setId(long value) {
        id = value;
    }

    public long getId() {
        return id;
    }

    public int getRequestCode() {
        return requestCode;
    }

    public int getDidFinish() {
        return didFinish;
    }

    public boolean isFinished() {
        return (didFinish == 1);
    }

    public String getUrl() { return url; }
}
