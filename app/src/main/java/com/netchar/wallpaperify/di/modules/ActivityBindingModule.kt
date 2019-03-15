package com.netchar.wallpaperify.di.modules

import com.netchar.wallpaperify.home.MainActivity
import com.netchar.wallpaperify.home.MainActivityModule
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.android.support.AndroidSupportInjectionModule

@Module(
    includes = [
        AndroidSupportInjectionModule::class,
        ViewModelModule::class]
)
abstract class ActivityBindingModule {

    @ContributesAndroidInjector(modules = [MainActivityModule::class])
    abstract fun contributeMainActivity(): MainActivity

}
