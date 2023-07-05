/*
 * Copyright 2022 The Quilt Project
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

package org.quiltmc.qsl.datafixerupper.impl.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.client.MinecraftClient;

import org.quiltmc.qsl.datafixerupper.impl.QuiltDataFixesInternals;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientLifecycleEvents;

@Environment(EnvType.CLIENT)
@ApiStatus.Internal
public final class ClientFreezer implements ClientLifecycleEvents.Ready {
	@Override
	public void readyClient(MinecraftClient client) {
		QuiltDataFixesInternals.get().freeze();
	}
}
