package com.afifistudio.iptvcinema.ui.browse

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.leanback.widget.Presenter
import androidx.leanback.widget.Row
import androidx.leanback.widget.RowHeaderPresenter
import com.afifistudio.iptvcinema.databinding.RowHeaderEnhancedBinding

class StreamingRowHeaderPresenter : RowHeaderPresenter() {

    override fun onCreateViewHolder(parent: ViewGroup): Presenter.ViewHolder {
        val binding = RowHeaderEnhancedBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(viewHolder: Presenter.ViewHolder, item: Any?) {
        val row = item as? Row ?: return
        val binding = RowHeaderEnhancedBinding.bind(viewHolder.view)
        val headerName = row.headerItem?.name.orEmpty()
        val parts = headerName.split(" · ", limit = 2)
        binding.rowHeaderTitle.text = parts.first()
        if (parts.size > 1) {
            binding.rowHeaderSubtitle.text = parts[1]
            binding.rowHeaderSubtitle.isVisible = true
        } else {
            binding.rowHeaderSubtitle.isVisible = false
        }
    }

    override fun onUnbindViewHolder(viewHolder: Presenter.ViewHolder) {
        val binding = RowHeaderEnhancedBinding.bind(viewHolder.view)
        binding.rowHeaderTitle.text = null
        binding.rowHeaderSubtitle.text = null
        binding.rowHeaderSubtitle.isVisible = false
    }
}
