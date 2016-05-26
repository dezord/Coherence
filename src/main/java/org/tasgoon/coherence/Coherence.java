package org.tasgoon.coherence;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tasgoon.coherence.client.PostCohere;
import org.tasgoon.coherence.client.handlers.MultiplayerHandler;
import org.tasgoon.coherence.common.Version;
import org.tasgoon.coherence.server.Server;

import java.io.File;
import java.io.IOException;

@SuppressWarnings("unused")
@Mod(modid = Coherence.MODID, version = Coherence.VERSION_STRING, acceptableRemoteVersions = "*", guiFactory = "org.tasgoon.coherence.client.ui.UiConfigFactory")
public class Coherence
{
	@Instance("Coherence")
	public static Coherence instance;
	
    public static final String MODID = "Coherence";
    public static final String VERSION_STRING = "1.8a01";
    public static final Version VERSION = Version.fromString(VERSION_STRING);
    public static final File CONFIG_FILE = new File("coherence", "Coherence.cfg");

    public static int clientPort = 25566;

    public Configuration config;
    public boolean connectOnStart;
	public boolean debug;

    public boolean postCohered = false;
    public static String modsToKeep;

    private static final Logger logger = LogManager.getLogger("Coherence");
    /*public static final int activationTicks = 60; //atm, no point
    private int ticks = 0;
    private boolean connected = true;*/
    
    /**Previous connected address*/
    public String address;
    public static int port;
    
    //=========================================CLIENT SIDE CODE=================================================================
    @EventHandler
    @SideOnly(Side.CLIENT)
    public void preInit(FMLPreInitializationEvent event) throws IOException {
        config = new Configuration(CONFIG_FILE);
    	
    	Property connectProperty = config.get(Configuration.CATEGORY_GENERAL, "connectOnStart", false);
    	connectProperty.setComment("Set this to true if you want to connect back to the server after cohering is done and Minecraft loads again."
    			+ "\nThis is not quite ready yet, so enable this at your own risk.");
    	connectOnStart = connectProperty.getBoolean();
    	
    	Property addressProperty = config.get(Configuration.CATEGORY_GENERAL, "connectToServer", "null");
    	addressProperty.setComment("This tells Coherence what server to connect to on start if connectOnStart is true, and is also for persistence."
    			+ "\nDon't touch this, unless you want to break a lot of things.");
    	address = addressProperty.getString(); addressProperty.set("null");
    	
    	Property debugProperty = config.get(Configuration.CATEGORY_GENERAL, "debug", false);
    	debugProperty.setComment("This tells Coherence if it should turn on advanced debugging features."
    			+ "\nUsed mainly for testing purposes.");
    	debug = debugProperty.getBoolean();
    	
    	Property portProperty = config.get(Configuration.CATEGORY_GENERAL, "port", 25566);
    	portProperty.setComment("This tells Coherence what port it should use."
    			+ "\nChange at your own risk.");
    	clientPort = portProperty.getInt();

    	if (!address.equals("null")) {
    		new PostCohere();
    		postCohered = true;
    	}
    	logger.info("Previously cohered: " + postCohered + ". Previous address: " + address);
    	
    	config.save();
    	
    	try {
			PostCohere.detectCrash();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    @SideOnly(Side.CLIENT)
    @EventHandler
	public void init(FMLInitializationEvent event) {
    	logger.info("Registering event handler");
		//MinecraftForge.EVENT_BUS.register(new UpdateHandler());
        MinecraftForge.EVENT_BUS.register(new MultiplayerHandler());
	}
    //=========================================END CLIENT SIDE CODE=================================================================
    
    //=========================================SERVER SIDE CODE=====================================================================
    @EventHandler
    @SideOnly(Side.SERVER)
    public void postInit(FMLPostInitializationEvent event) throws IOException
    {
    	config = new Configuration(CONFIG_FILE);
    	Property portProperty = config.get(Configuration.CATEGORY_GENERAL, "port", 25566);
    	portProperty.setComment("This tells Coherence what port it should use."
    			+ "\nChange at your own risk.");
    	new Server(portProperty.getInt());
    }
    //=========================================END SERVER SIDE CODE=================================================================
}
