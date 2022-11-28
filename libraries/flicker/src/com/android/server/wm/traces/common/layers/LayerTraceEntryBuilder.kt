/*
 * Copyright (C) 2021 The Android Open Source Project
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

package com.android.server.wm.traces.common.layers

import kotlin.js.JsName

/** Builder for LayerTraceEntries */
class LayerTraceEntryBuilder(
    _elapsedTimestamp: String,
    layers: Array<Layer>,
    @JsName("displays") private val displays: Array<Display>,
    @JsName("vSyncId") private val vSyncId: Long,
    @JsName("hwcBlob") private val hwcBlob: String = "",
    @JsName("where") private val where: String = "",
    realToElapsedTimeOffsetNs: String? = null,
) {
    // Necessary for compatibility with JS number type
    @JsName("elapsedTimestamp") private val elapsedTimestamp: Long = _elapsedTimestamp.toLong()
    @JsName("realTimestamp")
    private val realTimestamp: Long? =
        if (realToElapsedTimeOffsetNs != null && realToElapsedTimeOffsetNs.toLong() != 0L) {
            realToElapsedTimeOffsetNs.toLong() + _elapsedTimestamp.toLong()
        } else {
            null
        }

    @JsName("_orphanLayerCallback") private var orphanLayerCallback: ((Layer) -> Boolean)? = null
    @JsName("orphans") private val orphans = mutableListOf<Layer>()
    @JsName("layers") private val layers = setLayers(layers)
    @JsName("_ignoreVirtualDisplay") private var ignoreVirtualDisplay = false
    @JsName("_ignoreLayersStackMatchNoDisplay") private var ignoreLayersStackMatchNoDisplay = false

    @JsName("setLayers")
    private fun setLayers(layers: Array<Layer>): Map<Int, Layer> {
        val result = mutableMapOf<Int, Layer>()
        layers.forEach { layer ->
            val id = layer.id
            if (result.containsKey(id)) {
                throw RuntimeException("Duplicate layer id found: $id")
            }
            result[id] = layer
        }

        return result
    }

    @JsName("setOrphanLayerCallback")
    fun setOrphanLayerCallback(value: ((Layer) -> Boolean)?): LayerTraceEntryBuilder = apply {
        this.orphanLayerCallback = value
    }

    @JsName("notifyOrphansLayers")
    private fun notifyOrphansLayers() {
        val callback = this.orphanLayerCallback ?: return

        // Fail if we find orphan layers.
        orphans.forEach { orphan ->
            // Workaround for b/141326137, ignore the existence of an orphan layer
            if (callback.invoke(orphan)) {
                return@forEach
            }
            throw RuntimeException(
                ("Failed to parse layers trace. Found orphan layer with id = ${orphan.id}" +
                    " with parentId = ${orphan.parentId}")
            )
        }
    }

    /**
     * Update the parent layers or each trace
     *
     * @return root layer
     */
    @JsName("updateParents")
    private fun updateParents() {
        for (layer in layers.values) {
            val parentId = layer.parentId

            val parentLayer = layers[parentId]
            if (parentLayer == null) {
                orphans.add(layer)
                continue
            }
            parentLayer.addChild(layer)
            layer.parent = parentLayer
        }
    }

    /**
     * Update the parent layers or each trace
     *
     * @return root layer
     */
    @JsName("updateRelZParents")
    private fun updateRelZParents() {
        for (layer in layers.values) {
            val parentId = layer.zOrderRelativeOfId

            val parentLayer = layers[parentId]
            if (parentLayer == null) {
                layer.zOrderRelativeParentOf = parentId
                continue
            }
            layer.zOrderRelativeOf = parentLayer
        }
    }

    @JsName("computeRootLayers")
    private fun computeRootLayers(): List<Layer> {
        updateParents()
        updateRelZParents()

        // Find all root layers (any sibling of the root layer is considered a root layer in the
        // trace)
        val rootLayers = mutableListOf<Layer>()

        // Getting the first orphan works because when dumping the layers, the root layer comes
        // first, and given that orphans are added in the same order as the layers are provided
        // in the first orphan layer should be the root layer.
        if (orphans.isNotEmpty()) {
            val firstRoot = orphans.first()
            orphans.remove(firstRoot)
            rootLayers.add(firstRoot)

            val remainingRoots = orphans.filter { it.parentId == firstRoot.parentId }
            rootLayers.addAll(remainingRoots)

            // Remove RootLayers from orphans
            orphans.removeAll(rootLayers)
        }

        return rootLayers
    }

    @JsName("filterOutLayersInVirtualDisplays")
    private fun filterOutLayersInVirtualDisplays(roots: List<Layer>): List<Layer> {
        val physicalDisplays = displays.filterNot { it.isVirtual }.map { it.layerStackId }

        return roots.filter { physicalDisplays.contains(it.stackId) }
    }

    @JsName("filterOutVirtualDisplays")
    private fun filterOutVirtualDisplays(displays: List<Display>): List<Display> {
        return displays.filterNot { it.isVirtual }
    }

    @JsName("filterOutOffDisplays")
    private fun filterOutOffDisplays(displays: List<Display>): List<Display> {
        return displays.filterNot { it.isOff }
    }

    @JsName("filterOutLayersStackMatchNoDisplay")
    private fun filterOutLayersStackMatchNoDisplay(roots: List<Layer>): List<Layer> {
        val displayStacks = displays.map { it.layerStackId }
        return roots.filter { displayStacks.contains(it.stackId) }
    }

    /**
     * Defines if virtual displays and the layers belonging to virtual displays (e.g., Screen
     * Recording) should be ignored while parsing the entry
     *
     * @param ignore If the layers from virtual displays should be ignored or not
     */
    @JsName("ignoreVirtualDisplay")
    fun ignoreVirtualDisplay(ignore: Boolean): LayerTraceEntryBuilder = apply {
        this.ignoreVirtualDisplay = ignore
    }

    /**
     * Ignore layers whose stack ID doesn't match any display. This is the case, for example, when
     * the device screen is off, or for layers that have not yet been removed after a display change
     * (e.g., virtual screen recording display removed)
     *
     * @param ignore If the layers not matching any stack id should be removed or not
     */
    @JsName("ignoreLayersStackMatchNoDisplay")
    fun ignoreLayersStackMatchNoDisplay(ignore: Boolean): LayerTraceEntryBuilder = apply {
        this.ignoreLayersStackMatchNoDisplay = ignore
    }

    /** Constructs the layer hierarchy from a flattened list of layers. */
    @JsName("build")
    fun build(): LayerTraceEntry {
        val allRoots = computeRootLayers()
        var filteredRoots = allRoots
        var filteredDisplays = displays.toList()

        if (ignoreLayersStackMatchNoDisplay) {
            filteredRoots = filterOutLayersStackMatchNoDisplay(filteredRoots)
        }

        if (ignoreVirtualDisplay) {
            filteredRoots = filterOutLayersInVirtualDisplays(filteredRoots)
            filteredDisplays = filterOutVirtualDisplays(filteredDisplays)
        }

        filteredDisplays = filterOutOffDisplays(filteredDisplays)

        // Fail if we find orphan layers.
        notifyOrphansLayers()

        return LayerTraceEntry(
            elapsedTimestamp,
            realTimestamp,
            hwcBlob,
            where,
            filteredDisplays.toTypedArray(),
            vSyncId,
            filteredRoots.toTypedArray(),
        )
    }
}
