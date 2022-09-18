package com.example.casdoor_android_example

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
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
        next.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {

                try {
                    val intent = Intent(this@MainActivity, WebViewActivity::class.java)
                    intent.putExtra("url", casdoor?.getSignInUrl());
                    startActivityForResult(intent, 10001)
                } catch (e: Exception) {
                    println("The current phone does not have a browser installed")
                }

            }
        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == 10001) {
            val code = data?.getStringExtra("code")
            info?.text = code
            Thread(object : Runnable {
                override fun run() {

                    code?.let {
                        val token = casdoor?.requestOauthAccessToken(code)
                     runOnUiThread(object :Runnable{
                         override fun run() {
                             info?.text = token.toString()
                         }

                     })
                    }
                }
            }).start()

        }
    }
}