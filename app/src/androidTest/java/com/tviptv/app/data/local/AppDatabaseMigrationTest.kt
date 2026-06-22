package com.tviptv.app.data.local

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {

    @Test
    fun migrate1To2_preservesLastWatchedData() {
        openVersionOneDatabase().apply {
            execSQL(
                "INSERT INTO last_watched (sourceId, channelId, watchedAt) VALUES (1, 'channel-1', 12345)",
            )
            MIGRATION_1_2.migrate(this)
            query("SELECT sourceId, channelId, watchedAt FROM last_watched").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(1, cursor.getLong(0))
                assertEquals("channel-1", cursor.getString(1))
                assertEquals(12345, cursor.getLong(2))
            }
            close()
        }
    }

    @Test
    fun migrate1To2_allowsMultipleRowsPerSource() {
        openVersionOneDatabase().apply {
            execSQL(
                "INSERT INTO last_watched (sourceId, channelId, watchedAt) VALUES (1, 'channel-1', 100)",
            )
            MIGRATION_1_2.migrate(this)
            execSQL(
                "INSERT INTO last_watched (sourceId, channelId, watchedAt) VALUES (1, 'channel-2', 200)",
            )
            query("SELECT COUNT(*) FROM last_watched WHERE sourceId = 1").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(2, cursor.getInt(0))
            }
            close()
        }
    }

    @Test
    fun migrate6To7_addsLastWatchedMetadataColumns() {
        openVersionSixDatabase().apply {
            execSQL(
                """
                INSERT INTO last_watched (
                    sourceId, channelId, contentType, watchedAt, playbackPositionMs, durationMs
                ) VALUES (1, 'channel-1', 'LIVE', 12345, 0, 0)
                """.trimIndent(),
            )
            MIGRATION_6_7.migrate(this)
            query("PRAGMA table_info(last_watched)").use { cursor ->
                val columns = mutableSetOf<String>()
                while (cursor.moveToNext()) {
                    columns.add(cursor.getString(1))
                }
                assertTrue(columns.contains("title"))
                assertTrue(columns.contains("imageUrl"))
                assertTrue(columns.contains("categoryId"))
                assertTrue(columns.contains("seriesId"))
                assertTrue(columns.contains("seriesName"))
            }
            close()
        }
    }

    private fun openVersionSixDatabase(): SupportSQLiteDatabase {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase(TEST_DB_V6)
        val configuration = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(TEST_DB_V6)
            .callback(
                object : SupportSQLiteOpenHelper.Callback(6) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        db.execSQL(
                            """
                            CREATE TABLE last_watched (
                                sourceId INTEGER NOT NULL,
                                channelId TEXT NOT NULL,
                                contentType TEXT NOT NULL,
                                watchedAt INTEGER NOT NULL,
                                playbackPositionMs INTEGER NOT NULL DEFAULT 0,
                                durationMs INTEGER NOT NULL DEFAULT 0,
                                PRIMARY KEY(sourceId, channelId, contentType)
                            )
                            """.trimIndent(),
                        )
                    }

                    override fun onUpgrade(
                        db: SupportSQLiteDatabase,
                        oldVersion: Int,
                        newVersion: Int,
                    ) = Unit
                },
            )
            .build()
        return FrameworkSQLiteOpenHelperFactory().create(configuration).writableDatabase
    }

    private fun openVersionOneDatabase(): SupportSQLiteDatabase {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase(TEST_DB)
        val configuration = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(TEST_DB)
            .callback(
                object : SupportSQLiteOpenHelper.Callback(1) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        db.execSQL(
                            """
                            CREATE TABLE last_watched (
                                sourceId INTEGER NOT NULL PRIMARY KEY,
                                channelId TEXT NOT NULL,
                                watchedAt INTEGER NOT NULL
                            )
                            """.trimIndent(),
                        )
                    }

                    override fun onUpgrade(
                        db: SupportSQLiteDatabase,
                        oldVersion: Int,
                        newVersion: Int,
                    ) = Unit
                },
            )
            .build()
        return FrameworkSQLiteOpenHelperFactory().create(configuration).writableDatabase
    }

    companion object {
        private const val TEST_DB = "migration-test"
        private const val TEST_DB_V6 = "migration-test-v6"
    }
}
