package net.huebcraft.sciencia.compat.sodium;

import java.util.function.Function;

import net.caffeinemc.mods.sodium.api.texture.SpriteUtil;
import net.huebcraft.sciencia.Sciencia;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage;

public class SodiumCompat {
	/*
	public static final ResourceLocation SAW_TEXTURE = Sciencia.asResource("block/saw_reversed");
	public static final ResourceLocation FACTORY_PANEL_TEXTURE = Sciencia.asResource("block/factory_panel_connections_animated");
	*/

	public static void init(IEventBus modEventBus, IEventBus neoEventBus) {
		Minecraft mc = Minecraft.getInstance();
		neoEventBus.addListener((RenderLevelStageEvent event) -> {
			if (event.getStage() == Stage.AFTER_ENTITIES) {
				Function<ResourceLocation, TextureAtlasSprite> atlas = mc.getTextureAtlas(InventoryMenu.BLOCK_ATLAS);
				//TextureAtlasSprite sawSprite = atlas.apply(SAW_TEXTURE);
				//SpriteUtil.INSTANCE.markSpriteActive(sawSprite);

				//TextureAtlasSprite factoryPanelSprite = atlas.apply(FACTORY_PANEL_TEXTURE);
				//SpriteUtil.INSTANCE.markSpriteActive(factoryPanelSprite);
			}
		});
	}
}
