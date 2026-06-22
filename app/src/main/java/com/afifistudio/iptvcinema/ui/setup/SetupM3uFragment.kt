package com.afifistudio.iptvcinema.ui.setup

import android.os.Bundle
import androidx.leanback.app.GuidedStepSupportFragment
import androidx.leanback.widget.GuidanceStylist
import androidx.leanback.widget.GuidedAction
import androidx.lifecycle.lifecycleScope
import com.afifistudio.iptvcinema.R
import com.afifistudio.iptvcinema.data.m3u.M3uRepository
import com.afifistudio.iptvcinema.data.repository.SourceRepository
import com.afifistudio.iptvcinema.domain.model.SourceType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SetupM3uFragment : GuidedStepSupportFragment() {

    @Inject
    lateinit var sourceRepository: SourceRepository

    @Inject
    lateinit var m3uRepository: M3uRepository

    override fun onCreateGuidance(savedInstanceState: Bundle?): GuidanceStylist.Guidance {
        return SetupGuidance.create(
            requireContext(),
            getString(R.string.setup_m3u),
            getString(R.string.setup_url),
        )
    }

    override fun onCreateActions(actions: MutableList<GuidedAction>, savedInstanceState: Bundle?) {
        actions.add(
            GuidedAction.Builder(requireContext())
                .id(ACTION_NAME)
                .title(getString(R.string.setup_name))
                .descriptionEditable(true)
                .editable(true)
                .build(),
        )
        actions.add(
            GuidedAction.Builder(requireContext())
                .id(ACTION_URL)
                .title(getString(R.string.setup_url))
                .descriptionEditable(true)
                .editable(true)
                .build(),
        )
        actions.add(
            GuidedAction.Builder(requireContext())
                .id(ACTION_SAVE)
                .title(getString(R.string.setup_finish))
                .build(),
        )
    }

    override fun onGuidedActionClicked(action: GuidedAction) {
        if (action.id != ACTION_SAVE) return

        val name = SetupFormHelper.actionText(actionById(ACTION_NAME))
        val url = SetupFormHelper.actionText(actionById(ACTION_URL))
        val sourceName = name.ifBlank { SetupFormHelper.hostLabel(url).ifBlank { "IPTV" } }

        if (url.isBlank()) {
            showFieldError(ACTION_URL, getString(R.string.setup_error_empty_server))
            return
        }

        setLoading(true)
        lifecycleScope.launch {
            runCatching {
                m3uRepository.validateAndParse(url, SourceType.M3U_URL)
                val sourceId = sourceRepository.addSource(
                    type = SourceType.M3U_URL,
                    name = sourceName,
                    url = url,
                    username = null,
                    password = null,
                )
                m3uRepository.refreshSource(sourceId).getOrThrow()
            }.onSuccess {
                (activity as? SetupActivity)?.finishSetup()
            }.onFailure {
                setLoading(false)
                showFieldError(ACTION_URL, getString(R.string.setup_error_parse))
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        actionById(ACTION_SAVE)?.let { saveAction ->
            saveAction.title = if (loading) getString(R.string.loading) else getString(R.string.setup_finish)
            notifyActionChanged(actions.indexOf(saveAction))
        }
    }

    private fun showFieldError(actionId: Long, message: String) {
        actionById(actionId)?.let { action ->
            action.description = message
            notifyActionChanged(actions.indexOf(action))
        }
    }

    private fun actionById(actionId: Long): GuidedAction? =
        actions.find { it.id == actionId }

    companion object {
        private const val ACTION_NAME = 10L
        private const val ACTION_URL = 11L
        private const val ACTION_SAVE = 12L
    }
}
