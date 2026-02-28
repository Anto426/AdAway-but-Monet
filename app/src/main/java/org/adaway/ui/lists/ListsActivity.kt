package org.adaway.ui.lists

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import org.adaway.R
import org.adaway.helper.ThemeHelper
import org.adaway.ui.adblocking.ApplyConfigurationSnackbar
import org.adaway.ui.compose.ExpressiveAppContainer
import org.adaway.ui.compose.ExpressiveScaffold

/**
 * This activity display hosts list items.
 */
class ListsActivity : AppCompatActivity() {
    private lateinit var listsViewModel: ListsViewModel
    private lateinit var pagerAdapter: ListsFragmentPagerAdapter
    private lateinit var onBackPressedCallback: OnBackPressedCallback
    private var viewPager: ViewPager2? = null
    private var currentTab by mutableIntStateOf(BLOCKED_HOSTS_TAB)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        ThemeHelper.applyTheme(this)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        listsViewModel = ViewModelProvider(this)[ListsViewModel::class.java]
        pagerAdapter = ListsFragmentPagerAdapter(this)
        
        currentTab = intent.getIntExtra(TAB, BLOCKED_HOSTS_TAB)
        bindBackPress()

        setContent {
            ExpressiveAppContainer {
                ExpressiveScaffold(
                    bottomBar = {
                        ListsBottomNavigation(
                            selectedTab = currentTab,
                            onTabSelected = { tab ->
                                currentTab = tab
                                viewPager?.setCurrentItem(tab, true)
                            }
                        )
                    },
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = { pagerAdapter.addItem(currentTab) },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            shape = MaterialTheme.shapes.large
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_add_black_24px),
                                contentDescription = stringResource(R.string.lists_add)
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        ListsScreen(
                            onPagerReady = { pager ->
                                viewPager = pager
                                pager.adapter = pagerAdapter
                                pager.setCurrentItem(currentTab, false)
                                pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                                    override fun onPageSelected(position: Int) {
                                        currentTab = position
                                        pagerAdapter.ensureActionModeCanceled()
                                    }
                                })
                            }
                        )
                    }
                }
            }
        }
        
        // Use a hidden view or coordinator for the snackbar as before if needed, 
        // but let's try to bind it to the root later if possible.
        // For now, keep the ViewModel logic.
        val applySnackbar = ApplyConfigurationSnackbar(window.decorView, false, false)
        listsViewModel.modelChanged.observe(this, applySnackbar.createObserver())
        handleQuery(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleQuery(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.list_menu, menu)
        val searchManager = getSystemService(SEARCH_SERVICE) as? SearchManager
        val actionView = menu.findItem(R.id.menu_search).actionView
        val searchView = actionView as? SearchView
        if (searchManager != null && searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
            searchView.setIconifiedByDefault(false)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_toggle_source) {
            if (::listsViewModel.isInitialized) {
                listsViewModel.toggleSources()
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun bindBackPress() {
        onBackPressedCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                if (::listsViewModel.isInitialized) {
                    listsViewModel.clearSearch()
                }
                onBackPressedCallback.isEnabled = false
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun handleQuery(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEARCH) {
            val query = intent.getStringExtra(SearchManager.QUERY)
            if (::listsViewModel.isInitialized) {
                listsViewModel.search(query)
                onBackPressedCallback.isEnabled = true
            }
        }
    }

    companion object {
        const val TAB: String = "org.adaway.lists.tab"
        const val BLOCKED_HOSTS_TAB: Int = 0
        const val ALLOWED_HOSTS_TAB: Int = 1
        const val REDIRECTED_HOSTS_TAB: Int = 2
    }
}

@Composable
private fun ListsBottomNavigation(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = selectedTab == ListsActivity.BLOCKED_HOSTS_TAB,
            onClick = { onTabSelected(ListsActivity.BLOCKED_HOSTS_TAB) },
            icon = { Icon(painterResource(R.drawable.baseline_block_24), null) },
            label = { Text(stringResource(R.string.blocked_hosts_label)) }
        )
        NavigationBarItem(
            selected = selectedTab == ListsActivity.ALLOWED_HOSTS_TAB,
            onClick = { onTabSelected(ListsActivity.ALLOWED_HOSTS_TAB) },
            icon = { Icon(painterResource(R.drawable.baseline_check_24), null) },
            label = { Text(stringResource(R.string.allowed_hosts_label)) }
        )
        NavigationBarItem(
            selected = selectedTab == ListsActivity.REDIRECTED_HOSTS_TAB,
            onClick = { onTabSelected(ListsActivity.REDIRECTED_HOSTS_TAB) },
            icon = { Icon(painterResource(R.drawable.baseline_compare_arrows_24), null) },
            label = { Text(stringResource(R.string.redirect_hosts_label)) }
        )
    }
}

@Composable
private fun ListsScreen(onPagerReady: (ViewPager2) -> Unit) {
    AndroidView(
        factory = { context ->
            ViewPager2(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                onPagerReady(this)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
