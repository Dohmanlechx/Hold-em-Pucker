package com.dohman.holdempucker.ui.items

import android.content.Context
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.dohman.holdempucker.R
import com.dohman.holdempucker.util.Util
import com.mikepenz.fastadapter.items.AbstractItem

class LobbyItem(
    private val lobbyId: String?,
    private val lobbyName: String?,
    private val lobbyPassword: String?,
    private val amountPlayers: Int,
    private val fOnClick: (String?, String?) -> Unit
) : AbstractItem<LobbyItem, LobbyItem.ViewHolder>() {
    override fun getType(): Int = R.id.adapter_type_lobby
    override fun getViewHolder(v: View): ViewHolder = ViewHolder(v)
    override fun getLayoutRes(): Int = R.layout.lobby_item

    override fun bindView(holder: ViewHolder, payloads: MutableList<Any>) {
        super.bindView(holder, payloads)

        val cardViewBg = holder.itemView.findViewById<AppCompatImageView>(R.id.card_view_background)
        val padLock = holder.itemView.findViewById<AppCompatImageView>(R.id.v_padlock)

        val backgroundDrawable =
            if (amountPlayers >= 2) R.drawable.background_card_view_busy else R.drawable.background_card_view

        cardViewBg.setImageDrawable(
            ContextCompat.getDrawable(holder.context, backgroundDrawable)
        )

        padLock.visibility = if (lobbyPassword.isNullOrBlank()) View.GONE else View.VISIBLE

        holder.itemView.apply {
            val txtLobbyName = holder.itemView.findViewById<AppCompatTextView>(R.id.txt_lobby_name)
            val txtAmountPlayers = holder.itemView.findViewById<AppCompatTextView>(R.id.txt_amount_players)
            val lobbyItemLayout = holder.itemView.findViewById<ConstraintLayout>(R.id.lobby_item_layout)

            txtLobbyName.text = lobbyName
            txtAmountPlayers.text = "$amountPlayers/2"

            lobbyItemLayout.setOnClickListener {
                if (amountPlayers < 2)
                    fOnClick.invoke(lobbyId, lobbyPassword)
                else Util.vibrate(holder.context, false)
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val context: Context
            get() = itemView.context
    }
}