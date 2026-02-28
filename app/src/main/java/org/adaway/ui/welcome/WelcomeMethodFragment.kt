package org.adaway.ui.welcome

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.ACTION_VPN_SETTINGS
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.topjohnwu.superuser.Shell
import org.adaway.R
import org.adaway.helper.PreferenceHelper
import org.adaway.model.adblocking.AdBlockMethod
import org.adaway.ui.compose.AdAwayExpressiveTheme
import org.adaway.ui.compose.ExpressivePage
import org.adaway.util.log.SentryLog

/**
 * This class is a fragment to setup the ad blocking method.
 */
class WelcomeMethodFragment : WelcomeFragment() {
    private lateinit var prepareVpnLauncher: ActivityResultLauncher<Intent>
    private var selectedMethod by mutableStateOf(SelectedMethod.NONE)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        prepareVpnLauncher = registerForActivityResult(StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                notifyVpnEnabled()
            } else {
                notifyVpnDisabled()
                checkAlwaysOnVpn()
            }
        }

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AdAwayExpressiveTheme {
                    WelcomeMethodScreen(
                        selectedMethod = selectedMethod,
                        onRootClick = ::checkRoot,
                        onVpnClick = ::enableVpnService
                    )
                }
            }
        }
    }

    private fun checkRoot() {
        notifyVpnDisabled()
        Shell.getShell()
        if (java.lang.Boolean.TRUE == Shell.isAppGrantedRoot()) {
            notifyRootEnabled()
        } else {
            notifyRootDisabled(showDialog = true)
        }
    }

    private fun enableVpnService() {
        notifyRootDisabled(showDialog = false)
        val context = context ?: return
        val prepareIntent = VpnService.prepare(context)
        if (prepareIntent == null) {
            notifyVpnEnabled()
        } else {
            prepareVpnLauncher.launch(prepareIntent)
        }
    }

    private fun notifyRootEnabled() {
        SentryLog.recordBreadcrumb("Enable root ad-blocking method")
        PreferenceHelper.setAbBlockMethod(requireContext(), AdBlockMethod.ROOT)
        selectedMethod = SelectedMethod.ROOT
        allowNext()
    }

    private fun notifyRootDisabled(showDialog: Boolean) {
        PreferenceHelper.setAbBlockMethod(requireContext(), AdBlockMethod.UNDEFINED)
        selectedMethod = SelectedMethod.NONE
        if (showDialog) {
            blockNext()
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.welcome_root_missing_title)
                .setMessage(R.string.welcome_root_missile_description)
                .setPositiveButton(R.string.button_close, null)
                .create()
                .show()
        }
    }

    private fun notifyVpnEnabled() {
        SentryLog.recordBreadcrumb("Enable vpn ad-blocking method")
        PreferenceHelper.setAbBlockMethod(requireContext(), AdBlockMethod.VPN)
        selectedMethod = SelectedMethod.VPN
        allowNext()
    }

    private fun notifyVpnDisabled() {
        PreferenceHelper.setAbBlockMethod(requireContext(), AdBlockMethod.UNDEFINED)
        selectedMethod = SelectedMethod.NONE
        blockNext()
    }

    private fun checkAlwaysOnVpn() {
        var alwaysOnMessage = R.string.welcome_vpn_alwayson_description
        try {
            val alwaysOn = Settings.Secure.getString(requireContext().contentResolver, "always_on_vpn_app")
            if (alwaysOn == null) {
                return
            }
        } catch (_: SecurityException) {
            alwaysOnMessage = R.string.welcome_vpn_alwayson_blocked_description
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.welcome_vpn_alwayson_title)
            .setMessage(alwaysOnMessage)
            .setNegativeButton(R.string.button_close, null)
            .setPositiveButton(R.string.welcome_vpn_alwayson_settings_action) { dialog, _ ->
                dialog.dismiss()
                startActivity(Intent(ACTION_VPN_SETTINGS))
            }
            .create()
            .show()
    }
}

private enum class SelectedMethod {
    NONE,
    ROOT,
    VPN
}

@Composable
private fun WelcomeMethodScreen(
    selectedMethod: SelectedMethod,
    onRootClick: () -> Unit,
    onVpnClick: () -> Unit
) {
    ExpressivePage {
        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = stringResource(R.string.app_logo),
            modifier = Modifier
                .padding(top = 16.dp)
                .size(96.dp)
        )

        Text(
            text = stringResource(R.string.welcome_method_header),
            style = MaterialTheme.typography.displaySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        )

        Text(
            text = stringResource(R.string.welcome_method_summary),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp)
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            WelcomeMethodCard(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                selected = selectedMethod == SelectedMethod.ROOT,
                iconRes = R.drawable.ic_superuser_24dp,
                iconDescription = R.string.welcome_root_method_logo,
                titleRes = R.string.welcome_root_method_title,
                entries = listOf(
                    MethodEntry(R.drawable.ic_add_circle_outline_24dp, R.string.welcome_root_method_text1, MaterialTheme.colorScheme.primary),
                    MethodEntry(R.drawable.ic_add_circle_outline_24dp, R.string.welcome_root_method_text2, MaterialTheme.colorScheme.primary),
                    MethodEntry(R.drawable.ic_remove_circle_outline_24dp, R.string.welcome_root_method_text3, MaterialTheme.colorScheme.error)
                ),
                onClick = onRootClick
            )
            WelcomeMethodCard(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                selected = selectedMethod == SelectedMethod.VPN,
                iconRes = R.drawable.ic_vpn_key_24dp,
                iconDescription = R.string.welcome_vpn_method_logo,
                titleRes = R.string.welcome_vpn_method_title,
                entries = listOf(
                    MethodEntry(R.drawable.ic_remove_circle_outline_24dp, R.string.welcome_vpn_method_text1, MaterialTheme.colorScheme.error),
                    MethodEntry(R.drawable.ic_remove_circle_outline_24dp, R.string.welcome_vpn_method_text2, MaterialTheme.colorScheme.error),
                    MethodEntry(R.drawable.ic_add_circle_outline_24dp, R.string.welcome_vpn_method_text3, MaterialTheme.colorScheme.primary)
                ),
                onClick = onVpnClick
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

private data class MethodEntry(val iconRes: Int, val textRes: Int, val tint: Color)

@Composable
private fun WelcomeMethodCard(
    modifier: Modifier = Modifier,
    selected: Boolean,
    iconRes: Int,
    iconDescription: Int,
    titleRes: Int,
    entries: List<MethodEntry>,
    onClick: () -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
        label = "cardColor"
    )
    val elevation by animateDpAsState(
        targetValue = if (selected) 8.dp else 2.dp,
        label = "cardElevation"
    )

    ElevatedCard(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = elevation),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = stringResource(iconDescription),
                tint = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )

            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            entries.forEach { entry ->
                Row(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        painter = painterResource(entry.iconRes),
                        contentDescription = null,
                        tint = if (selected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else entry.tint,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = stringResource(entry.textRes),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}
