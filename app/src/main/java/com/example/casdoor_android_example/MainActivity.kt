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
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.casdoor.Casdoor
import org.casdoor.CasdoorConfig

class MainActivity : AppCompatActivity() {

    private var casdoor: Casdoor? = null
    private var isLogin = false
    private var acToken = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
        tv_login.setOnClickListener {
            if (!isLogin) {
                val intent = Intent(this@MainActivity, WebViewActivity::class.java)
                intent.putExtra("url", casdoor?.getSignInUrl(scope = "profile"));
                resultLauncher.launch(intent)
            } else {
                pb_progress.visibility = View.VISIBLE
                Thread {
                    try {
                        val logout = casdoor?.logout(acToken, null)
                        if (logout == true) {
                            runOnUiThread {
                                tv_name.text = ""
                                tv_name.visibility = View.GONE
                                isLogin = false
                                tv_login.text = "Login with Casdoor"
                                pb_progress.visibility = View.GONE
                            }
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            pb_progress.visibility = View.GONE
                            tv_name?.text = e.message
                        }
                    }
                }.start()
            }
        }

    }

    private val launcherCallback = ActivityResultCallback<ActivityResult> { result ->
        val data = result.data
        if (result.resultCode == RESULT_OK) {
            val code = data?.getStringExtra("code")
            pb_progress.visibility = View.VISIBLE
            Thread {
                runOnUiThread {
                    tv_name?.text = "Loading..."
                }
                code?.let {
                    try {
                        acToken = casdoor?.requestOauthAccessToken(code)?.accessToken.toString()
                        val userData = casdoor?.getUserInfo(acToken)
                        runOnUiThread {
                            tv_name.text = userData?.name
                            tv_name.visibility = View.VISIBLE
                            isLogin = true
                            tv_login.text = "Logout"
                            pb_progress.visibility = View.GONE
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            pb_progress.visibility = View.GONE
                            tv_name?.text = e.message
                        }
                    }
                }
            }.start()

        }
    }


}