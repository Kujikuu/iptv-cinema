package com.afifistudio.iptvcinema.ui.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.afifistudio.iptvcinema.R
import com.afifistudio.iptvcinema.databinding.FragmentChannelDetailsBinding
import com.afifistudio.iptvcinema.domain.model.Channel
import com.afifistudio.iptvcinema.domain.model.ContentType
import com.afifistudio.iptvcinema.ui.ContentFocusHandler
import com.afifistudio.iptvcinema.ui.HomeChromeHost
import com.afifistudio.iptvcinema.ui.common.TvFocusCoordinator
import com.afifistudio.iptvcinema.ui.registerContentFocusHandler
import com.afifistudio.iptvcinema.ui.requestChromeFocus
import com.afifistudio.iptvcinema.ui.unregisterContentFocusHandler
import com.afifistudio.iptvcinema.ui.common.ContentImageBindings.bindContentImage
import com.afifistudio.iptvcinema.ui.browse.BrowseViewModel
import com.afifistudio.iptvcinema.ui.player.PlayerActivity
import com.afifistudio.iptvcinema.ui.startContentActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChannelDetailsFragment : Fragment() {

    private val browseViewModel: BrowseViewModel by viewModels({ requireActivity() })
    private var _binding: FragmentChannelDetailsBinding? = null
    private val binding get() = _binding!!
    private lateinit var channel: Channel

    private val contentFocusHandler = object : ContentFocusHandler {
        override fun requestInitialFocus(): Boolean {
            binding.detailsWatchButton.requestFocus()
            return true
        }

        override fun canFocusUpToChrome(): Boolean = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        channel = requireArguments().getParcelable(ARG_CHANNEL)
            ?: throw IllegalArgumentException("Channel required")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentChannelDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindChannelInfo()
        binding.detailsWatchButton.setOnClickListener { openPlayer() }
        binding.detailsFavoriteButton.setOnClickListener { toggleFavorite() }
        lifecycleScope.launch {
            updateFavoriteButton(browseViewModel.isFavorite(channel))
        }
        view.post { binding.detailsWatchButton.requestFocus() }
        TvFocusCoordinator.wireViewUp(
            binding.detailsWatchButton,
            canFocusUpProvider = { true },
            requestChromeFocus = { requestChromeFocus() },
        )
        (activity as? HomeChromeHost)?.setChromeVisible(true)
    }

    override fun onResume() {
        super.onResume()
        registerContentFocusHandler(contentFocusHandler)
    }

    override fun onPause() {
        unregisterContentFocusHandler()
        super.onPause()
    }

    private fun bindChannelInfo() {
        binding.detailsTitle.text = channel.name
        binding.detailsSubtitle.text = buildSubtitle()
        binding.detailsWatchButton.text = watchActionLabel()

        val plot = channel.plot?.trim().orEmpty()
        binding.detailsPlot.isVisible = plot.isNotEmpty()
        binding.detailsPlot.text = plot

        binding.detailsLogo.bindContentImage(channel.logoUrl, channel.contentType)
    }

    private fun buildSubtitle(): String {
        val typeLabel = when (channel.contentType) {
            ContentType.LIVE -> getString(R.string.live_badge)
            ContentType.MOVIE -> getString(R.string.movie_badge)
            ContentType.SERIES -> getString(R.string.series_badge)
            ContentType.EPISODE -> getString(R.string.episode_badge)
        }
        return listOfNotNull(channel.categoryName, typeLabel).joinToString(" · ")
    }

    private fun watchActionLabel(): String = when (channel.contentType) {
        ContentType.LIVE -> getString(R.string.watch_live)
        ContentType.MOVIE -> getString(R.string.watch_movie)
        ContentType.EPISODE -> getString(R.string.watch_episode)
        ContentType.SERIES -> getString(R.string.browse_episodes)
    }

    private fun openPlayer() {
        browseViewModel.rememberLastWatched(channel)
        startContentActivity(PlayerActivity.createIntent(requireContext(), channel, channel.categoryId))
    }

    private fun toggleFavorite() {
        lifecycleScope.launch {
            browseViewModel.toggleFavorite(channel)
            updateFavoriteButton(browseViewModel.isFavorite(channel))
        }
    }

    private fun updateFavoriteButton(isFavorite: Boolean) {
        binding.detailsFavoriteButton.text = getString(
            if (isFavorite) R.string.remove_favorite else R.string.add_favorite,
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_CHANNEL = "arg_channel"

        fun newInstance(channel: Channel): ChannelDetailsFragment =
            ChannelDetailsFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_CHANNEL, channel)
                }
            }
    }
}
