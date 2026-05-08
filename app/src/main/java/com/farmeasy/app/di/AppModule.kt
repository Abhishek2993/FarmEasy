package com.farmeasy.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.farmeasy.app.data.local.AlertDao
import com.farmeasy.app.data.local.AppDatabase
import com.farmeasy.app.data.local.IrrigationDao
import com.farmeasy.app.data.local.SensorReadingDao
import com.farmeasy.app.data.remote.ApiService
import com.farmeasy.app.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "farmeasy_settings")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // ---- Room Database ----

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideSensorReadingDao(database: AppDatabase): SensorReadingDao {
        return database.sensorReadingDao()
    }

    @Provides
    fun provideAlertDao(database: AppDatabase): AlertDao {
        return database.alertDao()
    }

    @Provides
    fun provideIrrigationDao(database: AppDatabase): IrrigationDao {
        return database.irrigationDao()
    }

    // ---- Network ----

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(Constants.API_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(Constants.API_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(Constants.API_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    // ---- DataStore ----

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }
}
