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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.casdoor_android_example.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.casdoor.Casdoor
import org.casdoor.CasdoorConfig
import java.io.IOException
import java.security.SecureRandom

class MainActivity : AppCompatActivity() {

    // The Casdoor application to sign in with, the defaults are the public demo server https://door.casdoor.com
    private val casdoor = Casdoor(
        CasdoorConfig(
            endpoint = "https://door.casdoor.com",
            clientID = "014ae4bd048734ca2dea",
            organizationName = "casbin",
            // must be in the Redirect URLs of the application, WebViewActivity catches it
            redirectUri = REDIRECT_URI,
            appName = "app-casnode"
        )
    )

    private lateinit var binding: ActivityMainBinding
    private var accessToken: String? = null

    // WebViewActivity returns the code that Casdoor redirected back with
    private val signInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data = result.data
            if (result.resultCode == RESULT_OK && data != null) {
                val code = data.getStringExtra(WebViewActivity.EXTRA_CODE)
                if (code != null) {
                    signIn(code)
                } else {
                    showSignedOut(data.getStringExtra(WebViewActivity.EXTRA_ERROR))
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvLogin.setOnClickListener {
            if (accessToken == null) {
                openSignInPage()
            } else {
                signOut()
            }
        }
    }

    private fun openSignInPage() {
        // getSignInUrl() starts a new PKCE flow: only this Casdoor instance knows the code verifier.
        // The random state ties the redirect to this sign-in.
        val state = randomState()
        val intent = Intent(this, WebViewActivity::class.java)
            .putExtra(WebViewActivity.EXTRA_URL, casdoor.getSignInUrl(scope = "profile", state = state))
            .putExtra(WebViewActivity.EXTRA_REDIRECT_URI, REDIRECT_URI)
            .putExtra(WebViewActivity.EXTRA_STATE, state)
        signInLauncher.launch(intent)
    }

    private fun signIn(code: String) {
        binding.pbProgress.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                // the SDK makes blocking network calls
                val (token, user) = withContext(Dispatchers.IO) {
                    val token = casdoor.requestOauthAccessToken(code).accessToken
                        ?: throw IOException("No access token in the response")
                    token to casdoor.getUserInfo(token)
                }
                accessToken = token
                binding.tvName.text = getString(R.string.username, user?.name)
                binding.tvName.visibility = View.VISIBLE
                binding.tvLogin.setText(R.string.logout)
            } catch (e: Exception) {
                showSignedOut(e.message)
            }
            binding.pbProgress.visibility = View.GONE
        }
    }

    private fun signOut() {
        val token = accessToken ?: return
        binding.pbProgress.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) { casdoor.logout(token) }
            } catch (e: Exception) {
                // the Casdoor session may already have ended
            }
            showSignedOut(null)
            binding.pbProgress.visibility = View.GONE
        }
    }

    private fun showSignedOut(error: String?) {
        accessToken = null
        binding.tvName.text = error
        binding.tvName.visibility = if (error == null) View.GONE else View.VISIBLE
        binding.tvLogin.setText(R.string.login)
    }

    private fun randomState(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    companion object {
        private const val REDIRECT_URI = "casdoor://callback"
    }
}
