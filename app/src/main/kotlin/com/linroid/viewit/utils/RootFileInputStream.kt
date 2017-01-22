//package com.linroid.viewit.utils
//
//import android.system.Os
//import com.linroid.viewit.App
//import com.stericson.RootShell.exceptions.RootDeniedException
//import com.stericson.RootShell.execution.Command
//import com.stericson.RootTools.RootTools
//import timber.log.Timber
//import java.io.*
//import java.util.concurrent.TimeoutException
//
///**
// * @author linroid <linroid></linroid>@gmail.com>
// * *
// * @since 09/01/2017
// */
//class RootFileInputStream @Throws(FileNotFoundException::class, IOException::class, TimeoutException::class, RootDeniedException::class)
//constructor(file: File) : InputStream() {
//    internal var mException: Throwable? = null
//    private var mFilePosition: Long = 0
//    private val mLength: Long
//    private var mPipe: File? = null
//    private var mPipeStream: RandomAccessFile? = null
//    private var mWriteThread: Thread? = null
//
//    init {
//        this.mLength = 1024 // todo
//        if (this.mLength > 0) {
//            if (!file.canRead()) {
//                RootUtils.requireRoot()
//            }
//            this.mPipe = File(App.instance.cacheDir, System.nanoTime().toString())
//            val mkfifoCommand = object : Command(0, "/data/data/com.linroid.viewit/busybox mkfifo -m 666 " + this.mPipe!!.absolutePath) {
//                override fun commandOutput(id: Int, line: String?) {
//                    super.commandOutput(id, line)
//                    Timber.d(line)
//                }
//
//                override fun commandTerminated(id: Int, reason: String?) {
//                    super.commandTerminated(id, reason)
//                }
//
//                override fun commandCompleted(id: Int, exitcode: Int) {
//                    super.commandCompleted(id, exitcode)
//                }
//            }
//            RootTools.getShell(true).add(mkfifoCommand)
//            this.mWriteThread = object : Thread() {
//                override fun run() {
//                    try {
//                        Timber.w("Start writing to pipe")
//                        dd(file.absolutePath, mPipe!!.absolutePath)
//                        Timber.w("Writing to pipe finished")
//                    } catch (e: Throwable) {
//                        Timber.w(e)
//                        mException = e
//                    } finally {
//                        mPipeStream!!.close()
//                    }
//                }
//            }
//            this.mWriteThread!!.start()
//            this.mPipeStream = RandomAccessFile(this.mPipe, "r")
//        }
//    }
//
//    @Throws(TimeoutException::class, RootDeniedException::class, IOException::class)
//    private fun dd(path1: String, path2: String) {
//        RootTools.remount(path2, "rw")
//        val ddCommand = object : Command(0, "dd if=\"$path1\" of=\"$path2\"") {
//            override fun commandOutput(id: Int, line: String?) {
//                super.commandOutput(id, line)
//                Timber.d(line)
//            }
//
//            override fun commandTerminated(id: Int, reason: String?) {
//                super.commandTerminated(id, reason)
//            }
//
//            override fun commandCompleted(id: Int, exitcode: Int) {
//                super.commandCompleted(id, exitcode)
//            }
//        }
//        RootTools.getShell(true).add(ddCommand)
//        RootTools.remount(path2, "r")
//    }
//
//    @Throws(IOException::class)
//    override fun read(): Int {
//        if (this.mPipeStream == null) {
//            return -1
//        }
//        val read = this.mPipeStream!!.read()
//        this.mFilePosition++
//        return read
//    }
//
//    @Throws(IOException::class)
//    override fun available(): Int {
//        return if (this.mPipe == null) 0 else Math.max((this.mLength - this.mFilePosition).toInt(), 0)
//    }
//
//    @Throws(IOException::class)
//    override fun read(bArr: ByteArray, i: Int, i2: Int): Int {
//        if (this.mPipeStream == null || available() <= 0) {
//            return -1
//        }
//        val read = this.mPipeStream!!.read(bArr, i, i2)
//        this.mFilePosition += read.toLong()
//        return read
//    }
//
//    @Throws(IOException::class)
//    override fun read(bArr: ByteArray): Int {
//        return read(bArr, 0, bArr.size)
//    }
//
//    @Throws(IOException::class)
//    override fun close() {
//        Timber.i("Closing stream...")
//        super.close()
//        if (this.mPipeStream != null) {
//            this.mPipeStream!!.close()
//            this.mPipe!!.delete()
//            try {
//                this.mWriteThread!!.join()
//            } catch (e: Throwable) {
//                Timber.i(e)
//            }
//
//        }
//        if (this.mException != null) {
//            throw IOException(this.mException)
//        }
//    }
//}
