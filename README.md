# AR Multiplayer Escape Room

This diploma project represents an Android client and cloud infrastructure for a multiplayer, augmented reality (AR) escape room game. The application allows users located in different physical locations to collaborate in real-time to solve puzzles in an asymmetric environment (assuming the roles of Elara and Julian).

## Main Features

* Asymmetric Multiplayer Gameplay: Two players experience different scenarios, holding distinct information and objects, compelling them to communicate to find the solution.
* Native AR Integration (UaaL): The Unity AR game module is integrated as a library (.aar) directly into the native Android application. It runs in a separate process to prevent ANR (Application Not Responding) errors and to ensure a fluid transition between the app menu and the game.
* Authentication and Account Management: Secure Google authentication using the Credential Manager API, with user profile management handled by the backend.
* Lobby and Invite System: Features unique room code generation and a real-time invite system with a 10-minute automatic expiration (TTL).
* Multiplayer Synchronization: Inventory sharing, voice communication, and text messaging at low latency, entirely managed through Photon.
* Global Leaderboard and Statistics: The backend aggregates session data (completion time, messages sent, voice communication duration, hints used) to generate personal statistics and a team-based leaderboard, optimized using compound indexes in the database.

## Architecture and Technologies

The system is divided into three main layers:

1. Android Client (User Interface):
   * Kotlin and Jetpack Compose for a modern, state-based UI.
   * Custom lifecycle management for the isolated game process.

2. Unity AR Game (Immersive Experience):
   * Unity 6 and ARCore for rendering three-dimensional objects in the physical environment.
   * Photon (PUN and Voice) for synchronizing actions and communication within the shared game room.

3. Backend and Cloud Services (Business Logic):
   * Server: Ktor (REST API) based on coroutines for routing the lobby, sessions, and leaderboard.
   * Database: MongoDB (document-based) utilizing four main collections (users, rooms, game_sessions, invites).
   * Networking: Tunneling via ngrok to expose the local server to mobile clients.
