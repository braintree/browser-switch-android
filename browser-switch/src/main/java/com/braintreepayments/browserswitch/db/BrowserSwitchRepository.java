package com.braintreepayments.browserswitch.db;

import android.app.Application;

import androidx.lifecycle.LiveData;

public class BrowserSwitchRepository {

    public static BrowserSwitchRepository newInstance(Application application) {
        return new BrowserSwitchRepository(application);
    }

    private PendingRequestDao pendingRequestDao;
    private LiveData<PendingRequest> pendingRequest;

    private BrowserSwitchRepository(Application application) {
        BrowserSwitchDatabase db = BrowserSwitchDatabase.getDatabase(application);
        pendingRequestDao = db.pendingRequestDao();
        pendingRequest = pendingRequestDao.getPendingRequest();
    }

    public LiveData<PendingRequest> getPendingRequest() {
        return pendingRequest;
    }

    public void insert(PendingRequest pendingRequest) {
        BrowserSwitchDatabase.databaseWriteExecutor.execute(() -> {
            pendingRequestDao.insert(pendingRequest);
        });
    }

    public void deleteAll() {
        BrowserSwitchDatabase.databaseWriteExecutor.execute(() -> {
            pendingRequestDao.deleteAll();
        });
    }
}
