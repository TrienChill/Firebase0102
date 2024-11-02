package com.example.firebase02

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.firebase02.databinding.ItemContactBinding

class ContactAdapter(
    private var contacts: List<Contact>,
    private val onItemClick: (Contact) -> Unit
) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    class ContactViewHolder(val binding: ItemContactBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val binding = ItemContactBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ContactViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]
        holder.binding.apply {
            textName.text = contact.name
            textPhone.text = contact.phone
            root.setOnClickListener { onItemClick(contact) }
        }
    }

    override fun getItemCount() = contacts.size

    fun updateList(newList: List<Contact>) {
        contacts = newList
        notifyDataSetChanged()
    }
}