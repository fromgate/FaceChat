/*  
 *  FaceChat (Minecraft bukkit plugin)
 *  (c)2014, fromgate, fromgate@gmail.com
 *  http://dev.bukkit.org/bukkit-plugins/facechat/
 *    
 *  This file is part of ReActions.
 *  
 *  ReActions is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  FaceChat is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with FaceChat.  If not, see <http://www.gnorg/licenses/>.
 * 
 */


package me.fromgate.facechat;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class FaceChat extends JavaPlugin implements CommandExecutor, Listener {
	private static FaceChat instance;
	public static FaceChat getPlugin() {
		return instance;
	}

	boolean enableUpdateChecker;
	
	String skinUrl;
	String separatorLine;
	boolean messageOnJoin;
	boolean messageInChat;
	boolean removeJoinMessage;
	
	int cacheLifeTime; 
	int messageLine;
	List<String> lineMask;
	List<String> joinMask;
	String defaultColor;

	
	@Override
	public void onEnable(){
		instance = this;
		getServer().getPluginManager().registerEvents(this, this);
		getCommand("facechat").setExecutor(this);
		load();
		save();
		UpdateChecker.init(this, "FaceChat", "82886", "facechat", enableUpdateChecker);
	}
	
	@EventHandler(priority=EventPriority.LOWEST, ignoreCancelled = true)
	public void onJoin (PlayerJoinEvent event){
		if (!messageOnJoin) return;
		if (!event.getPlayer().hasPermission("facechat.join")) return;
		List<String> joinMessages = new ArrayList<String>();
		for (String message : this.joinMask)
			joinMessages.add(message.replace("%player%", event.getPlayer().getName()));
		ImageHead.printHeadedMessage (event.getPlayer(), joinMessages);
		if (removeJoinMessage) event.setJoinMessage(null);
	}
	
	
    @EventHandler(priority=EventPriority.HIGH, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event){
    	if (!messageInChat) return;
    	if (!event.getPlayer().hasPermission("facechat.chat")) return;
    	ImageHead.printHeadedMessage (event.getPlayer(), ImageHead.getAddLine(event.getPlayer().getName(), event.getMessage(), this.lineMask, this.messageLine));
    	event.setCancelled(true);
    }

    private boolean reloadCfg (CommandSender sender){
    	if (sender!=null) sender.sendMessage(ChatColor.DARK_GREEN+"FaceChat configuration reloaded!");
    	load();
    	return true;
    }
    
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
		if (args.length == 0) return false;
		Player player = (sender instanceof Player) ? (Player) sender : null;
		
		if (args[0].equalsIgnoreCase("reload")){
				if (player==null||player.hasPermission("facechat.reload")) return reloadCfg(sender);
				else return false;
		}
		
		
		if (!player.hasPermission("facechat.command")) return false;
		String playerName = player.getName();
		
		String message = "";
		for (int i = 0; i<args.length; i++){
			if (i==0) {
				if (args[0].toLowerCase().startsWith("player:")&&args[0].length()>7){
					if (player.hasPermission("facechat.command.player")) playerName = args[0].substring(7); 
				} else message = args[0];
			}
			message = i==0 ? message : message+" "+args[i];
		}
			ImageHead.printHeadedMessage (playerName, ImageHead.getAddLine(player.getName(),message.trim(), this.lineMask, this.messageLine));
		return true;
	}
	
	public void save(){
		getConfig().set("general.check-updates",enableUpdateChecker);
		getConfig().set("face.skin-url",skinUrl);
		getConfig().set("face.separator",separatorLine);
		getConfig().set("face.cache-lifetime-(minutes)",cacheLifeTime);
		getConfig().set("face.default-color",defaultColor);
		getConfig().set("face.chat.enable-for-chat-messages",messageInChat);
		getConfig().set("face.chat.message-start-line",messageLine);
		getConfig().set("face.chat.message",lineMask);
		getConfig().set("face.join.show-join-message",messageOnJoin);
		getConfig().set("face.join.remove-default-join-message",removeJoinMessage);
		getConfig().set("face.join.message",joinMask);
		saveConfig();
	}
	
	public void load(){
		reloadConfig();
		enableUpdateChecker = getConfig().getBoolean("general.check-updates",true);
		skinUrl = getConfig().getString("face.skin-url","http://s3.amazonaws.com/MinecraftSkins/");
		separatorLine = getConfig().getString("face.separator","&8++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		cacheLifeTime = getConfig().getInt("face.cache-lifetime-(minutes)",30);
		defaultColor = getConfig().getString("face.default-color","&e");
		lineMask = getConfig().getStringList("face.chat.message");
		messageInChat = getConfig().getBoolean("face.chat.enable-for-chat-messages",false);
		messageLine = getConfig().getInt("face.chat.message-start-line",3);
		if (lineMask == null||lineMask.isEmpty()){
			lineMask = new ArrayList<String>();			
			lineMask.add("&6&l%player%");
			lineMask.add("+--------------------------------------------------+");
			lineMask.add("");
			lineMask.add("");
			lineMask.add("");
			lineMask.add("");
			lineMask.add("+--------------------------------------------------+");
			lineMask.add("&6&lFaceChat plugin by fromgate");
		}
		messageOnJoin = getConfig().getBoolean("face.join.show-join-message",true);
		removeJoinMessage = getConfig().getBoolean("face.join.remove-default-join-message",true);
		joinMask = getConfig().getStringList("face.join.message");
		if (joinMask == null||joinMask.isEmpty()){
			joinMask = new ArrayList<String>();
			joinMask.add("");
			joinMask.add("+--------------------------------------------------+");
			joinMask.add("");
			joinMask.add("&6&l%player%&ejoined the game!");
			joinMask.add("");
			joinMask.add("");
			joinMask.add("+--------------------------------------------------+");
			joinMask.add("&6&lFaceChat plugin by fromgate");
		}
	}

    
}
