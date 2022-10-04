/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.server.wm.traces.common.windowmanager.windows

import com.android.server.wm.traces.common.withCache
import kotlin.js.JsName

/**
 * Represents the requested policy of a WM container
 *
 * This is a generic object that is reused by both Flicker and Winscope and cannot access internal
 * Java/Android functionality
 */
class WindowManagerPolicy
private constructor(
    @JsName("focusedAppToken") val focusedAppToken: String = "",
    @JsName("forceStatusBar") val forceStatusBar: Boolean = false,
    @JsName("forceStatusBarFromKeyguard") val forceStatusBarFromKeyguard: Boolean = false,
    @JsName("keyguardDrawComplete") val keyguardDrawComplete: Boolean = false,
    @JsName("keyguardOccluded") val keyguardOccluded: Boolean = false,
    @JsName("keyguardOccludedChanged") val keyguardOccludedChanged: Boolean = false,
    @JsName("keyguardOccludedPending") val keyguardOccludedPending: Boolean = false,
    @JsName("lastSystemUiFlags") val lastSystemUiFlags: Int = 0,
    @JsName("orientation") val orientation: Int = 0,
    @JsName("rotation") val rotation: Int = 0,
    @JsName("rotationMode") val rotationMode: Int = 0,
    @JsName("screenOnFully") val screenOnFully: Boolean = false,
    @JsName("windowManagerDrawComplete") val windowManagerDrawComplete: Boolean = false
) {
    @JsName("isOrientationNoSensor")
    val isOrientationNoSensor: Boolean
        get() = orientation == SCREEN_ORIENTATION_NOSENSOR

    @JsName("isFixedOrientation")
    val isFixedOrientation: Boolean
        get() =
            isFixedOrientationLandscape ||
                isFixedOrientationPortrait ||
                orientation == SCREEN_ORIENTATION_LOCKED

    @JsName("isFixedOrientationLandscape")
    private val isFixedOrientationLandscape
        get() =
            orientation == SCREEN_ORIENTATION_LANDSCAPE ||
                orientation == SCREEN_ORIENTATION_SENSOR_LANDSCAPE ||
                orientation == SCREEN_ORIENTATION_REVERSE_LANDSCAPE ||
                orientation == SCREEN_ORIENTATION_USER_LANDSCAPE

    @JsName("isFixedOrientationPortrait")
    private val isFixedOrientationPortrait
        get() =
            orientation == SCREEN_ORIENTATION_PORTRAIT ||
                orientation == SCREEN_ORIENTATION_SENSOR_PORTRAIT ||
                orientation == SCREEN_ORIENTATION_REVERSE_PORTRAIT ||
                orientation == SCREEN_ORIENTATION_USER_PORTRAIT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is WindowManagerPolicy) return false

        if (focusedAppToken != other.focusedAppToken) return false
        if (forceStatusBar != other.forceStatusBar) return false
        if (forceStatusBarFromKeyguard != other.forceStatusBarFromKeyguard) return false
        if (keyguardDrawComplete != other.keyguardDrawComplete) return false
        if (keyguardOccluded != other.keyguardOccluded) return false
        if (keyguardOccludedChanged != other.keyguardOccludedChanged) return false
        if (keyguardOccludedPending != other.keyguardOccludedPending) return false
        if (lastSystemUiFlags != other.lastSystemUiFlags) return false
        if (orientation != other.orientation) return false
        if (rotation != other.rotation) return false
        if (rotationMode != other.rotationMode) return false
        if (screenOnFully != other.screenOnFully) return false
        if (windowManagerDrawComplete != other.windowManagerDrawComplete) return false

        return true
    }

    override fun hashCode(): Int {
        var result = focusedAppToken.hashCode()
        result = 31 * result + forceStatusBar.hashCode()
        result = 31 * result + forceStatusBarFromKeyguard.hashCode()
        result = 31 * result + keyguardDrawComplete.hashCode()
        result = 31 * result + keyguardOccluded.hashCode()
        result = 31 * result + keyguardOccludedChanged.hashCode()
        result = 31 * result + keyguardOccludedPending.hashCode()
        result = 31 * result + lastSystemUiFlags
        result = 31 * result + orientation
        result = 31 * result + rotation
        result = 31 * result + rotationMode
        result = 31 * result + screenOnFully.hashCode()
        result = 31 * result + windowManagerDrawComplete.hashCode()
        return result
    }

    override fun toString(): String {
        return "${this::class.simpleName} (focusedAppToken='$focusedAppToken', " +
            "forceStatusBar=$forceStatusBar, " +
            "forceStatusBarFromKeyguard=$forceStatusBarFromKeyguard, " +
            "keyguardDrawComplete=$keyguardDrawComplete, keyguardOccluded=$keyguardOccluded, " +
            "keyguardOccludedChanged=$keyguardOccludedChanged, " +
            "keyguardOccludedPending=$keyguardOccludedPending, " +
            "lastSystemUiFlags=$lastSystemUiFlags, orientation=$orientation, " +
            "rotation=$rotation, rotationMode=$rotationMode, " +
            "screenOnFully=$screenOnFully, " +
            "windowManagerDrawComplete=$windowManagerDrawComplete)"
    }

    companion object {
        @JsName("EMPTY")
        val EMPTY: WindowManagerPolicy
            get() = withCache { WindowManagerPolicy() }

        /** From [android.content.pm.ActivityInfo] */
        private const val SCREEN_ORIENTATION_LANDSCAPE = 0
        private const val SCREEN_ORIENTATION_PORTRAIT = 1
        private const val SCREEN_ORIENTATION_NOSENSOR = 5
        private const val SCREEN_ORIENTATION_SENSOR_LANDSCAPE = 6
        private const val SCREEN_ORIENTATION_SENSOR_PORTRAIT = 7
        private const val SCREEN_ORIENTATION_REVERSE_LANDSCAPE = 8
        private const val SCREEN_ORIENTATION_REVERSE_PORTRAIT = 9
        private const val SCREEN_ORIENTATION_USER_LANDSCAPE = 11
        private const val SCREEN_ORIENTATION_USER_PORTRAIT = 12
        private const val SCREEN_ORIENTATION_LOCKED = 14

        @JsName("from")
        fun from(
            focusedAppToken: String,
            forceStatusBar: Boolean,
            forceStatusBarFromKeyguard: Boolean,
            keyguardDrawComplete: Boolean,
            keyguardOccluded: Boolean,
            keyguardOccludedChanged: Boolean,
            keyguardOccludedPending: Boolean,
            lastSystemUiFlags: Int,
            orientation: Int,
            rotation: Int,
            rotationMode: Int,
            screenOnFully: Boolean,
            windowManagerDrawComplete: Boolean
        ): WindowManagerPolicy = withCache {
            WindowManagerPolicy(
                focusedAppToken,
                forceStatusBar,
                forceStatusBarFromKeyguard,
                keyguardDrawComplete,
                keyguardOccluded,
                keyguardOccludedChanged,
                keyguardOccludedPending,
                lastSystemUiFlags,
                orientation,
                rotation,
                rotationMode,
                screenOnFully,
                windowManagerDrawComplete
            )
        }
    }
}
