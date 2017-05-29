package xyz.derkades.loadx;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import net.md_5.bungee.api.ChatColor;

public class Main extends JavaPlugin {
	
	//Sorry for one class code :(
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
		if (label.equalsIgnoreCase("generate")){
			if (args.length != 2){
				return false;
			}
			
			if (sender instanceof Player){
				Player player = (Player) sender;
				Location playerLocation = player.getLocation();
				
				int blocks = Integer.parseInt(args[0]);
				int interval = Integer.parseInt(args[1]); 
				
				final List<Location> teleportLocations = new ArrayList<Location>();
				// Add all locations to a list, so we can teleport the player to all locations later.
				for (int x = -blocks; x < blocks + 1; x += 50){
					for (int z = -blocks; z < blocks + 1; z += 50){
						Location location = new Location(
								playerLocation.getWorld(), 
								playerLocation.getX() + x, 
								100, 
								playerLocation.getZ() + z,
								0,
								90);
						teleportLocations.add(location);
					}
				}
				
				player.sendMessage(ChatColor.AQUA + "Teleporting will begin in 10 seconds. Please look at the ground and do not move. Feel free to have a drink and come back later.");
				player.sendMessage(ChatColor.BLUE + "This will take approximately " + ((teleportLocations.size() * interval) / 20) / 60 + " minutes.");
				// Look down
				playerLocation.setPitch(90);
				player.teleport(playerLocation);
				
				final GameMode originalGameMode = player.getGameMode();
				player.setGameMode(GameMode.SPECTATOR);
				
				
				
				// Now teleport the player to all locations with delay
				new BukkitRunnable(){
					
					boolean sendTitle = false;
					
					@SuppressWarnings("deprecation")
					public void run(){
						if (teleportLocations.isEmpty()){
							player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Chunk generating complete!");
							player.sendMessage(ChatColor.GRAY + 
									"You should now restart your server, because due to a bug, Minecraft may "
									+ "keep some chunks loaded. We want these chunks to be generated so "
									+ "they load faster or show up on a map, but we don't want chunks to be loaded when "
									+ "no one is nearby. This pointlessly uses up RAM.");
							player.teleport(playerLocation); //Teleport back to original location
							player.setGameMode(originalGameMode);
							player.sendTitle("", "");
							this.cancel();
							return;
						}
						
						// Get the first location from the list, then remove the first, so the next time we get the first is a different location
						Location teleportLocation = teleportLocations.get(0);
						teleportLocations.remove(0);
						
						player.teleport(teleportLocation);
						
						int timeLeft = (teleportLocations.size() * interval) / 20; //Divide by 20 because interval is in ticks
						
						// The sendTitle boolean thing is to only send the title every other time.
						if (sendTitle){
							player.sendTitle("Teleporting...", "ETA: " + timeLeft + " seconds.");
							sendTitle = false;
						} else {
							sendTitle = true;
						}
					}
				}.runTaskTimer(this, 10*20, interval);
				return true;
			} else {
				sender.sendMessage("You must be a player in order to execute this command, because I can't teleport a console around the map.");
			}
		}
		return false;
	}

}
