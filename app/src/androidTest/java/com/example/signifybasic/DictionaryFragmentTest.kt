package com.example.signifybasic

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.fragment.app.testing.launchFragmentInContainer
import com.example.signifybasic.features.tabs.dictionary.DictionaryFragment
import org.junit.Test
import org.junit.runner.RunWith

class DictionaryFragmentTest {

    @Test
    fun testValidSign_displaysVideo() {
        launchFragmentInContainer<DictionaryFragment>(
            themeResId = R.style.AppTheme // ← replace with your actual app theme
        )

        onView(withId(R.id.searchInput)).perform(typeText("hello"), closeSoftKeyboard())
        onView(withId(R.id.searchButton)).perform(click())

        onView(withId(R.id.videoWebView)).check(matches(isDisplayed()))
        onView(withId(R.id.errorMessage)).check(matches(withEffectiveVisibility(Visibility.GONE)))
    }

    @Test
    fun testInvalidSign_displaysErrorMessage() {
        launchFragmentInContainer<DictionaryFragment>(
            themeResId = R.style.AppTheme // ← replace with your actual app theme
        )

        onView(withId(R.id.searchInput)).perform(typeText("invalidsign"), closeSoftKeyboard())
        onView(withId(R.id.searchButton)).perform(click())

        onView(withId(R.id.errorMessage)).check(matches(isDisplayed()))
        onView(withId(R.id.videoWebView)).check(matches(withEffectiveVisibility(Visibility.GONE)))
    }
}
