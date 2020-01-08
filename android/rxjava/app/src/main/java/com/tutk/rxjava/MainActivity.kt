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
        Observable.create<String> { e ->
            Log.e(TAG, "emit onNext")
            e.onNext("hello")
            e.onError(Exception("error1"))
        }.observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .onErrorReturn { "error" }
            .subscribe { e -> Log.e(TAG, "subscribe1 $e") }

        Observable.create<String> { e ->
            Log.e(TAG, "emit onNext")
            e.onNext("hello")
            e.onError(Exception("error1"))
        }.onErrorReturn { "error" }
            .subscribe { e -> Log.e(TAG, "subscribe2 $e") }

        Observable.just("hello")
            .map { e -> "$e world" }
            .subscribe { e -> Log.e(TAG, "subscribe3 $e") }

        Observable.just(1, 2, 3, 4, 5)
            .flatMap { e -> getData(e).delay(100, TimeUnit.MILLISECONDS) }
            .subscribe { e -> Log.e(TAG, "subscribe4 $e") }

        // concatMap process next item after onComplete emitted
        Observable.just(1, 2, 3, 4, 5)
            .filter { e -> e in 1..3 }
            .concatMap { e -> getData(e).delay(300, TimeUnit.MILLISECONDS) }
            .subscribe { e -> Log.e(TAG, "subscribe5 $e") }

        Observable.zip(
            Observable.just("cat", "cat1", "cat2", "cat3"),
            Observable.just("dog", "dog1", "dog2"),
            BiFunction<String, String, String> { e1, e2 -> "$e1 $e2" })
            .subscribe { e -> Log.e(TAG, "subscribe6 $e") }

        val ob1 = getIndexFromServer()
        val ob2 = ob1.flatMap { e -> getNameFromServer(e) }

        // ob1 do twice
        Observable.merge(ob1, ob2)
            .subscribe { e -> Log.e(TAG, "subscribe7 $e") }

        ob1.publish { e ->
            val e2 = e.flatMap { index -> getNameFromServer(index) }
            Observable.merge(e, e2)
        }.subscribe { e -> Log.e(TAG, "subscribe8 $e") }

    }

    fun getData(i: Int) = Observable.create<String> { e ->
        Log.e(TAG, "getData $i")
        e.onNext("data $i")
        e.onComplete()
    }

    fun getIndexFromServer() = Observable.create<Int> { e ->
        Log.e(TAG, "getIndexFromServer")
        e.onNext(7)
        e.onComplete()
    }

    fun getNameFromServer(i: Int) = Observable.create<String> { e ->
        Log.e(TAG, "getNameFromServer $i")
        e.onNext("name $i")
        e.onComplete()
    }

}
