package com.braintreepayments.browserswitch.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Ref: https://codelabs.developers.google.com/codelabs/android-room-with-a-view/#6
@Database(entities = { PendingRequest.class }, version = 1, exportSchema = false)
public abstract class BrowserSwitchDatabase extends RoomDatabase {

    public abstract PendingRequestDao pendingRequestDao();

    private static volatile BrowserSwitchDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static BrowserSwitchDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (BrowserSwitchDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(), BrowserSwitchDatabase.class, "word_database"
                    )
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}
