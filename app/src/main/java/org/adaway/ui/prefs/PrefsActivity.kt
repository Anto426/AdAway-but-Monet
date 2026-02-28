package org.adaway.ui.prefs

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import org.adaway.R
import org.adaway.helper.ThemeHelper
import org.adaway.ui.compose.ExpressiveAppContainer

/**
 * This activity is the preferences activity.
 */
class PrefsActivity : AppCompatActivity(),
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
    private var settingsContainerId: Int = View.NO_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        ThemeHelper.applyTheme(this)
        settingsContainerId = savedInstanceState?.getInt(SETTINGS_CONTAINER_ID_KEY) ?: View.generateViewId()

        setContent {
            ExpressiveAppContainer {
                AndroidView(
                    factory = { context ->
                        FragmentContainerView(context).apply {
                            id = settingsContainerId
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        if (savedInstanceState == null) {
            window.decorView.post {
                if (supportFragmentManager.findFragmentById(settingsContainerId) == null) {
                    supportFragmentManager.beginTransaction()
                        .replace(settingsContainerId, PrefsMainFragment())
                        .commit()
                }
            }
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(SETTINGS_CONTAINER_ID_KEY, settingsContainerId)
        super.onSaveInstanceState(outState)
    }

    override fun onSupportNavigateUp(): Boolean {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onSupportNavigateUp()
        }
        return true
    }

    override fun onPreferenceStartFragment(
        caller: PreferenceFragmentCompat,
        pref: Preference
    ): Boolean {
        val fragmentClassName = pref.fragment ?: return false
        val fragment: Fragment = supportFragmentManager.fragmentFactory.instantiate(
            classLoader,
            fragmentClassName
        )
        fragment.arguments = pref.extras
        // See https://developer.android.com/guide/topics/ui/settings/organize-your-settings#java
        @Suppress("DEPRECATION")
        fragment.setTargetFragment(caller, 0)
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.animator.fragment_open_enter,
                R.animator.fragment_open_exit,
                R.animator.fragment_close_enter,
                R.animator.fragment_close_exit
            )
            .replace(settingsContainerId, fragment)
            .addToBackStack(null)
            .commit()
        return true
    }

    companion object {
        private const val SETTINGS_CONTAINER_ID_KEY = "settings_container_id"

        @JvmField
        val PREFERENCE_NOT_FOUND: String = "preference not found"

        @JvmStatic
        fun setAppBarTitle(fragment: PreferenceFragmentCompat, @StringRes title: Int) {
            val activity: FragmentActivity = fragment.activity ?: return
            if (activity is PrefsActivity) {
                activity.supportActionBar?.setTitle(title)
            }
        }
    }
}
