package com.dohman.holdempucker.ui.items

import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.dohman.holdempucker.R
import com.dohman.holdempucker.util.Util
import com.mikepenz.fastadapter.items.AbstractItem
import kotlinx.android.synthetic.main.lobby_item.view.*

class LobbyItem(
    private val lobbyId: String?,
    private val lobbyName: String?,
    private val lobbyPassword: String?,
    private val amountPlayers: Int,
    private val fOnClick: (String?, String?) -> Unit
) : AbstractItem<LobbyItem, LobbyItem.ViewHolder>() {
    override fun getType(): Int = R.id.fastadapter_item
    override fun getViewHolder(v: View): ViewHolder = ViewHolder(v)
    override fun getLayoutRes(): Int = R.layout.lobby_item

    override fun bindView(holder: ViewHolder, payloads: MutableList<Any>) {
        super.bindView(holder, payloads)

        if (amountPlayers >= 2)
            holder.itemView.card_view_background.setImageDrawable(
                ContextCompat.getDrawable(holder.context, R.drawable.background_card_view_busy)
            )

        holder.itemView.v_padlock.visibility = if (lobbyPassword.isNullOrBlank()) View.GONE else View.VISIBLE

        holder.itemView.apply {
            txt_lobby_name.text = lobbyName
            txt_amount_players.text = "$amountPlayers/2"

            lobby_item_layout.setOnClickListener {
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