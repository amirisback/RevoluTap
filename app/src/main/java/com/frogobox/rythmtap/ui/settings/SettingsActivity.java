package com.frogobox.rythmtap.ui.settings;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.frogobox.rythmtap.R;

public class SettingsActivity extends AppCompatActivity implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

	@Override
	public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
		Bundle args = pref.getExtras();
		Fragment fragment = getSupportFragmentManager().getFragmentFactory().instantiate(getClassLoader(), pref.getFragment());
		fragment.setArguments(args);
		fragment.setTargetFragment(caller, 0);
		getSupportFragmentManager().beginTransaction().replace(R.id.settings_layout, fragment).addToBackStack(null).commit();
		return true;
	}

	public static class MenuSettingsFragment extends PreferenceFragmentCompat {
		public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
			setPreferencesFromResource(R.xml.settings, rootKey);
		}
	}

	public static class MenuSettingsFragmentModifiers extends PreferenceFragmentCompat {
		public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
			setPreferencesFromResource(R.xml.settings_modifiers, rootKey);
		}
	}

	public static class MenuSettingsFragmentGame extends PreferenceFragmentCompat {
		public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
			setPreferencesFromResource(R.xml.settings_game, rootKey);
		}
	}

	public static class MenuSettingsFragmentVibrate extends PreferenceFragmentCompat {
		public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
			setPreferencesFromResource(R.xml.settings_vibrate, rootKey);
		}
	}

	public static class MenuSettingsFragmentInfo extends PreferenceFragmentCompat {
		public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
			setPreferencesFromResource(R.xml.settings_info, rootKey);
		}
	}

	public static class MenuSettingsFragmentAdvanced extends PreferenceFragmentCompat {
		public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
			setPreferencesFromResource(R.xml.settings_advanced, rootKey);
		}
	}

	public void onCreate(Bundle savedInstanceState) {
		SettingsActivity.super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		getSupportFragmentManager().beginTransaction().replace(R.id.settings_layout, new MenuSettingsFragment()).commit();
	}

}