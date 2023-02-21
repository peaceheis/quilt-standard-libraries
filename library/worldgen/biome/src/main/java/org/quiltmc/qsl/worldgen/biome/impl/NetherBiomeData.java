/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
 * Copyright 2022 QuiltMC
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

package org.quiltmc.qsl.worldgen.biome.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;

import net.minecraft.registry.Holder;
import net.minecraft.registry.HolderProvider;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;

import org.quiltmc.qsl.worldgen.biome.impl.modification.BuiltInRegistryKeys;

/**
 * Internal data for modding Vanilla's {@link MultiNoiseBiomeSource.Preset#NETHER}.
 */
@ApiStatus.Internal
public final class NetherBiomeData {
	// Cached sets of the biomes that would generate from Vanilla's default biome source without consideration
	// for data packs (as those would be distinct biome sources).
	private static final Set<RegistryKey<Biome>> NETHER_BIOMES = new HashSet<>();

	private static final Map<RegistryKey<Biome>, MultiNoiseUtil.NoiseHypercube> NETHER_BIOME_NOISE_POINTS = new HashMap<>();

	private static final Logger LOGGER = LogUtils.getLogger();

	private NetherBiomeData() {
	}

	public static void addNetherBiome(RegistryKey<Biome> biome, MultiNoiseUtil.NoiseHypercube spawnNoisePoint) {
		Preconditions.checkArgument(biome != null, "Biome is null");
		Preconditions.checkArgument(spawnNoisePoint != null, "MultiNoiseUtil.NoiseValuePoint is null");
		NETHER_BIOME_NOISE_POINTS.put(biome, spawnNoisePoint);
		clearBiomeSourceCache();
	}

	public static Map<RegistryKey<Biome>, MultiNoiseUtil.NoiseHypercube> getNetherBiomeNoisePoints() {
		return NETHER_BIOME_NOISE_POINTS;
	}

	public static boolean canGenerateInNether(RegistryKey<Biome> biome) {
		if (NETHER_BIOMES.isEmpty()) {
			MultiNoiseBiomeSource source = MultiNoiseBiomeSource.Preset.NETHER.m_zfjbeiiv(BuiltInRegistryKeys.biomeRegistryWrapper());

			for (Holder<Biome> entry : source.getBiomes()) {
				entry.getKey().ifPresent(NETHER_BIOMES::add);
			}
		}

		return NETHER_BIOMES.contains(biome) || NETHER_BIOME_NOISE_POINTS.containsKey(biome);
	}

	private static void clearBiomeSourceCache() {
		NETHER_BIOMES.clear(); // Clear cached biome source data
	}

	private static MultiNoiseUtil.ParameterRangeList<Holder<Biome>> withModdedBiomePoints(MultiNoiseUtil.ParameterRangeList<Holder<Biome>> defaultEntries,
			HolderProvider<Biome> biomeRegistry) {
		if (NETHER_BIOME_NOISE_POINTS.isEmpty()) {
			return defaultEntries;
		}

		var entries = new ArrayList<>(defaultEntries.getEntries());

		for (Map.Entry<RegistryKey<Biome>, MultiNoiseUtil.NoiseHypercube> entry : NETHER_BIOME_NOISE_POINTS.entrySet()) {
			Holder.Reference<Biome> biomeEntry = biomeRegistry.getHolder(entry.getKey()).orElse(null);

			if (biomeEntry != null) {
				entries.add(Pair.of(entry.getValue(), biomeEntry));
			} else {
				LOGGER.warn("Nether biome {} not loaded", entry.getKey().getValue());
			}
		}

		return new MultiNoiseUtil.ParameterRangeList<>(entries);
	}

	public static void modifyBiomeSource(HolderProvider<Biome> biomeRegistry, BiomeSource biomeSource) {
		if (biomeSource instanceof MultiNoiseBiomeSource multiNoiseBiomeSource) {
			if (((BiomeSourceAccess) multiNoiseBiomeSource).quilt$shouldModifyBiomePoints() && multiNoiseBiomeSource.matchesInstance(MultiNoiseBiomeSource.Preset.NETHER)) {
				multiNoiseBiomeSource.biomePoints = NetherBiomeData.withModdedBiomePoints(
						MultiNoiseBiomeSource.Preset.NETHER.biomeSourceFunction.apply(biomeRegistry),
						biomeRegistry);
				multiNoiseBiomeSource.biomes = multiNoiseBiomeSource.biomePoints.getEntries().stream().map(Pair::getSecond).collect(Collectors.toSet());
				((BiomeSourceAccess) multiNoiseBiomeSource).quilt$setModifyBiomePoints(false);
			}
		}
	}
}
