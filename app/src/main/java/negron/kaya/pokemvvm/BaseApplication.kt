package negron.kaya.pokemvvm

import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import negron.kaya.pokemvvm.di.DaggerAppComponent

class BaseApplication: DaggerApplication() {
    override fun applicationInjector(): AndroidInjector<out DaggerApplication>? {
        return DaggerAppComponent.builder().application(this).build()
    }
}