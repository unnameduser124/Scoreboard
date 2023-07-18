package com.example.scoreboard.popups

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.scoreboard.MainActivity
import com.example.scoreboard.Tag
import com.example.scoreboard.database.TagDBService

class TagDetailsPopup(val context: Context, val tag: Tag) : ComponentActivity() {

    @Composable
    fun GeneratePopup(popupVisible: MutableState<Boolean>) {
        Popup(
            popupPositionProvider = WindowCenterOffsetPositionProvider(),
            onDismissRequest = { popupVisible.value = false },
            properties = PopupProperties(focusable = true)
        ) {
            TagDetailsPopupLayout(popupVisible)
        }
    }

    @Composable
    fun TagDetailsPopupLayout(popupVisible: MutableState<Boolean>) {
        Column(
            modifier = Modifier
                .widthIn(min = 0.200.dp, max = 300.dp)
                .background(Color.LightGray, RoundedCornerShape(16.dp)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            TagHeader()
            val buttonsModifier = Modifier
                .padding(horizontal = 10.dp)
                .widthIn(100.dp)
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ChangeNameButton(buttonsModifier)
                DeleteButton(popupVisible, buttonsModifier)
            }
        }
    }

    @Composable
    fun TagHeader() {
        Text(text = "Tag details", fontSize = 15.sp, modifier = Modifier.padding(top = 10.dp))
        Text(
            text = tag.tagName,
            fontSize = 20.sp,
            modifier = Modifier.padding(top = 10.dp, start = 10.dp, end = 10.dp),
            textAlign = TextAlign.Center
        )
    }

    @Composable
    fun ChangeNameButton(modifier: Modifier) {
        val dialogOpen = remember { mutableStateOf(false) }
        val newTagName = remember { mutableStateOf(tag.tagName) }
        Button(
            onClick = { dialogOpen.value = true }, modifier = modifier,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
            elevation = ButtonDefaults.elevation(0.dp)
        ) {
            Text(text = "Rename")
        }
        if (dialogOpen.value) {
            ChangeNameDialog(dialogOpen, newTagName)
        }
    }

    @Composable
    fun ChangeNameDialog(dialogOpen: MutableState<Boolean>, newTagName: MutableState<String>) {
        Dialog(onDismissRequest = { dialogOpen.value = false }) {
            Column(
                modifier = Modifier
                    .width(300.dp)
                    .background(Color.LightGray, RoundedCornerShape(16.dp))
                    .padding(top = 10.dp, bottom = 10.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Change name",
                    fontSize = 25.sp,
                    modifier = Modifier.padding(10.dp)
                )
                OutlinedTextField(
                    value = newTagName.value,
                    onValueChange = { newTagName.value = it },
                    label = { Text(text = "Tag name") },
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth(),
                    singleLine = true
                )
                Button(
                    onClick = {
                        if (newTagName.value != "") {
                            tag.tagName = newTagName.value
                            TagDBService(context).updateTag(tag)
                        }
                        dialogOpen.value = false
                    },
                    modifier = Modifier.padding(10.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
                    elevation = ButtonDefaults.elevation(0.dp)
                ) {
                    Text(text = "Change name")
                }
            }
        }
    }

    @Composable
    fun DeleteButton(popupVisible: MutableState<Boolean>, modifier: Modifier) {
        Button(
            onClick = {
                TagDBService(context).deleteTagByID(tag.id)
                popupVisible.value = false
                MainActivity.activitiesDataUpdate.value = true
            },
            modifier = modifier,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
            elevation = ButtonDefaults.elevation(0.dp)
        ) {
            Text(text = "Delete")
        }
    }
}