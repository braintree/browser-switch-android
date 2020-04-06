package com.braintreepayments.browserswitch.db;

import android.app.Application;
import android.net.Uri;

import androidx.lifecycle.LiveData;

import com.braintreepayments.browserswitch.BrowserSwitchConstants;

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

    public void markPendingRequestAsFinished(long pendingRequestId) {
        BrowserSwitchDatabase.databaseWriteExecutor.execute(() -> {
            pendingRequestDao.updatePendingRequest(pendingRequestId, 1);
        });
    }

    public void deleteAll() {
        BrowserSwitchDatabase.databaseWriteExecutor.execute(() -> {
            pendingRequestDao.deleteAll();
        });
    }

    public void updatePendingRequest(int requestCode, Uri uri) {
        // TODO: unit test
        insert(new PendingRequest(BrowserSwitchConstants.PENDING_REQUEST_ID, requestCode, uri.toString(), 0));
    }
}
