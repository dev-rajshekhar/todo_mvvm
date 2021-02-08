package com.example.toddomvvm.di

import android.app.Application
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.toddomvvm.data.TodoDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDataBase(app:Application,callback:TodoDatabase.Callback)=
        Room.databaseBuilder(app,TodoDatabase::class.java,"task_database").fallbackToDestructiveMigration().addCallback(callback).build()
@Provides
fun provideTaskDao(db:TodoDatabase)=db.todoDao()

    @ApplicationScope
    @Provides
    @Singleton
    fun provideApplicationScope()= CoroutineScope(SupervisorJob())
}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope