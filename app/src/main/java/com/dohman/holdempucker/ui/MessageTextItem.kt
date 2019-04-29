package com.dohman.holdempucker.ui

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.dohman.holdempucker.R
import com.mikepenz.fastadapter.items.AbstractItem
import kotlinx.android.synthetic.main.message_box_item.view.*

class MessageTextItem(
    private val team: String?,
    private val message: String
) : AbstractItem<MessageTextItem, MessageTextItem.ViewHolder>() {
    override fun getType(): Int = R.id.fastadapter_item
    override fun getViewHolder(v: View): ViewHolder = ViewHolder(v)
    override fun getLayoutRes(): Int = R.layout.message_box_item

    override fun bindView(holder: ViewHolder, payloads: MutableList<Any>) {
        super.bindView(holder, payloads)

        team?.let {
            holder.itemView.txt_team.visibility = View.VISIBLE
            holder.itemView.txt_team.text = it
        }
        holder.itemView.txt_message.text = message
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val context: Context
            get() = itemView.context

    }
}