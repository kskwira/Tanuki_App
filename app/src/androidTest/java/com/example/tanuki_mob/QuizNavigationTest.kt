package com.example.tanuki_mob

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.pressBack
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class QuizNavigationTest {

    @Before
    fun setUp() {
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @Test
    fun test_ToSettingsNavigation() {
        onView(withId(R.id.action_settings)).perform(click())
        onView(withId(R.id.content_settings)).check(matches(isDisplayed()))
    }

    @Test
    fun test_BackFromSettingsNavigation() {
        onView(withId(R.id.action_settings)).perform(click())
        onView(withId(R.id.content_settings)).perform(pressBack())
        onView(withId(R.id.quizFragment)).check(matches(isDisplayed()))
    }

    @Test
    fun test_ChangeNumberOfAnswers() {
        onView(withId(R.id.action_settings)).perform(click())
    }

}
