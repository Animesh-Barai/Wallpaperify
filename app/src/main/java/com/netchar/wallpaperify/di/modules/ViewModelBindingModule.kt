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

package com.netchar.wallpaperify.di.modules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.netchar.common.di.ViewModelKey
import com.netchar.wallpaperify.di.ViewModelFactory
import com.netchar.wallpaperify.ui.about.AboutViewModel
import com.netchar.wallpaperify.ui.collectiondetails.CollectionDetailsViewModel
import com.netchar.wallpaperify.ui.collections.CollectionsViewModel
import com.netchar.wallpaperify.ui.feedback.FeedbackViewModel
import com.netchar.wallpaperify.ui.photos.PhotosViewModel
import com.netchar.wallpaperify.ui.photosdetails.PhotoDetailsViewModel
import com.netchar.wallpaperify.ui.settings.SettingsViewModel
import com.netchar.wallpaperify.ui.support.SupportDevelopmentViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelBindingModule {

    @Binds
    @IntoMap
    @ViewModelKey(PhotosViewModel::class)
    abstract fun bindLatestViewModel(viewModel: PhotosViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PhotoDetailsViewModel::class)
    abstract fun bindPhotoDetailsViewModel(viewModel: PhotoDetailsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CollectionsViewModel::class)
    abstract fun bindCollectionsViewModel(viewModel: CollectionsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SettingsViewModel::class)
    abstract fun bindSettingsViewModel(viewModel: SettingsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CollectionDetailsViewModel::class)
    abstract fun bindCollectionDetailsViewModel(viewModel: CollectionDetailsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AboutViewModel::class)
    abstract fun bindAboutViewModel(viewModel: AboutViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FeedbackViewModel::class)
    abstract fun bindFeedbackViewModel(viewModel: FeedbackViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SupportDevelopmentViewModel::class)
    abstract fun bindSupportDevelopmentViewModel(viewModel: SupportDevelopmentViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}