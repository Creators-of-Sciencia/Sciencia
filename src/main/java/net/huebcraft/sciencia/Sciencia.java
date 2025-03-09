package net.huebcraft.sciencia;

import java.util.Random;

import net.huebcraft.sciencia.foundation.data.ScienciaRegistrate;

import org.slf4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;

import net.createmod.catnip.lang.FontHelper;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(Sciencia.ID)
public class Sciencia {
	public static final String ID = "create";
	public static final String NAME = "Create";

	public static final Logger LOGGER = LogUtils.getLogger();

	private static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

	public static final Gson GSON = new GsonBuilder().setPrettyPrinting()
		.disableHtmlEscaping()
		.create();

	/**
	 * Use the {@link Random} of a local {@link Level} or {@link Entity} or create one
	 */
	@Deprecated
	public static final Random RANDOM = new Random();

	/**
	 * <b>Other mods should not use this field!</b> If you are an addon developer, create your own instance of
	 * {@link ScienciaRegistrate}.
	 * </br
	 * If you were using this instance to render a callback listener use {@link CreateRegistrateRegistrationCallback#register} instead.
	 */
	private static final ScienciaRegistrate REGISTRATE = ScienciaRegistrate.create(ID)
		.defaultCreativeTab((ResourceKey<CreativeModeTab>) null)
		.setTooltipModifierFactory(item ->
			new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
				.andThen(TooltipModifier.mapNull(KineticStats.create(item)))
		);

	public static final ServerSchematicLoader SCHEMATIC_RECEIVER = new ServerSchematicLoader();
	public static final RedstoneLinkNetworkHandler REDSTONE_LINK_NETWORK_HANDLER = new RedstoneLinkNetworkHandler();
	public static final TorquePropagator TORQUE_PROPAGATOR = new TorquePropagator();
	public static final GlobalRailwayManager RAILWAYS = new GlobalRailwayManager();
	public static final GlobalLogisticsManager LOGISTICS = new GlobalLogisticsManager();
	public static final ServerLagger LAGGER = new ServerLagger();

	public Create(IEventBus eventBus, ModContainer modContainer) {
		onCtor(eventBus, modContainer);
	}

	public static void onCtor(IEventBus modEventBus, ModContainer modContainer) {
		LOGGER.info("{} {} initializing! Commit hash: {}", NAME, CreateBuildInfo.VERSION, CreateBuildInfo.GIT_COMMIT);
		ModLoadingContext modLoadingContext = ModLoadingContext.get();

		REGISTRATE.registerEventListeners(modEventBus);

		AllSoundEvents.prepare();
		AllTags.init();
		AllCreativeModeTabs.register(modEventBus);
		AllArmorMaterials.register(modEventBus);
		AllDisplaySources.register();
		AllDisplayTargets.register();
		AllBlocks.register();
		AllItems.register();
		AllFluids.register();
		AllPaletteBlocks.register();
		AllMenuTypes.register();
		AllEntityTypes.register();
		AllBlockEntityTypes.register();
		AllRecipeTypes.register(modEventBus);
		AllParticleTypes.register(modEventBus);
		AllStructureProcessorTypes.register(modEventBus);
		AllEntityDataSerializers.register(modEventBus);
		AllPackets.register();
		AllFeatures.register(modEventBus);
		AllPlacementModifiers.register(modEventBus);
		AllIngredients.register(modEventBus);
		AllAttachmentTypes.register(modEventBus);
		AllDataComponents.register(modEventBus);
		AllMapDecorationTypes.register(modEventBus);
		AllMountedStorageTypes.register();

		AllConfigs.register(modLoadingContext, modContainer);

		// TODO - Make these use Registry.register and move them into the RegisterEvent
		AllPackagePortTargetTypes.register(modEventBus);

		AllSchematicStateFilters.registerDefaults();

		// FIXME: some of these registrations are not thread-safe
		BogeySizes.init();
		AllBogeyStyles.init();
		// ----

		ComputerCraftProxy.register();

		NeoForgeMod.enableMilkFluid();

		modEventBus.addListener(Create::init);
		modEventBus.addListener(Create::onRegister);
		modEventBus.addListener(AllEntityTypes::registerEntityAttributes);
		modEventBus.addListener(EventPriority.LOWEST, CreateDatagen::gatherData);
		modEventBus.addListener(AllSoundEvents::register);

		// FIXME: this is not thread-safe
		Mods.CURIOS.executeIfInstalled(() -> () -> Curios.init(modEventBus));
		Mods.INVENTORYSORTER.executeIfInstalled(() -> () -> InventorySorterCompat.init(modEventBus));
	}

	public static void init(final FMLCommonSetupEvent event) {
		AllFluids.registerFluidInteractions();
		CreateNBTProcessors.register();

		event.enqueueWork(() -> {
			// TODO: custom registration should all happen in one place
			// Most registration happens in the constructor.
			// These registrations use Create's registered objects directly so they must run after registration has finished.
			BoilerHeaters.registerDefaults();
			AllPortalTracks.registerDefaults();
			AllBlockSpoutingBehaviours.registerDefaults();
			AllMovementBehaviours.registerDefaults();
			AllInteractionBehaviours.registerDefaults();
			AllContraptionMovementSettings.registerDefaults();
			AllOpenPipeEffectHandlers.registerDefaults();
			AllMountedDispenseItemBehaviors.registerDefaults();
			AllUnpackingHandlers.registerDefaults();
			AllInventoryIdentifiers.registerDefaults();
			// --
		});
	}

	public static void onRegister(final RegisterEvent event) {
		AllArmInteractionPointTypes.init();
		AllFanProcessingTypes.init();
		AllItemAttributeTypes.init();
		AllContraptionTypes.init();
		AllPotatoProjectileRenderModes.init();
		AllPotatoProjectileEntityHitActions.init();
		AllPotatoProjectileBlockHitActions.init();

		if (event.getRegistry() == BuiltInRegistries.TRIGGER_TYPES) {
			AllAdvancements.register();
			AllTriggers.register();
		}
	}

	public static LangBuilder lang() {
		return new LangBuilder(ID);
	}

	public static ResourceLocation asResource(String path) {
		return ResourceLocation.fromNamespaceAndPath(ID, path);
	}

	public static ScienciaRegistrate registrate() {
		if (!STACK_WALKER.getCallerClass().getPackageName().startsWith("com.simibubi.create"))
			throw new UnsupportedOperationException("Other mods are not permitted to use create's registrate instance.");
		return REGISTRATE;
	}
}
