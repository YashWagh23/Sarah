package com.sarah.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sarah.app.data.local.entity.AcademicNoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AcademicNoteDao {

    @Query("SELECT * FROM academic_notes ORDER BY isPinned DESC, updatedEpochMs DESC")
    fun getAllNotes(): Flow<List<AcademicNoteEntity>>

    @Query("SELECT * FROM academic_notes WHERE subjectId = :subjectId ORDER BY isPinned DESC, updatedEpochMs DESC")
    fun getNotesBySubject(subjectId: Long): Flow<List<AcademicNoteEntity>>

    @Query("SELECT * FROM academic_notes WHERE id = :id")
    suspend fun getNoteById(id: Long): AcademicNoteEntity?

    @Query("SELECT * FROM academic_notes ORDER BY isPinned DESC, updatedEpochMs DESC")
    suspend fun getAllNotesSync(): List<AcademicNoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: AcademicNoteEntity): Long

    @Update
    suspend fun updateNote(note: AcademicNoteEntity)

    @Delete
    suspend fun deleteNote(note: AcademicNoteEntity)

    @Query("DELETE FROM academic_notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)

    @Query("UPDATE academic_notes SET isPinned = :isPinned, updatedEpochMs = :updatedEpochMs WHERE id = :id")
    suspend fun updatePinStatus(id: Long, isPinned: Boolean, updatedEpochMs: Long = System.currentTimeMillis())
}
