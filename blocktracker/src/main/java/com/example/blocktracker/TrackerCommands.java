package com.example.blocktracker;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Map;

/**
 * /track add <block> [#RRGGBB]
 * /track remove <block>
 * /track list
 * /track radius <1-64>
 * /track walls <true|false>
 * /track alpha <0.05-1>
 * /track toggle
 */
public final class TrackerCommands {
	private TrackerCommands() {}

	public static LiteralArgumentBuilder<FabricClientCommandSource> build() {
		return ClientCommandManager.literal("track")
			.then(ClientCommandManager.literal("add")
				.then(ClientCommandManager.argument("block", StringArgumentType.word())
					.suggests((c, b) -> {
						String typed = b.getRemaining().toLowerCase();
						Registries.BLOCK.getIds().stream()
							.map(Identifier::toString)
							.filter(s -> s.contains(typed) || s.replace("minecraft:", "").startsWith(typed))
							.limit(100)
							.forEach(b::suggest);
						return b.buildFuture();
					})
					.executes(c -> add(c, null))
					.then(ClientCommandManager.argument("color", StringArgumentType.word())
						.executes(c -> add(c, StringArgumentType.getString(c, "color"))))))
			.then(ClientCommandManager.literal("remove")
				.then(ClientCommandManager.argument("block", StringArgumentType.word())
					.suggests((c, b) -> {
						TrackerConfig.get().blocks.keySet().forEach(b::suggest);
						return b.buildFuture();
					})
					.executes(TrackerCommands::remove)))
			.then(ClientCommandManager.literal("list").executes(TrackerCommands::list))
			.then(ClientCommandManager.literal("radius")
				.then(ClientCommandManager.argument("value", IntegerArgumentType.integer(1, TrackerConfig.MAX_RADIUS))
					.executes(c -> {
						TrackerConfig cfg = TrackerConfig.get();
						cfg.radius = IntegerArgumentType.getInteger(c, "value");
						cfg.save();
						BlockScanner.markDirty();
						msg(c, "Scan radius set to " + cfg.radius);
						return 1;
					})))
			.then(ClientCommandManager.literal("walls")
				.then(ClientCommandManager.argument("value", com.mojang.brigadier.arguments.BoolArgumentType.bool())
					.executes(c -> {
						TrackerConfig cfg = TrackerConfig.get();
						cfg.throughWalls = com.mojang.brigadier.arguments.BoolArgumentType.getBool(c, "value");
						cfg.save();
						msg(c, "Through walls: " + cfg.throughWalls);
						return 1;
					})))
			.then(ClientCommandManager.literal("alpha")
				.then(ClientCommandManager.argument("value", FloatArgumentType.floatArg(0.05f, 1f))
					.executes(c -> {
						TrackerConfig cfg = TrackerConfig.get();
						cfg.lineAlpha = FloatArgumentType.getFloat(c, "value");
						cfg.save();
						msg(c, "Outline opacity set to " + cfg.lineAlpha);
						return 1;
					})))
			.then(ClientCommandManager.literal("toggle").executes(c -> {
				toggle();
				msg(c, "Block Tracker " + (TrackerConfig.get().enabled ? "enabled" : "disabled"));
				return 1;
			}));
	}

	public static void toggle() {
		TrackerConfig cfg = TrackerConfig.get();
		cfg.enabled = !cfg.enabled;
		cfg.save();
		BlockScanner.markDirty();
	}

	private static int add(CommandContext<FabricClientCommandSource> c, String color) {
		String raw = StringArgumentType.getString(c, "block");
		Identifier id = Identifier.tryParse(raw.contains(":") ? raw : "minecraft:" + raw);
		if (id == null || !Registries.BLOCK.containsId(id)) {
			msg(c, "Unknown block: " + raw);
			return 0;
		}
		String key = id.toString();
		String hex = color != null ? (color.startsWith("#") ? color : "#" + color) : TrackerConfig.autoColor(key);
		TrackerConfig cfg = TrackerConfig.get();
		cfg.blocks.put(key, hex);
		cfg.save();
		BlockScanner.markDirty();
		msg(c, "Tracking " + key + " (" + hex + ")");
		return 1;
	}

	private static int remove(CommandContext<FabricClientCommandSource> c) {
		String raw = StringArgumentType.getString(c, "block");
		String key = raw.contains(":") ? raw : "minecraft:" + raw;
		TrackerConfig cfg = TrackerConfig.get();
		if (cfg.blocks.remove(key) == null) {
			msg(c, "Not tracking " + key);
			return 0;
		}
		cfg.save();
		BlockScanner.markDirty();
		msg(c, "Stopped tracking " + key);
		return 1;
	}

	private static int list(CommandContext<FabricClientCommandSource> c) {
		TrackerConfig cfg = TrackerConfig.get();
		if (cfg.blocks.isEmpty()) {
			msg(c, "No blocks tracked. Use /track add <block>");
			return 1;
		}
		StringBuilder sb = new StringBuilder("Tracking (radius " + cfg.radius + "):");
		for (Map.Entry<String, String> e : cfg.blocks.entrySet()) {
			sb.append("\n  ").append(e.getKey()).append("  ").append(e.getValue());
		}
		msg(c, sb.toString());
		return 1;
	}

	private static void msg(CommandContext<FabricClientCommandSource> c, String text) {
		c.getSource().sendFeedback(Text.literal(text));
	}
}
