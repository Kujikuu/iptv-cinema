package com.tviptv.app.ui.setup

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.commit
import com.tviptv.app.R
import com.tviptv.app.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import androidx.fragment.app.FragmentActivity

@AndroidEntryPoint
class SetupActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.setup_container, SetupChooseFragment())
            }
        }
    }

    fun finishSetup() {
        if (isTaskRoot) {
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            setResult(RESULT_OK)
        }
        finish()
    }
}
