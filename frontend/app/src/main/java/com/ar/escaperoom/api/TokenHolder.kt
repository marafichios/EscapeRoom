package com.ar.escaperoom.api


//pastrez token sesiune de la backend
object TokenHolder {
    @Volatile //singleton in memory pt o sesiune pt ca nu avem login persistent
    var token: String? = null
}
