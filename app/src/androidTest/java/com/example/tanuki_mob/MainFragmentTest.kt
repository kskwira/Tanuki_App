package com.example.tanuki_mob

import androidx.fragment.app.testing.FragmentScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.PositionAssertions.isCompletelyLeftOf
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class MainFragmentTest {

    @Before
    fun setUp() {
        FragmentScenario.launchInContainer(MainFragment::class.java)
    }

    @Test
    fun test_cardFrontInView() {
        onView(withId(R.id.card_front)).check(matches(isDisplayed()))
    }

    @Test
    fun test_buttonPresent() {
        onView(withId(R.id.button1)).check((matches(isDisplayed())))
    }

    @Test
    fun test_buttonClickable() {
        onView(withId(R.id.button1)).check((matches(isClickable())))
    }

    @Test
    fun test_buttonsInLine() {
        onView(withId(R.id.button1)).check((isCompletelyLeftOf(withId(R.id.button2))))
    }

    @After
    fun tearDown() {
    }
}