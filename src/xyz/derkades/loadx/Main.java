package xyz.derkades.loadx;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import net.md_5.bungee.api.ChatColor;

public class Main extends JavaPlugin {
	
	private static final int CHUNKS_EVERY_TICK = 30;
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
		if (command.getName().equalsIgnoreCase("generate")){
			if (args.length != 1){
				return false;
			}
			
			if (sender instanceof Player){
				Player player = (Player) sender;
				Location loc = player.getLocation();
				
				int blocks = Integer.parseInt(args[0]);
				
				player.sendMessage(ChatColor.BLUE + "Calculating chunk coordinates...");
				
				//Unfortunately I had to use a Block list instead of Chunk list, because bukkit apperently can't handle that many chunks.
				List<Location> chunkBlocks = new ArrayList<>();
				
				for (int x = -blocks; x < blocks; x += 16){
					for (int y = 0; y < loc.getWorld().getMaxHeight(); y += 16){
						for (int z = -blocks; z < blocks; z+= 16){
							Location chunkLoc = new Location(player.getWorld(), loc.getX() + x, y, loc.getZ() + z);
							chunkBlocks.add(chunkLoc);
						}
					}
				}
				
				final int totalChunks = chunkBlocks.size();
				
				player.sendMessage(ChatColor.BLUE + "Done! " + totalChunks + " chunks found.");
				player.sendMessage(ChatColor.AQUA + "Chunk generation will start in a few seconds! Please note that it may take a long time and will probably cause a bit of lag.");				
				
				new BukkitRunnable(){
					
					int passedCount = 0;
					
					public void run(){						
						if (chunkBlocks.size() == 0){ //If there are no chunks left to generate
							player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Chunk loading complete.");
							this.cancel();
							return;
						}
						
						//Get chunks then remove chunks, the next time this is run there are new chunks to generate.
						
						for (int i = 0; i <= CHUNKS_EVERY_TICK; i++){
							passedCount++;
							
							if (chunkBlocks.size() == 0){
								return; //Return for now, next run it will be catched by the code above.
							}
							
							Chunk chunk = chunkBlocks.get(0).getChunk();
							chunkBlocks.remove(0);
							chunk.load(true); //Generate chunk
							chunk.unload(); //Unload chunk to save RAM
						}
						
						int remaining = totalChunks - passedCount;
						player.sendMessage(ChatColor.DARK_AQUA + "Generating chunks... " + ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "|" + ChatColor.AQUA + " Chunks remaining: " + remaining);
					}
				}.runTaskTimer(this, 5*20, 1);
				return true;
			} else {
				sender.sendMessage("You must be a player in order to execute this command, because I can't teleport a console around the map.");
				return false;
			}
		} else {
			return false;
		}
	}
}
