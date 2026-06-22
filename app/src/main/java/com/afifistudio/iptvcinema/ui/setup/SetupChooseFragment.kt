package com.afifistudio.iptvcinema.ui.setup

import android.os.Bundle
import androidx.leanback.app.GuidedStepSupportFragment
import androidx.leanback.widget.GuidanceStylist
import androidx.leanback.widget.GuidedAction
import com.afifistudio.iptvcinema.R
import com.afifistudio.iptvcinema.ui.setup.SetupGuidance

class SetupChooseFragment : GuidedStepSupportFragment() {

    override fun onCreateGuidance(savedInstanceState: Bundle?): GuidanceStylist.Guidance {
        return SetupGuidance.create(
            requireContext(),
            getString(R.string.setup_title),
            getString(R.string.setup_choose_type),
        )
    }

    override fun onCreateActions(actions: MutableList<GuidedAction>, savedInstanceState: Bundle?) {
        actions.add(
            GuidedAction.Builder(requireContext())
                .id(ACTION_M3U)
                .title(getString(R.string.setup_m3u))
                .build(),
        )
        actions.add(
            GuidedAction.Builder(requireContext())
                .id(ACTION_M3U_FILE)
                .title(getString(R.string.setup_m3u_file))
                .build(),
        )
        actions.add(
            GuidedAction.Builder(requireContext())
                .id(ACTION_XTREAM)
                .title(getString(R.string.setup_xtream))
                .build(),
        )
    }

    override fun onGuidedActionClicked(action: GuidedAction) {
        when (action.id) {
            ACTION_M3U -> add(
                parentFragmentManager,
                SetupM3uFragment(),
            )
            ACTION_M3U_FILE -> add(
                parentFragmentManager,
                SetupM3uFileFragment(),
            )
            ACTION_XTREAM -> add(
                parentFragmentManager,
                SetupXtreamFragment(),
            )
        }
    }

    companion object {
        private const val ACTION_M3U = 1L
        private const val ACTION_M3U_FILE = 3L
        private const val ACTION_XTREAM = 2L
    }
}
