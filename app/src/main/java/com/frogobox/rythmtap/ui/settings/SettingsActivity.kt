package com.frogobox.rythmtap.ui.settings

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.frogobox.rythmtap.R
import com.frogobox.rythmtap.common.core.BaseBindActivity
import com.frogobox.rythmtap.databinding.ActivitySettingsBinding

class SettingsActivity : BaseBindActivity<ActivitySettingsBinding>(),
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    override fun setupViewBinding(): ActivitySettingsBinding {
        return ActivitySettingsBinding.inflate(layoutInflater)
    }

    override fun onPreferenceStartFragment(caller: PreferenceFragmentCompat, pref: Preference): Boolean {
        val args = pref.extras
        val fragment = supportFragmentManager.fragmentFactory.instantiate(classLoader, pref.fragment!!)
        fragment.arguments = args
        fragment.setTargetFragment(caller, 0)
        supportFragmentManager.beginTransaction().replace(R.id.settings_layout, fragment).addToBackStack(null).commit()
        return true
    }

    class MenuSettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.settings, rootKey)
        }
    }

    class MenuSettingsFragmentModifiers : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.settings_modifiers, rootKey)
        }
    }

    class MenuSettingsFragmentGame : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.settings_game, rootKey)
        }
    }

    class MenuSettingsFragmentVibrate : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.settings_vibrate, rootKey)
        }
    }

    class MenuSettingsFragmentInfo : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.settings_info, rootKey)
        }
    }

    class MenuSettingsFragmentAdvanced : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.settings_advanced, rootKey)
        }
    }

    override fun onCreateExt(savedInstanceState: Bundle?) {
        super.onCreateExt(savedInstanceState)
        setupChildFragment(R.id.settings_layout, MenuSettingsFragment())
    }

}