package com.example.scoreboard

import android.content.Context
import android.provider.BaseColumns
import androidx.test.platform.app.InstrumentationRegistry
import com.example.scoreboard.database.DatabaseConstants
import com.example.scoreboard.database.SessionDBService
import com.example.scoreboard.session.Session
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Calendar

class SessionDBServiceTests {

    private lateinit var applicationContext: Context
    private lateinit var sessionDBService: SessionDBService

    @Before
    fun setUp() {
        applicationContext = InstrumentationRegistry.getInstrumentation().targetContext
        sessionDBService = SessionDBService(applicationContext)
    }

    @After
    fun tearDown() {
        sessionDBService.close()
        applicationContext.deleteDatabase(DatabaseConstants.DATABASE_NAME)
    }

    @Test
    fun addSessionTest() {
        val calendar = Calendar.getInstance()
        val session = Session(0, calendar, 0, mutableListOf())
        val id = sessionDBService.addSession(session)
        val cursor = sessionDBService.readableDatabase.rawQuery(
            "SELECT * FROM ${DatabaseConstants.SessionsTable.TABLE_NAME} " +
                    "WHERE ${BaseColumns._ID} = $id",
            null
        )
        cursor.use {
            assertTrue(cursor.moveToFirst())
            assertEquals(
                0,
                cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseConstants.SessionsTable.DURATION_COLUMN))
            )
            assertEquals(
                calendar.timeInMillis,
                cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseConstants.SessionsTable.DATE_COLUMN))
            )
        }
    }

    @Test
    fun addSessionFailNegativeDuration() {
        val calendar = Calendar.getInstance()
        val session = Session(-1, calendar, 0, mutableListOf())
        val id = sessionDBService.addSession(session)
        assertEquals(-1L, id)
    }

    @Test
    fun addSessionFailFutureDate() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val session = Session(0, calendar, 0, mutableListOf())
        val id = sessionDBService.addSession(session)
        assertEquals(-1L, id)
    }

    @Test
    fun getSessionDataByIDTest() {
        val calendar = Calendar.getInstance()
        val session = Session(0, calendar, 0, mutableListOf())
        val id = sessionDBService.addSession(session)

        val sessionAdded = Session(1, calendar, id, mutableListOf())
        assertSessionAdded(sessionAdded)

        val sessionData = sessionDBService.getSessionDataByID(id)
        assertEquals(id, sessionData?.id)
        assertEquals(0, sessionData?.duration)
        assertEquals(calendar.timeInMillis, sessionData?.date)
    }

    @Test
    fun getSessionDataByIDFailInvalidID(){
        val sessionData = sessionDBService.getSessionDataByID(-1)
        assertEquals(null, sessionData)
    }

    @Test
    fun getSessionWithTagsByIDTest(){
        val calendar = Calendar.getInstance()
        val tags = mutableListOf<Tag>(Tag("tag1", 1), Tag("tag2", 2))
        val session = Session(0, calendar, 0, tags)
        val id = sessionDBService.addSession(session)

        val sessionAdded = Session(1, calendar, id, mutableListOf())
        assertSessionAdded(sessionAdded)

        val sessionData = sessionDBService.getSessionWithTagsByID(id)

        assertEquals(id, sessionData?.id)
        assertEquals(0, sessionData?.getDuration())
        assertEquals(calendar.timeInMillis, sessionData?.getDate()?.timeInMillis)
        assertEquals(2, sessionData?.tags?.size)
    }

    @Test
    fun getSessionWithTagsTestNoTagsForSession(){
        val calendar = Calendar.getInstance()
        val session = Session(0, calendar, 0, mutableListOf())
        val id = sessionDBService.addSession(session)

        val sessionAdded = Session(1, calendar, id, mutableListOf())
        assertSessionAdded(sessionAdded)

        val sessionData = sessionDBService.getSessionWithTagsByID(id)

        assertEquals(id, sessionData?.id)
        assertEquals(0, sessionData?.getDuration())
        assertEquals(calendar.timeInMillis, sessionData?.getDate()?.timeInMillis)
        assertEquals(0, sessionData?.tags?.size)
    }

    @Test
    fun getSessionWithTagsByIDFailInvalidID(){
        val sessionData = sessionDBService.getSessionWithTagsByID(-1)
        assertEquals(null, sessionData)
    }

    @Test
    fun updateSessionTest(){
        val calendar = Calendar.getInstance()
        val session = Session(0, calendar, 0, mutableListOf())
        val id = sessionDBService.addSession(session)

        val sessionAdded = Session(1, calendar, id, mutableListOf())
        assertSessionAdded(sessionAdded)

        val newCalendar = Calendar.getInstance()
        newCalendar.add(Calendar.DAY_OF_MONTH, -1)
        val newSession = Session(1, newCalendar, id, mutableListOf())
        sessionDBService.updateSession(newSession)

        val updated = sessionDBService.getSessionDataByID(id)
        assertEquals(1, updated?.duration)
        assertEquals(newCalendar.timeInMillis, updated?.date)
    }

    @Test
    fun updateSessionFailInvalidID(){
        val calendar = Calendar.getInstance()
        val session = Session(0, calendar, 0, mutableListOf())

        val sessionID = sessionDBService.addSession(session)
        val sessionAdded = Session(1, calendar, sessionID, mutableListOf())
        assertSessionAdded(sessionAdded)

        val id = -1L
        val newCalendar = Calendar.getInstance()
        newCalendar.add(Calendar.DAY_OF_MONTH, -1)
        val newSession = Session(1, newCalendar, id, mutableListOf())
        sessionDBService.updateSession(newSession)

        val updated = sessionDBService.getSessionDataByID(id)
        assertEquals(0, updated?.duration)
        assertEquals(calendar.timeInMillis, updated?.date)
    }

    @Test
    fun updateSessionFailNegativeDuration(){
        val calendar = Calendar.getInstance()
        val session = Session(0, calendar, 0, mutableListOf())

        val sessionID = sessionDBService.addSession(session)
        val sessionAdded = Session(1, calendar, sessionID, mutableListOf())
        assertSessionAdded(sessionAdded)

        val id = -1L
        val newCalendar = Calendar.getInstance()
        newCalendar.add(Calendar.DAY_OF_MONTH, -1)
        val newSession = Session(-1, newCalendar, id, mutableListOf())
        sessionDBService.updateSession(newSession)

        val updated = sessionDBService.getSessionDataByID(id)
        assertEquals(0, updated?.duration)
        assertEquals(calendar.timeInMillis, updated?.date)
    }

    @Test
    fun updateSessionFailFutureDate(){
        val calendar = Calendar.getInstance()
        val session = Session(0, calendar, 0, mutableListOf())

        val sessionID = sessionDBService.addSession(session)
        val sessionAdded = Session(1, calendar, sessionID, mutableListOf())
        assertSessionAdded(sessionAdded)

        val id = -1L
        val newCalendar = Calendar.getInstance()
        newCalendar.add(Calendar.DAY_OF_MONTH, 1)
        val newSession = Session(1, newCalendar, id, mutableListOf())
        sessionDBService.updateSession(newSession)

        val updated = sessionDBService.getSessionDataByID(id)
        assertEquals(0, updated?.duration)
        assertEquals(calendar.timeInMillis, updated?.date)
    }

    @Test
    fun deleteSessionByID(){
        val calendar = Calendar.getInstance()
        val session = Session(0, calendar, 0, mutableListOf())
        val id = sessionDBService.addSession(session)
        val sessionAdded = Session(id, calendar, 0, mutableListOf())
        assertSessionAdded(sessionAdded)
        sessionDBService.deleteSessionByID(id)
        val deleted = sessionDBService.getSessionDataByID(id)
        assertEquals(null, deleted)
    }

    @Test
    fun deleteSessionByIDFailInvalidID(){
         val calendar = Calendar.getInstance()
        val session = Session(0, calendar, 0, mutableListOf())
        val id = sessionDBService.addSession(session)
        val sessionAdded = Session(id, calendar, 0, mutableListOf())
        assertSessionAdded(sessionAdded)
        val invalidID = -1L
        sessionDBService.deleteSessionByID(invalidID)
        val deleted = sessionDBService.getSessionDataByID(id)
        assertNotNull(deleted)
    }

    @Test
    fun getAllSessionsTest(){
        val calendar = Calendar.getInstance()
        val session1 = Session(0, calendar, 0, mutableListOf())
        val session2 = Session(0, calendar, 0, mutableListOf())
        val session3 = Session(0, calendar, 0, mutableListOf())

        val session1ID = sessionDBService.addSession(session1)
        val session2ID = sessionDBService.addSession(session2)
        val session3ID = sessionDBService.addSession(session3)

        val session1Added = Session(session1ID, calendar, 0, mutableListOf())
        val session2Added = Session(session2ID, calendar, 0, mutableListOf())
        val session3Added = Session(session3ID, calendar, 0, mutableListOf())

        assertSessionAdded(session1Added)
        assertSessionAdded(session2Added)
        assertSessionAdded(session3Added)

        val sessions = sessionDBService.getAllSessions()
        assertEquals(3, sessions.size)
    }

    @Test
    fun getAllSessionsTestNoSessions(){
        val sessions = sessionDBService.getAllSessions()
        assertEquals(0, sessions.size)
    }

    private fun assertSessionAdded(session: Session) {
        val getSession = sessionDBService.getSessionDataByID(session.id)
        assertNotNull(getSession)
        assertEquals(session.id, getSession?.id)
        assertEquals(session.getDuration(), getSession?.duration)
        assertEquals(session.getDate().timeInMillis, getSession?.date)
    }
}