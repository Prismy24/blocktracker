package com.example.blocktracker;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;

import java.util.OptionalDouble;

/**
 * Custom render layers. Extending RenderLayer gives access to the protected phase constants
 * and the layer factory method.
 */
public final class TrackerRenderLayers extends RenderLayer {
	private TrackerRenderLayers(String name, VertexFormat format, VertexFormat.DrawMode drawMode,
								int bufferSize, boolean hasCrumbling, boolean translucent,
								Runnable begin, Runnable end) {
		super(name, format, drawMode, bufferSize, hasCrumbling, translucent, begin, end);
	}

	/** Lines that ignore the depth buffer, so outlines show through walls. */
	public static final RenderLayer LINES_THROUGH_WALLS = of(
		"blocktracker_lines_through_walls",
		VertexFormats.LINES,
		VertexFormat.DrawMode.LINES,
		1536,
		false,
		false,
		RenderLayer.MultiPhaseParameters.builder()
			.program(LINES_PROGRAM)
			.lineWidth(new RenderPhase.LineWidth(OptionalDouble.of(2.5)))
			.layering(VIEW_OFFSET_Z_LAYERING)
			.transparency(TRANSLUCENT_TRANSPARENCY)
			.writeMaskState(COLOR_MASK)
			.cull(DISABLE_CULLING)
			.depthTest(ALWAYS_DEPTH_TEST)
			.build(false)
	);

	/** Normal depth-tested lines, so outlines are hidden behind terrain. */
	public static final RenderLayer LINES_DEPTH_TESTED = of(
		"blocktracker_lines_depth_tested",
		VertexFormats.LINES,
		VertexFormat.DrawMode.LINES,
		1536,
		false,
		false,
		RenderLayer.MultiPhaseParameters.builder()
			.program(LINES_PROGRAM)
			.lineWidth(new RenderPhase.LineWidth(OptionalDouble.of(2.5)))
			.layering(VIEW_OFFSET_Z_LAYERING)
			.transparency(TRANSLUCENT_TRANSPARENCY)
			.writeMaskState(COLOR_MASK)
			.cull(DISABLE_CULLING)
			.depthTest(LEQUAL_DEPTH_TEST)
			.build(false)
	);
}
