package com.afifistudio.iptvcinema.ui.setup

import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
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
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@AndroidEntryPoint
class SetupM3uFileFragment : GuidedStepSupportFragment() {

    @Inject
    lateinit var sourceRepository: SourceRepository

    @Inject
    lateinit var m3uRepository: M3uRepository

    private var selectedFilePath: String? = null

    private val openDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) return@registerForActivityResult
        lifecycleScope.launch {
            runCatching {
                persistPlaylist(uri)
            }.onSuccess { path ->
                selectedFilePath = path
                actionById(ACTION_FILE)?.let { action ->
                    action.description = File(path).name
                    notifyActionChanged(actions.indexOf(action))
                }
            }.onFailure {
                showFieldError(ACTION_FILE, getString(R.string.setup_error_parse))
            }
        }
    }

    override fun onCreateGuidance(savedInstanceState: Bundle?): GuidanceStylist.Guidance {
        return SetupGuidance.create(
            requireContext(),
            getString(R.string.setup_m3u_file),
            getString(R.string.setup_m3u_file_hint),
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
                .id(ACTION_FILE)
                .title(getString(R.string.setup_choose_file))
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
        when (action.id) {
            ACTION_FILE -> openDocumentLauncher.launch(arrayOf("*/*", "application/vnd.apple.mpegurl", "text/plain"))
            ACTION_SAVE -> saveSource()
        }
    }

    private fun saveSource() {
        val name = SetupFormHelper.actionText(actionById(ACTION_NAME))
        val filePath = selectedFilePath
        if (filePath.isNullOrBlank()) {
            showFieldError(ACTION_FILE, getString(R.string.setup_error_empty_file))
            return
        }

        val sourceName = name.ifBlank { File(filePath).nameWithoutExtension.ifBlank { "IPTV" } }
        setLoading(true)
        lifecycleScope.launch {
            runCatching {
                m3uRepository.validateAndParse(filePath, SourceType.M3U_FILE)
                val sourceId = sourceRepository.addSource(
                    type = SourceType.M3U_FILE,
                    name = sourceName,
                    url = filePath,
                    username = null,
                    password = null,
                )
                m3uRepository.refreshSource(sourceId).getOrThrow()
            }.onSuccess {
                (activity as? SetupActivity)?.finishSetup()
            }.onFailure {
                setLoading(false)
                showFieldError(ACTION_FILE, getString(R.string.setup_error_parse))
            }
        }
    }

    private suspend fun persistPlaylist(uri: Uri): String {
        val context = requireContext()
        val playlistsDir = File(context.filesDir, "playlists").apply { mkdirs() }
        val target = File(playlistsDir, "import_${System.currentTimeMillis()}.m3u")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(target).use { output ->
                input.copyTo(output)
            }
        } ?: throw IllegalStateException("Could not read playlist file")
        return target.absolutePath
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
        private const val ACTION_NAME = 20L
        private const val ACTION_FILE = 21L
        private const val ACTION_SAVE = 22L
    }
}
