package com.lanqi.qiwei

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/** @HiltAndroidApp：App 一启动，Hilt 就按 di/ 里的登记表把「人力派遣中心」建起来。 */
@HiltAndroidApp
class QiweiApplication : Application()
