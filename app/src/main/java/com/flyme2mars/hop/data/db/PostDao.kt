package com.flyme2mars.hop.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {
    @Query("SELECT * FROM posts ORDER BY createdAtMillis DESC")
    fun observeAll(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts ORDER BY createdAtMillis DESC")
    suspend fun getAll(): List<PostEntity>

    @Query("SELECT * FROM posts WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): PostEntity?

    @Query("SELECT COUNT(*) FROM posts")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PostEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(entity: PostEntity)

    @Query("UPDATE posts SET claimed = 1, updatedAtMillis = :updatedAtMillis WHERE id = :id")
    suspend fun markClaimed(id: String, updatedAtMillis: Long)

    @Query("DELETE FROM posts WHERE id = :id")
    suspend fun delete(id: String)
}
