package `in`.programy.audiorecorder.util

import java.util.concurrent.TimeUnit

object Methods {
    fun longToTime(millis: Long): String{
        return String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
            TimeUnit.MILLISECONDS.toMinutes(millis) % 60,
            TimeUnit.MILLISECONDS.toSeconds(millis) % 60);
    }
}