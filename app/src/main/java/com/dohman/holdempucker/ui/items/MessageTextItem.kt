package com.dohman.holdempucker.ui.items

import android.util.Log
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.dohman.holdempucker.R
import com.dohman.holdempucker.util.Animations
import com.dohman.holdempucker.util.Constants.Companion.isShootingAtGoalie
import com.dohman.holdempucker.util.Constants.WhoseTurn.Companion.isBotMoving
import com.dohman.holdempucker.util.Constants.WhoseTurn.Companion.isOpponentMoving
import com.dohman.holdempucker.util.Constants.WhoseTurn.Companion.isTeamGreenTurn
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem

class MessageTextItem(
     val message: String,
     val isNeutralMessage: Boolean = false
) : AbstractItem<MessageTextItem.ViewHolder>() {
    override val type: Int = R.id.adapter_type_computer_text
    override fun getViewHolder(v: View): ViewHolder = ViewHolder(v)
    override val layoutRes: Int = R.layout.message_box_item

    class ViewHolder(view: View) : FastAdapter.ViewHolder<MessageTextItem>(view) {
        var txtMessage: AppCompatTextView = view.findViewById(R.id.txt_message)

        override fun bindView(item: MessageTextItem, payloads: List<Any>) {
            txtMessage.setTextColor(
                ContextCompat.getColor(
                    itemView.context,
                    when {
                        item.isNeutralMessage -> R.color.white
                        isTeamGreenTurn() -> R.color.text_background_btm
                        else -> R.color.text_background_top
                    }
                )
            )

            if (isBotMoving() && !item.isNeutralMessage && !isShootingAtGoalie) {
                txtMessage.text = itemView.context.getString(R.string.bot_inputting)
            } else if (isOpponentMoving() && !item.isNeutralMessage && !isShootingAtGoalie) {
                txtMessage.text = itemView.context.getString(R.string.opponent_inputting)
            } else {
                txtMessage.apply {
                    text = item.message
                    Animations.animateComputerText(this)
                }
            }
        }

        override fun unbindView(item: MessageTextItem) {
            txtMessage.text = null

        }
    }
}