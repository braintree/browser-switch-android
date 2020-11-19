package com.braintreepayments.api;

import android.content.Context;
import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static com.braintreepayments.api.PersistentStore.PREFERENCES_KEY;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({SharedPreferences.class })
public class PersistentStoreTest {

    private Context context;
    private Context applicationContext;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor sharedPreferencesEditor;

    @Before
    public void beforeEach() {
        context = mock(Context.class);
        applicationContext = mock(Context.class);

        sharedPreferences = mock(SharedPreferences.class);
        sharedPreferencesEditor = mock(SharedPreferences.Editor.class);

        when(context.getApplicationContext()).thenReturn(applicationContext);

        when(
            applicationContext.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE)
        ).thenReturn(sharedPreferences);
        when(sharedPreferences.edit()).thenReturn(sharedPreferencesEditor);
    }

    @Test
    public void put_placesKeyValuePairIntoSharedPrefs() {
        when(
            sharedPreferencesEditor.putString(anyString(), anyString())
        ).thenReturn(sharedPreferencesEditor);
        PersistentStore.put("key", "value", context);

        InOrder inOrder = Mockito.inOrder(sharedPreferencesEditor);
        inOrder.verify(sharedPreferencesEditor).putString("key", "value");
        inOrder.verify(sharedPreferencesEditor).apply();
    }

    @Test
    public void get_retrievesValueFromSharedPrefsByKey() {
        when(sharedPreferences.getString("key", null)).thenReturn("sampleValue");
        String result = PersistentStore.get("key", context);
        assertEquals(result, "sampleValue");
    }

    @Test
    public void remove_removesValueInSharedPrefsByKey() {
        when(sharedPreferencesEditor.remove(anyString())).thenReturn(sharedPreferencesEditor);
        PersistentStore.remove("key", context);

        InOrder inOrder = Mockito.inOrder(sharedPreferencesEditor);
        inOrder.verify(sharedPreferencesEditor).remove("key");
        inOrder.verify(sharedPreferencesEditor).apply();
    }
}
