// MongoDB initialization script
db = db.getSiblingDB("ar_escape_room");

// Create collections
db.createCollection("users");
db.createCollection("game_sessions");
db.createCollection("rooms");
db.createCollection("achievements");

// Create indexes
db.users.createIndex({ email: 1 }, { unique: true });
db.users.createIndex({ username: 1 });

// Insert sample data (optional)
db.users.insertOne({
  username: "demo_user",
  email: "demo@arescaperoom.com",
  password: "demo123",
  createdAt: NumberLong(Date.now()),
  gameProgress: {
    currentLevel: 1,
    completedRooms: [],
    score: 0,
    achievements: [],
    lastPlayedAt: NumberLong(Date.now()),
  },
});

print("Database initialized successfully!");
