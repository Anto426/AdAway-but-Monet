package org.adaway.ui.welcome

import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.TIRAMISU
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import org.adaway.R
import org.adaway.model.error.HostError
import org.adaway.ui.compose.AdAwayExpressiveTheme
import org.adaway.ui.compose.ExpressivePage
import org.adaway.ui.compose.ExpressiveSection
import org.adaway.ui.home.HomeViewModel
import java.util.Timer
import java.util.TimerTask

/**
 * This class is a fragment to first sync the main hosts source.
 */
class WelcomeSyncFragment : WelcomeFragment() {
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private var requestPostNotificationsPermission = false

    private var headerTextRes by mutableIntStateOf(R.string.welcome_sync_header)
    private var showProgress by mutableStateOf(true)
    private var showSyncedIcon by mutableStateOf(false)
    private var showErrorIcon by mutableStateOf(false)
    private var showRetry by mutableStateOf(false)
    private var errorText by mutableStateOf("")
    private var showNotificationsText by mutableStateOf(false)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { _ -> }

        bindNotifications()

        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        homeViewModel.isAdBlocked().observe(viewLifecycleOwner) { adBlocked ->
            if (adBlocked == true) {
                notifySynced()
            }
        }
        homeViewModel.error.observe(viewLifecycleOwner, ::notifyError)
        homeViewModel.sync()

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AdAwayExpressiveTheme {
                    WelcomeSyncScreen(
                        headerTextRes = headerTextRes,
                        showProgress = showProgress,
                        showSyncedIcon = showSyncedIcon,
                        showErrorIcon = showErrorIcon,
                        showRetry = showRetry,
                        errorText = errorText,
                        showNotificationsText = showNotificationsText,
                        onRetry = ::retry
                    )
                }
            }
        }
    }

    private fun bindNotifications() {
        if (SDK_INT < TIRAMISU || requireActivity().checkSelfPermission(POST_NOTIFICATIONS) == PERMISSION_GRANTED) {
            requestPostNotificationsPermission = false
            return
        }
        showNotificationsText = true
        requestPostNotificationsPermission = true
        Timer(true).schedule(object : TimerTask() {
            override fun run() {
                requestPostNotificationsPermission()
            }
        }, 10_000)
    }

    private fun notifySynced() {
        homeViewModel.enableAllSources()
        headerTextRes = R.string.welcome_synced_header
        showProgress = false
        showSyncedIcon = true
        showErrorIcon = false
        showRetry = false
        errorText = ""
        allowNext()
        requestPostNotificationsPermission()
    }

    private fun notifyError(error: HostError) {
        val errorMessage = getString(error.messageKey)
        errorText = getString(R.string.welcome_sync_error, errorMessage)
        showProgress = false
        showSyncedIcon = false
        showErrorIcon = true
        showRetry = true
    }

    private fun retry() {
        showErrorIcon = false
        showRetry = false
        errorText = ""
        showProgress = true
        homeViewModel.sync()
    }

    private fun requestPostNotificationsPermission() {
        if (SDK_INT >= TIRAMISU && requestPostNotificationsPermission) {
            requestPostNotificationsPermission = false
            permissionLauncher.launch(POST_NOTIFICATIONS)
        }
    }
}

@Composable
private fun WelcomeSyncScreen(
    headerTextRes: Int,
    showProgress: Boolean,
    showSyncedIcon: Boolean,
    showErrorIcon: Boolean,
    showRetry: Boolean,
    errorText: String,
    showNotificationsText: Boolean,
    onRetry: () -> Unit
) {
    ExpressivePage {
        Spacer(modifier = Modifier.size(32.dp))

        AnimatedContent(
            targetState = Triple(showProgress, showSyncedIcon, showErrorIcon),
            transitionSpec = {
                fadeIn(animationSpec = tween(220, delayMillis = 90)) togetherWith
                        fadeOut(animationSpec = tween(90))
            },
            label = "statusIconTransition"
        ) { (progress, synced, error) ->
            when {
                progress -> CircularProgressIndicator(
                    modifier = Modifier.size(120.dp),
                    strokeWidth = 8.dp,
                    strokeCap = StrokeCap.Round,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                synced -> Icon(
                    painter = painterResource(R.drawable.baseline_check_24),
                    contentDescription = stringResource(R.string.welcome_sync_done_logo),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(120.dp)
                )
                error -> Icon(
                    painter = painterResource(R.drawable.ic_cloud_off_24dp),
                    contentDescription = stringResource(R.string.welcome_sync_error_logo),
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(120.dp)
                )
            }
        }

        AnimatedContent(
            targetState = headerTextRes,
            transitionSpec = {
                fadeIn(animationSpec = tween(220, delayMillis = 90)) togetherWith
                        fadeOut(animationSpec = tween(90))
            },
            label = "headerTransition"
        ) { targetHeader ->
            Text(
                text = stringResource(targetHeader),
                style = MaterialTheme.typography.displaySmall,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp)
            )
        }

        ExpressiveSection(
            modifier = Modifier.padding(top = 32.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.welcome_sync_summary),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                if (showRetry) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .padding(top = 24.dp)
                            .clickable(onClick = onRetry)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_sync_24dp),
                            contentDescription = stringResource(R.string.welcome_sync_retry_logo),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = errorText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                        )
                    }
                }
            }
        }

        if (showNotificationsText) {
            Text(
                text = stringResource(R.string.welcome_sync_notifications),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, bottom = 16.dp)
            )
        }
    }
}
