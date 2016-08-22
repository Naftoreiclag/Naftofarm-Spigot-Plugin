package naftoreiclag.mc.naftofarms;

import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;

// Java is my nth favorite scripting language!

public class PluginNF extends JavaPlugin {
	// The instance
	private static PluginNF instance;
	public static PluginNF getInstance() { return instance; }
	public PluginNF() { instance = this; }
	
	public ProtocolManager protocolManager;
	public Server server;
	public Logger logger;
	public PluginManager pluginManager;
	
	/* /kill suicides
	 */
	
	@Override
    public void onEnable()
	{
		server = this.getServer();
		logger = this.getLogger();
		pluginManager = server.getPluginManager();
		protocolManager = ProtocolLibrary.getProtocolManager();
		
		PlantLogic debugCMD = new PlantLogic(this);
		
		debugCMD.registerListeners();
		getCommand("test").setExecutor(debugCMD);
		pluginManager.registerEvents(debugCMD, this);
    }
}
