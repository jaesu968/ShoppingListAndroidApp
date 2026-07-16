package com.example.shoppinglist

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

// Swaps Dispatchers.Main for a test dispatcher so viewModelScope works off-device.
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    // TestWatched is a JUnit rule base clas that gives you hooks around each test

    // starting swaps in a test dispatcher before the test
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }
    // finished restores the real one after the test
    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
