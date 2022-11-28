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

package com.android.server.wm.flicker.traces.eventlog

import com.android.server.wm.traces.common.ITraceEntry
import com.android.server.wm.traces.common.Timestamp

class FocusEvent(
    override val timestamp: Timestamp,
    val window: String,
    val focus: Focus,
    val reason: String
) : ITraceEntry {
    enum class Focus {
        GAINED,
        LOST,
        REQUESTED
    }

    override fun toString(): String {
        return "$timestamp: Focus ${focus.name} $window Reason=$reason"
    }

    fun hasFocus(): Boolean {
        return this.focus == Focus.GAINED
    }
}
