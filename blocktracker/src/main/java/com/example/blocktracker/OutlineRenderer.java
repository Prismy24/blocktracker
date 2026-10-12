package com.example.blocktracker;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Draws a wireframe box around every cached hit using vanilla's line render type.
 * All vertex coordinates handed to the world buffer must be relative to the camera.
 */
public final class OutlineRenderer {
	private static final double EXPAND = 0.003; // avoids z-fighting with the block faces

	private OutlineRenderer() {}

	public static void render(WorldRenderContext ctx) {
		TrackerConfig cfg = TrackerConfig.get();
		if (!cfg.enabled) {
			return;
		}
		List<BlockScanner.Hit> hits = BlockScanner.getHits();
		if (hits.isEmpty()) {
			return;
		}

		MultiBufferSource consumers = ctx.consumers();
		PoseStack matrices = ctx.matrices();
		if (consumers == null || matrices == null) {
			return;
		}

		Vec3 cam = ctx.gameRenderer().getMainCamera().getPosition();
		VertexConsumer lines = consumers.getBuffer(RenderType.lines());

		float alpha = Math.max(0.05f, Math.min(1f, cfg.lineAlpha));
		for (BlockScanner.Hit hit : hits) {
			float r = ((hit.rgb() >> 16) & 0xFF) / 255f;
			float g = ((hit.rgb() >> 8) & 0xFF) / 255f;
			float b = (hit.rgb() & 0xFF) / 255f;
			double x = hit.pos().getX() - cam.x;
			double y = hit.pos().getY() - cam.y;
			double z = hit.pos().getZ() - cam.z;
			ShapeRenderer.renderLineBox(
				matrices.last(), lines,
				x - EXPAND, y - EXPAND, z - EXPAND,
				x + 1 + EXPAND, y + 1 + EXPAND, z + 1 + EXPAND,
				r, g, b, alpha
			);
		}
	}
}
