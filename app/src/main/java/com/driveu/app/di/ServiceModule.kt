package com.driveu.app.di

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.driveu.app.R
import com.driveu.app.ui.MainActivity
import com.driveu.app.util.Constants
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(ServiceComponent::class)
class ServiceModule {


    @ServiceScoped
    @Provides
    fun provideFusedLocationProviderClient(
        @ApplicationContext app: Context,
    ) = LocationServices.getFusedLocationProviderClient(app)


    @ServiceScoped
    @Provides
    fun providePendingIntent(@ApplicationContext app: Context): PendingIntent = PendingIntent.getActivity(
        app,
        0,
        Intent(app, MainActivity::class.java).also {
            it.action = Constants.ACTION_SHOW_TRACKING_FRAGMENT
        },
        PendingIntent.FLAG_IMMUTABLE
    )

    @ServiceScoped
    @Provides
    fun provideBaseNotificationBuilder(@ApplicationContext app:Context,pendingIntent: PendingIntent) = NotificationCompat.Builder(app, Constants.NOTIFICATION_CHANNEL_ID)
        .setAutoCancel(false)
        .setOngoing(true)
        .setSmallIcon(R.drawable.ic_run)
        .setContentTitle("RunTrack")
        .setContentText("00:00:00")
        .setContentIntent(pendingIntent)
}