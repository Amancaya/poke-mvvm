package negron.kaya.pokemvvm.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import negron.kaya.pokemvvm.di.main.*
import negron.kaya.pokemvvm.view.MainActivity

@Module
abstract class ActivityBuildersModule {

    @MainScope
    @ContributesAndroidInjector(
        modules = [MainModule::class,
            MainViewModelModule::class,
            MainServiceProvider::class,
            MainRepositoryProvider::class]
    )
    abstract fun contributeMainActivity(): MainActivity
}