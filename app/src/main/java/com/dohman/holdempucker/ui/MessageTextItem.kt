package com.dohman.holdempucker.ui

import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.dohman.holdempucker.R
import com.mikepenz.fastadapter.items.AbstractItem
import kotlinx.android.synthetic.main.message_box_item.view.*

class MessageTextItem(
    private val message: String,
    private val isTeamTop: Boolean = false,
    private val isNeutralMessage: Boolean = false
) : AbstractItem<MessageTextItem, MessageTextItem.ViewHolder>() {
    override fun getType(): Int = R.id.fastadapter_item
    override fun getViewHolder(v: View): ViewHolder = ViewHolder(v)
    override fun getLayoutRes(): Int = R.layout.message_box_item

    override fun bindView(holder: ViewHolder, payloads: MutableList<Any>) {
        super.bindView(holder, payloads)

        holder.itemView.txt_message.setTextColor(
            ContextCompat.getColor(
                holder.context,
                when {
                    isNeutralMessage -> R.color.white
                    isTeamTop -> R.color.text_background_top
                    else -> R.color.text_background_btm
                }
            )
        )
        holder.itemView.txt_message.apply {
            setCharacterDelay(50) // Setter, custom speed
            animateText(message)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val context: Context
            get() = itemView.context

    }
}