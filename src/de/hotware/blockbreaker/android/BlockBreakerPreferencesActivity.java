package de.hotware.blockbreaker.android;

import de.hotware.blockbreaker.android.R;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class BlockBreakerPreferencesActivity extends PreferenceActivity {

	 @Override
     protected void onCreate(Bundle savedInstanceState) {
             super.onCreate(savedInstanceState);
             this.addPreferencesFromResource(R.xml.preferences);
     }
	
}
