package negron.kaya.pokemvvm.utils

import android.util.Log


fun String.errorLogs(tag: String) {
    Log.e(tag,this)
//    Crashlytics.log(Log.ERROR, tag, this)
}