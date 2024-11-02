package com.example.firebase02

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firebase02.databinding.ActivityMainBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import androidx.appcompat.widget.SearchView


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var database: DatabaseReference
    private lateinit var contactAdapter: ContactAdapter
    private val contacts = mutableListOf<Contact>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase
        database = FirebaseDatabase.getInstance().reference.child("contacts")

        // Setup RecyclerView
        contactAdapter = ContactAdapter(contacts) { contact ->
            showContactDetails(contact)
        }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = contactAdapter
        }

        // Setup search functionality
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                filterContacts(newText)
                return true
            }
        })

        // FAB for adding new contact
        binding.fabAdd.setOnClickListener {
            showAddContactDialog()
        }

        // Load contacts from Firebase
        loadContacts()
    }

    private fun loadContacts() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                contacts.clear()
                for (contactSnapshot in snapshot.children) {
                    val contact = contactSnapshot.getValue(Contact::class.java)
                    contact?.let {
                        it.id = contactSnapshot.key ?: ""
                        contacts.add(it)
                    }
                }
                contacts.sortBy { it.name }
                contactAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filterContacts(query: String?) {
        query?.let {
            val filteredList = contacts.filter { contact ->
                contact.name.contains(query, ignoreCase = true)
            }
            contactAdapter.updateList(filteredList)
        }
    }

    private fun showAddContactDialog(contact: Contact? = null) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_contact, null)
        val nameEdit = dialogView.findViewById<EditText>(R.id.editName)
        val phoneEdit = dialogView.findViewById<EditText>(R.id.editPhone)
        val emailEdit = dialogView.findViewById<EditText>(R.id.editEmail)
        val addressEdit = dialogView.findViewById<EditText>(R.id.editAddress)

        // Pre-fill fields if editing existing contact
        contact?.let {
            nameEdit.setText(it.name)
            phoneEdit.setText(it.phone)
            emailEdit.setText(it.email)
            addressEdit.setText(it.address)
        }

        AlertDialog.Builder(this)
            .setTitle(if (contact == null) "Add New Contact" else "Edit Contact")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newContact = Contact(
                    id = contact?.id ?: "",
                    name = nameEdit.text.toString(),
                    phone = phoneEdit.text.toString(),
                    email = emailEdit.text.toString(),
                    address = addressEdit.text.toString()
                )
                saveContact(newContact)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveContact(contact: Contact) {
        if (contact.id.isEmpty()) {
            // Add new contact
            val newRef = database.push()
            contact.id = newRef.key ?: return
            newRef.setValue(contact)
        } else {
            // Update existing contact
            database.child(contact.id).setValue(contact)
        }
    }

    private fun showContactDetails(contact: Contact) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_contact_details, null)

        dialogView.apply {
            findViewById<TextView>(R.id.textName).text = contact.name
            findViewById<TextView>(R.id.textPhone).text = contact.phone
            findViewById<TextView>(R.id.textEmail).text = contact.email
            findViewById<TextView>(R.id.textAddress).text = contact.address
        }

        AlertDialog.Builder(this)
            .setTitle("Contact Details")
            .setView(dialogView)
            .setPositiveButton("Edit") { _, _ ->
                showAddContactDialog(contact)
            }
            .setNegativeButton("Delete") { _, _ ->
                deleteContact(contact)
            }
            .setNeutralButton("Close", null)
            .show()
    }

    private fun deleteContact(contact: Contact) {
        AlertDialog.Builder(this)
            .setTitle("Delete Contact")
            .setMessage("Are you sure you want to delete ${contact.name}?")
            .setPositiveButton("Yes") { _, _ ->
                database.child(contact.id).removeValue()
            }
            .setNegativeButton("No", null)
            .show()
    }
}