package com.snaimio.familyaiplanner.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.snaimio.familyaiplanner.R
import com.snaimio.familyaiplanner.data.FamilyMember

class MemberAdapter(
    private var members: List<FamilyMember>,
    private val onItemClick: ((FamilyMember) -> Unit)? = null
) : RecyclerView.Adapter<MemberAdapter.MemberViewHolder>() {

    class MemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iconText: TextView = itemView.findViewById(R.id.itemMemberIcon)
        val nameText: TextView = itemView.findViewById(R.id.itemMemberName)
        val roleText: TextView = itemView.findViewById(R.id.itemMemberRole)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_family_member, parent, false)
        return MemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val member = members[position]
        holder.iconText.text = member.avatarEmoji
        holder.nameText.text = member.name
        holder.roleText.text = member.role
        holder.itemView.setOnClickListener { onItemClick?.invoke(member) }
    }

    override fun getItemCount(): Int = members.size

    fun updateData(newMembers: List<FamilyMember>) {
        members = newMembers
        notifyDataSetChanged()
    }
}
