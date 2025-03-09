package net.huebcraft.sciencia.compat.curios;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

public class Curios {

	/**
	 * Resolves the Stacks Handler Map given an Entity.
	 * It is recommended to then use a `.map(curiosMap -> curiosMap.get({key})`,
	 * which can be null and would therefore be caught by the Optional::map function.
	 *
	 * @param entity The entity which possibly has a Curio Inventory capability
	 * @return An optional of the Stacks Handler Map
	 */
	private static Optional<Map<String, ICurioStacksHandler>> resolveCuriosMap(LivingEntity entity) {
		return Optional.ofNullable(entity.getCapability(CuriosCapability.INVENTORY)).map(ICuriosItemHandler::getCurios);
	}

	public static void init(IEventBus modEventBus) {
		modEventBus.addListener(Curios::onClientSetup);

		GogglesItem.addIsWearingPredicate(player -> resolveCuriosMap(player)
			.map(curiosMap -> {
				for (ICurioStacksHandler stacksHandler : curiosMap.values()) {
					// Search all the curio slots for Goggles existing
					int slots = stacksHandler.getSlots();
					for (int slot = 0; slot < slots; slot++) {
						if (AllItems.GOGGLES.isIn(stacksHandler.getStacks().getStackInSlot(slot))) {
							return true;
						}
					}
				}

				return false;
			})
			.orElse(false));

		CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> modEventBus.addListener(CuriosRenderers::onLayerRegister));
	}

	private static void onClientSetup(final FMLClientSetupEvent event) {
		CuriosRenderers.register();
	}
}
