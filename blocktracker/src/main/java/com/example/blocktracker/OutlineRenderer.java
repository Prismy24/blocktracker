package com.example.blocktracker;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;

/** Draws a wireframe box around every cached hit. */
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

		MatrixStack matrices = ctx.matrixStack();
		VertexConsumerProvider consumers = ctx.consumers();
		if (matrices == null || consumers == null) {
			return;
		}

		RenderLayer layer = cfg.throughWalls
			? TrackerRenderLayers.LINES_THROUGH_WALLS
			: TrackerRenderLayers.LINES_DEPTH_TESTED;
		VertexConsumer lines = consumers.getBuffer(layer);

		Vec3d cam = ctx.camera().getPos();
		matrices.push();
		matrices.translate(-cam.x, -cam.y, -cam.z);

		float alpha = Math.max(0.05f, Math.min(1f, cfg.lineAlpha));
		for (BlockScanner.Hit hit : hits) {
			float r = ((hit.rgb() >> 16) & 0xFF) / 255f;
			float g = ((hit.rgb() >> 8) & 0xFF) / 255f;
			float b = (hit.rgb() & 0xFF) / 255f;
			Box box = new Box(hit.pos()).expand(EXPAND);
			VertexRendering.drawBox(matrices, lines, box, r, g, b, alpha);
		}

		matrices.pop();

		// Flush our layer now so it is drawn with its own depth state.
		if (consumers instanceof VertexConsumerProvider.Immediate immediate) {
			immediate.draw(layer);
		}
	}
}
