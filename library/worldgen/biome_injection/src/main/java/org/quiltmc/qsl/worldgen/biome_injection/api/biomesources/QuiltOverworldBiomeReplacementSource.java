package org.quiltmc.qsl.worldgen.biome_injection.api.biomesources;

import net.minecraft.util.Holder;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSupplier;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;

public class QuiltOverworldBiomeReplacementSource implements BiomeSupplier {
	private Holder<Biome> toReplace;
	private Holder<Biome> replaceWith;

	public QuiltOverworldBiomeReplacementSource(Holder<Biome> toReplace, Holder<Biome> replaceWith) {

	}

	@Override
	public Holder<Biome> getNoiseBiome(int i, int j, int k, MultiNoiseUtil.MultiNoiseSampler multiNoiseSampler) {
		return null;
	}
}
