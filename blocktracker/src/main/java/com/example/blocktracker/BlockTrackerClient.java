package com.example.blocktracker;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockTrackerClient implements ClientModInitializer {
	public static final String MOD_ID = "blocktracker";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static KeyBinding toggleKey;

	@Override
	public void onInitializeClient() {
		TrackerConfig.get(); // load (or create) the config early

		toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.blocktracker.toggle",
			InputUtil.Type.KEYSYM,
			GLFW.GLFW_KEY_K,
			"category.blocktracker"
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (toggleKey.wasPressed()) {
				TrackerCommands.toggle();
				if (client.player != null) {
					client.player.sendMessage(Text.literal(
						"Block Tracker " + (TrackerConfig.get().enabled ? "enabled" : "disabled")), true);
				}
			}
			BlockScanner.tick(client);
		});

		WorldRenderEvents.AFTER_TRANSLUCENT.register(OutlineRenderer::render);

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
			dispatcher.register(TrackerCommands.build()));

		LOGGER.info("Block Tracker loaded");
	}
}
