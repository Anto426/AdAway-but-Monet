package org.adaway.ui.support

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.activity.compose.setContent
import org.adaway.R

/**
 * This class is an activity for users to show their supports to the project.
 */
class SupportActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                SupportContent(
                    onSupportClick = { openLink(SUPPORT_LINK) },
                    onSponsorshipClick = { openLink(SPONSORSHIP_LINK) }
                )
            }
        }
    }

    private fun openLink(uri: Uri) {
        startActivity(Intent(Intent.ACTION_VIEW, uri))
    }

    companion object {
        @JvmField
        val SUPPORT_LINK: Uri = Uri.parse("https://paypal.me/BruceBUJON")

        @JvmField
        val SPONSORSHIP_LINK: Uri = Uri.parse("https://github.com/sponsors/PerfectSlayer")

        @JvmStatic
        fun animateHeart(heartImageView: ImageView) {
            val growScaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1F, 1.2F)
            val growScaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1F, 1.2F)
            val growAnimator = ObjectAnimator.ofPropertyValuesHolder(heartImageView, growScaleX, growScaleY)
            growAnimator.duration = 200
            growAnimator.startDelay = 2000

            val shrinkScaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1.2F, 1F)
            val shrinkScaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.2F, 1F)
            val shrinkAnimator = ObjectAnimator.ofPropertyValuesHolder(heartImageView, shrinkScaleX, shrinkScaleY)
            growAnimator.duration = 400

            val animationSet = AnimatorSet()
            animationSet.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    animationSet.start()
                }
            })
            animationSet.playSequentially(growAnimator, shrinkAnimator)
            animationSet.start()
        }

        @JvmStatic
        fun bindLink(context: Context, view: View, uri: Uri) {
            view.setOnClickListener {
                val browserIntent = Intent(Intent.ACTION_VIEW, uri)
                context.startActivity(browserIntent)
            }
        }
    }
}

@Composable
private fun SupportContent(onSupportClick: () -> Unit, onSponsorshipClick: () -> Unit) {
    val heartTransition = rememberInfiniteTransition(label = "heart")
    val heartScale by heartTransition.animateFloat(
        initialValue = 1F,
        targetValue = 1.2F,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heartScale"
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colorResource(R.color.welcomeBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(R.drawable.baseline_favorite_24),
                contentDescription = stringResource(R.string.welcome_support_logo),
                tint = Color.White,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .size(128.dp)
                    .scale(heartScale)
                    .clickable(onClick = onSupportClick)
            )

            Text(
                text = stringResource(R.string.welcome_support_header),
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                modifier = Modifier.padding(top = 16.dp)
            )

            Text(
                text = stringResource(R.string.welcome_support_summary),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                modifier = Modifier.padding(top = 32.dp)
            )

            SupportActionCard(
                label = stringResource(R.string.welcome_support_button),
                icon = {
                    Image(
                        painter = painterResource(R.drawable.paypal),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                },
                onClick = onSupportClick
            )

            SupportActionCard(
                label = stringResource(R.string.support_sponsorship_button),
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_github_32dp),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(24.dp)
                    )
                },
                onClick = onSponsorshipClick
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SupportActionCard(
    label: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.cardBackground)),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SupportPreview() {
    MaterialTheme {
        SupportContent(onSupportClick = {}, onSponsorshipClick = {})
    }
}
