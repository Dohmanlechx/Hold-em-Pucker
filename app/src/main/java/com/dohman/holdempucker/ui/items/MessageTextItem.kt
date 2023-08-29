package com.dohman.holdempucker.ui.items

import android.content.Context
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.dohman.holdempucker.R
import com.dohman.holdempucker.util.Animations
import com.dohman.holdempucker.util.Constants.Companion.isShootingAtGoalie
import com.dohman.holdempucker.util.Constants.WhoseTurn.Companion.isBotMoving
import com.dohman.holdempucker.util.Constants.WhoseTurn.Companion.isOpponentMoving
import com.dohman.holdempucker.util.Constants.WhoseTurn.Companion.isTeamGreenTurn
import com.mikepenz.fastadapter.items.AbstractItem

class MessageTextItem(
    private val message: String,
    private val isNeutralMessage: Boolean = false
) : AbstractItem<MessageTextItem, MessageTextItem.ViewHolder>() {
    override fun getType(): Int = R.id.adapter_type_computer_text
    override fun getViewHolder(v: View): ViewHolder = ViewHolder(v)
    override fun getLayoutRes(): Int = R.layout.message_box_item

    override fun bindView(holder: ViewHolder, payloads: MutableList<Any>) {
        super.bindView(holder, payloads)

        val txtMessage = holder.itemView.findViewById<AppCompatTextView>(R.id.txt_message)

        txtMessage.setTextColor(
            ContextCompat.getColor(
                holder.context,
                when {
                    isNeutralMessage -> R.color.white
                    isTeamGreenTurn() -> R.color.text_background_btm
                    else -> R.color.text_background_top
                }
            )
        )

        if (isBotMoving() && !isNeutralMessage && !isShootingAtGoalie) {
            txtMessage.text = holder.context.getString(R.string.bot_inputting)
        } else if (isOpponentMoving() && !isNeutralMessage && !isShootingAtGoalie) {
            txtMessage.text = holder.context.getString(R.string.opponent_inputting)
        } else {
            txtMessage.apply {
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