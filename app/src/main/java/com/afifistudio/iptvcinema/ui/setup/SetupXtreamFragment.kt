package com.afifistudio.iptvcinema.ui.setup

import android.os.Bundle
import android.text.InputType
import android.widget.Toast
import androidx.leanback.app.GuidedStepSupportFragment
import androidx.leanback.widget.GuidanceStylist
import androidx.leanback.widget.GuidedAction
import androidx.lifecycle.lifecycleScope
import com.afifistudio.iptvcinema.R
import com.afifistudio.iptvcinema.data.repository.SourceRepository
import com.afifistudio.iptvcinema.data.xtream.XtreamRepository
import com.afifistudio.iptvcinema.domain.model.SourceType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class SetupXtreamFragment : GuidedStepSupportFragment() {

    @Inject
    lateinit var sourceRepository: SourceRepository

    @Inject
    lateinit var xtreamRepository: XtreamRepository

    private var formState = XtreamFormState()
    private var statusMessage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.let { bundle ->
            formState = XtreamFormState(
                name = bundle.getString(STATE_NAME, SetupDefaults.XTREAM_NAME),
                serverUrl = bundle.getString(STATE_SERVER, SetupDefaults.XTREAM_SERVER),
                username = bundle.getString(STATE_USERNAME, SetupDefaults.XTREAM_USERNAME),
                password = bundle.getString(STATE_PASSWORD, SetupDefaults.XTREAM_PASSWORD),
            )
            statusMessage = bundle.getString(STATE_STATUS)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_NAME, formState.name)
        outState.putString(STATE_SERVER, formState.serverUrl)
        outState.putString(STATE_USERNAME, formState.username)
        outState.putString(STATE_PASSWORD, formState.password)
        outState.putString(STATE_STATUS, statusMessage)
    }

    override fun onCreateGuidance(savedInstanceState: Bundle?): GuidanceStylist.Guidance {
        val description = statusMessage ?: getString(R.string.setup_xtream_hint)
        return SetupGuidance.create(
            requireContext(),
            getString(R.string.setup_xtream),
            description,
        )
    }

    override fun onCreateActions(actions: MutableList<GuidedAction>, savedInstanceState: Bundle?) {
        actions.add(
            editableAction(
                id = ACTION_NAME,
                title = getString(R.string.setup_name),
                value = formState.name,
            ),
        )
        actions.add(
            editableAction(
                id = ACTION_SERVER,
                title = getString(R.string.setup_server_url),
                value = formState.serverUrl,
            ),
        )
        actions.add(
            editableAction(
                id = ACTION_USERNAME,
                title = getString(R.string.setup_username),
                value = formState.username,
            ),
        )
        actions.add(
            editableAction(
                id = ACTION_PASSWORD,
                title = getString(R.string.setup_password),
                value = formState.password,
                secret = true,
            ),
        )
        actions.add(
            GuidedAction.Builder(requireContext())
                .id(ACTION_SAVE)
                .title(getString(R.string.setup_finish))
                .build(),
        )
    }

    override fun onGuidedActionEdited(action: GuidedAction) {
        syncActionToForm(action)
    }

    override fun onGuidedActionEditedAndProceed(action: GuidedAction): Long {
        syncActionToForm(action)
        return GuidedAction.ACTION_ID_CURRENT
    }

    override fun onGuidedActionClicked(action: GuidedAction) {
        if (action.id != ACTION_SAVE) return

        syncAllActionsToForm()
        val validationError = validateForm()
        if (validationError != null) {
            showStatus(validationError)
            return
        }

        connect()
    }

    private fun connect() {
        val sourceName = formState.resolvedName()
        setLoading(true)
        showStatus(getString(R.string.setup_connecting))

        lifecycleScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    xtreamRepository.validateCredentials(
                        formState.serverUrl.trim(),
                        formState.username.trim(),
                        formState.password,
                    )
                    val sourceId = sourceRepository.addSource(
                        type = SourceType.XTREAM,
                        name = sourceName,
                        url = formState.serverUrl.trim(),
                        username = formState.username.trim(),
                        password = formState.password,
                    )
                    xtreamRepository.refreshSource(sourceId).getOrThrow()
                }
            }.onSuccess {
                (activity as? SetupActivity)?.finishSetup()
            }.onFailure { error ->
                setLoading(false)
                val message = error.message ?: getString(R.string.setup_error_auth)
                showStatus(getString(R.string.setup_error_auth_detail, message))
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun validateForm(): String? {
        if (formState.serverUrl.isBlank()) {
            return getString(R.string.setup_error_empty_server)
        }
        if (formState.username.isBlank()) {
            return getString(R.string.setup_error_empty_username)
        }
        if (formState.password.isBlank()) {
            return getString(R.string.setup_error_empty_password)
        }
        return null
    }

    private fun syncAllActionsToForm() {
        actions.forEach { action ->
            if (action.isEditable) {
                syncActionToForm(action)
            }
        }
    }

    private fun syncActionToForm(action: GuidedAction) {
        val text = SetupFormHelper.actionText(action)
        when (action.id) {
            ACTION_NAME -> formState.name = text
            ACTION_SERVER -> formState.serverUrl = text
            ACTION_USERNAME -> formState.username = text
            ACTION_PASSWORD -> formState.password = text
        }
    }

    private fun editableAction(
        id: Long,
        title: String,
        value: String,
        secret: Boolean = false,
    ): GuidedAction {
        val builder = GuidedAction.Builder(requireContext())
            .id(id)
            .title(title)
            .description(value)
            .descriptionEditable(true)
            .editable(true)
            .editInputType(
                if (secret) {
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                } else {
                    InputType.TYPE_CLASS_TEXT
                },
            )
        return builder.build()
    }

    private fun setLoading(loading: Boolean) {
        actionById(ACTION_SAVE)?.let { saveAction ->
            saveAction.title = if (loading) getString(R.string.loading) else getString(R.string.setup_finish)
            notifyActionChanged(actions.indexOf(saveAction))
        }
    }

    private fun showStatus(message: String) {
        statusMessage = message
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun actionById(actionId: Long): GuidedAction? =
        actions.find { it.id == actionId }

    companion object {
        private const val STATE_NAME = "state_name"
        private const val STATE_SERVER = "state_server"
        private const val STATE_USERNAME = "state_username"
        private const val STATE_PASSWORD = "state_password"
        private const val STATE_STATUS = "state_status"

        private const val ACTION_NAME = 20L
        private const val ACTION_SERVER = 21L
        private const val ACTION_USERNAME = 22L
        private const val ACTION_PASSWORD = 23L
        private const val ACTION_SAVE = 24L
    }
}
