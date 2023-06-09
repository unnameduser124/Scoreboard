package com.example.scoreboard.database

import android.content.ContentValues
import android.content.Context
import android.provider.BaseColumns
import com.example.scoreboard.session.Session
import com.example.scoreboard.session.SessionData
import com.example.scoreboard.setCalendarToDayEnd
import java.util.Calendar

class SessionDBService(private val appContext: Context) : ScoreboardDatabase(appContext) {

    fun addSession(session: Session): Long {
        if (session.getDuration() < 0) {
            return -1L
        }
        val today = Calendar.getInstance()
        setCalendarToDayEnd(today)
        if (session.getDate().after(today)) {
            return -1L
        }

        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(DatabaseConstants.SessionsTable.DURATION_COLUMN, session.getDuration())
            put(DatabaseConstants.SessionsTable.DATE_COLUMN, session.getDate().timeInMillis)
        }

        val sessionID = db.insert(DatabaseConstants.SessionsTable.TABLE_NAME, null, contentValues)

        session.tags.forEach {
            SessionTagDBService(appContext).addTagToSession(it.id, sessionID)
        }

        db.close()
        return sessionID
    }

    fun updateSession(session: Session) {
        if (session.getDuration() < 0) {
            return
        }
        val today = Calendar.getInstance()
        setCalendarToDayEnd(today)
        if (session.getDate().after(today)) {
            return
        }

        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(DatabaseConstants.SessionsTable.DURATION_COLUMN, session.getDuration())
            put(DatabaseConstants.SessionsTable.DATE_COLUMN, session.getDate().timeInMillis)
        }
        val selection = "${BaseColumns._ID} = ?"
        val selectionArgs = arrayOf(session.id.toString())
        db.update(
            DatabaseConstants.SessionsTable.TABLE_NAME,
            contentValues,
            selection,
            selectionArgs
        )
        SessionTagDBService(appContext).deleteSessionTagsOnSessionDelete(session.id)
        session.tags.forEach {
            SessionTagDBService(appContext).addTagToSession(it.id, session.id)
        }
        db.close()
    }

    fun deleteSessionByID(id: Long) {
        val db = this.writableDatabase
        val selection = "${BaseColumns._ID} = ?"
        val selectionArgs = arrayOf(id.toString())
        db.delete(
            DatabaseConstants.SessionsTable.TABLE_NAME,
            selection,
            selectionArgs
        )
        SessionTagDBService(appContext).deleteSessionTagsOnSessionDelete(id)
        db.close()
    }

    fun getSessionDataByID(id: Long): SessionData? {
        val db = this.readableDatabase
        val projection = arrayOf(
            DatabaseConstants.SessionsTable.DURATION_COLUMN,
            DatabaseConstants.SessionsTable.DATE_COLUMN
        )
        val selection = "${BaseColumns._ID} = ?"
        val selectionArgs = arrayOf(id.toString())
        val cursor = db.query(
            DatabaseConstants.SessionsTable.TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null
        )
        if (cursor.moveToFirst()) {
            val duration =
                cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseConstants.SessionsTable.DURATION_COLUMN))
            val date =
                cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseConstants.SessionsTable.DATE_COLUMN))
            val sessionData = SessionData(duration, date, id)
            cursor.close()
            return sessionData
        }
        cursor.close()
        return null
    }

    fun getSessionWithTagsByID(id: Long): Session? {
        val db = this.readableDatabase
        val projection = arrayOf(
            DatabaseConstants.SessionsTable.DURATION_COLUMN,
            DatabaseConstants.SessionsTable.DATE_COLUMN
        )
        val selection = "${BaseColumns._ID} = ?"
        val selectionArgs = arrayOf(id.toString())
        val cursor = db.query(
            DatabaseConstants.SessionsTable.TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null
        )
        if (cursor.moveToFirst()) {
            val duration =
                cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseConstants.SessionsTable.DURATION_COLUMN))
            val date =
                cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseConstants.SessionsTable.DATE_COLUMN))
            val session = Session(duration, Calendar.getInstance().apply{ timeInMillis = date }, id, mutableListOf())
            val sessionTagIDs = SessionTagDBService(appContext).getTagIDsForSession(id)
            sessionTagIDs.forEach {
                val tag = TagDBService(appContext).getTagByID(it)
                if(tag!=null){
                    session.tags.add(tag)
                }
            }
            cursor.close()
            return session
        }
        cursor.close()
        return null
    }

    fun getAllSessions(): List<Session> {
        val db = this.readableDatabase
        val projection = arrayOf(
            BaseColumns._ID,
            DatabaseConstants.SessionsTable.DURATION_COLUMN,
            DatabaseConstants.SessionsTable.DATE_COLUMN
        )
        val cursor = db.query(
            DatabaseConstants.SessionsTable.TABLE_NAME,
            projection,
            null,
            null,
            null,
            null,
            null
        )
        val sessions = mutableListOf<Session>()
        while(cursor.moveToNext()){
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(BaseColumns._ID))
            val duration =
                cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseConstants.SessionsTable.DURATION_COLUMN))
            val date =
                cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseConstants.SessionsTable.DATE_COLUMN))
            val session = Session(duration, Calendar.getInstance().apply{ timeInMillis = date }, id, mutableListOf())
            val sessionTagIDs = SessionTagDBService(appContext).getTagIDsForSession(id)
            sessionTagIDs.forEach {
                val tag = TagDBService(appContext).getTagByID(it)
                if(tag!=null){
                    session.tags.add(tag)
                }
            }
            sessions.add(session)
        }
        cursor.close()
        return sessions
    }
}