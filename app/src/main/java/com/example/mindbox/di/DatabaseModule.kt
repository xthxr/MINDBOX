package com.example.mindbox.di

import android.content.Context
import com.example.mindbox.data.local.MindBoxDatabase
import com.example.mindbox.data.local.dao.*
import com.example.mindbox.security.DatabasePassphraseProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideMindBoxDatabase(
        @ApplicationContext context: Context,
        passphraseProvider: DatabasePassphraseProvider
    ): MindBoxDatabase {
        return MindBoxDatabase.create(context, passphraseProvider)
    }

    @Provides fun provideRawEntryDao(db: MindBoxDatabase): RawEntryDao = db.rawEntryDao()
    @Provides fun provideEventDao(db: MindBoxDatabase): EventDao = db.eventDao()
    @Provides fun provideNoteDao(db: MindBoxDatabase): NoteDao = db.noteDao()
    @Provides fun providePersonDao(db: MindBoxDatabase): PersonDao = db.personDao()
    @Provides fun provideOrganizationDao(db: MindBoxDatabase): OrganizationDao = db.organizationDao()
    @Provides fun provideEmbeddingDao(db: MindBoxDatabase): EmbeddingDao = db.embeddingDao()
}
