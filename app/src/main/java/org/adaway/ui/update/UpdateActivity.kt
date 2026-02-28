package org.adaway.ui.update

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import org.adaway.R
import org.adaway.helper.ThemeHelper
import org.adaway.model.update.Manifest
import org.adaway.ui.compose.ExpressiveAppContainer
import org.adaway.ui.compose.ExpressivePage
import org.adaway.ui.compose.ExpressiveSection
import org.adaway.ui.support.SupportActivity

/**
 * This class is the application update activity.
 */
class UpdateActivity : AppCompatActivity() {
    private lateinit var updateViewModel: UpdateViewModel
    private var screenState by mutableStateOf(UpdateScreenState())

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        ThemeHelper.applyTheme(this)

        updateViewModel = ViewModelProvider(this)[UpdateViewModel::class.java]
        bindManifest()
        bindProgress()

        setContent {
            ExpressiveAppContainer {
                UpdateScreen(
                    state = screenState,
                    onUpdate = ::startUpdate,
                    onDonate = { openLink(SupportActivity.SUPPORT_LINK) },
                    onSponsor = { openLink(SupportActivity.SPONSORSHIP_LINK) }
                )
            }
        }
    }

    private fun bindManifest() {
        updateViewModel.appManifest.observe(this) { manifest: Manifest ->
            screenState = screenState.copy(
                changelog = manifest.changelog,
                headerRes = if (manifest.updateAvailable) {
                    R.string.update_update_available_header
                } else {
                    R.string.update_up_to_date_header
                },
                showUpdateButton = manifest.updateAvailable && !screenState.showProgress
            )
        }
    }

    private fun bindProgress() {
        updateViewModel.downloadProgress.observe(this) { progress ->
            if (progress == null) {
                screenState = screenState.copy(
                    showProgress = false,
                    showUpdateButton = screenState.showUpdateButton
                )
                return@observe
            }
            screenState = screenState.copy(
                showProgress = true,
                showUpdateButton = false,
                progress = progress.progress,
                progressLabel = progress.format(this)
            )
        }
    }

    private fun startUpdate() {
        screenState = screenState.copy(showUpdateButton = false, showProgress = true)
        updateViewModel.update()
    }

    private fun openLink(uri: Uri) {
        startActivity(Intent(Intent.ACTION_VIEW, uri))
    }
}

private data class UpdateScreenState(
    @param:StringRes @field:StringRes val headerRes: Int = R.string.update_up_to_date_header,
    val changelog: String = "",
    val showUpdateButton: Boolean = false,
    val showProgress: Boolean = false,
    val progress: Int = 0,
    val progressLabel: String = ""
)

@Composable
private fun UpdateScreen(
    state: UpdateScreenState,
    onUpdate: () -> Unit,
    onDonate: () -> Unit,
    onSponsor: () -> Unit
) {
    ExpressivePage {
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = stringResource(R.string.app_logo),
                modifier = Modifier.size(140.dp)
            )

        Spacer(modifier = Modifier.height(24.dp))

        AnimatedContent(
            targetState = state.headerRes,
            transitionSpec = {
                fadeIn(animationSpec = tween(220, delayMillis = 90)) togetherWith
                        fadeOut(animationSpec = tween(90))
            },
            label = "headerTransition"
        ) { targetHeader ->
            Text(
                text = stringResource(targetHeader),
                style = MaterialTheme.typography.displaySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (state.showUpdateButton) {
            Button(
                onClick = onUpdate,
                modifier = Modifier.padding(top = 24.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = stringResource(R.string.update_update_button),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (state.showProgress) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LinearProgressIndicator(
                    progress = { state.progress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp),
                    strokeCap = StrokeCap.Round,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                Text(
                    text = state.progressLabel,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 12.dp),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        ExpressiveSection(
            modifier = Modifier.padding(top = 32.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = stringResource(R.string.update_last_changelog_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = state.changelog,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                )
            }
        }

        ExpressiveSection(
            modifier = Modifier.padding(top = 16.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = stringResource(R.string.update_support_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDonate,
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Image(
                            painter = painterResource(R.drawable.paypal),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(text = stringResource(R.string.update_donate_button))
                    }
                    Button(
                        onClick = onSponsor,
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_github_24dp),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(text = stringResource(R.string.update_sponsor_button))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
