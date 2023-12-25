package icsdiscover.coingecko

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import icsdiscover.coingecko.databinding.ActivityMainBinding
import icsdiscover.coingecko.ui.CoinGeckoFragment
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val TIMER = 60

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarMain.toolbar)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val result = super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.action_options, menu)
        return result
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                val navHostFragment = supportFragmentManager.fragments.first() as? NavHostFragment
                if (navHostFragment != null) {
                    val childFragments = navHostFragment.childFragmentManager.fragments
                    runOnUiThread {
                        childFragments.forEach { fragment ->
                            if (fragment is CoinGeckoFragment) {
                                fragment.setupSearch(item)
                            }
                        }
                    }
                }
                true
            }

            R.id.action_signout -> {
                finishAndRemoveTask()
                true
            }

            R.id.action_refresh -> {
                reload()
                true
            }

            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onResume() {
        super.onResume()

        GlobalScope.launch {
            var i = TIMER
            //runOnUiThread {
            while (i >= 0) {
                delay(1000L)
                runOnUiThread {
                    title =
                        resources.getString(R.string.app_name) + (if (i == 0) " (Refreshing ...)" else " (" + i + " secs)")
                }
                if (i == 0) {
                    i = TIMER
                    reload()
                } else
                    i--
            }
        }
    }

    private fun reload() {
        val navHostFragment = supportFragmentManager.fragments.first() as? NavHostFragment
        if (navHostFragment != null) {
            val childFragments = navHostFragment.childFragmentManager.fragments
            runOnUiThread {
                childFragments.forEach { fragment ->
                    if (fragment is CoinGeckoFragment) {
                        fragment.reload()
                    }
                }
            }
        }
    }
}