package com.example.tanuki_mob

import androidx.fragment.app.testing.FragmentScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class MainFragmentTest {

    @Before
    fun setUp() {
    }

    @Test
    fun test_cardFrontInView() {
        FragmentScenario.launchInContainer(MainFragment::class.java)
        onView(withId(R.id.card_front)).check(matches(isDisplayed()))
    }

    @After
    fun tearDown() {
    }
}