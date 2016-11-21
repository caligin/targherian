package tech.anima.targherian;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class SearchResultsActivity extends Activity {

    private static final String TAG = SearchResultsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG,"search results created");
        setContentView(R.layout.activity_search_results);
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // TODO: learn more about android:launchMode="singleTop"
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (!Intent.ACTION_SEARCH.equals(intent.getAction())) {
            Log.wtf(TAG, "received unrecognized intent action");
            throw new IllegalArgumentException("received unrecognized intent action");
        }
        final String query = intent.getStringExtra(SearchManager.QUERY);
        //use the query to search your data somehow
        Toast.makeText(this, query, Toast.LENGTH_LONG).show();
    }
}
