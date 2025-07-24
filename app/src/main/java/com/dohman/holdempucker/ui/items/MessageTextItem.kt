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
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem

class MessageTextItem(
    private val message: String,
    private val isNeutralMessage: Boolean = false
) : AbstractItem<MessageTextItem.ViewHolder>() {
    override val type: Int = R.id.adapter_type_computer_text
    override fun getViewHolder(v: View): ViewHolder = ViewHolder(message, isNeutralMessage, v)
    override val layoutRes: Int = R.layout.message_box_item

    class ViewHolder(
        private val message: String,
        private val isNeutralMessage: Boolean = false,
        view: View

    ) : FastAdapter.ViewHolder<MessageTextItem>(view) {
        var txtMessage: AppCompatTextView = view.findViewById(R.id.txt_message)


        override fun bindView(item: MessageTextItem, payloads: List<Any>) {
            txtMessage.setTextColor(
                ContextCompat.getColor(
                    itemView.context,
                    when {
                        isNeutralMessage -> R.color.white
                        isTeamGreenTurn() -> R.color.text_background_btm
                        else -> R.color.text_background_top
                    }
                )
            )

            if (isBotMoving() && !isNeutralMessage && !isShootingAtGoalie) {
                txtMessage.text = itemView.context.getString(R.string.bot_inputting)
            } else if (isOpponentMoving() && !isNeutralMessage && !isShootingAtGoalie) {
                txtMessage.text = itemView.context.getString(R.string.opponent_inputting)
            } else {
                txtMessage.apply {
                    text = message
                    Animations.animateComputerText(this)
                }
            }
        }

        override fun unbindView(item: MessageTextItem) {
            TODO("Not yet implemented")
        }
    }
}