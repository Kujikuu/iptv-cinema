package com.afifistudio.iptvcinema.ui

import android.content.Intent
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.format.DateFormat
import android.util.TypedValue
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.afifistudio.iptvcinema.ui.common.TvFocusCoordinator
import androidx.lifecycle.lifecycleScope
import coil.load
import com.afifistudio.iptvcinema.R
import com.afifistudio.iptvcinema.data.repository.SourceRepository
import com.afifistudio.iptvcinema.databinding.ActivityMainBinding
import com.afifistudio.iptvcinema.ui.launcher.LauncherHomeFragment
import com.afifistudio.iptvcinema.ui.search.ChannelSearchFragment
import com.afifistudio.iptvcinema.ui.settings.SettingsFragment
import com.afifistudio.iptvcinema.ui.setup.SetupActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), HomeChromeHost {

    @Inject
    lateinit var sourceRepository: SourceRepository

    private lateinit var binding: ActivityMainBinding
    private val clockHandler = Handler(Looper.getMainLooper())
    private var searchClickListener: (() -> Unit)? = null
    private var settingsClickListener: (() -> Unit)? = null
    private var contentFocusHandler: ContentFocusHandler? = null
    private var lastChromeFocusedViewId: Int = View.NO_ID
    private var lastBackdropImageUrl: String? = null

    private val clockRunnable = object : Runnable {
        override fun run() {
            updateClock()
            clockHandler.postDelayed(this, CLOCK_TICK_MS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.homeChrome.chromeSearch.setOnClickListener { searchClickListener?.invoke() }
        binding.homeChrome.chromeSettings.setOnClickListener { settingsClickListener?.invoke() }

        lifecycleScope.launch {
            if (!sourceRepository.hasSources()) {
                startActivity(Intent(this@MainActivity, SetupActivity::class.java))
                finish()
                return@launch
            }

            if (savedInstanceState == null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.main_container, LauncherHomeFragment())
                    .commitNow()
            }
        }

        wireDefaultChromeActions()
        wireChromeFocus()
        updateClock()
    }

    override fun onDestroy() {
        contentFocusHandler = null
        super.onDestroy()
    }

    override fun onStart() {
        super.onStart()
        clockHandler.post(clockRunnable)
    }

    override fun onStop() {
        super.onStop()
        clockHandler.removeCallbacks(clockRunnable)
    }

    private fun wireDefaultChromeActions() {
        searchClickListener = {
            replaceContentFromActivity(ChannelSearchFragment())
        }
        settingsClickListener = {
            replaceContentFromActivity(SettingsFragment())
        }
    }

    private fun replaceContentFromActivity(fragment: androidx.fragment.app.Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_container, fragment)
            .addToBackStack(null)
            .commit()
        supportFragmentManager.executePendingTransactions()
    }

    override fun setChromeVisible(visible: Boolean) {
        val visibility = if (visible) View.VISIBLE else View.GONE
        binding.homeChrome.root.visibility = visibility
    }

    override fun setChromeMode(mode: HomeChromeMode) {
        val chrome = binding.homeChrome
        when (mode) {
            HomeChromeMode.LAUNCHER -> {
                chrome.chromeLogo.visibility = View.VISIBLE
                chrome.chromeSearch.text = getString(R.string.search_hint)
                chrome.chromeSearch.minWidth =
                    resources.getDimensionPixelSize(R.dimen.chrome_search_min_width)
                chrome.chromeDate.visibility = View.VISIBLE
                chrome.chromeFooter.alpha = 1f
                chrome.chromeClock.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    resources.getDimension(R.dimen.chrome_clock_text_size),
                )
            }
            HomeChromeMode.BROWSE -> {
                chrome.chromeLogo.visibility = View.GONE
                chrome.chromeSearch.text = getString(R.string.search_short_hint)
                chrome.chromeSearch.minWidth =
                    resources.getDimensionPixelSize(R.dimen.chrome_search_browse_min_width)
                chrome.chromeDate.visibility = View.GONE
                chrome.chromeFooter.alpha = 0.85f
                chrome.chromeClock.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    resources.getDimension(R.dimen.chrome_clock_browse_text_size),
                )
            }
        }
    }

    override fun setChromeFooterVisible(visible: Boolean) {
        binding.homeChrome.chromeFooter.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun setBackdropImageUrl(url: String?) {
        val normalizedUrl = url?.takeIf { it.isNotBlank() }
        if (normalizedUrl == lastBackdropImageUrl) return
        lastBackdropImageUrl = normalizedUrl
        if (normalizedUrl == null) {
            clearBackdropBlur()
            binding.backdropImage.setImageResource(R.drawable.launcher_backdrop_fallback)
            return
        }
        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels
        binding.backdropImage.load(normalizedUrl) {
            crossfade(false)
            size(width, height)
            placeholder(R.drawable.launcher_backdrop_fallback)
            error(R.drawable.launcher_backdrop_fallback)
            listener(
                onSuccess = { _, _ -> applyBackdropBlur() },
                onError = { _, _ -> clearBackdropBlur() },
            )
        }
    }

    private fun applyBackdropBlur() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            binding.backdropImage.setRenderEffect(
                RenderEffect.createBlurEffect(48f, 48f, Shader.TileMode.CLAMP),
            )
        }
    }

    private fun clearBackdropBlur() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            binding.backdropImage.setRenderEffect(null)
        }
    }

    override fun setOnSearchClickListener(listener: (() -> Unit)?) {
        searchClickListener = listener ?: {
            replaceContentFromActivity(ChannelSearchFragment())
        }
    }

    override fun setOnSettingsClickListener(listener: (() -> Unit)?) {
        settingsClickListener = listener ?: {
            replaceContentFromActivity(SettingsFragment())
        }
    }

    override fun registerContentFocusHandler(handler: ContentFocusHandler?) {
        contentFocusHandler = handler
    }

    override fun requestChromeFocus(): Boolean {
        val chrome = binding.homeChrome
        if (chrome.root.visibility != View.VISIBLE) return false
        contentFocusHandler?.onChromeFocusGained()
        val target = when (lastChromeFocusedViewId) {
            R.id.chrome_settings -> chrome.chromeSettings
            else -> chrome.chromeSearch
        }
        return target.requestFocus()
    }

    override fun requestContentFocus(): Boolean {
        contentFocusHandler?.onChromeFocusLost()
        return contentFocusHandler?.requestInitialFocus() == true
    }

    private fun wireChromeFocus() {
        val chrome = binding.homeChrome
        TvFocusCoordinator.wireChromeHorizontal(chrome.chromeSearch, chrome.chromeSettings)
        chrome.chromeSearch.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) lastChromeFocusedViewId = R.id.chrome_search
        }
        chrome.chromeSettings.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) lastChromeFocusedViewId = R.id.chrome_settings
        }
        TvFocusCoordinator.wireChromeDown(
            chrome.chromeSearch,
            chrome.chromeSettings,
        ) { requestContentFocus() }
    }

    private fun updateClock() {
        val now = Calendar.getInstance()
        binding.homeChrome.chromeClock.text =
            DateFormat.getTimeFormat(this).format(now.time)
        binding.homeChrome.chromeDate.text =
            DateFormat.format("EEEE, MMM d", now)
    }

    companion object {
        private const val CLOCK_TICK_MS = 30_000L
    }
}
