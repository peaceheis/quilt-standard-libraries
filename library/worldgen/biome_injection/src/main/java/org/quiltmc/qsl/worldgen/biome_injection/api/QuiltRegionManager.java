package org.quiltmc.qsl.worldgen.biome_injection.api;

import net.minecraft.util.Holder;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSupplier;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.quiltmc.qsl.worldgen.biome_injection.impl.QuiltRegionMangerImpl;

public class QuiltRegionManager {
	public static <T extends BiomeSupplier> void registerOverworldRegion(T biomeSupplier, float weight) {
		QuiltRegionMangerImpl.registerOverworldRegion(biomeSupplier, weight);
	}

	public static <T extends BiomeSupplier> void registerNetherRegion(T biomeSupplier, float weight) {
		QuiltRegionMangerImpl.registerNetherRegion(biomeSupplier, weight);
	}

}
