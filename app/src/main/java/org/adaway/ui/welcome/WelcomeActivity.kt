package org.adaway.ui.welcome

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.viewpager2.widget.ViewPager2
import org.adaway.R
import org.adaway.ui.compose.ExpressiveAppContainer
import org.adaway.ui.home.HomeActivity

/**
 * This class is a welcome activity to run first time setup on the user device.
 */
class WelcomeActivity : AppCompatActivity(), WelcomeNavigable {
    private lateinit var pagerAdapter: WelcomePagerAdapter
    private var currentPage by mutableIntStateOf(0)
    private var showBackButton by mutableStateOf(false)
    private var showNextButton by mutableStateOf(false)
    private var nextButtonTextRes by mutableIntStateOf(R.string.welcome_next_button)
    private var viewPager: ViewPager2? = null
    private lateinit var onBackPressedCallback: OnBackPressedCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        pagerAdapter = WelcomePagerAdapter(this)
        bindBackPress()
        setContent {
            ExpressiveAppContainer {
                WelcomeActivityScreen(
                    currentPage = currentPage,
                    pageCount = pagerAdapter.itemCount,
                    showBackButton = showBackButton,
                    showNextButton = showNextButton,
                    nextButtonTextRes = nextButtonTextRes,
                    onBack = ::goBack,
                    onNext = ::goNext,
                    onPagerReady = ::setupPager
                )
            }
        }
    }

    private fun bindBackPress() {
        onBackPressedCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                goBack()
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun setupPager(pager: ViewPager2) {
        if (viewPager != null) {
            return
        }
        viewPager = pager
        pager.adapter = pagerAdapter
        pager.isUserInputEnabled = false
        pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                currentPage = position
                onBackPressedCallback.isEnabled = position > 0
            }
        })
    }

    override fun allowNext() {
        nextButtonTextRes = if (currentPage == pagerAdapter.itemCount - 1) {
            R.string.welcome_finish_button
        } else {
            R.string.welcome_next_button
        }
        showNextButton = true
    }

    override fun blockNext() {
        showNextButton = false
    }

    private fun allowBack() {
        showBackButton = true
    }

    private fun blockBack() {
        showBackButton = false
    }

    private fun goNext() {
        val pageCount = pagerAdapter.itemCount
        if (currentPage >= pageCount - 1) {
            startHomeActivity()
            return
        }
        val nextPage = currentPage + 1
        currentPage = nextPage
        viewPager?.setCurrentItem(nextPage, true)
        allowBack()
        if (pagerAdapter.createFragment(nextPage).canGoNext()) {
            allowNext()
        } else {
            blockNext()
        }
    }

    private fun goBack() {
        if (currentPage == 0) {
            return
        }
        val previousPage = currentPage - 1
        currentPage = previousPage
        viewPager?.setCurrentItem(previousPage, true)
        if (previousPage == 0) {
            blockBack()
        }
        allowNext()
    }

    private fun startHomeActivity() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}

@Composable
private fun WelcomeActivityScreen(
    currentPage: Int,
    pageCount: Int,
    showBackButton: Boolean,
    showNextButton: Boolean,
    nextButtonTextRes: Int,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onPagerReady: (ViewPager2) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        AndroidView(
            factory = { context ->
                ViewPager2(context).apply {
                    onPagerReady(this)
                }
            },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(modifier = Modifier.width(100.dp)) {
                if (showBackButton) {
                    TextButton(onClick = onBack) {
                        Text(
                            text = stringResource(R.string.welcome_back_button),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pageCount) { index ->
                    val selected = index == currentPage
                    val dotColor by animateColorAsState(
                        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        label = "dotColor"
                    )
                    val dotWidth by animateDpAsState(
                        targetValue = if (selected) 24.dp else 8.dp,
                        label = "dotWidth"
                    )
                    
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(width = dotWidth, height = 8.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                            .semantics {
                                contentDescription = "Step ${index + 1}"
                            }
                    )
                }
            }

            Box(modifier = Modifier.width(100.dp), contentAlignment = Alignment.CenterEnd) {
                if (showNextButton) {
                    TextButton(onClick = onNext) {
                        Text(
                            text = stringResource(nextButtonTextRes),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
