package org.adaway.ui.help

import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.adaway.R
import org.adaway.helper.ThemeHelper
import org.adaway.ui.compose.ExpressiveAppContainer
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

/**
 * Help screen with HTML tabs.
 */
class HelpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        ThemeHelper.applyTheme(this)

        val actionBar: ActionBar? = supportActionBar
        actionBar?.setDisplayShowTitleEnabled(true)
        actionBar?.setDisplayHomeAsUpEnabled(true)

        setContent {
            ExpressiveAppContainer {
                HelpScreen()
            }
        }
    }
}

private data class HelpTab(@StringRes val titleRes: Int, @RawRes val rawRes: Int)

private val helpTabs = listOf(
    HelpTab(R.string.help_tab_faq, R.raw.help_faq),
    HelpTab(R.string.help_tab_problems, R.raw.help_problems),
    HelpTab(R.string.help_tab_s_on_s_off, R.raw.help_s_on_s_off)
)

@Composable
private fun HelpScreen() {
    val context = LocalContext.current
    val tabContents = remember {
        helpTabs.map { tab ->
            Html.fromHtml(readRawResource(context, tab.rawRes), Html.FROM_HTML_MODE_LEGACY)
        }
    }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                if (selectedTab < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            divider = {}
        ) {
            helpTabs.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = context.getString(tab.titleRes),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }
        HelpHtmlView(
            html = tabContents[selectedTab],
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )
    }
}

@Composable
private fun HelpHtmlView(html: Spanned, modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            val density = context.resources.displayMetrics.density
            val margin = (24 * density).toInt()
            val textView = TextView(context).apply {
                setTextIsSelectable(false)
                movementMethod = LinkMovementMethod.getInstance()
                setPadding(margin, margin, margin, margin)
                // Set text color to match onSurface if possible, 
                // but Html.fromHtml might have its own colors.
            }
            ScrollView(context).apply {
                isFillViewport = true
                addView(
                    textView,
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                )
                clipToPadding = false
            }
        },
        update = { scrollView ->
            val textView = scrollView.getChildAt(0) as TextView
            textView.text = html
        }
    )
}

private fun readRawResource(context: android.content.Context, @RawRes resourceId: Int): String {
    context.resources.openRawResource(resourceId).use { inputStream: InputStream ->
        BufferedReader(InputStreamReader(inputStream)).use { reader ->
            val content = StringBuilder()
            var line: String? = reader.readLine()
            while (line != null) {
                content.append(line)
                line = reader.readLine()
            }
            return content.toString()
        }
    }
}
