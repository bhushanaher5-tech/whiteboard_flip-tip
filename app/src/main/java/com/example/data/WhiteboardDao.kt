package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WhiteboardDao {
    @Query("SELECT * FROM whiteboards ORDER BY updatedAt DESC")
    fun getAllWhiteboards(): Flow<List<WhiteboardEntity>>

    @Query("SELECT * FROM whiteboards WHERE id = :id LIMIT 1")
    suspend fun getWhiteboardById(id: String): WhiteboardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWhiteboard(entity: WhiteboardEntity)

    @Update
    suspend fun updateWhiteboard(entity: WhiteboardEntity)

    @Query("DELETE FROM whiteboards WHERE id = :id")
    suspend fun deleteWhiteboard(id: String)

    @Query("SELECT COUNT(*) FROM whiteboards")
    suspend fun getBoardCount(): Int
}
