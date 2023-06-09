package com.example.scoreboard.database

import android.content.ContentValues
import android.content.Context

class SessionTagDBService(context: Context) : ScoreboardDatabase(context) {

    fun addTagToSession(tagID: Long, sessionID: Long) {
        val db = this.writableDatabase
        if(tagID<0 || sessionID<0){
            return
        }
        val contentValues = ContentValues().apply {
            put(DatabaseConstants.SessionTagTable.TAG_ID_COLUMN, tagID)
            put(DatabaseConstants.SessionTagTable.SESSION_ID_COLUMN, sessionID)
        }
        db.insert(DatabaseConstants.SessionTagTable.TABLE_NAME, null, contentValues)
        db.close()
    }

    fun removeTagFromSession(tagID: Long, sessionID: Long) {
        val db = this.writableDatabase
        val selection =
            "${DatabaseConstants.SessionTagTable.TAG_ID_COLUMN} = ? " +
                    "AND ${DatabaseConstants.SessionTagTable.SESSION_ID_COLUMN} = ?"
        val selectionArgs = arrayOf(tagID.toString(), sessionID.toString())
        db.delete(DatabaseConstants.SessionTagTable.TABLE_NAME, selection, selectionArgs)
        db.close()
    }

    fun getSessionIDsForTag(tagID: Long): List<Long> {
        val db = this.readableDatabase
        val projection = arrayOf(DatabaseConstants.SessionTagTable.SESSION_ID_COLUMN)
        val selection = "${DatabaseConstants.SessionTagTable.TAG_ID_COLUMN} = ?"
        val selectionArgs = arrayOf(tagID.toString())
        val cursor = db.query(
            DatabaseConstants.SessionTagTable.TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null
        )
        val sessionIDs = mutableListOf<Long>()
        with(cursor) {
            while (moveToNext()) {
                val sessionID = getLong(getColumnIndexOrThrow(DatabaseConstants.SessionTagTable.SESSION_ID_COLUMN))
                sessionIDs.add(sessionID)
            }
        }
        cursor.close()
        db.close()
        return sessionIDs
    }

    fun getTagIDsForSession(sessionID: Long): List<Long> {
        val db = this.readableDatabase
        val projection = arrayOf(DatabaseConstants.SessionTagTable.TAG_ID_COLUMN)
        val selection = "${DatabaseConstants.SessionTagTable.SESSION_ID_COLUMN} = ?"
        val selectionArgs = arrayOf(sessionID.toString())
        val cursor = db.query(
            DatabaseConstants.SessionTagTable.TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null
        )
        val tagIDs = mutableListOf<Long>()
        with(cursor) {
            while (moveToNext()) {
                val tagID = getLong(getColumnIndexOrThrow(DatabaseConstants.SessionTagTable.TAG_ID_COLUMN))
                tagIDs.add(tagID)
            }
        }
        cursor.close()
        db.close()
        return tagIDs
    }

    fun deleteSessionTagsOnSessionDelete(sessionID: Long) {
        val db = this.writableDatabase
        val selection = "${DatabaseConstants.SessionTagTable.SESSION_ID_COLUMN} = ?"
        val selectionArgs = arrayOf(sessionID.toString())
        db.delete(DatabaseConstants.SessionTagTable.TABLE_NAME, selection, selectionArgs)
        db.close()
    }

    fun deleteSessionTagsOnTagDelete(tagID: Long) {
        val db = this.writableDatabase
        val selection = "${DatabaseConstants.SessionTagTable.TAG_ID_COLUMN} = ?"
        val selectionArgs = arrayOf(tagID.toString())
        db.delete(DatabaseConstants.SessionTagTable.TABLE_NAME, selection, selectionArgs)
        db.close()
    }

}