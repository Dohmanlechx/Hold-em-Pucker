package com.dohman.holdempucker.ui.items

import android.content.Context
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.dohman.holdempucker.R
import com.dohman.holdempucker.databinding.LobbyItemBinding
import com.dohman.holdempucker.util.Util
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem

class LobbyItem(
    private val lobbyId: String?,
    private val lobbyName: String?,
    private val lobbyPassword: String?,
    private val amountPlayers: Int,
    private val fOnClick: (String?, String?) -> Unit
) : AbstractItem<LobbyItem.ViewHolder>() {
    override val type: Int = R.id.adapter_type_lobby
    override fun getViewHolder(v: View): ViewHolder = ViewHolder(
        lobbyId, lobbyName, lobbyPassword, amountPlayers, fOnClick,
        v
    )

    override val layoutRes: Int = R.layout.lobby_item

    class ViewHolder(
        private val lobbyId: String?,
        private val lobbyName: String?,
        private val lobbyPassword: String?,
        private val amountPlayers: Int,
        private val fOnClick: (String?, String?) -> Unit,
        itemView: View
    ) : FastAdapter.ViewHolder<MessageTextItem>(itemView) {
        var bgText: AppCompatImageView = itemView.findViewById(R.id.card_view_background)
        var padlock: AppCompatImageView = itemView.findViewById(R.id.v_padlock)
        var txtLobbyName: AppCompatTextView = itemView.findViewById(R.id.txt_lobby_name)
        var txtAmountPlayers: AppCompatTextView = itemView.findViewById(R.id.txt_amount_players)
        var lobbyItemLayout: View = itemView.findViewById(R.id.lobby_item_layout)


        override fun bindView(item: MessageTextItem, payloads: List<Any>) {
            val backgroundDrawable =
                if (amountPlayers >= 2) R.drawable.background_card_view_busy else R.drawable.background_card_view

            bgText.setImageDrawable(
                ContextCompat.getDrawable(itemView.context, backgroundDrawable)
            )

            padlock.visibility = if (lobbyPassword.isNullOrBlank()) View.GONE else View.VISIBLE

            itemView.apply {
                txtLobbyName.text = lobbyName
                txtAmountPlayers.text = "$amountPlayers/2"

                lobbyItemLayout.setOnClickListener {
                    if (amountPlayers < 2)
                        fOnClick.invoke(lobbyId, lobbyPassword)
                    else Util.vibrate(itemView.context, false)
                }
            }
        }

        override fun unbindView(item: MessageTextItem) {
            TODO("Not yet implemented")
        }
    }
}