package com.example.apriltagapp

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.apriltagapp.model.Stadium
import com.example.apriltagapp.model.repository.StadiumRepository

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    val repository = StadiumRepository()

    @Test
    fun initStadium() {
        val nodes = repository.nodes
        assertEquals(nodes, 1)
    }

    @Test
    fun 다익스트라_테스트() {

    }
}