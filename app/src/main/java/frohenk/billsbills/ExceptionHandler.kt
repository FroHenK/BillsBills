package frohenk.billsbills

import android.util.Log

abstract class ExceptionHandler {
    companion object {
        val errorLogger: (Throwable) -> Unit = { Log.e("kek", "error", it) }
    }
}