package com.driveu.app.util

import android.graphics.Color

object Constants {
    const val RUNNING_DATABASE_NAME="running_db"
    const val ACTION_START_OR_RESUME_SERVICE="start_or_resume"
    const val ACTION_PAUSE_SERVICE="pause"
    const val ACTION_STOP_SERVICE="stop"

    const val BUNDLE_KEY = "map_view_bundle_key"
    const val ACTION_SHOW_TRACKING_FRAGMENT="tracking_fragment"

    const val TIMER_UPDATE_INTERVAL = 50L

    const val LOCATION_UPDATE_INTERVAL = 5000L
    const val FASTEST_LOCATION_INTERVAL = 2000L

    const val POLYLINE_COLOR = Color.RED
    const val POLYLINE_WIDTH = 10f
    const val MAP_ZOOM = 15f

    const val NOTIFICATION_CHANNEL_ID = "tracking_channel"
    const val NOTIFICATION_CHANNEL_NAME = "Tracking"
    const val NOTIFICATION_ID = 1
}