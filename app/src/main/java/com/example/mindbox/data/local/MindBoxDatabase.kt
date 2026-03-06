package com.example.mindbox.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.mindbox.data.local.converter.RoomTypeConverters
import com.example.mindbox.data.local.dao.*
import com.example.mindbox.data.local.entity.*
import com.example.mindbox.security.DatabasePassphraseProvider
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

@Database(
    entities = [
        RawEntryEntity::class,
        EventEntity::class,
        NoteEntity::class,
        PersonEntity::class,
        OrganizationEntity::class,
        EmbeddingEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(RoomTypeConverters::class)
abstract class MindBoxDatabase : RoomDatabase() {
    abstract fun rawEntryDao(): RawEntryDao
    abstract fun eventDao(): EventDao
    abstract fun noteDao(): NoteDao
    abstract fun personDao(): PersonDao
    abstract fun organizationDao(): OrganizationDao
    abstract fun embeddingDao(): EmbeddingDao

    companion object {
        const val DATABASE_NAME = "mindbox.db"

        fun create(context: Context, passphraseProvider: DatabasePassphraseProvider): MindBoxDatabase {
            val passphrase = passphraseProvider.getPassphrase()
            val factory = SupportOpenHelperFactory(passphrase)
            return Room.databaseBuilder(context, MindBoxDatabase::class.java, DATABASE_NAME)
                .openHelperFactory(factory)
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
