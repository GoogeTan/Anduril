package sybyline.anduril;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.*;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.*;
import net.minecraftforge.fml.event.server.*;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forgespi.language.IModInfo;
import sybyline.anduril.extensions.*;
import sybyline.anduril.network.*;
import sybyline.anduril.scripting.common.CommonScripting;
import sybyline.anduril.scripting.server.ServerManagement;

import org.apache.logging.log4j.*;
import org.apache.maven.artifact.versioning.*;

@Mod(Anduril.MODID)
public class Anduril {

	public static final String MODID = "anduril";
	public static final String NAME = "And�ril";
	public static final ArtifactVersion VERSION = new DefaultArtifactVersion("0.0.0-unknown");
    public static final Logger LOGGER = LogManager.getLogger();

    private static Anduril instance;

    public static Anduril instance() {
    	return instance;
    }

	public SybylineNetwork network;

    public Anduril() {
    	synchronized(Anduril.class) {
    		if (instance != null)
    			throw new IllegalArgumentException("Can't create multiple mod instances!");
    		instance = this;
    	}
        IEventBus mods = FMLJavaModLoadingContext.get().getModEventBus();
	        mods.addListener(this::setup);
	        mods.addListener(this::gameStart);
	        mods.addListener(this::enqueueIMC);
	        mods.addListener(this::processIMC);
        IEventBus forge = MinecraftForge.EVENT_BUS;
        	forge.register(this);
        AndurilGameRules.init();
    }

    private void setup(FMLCommonSetupEvent event) {
    	ModList.get().getModContainerById(MODID).map(ModContainer::getModInfo).map(IModInfo::getVersion).map(ArtifactVersion::toString).ifPresent(VERSION::parseVersion);
    	network = new SybylineNetwork(new ResourceLocation(MODID, "main"), MODID, network -> {
    		network.register(S2CDisplayGui.class, S2CDisplayGui::new);
    		network.register(C2SAttackEntity.class, C2SAttackEntity::new);
    		network.register(S2CSyncGameRules.class, S2CSyncGameRules::new);
    	});
    	CommonScripting.INSTANCE.commonStart(event);
        Submod.loadSubmods();
    }

    private void gameStart(FMLClientSetupEvent event) {
    	CommonScripting.INSTANCE.gameStart(event);
    	Submod.gameStart(event);
    }

    @SubscribeEvent
    public void serverStartPre(FMLServerAboutToStartEvent event) {
    }

    @SubscribeEvent
    public void serverStart(FMLServerStartingEvent event) {
    	ServerManagement.INSTANCE.serverStart(event);
    	CommonScripting.INSTANCE.serverStart(event);
    }

    @SubscribeEvent
    public void serverStartPost(FMLServerStartedEvent event) {
    	event.getServer().reload();
    }

    @SubscribeEvent
    public void serverTick(TickEvent.ServerTickEvent event) {
    	CommonScripting.INSTANCE.serverTick(event);
    }

    @SubscribeEvent
    public void serverClose(FMLServerStoppingEvent event) {
    	CommonScripting.INSTANCE.serverStop(event);
    	ServerManagement.INSTANCE.serverStop(event);
    }

    @SubscribeEvent
    public void serverClosePost(FMLServerStoppedEvent event) {
    }

    private void enqueueIMC(InterModEnqueueEvent event) {
    	Submod.interModCommsEnqueue();
    }

    private void processIMC(InterModProcessEvent event) {
    	event.getIMCStream().forEach(msg -> {
    		// String from = msg.getSenderModId();
    		// String method = msg.getMethod();
    		// Handle compatibility
    	});
    }

/*
    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
        }
    }
*/

}
