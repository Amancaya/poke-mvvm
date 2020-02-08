package negron.kaya.pokemvvm.di

import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import negron.kaya.pokemvvm.utils.ViewModelProviderFactory

@Module
abstract class ViewModelFactoryModule {

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelProviderFactory): ViewModelProvider.Factory
}