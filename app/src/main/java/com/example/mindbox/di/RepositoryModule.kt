package com.example.mindbox.di

import com.example.mindbox.data.repository.*
import com.example.mindbox.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindEntryRepository(impl: EntryRepository): IEntryRepository

    @Binds @Singleton
    abstract fun bindEventRepository(impl: EventRepository): IEventRepository

    @Binds @Singleton
    abstract fun bindNoteRepository(impl: NoteRepository): INoteRepository

    @Binds @Singleton
    abstract fun bindPeopleRepository(impl: PeopleRepository): IPeopleRepository

    @Binds @Singleton
    abstract fun bindOrgRepository(impl: OrgRepository): IOrgRepository

    @Binds @Singleton
    abstract fun bindEmbeddingRepository(impl: EmbeddingRepository): IEmbeddingRepository
}
