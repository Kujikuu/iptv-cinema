package com.afifistudio.iptvcinema.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS last_watched_new (
                sourceId INTEGER NOT NULL,
                channelId TEXT NOT NULL,
                watchedAt INTEGER NOT NULL,
                PRIMARY KEY(sourceId, channelId)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO last_watched_new (sourceId, channelId, watchedAt)
            SELECT sourceId, channelId, watchedAt FROM last_watched
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE last_watched")
        db.execSQL("ALTER TABLE last_watched_new RENAME TO last_watched")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS categories_new (
                sourceId INTEGER NOT NULL,
                externalId TEXT NOT NULL,
                name TEXT NOT NULL,
                contentType TEXT NOT NULL DEFAULT 'LIVE',
                PRIMARY KEY(sourceId, externalId, contentType)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO categories_new (sourceId, externalId, name, contentType)
            SELECT sourceId, externalId, name, 'LIVE' FROM categories
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE categories")
        db.execSQL("ALTER TABLE categories_new RENAME TO categories")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS channels_new (
                sourceId INTEGER NOT NULL,
                externalId TEXT NOT NULL,
                name TEXT NOT NULL,
                logoUrl TEXT,
                categoryId TEXT,
                streamUrl TEXT,
                sortOrder INTEGER NOT NULL,
                contentType TEXT NOT NULL DEFAULT 'LIVE',
                containerExtension TEXT,
                plot TEXT,
                PRIMARY KEY(sourceId, externalId, contentType)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO channels_new (
                sourceId, externalId, name, logoUrl, categoryId, streamUrl, sortOrder, contentType
            )
            SELECT sourceId, externalId, name, logoUrl, categoryId, streamUrl, sortOrder, 'LIVE'
            FROM channels
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE channels")
        db.execSQL("ALTER TABLE channels_new RENAME TO channels")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS favorites_new (
                sourceId INTEGER NOT NULL,
                channelId TEXT NOT NULL,
                contentType TEXT NOT NULL DEFAULT 'LIVE',
                addedAt INTEGER NOT NULL,
                PRIMARY KEY(sourceId, channelId, contentType)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO favorites_new (sourceId, channelId, contentType, addedAt)
            SELECT sourceId, channelId, 'LIVE', addedAt FROM favorites
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE favorites")
        db.execSQL("ALTER TABLE favorites_new RENAME TO favorites")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS last_watched_new (
                sourceId INTEGER NOT NULL,
                channelId TEXT NOT NULL,
                contentType TEXT NOT NULL DEFAULT 'LIVE',
                watchedAt INTEGER NOT NULL,
                PRIMARY KEY(sourceId, channelId, contentType)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO last_watched_new (sourceId, channelId, contentType, watchedAt)
            SELECT sourceId, channelId, 'LIVE', watchedAt FROM last_watched
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE last_watched")
        db.execSQL("ALTER TABLE last_watched_new RENAME TO last_watched")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE channels ADD COLUMN addedAt INTEGER")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE last_watched ADD COLUMN playbackPositionMs INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            "ALTER TABLE last_watched ADD COLUMN durationMs INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS epg_programs (
                sourceId INTEGER NOT NULL,
                streamId TEXT NOT NULL,
                title TEXT NOT NULL,
                description TEXT,
                startMs INTEGER NOT NULL,
                endMs INTEGER NOT NULL,
                fetchedAt INTEGER NOT NULL,
                PRIMARY KEY(sourceId, streamId, startMs)
            )
            """.trimIndent(),
        )
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE last_watched ADD COLUMN title TEXT")
        db.execSQL("ALTER TABLE last_watched ADD COLUMN imageUrl TEXT")
        db.execSQL("ALTER TABLE last_watched ADD COLUMN categoryId TEXT")
        db.execSQL("ALTER TABLE last_watched ADD COLUMN seriesId TEXT")
        db.execSQL("ALTER TABLE last_watched ADD COLUMN seriesName TEXT")
    }
}

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE channels ADD COLUMN channelNumber INTEGER")

        db.execSQL(
            """
            CREATE VIRTUAL TABLE IF NOT EXISTS channels_fts USING fts4(
                name,
                content='channels',
                tokenize=unicode61
            )
            """.trimIndent(),
        )
        db.execSQL("INSERT INTO channels_fts(rowid, name) SELECT rowid, name FROM channels")
        db.execSQL(
            """
            CREATE TRIGGER IF NOT EXISTS channels_fts_ai AFTER INSERT ON channels BEGIN
                INSERT INTO channels_fts(rowid, name) VALUES (new.rowid, new.name);
            END
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TRIGGER IF NOT EXISTS channels_fts_ad AFTER DELETE ON channels BEGIN
                INSERT INTO channels_fts(channels_fts, rowid, name) VALUES('delete', old.rowid, old.name);
            END
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TRIGGER IF NOT EXISTS channels_fts_au AFTER UPDATE ON channels BEGIN
                INSERT INTO channels_fts(channels_fts, rowid, name) VALUES('delete', old.rowid, old.name);
                INSERT INTO channels_fts(rowid, name) VALUES (new.rowid, new.name);
            END
            """.trimIndent(),
        )
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_channels_source_content_category
            ON channels(sourceId, contentType, categoryId)
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_channels_category_sort
            ON channels(sourceId, contentType, categoryId, addedAt, sortOrder, name)
            """.trimIndent(),
        )
    }
}
