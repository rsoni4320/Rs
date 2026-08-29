package com.example

import com.example.data.local.entities.CustomCommandEntity
import com.example.voice.ActionType
import com.example.voice.CommandParser
import org.junit.Assert.assertEquals
import org.junit.Test

class CommandParserTest {

    private val parser = CommandParser()

    @Test
    fun testTimeCommand() {
        val result = parser.parse("Jarvis what time is it")
        assertEquals(ActionType.TIME, result.actionType)
    }

    @Test
    fun testDateCommand() {
        val result = parser.parse("hey jarvis, what is today's date")
        assertEquals(ActionType.DATE, result.actionType)
    }

    @Test
    fun testBatteryCommand() {
        val result = parser.parse("jarvis check battery status")
        assertEquals(ActionType.CHECK_BATTERY, result.actionType)
    }

    @Test
    fun testSearchCommand() {
        val result = parser.parse("search how to build an android app")
        assertEquals(ActionType.SEARCH_WEB, result.actionType)
        assertEquals("how to build an android app", result.payload)
    }

    @Test
    fun testOpenAppCommand() {
        val result = parser.parse("open youtube")
        assertEquals(ActionType.OPEN_APP, result.actionType)
        assertEquals("youtube", result.payload)
    }

    @Test
    fun testCreateNoteCommand() {
        val result = parser.parse("create a note review quantum computing architecture")
        assertEquals(ActionType.CREATE_NOTE, result.actionType)
        assertEquals("review quantum computing architecture", result.payload)
    }

    @Test
    fun testRememberCommand() {
        val result = parser.parse("remember that my primary focus is robotics")
        assertEquals(ActionType.REMEMBER_FACT, result.actionType)
        assertEquals("my primary focus is robotics", result.payload)
    }

    @Test
    fun testCreateTaskCommand() {
        val result = parser.parse("create a task finish unit tests")
        assertEquals(ActionType.CREATE_TASK, result.actionType)
        assertEquals("finish unit tests", result.payload)
    }

    @Test
    fun testCustomCommand() {
        val customCommands = listOf(
            CustomCommandEntity(
                phrase = "matrix protocol",
                actionType = "OPEN_URL",
                targetPayload = "https://github.com",
                description = "Launch GitHub"
            )
        )
        val result = parser.parse("jarvis matrix protocol", customCommands)
        assertEquals(ActionType.CUSTOM_COMMAND, result.actionType)
        assertEquals("https://github.com", result.payload)
    }

    @Test
    fun testAiFallback() {
        val result = parser.parse("Explain general relativity in three bullet points")
        assertEquals(ActionType.ASK_AI, result.actionType)
    }
}
