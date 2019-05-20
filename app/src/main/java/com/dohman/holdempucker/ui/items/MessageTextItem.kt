package com.dohman.holdempucker.ui.items

import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.dohman.holdempucker.R
import com.dohman.holdempucker.util.Animations
import com.dohman.holdempucker.util.Constants.Companion.isShootingAtGoalie
import com.dohman.holdempucker.util.Constants.WhoseTurn.Companion.isBotMoving
import com.dohman.holdempucker.util.Constants.WhoseTurn.Companion.isTeamBottomTurn
import com.mikepenz.fastadapter.items.AbstractItem
import kotlinx.android.synthetic.main.message_box_item.view.*

class MessageTextItem(
    private val message: String,
    private val isNeutralMessage: Boolean = false
) : AbstractItem<MessageTextItem, MessageTextItem.ViewHolder>() {
    override fun getType(): Int = R.id.fastadapter_item
    override fun getViewHolder(v: View): ViewHolder = ViewHolder(v)

    override fun getLayoutRes(): Int = R.layout.message_box_item

    override fun bindView(holder: ViewHolder, payloads: MutableList<Any>) {
        super.bindView(holder, payloads)

        val isTeamBottom = isTeamBottomTurn()

        holder.itemView.txt_message.setTextColor(
            ContextCompat.getColor(
                holder.context,
                when {
                    isNeutralMessage -> R.color.white
                    isTeamBottom -> R.color.text_background_btm
                    else -> R.color.text_background_top
                }
            )
        )

        if (isBotMoving() && !isNeutralMessage && !isShootingAtGoalie) {
            holder.itemView.txt_message.text = holder.context.getString(R.string.bot_inputting)
        } else {
            holder.itemView.txt_message.apply {
                text = message
                Animations.animateComputerText(this)
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val context: Context
            get() = itemView.context

    }
}