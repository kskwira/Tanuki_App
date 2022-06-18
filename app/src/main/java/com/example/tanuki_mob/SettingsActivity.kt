package com.example.tanuki_mob

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.tanuki_mob.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.activitySettings)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.content_settings, SettingsFragment())
            .commit()
    }
}