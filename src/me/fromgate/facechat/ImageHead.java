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

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.ChatPaginator;

public class ImageHead {
	private static List<String> lineToList(String message){
		List<String> lines = new ArrayList<String>();
		String [] ln = message.split("\\n");
		for (String s : ln){
			String[] ln2 = ChatPaginator.wordWrap(ChatColor.translateAlternateColorCodes('&', FaceChat.getPlugin().defaultColor+s), 50);
			for (String s2 : ln2) lines.add(s2);
		}
		return lines;
	}

	private static Color[] colors =  {
		new Color(0, 0, 0),           //&0
		new Color(0, 0, 170),         //&1
		new Color(0, 170, 0),         //&2
		new Color(0, 170, 170),       //&3
		new Color(170, 0, 0),         //&4
		new Color(170, 0, 170),       //&5
		new Color(255, 170, 0),       //&6
		new Color(170, 170, 170),     //&7
		new Color(85, 85, 85),        //&8
		new Color(85, 85, 255),       //&9
		new Color(85, 255, 85),       //&a
		new Color(85, 255, 255),      //&b
		new Color(255, 85, 85),       //&c
		new Color(255, 85, 255),      //&d
		new Color(255, 255, 85),      //&e
		new Color(255, 255, 255)      //&f
	};

	private static String[] chatColors = {"&0","&1","&2","&3","&4","&5","&6","&7","&8","&9","&a","&b","&c","&d","&e","&f"};



	public static List<String> getAddLine (String sourcePlayer, String message, List<String> mask, int position){
		List<String> newLines = new ArrayList<String>();
		for (String line : mask)
			newLines.add(line.replace("%player%", sourcePlayer));
		List<String> textLines = lineToList(message);
		for (int i = 0;i<8;i++){
			if (newLines.size()<=i) newLines.add("");
			if (i>=position&&i<position+textLines.size()) newLines.set(i, textLines.get(i-position));
		}
		return newLines;
	}

	public static void printHeadedMessage(Player player, List<String> messageLines) {
		printHeadedMessage (player.getName(), messageLines);
	}
	

	public static void printHeadedMessage(final String player, final List<String> messageLines) {
		Bukkit.getScheduler().runTaskAsynchronously(FaceChat.getPlugin(), new Runnable(){
			@Override
			public void run() {
				List<String> headLine = headToLines (getHeadFromSkin (getSkinByName(player)));

				if (!headLine.isEmpty()) 
					for (int i = 0; i<headLine.size(); i++){
						headLine.set(i, headLine.get(i)+(i<messageLines.size() ? messageLines.get(i) : ""));
					}
				else headLine.addAll(messageLines);

				for (Player p : Bukkit.getOnlinePlayers()){
					if (!FaceChat.getPlugin().separatorLine.isEmpty()) p.sendMessage(ChatColor.translateAlternateColorCodes('&', FaceChat.getPlugin().separatorLine));

					for (int i = 0; i<headLine.size(); i++)	
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', headLine.get(i)));
				}

			}
		});
	}


	public static List<String> headToLines (BufferedImage head){
		List<String> headLine = new ArrayList<String> ();
		if (head == null) return headLine;
		for (int y = 0; y < head.getHeight(); y++){
			String line = "";
			for (int x = 0; x < head.getWidth(); x++){		
				line = line + rgbColor (head.getRGB(x, y), "█");
			}
			headLine.add(line+"  "+FaceChat.getPlugin().defaultColor);
		}
		return headLine;
	}


	private static String rgbColor(int rgb, String string) {
		return getChatColor (rgbToColor(rgb))+string;
	}


	public static BufferedImage getHeadFromSkin (BufferedImage skin){
		BufferedImage face = null;
		if (skin != null){
			face = new BufferedImage (8,8,BufferedImage.TYPE_INT_ARGB);
			for (int x = 0; x<8; x++)
				for (int y = 0; y<8; y++)
					face.setRGB(x, y, skin.getRGB(x+8, y+8));
		}
		return face;
	}

	public static BufferedImage getSkinByName (String name){
		BufferedImage img = getCachedImage(name);
		if (img!=null) return img;
		String strurl = FaceChat.getPlugin().skinUrl+name+".png";
		try {
			URL url = new URL (strurl);
			img = ImageIO.read(url);
			saveToCache (img,name);
		} catch (Exception e){
		}
		return img;
	}


	private static Color rgbToColor (int rgb){
		int r = (rgb>>16)&255;
		int g = (rgb>>8)&255;
		int b = rgb&255;
		return new Color (r,g,b);
	}

	private static String getChatColor (Color color){
		int index = matchColor (color);
		if (index<0||index>15) return "&0";
		return chatColors[index];
	}

	private static int matchColor(Color color) {
		//if (color.getAlpha() < 128) return 0;
		int index = 0;
		double best = -1;
		for (int i = 0; i < colors.length; i++) {
			double distance = getDistance(color, colors[i]);
			if (distance < best || best == -1) {
				best = distance;
				index = i;
			}
		}
		return index;
	}


	private static double getDistance(Color c1, Color c2) {
		double rmean = (c1.getRed() + c2.getRed()) / 2.0;
		double r = c1.getRed() - c2.getRed();
		double g = c1.getGreen() - c2.getGreen();
		int b = c1.getBlue() - c2.getBlue();
		double weightR = 2 + rmean / 256.0;
		double weightG = 4.0;
		double weightB = 2 + (255 - rmean) / 256.0;
		return weightR * r * r + weightG * g * g + weightB * b * b;
	}


	public static void saveToCache (BufferedImage img, String name){
		if (img==null) return;
		File dir = new File (FaceChat.getPlugin().getDataFolder()+File.separator+"cache");
		if (!dir.exists()) dir.mkdirs();
		File cfgFile = new File (dir+File.separator+"cache.yml");

		try {
			File f = new File (dir+File.separator+name+".png");
			ImageIO.write(img, "PNG", f);
			YamlConfiguration cfg = new YamlConfiguration ();
			if (cfgFile.exists()) cfg.load(cfgFile);
			cfg.set(name, System.currentTimeMillis());
			cfg.save(cfgFile);
		} catch (Exception e){
		}


	}

	public static BufferedImage getCachedImage (String name){
		File dir = new File (FaceChat.getPlugin().getDataFolder()+File.separator+"cache");
		if (!dir.exists()) dir.mkdirs();
		File cfgFile = new File (dir+File.separator+"cache.yml");
		YamlConfiguration cfg = new YamlConfiguration ();
		try {
			cfg.load(cfgFile);
		} catch (Exception e) {
			return null;
		}
		if (!cfg.contains(name)) return null;

		Long saveTime = cfg.getLong(name);
		if ((FaceChat.getPlugin().cacheLifeTime*1000*60+saveTime)< System.currentTimeMillis()) return null;

		File f = new File (dir+File.separator+name+".png");
		if (!f.exists()) return null;
		BufferedImage img = null;
		try {
			img = ImageIO.read(f);
		} catch (Exception ignore){
		}
		return img;
	}

}
