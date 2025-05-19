package com.g2.bcu.androidutil.io

import android.os.Build
import android.os.Environment
import android.util.Log
import com.g2.bcu.androidutil.StaticStore
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter
import java.io.Writer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ErrorLogWriter : Thread.UncaughtExceptionHandler {
    private val errors: Thread.UncaughtExceptionHandler? = Thread.getDefaultUncaughtExceptionHandler()
    private val path: String

    constructor() {
        path = LOG_PATH
    }
    constructor(p : String) {
        path = p
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        writeToFile(e)
        errors?.uncaughtException(t, e)
    }

    private fun writeToFile(e: Throwable) {
        if (written)
            return
        try {
            val f = File(path)
            if (!f.exists())
                f.mkdirs()
            val dateFormat = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US)
            val name = "UERR_" + dateFormat.format(Date()) + "_" + Build.MODEL + ".txt"
            val stringbuff: Writer = StringWriter()
            val printWriter = PrintWriter(stringbuff)
            e.printStackTrace(printWriter)
            val current = stringbuff.toString()
            printWriter.close()

            var file = File(path, name)
            if (!file.exists())
                file.createNewFile()
            else {
                file = File(path, getExistingFileName(path, name))
                file.createNewFile()
            }
            val fileWriter = FileWriter(file)
            fileWriter.append("VERSION : ").append(StaticStore.VER).append("\r\n")
            fileWriter.append("MODEL : ").append(Build.MANUFACTURER).append(" ").append(Build.MODEL.toString()).append("\r\n")
            fileWriter.append("IS EMULATOR : ").append((Build.MODEL.contains("Emulator") || Build.MODEL.contains("Android SDK")).toString()).append("\r\n")
            fileWriter.append("ANDROID_VER : ").append("API ").append(Build.VERSION.SDK_INT.toString()).append(" (").append(Build.VERSION.RELEASE).append(")").append("\r\n").append("\r\n")
            fileWriter.append(current)
            fileWriter.flush()
            fileWriter.close()
            written = true
        } catch (e1: IOException) {
            e1.printStackTrace()
        }
    }

    companion object {

        private var written = false
        private val LOG_PATH = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)}/bcu/logs/"

        fun writeDriveLog(e: Exception) {
            try {
                val path = File(LOG_PATH)
                if(!path.exists() && !path.mkdirs()) {
                    Log.e("ErrorLogWriter", "Failed to create folder "+path.absolutePath)
                    return
                }

                val stringbuff: Writer = StringWriter()
                val printWriter = PrintWriter(stringbuff)

                e.printStackTrace(printWriter)

                val dateFormat = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US)
                val date = Date()
                val name = dateFormat.format(date) + "_" + Build.MODEL + ".txt"

                val df = File(LOG_PATH, name)
                if (!df.exists()) df.createNewFile()
                val dfileWriter = FileWriter(df)
                dfileWriter.append("VERSION : ").append(StaticStore.VER).append("\r\n")
                dfileWriter.append("MODEL : ").append(Build.MANUFACTURER).append(" ").append(Build.MODEL.toString()).append("\r\n")
                dfileWriter.append("IS EMULATOR : ").append((Build.MODEL.contains("Emulator") || Build.MODEL.contains("Android SDK")).toString()).append("\r\n")
                dfileWriter.append("ANDROID_VER : ").append("API ").append(Build.VERSION.SDK_INT.toString()).append(" (").append(Build.VERSION.RELEASE).append(")").append("\r\n").append("\r\n")
                dfileWriter.append(stringbuff.toString())
                dfileWriter.flush()
                dfileWriter.close()
                printWriter.close()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }

        fun writeLog(error: Exception) {
            error.printStackTrace()

            try {
                val f = File(LOG_PATH)
                if (!f.exists())
                    f.mkdirs()

                val dateFormat = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US)
                val date = Date()
                val name = dateFormat.format(date) + "_" + Build.MODEL + ".txt"
                var file = File(LOG_PATH, name)
                val stringbuff: Writer = StringWriter()
                val printWriter = PrintWriter(stringbuff)
                error.printStackTrace(printWriter)
                if (!file.exists()) file.createNewFile() else {
                    file = File(LOG_PATH, getExistingFileName(LOG_PATH, name))
                    file.createNewFile()
                }

                val fileWriter = FileWriter(file)
                fileWriter.append("VERSION : ").append(StaticStore.VER).append("\r\n")
                fileWriter.append("MODEL : ").append(Build.MANUFACTURER).append(" ").append(Build.MODEL.toString()).append("\r\n")
                fileWriter.append("IS EMULATOR : ").append((Build.MODEL.contains("Emulator") || Build.MODEL.contains("Android SDK")).toString()).append("\r\n")
                fileWriter.append("ANDROID_VER : ").append("API ").append(Build.VERSION.SDK_INT.toString()).append(" (").append(Build.VERSION.RELEASE).append(")").append("\r\n").append("\r\n")
                fileWriter.append(stringbuff.toString())
                fileWriter.flush()
                fileWriter.close()
                printWriter.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        fun writeLog(error: Exception, msg: String, upload: Boolean) {
            try {
                val path = StaticStore.getPublicDirectory() + "logs"
                val f = File(path)
                if (!f.exists()) {
                    f.mkdirs()
                }
                val dateFormat = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US)
                val date = Date()
                val name = dateFormat.format(date) + ".txt"
                var file = File(path, name)
                val stringbuff: Writer = StringWriter()
                val printWriter = PrintWriter(stringbuff)
                error.printStackTrace(printWriter)
                if (!file.exists())
                    file.createNewFile()
                else {
                    file = File(path, getExistingFileName(path, name))
                    file.createNewFile()
                }
                if (upload) {
                    val dname = dateFormat.format(date) + "_" + Build.MODEL + ".txt"
                    val df = File(StaticStore.getPublicDirectory() + "logs/", dname)//Environment.getDataDirectory().toString() + "/data/com.g2.bcu/upload/"
                    if (!df.exists()) df.createNewFile()
                    val dfileWriter = FileWriter(df)
                    dfileWriter.append("VERSION : ").append(StaticStore.VER).append("\r\n")
                    dfileWriter.append("MODEL : ").append(Build.MANUFACTURER).append(" ").append(Build.MODEL.toString()).append("\r\n")
                    dfileWriter.append("IS EMULATOR : ").append((Build.MODEL.contains("Emulator") || Build.MODEL.contains("Android SDK")).toString()).append("\r\n")
                    dfileWriter.append("ANDROID_VER : ").append("API ").append(Build.VERSION.SDK_INT.toString()).append(" (").append(Build.VERSION.RELEASE).append(")").append("\r\n").append("\r\n")
                    dfileWriter.append("Message : ").append(msg).append("\r\n\r\n")
                    dfileWriter.append(stringbuff.toString())
                    dfileWriter.flush()
                    dfileWriter.close()
                }
                val fileWriter = FileWriter(file)
                fileWriter.append("Message : ").append(msg).append("\r\n\r\n")
                fileWriter.append(stringbuff.toString())
                fileWriter.flush()
                fileWriter.close()
                printWriter.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        private fun getExistingFileName(path: String, name: String): String {
            var decided = false
            var exist = 1
            var nam = "$name-$exist"
            while (!decided) {
                val f = File(path, nam)
                nam = if (!f.exists()) return nam else {
                    exist++
                    "$name-$exist"
                }
                decided = true
            }
            return nam
        }
    }
}