package com.example.scoreboard

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.example.scoreboard.database.DatabaseConstants
import com.example.scoreboard.database.SessionDBService
import com.example.scoreboard.database.SessionTagDBService
import com.example.scoreboard.database.TagDBService
import com.example.scoreboard.session.Session
import junit.framework.TestCase.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Calendar

class SessionTagAutomationTests {

    private lateinit var sessionTagDBService: SessionTagDBService
    private lateinit var tagDBService: TagDBService
    private lateinit var sessionDBService: SessionDBService
    private lateinit var applicationContext: Context

    @Before
    fun setUp() {
        applicationContext = InstrumentationRegistry.getInstrumentation().targetContext
        sessionTagDBService = SessionTagDBService(applicationContext)
        tagDBService = TagDBService(applicationContext)
        sessionDBService = SessionDBService(applicationContext)
    }

    @After
    fun tearDown() {
        sessionTagDBService.close()
        applicationContext.deleteDatabase(DatabaseConstants.DATABASE_NAME)
    }

    @Test
    fun addSessionTagsOnSessionAdd(){
        val tag = Tag("tag_name", -1)
        val tagID = tagDBService.addTag(tag)
        val addedTag = tagDBService.getTagByID(tagID)

        val tag2 = Tag("tag_name2", -1)
        val tagID2 = tagDBService.addTag(tag2)
        val addedTag2 = tagDBService.getTagByID(tagID2)

        val session = Session(0, Calendar.getInstance(), -1, mutableListOf(addedTag!!, addedTag2!!))
        val sessionID = sessionDBService.addSession(session)

        val sessionTags = sessionTagDBService.getTagIDsForSession(sessionID)
        assertEquals(2, sessionTags.size)
    }

    @Test
    fun addSessionTagsOnSessionAddNoSessionTags(){
        val session = Session(0, Calendar.getInstance(), -1, mutableListOf())
        val sessionID = sessionDBService.addSession(session)

        val sessionTags = sessionTagDBService.getTagIDsForSession(sessionID)
        assertEquals(0, sessionTags.size)
    }

    @Test
    fun addSessionTagsOnSessionAddInvalidTagID(){
        val tag = Tag("tag_name", -1)
        val session = Session(0, Calendar.getInstance(), -1, mutableListOf(tag))
        val sessionID = sessionDBService.addSession(session)

        val sessionTags = sessionTagDBService.getTagIDsForSession(sessionID)
        assertEquals(0, sessionTags.size)
    }

    @Test
    fun deleteSessionTagsOnSessionDelete(){
        val tag = Tag("tag_name", -1)
        val tagID = tagDBService.addTag(tag)
        val addedTag = tagDBService.getTagByID(tagID)

        val tag2 = Tag("tag_name2", -1)
        val tagID2 = tagDBService.addTag(tag2)
        val addedTag2 = tagDBService.getTagByID(tagID2)

        val session = Session(0, Calendar.getInstance(), -1, mutableListOf(addedTag!!, addedTag2!!))
        val sessionID = sessionDBService.addSession(session)

        sessionDBService.deleteSessionByID(sessionID)

        val sessionTags = sessionTagDBService.getTagIDsForSession(sessionID)
        assertEquals(0, sessionTags.size)
    }

    @Test
    fun deleteSessionTagsOnSessionDeleteNoSessionTags(){
        val session = Session(0, Calendar.getInstance(), -1, mutableListOf())
        val sessionID = sessionDBService.addSession(session)

        sessionDBService.deleteSessionByID(sessionID)

        val sessionTags = sessionTagDBService.getTagIDsForSession(sessionID)
        assertEquals(0, sessionTags.size)
    }

    @Test
    fun deleteSessionTagsOnSessionDeleteInvalidTagID(){
        val tag = Tag("tag_name", -1)
        val session = Session(0, Calendar.getInstance(), -1, mutableListOf(tag))
        val sessionID = sessionDBService.addSession(session)

        sessionDBService.deleteSessionByID(sessionID)

        val sessionTags = sessionTagDBService.getTagIDsForSession(sessionID)
        assertEquals(0, sessionTags.size)
    }
}