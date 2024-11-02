package com.example.firebase01

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firebase01.Adapter.NotesAdapter
import com.example.firebase01.Model.Note
import com.example.firebase01.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: NotesAdapter
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase
        database = Firebase.database.reference.child("notes")

        setupRecyclerView()
        setupFab()
        loadNotes()
    }

    private fun setupRecyclerView() {
        adapter = NotesAdapter(
            mutableListOf(),
            onNoteClick = { note -> openNoteDetail(note) },
            onDeleteClick = { note -> deleteNote(note) }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter

            // Add swipe to delete
            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            ) {
                override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                                    target: RecyclerView.ViewHolder) = false

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val localAdapter = adapter // Tạo biến cục bộ
                    if (localAdapter is NotesAdapter) { // Kiểm tra kiểu adapter
                        val position = viewHolder.adapterPosition
                        val note = localAdapter.getNotes()[position]
                        deleteNote(note)
                    }
                }
            }).attachToRecyclerView(this)
        }
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            openNoteDetail(null)
        }
    }

    private fun loadNotes() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val notes = mutableListOf<Note>()
                for (noteSnapshot in snapshot.children) {
                    noteSnapshot.getValue(Note::class.java)?.let { note ->
                        note.id = noteSnapshot.key ?: ""
                        notes.add(note)
                    }
                }
                adapter.updateNotes(notes.sortedByDescending { it.createdDate })
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Error: ${error.message}",
                    Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun deleteNote(note: Note) {
        database.child(note.id).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Note deleted successfully",
                    Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error deleting note: ${e.message}",
                    Toast.LENGTH_SHORT).show()
            }
    }

    private fun openNoteDetail(note: Note?) {
        val intent = Intent(this, NoteDetailActivity::class.java)
        intent.putExtra("note_id", note?.id)
        startActivity(intent)
    }
}