package cn.appsdream.nestrefresh.util;

import android.util.Log;

/**
 * Created by zewei on 2015-12-10.
 */
public class L {

    private static final String TAG = "AbsListLoader";
    private static final String LOG_FORMAT = "%1%2";
    private static boolean writeLogs;


    public static void d(String message, Object... args) {
        log(Log.DEBUG, null, message, args);
    }

    public static void i(String message, Object... args) {
        log(Log.INFO, null, message, args);
    }

    public static void w(String message, Object... args) {
        log(Log.WARN, null, message, args);
    }

    public static void e(Throwable ex) {
        log(Log.ERROR, ex, null);
    }

    public static void e(String message, Object... args) {
        log(Log.ERROR, null, message, args);
    }

    public static void e(Throwable ex, String message, Object... args) {
        log(Log.ERROR, ex, message, args);
    }

    private static void log(int priority, Throwable ex, String message, Object... args) {
        if (!writeLogs) return;
        if (args.length > 0) {
            message = String.format(message, args);
        }

        String log;
        if (ex == null) {
            log = message;
        } else {
            String logMessage = message == null ? ex.getMessage() : message;
            String logBody = Log.getStackTraceString(ex);
            log = String.format(LOG_FORMAT, logMessage, logBody);
        }
        Log.println(priority, TAG, log);
    }

    public static void setWriteLogs(boolean writeLogs) {
        L.writeLogs = writeLogs;
    }
}
