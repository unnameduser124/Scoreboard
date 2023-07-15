package com.example.scoreboard

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scoreboard.database.SessionDBService
import com.example.scoreboard.popups.SessionDetailsPopup
import com.example.scoreboard.session.Session

class HistoryTab(val context: Context) : ComponentActivity() {

    private lateinit var sessions: SnapshotStateList<Session>

    @Composable
    fun GenerateLayout() {
        sessions = remember {SnapshotStateList<Session>()}
        var tempSessions = SessionDBService(context).getAllSessions()
        tempSessions = tempSessions.sortedByDescending{ it.getDate().timeInMillis }
        sessions.clear()
        sessions.addAll(tempSessions)
        HistoryTabLayout()
    }

    @Composable
    fun HistoryTabLayout() {
        if(MainActivity.historyDataUpdate.value){
            var tempSessions = SessionDBService(context).getAllSessions()
            tempSessions = tempSessions.sortedByDescending{ it.getDate().timeInMillis }
            sessions.clear()
            sessions.addAll(tempSessions)
            MainActivity.historyDataUpdate.value = false
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            SessionsHistoryList()
        }
    }

    @Composable
    fun SessionsHistoryList() {
        LazyColumn {
            items(sessions.size) {
                SessionItem(sessions[it])
            }
        }
    }

    @Composable
    fun SessionItem(session: Session) {
        val sessionDetailsPopupVisible = remember { mutableStateOf(false) }
        Row(
            modifier = Modifier
                .padding(start = 10.dp, end = 10.dp, bottom = 10.dp, top = 10.dp)
                .fillMaxWidth()
                .clickable {
                    sessionDetailsPopupVisible.value = true
                },
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SessionTagsList(session.tags)
            Text(text = durationInSecondsToHoursAndMinutes(session.getDuration()), fontSize = 20.sp)
        }
        if (sessionDetailsPopupVisible.value) {
            SessionDetailsPopup(context, session).GeneratePopup(sessionDetailsPopupVisible)
        }
    }

    @Composable
    fun SessionTagsList(tags: MutableList<Tag>) {
        LazyRow(modifier = Modifier.width(275.dp)) {
            items(tags.size) {
                Text(
                    text = tags[it].tagName,
                    modifier = Modifier.padding(start = 10.dp, end = 10.dp),
                    fontSize = 20.sp
                )
            }
        }
    }
}