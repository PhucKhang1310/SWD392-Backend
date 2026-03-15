const mongoose = require("mongoose");

let cached = global.__mongoose_cache;
if (!cached) {
  cached = global.__mongoose_cache = { conn: null, promise: null };
}

const connectDB = async () => {
  if (cached.conn) return cached.conn;

  const uri = process.env.MONGO_URI;
  if (!uri) {
    throw new Error(
      "MONGO_URI chưa được cấu hình trong Environment Variables",
    );
  }

  if (!cached.promise) {
    cached.promise = mongoose
      .connect(uri)
      .then((conn) => {
        console.log(`MongoDB Atlas Connected: ${conn.connection.host}`);
        return conn;
      })
      .catch((err) => {
        cached.promise = null;
        throw err;
      });
  }

  cached.conn = await cached.promise;
  return cached.conn;
};

module.exports = connectDB;
