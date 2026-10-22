package com.ar.escaperoom.activities

import com.unity3d.player.UnityPlayerGameActivity

//inchei activ unity si revin la lobby
//default ar reveni la home
class MyUnityActivity : UnityPlayerGameActivity() {

    override fun onUnityPlayerUnloaded() {
        // revin la lobby
        runOnUiThread { finish() }
    }
}
