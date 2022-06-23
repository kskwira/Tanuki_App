package com.example.tanuki_mob

import android.content.ContentValues.TAG
import android.content.Intent
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.example.tanuki_mob.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    // keys for reading data from SharedPreferences
    private var CHOICES = "pref_numberOfChoices"
    private var SIGNS = "pref_signsToInclude"

    private var preferencesChanged = true // did preferences change?


    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // set the Toolbar
        setSupportActionBar(binding.toolbar)

        // set default values in the app's SharedPreferences
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)

        // register listener for SharedPreferences changes
        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(preferencesListener)

    }

    override fun onStart() {
        super.onStart()

        if (preferencesChanged) {

            // initialize MainActivityFragment and start the quiz after preferences changed
            val quizFragment: MainFragment? = supportFragmentManager
                .findFragmentById(R.id.quizFragment) as MainFragment?

            // update the number of guessRows
            quizFragment?.updateGuessRows(
                PreferenceManager.getDefaultSharedPreferences(this)
            )

            // update the signsSet
            quizFragment?.updateSigns(
                PreferenceManager.getDefaultSharedPreferences(this)
            )

            quizFragment?.resetQuiz()
            preferencesChanged = false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                val settingsIntent = Intent(this, SettingsActivity::class.java)
                startActivity(settingsIntent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // listener for changes to the app's SharedPreferences
    private var preferencesListener =
        OnSharedPreferenceChangeListener { sharedPreferences, key ->
            preferencesChanged = true
            val quizFragment: MainFragment? = supportFragmentManager
                .findFragmentById(R.id.quizFragment) as MainFragment?

            // update the number of guessRows and restart the quiz
            if (key == CHOICES) {
                quizFragment?.updateGuessRows(sharedPreferences)
                quizFragment?.resetQuiz()
                Log.i(TAG, "Number of choice changed")

            // update the signsSet and restart the quiz
            } else if (key == SIGNS) {
                quizFragment?.updateSigns(sharedPreferences)
                quizFragment?.resetQuiz()
                Log.i(TAG, "Signs set changed")
            }

            // display toast message for preferences changed
            Toast.makeText(applicationContext, R.string.restarting_quiz, Toast.LENGTH_SHORT).show()
        }

}
