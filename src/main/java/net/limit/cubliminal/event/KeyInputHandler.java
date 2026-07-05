package net.limit.cubliminal.event;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.limit.cubliminal.IniterClient;
import net.limit.cubliminal.access.GameRendererAccessor;
import net.limit.cubliminal.access.PEAccessor;
import net.limit.cubliminal.client.screen.roomcreator.RoomCreatorScreen;
import net.limit.cubliminal.networking.c2s.NoClipC2SPayload;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class KeyInputHandler implements IniterClient {
	private static final String KEY_CATEGORY_CUBLIMINAL = "key.category.cubliminal";
	private static final String KEY_OPEN_ROOM_CREATOR_MENU = "key.cubliminal.open_room_creator_menu";
	public static KeyBinding ROOM_CREATION_MENU_KEY;

	public static int ticksColliding = 0;

	@Override
	public void init() {
		ROOM_CREATION_MENU_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				KEY_OPEN_ROOM_CREATOR_MENU, GLFW.GLFW_KEY_Z, KEY_CATEGORY_CUBLIMINAL
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			ClientPlayerEntity player = client.player;
			if (player == null) return;

			if (player.isCreativeLevelTwoOp() && ROOM_CREATION_MENU_KEY.wasPressed()) {
				client.setScreen(new RoomCreatorScreen(client.currentScreen));
			}

			if (((PEAccessor) player).getNoclipEngine().canClip() && client.options.forwardKey.isPressed() && player.horizontalCollision && !player.isSneaking() && player.isOnGround()) {
				if (ticksColliding > 40) {
					ClientPlayNetworking.send(new NoClipC2SPayload(false));
				} else {
					++ticksColliding;
					((GameRendererAccessor) client.gameRenderer).setClippingIntoWall(true);
					return;
				}
			}
			ticksColliding = 0;
			((GameRendererAccessor) client.gameRenderer).setClippingIntoWall(false);
		});
	}
}
