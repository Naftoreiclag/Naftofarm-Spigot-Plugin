package naftoreiclag.mc.naftofarms;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Dropper;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import com.comphenix.protocol.wrappers.MultiBlockChangeInfo;
import com.comphenix.protocol.wrappers.WrappedBlockData;

public class PlantLogic implements CommandExecutor, Listener {

	public PluginNF plugin;
	
	public PlantLogic(PluginNF plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
		if(cmd.getName().equalsIgnoreCase("test")) {
			if(!(sender instanceof Player)) {
				sender.sendMessage("Must be player to use this command.");
				return true;
			}
			
			Player player = (Player) sender;
			
			if(!player.hasPermission("naftofarms.test")) {
				sender.sendMessage("You do not have permission to use this command.");
				return true;
			}
			
			PacketContainer test = plugin.protocolManager.createPacket(PacketType.Play.Server.BLOCK_CHANGE, false);
			
			test.getBlockPositionModifier().write(0, new BlockPosition(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ()));
			test.getBlockData().write(0, WrappedBlockData.createData(Material.CAKE_BLOCK));
			
			try {
				plugin.protocolManager.sendServerPacket(player, test);
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
			
			return true;
		}
		
		return false;
	}
	
	public boolean sameBlockXYZ(Location a, Location b) {
		return a.getBlockX() == b.getBlockX() && a.getBlockY() == b.getBlockY() && a.getBlockZ() == b.getBlockZ();
	}
	
	public void plantAppleTree(Location blockLocation) {
		World world = blockLocation.getWorld();
		
		Block baseBlock = world.getBlockAt(blockLocation);
		baseBlock.setData((byte) 0);
		baseBlock.setType(Material.DROPPER);
		
		Dropper chest = (Dropper) baseBlock.getState();
		
		ItemStack potato = new ItemStack(Material.COBBLESTONE);
		ItemMeta im = potato.getItemMeta();
		im.setDisplayName("nf_tree_apple");
		potato.setItemMeta(im);
		
		ItemStack[] inventory = chest.getInventory().getContents();
		inventory[0] = potato;
		
		chest.getInventory().setContents(inventory);

		Block saplingBlock = world.getBlockAt(blockLocation.getBlockX(), blockLocation.getBlockY() + 1, blockLocation.getBlockZ());
		Location saplingBlockLocation = saplingBlock.getLocation();
		
		saplingBlock.setType(Material.LEVER);
		saplingBlock.setData((byte) 5);
		
		PacketContainer dirtify = plugin.protocolManager.createPacket(PacketType.Play.Server.BLOCK_CHANGE, false);
		dirtify.getBlockPositionModifier().write(0, new BlockPosition(blockLocation.getBlockX(), blockLocation.getBlockY(), blockLocation.getBlockZ()));
		dirtify.getBlockData().write(0, WrappedBlockData.createData(Material.DIRT, 1));
		broadcast(dirtify);
		
		PacketContainer saplify = plugin.protocolManager.createPacket(PacketType.Play.Server.BLOCK_CHANGE, false);
		saplify.getBlockPositionModifier().write(0, new BlockPosition(saplingBlockLocation.getBlockX(), saplingBlockLocation.getBlockY(), saplingBlockLocation.getBlockZ()));
		saplify.getBlockData().write(0, WrappedBlockData.createData(Material.SAPLING, 5));
		broadcast(saplify);
	}
	
	public void broadcast(PacketContainer test) {

		for(Player player : plugin.server.getOnlinePlayers()) {
			try {
				plugin.protocolManager.sendServerPacket(player, test);
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void registerListeners() {
		
		PacketAdapter listener = new PacketAdapter(plugin, 
				PacketType.Play.Server.BLOCK_CHANGE, 
				PacketType.Play.Server.MULTI_BLOCK_CHANGE,
				PacketType.Play.Server.MAP_CHUNK) {
			
			@Override
			public void onPacketSending(PacketEvent event) {
				
				PacketContainer packet = event.getPacket();
				if(event.getPacketType() == PacketType.Play.Server.BLOCK_CHANGE) {
					
					WrappedBlockData blockData = packet.getBlockData().getValues().get(0);
					if(blockData.getType() == Material.DROPPER) {
						packet.getBlockData().write(0, WrappedBlockData.createData(Material.DIRT, 1));
					} else if(blockData.getType() == Material.LEVER) {
						packet.getBlockData().write(0, WrappedBlockData.createData(Material.SAPLING, 5));
					}
				} else if(event.getPacketType() == PacketType.Play.Server.MULTI_BLOCK_CHANGE) {
					
					
					ChunkCoordIntPair asdf = packet.getChunkCoordIntPairs().read(0);
					int chunkBlockX = asdf.getChunkX();
					int chunkBlockY = asdf.getChunkZ();
					
					System.out.println(chunkBlockX + ", " + chunkBlockY);

					/*
					System.out.println(": " + packet.getAttributeCollectionModifier().size());
					System.out.println(": " + packet.getBlockData().size());
					System.out.println(": " + packet.getBlockPositionCollectionModifier().size());
					System.out.println(": " + packet.getBlockPositionModifier().size());
					System.out.println(": " + packet.getBlocks().size());
					System.out.println(": " + packet.getBooleans().size());
					System.out.println(": " + packet.getByteArrays().size());
					System.out.println(": " + packet.getByteArraySerializer().size());
					System.out.println(": " + packet.getBytes().size());
					System.out.println(": " + packet.getChatComponentArrays().size());
					System.out.println(": " + packet.getChatComponents().size());
					System.out.println(": " + packet.getChatVisibilities().size());
					System.out.println(": " + packet.getChunkCoordIntPairs().size());
					System.out.println(": " + packet.getClientCommands().size());
					System.out.println(": " + packet.getCombatEvents().size());
					System.out.println(": " + packet.getDataWatcherModifier().size());
					System.out.println(": " + packet.getDifficulties().size());
					System.out.println(": " + packet.getDirections().size());
					System.out.println(": " + packet.getDoubles().size());
					System.out.println(": " + packet.getEffectTypes().size());
					System.out.println(": " + packet.getEntityUseActions().size());
					System.out.println(": " + packet.getFloat().size());
					System.out.println(": " + packet.getGameModes().size());
					System.out.println(": " + packet.getGameProfiles().size());
					System.out.println(": " + packet.getHands().size());
					System.out.println(": " + packet.getIntegerArrays().size());
					System.out.println(": " + packet.getIntegers().size());
					System.out.println(": " + packet.getItemArrayModifier().size());
					System.out.println(": " + packet.getItemModifier().size());
					System.out.println(": " + packet.getItemSlots().size());
					System.out.println(": " + packet.getLongs().size());
					System.out.println(": " + packet.getModifier().size());
					System.out.println(": " + packet.getMultiBlockChangeInfoArrays().size());
					System.out.println(": " + packet.getNbtModifier().size());
					System.out.println(": " + packet.getParticles().size());
					*/
					
					MultiBlockChangeInfo[] mbci = packet.getMultiBlockChangeInfoArrays().read(0);
					
					for(int i = 0; i < mbci.length; ++ i) {
						
						WrappedBlockData blockData = mbci[i].getData();

						if(blockData.getType() == Material.DROPPER) {
							blockData.setType(Material.DIRT);
							blockData.setData(1);
						} else if(blockData.getType() == Material.LEVER) {
							blockData.setType(Material.SAPLING);
							blockData.setData(5);
						}
						mbci[i].setData(blockData);
					}
					
					packet.getMultiBlockChangeInfoArrays().write(0, mbci);
				} else if(event.getPacketType() == PacketType.Play.Server.MAP_CHUNK) {

					System.out.println("Type:" +                               packet.getType().toString());
					System.out.println("AttributeCollectionModifier: " +       packet.getAttributeCollectionModifier().size());
					System.out.println("BlockData: " +                         packet.getBlockData().size());
					System.out.println("BlockPositionCollectionModifier: " +   packet.getBlockPositionCollectionModifier().size());
					System.out.println("BlockPositionModifier: " +             packet.getBlockPositionModifier().size());
					System.out.println("Blocks: " +                            packet.getBlocks().size());
					System.out.println("Booleans: " +                          packet.getBooleans().size());
					System.out.println("ByteArrays: " +                        packet.getByteArrays().size());
					System.out.println("Bytes: " +                             packet.getBytes().size());
					System.out.println("ChatComponentArrays: " +               packet.getChatComponentArrays().size());
					System.out.println("ChatComponents: " +                    packet.getChatComponents().size());
					System.out.println("ChatVisibilities: " +                  packet.getChatVisibilities().size());
					System.out.println("ChunkCoordIntPairs: " +                packet.getChunkCoordIntPairs().size());
					System.out.println("ClientCommands: " +                    packet.getClientCommands().size());
					System.out.println("CombatEvents: " +                      packet.getCombatEvents().size());
					System.out.println("DataWatcherModifier: " +               packet.getDataWatcherModifier().size());
					System.out.println("Difficulties: " +                      packet.getDifficulties().size());
					System.out.println("Directions: " +                        packet.getDirections().size());
					System.out.println("Doubles: " +                           packet.getDoubles().size());
					System.out.println("EffectTypes: " +                       packet.getEffectTypes().size());
					System.out.println("EntityUseActions: " +                  packet.getEntityUseActions().size());
					System.out.println("Float: " +                             packet.getFloat().size());
					System.out.println("GameModes: " +                         packet.getGameModes().size());
					System.out.println("GameProfiles: " +                      packet.getGameProfiles().size());
					System.out.println("Hands: " +                             packet.getHands().size());
					System.out.println("IntegerArrays: " +                     packet.getIntegerArrays().size());
					System.out.println("Integers: " +                          packet.getIntegers().size());
					System.out.println("ItemArrayModifier: " +                 packet.getItemArrayModifier().size());
					System.out.println("ItemModifier: " +                      packet.getItemModifier().size());
					System.out.println("ItemSlots: " +                         packet.getItemSlots().size());
					System.out.println("Longs: " +                             packet.getLongs().size());
					System.out.println("Modifier: " +                          packet.getModifier().size());
					System.out.println("MultiBlockChangeInfoArrays: " +        packet.getMultiBlockChangeInfoArrays().size());
					System.out.println("NbtModifier: " +                       packet.getNbtModifier().size());
					System.out.println("Particles: " +                         packet.getParticles().size());
					System.out.println("PlayerActions: " +                     packet.getPlayerActions().size());
					System.out.println("PlayerDigTypes: " +                    packet.getPlayerDigTypes().size());
					System.out.println("PlayerInfoAction: " +                  packet.getPlayerInfoAction().size());
					System.out.println("PlayerInfoAction: " +                  packet.getPlayerInfoAction().size());
					System.out.println("PlayerInfoDataLists: " +               packet.getPlayerInfoDataLists().size());
					System.out.println("PositionCollectionModifier: " +        packet.getPositionCollectionModifier().size());
					System.out.println("PositionModifier: " +                  packet.getPositionModifier().size());
					System.out.println("Protocols: " +                         packet.getProtocols().size());
					System.out.println("ResourcePackStatus: " +                packet.getResourcePackStatus().size());
					System.out.println("ScoreboardActions: " +                 packet.getScoreboardActions().size());
					System.out.println("ServerPings: " +                       packet.getServerPings().size());
					System.out.println("Shorts: " +                            packet.getShorts().size());
					System.out.println("SoundCategories: " +                   packet.getSoundCategories().size());
					System.out.println("SoundEffects: " +                      packet.getSoundEffects().size());
					System.out.println("StatisticMaps: " +                     packet.getStatisticMaps().size());
					System.out.println("StringArrays: " +                      packet.getStringArrays().size());
					System.out.println("Strings: " +                           packet.getStrings().size());
					System.out.println("TitleActions: " +                      packet.getTitleActions().size());
					System.out.println("UUIDs: " +                             packet.getUUIDs().size());
					System.out.println("Vectors: " +                           packet.getVectors().size());
					System.out.println("WatchableCollectionModifier: " +       packet.getWatchableCollectionModifier().size());
					System.out.println("WorldBorderActions: " +                packet.getWorldBorderActions().size());
					System.out.println("WorldTypeModifier: " +                 packet.getWorldTypeModifier().size());
				}
			}
		};
		
		plugin.protocolManager.addPacketListener(listener);
	}
	
	@EventHandler
	public void onBlockPlaceEvent(BlockPlaceEvent event)
	{
		Block blockPlaced = event.getBlockPlaced();
		
		if(blockPlaced.getType() == Material.DIRT && blockPlaced.getData() == 1) {
			Player player = event.getPlayer();
			Location blockLocation = blockPlaced.getLocation();
			
			List<Entity> nearbyEntities = player.getNearbyEntities(10, 10, 10);
			
			boolean valid = false;
			
			for(Entity entity : nearbyEntities) {
				if(entity instanceof Item) {
					Item item = (Item) entity;
					
					ItemStack itemStack = item.getItemStack();
					
					if(itemStack.getType() == Material.APPLE) {
						
						Location itemLocation = item.getLocation();
						
						if(sameBlockXYZ(itemLocation, blockLocation)) {
							
							if(!player.hasPermission("naftofarms.plant.tree.apple")) {
								player.sendMessage(ChatColor.RED + "Denied planting of apple tree!");
								return;
							}
							
							if(itemStack.getAmount() > 1) {
								itemStack.setAmount(itemStack.getAmount() - 1);
							} else {
								item.remove();
							}
							
							valid = true;
						}
					}
				}
			}
			
			if(valid) {
				player.sendMessage("success");
				
				plantAppleTree(blockLocation);
			}
		}
	}
	
	@EventHandler
	public void onPlayerInteractEvent(PlayerInteractEvent event) {
		Block interacted = event.getClickedBlock();
		
		if(interacted == null) {
			return;
		}

		Player player = event.getPlayer();
		
		Location interactedLoc = interacted.getLocation();
		if(interacted.getType() == Material.LEVER) {
			Block baseBlock = interactedLoc.getWorld().getBlockAt(interactedLoc.getBlockX(), interactedLoc.getBlockY() - 1, interactedLoc.getBlockZ());
			Block saplingBlock = interacted;
			
			boolean validTree = false;
			
			if(baseBlock.getType() == Material.DROPPER) {
				Dropper dropper = (Dropper) baseBlock.getState();
				ItemStack[] inventory = dropper.getInventory().getContents();
				if(inventory[0].getType() == Material.COBBLESTONE && inventory[0].getItemMeta().getDisplayName().equals("nf_tree_apple")) {
					validTree = true;
				}
			}
			
			if(validTree) {
				// Do not interact with the lever
				event.setCancelled(true);
				
				// Trying to break it
				if(event.getAction() == Action.LEFT_CLICK_BLOCK) {
					saplingBlock.setType(Material.AIR);
					saplingBlock.setData((byte) 0);
					
				} else if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {

					ItemStack usedItem = event.getItem();
					
					if(usedItem == null) {
						return;
					}
					
					// Using bonemeal
					if(usedItem.getType() == Material.INK_SACK && usedItem.getData().getData() == (byte) 15) {
						
						if(event.getPlayer().getGameMode() != GameMode.CREATIVE) {
							if(usedItem.getAmount() > 1) {
								usedItem.setAmount(usedItem.getAmount() - 1);
							} else {
								usedItem.setType(Material.AIR);
								player.getInventory().setItemInHand(usedItem);
							}
							player.updateInventory();
						}
						
						event.getPlayer().sendMessage("bone");
						
						interacted.getWorld().spigot().playEffect(interactedLoc.add(0.5f, 0.5f, 0.5f), Effect.VILLAGER_PLANT_GROW, 0, 0, 0.5f, 0.5f, 0.5f, 1.0f, 10, 20);
						
					}
				}
			}
			
		} else if(interacted.getType() == Material.DROPPER) {
			Block saplingBlock = interactedLoc.getWorld().getBlockAt(interactedLoc.getBlockX(), interactedLoc.getBlockY() + 1, interactedLoc.getBlockZ());
			Block baseBlock = interacted;
			
			if(saplingBlock.getType() == Material.LEVER && saplingBlock.getData() == (byte) 5) {
				// Do not interact with the lever
				event.setCancelled(true);

				// Trying to break it
				if(event.getAction() == Action.LEFT_CLICK_BLOCK) {
					
				} else if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
					
				}
			}
		}
	}
}
