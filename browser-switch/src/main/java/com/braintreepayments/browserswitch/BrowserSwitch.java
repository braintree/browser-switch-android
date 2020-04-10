package com.braintreepayments.browserswitch;

import android.content.Context;

class BrowserSwitch {
    private Context mContext;
    private int mRequestCode;

    public Context getContext() {
        return mContext;
    }

    public void setContext(Context context) {
        mContext = context;
    }

    public int getRequestCode() {
        return mRequestCode;
    }

    public void setRequestCode(int newRequestCode) {
        mRequestCode = newRequestCode;
    }
}
