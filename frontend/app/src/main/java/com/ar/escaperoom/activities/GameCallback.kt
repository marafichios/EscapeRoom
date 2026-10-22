package com.ar.escaperoom.activities

import androidx.annotation.Keep

//singleton accesibil din unity prin apel static ca unity sa le gaseasc dupa nume
object GameCallback {

    // terminarea jocului
    var onGameFinished: ((statsJson: String) -> Unit)? = null


    @Keep
    @JvmStatic //pt call static din c#
    fun notifyGameFinished(statsJson: String) {
        onGameFinished?.invoke(statsJson)
    }
}
