package com.braintreepayments.browserswitch.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

/**
 * This DAO enforces a singleton table. Only one pending request may be present at a time to
 * provide backward compatibility with previous versions that use Fragment lifecycle and
 * saved instance state to communicate browser switch events.
 */
@Dao
public interface PendingRequestDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(PendingRequest pendingRequest);

    @Query("SELECT * from pending_request WHERE success = 1 LIMIT 1")
    LiveData<PendingRequest> getPendingRequest();

    @Query("DELETE FROM pending_request")
    void deleteAll();

    @Query("UPDATE pending_request SET success = :didFinish WHERE id = :id")
    void updatePendingRequest(long id, int didFinish);
}
