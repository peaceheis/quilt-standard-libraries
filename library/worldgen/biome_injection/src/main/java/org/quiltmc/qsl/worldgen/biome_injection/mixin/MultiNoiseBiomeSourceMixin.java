/*
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

package org.quiltmc.qsl.worldgen.biome_injection.mixin;

import net.minecraft.util.Holder;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource.Instance;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import org.quiltmc.qsl.worldgen.biome_injection.api.QuiltRegionManager;
import org.quiltmc.qsl.worldgen.biome_injection.impl.QuiltRegionMangerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * Provides an injection point to allow the Biome Injection API to do its thing.
 *
 * @see QuiltRegionManager
 */
@Mixin(MultiNoiseBiomeSource.class)
public abstract class MultiNoiseBiomeSourceMixin {

	private boolean isOverworldBiomeSource;
	private boolean isNetherBiomeSource;
	private static boolean injected;

	@Shadow
	public abstract boolean matchesInstance(MultiNoiseBiomeSource.Preset preset);

	@Inject(method = "<init>(Lnet/minecraft/world/biome/source/util/MultiNoiseUtil$ParameterRangeList;Ljava/util/Optional;)V",
			at = @At("TAIL"))
	private void quilt$markOverworldOrNetherBiomeSource(MultiNoiseUtil.ParameterRangeList<Holder<Biome>> biomePoints, Optional<Instance> instance, CallbackInfo ci) {
		if (this.matchesInstance(MultiNoiseBiomeSource.Preset.OVERWORLD)) {
			this.isOverworldBiomeSource = true;
		}
		else if (this.matchesInstance(MultiNoiseBiomeSource.Preset.NETHER)) {
			this.isNetherBiomeSource = true;
		}
	}



	@Inject(method = "getNoiseBiome(IIILnet/minecraft/world/biome/source/util/MultiNoiseUtil$MultiNoiseSampler;)Lnet/minecraft/util/Holder;",
			at = @At("HEAD"), cancellable = true)
	private void quilt$injectRegionManagerAccess(int i, int j, int k, MultiNoiseUtil.MultiNoiseSampler multiNoiseSampler, CallbackInfoReturnable<Holder<Biome>> cir) {
		if(!MultiNoiseBiomeSourceMixin.injected) {
			MultiNoiseBiomeSourceMixin.injected = true; // prevents recursive injection
			if (this.isOverworldBiomeSource) {
				cir.setReturnValue(QuiltRegionMangerImpl.getOverworldBiome(i, j, k, multiNoiseSampler));
			}
			else if (this.isNetherBiomeSource) {
				cir.setReturnValue(QuiltRegionMangerImpl.getOverworldBiome(i, j, k, multiNoiseSampler));
			}
		}
		cir.cancel();
	}
}
