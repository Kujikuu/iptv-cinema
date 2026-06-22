package com.afifistudio.iptvcinema.ui.settings

import android.content.Intent
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.afifistudio.iptvcinema.BuildConfig
import com.afifistudio.iptvcinema.R
import com.afifistudio.iptvcinema.data.prefs.AppPreferences
import com.afifistudio.iptvcinema.data.prefs.RefreshInterval
import com.afifistudio.iptvcinema.databinding.FragmentSettingsBinding
import com.afifistudio.iptvcinema.ui.ContentFocusHandler
import com.afifistudio.iptvcinema.ui.HomeChromeHost
import com.afifistudio.iptvcinema.ui.common.TvFocusCoordinator
import com.afifistudio.iptvcinema.ui.registerContentFocusHandler
import com.afifistudio.iptvcinema.ui.requestChromeFocus
import com.afifistudio.iptvcinema.ui.unregisterContentFocusHandler
import com.afifistudio.iptvcinema.ui.browse.BrowseViewModel
import com.afifistudio.iptvcinema.ui.library.LibraryListFragment
import com.afifistudio.iptvcinema.ui.library.LibraryType
import com.afifistudio.iptvcinema.ui.replaceContent
import com.afifistudio.iptvcinema.ui.setup.SetupActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    @Inject
    lateinit var appPreferences: AppPreferences

    private val browseViewModel: BrowseViewModel by viewModels({ requireActivity() })
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val contentFocusHandler = object : ContentFocusHandler {
        override fun requestInitialFocus(): Boolean {
            binding.settingsRefreshInterval.requestFocus()
            return true
        }

        override fun canFocusUpToChrome(): Boolean = true
    }

    private val setupLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        browseViewModel.loadInitial()
        renderRefreshSettings()
        renderSourceActions()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.settingsVersion.text = getString(R.string.settings_version, BuildConfig.VERSION_NAME)
        binding.settingsRefresh.setOnClickListener { browseViewModel.refreshCurrentSource() }
        binding.settingsRefreshInterval.setOnClickListener {
            appPreferences.cycleRefreshInterval()
            renderRefreshSettings()
        }
        binding.settingsSwitchSource.setOnClickListener { browseViewModel.cycleSource() }
        binding.settingsDeleteSource.setOnClickListener { confirmDeleteSource() }
        binding.settingsAddSource.setOnClickListener {
            setupLauncher.launch(Intent(requireContext(), SetupActivity::class.java))
        }
        binding.settingsClearHistory.setOnClickListener { confirmClearHistory() }
        binding.settingsContinue.setOnClickListener {
            replaceContent(LibraryListFragment.newInstance(LibraryType.CONTINUE_WATCHING))
        }
        binding.settingsFavorites.setOnClickListener {
            replaceContent(LibraryListFragment.newInstance(LibraryType.FAVORITES))
        }
        binding.settingsAutoplayNextEpisode.setOnClickListener {
            appPreferences.toggleAutoplayNextEpisode()
            renderAutoplaySettings()
        }
        renderRefreshSettings()
        renderAutoplaySettings()
        observeState()
        wireFocusNavigation()
        (activity as? HomeChromeHost)?.setChromeVisible(true)
        view.post { binding.settingsRefreshInterval.requestFocus() }
    }

    override fun onResume() {
        super.onResume()
        registerContentFocusHandler(contentFocusHandler)
    }

    override fun onPause() {
        unregisterContentFocusHandler()
        super.onPause()
    }

    private fun wireFocusNavigation() {
        TvFocusCoordinator.wireViewUp(
            binding.settingsRefreshInterval,
            canFocusUpProvider = { true },
            requestChromeFocus = { requestChromeFocus() },
        )
        TvFocusCoordinator.wireScrollableFocusScroll(
            binding.settingsScroll,
            binding.settingsRefreshInterval,
            binding.settingsRefresh,
            binding.settingsSwitchSource,
            binding.settingsDeleteSource,
            binding.settingsAddSource,
            binding.settingsContinue,
            binding.settingsFavorites,
            binding.settingsClearHistory,
            binding.settingsAutoplayNextEpisode,
        )
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                browseViewModel.uiState.collect { state ->
                    binding.settingsActiveSource.text = getString(
                        R.string.settings_active_source,
                        state.activeSourceName.ifBlank { getString(R.string.app_name) },
                    )
                    binding.settingsLastRefresh.text = getString(
                        R.string.settings_last_refresh,
                        formatLastRefresh(state.sourceUpdatedAt),
                    )
                    renderSourceActions(state.sources.size)
                }
            }
        }
    }

    private fun renderSourceActions(sourceCount: Int = browseViewModel.uiState.value.sources.size) {
        val hasMultipleSources = sourceCount > 1
        binding.settingsSwitchSource.isEnabled = hasMultipleSources
        binding.settingsSwitchSource.alpha = if (hasMultipleSources) 1f else 0.5f
        binding.settingsSwitchSource.text = if (hasMultipleSources) {
            getString(R.string.settings_switch_source)
        } else {
            getString(R.string.settings_switch_source_single)
        }
        binding.settingsDeleteSource.isEnabled = sourceCount > 0
        binding.settingsDeleteSource.alpha = if (sourceCount > 0) 1f else 0.5f
    }

    private fun confirmDeleteSource() {
        val sourceId = browseViewModel.uiState.value.selectedSourceId ?: return
        val sourceName = browseViewModel.uiState.value.activeSourceName
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.settings_delete_source)
            .setMessage(getString(R.string.settings_delete_source_confirm, sourceName))
            .setPositiveButton(R.string.settings_delete_confirm) { _, _ ->
                browseViewModel.deleteSource(sourceId)
            }
            .setNegativeButton(R.string.setup_cancel, null)
            .show()
    }

    private fun confirmClearHistory() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.settings_clear_history)
            .setMessage(R.string.settings_clear_history_confirm)
            .setPositiveButton(R.string.settings_clear_confirm) { _, _ ->
                browseViewModel.clearWatchHistory()
            }
            .setNegativeButton(R.string.setup_cancel, null)
            .show()
    }

    private fun renderRefreshSettings() {
        binding.settingsRefreshInterval.text = getString(
            R.string.settings_refresh_interval,
            refreshIntervalLabel(appPreferences.getRefreshInterval()),
        )
    }

    private fun renderAutoplaySettings() {
        binding.settingsAutoplayNextEpisode.text = getString(
            if (appPreferences.isAutoplayNextEpisodeEnabled()) {
                R.string.settings_autoplay_next_episode_on
            } else {
                R.string.settings_autoplay_next_episode_off
            },
        )
    }

    private fun refreshIntervalLabel(interval: RefreshInterval): String = when (interval) {
        RefreshInterval.MANUAL -> getString(R.string.settings_refresh_manual)
        RefreshInterval.ONE_DAY -> getString(R.string.settings_refresh_every_days, 1)
        RefreshInterval.TWO_DAYS -> getString(R.string.settings_refresh_every_days, 2)
        RefreshInterval.SEVEN_DAYS -> getString(R.string.settings_refresh_every_days, 7)
    }

    private fun formatLastRefresh(updatedAt: Long): String {
        if (updatedAt <= 0L) return getString(R.string.settings_last_refresh_never)
        return DateUtils.getRelativeTimeSpanString(
            updatedAt,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_RELATIVE,
        ).toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
