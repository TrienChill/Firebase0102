package com.example.firebase01

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.firebase01.Model.Note
import com.example.firebase01.databinding.ActivityNoteDetailBinding
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database

class NoteDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNoteDetailBinding
    private lateinit var database: DatabaseReference
    private var noteId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = Firebase.database.reference.child("notes")
        noteId = intent.getStringExtra("note_id")

        if (noteId != null) {
            loadNote()
        }

        setupSaveButton()
    }

    private fun loadNote() {
        database.child(noteId!!).get().addOnSuccessListener { snapshot ->
            snapshot.getValue(Note::class.java)?.let { note ->
                binding.etTitle.setText(note.title)
                binding.etContent.setText(note.content)
            }
        }
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val content = binding.etContent.text.toString().trim()

            if (title.isEmpty()) {
                binding.etTitle.error = "Title is required"
                return@setOnClickListener
            }

            val note = Note(
                id = noteId ?: database.push().key ?: return@setOnClickListener,
                title = title,
                content = content,
                createdDate = if (noteId == null) System.currentTimeMillis()
                else System.currentTimeMillis()
            )

            database.child(note.id).setValue(note)
                .addOnSuccessListener {
                    Toast.makeText(this, "Note saved successfully",
                        Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error saving note: ${e.message}",
                        Toast.LENGTH_SHORT).show()
                }
        }
    }
}

