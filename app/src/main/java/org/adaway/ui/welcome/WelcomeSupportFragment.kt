package org.adaway.ui.welcome

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.adaway.R
import org.adaway.helper.PreferenceHelper
import org.adaway.ui.compose.AdAwayExpressiveTheme
import org.adaway.ui.compose.ExpressivePage
import org.adaway.ui.compose.ExpressiveSection
import org.adaway.ui.support.SupportActivity
import org.adaway.util.log.SentryLog

/**
 * This class is a fragment to inform user how to support the application development.
 */
class WelcomeSupportFragment : WelcomeFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val showSponsorship = SentryLog.isStub()
        val telemetryEnabled = PreferenceHelper.getTelemetryEnabled(requireContext())

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AdAwayExpressiveTheme {
                    WelcomeSupportContent(
                        showSponsorship = showSponsorship,
                        initialTelemetryEnabled = telemetryEnabled,
                        onSupportClick = { openLink(SupportActivity.SUPPORT_LINK) },
                        onSponsorshipClick = { openLink(SupportActivity.SPONSORSHIP_LINK) },
                        onTelemetryChanged = { enabled ->
                            PreferenceHelper.setTelemetryEnabled(requireContext(), enabled)
                            SentryLog.setEnabled(requireActivity().application, enabled)
                        }
                    )
                }
            }
        }
    }

    override fun canGoNext(): Boolean {
        return true
    }

    private fun openLink(uri: Uri) {
        startActivity(Intent(Intent.ACTION_VIEW, uri))
    }
}

@Composable
private fun WelcomeSupportContent(
    showSponsorship: Boolean,
    initialTelemetryEnabled: Boolean,
    onSupportClick: () -> Unit,
    onSponsorshipClick: () -> Unit,
    onTelemetryChanged: (Boolean) -> Unit
) {
    val heartTransition = rememberInfiniteTransition(label = "welcomeHeart")
    val heartScale by heartTransition.animateFloat(
        initialValue = 1F,
        targetValue = 1.25F,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "welcomeHeartScale"
    )
    var telemetryEnabled by remember(initialTelemetryEnabled) {
        mutableStateOf(initialTelemetryEnabled)
    }

    ExpressivePage {
        Icon(
            painter = painterResource(R.drawable.baseline_favorite_24),
            contentDescription = stringResource(R.string.welcome_support_logo),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(top = 16.dp)
                .size(100.dp)
                .scale(heartScale)
                .clickable(onClick = onSupportClick)
        )

        Text(
            text = stringResource(R.string.welcome_support_header),
            style = MaterialTheme.typography.displaySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp)
        )

        Text(
            text = stringResource(R.string.welcome_support_summary),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        WelcomeSupportActionCard(
            label = stringResource(R.string.welcome_support_button),
            icon = {
                Image(
                    painter = painterResource(R.drawable.paypal),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
            },
            onClick = onSupportClick
        )

        if (showSponsorship) {
            WelcomeSupportActionCard(
                label = stringResource(R.string.support_sponsorship_button),
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_github_32dp),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(28.dp)
                    )
                },
                onClick = onSponsorshipClick
            )
        } else {
            ExpressiveSection(
                modifier = Modifier.padding(top = 24.dp),
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
                    Text(
                        text = stringResource(R.string.welcome_support_telemetry_summary),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .clickable {
                                telemetryEnabled = !telemetryEnabled
                                onTelemetryChanged(telemetryEnabled)
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Checkbox(
                            checked = telemetryEnabled,
                            onCheckedChange = { checked ->
                                telemetryEnabled = checked
                                onTelemetryChanged(checked)
                            }
                        )
                        Text(
                            text = stringResource(R.string.welcome_support_telemetry_button),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun WelcomeSupportActionCard(
    label: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    ExpressiveSection(
        modifier = Modifier.clickable(onClick = onClick),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Spacer(modifier = Modifier.size(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
