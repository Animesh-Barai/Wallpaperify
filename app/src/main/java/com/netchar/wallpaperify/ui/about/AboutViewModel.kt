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

package com.netchar.wallpaperify.ui.about

import com.netchar.common.DEVELOPER_INSTAGRAM_URL
import com.netchar.common.DEVELOPER_LINKEDIN_URL
import com.netchar.common.base.BaseViewModel
import com.netchar.common.services.IExternalAppService
import com.netchar.common.services.IExternalAppService.ExternalApp
import com.netchar.common.utils.CoroutineDispatchers
import com.netchar.common.utils.IBuildPreferences
import javax.inject.Inject

class AboutViewModel @Inject constructor(
        coroutineDispatchers: CoroutineDispatchers,
        private val buildPreferences: IBuildPreferences,
        private val appService: IExternalAppService
) : BaseViewModel(coroutineDispatchers) {

    fun getVersionName() = buildPreferences.getVersionName()

    fun sendEmail() {
        appService.sendEmail("Hi, Eugene", "")
    }

    fun redirectToInstagramAcc() {
        appService.openWith(ExternalApp.INSTAGRAM, DEVELOPER_INSTAGRAM_URL)
    }

    fun redirectToLinkedInAcc() {
        appService.openWith(ExternalApp.LINKED_IN, DEVELOPER_LINKEDIN_URL)
    }
}