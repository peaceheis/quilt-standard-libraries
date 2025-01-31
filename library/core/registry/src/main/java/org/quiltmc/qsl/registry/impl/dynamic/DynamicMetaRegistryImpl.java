/*
 * Copyright 2023 The Quilt Project
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

package org.quiltmc.qsl.registry.impl.dynamic;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryLoader;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.registry.mixin.DynamicRegistrySyncAccessor;

@ApiStatus.Internal
public class DynamicMetaRegistryImpl {
	private static boolean frozen;
	private static final Set<Identifier> MODDED_REGISTRY_IDS = new HashSet<>();

	public static boolean isModdedRegistryId(Identifier id) {
		return MODDED_REGISTRY_IDS.contains(id);
	}

	public static <E> void register(RegistryKey<? extends Registry<E>> ref, Codec<E> entryCodec) {
		if (frozen) throw new IllegalStateException("Registry is already frozen");
		MODDED_REGISTRY_IDS.add(ref.getValue());
		RegistryLoader.WORLDGEN_REGISTRIES.add(new RegistryLoader.DecodingData<>(ref, entryCodec));
	}

	public static <E> void registerSynced(RegistryKey<? extends Registry<E>> ref, Codec<E> entryCodec, Codec<E> syncCodec) {
		register(ref, entryCodec);
		var builder = ImmutableMap.<RegistryKey<? extends Registry<?>>, Object>builder().putAll(DynamicRegistrySyncAccessor.quilt$getSyncedCodecs());
		DynamicRegistrySyncAccessor.quilt$invokeAddSyncedRegistry(builder, ref, syncCodec);
		DynamicRegistrySyncAccessor.quilt$setSyncedCodecs(builder.build());
	}

	public static void freeze() {
		frozen = true;
	}
}
