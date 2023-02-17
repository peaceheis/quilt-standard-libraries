package org.quiltmc.qsl.worldgen.biome_injection.impl;

import net.minecraft.util.Holder;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSupplier;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.qsl.resource.loader.api.ResourceLoaderEvents;
import org.quiltmc.qsl.worldgen.biome_injection.api.QuiltRegionManager;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;


@ApiStatus.Internal
public class QuiltRegionMangerImpl {
	private static final QuiltRegionMangerImpl OVERWORLD_REGION_MANAGER = new QuiltRegionMangerImpl();
	private static final QuiltRegionMangerImpl NETHER_REGION_MANAGER = new QuiltRegionMangerImpl();

	private final QuiltVoronoiNoise REGION_NOISE;
	private final Map<BiomeSupplier, Float> MAIN_WEIGHTS_MAP;
	private final Map<Float, BiomeSupplier> BIOME_SOURCE_INTERVALS;
	private float weightSum;
	private static final Identifier BIOME_INJECTION_APPLY_PHASE = new Identifier("quilt_biome_injection", "finalize");

	public QuiltRegionMangerImpl() {
		this.REGION_NOISE = new QuiltVoronoiNoise();
		this.MAIN_WEIGHTS_MAP = new HashMap<>();
		this.BIOME_SOURCE_INTERVALS = new HashMap<>();
		this.weightSum = 0;
		ResourceLoaderEvents.END_DATA_PACK_RELOAD.register(BIOME_INJECTION_APPLY_PHASE, (server, resourceManager, error) -> {
			if (error == null && server == null) {
				this.finalizeBiomeInjectionMap();
			}
		});
	}

	public <T extends BiomeSupplier> void registerBiomeSourceRegion(T biomeSupplier, float weight) {
		if (weight <= 0) {
			throw new InvalidParameterException("Weights registered with BiomeSources must be greater than 0.");
		}

		// Make an entry to a master Map of BiomeSources and weights
		this.MAIN_WEIGHTS_MAP.put(biomeSupplier, weight);
		this.weightSum += weight;
	}

	/**
	 * Finalizes <@code> </@code>
	 */
	private void finalizeBiomeInjectionMap() {
		float upperIntervalBound = -1.0F;
		// label every region with the proportion
		for (Map.Entry<BiomeSupplier, Float> val : MAIN_WEIGHTS_MAP.entrySet()) {
			upperIntervalBound += val.getValue();
			this.BIOME_SOURCE_INTERVALS.put(upperIntervalBound/weightSum * 2, val.getKey());
		}
	}

	public static <T extends BiomeSupplier> void registerOverworldRegion(T biomeSupplier, float weight) {
		OVERWORLD_REGION_MANAGER.registerBiomeSourceRegion(biomeSupplier, weight);
	}

	public static <T extends BiomeSupplier> void registerNetherRegion(T biomeSupplier, float weight) {
		NETHER_REGION_MANAGER.registerBiomeSourceRegion(biomeSupplier, weight);
	}

	public static Holder<Biome> getOverworldBiome(int i, int j, int k, MultiNoiseUtil.MultiNoiseSampler multiNoiseSampler) {
		return OVERWORLD_REGION_MANAGER.getBiome(i, j, k, multiNoiseSampler);
	}

	public static Holder<Biome> getNetherBiome(int i, int j, int k, MultiNoiseUtil.MultiNoiseSampler multiNoiseSampler) {
		return NETHER_REGION_MANAGER.getBiome(i, j, k, multiNoiseSampler);
	}

	public Holder<Biome> getBiome(int i, int j, int k, MultiNoiseUtil.MultiNoiseSampler multiNoiseSampler) {
		return this.getBiomeSupplier(i, j, k, multiNoiseSampler).getNoiseBiome(i, j, k, multiNoiseSampler);
	}

	private BiomeSupplier getBiomeSupplier(int i, int j, int k, MultiNoiseUtil.MultiNoiseSampler multiNoiseSampler) {
		float noiseVal = REGION_NOISE.getNoiseValue(i, j, k);

		for (Map.Entry<Float, BiomeSupplier> entry : this.BIOME_SOURCE_INTERVALS.entrySet()) {
			if (entry.getKey() <= noiseVal) {
				return (BiomeSupplier) entry.getValue();
			}
		}
		return this.BIOME_SOURCE_INTERVALS.get(2.0F); // it's going to do that in the for loop, but just to placate Java.
	}
}
