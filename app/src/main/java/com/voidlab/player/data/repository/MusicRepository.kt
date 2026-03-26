package com.voidlab.player.data.repository

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.voidlab.player.data.models.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.io.FileOutputStream

class MusicRepository(private val context: Context) {
    
    private val artworkCache = mutableMapOf<Long, Uri?>()
    private val cacheDir = File(context.cacheDir, "song_artwork").apply { 
        if (!exists()) mkdirs() 
    }
    
    fun getAllSongs(): Flow<List<Song>> = flow {
        val songs = mutableListOf<Song>()
        
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.SIZE
        )
        
        // Filter out songs with zero duration
        val selection = "${MediaStore.Audio.Media.DURATION} > ?"
        val selectionArgs = arrayOf("0")
        
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"
        
        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val albumId = cursor.getLong(albumIdColumn)
                val title = cursor.getString(titleColumn)
                val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                val album = cursor.getString(albumColumn) ?: "Unknown Album"
                val duration = cursor.getLong(durationColumn)
                val filePath = cursor.getString(dataColumn)
                val dateAdded = cursor.getLong(dateAddedColumn)
                val size = cursor.getLong(sizeColumn)
                
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                
                // Extract embedded artwork from THIS specific song file
                val albumArtUri = extractEmbeddedArtwork(id, filePath, albumId)
                
                songs.add(
                    Song(
                        id = id,
                        title = title,
                        artist = artist,
                        album = album,
                        duration = duration,
                        uri = contentUri,
                        albumArtUri = albumArtUri,
                        dateAdded = dateAdded,
                        size = size
                    )
                )
            }
        }
        
        emit(songs)
    }.flowOn(Dispatchers.IO)
    
    /**
     * Extract embedded artwork from individual audio file.
     * Falls back to album art if no embedded art found.
     */
    private fun extractEmbeddedArtwork(songId: Long, filePath: String, albumId: Long): Uri? {
        // Check cache first
        if (artworkCache.containsKey(songId)) {
            return artworkCache[songId]
        }
        
        var artworkUri: Uri? = null
        val retriever = MediaMetadataRetriever()
        
        try {
            retriever.setDataSource(filePath)
            val embeddedPicture = retriever.embeddedPicture
            
            if (embeddedPicture != null) {
                // Embedded artwork found! Save it to cache
                val artworkFile = File(cacheDir, "song_${songId}.jpg")
                
                FileOutputStream(artworkFile).use { outputStream ->
                    outputStream.write(embeddedPicture)
                }
                
                artworkUri = Uri.fromFile(artworkFile)
                Log.d("MusicRepository", "Extracted embedded art for song $songId: ${artworkFile.absolutePath}")
            } else {
                // No embedded art, fall back to album art
                artworkUri = ContentUris.withAppendedId(
                    Uri.parse("content://media/external/audio/albumart"),
                    albumId
                )
                Log.d("MusicRepository", "No embedded art for song $songId, using album art")
            }
        } catch (e: Exception) {
            Log.e("MusicRepository", "Failed to extract artwork for song $songId: ${e.message}")
            // Fall back to album art on error
            artworkUri = ContentUris.withAppendedId(
                Uri.parse("content://media/external/audio/albumart"),
                albumId
            )
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {
                Log.e("MusicRepository", "Failed to release MediaMetadataRetriever: ${e.message}")
            }
        }
        
        // Cache the result
        artworkCache[songId] = artworkUri
        return artworkUri
    }
    
    suspend fun findSongById(songId: Long): Song? {
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.SIZE
        )
        
        val selection = "${MediaStore.Audio.Media._ID} = ?"
        val selectionArgs = arrayOf(songId.toString())
        
        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                
                val id = cursor.getLong(idColumn)
                val albumId = cursor.getLong(albumIdColumn)
                val filePath = cursor.getString(dataColumn)
                val dateAdded = cursor.getLong(dateAddedColumn)
                val size = cursor.getLong(sizeColumn)
                
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                
                // Extract embedded artwork from THIS specific song file
                val albumArtUri = extractEmbeddedArtwork(id, filePath, albumId)
                
                return Song(
                    id = id,
                    title = cursor.getString(titleColumn),
                    artist = cursor.getString(artistColumn) ?: "Unknown Artist",
                    album = cursor.getString(albumColumn) ?: "Unknown Album",
                    duration = cursor.getLong(durationColumn),
                    uri = contentUri,
                    albumArtUri = albumArtUri,
                    dateAdded = dateAdded,
                    size = size
                )
            }
        }
        
        return null
    }
    
    /**
     * Clear the artwork cache (call when songs are added/removed)
     */
    fun clearArtworkCache() {
        artworkCache.clear()
        cacheDir.listFiles()?.forEach { it.delete() }
    }
}
