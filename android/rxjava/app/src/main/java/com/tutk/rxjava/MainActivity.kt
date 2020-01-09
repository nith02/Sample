package com.tutk.rxjava

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import java.lang.Exception
import java.util.concurrent.TimeUnit


const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // onNext not emit if in io schedule
        Observable.create<String> {
            Log.e(TAG, "emit onNext")
            it.onNext("hello")
            it.onError(Exception("error1"))
        }.observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .onErrorReturn { "error" }
            .subscribe { Log.e(TAG, "subscribe1 $it") }

        Observable.create<String> {
            Log.e(TAG, "emit onNext")
            it.onNext("hello")
            it.onError(Exception("error1"))
        }.onErrorReturn { "error" }
            .subscribe { Log.e(TAG, "subscribe2 $it") }

        Observable.just("hello")
            .map { "$it world" }
            .subscribe { Log.e(TAG, "subscribe3 $it") }

        Observable.just(1, 2, 3, 4, 5)
            .flatMap { getData(it).delay(100, TimeUnit.MILLISECONDS) }
            .subscribe { Log.e(TAG, "subscribe4 $it") }

        // concatMap process next item after onComplete emitted
        Observable.just(1, 2, 3, 4, 5)
            .filter { it in 1..3 }
            .concatMap { getData(it).delay(300, TimeUnit.MILLISECONDS) }
            .subscribe { Log.e(TAG, "subscribe5 $it") }

        Observable.zip(
            Observable.just("cat", "cat1", "cat2", "cat3"),
            Observable.just("dog", "dog1", "dog2"),
            BiFunction<String, String, String> { e1, e2 -> "$e1 $e2" })
            .subscribe { Log.e(TAG, "subscribe6 $it") }

        val ob1 = getIndexFromServer()
        val ob2 = ob1.flatMap { getNameFromServer(it) }

        // ob1 do twice
        Observable.merge(ob1, ob2)
            .subscribe { Log.e(TAG, "subscribe7 $it") }

        ob1.publish {
            val ob2 = it.flatMap { index -> getNameFromServer(index) }
            Observable.merge(it, ob2)
        }.subscribe { Log.e(TAG, "subscribe8 $it") }

    }

    fun getData(i: Int) = Observable.create<String> {
        it.onNext("data $i")
        it.onComplete()
    }

    fun getIndexFromServer() = Observable.create<Int> {
        Log.e(TAG, "getIndexFromServer")
        it.onNext(7)
        it.onComplete()
    }

    fun getNameFromServer(i: Int) = Observable.create<String> {
        Log.e(TAG, "getNameFromServer $i")
        it.onNext("name $i")
        it.onComplete()
    }

}
