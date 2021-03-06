/*
 * Copyright © 2019 Eugene Glushankov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netchar.common.di

import android.content.Context
import com.netchar.common.services.*
import com.netchar.common.utils.CoroutineDispatchers
import com.netchar.common.utils.navigation.IToolbarNavigationBinder
import com.netchar.common.utils.navigation.ToolbarNavigationBinder
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object CommonModule {

    @JvmStatic
    @Provides
    @Singleton
    fun provideDispatchers(): CoroutineDispatchers = CoroutineDispatchers()

    @JvmStatic
    @Provides
    @Singleton
    fun providesWallpaperApplierService(context: Context): IWallpaperApplierService = WallpaperApplierService(context)

    @JvmStatic
    @Provides
    @Singleton
    fun providesToolbarNavigationBinder(): IToolbarNavigationBinder = ToolbarNavigationBinder()

    @JvmStatic
    @Provides
    @Singleton
    fun providesPhotoCacheService(context: Context, coroutineDispatchers: CoroutineDispatchers): IPhotoCacheService = PhotoCacheService(context, coroutineDispatchers)

    @JvmStatic
    @Provides
    fun providesCommunicationService(context: Context): IExternalAppService = ExternalAppService(context)

    @JvmStatic
    @Provides
    fun providesExternalLibrariesService(): IExternalLibraryProvider = ExternalLibraryProvider()
}
