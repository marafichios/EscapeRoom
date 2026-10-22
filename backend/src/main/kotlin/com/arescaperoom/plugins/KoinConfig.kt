package com.arescaperoom.plugins

import com.arescaperoom.domain.invites.IInviteRepository
import com.arescaperoom.domain.rooms.IRoomRepository
import com.arescaperoom.domain.sessions.IGameSessionRepository
import com.arescaperoom.domain.users.IUserRepository
import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import com.arescaperoom.infrastructure.users.MongoRepositoryUser
import com.arescaperoom.infrastructure.rooms.MongoRepositoryRoom
import com.arescaperoom.infrastructure.sessions.MongoRepositoryGameSession
import com.arescaperoom.infrastructure.invites.MongoRepositoryInvite
import com.arescaperoom.services.GameSessionService
import com.arescaperoom.services.InviteService
import com.arescaperoom.services.RoomService
import com.arescaperoom.services.UserService

val databaseModule = module {
    single<CoroutineClient> {
        val mongoHost     = System.getenv("MONGODB_HOST")     ?: "localhost"
        val mongoPort     = System.getenv("MONGODB_PORT")     ?: "27017"
        val mongoUsername = System.getenv("MONGODB_USERNAME") ?: "admin"
        val mongoPassword = System.getenv("MONGODB_PASSWORD") ?: "admin123"
        val mongoDatabase = System.getenv("MONGODB_DATABASE") ?: "ar_escape_room"
        val connectionString = "mongodb://$mongoUsername:$mongoPassword@$mongoHost:$mongoPort/$mongoDatabase?authSource=admin"
        KMongo.createClient(connectionString).coroutine
    }
    single<CoroutineDatabase> {
        val dbName = System.getenv("MONGODB_DATABASE") ?: "ar_escape_room"
        get<CoroutineClient>().getDatabase(dbName)
    }
}

val repositoryModule = module {
    single<IUserRepository>        { MongoRepositoryUser(get()) }
    single<IRoomRepository>        { MongoRepositoryRoom(get()) }
    single<IGameSessionRepository> { MongoRepositoryGameSession(get()) }
    single<IInviteRepository>      { MongoRepositoryInvite(get()) }
}

val serviceModule = module {
    single { UserService(get()) }
    single { RoomService(get()) }
    single { GameSessionService(get()) }
    single { InviteService(get()) }
}

fun Application.configureKoin() {
    install(Koin) {
        slf4jLogger()
        modules(databaseModule, repositoryModule, serviceModule)
    }
}
