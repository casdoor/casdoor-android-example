/*
 * Copyright 2022 The Casdoor Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.casdoor_android_example

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import org.casdoor.Casdoor
import org.casdoor.CasdoorConfig

class MainActivity : AppCompatActivity() {
    private var casdoor: Casdoor? = null
    private var info: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        info = findViewById<TextView>(R.id.info)
        val next = findViewById<Button>(R.id.start)

        val casdoorConfig = CasdoorConfig(
            endpoint = "https://door.casdoor.com",
            clientID = "014ae4bd048734ca2dea",
            organizationName = "casbin",
            redirectUri = "casdoor://callback",
            appName = "app-casnode"
        )
        casdoor = Casdoor(casdoorConfig)
        val resultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            launcherCallback
        )
        next.setOnClickListener {
            val intent = Intent(this@MainActivity, WebViewActivity::class.java)
            intent.putExtra("url", casdoor?.getSignInUrl());
            resultLauncher.launch(intent)
        }

    }

    private val launcherCallback = ActivityResultCallback<ActivityResult> { result ->
        val data = result.data
        if (result.resultCode == RESULT_OK) {
            val code = data?.getStringExtra("code")
            info?.text = code
            Thread {
                code?.let {
                    val token = casdoor?.requestOauthAccessToken(code)
                    runOnUiThread { info?.text = token.toString() }
                }
            }.start()

        }
    }


}