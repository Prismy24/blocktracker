package com.example.blocktracker;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Periodically scans the cube around the player for tracked blocks and caches the hits,
 * so the renderer only has to draw a ready-made list every frame.
 */
public final class BlockScanner {
	public record Hit(BlockPos pos, int rgb) {}

	private static final int SCAN_INTERVAL_TICKS = 10;
	private static final int MAX_HITS = 2000;

	private static volatile List<Hit> hits = List.of();
	private static int tickCounter = 0;
	private static boolean dirty = true;

	private BlockScanner() {}

	/** Call after the tracked block list or radius changes so the next tick rescans. */
	public static void markDirty() {
		dirty = true;
	}

	public static List<Hit> getHits() {
		return hits;
	}

	public static void tick(MinecraftClient client) {
		TrackerConfig cfg = TrackerConfig.get();
		if (client.world == null || client.player == null || !cfg.enabled || cfg.blocks.isEmpty()) {
			hits = List.of();
			return;
		}
		if (!dirty && ++tickCounter < SCAN_INTERVAL_TICKS) {
			return;
		}
		tickCounter = 0;
		dirty = false;
		hits = scan(client.world, client.player.getBlockPos(), cfg);
	}

	private static List<Hit> scan(ClientWorld world, BlockPos center, TrackerConfig cfg) {
		Map<Block, Integer> wanted = new HashMap<>();
		for (Map.Entry<String, String> e : cfg.blocks.entrySet()) {
			net.minecraft.util.Identifier id = net.minecraft.util.Identifier.tryParse(e.getKey());
			if (id != null && Registries.BLOCK.containsId(id)) {
				wanted.put(Registries.BLOCK.get(id), TrackerConfig.parseColor(e.getValue()));
			}
		}
		if (wanted.isEmpty()) {
			return List.of();
		}

		int r = cfg.radius;
		int minY = Math.max(world.getBottomY(), center.getY() - r);
		int maxY = Math.min(world.getBottomY() + world.getHeight() - 1, center.getY() + r);

		List<Hit> found = new ArrayList<>();
		BlockPos.Mutable pos = new BlockPos.Mutable();
		for (int x = center.getX() - r; x <= center.getX() + r; x++) {
			for (int z = center.getZ() - r; z <= center.getZ() + r; z++) {
				if (!world.getChunkManager().isChunkLoaded(x >> 4, z >> 4)) {
					continue;
				}
				for (int y = minY; y <= maxY; y++) {
					pos.set(x, y, z);
					BlockState state = world.getBlockState(pos);
					Integer rgb = wanted.get(state.getBlock());
					if (rgb != null) {
						found.add(new Hit(pos.toImmutable(), rgb));
						if (found.size() >= MAX_HITS) {
							return found;
						}
					}
				}
			}
		}
		return found;
	}
}
