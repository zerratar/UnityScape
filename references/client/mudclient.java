package org.rscemulation.client;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.Random;
import java.util.Map.Entry;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.text.DateFormat;
import java.net.UnknownHostException;

import javax.imageio.ImageIO;
/* 
import javazoom.jl.player.advanced.jlap;
*/
import org.rscemulation.client.entityhandling.EntityHandler;
import org.rscemulation.client.entityhandling.Strings;
import org.rscemulation.client.entityhandling.defs.DoorDef;
import org.rscemulation.client.entityhandling.defs.GameObjectDef;
import org.rscemulation.client.entityhandling.defs.ItemDef;
import org.rscemulation.client.entityhandling.defs.NPCDef;
import org.rscemulation.client.model.Sprite;
// import org.rscemulation.client.recorder.Recorder;
import org.rscemulation.client.util.ChatFilter;
import org.rscemulation.client.util.DataConversions;
import org.rscemulation.loader.AppletLoader;

@SuppressWarnings({})
public final class mudclient extends GameWindowMiddleMan {
	private static final long serialVersionUID = 1L;
	
	public static mudclient mc;
	
	public static final int SPRITE_MEDIA_START 		= 2000;
	public static final int SPRITE_UTIL_START 		= 2100;
	public static final int SPRITE_ITEM_START 		= 2150;
	public static final int SPRITE_LOGO_START 		= 3150;
	public static final int SPRITE_PROJECTILE_START = 3160;
	public static final int SPRITE_TEXTURE_START 	= 3220;
	
	public static final void main(String[] args) throws Exception {
		File RF = new File(RSCE_DIR);
		File RDF = new File(DATA_DIR);		
		try {
			if (!RF.isDirectory() || !RDF.isDirectory()) {
				System.out.println("Welcome to RSCEmulation!\n");
				System.out.println("As the location \"" + RSCE_DIR + "\" doesn't exist RSCEmulation will now attempt to run in external mode...\n");
				System.out.println("Where possible, it's always recommended that you launch the client through the automatic client updater to ensure that you have the latest version. External mode should only be used if you cannot do that.\n");
				RSCE_DIR = "";
				DATA_DIR = RSCE_DIR + "data" + System.getProperty("file.separator");
				MEDIA_DIR = RSCE_DIR + "media" + System.getProperty("file.separator");
				LOG_DIR = RSCE_DIR + "logs" + System.getProperty("file.separator");
				SOUND_DIR = DATA_DIR + "sfx" + System.getProperty("file.separator");
			}
		} catch(Exception ex) {
			System.out.println(ex.getStackTrace());
		}
		File RDF2 = new File(DATA_DIR);	
		try {
			if (!RDF2.isDirectory()) {
				System.out.println("DATA_DIR doesn't exist... RSCEmulation will now close.");
				System.out.println("Please post a topic in the \"Support\" forum section.\n");
				System.exit(-1);
			}
		} catch(Exception ex) {
			System.out.println(ex.getStackTrace());
		}		
		File MDF = new File(MEDIA_DIR);	
		try {
			if (!MDF.isDirectory())
				MDF.mkdirs();
		} catch(Exception ex) {
			System.out.println(ex.getStackTrace());
		}
		
		File CF = new File(RSCE_DIR + "RSCEmulation.conf");
		try {
			if (!CF.exists())
				CF.createNewFile();
		} catch(Exception ex) {
			System.out.println(ex.getStackTrace());
		}
		
		setConf();
		loadConf();
		
		GameWindowMiddleMan.clientVersion = 155;
		
		int width = 512;
		int height = 334;
		
		try {
			width = Integer.parseInt(args[0]);
			height = Integer.parseInt(args[1]);
		} catch(Exception e) {}
		mc = new mudclient(width, height);
		try {
		
					GameWindowMiddleMan.serverIP =  "192.99.152.220"; // "72.20.32.58"; //213.105.74.14
			GameWindowMiddleMan.port = 53595;// 47223;
			mc.VCS_IP   = "192.99.152.220"; // "72.20.32.58"; // 72.20.32.58
			mc.VCS_PORT = 53595; // 47220;
		
			/* GameWindowMiddleMan.serverIP = args[2];       
			GameWindowMiddleMan.port = Integer.parseInt(args[3]);
			mc.VCS_IP   = args[4];
			mc.VCS_PORT = Integer.parseInt(args[5]); */
		} catch(Exception e) {
			GameWindowMiddleMan.serverIP =  "192.99.152.220"; // "72.20.32.58"; //213.105.74.14
			GameWindowMiddleMan.port = 53595;// 47223;
			mc.VCS_IP   = "192.99.152.220"; // "72.20.32.58"; // 72.20.32.58
			mc.VCS_PORT = 53595; // 47220;
		}
		mc.appletMode = false;
		mc.createWindow(gameWidth, gameHeight + 11, "RSCEmulation", false);
		mc.setLogo(Toolkit.getDefaultToolkit().getImage(DATA_DIR + "logo"));
		mc.messagesArray = new String[5];
		mc.messagesTimeout = new int[mc.messagesArray.length];
	}
	
	public final void init() {
		appletMode = true;
		GameWindowMiddleMan.serverIP      = "72.20.32.58"; //213.105.74.14
		GameWindowMiddleMan.port          = 47223;
		GameWindowMiddleMan.clientVersion = 155;
		mc = new mudclient(AppletLoader.width, AppletLoader.height);
		AppletLoader.createConstructor(this, mc);
		this.appletWidth  = AppletLoader.width;
		this.appletHeight = AppletLoader.height;
		this.setLogo(Toolkit.getDefaultToolkit().getImage(DATA_DIR + "logo"));
		loadingScreen = 1;
		startThread(this);
	}

	public static void setConf() {
        try {
			Properties props = new Properties();
			props.load(new FileInputStream(RSCE_DIR + "RSCEmulation.conf"));
			
			try {
				if (!props.getProperty("CHAT_LOGGING").equalsIgnoreCase("ON") && !props.getProperty("CHAT_LOGGING").equalsIgnoreCase("OFF"))
					setProp("CHAT_LOGGING", "OFF");
			} catch (Exception ex) {
				setProp("CHAT_LOGGING", "OFF");
			}
			try {
				if (!props.getProperty("CHAT_FILTER").equalsIgnoreCase("ON") && !props.getProperty("CHAT_FILTER").equalsIgnoreCase("OFF"))
					setProp("CHAT_FILTER", "ON");
			} catch (Exception ex) {
				setProp("CHAT_FILTER", "ON");
			}
			try {
				if (!props.getProperty("FOG").equalsIgnoreCase("ON") && !props.getProperty("FOG").equalsIgnoreCase("OFF"))
					setProp("FOG", "ON");
			} catch (Exception ex) {
				setProp("FOG", "ON");
			}
			try {
				if (!props.getProperty("ADVANCED_MENU").equalsIgnoreCase("ON") && !props.getProperty("ADVANCED_MENU").equalsIgnoreCase("OFF"))
					setProp("ADVANCED_MENU", "OFF");
			} catch (Exception ex) {
				setProp("ADVANCED_MENU", "OFF");
			}	
			try {
				if (!props.getProperty("SIDE_MENU").equalsIgnoreCase("ON") && !props.getProperty("SIDE_MENU").equalsIgnoreCase("OFF"))
					setProp("SIDE_MENU", "OFF");
			} catch (Exception ex) {
				setProp("SIDE_MENU", "OFF");
			}
			try {
				if (Integer.parseInt(props.getProperty("MOVIE_FPS")) < 5 || Integer.parseInt(props.getProperty("MOVIE_FPS")) > 100)
					setProp("MOVIE_FPS", "40");
			} catch (Exception ex) {
				setProp("MOVIE_FPS", "40");
			}				

			props.clear();
		} catch (Exception ex) {}
	}
	
    public static void loadConf() {
        try {
			Properties props = new Properties();
			props.load(new FileInputStream(RSCE_DIR + "RSCEmulation.conf"));
			MOVIE_FPS = Integer.parseInt(props.getProperty("MOVIE_FPS"));
			
			if (props.getProperty("CHAT_LOGGING").equalsIgnoreCase("ON"))
				loggingEnabled = true;
			else
				loggingEnabled = false;
			if (props.getProperty("CHAT_FILTER").equalsIgnoreCase("OFF"))
				chatFilter = false;
			else
				chatFilter = true;
			if (props.getProperty("FOG").equalsIgnoreCase("OFF"))					
				fog = false;
			else
				fog = true;
			if (props.getProperty("ADVANCED_MENU").equalsIgnoreCase("ON"))					
				ADVANCED_MENU = true;
			else
				ADVANCED_MENU = false;
				
			props.clear();
		} catch (Exception ex) {} 
    }	
	
	public static mudclient getMC() {
		return mc;
	}
	
	public Menu getGameMenu() {
		return gameMenu;
	}
	
	private static boolean logEvent(String log, int type) {
		BufferedWriter f = null;
		try {
			Date now = new Date();
			f = new BufferedWriter(new FileWriter(logFile, true));
			if (logType != 0 && logType != type) {
				f.newLine();
				f.newLine();
			}
			logType = type;
			f.write(DateFormat.getTimeInstance(DateFormat.MEDIUM).format(now) + ": " + log);
			f.newLine();
			f.flush();
		} catch (IOException ex) {
			System.out.println("Cannot find log file: " + logFile);
			ex.printStackTrace();
		} finally {
			if (f != null)
				try {
					f.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
		}
		return true;
	}
	
	private static String generateLogFile(String currentLogName) {
		logName = currentLogName;
		String[] monthName = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
		Calendar c = Calendar.getInstance();
		File year = new File(LOG_DIR + c.get(Calendar.YEAR) + System.getProperty("file.separator"));	
		try {
			if (!year.isDirectory())
				year.mkdirs();
			logFile = year.getPath() + System.getProperty("file.separator");
			File month = new File(logFile + monthName[c.get(Calendar.MONTH)]);	
			if (!month.isDirectory())
				month.mkdirs();
			logFile = month.getPath() + System.getProperty("file.separator");
			File day = new File(logFile + c.get(Calendar.DAY_OF_MONTH));
			if (!day.isDirectory())
				day.mkdirs();
			logFile = day.getPath() + System.getProperty("file.separator") + currentLogName + ".txt";
			File character = new File(logFile);
			if (!character.exists())
				character.createNewFile();									
		} catch(Exception ex) {
			System.out.println(ex.getStackTrace());
		}	
		return logFile;
	}	
	
	private final boolean mouseInCoords(int x1, int x2, int y1, int y2) {
		return
		super.mouseX > x1 &&
		super.mouseX < x2 &&
		super.mouseY > y1 &&
		super.mouseY < y2;
		
	}
	
    private static final int[] getCenter(int[] coords) {
        int x = (int) ((coords[0] + coords[1]) / 2);
        int y = (int) ((coords[2] + coords[3]) / 2);
        return new int[] { x, y};
    }

    private static final boolean inArea(int x, int y, int[] coords) {
        return x >= coords[0] && x <= coords[1] && y >= coords[2] && y <= coords[3];
    }
	
    private boolean isOnFriendsList(String username){
		for (int i = 0;i < super.friendsListLongs.length;i++)
			if (DataOperations.longToString(super.friendsListLongs[i]).equalsIgnoreCase(username))
				return true;
		return false;
	}
	
	private boolean isOnClanList(String username) {
		for (int i = 0;i < super.clanListLongs.length;i++)
			if (DataOperations.longToString(super.clanListLongs[i]).equalsIgnoreCase(username))
				return true;
		return false;
	}

	public void handleScroll(int x) {
		if (x > 1)
			x += x;
		else if (x < -1)
			x -= (-x);
        if (mouseOverMenu == 5)  
                friendsMenu.scroll(friendsMenuHandle, x);
        else if (mouseOverMenu == 4)
                spellMenu.scroll(spellMenuHandle, x);
		else if (mouseOverMenu == 3)
			questMenu.scroll(questMenuHandle, x);
        else if (messagesTab == 1)                                                   
                gameMenu.scroll(messagesHandleType2, x);
        else if (messagesTab == 2)
                gameMenu.scroll(messagesHandleType5, x);
        else if (messagesTab == 3)
                gameMenu.scroll(messagesHandleType6, x);
	}
	
	public static String getHostname(String ip) {
		try {
			InetAddress addr = InetAddress.getByName(ip);
			if(addr.isReachable(200) && !addr.isSiteLocalAddress())
				return addr.getHostName();
			else
				return ip;
		} catch (UnknownHostException e) {} catch (IOException e) {}

		return ip;
	}

	 public void launchURL(String s){
			String s1 = System.getProperty("os.name");
			try {
				if (s1.startsWith("Windows"))
					Runtime.getRuntime().exec((new StringBuilder()).append("rundll32 url.dll,FileProtocolHandler ").append(s).toString());
				else {
					String as[] = {"firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape"};
					String s2 = null;
					for (int i = 0; i < as.length && s2 == null; i++)
						if (Runtime.getRuntime().exec(new String[] {"which", as[i]}).waitFor() == 0)
							s2 = as[i];

					if (s2 == null)
						return;
					Runtime.getRuntime().exec(new String[] {s2, s});
				}
			} catch(Exception exception) {}
		}	
	
	private boolean handleCommand(String s) {
		int firstSpace = s.indexOf(" ");
		String cmd = s;
		String[] args = new String[0];
		if (firstSpace != -1) {
			cmd = s.substring(0, firstSpace).trim();
			args = s.substring(firstSpace + 1).trim().split(" ");
		}
      	if (cmd.equals("noloot") || cmd.equals("loot") || cmd.equals("groundloot") || cmd.equals("flooritems") || cmd.equals("grounditems")) {
			showLoot = !showLoot;
			displayMessage("@gre@RSCE:@whi@ Groud loot is now " + (showLoot ? "displayed" : "hidden"), 3, 0);
		} else if (cmd.equals("site") || cmd.equals("website") || cmd.equals("rsce") || cmd.equals("rscemulation") || cmd.equals("webpage"))
			launchURL(SITE_URL);		
		else if (cmd.equals("forum") || cmd.equals("forums"))
			launchURL(SITE_URL + "/forum");		
		else if (cmd.equals("report"))
			launchURL(SITE_URL + "/forum/viewforum.php?id=44");		
		else if (cmd.equals("appeal"))
			launchURL(SITE_URL + "/forum/viewforum.php?id=17");
		else if (cmd.equals("subscribe") || cmd.equals("sub"))
			launchURL(SITE_URL + "/subscribe.html");
		else if (cmd.equals("irc"))
			launchURL(SITE_URL + "/irc.html");
		else if (cmd.equals("rules"))
			launchURL(SITE_URL + "/rules.html");
		else if (cmd.equals("about") || cmd.equals("faq") || cmd.equals("help"))
			launchURL(SITE_URL + "/about.html");
		else if (cmd.equals("calculators") || cmd.equals("calcs") || cmd.equals("calculator") || cmd.equals("calc"))
			launchURL(SITE_URL + "/calculators.html");
		else if (cmd.equals("ventrilo") || cmd.equals("vent"))
			launchURL(SITE_URL + "/ventrilo.html");
		else if (cmd.equals("client") || cmd.equals("play"))
			launchURL("http://play.rscemulation.net");
		else if (cmd.equals("highscores") || cmd.equals("hiscores") || cmd.equals("highscores") || cmd.equals("hiscore"))
			launchURL(SITE_URL + "/highscores.html");		
		else if (cmd.equals("vote"))
			launchURL("http://www.top100arena.com/in.asp?id=42646&incentive=" + ownerID);
		else if (cmd.equals("commands") || cmd.equals("command"))
			showCommandsWindow = 1;
		else if (cmd.equals("dir") || cmd.equals("folder") || cmd.equals("install") || cmd.equals("location"))
			if (RSCE_DIR.equals(""))
				displayMessage("@gre@RSCE:@whi@ RSCEmulation is currently running in external mode", 3, 0);
			else
				displayMessage("@gre@RSCE:@whi@ RSCEmulation is installed to: " + RSCE_DIR, 3, 0);
		else if (cmd.equals("config") || cmd.equals("conf") || cmd.equals("loadconfig") || cmd.equals("loadconf") || cmd.equals("resetconfig") || cmd.equals("resetconf")) {
			loadConf();
			displayMessage("@gre@RSCE:@whi@ Configuration file has been refreshed", 3, 0);
		} else if (cmd.equals("menu")) {
			setProp("ADVANCED_MENU", ADVANCED_MENU ? "OFF" : "ON");
			ADVANCED_MENU = !ADVANCED_MENU;
		} else if (cmd.equals("filter") || cmd.equals("chatfilter")) {
			setProp("CHAT_FILTER", chatFilter ? "OFF" : "ON");
			chatFilter = !chatFilter;
		} else if (cmd.equals("fog")) {
			setProp("FOG", fog ? "OFF" : "ON");
			fog = !fog;
		} else if (cmd.equals("ceiling") || cmd.equals("ceilings") || cmd.equals("roof") || cmd.equals("roofs")) {
			hideCeilings = !hideCeilings;
			super.streamClass.createPacket(15);
			super.streamClass.addByte(hideCeilings ? 3 : 8);
			super.streamClass.formatPacket();
		}
      	return false;
	}
	
	private static boolean setProp(String key, String value) {
        try {
			Properties props = new Properties();
			props.load(new FileInputStream(RSCE_DIR + "RSCEmulation.conf"));
			props.setProperty(key, value);

			OutputStream propOut = new FileOutputStream(new File(RSCE_DIR + "RSCEmulation.conf"));
			props.store(propOut, "RSCEmulation Configuration");
			props.clear();
		} catch (Exception ex) {
			return false;
		}
		
		return true;
	}
	
	private static String timeSince(long time) {
		int seconds = (int)((System.currentTimeMillis() - time) / 1000);
		int minutes = (int)(seconds / 60);
		int hours = (int)(minutes / 60);
		int days = (int)(hours / 24);
		
		if ((minutes % 60) < 1 && (hours % 24) < 1 && days < 1)
			return null;
		else
			if (days > 0)
				if ((hours % 24) > 0)
					return days + " Day" + ((days == 1) ? "" : "s") + ", " + (hours % 24) + " Hour" + (((hours % 24) == 1) ? "" : "s");
				else
					return days + " Day" + ((days == 1) ? "" : "s") + ", " + (minutes % 60) + " Minute" + (((minutes % 60) == 1) ? "" : "s");
			else
				if ((hours % 24) > 0)
					return (hours % 24) + " Hour" + (((hours % 24) == 1) ? "" : "s") + ", " + (minutes % 60) + " Minute" + (((minutes % 60) == 1) ? "" : "s");
				else
					return (minutes % 60) + " Minute" + (((minutes % 60) == 1) ? "" : "s");
	}
	
	private BufferedImage getImage() throws IOException {
		BufferedImage bufferedImage = new BufferedImage(windowWidth, windowHeight + 11, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = bufferedImage.createGraphics();
		g2d.drawImage(gameGraphics.image, 0, 0, this);
		g2d.dispose();
		return bufferedImage;
	}
	
	private File getEmptyFile(boolean movie) throws IOException {
		String charName = DataOperations.longToString(DataOperations.stringLength12ToLong(currentUser));
		File file = new File(MEDIA_DIR + charName);	
		try {
			if (file.exists() || !file.isDirectory())
				file.mkdirs();
			String folder = file.getPath() + System.getProperty("file.separator");
			file = null;
			for (int suffix = 0; file == null || file.exists(); suffix++)
				file = movie ? new File(folder + "movie" + suffix + ".mov") : new File(folder + "screenshot" + suffix + ".png");
		} catch(Exception ex) {
			System.out.println(ex.getStackTrace());
		}
		return file;		
	}
	
	private boolean takeScreenshot(boolean verbose) {
		try {
			File file = getEmptyFile(false);
			ImageIO.write(getImage(), "png", file);
			if (verbose)
				handleServerMessage("@gre@RSCE:@whi@ Screenshot saved as:@gre@ " + file.getName());
			return true;
		} catch(IOException e) {
			if (verbose)
				handleServerMessage("@gre@RSCE:@whi@ Error saving screenshot");
			return false;
		}
	}

    public final void drawMob(int i, int j, int k, int l, int i1, int j1, int k1) {
    	if(loggedIn == 0)
    		return;
        Mob mob = npcArray[i1];
        int l1 = mob.currentSprite + (cameraRotation + 16) / 32 & 7;
        boolean flag = false;
        int i2 = l1;
        if (i2 == 5) {
            i2 = 3;
            flag = true;
        } else if (i2 == 6) {
            i2 = 2;
            flag = true;
        } else if (i2 == 7) {
            i2 = 1;
            flag = true;
        }
        int j2 = i2 * 3 + walkModel[(mob.stepCount / EntityHandler.getNpcDef(mob.type).getWalkModel()) % 4];
        if (mob.currentSprite == 8) {
            i2 = 5;
            l1 = 2;
            flag = false;
            i -= (EntityHandler.getNpcDef(mob.type).getCombatSprite() * k1) / 100;
            j2 = i2 * 3 + npcCombatModelArray1[(loginTimer / (EntityHandler.getNpcDef(mob.type).getCombatModel() - 1)) % 8];
        } else if (mob.currentSprite == 9) {
            i2 = 5;
            l1 = 2;
            flag = true;
            i += (EntityHandler.getNpcDef(mob.type).getCombatSprite() * k1) / 100;
            j2 = i2 * 3 + npcCombatModelArray2[(loginTimer / EntityHandler.getNpcDef(mob.type).getCombatModel()) % 8];
        }
        for (int k2 = 0; k2 < 12; k2++) {
            int l2 = npcAnimationArray[l1][k2];
            int k3 = EntityHandler.getNpcDef(mob.type).getSprite(l2);
            if (k3 >= 0) {
                int i4 = 0;
                int j4 = 0;
                int k4 = j2;
                if (flag && i2 >= 1 && i2 <= 3 && EntityHandler.getAnimationDef(k3).hasF())
                    k4 += 15;
                if (i2 != 5 || EntityHandler.getAnimationDef(k3).hasA()) {
                    int l4 = k4 + EntityHandler.getAnimationDef(k3).getNumber();
                    i4 = (i4 * k) / ((GameImage) (gameGraphics)).sprites[l4].getSomething1();
                    j4 = (j4 * l) / ((GameImage) (gameGraphics)).sprites[l4].getSomething2();
                    int i5 = (k * ((GameImage) (gameGraphics)).sprites[l4].getSomething1()) / ((GameImage) (gameGraphics)).sprites[EntityHandler.getAnimationDef(k3).getNumber()].getSomething1();
                    i4 -= (i5 - k) / 2;
                    int colour = EntityHandler.getAnimationDef(k3).getCharColour();
                    int skinColour = 0;
                    if (colour == 1) {
                        colour = EntityHandler.getNpcDef(mob.type).getHairColour();
                        skinColour = EntityHandler.getNpcDef(mob.type).getSkinColour();
                    } else if (colour == 2) {
                        colour = EntityHandler.getNpcDef(mob.type).getTopColour();
                        skinColour = EntityHandler.getNpcDef(mob.type).getSkinColour();
                    } else if (colour == 3) {
                        colour = EntityHandler.getNpcDef(mob.type).getBottomColour();
                        skinColour = EntityHandler.getNpcDef(mob.type).getSkinColour();
                    }
                    gameGraphics.spriteClip4(i + i4, j + j4, i5, l, l4, colour, skinColour, j1, flag);
                }
            }
        }

        if (mob.lastMessageTimeout > 0) {
            mobMessagesWidth[mobMessageCount] = gameGraphics.textWidth(mob.lastMessage, 1) / 2;
            if (mobMessagesWidth[mobMessageCount] > 150)
                mobMessagesWidth[mobMessageCount] = 150;
            mobMessagesHeight[mobMessageCount] = (gameGraphics.textWidth(mob.lastMessage, 1) / 300) * gameGraphics.messageFontHeight(1);
            mobMessagesX[mobMessageCount] = i + k / 2;
            mobMessagesY[mobMessageCount] = j;
            mobMessages[mobMessageCount++] = mob.lastMessage;
        }
        if (mob.currentSprite == 8 || mob.currentSprite == 9 || mob.combatTimer != 0) {
            if (mob.combatTimer > 0) {
                int i3 = i;
                if (mob.currentSprite == 8)
                    i3 -= (20 * k1) / 100;
                else if (mob.currentSprite == 9)
                    i3 += (20 * k1) / 100;
                int l3 = (mob.hitPointsCurrent * 30) / mob.hitPointsBase;
                anIntArray786[anInt718] = i3 + k / 2;
                anIntArray787[anInt718] = j;
                anIntArray788[anInt718++] = l3;
            }
            if (mob.combatTimer > 150) {
                int j3 = i;
                if (mob.currentSprite == 8)
                    j3 -= (10 * k1) / 100;
                else if (mob.currentSprite == 9)
                    j3 += (10 * k1) / 100;
                gameGraphics.drawPicture((j3 + k / 2) - 12, (j + l / 2) - 12, SPRITE_MEDIA_START + 12);
                drawText(String.valueOf(mob.anInt164), (j3 + k / 2) - 1, j + l / 2 + 5, 3, 0xffffff);
            }
        }
    }
	
    private final void drawCharacterLookScreen() {
        characterDesignMenu.updateActions(super.mouseX, super.mouseY, super.lastMouseDownButton, super.mouseDownButton);
        if (characterDesignMenu.hasActivated(characterDesignHeadButton1))
            do
                characterHeadType = ((characterHeadType - 1) + EntityHandler.animationCount()) % EntityHandler.animationCount();
            while ((EntityHandler.getAnimationDef(characterHeadType).getGenderModel() & 3) != 1 || (EntityHandler.getAnimationDef(characterHeadType).getGenderModel() & 4 * characterHeadGender) == 0);
        if (characterDesignMenu.hasActivated(characterDesignHeadButton2))
            do characterHeadType = (characterHeadType + 1) % EntityHandler.animationCount();
            while ((EntityHandler.getAnimationDef(characterHeadType).getGenderModel() & 3) != 1 || (EntityHandler.getAnimationDef(characterHeadType).getGenderModel() & 4 * characterHeadGender) == 0);
        if (characterDesignMenu.hasActivated(characterDesignHairColourButton1))
            characterHairColour = ((characterHairColour - 1) + characterHairColours.length) % characterHairColours.length;
        if (characterDesignMenu.hasActivated(characterDesignHairColourButton2))
            characterHairColour = (characterHairColour + 1) % characterHairColours.length;
        if (characterDesignMenu.hasActivated(characterDesignGenderButton1) || characterDesignMenu.hasActivated(characterDesignGenderButton2)) {
            for (characterHeadGender = 3 - characterHeadGender; (EntityHandler.getAnimationDef(characterHeadType).getGenderModel() & 3) != 1 || (EntityHandler.getAnimationDef(characterHeadType).getGenderModel() & 4 * characterHeadGender) == 0; characterHeadType = (characterHeadType + 1) % EntityHandler.animationCount());
            for (; (EntityHandler.getAnimationDef(characterBodyGender).getGenderModel() & 3) != 2 || (EntityHandler.getAnimationDef(characterBodyGender).getGenderModel() & 4 * characterHeadGender) == 0; characterBodyGender = (characterBodyGender + 1) % EntityHandler.animationCount());
        }
        if (characterDesignMenu.hasActivated(characterDesignTopColourButton1))
            characterTopColour = ((characterTopColour - 1) + characterTopBottomColours.length) % characterTopBottomColours.length;
        if (characterDesignMenu.hasActivated(characterDesignTopColourButton2))
            characterTopColour = (characterTopColour + 1) % characterTopBottomColours.length;
        if (characterDesignMenu.hasActivated(characterDesignSkinColourButton1))
            characterSkinColour = ((characterSkinColour - 1) + characterSkinColours.length) % characterSkinColours.length;
        if (characterDesignMenu.hasActivated(characterDesignSkinColourButton2))
            characterSkinColour = (characterSkinColour + 1) % characterSkinColours.length;
        if (characterDesignMenu.hasActivated(characterDesignBottomColourButton1))
            characterBottomColour = ((characterBottomColour - 1) + characterTopBottomColours.length) % characterTopBottomColours.length;
        if (characterDesignMenu.hasActivated(characterDesignBottomColourButton2))
            characterBottomColour = (characterBottomColour + 1) % characterTopBottomColours.length;
		if (characterDesignMenu.hasActivated(characterDesignAcceptButton)) {
			super.streamClass.createPacket(13);
			super.streamClass.addByte(characterHeadGender);
			super.streamClass.addByte(characterHeadType);
			super.streamClass.addByte(characterBodyGender);
			super.streamClass.addByte(character2Colour);
			super.streamClass.addByte(characterHairColour);
			super.streamClass.addByte(characterTopColour);
			super.streamClass.addByte(characterBottomColour);
			super.streamClass.addByte(characterSkinColour);
			super.streamClass.formatPacket();
			gameGraphics.method211();
			showCharacterLookScreen = false;
		}
	}

	private final int inventoryCount(int reqID) {
		int amount = 0;
		for (int index = 0; index < inventoryCount; index++)
			if (inventoryItems[index] == reqID)
				if (!EntityHandler.getItemDef(reqID).isStackable())
					amount++;
				else
					amount += inventoryItemsCount[index];
		return amount;
	}

	private final void updateLoginScreen() {
		if (super.socketTimeout > 0)
			super.socketTimeout--;
		if (loginScreenNumber == 0) {
			menuWelcome.updateActions(super.mouseX, super.mouseY, super.lastMouseDownButton, super.mouseDownButton);
			if (menuWelcome.hasActivated(loginButtonNewUser))
				loginScreenNumber = 1;
			if (menuWelcome.hasActivated(loginButtonExistingUser)) {
				loginScreenNumber = 2;
				menuLogin.updateText(loginStatusText, "Please enter your username and password");
				menuLogin.updateText(loginUsernameTextBox, currentUser);
				menuLogin.updateText(loginPasswordTextBox, currentPass);
				menuLogin.setFocus(loginUsernameTextBox);
				return;
			}
		} else if (loginScreenNumber == 1) {
			menuNewUser.updateActions(super.mouseX, super.mouseY, super.lastMouseDownButton, super.mouseDownButton);
			if (menuNewUser.hasActivated(newUserOkButton)) {
				loginScreenNumber = 0;
				return;
			}
		} else if (loginScreenNumber == 2) {
			menuLogin.updateActions(super.mouseX, super.mouseY, super.lastMouseDownButton, super.mouseDownButton);
			if (menuLogin.hasActivated(loginCancelButton))
				loginScreenNumber = 0;
			if (menuLogin.hasActivated(loginUsernameTextBox))
				menuLogin.setFocus(loginPasswordTextBox);
			if (menuLogin.hasActivated(loginPasswordTextBox) || menuLogin.hasActivated(loginOkButton)) {
				currentUser = menuLogin.getText(loginUsernameTextBox);
				currentPass = menuLogin.getText(loginPasswordTextBox);
				login(currentUser, currentPass, false);
			}
		}
	}
	private int justTesting = 9200;
	private int justTesting2 = 8800;
	private int cameraRotation1 = 0;
	private int verticalCamera = 940;

	private void drawLoadingScreen() {
		cameraRotation1++;
		gameGraphics.removeAllPixels();
		gameCamera.setCamera(justTesting, -engineHandle.getAveragedElevation(justTesting, justTesting2), justTesting2, verticalCamera, cameraRotation1, 0, cameraHeight * 4);
		gameCamera.finishCamera();
		gameGraphics.drawPicture((windowWidth / 2 - 241), (windowHeight / 2) - 127, SPRITE_MEDIA_START + 10);
	}

    private final void drawLoginScreen() {
        hasReceivedWelcomeBoxDetails = false;
        gameGraphics.f1Toggle = false;
        gameGraphics.method211();
        if (loginScreenNumber == 0 || loginScreenNumber == 1 || loginScreenNumber == 2 || loginScreenNumber == 3) {
			gameGraphics.drawPicture(0, 10, SPRITE_TEXTURE_START);
			drawLoadingScreen();
			
        }
        if (loginScreenNumber == 0)
            menuWelcome.drawMenu();
        if (loginScreenNumber == 1)
            menuNewUser.drawMenu();
        if (loginScreenNumber == 2)
            menuLogin.drawMenu();
        gameGraphics.drawPicture(0, windowHeight, SPRITE_MEDIA_START + 22);
		gameGraphics.drawPicture(512, windowHeight, SPRITE_MEDIA_START + 22);
        gameGraphics.drawImage(aGraphics936, 0, 0);
    }

    public byte landscapeLoadingX = 50;
    public byte landscapeLoadingY = 50;
    
	private final void setupLoginScreenCamera() {
		int i = 0;
		engineHandle.method401(landscapeLoadingX * 48 + 23, landscapeLoadingY * 48 + 23, i);
		engineHandle.method428(gameDataModels);
		gameCamera.zoom1 = (gameWidth * 9);
		gameCamera.zoom2 = (gameWidth * 9);
		gameCamera.zoom3 = 1;
		gameCamera.zoom4 = (gameWidth * 9);
		gameCamera.setCamera(justTesting, -engineHandle.getAveragedElevation(justTesting, justTesting2), justTesting2, 912, cameraRotation1, 0, cameraHeight * 2);
		gameCamera.finishCamera();
		gameGraphics.fadePixels();
		gameGraphics.fadePixels();
		gameGraphics.drawBox(0, windowHeight, windowWidth, 0, 0);
		//gameGraphics.drawPicture(15, 15, SPRITE_MEDIA_START + 10);
		//gameGraphics.storeSpriteVert(SPRITE_TEXTURE_START, 0, 0, windowWidth, windowHeight);
	}

	private final void drawCommandsWindow() {
		int i = 320;
		int s = 490;
		int j = 30;
        gameGraphics.drawBox((gameWidth / 2) - (s / 2), (gameHeight / 2) - (i / 2), s, i, 0);
        gameGraphics.drawBoxEdge((gameWidth / 2) - (s / 2), (gameHeight / 2) - (i / 2), s, i, 0xffffff);
        drawText("@gre@RSCEmulation Command List", gameWidth / 2, j, 1, 0xffffff);
		j += 15;
		drawText("To use a command enter two colons before the command, such as ::COMMAND", gameWidth / 2, j, 1, 0xffffff);
        int l = 0xffffff;
		j += 30;
		drawText("@gre@OFFER <ID> <AMOUNT>: @whi@Offer an item when in a trade/duel", gameWidth / 2, j, 1, 0xffffff);
		j += 15;
		drawText("@gre@DEPOSIT <ID> <AMOUNT>: @whi@Deposit an item when in a bank screen", gameWidth / 2, j, 1, 0xffffff);
		j += 15;
		drawText("@gre@WITHDRAW <ID> <AMOUNT>: @whi@Deposit an item when in a bank screen", gameWidth / 2, j, 1, 0xffffff);	
		j += 15;
		drawText("@gre@DEPOSITALL: @whi@Deposit every item when in a bank screen", gameWidth / 2, j, 1, 0xffffff);
		j += 15;
		drawText("@gre@SKULL: @whi@Skull yourself for 20 minutes", gameWidth / 2, j, 1, 0xffffff);
		j += 15;
		drawText("@gre@FATIGUE: @whi@Set your fatigue to 100%", gameWidth / 2, j, 1, 0xffffff);
		j += 15;
		drawText("@gre@STUCK: @whi@Teleport to Lumbridge if you're stuck", gameWidth / 2, j, 1, 0xffffff);
		j += 15;
		drawText("@gre@STAFF: @whi@Display a list of the current online staff", gameWidth / 2, j, 1, 0xffffff);
		j += 15;
		drawText("@gre@NOLOOT: @whi@Hide ground items visually", gameWidth / 2, j, 1, 0xffffff);
		j += 15;
		drawText("@gre@ONLINE: @whi@Display the amount of players online", gameWidth / 2, j, 1, 0xffffff);
		j += 15;
		drawText("@gre@WILDERNESS: @whi@Display wilderness state and player list", gameWidth / 2, j, 1, 0xffffff);
		j += 15;
		drawText("@gre@SAY <MESSAGE>: @whi@Announce a message over Global Chat", gameWidth / 2, j, 1, 0xffffff);
		j += 15;
		drawText("@gre@EVENT: @whi@Join the running event", gameWidth / 2, j, 1, 0xffffff);
		j += 15;		
		drawText("@gre@SITE: @whi@Quickly load RSCEmulation's website", gameWidth / 2, j, 1, 0xffffff);
		j += 15;		
		drawText("@gre@VOTE: @whi@Vote for RSCEmulation on the RuneScape Top 100", gameWidth / 2, j, 1, 0xffffff);		
		j += 30;		
        if (super.mouseY > j - 12 && super.mouseY <= j && super.mouseX > gameWidth / 2 - 100 && super.mouseX < gameWidth + 100) // WAS 106, 406
            l = 0xff0000;
        drawText("Click here to close window", gameWidth / 2, j, 1, l); // WAS 256
        if (mouseButtonClick == 1) {
            if (l == 0xff0000)
				showCommandsWindow = 0;
            if ((super.mouseX < gameWidth / 2 - 200 || super.mouseX > gameWidth / 2 + 200) || (super.mouseY < gameHeight / 2 - i / 2 || super.mouseY > gameHeight / 2 + i / 2))
				showCommandsWindow = 0;
        }
        mouseButtonClick = 0;
	}
	
	private final void drawAbuseWindow1() {
		int i = 80;
		int j = (gameHeight / 2) - 18;
        gameGraphics.drawBox(gameWidth / 2 - 200, gameHeight / 2 - i / 2, 400, i, 0);
        gameGraphics.drawBoxEdge(gameWidth / 2 - 200, gameHeight / 2 - i / 2, 400, i, 0xffffff);
        drawText("This feature is currently unavailable.", gameWidth / 2, j, 1, 0xffffff);
		j += 15;
		drawText("Please report players in the \"Report Player\" forum section.", gameWidth / 2, j, 1, 0xffffff);
        int l = 0xffffff;
		j += 30;
        if (super.mouseY > j - 12 && super.mouseY <= j && super.mouseX > gameWidth / 2 - 100 && super.mouseX < gameWidth + 100) // WAS 106, 406
            l = 0xff0000;
        drawText("Click here to close window", gameWidth / 2, j, 1, l); // WAS 256
        if (mouseButtonClick == 1) {
            if (l == 0xff0000)
				showAbuseWindow = 0;
            if ((super.mouseX < gameWidth / 2 - 200 || super.mouseX > gameWidth / 2 + 200) || (super.mouseY < gameHeight / 2 - i / 2 || super.mouseY > gameHeight / 2 + i / 2))
				showAbuseWindow = 0;
        }
        mouseButtonClick = 0;
	}
	
	private final void autoRotateCamera() {
		if ((cameraAutoAngle & 1) == 1 && enginePlayerVisible(cameraAutoAngle))
			return;
		if ((cameraAutoAngle & 1) == 0 && enginePlayerVisible(cameraAutoAngle)) {
			if (enginePlayerVisible(cameraAutoAngle + 1 & 7)) {
				cameraAutoAngle = cameraAutoAngle + 1 & 7;
				return;
			}
			if (enginePlayerVisible(cameraAutoAngle + 7 & 7))
				cameraAutoAngle = cameraAutoAngle + 7 & 7;
			return;
		}
		int ai[] = {1, -1, 2, -2, 3, -3, 4};
		for (int i = 0; i < 7; i++) {
			if (!enginePlayerVisible(cameraAutoAngle + ai[i] + 8 & 7))
				continue;
			cameraAutoAngle = cameraAutoAngle + ai[i] + 8 & 7;
			break;
		}
		if ((cameraAutoAngle & 1) == 0 && enginePlayerVisible(cameraAutoAngle)) {
			if (enginePlayerVisible(cameraAutoAngle + 1 & 7)) {
				cameraAutoAngle = cameraAutoAngle + 1 & 7;
				return;
			}
			if (enginePlayerVisible(cameraAutoAngle + 7 & 7))
				cameraAutoAngle = cameraAutoAngle + 7 & 7;
		}
	}

	public final Graphics getGraphics() {
		if (GameWindow.gameFrame != null)
			return GameWindow.gameFrame.getGraphics();
		return super.getGraphics();
	}

    final void drawPlayer(int i, int j, int k, int l, int i1, int j1, int k1) {
    	if(loggedIn == 0)
    		return;
        Mob mob = playerArray[i1];
        if (mob.colourBottomType == 255)
            return;
        int l1 = mob.currentSprite + (cameraRotation + 16) / 32 & 7;
        boolean flag = false;
        int i2 = l1;
        if (i2 == 5) {
            i2 = 3;
            flag = true;
        } else if (i2 == 6) {
            i2 = 2;
            flag = true;
        } else if (i2 == 7) {
            i2 = 1;
            flag = true;
        }
        int j2 = i2 * 3 + walkModel[(mob.stepCount / 6) % 4];
        if (mob.currentSprite == 8) {
            i2 = 5;
            l1 = 2;
            flag = false;
            i -= (5 * k1) / 100;
            j2 = i2 * 3 + npcCombatModelArray1[(loginTimer / 5) % 8];
        } else if (mob.currentSprite == 9) {
            i2 = 5;
            l1 = 2;
            flag = true;
            i += (5 * k1) / 100;
            j2 = i2 * 3 + npcCombatModelArray2[(loginTimer / 6) % 8];
        }
        for (int k2 = 0; k2 < 12; k2++) {
            int l2 = npcAnimationArray[l1][k2];
            int l3 = mob.animationCount[l2] - 1;
            if (l3 >= 0) {
                int k4 = 0;
                int i5 = 0;
                int j5 = j2;
                if (flag && i2 >= 1 && i2 <= 3)
                    if (EntityHandler.getAnimationDef(l3).hasF())
                        j5 += 15;
                    else if (l2 == 4 && i2 == 1) {
                        k4 = -22;
                        i5 = -3;
                        j5 = i2 * 3 + walkModel[(2 + mob.stepCount / 6) % 4];
                    } else if (l2 == 4 && i2 == 2) {
                        k4 = 0;
                        i5 = -8;
                        j5 = i2 * 3 + walkModel[(2 + mob.stepCount / 6) % 4];
                    } else if (l2 == 4 && i2 == 3) {
                        k4 = 26;
                        i5 = -5;
                        j5 = i2 * 3 + walkModel[(2 + mob.stepCount / 6) % 4];
                    } else if (l2 == 3 && i2 == 1) {
                        k4 = 22;
                        i5 = 3;
                        j5 = i2 * 3 + walkModel[(2 + mob.stepCount / 6) % 4];
                    } else if (l2 == 3 && i2 == 2) {
                        k4 = 0;
                        i5 = 8;
                        j5 = i2 * 3 + walkModel[(2 + mob.stepCount / 6) % 4];
                    } else if (l2 == 3 && i2 == 3) {
                        k4 = -26;
                        i5 = 5;
                        j5 = i2 * 3 + walkModel[(2 + mob.stepCount / 6) % 4];
                    }
                if (i2 != 5 || EntityHandler.getAnimationDef(l3).hasA()) {
                    int k5 = j5 + EntityHandler.getAnimationDef(l3).getNumber();
                    k4 = (k4 * k) / ((GameImage) (gameGraphics)).sprites[k5].getSomething1();
                    i5 = (i5 * l) / ((GameImage) (gameGraphics)).sprites[k5].getSomething2();
                    int l5 = (k * ((GameImage) (gameGraphics)).sprites[k5].getSomething1()) / ((GameImage) (gameGraphics)).sprites[EntityHandler.getAnimationDef(l3).getNumber()].getSomething1();
                    k4 -= (l5 - k) / 2;
                    int colour = EntityHandler.getAnimationDef(l3).getCharColour();
					if (mob.animationCount[l2] == 246 || mob.animationCount[l2] == 245) // Flashing Cape and Party Hat
						colour = (int)(Math.random() * 167772150);
                    int skinColour = characterSkinColours[mob.colourSkinType];
                    if (colour == 1)
                        colour = characterHairColours[mob.colourHairType];
                    else if (colour == 2)
                        colour = characterTopBottomColours[mob.colourTopType];
                    else if (colour == 3)
                        colour = characterTopBottomColours[mob.colourBottomType];
                    gameGraphics.spriteClip4(i + k4, j + i5, l5, l, k5, colour, skinColour, j1, flag);
                }
            }
        }

        if (mob.lastMessageTimeout > 0) {
            mobMessagesWidth[mobMessageCount] = gameGraphics.textWidth(mob.lastMessage, 1) / 2;
            if (mobMessagesWidth[mobMessageCount] > 150)
                mobMessagesWidth[mobMessageCount] = 150;
            mobMessagesHeight[mobMessageCount] = (gameGraphics.textWidth(mob.lastMessage, 1) / 300) * gameGraphics.messageFontHeight(1);
            mobMessagesX[mobMessageCount] = i + k / 2;
            mobMessagesY[mobMessageCount] = j;
            mobMessages[mobMessageCount++] = mob.lastMessage;
        }
        if (mob.anInt163 > 0) {
            anIntArray858[anInt699] = i + k / 2;
            anIntArray859[anInt699] = j;
            anIntArray705[anInt699] = k1;
            anIntArray706[anInt699++] = mob.anInt162;
        }
        if (mob.currentSprite == 8 || mob.currentSprite == 9 || mob.combatTimer != 0) {
            if (mob.combatTimer > 0) {
                int i3 = i;
                if (mob.currentSprite == 8)
                    i3 -= (20 * k1) / 100;
                else if (mob.currentSprite == 9)
                    i3 += (20 * k1) / 100;
                int i4 = (mob.hitPointsCurrent * 30) / mob.hitPointsBase;
                anIntArray786[anInt718] = i3 + k / 2;
                anIntArray787[anInt718] = j;
                anIntArray788[anInt718++] = i4;
            }
            if (mob.combatTimer > 150) {
                int j3 = i;
                if (mob.currentSprite == 8)
                    j3 -= (10 * k1) / 100;
                else if (mob.currentSprite == 9)
                    j3 += (10 * k1) / 100;
                gameGraphics.drawPicture((j3 + k / 2) - 12, (j + l / 2) - 12, SPRITE_MEDIA_START + 11);
                drawText(String.valueOf(mob.anInt164), (j3 + k / 2) - 1, j + l / 2 + 5, 3, 0xffffff);
            }
        }
        if (mob.anInt179 == 1 && mob.anInt163 == 0) {
            int k3 = j1 + i + k / 2;
            if (mob.currentSprite == 8)
                k3 -= (20 * k1) / 100;
            else if (mob.currentSprite == 9)
                k3 += (20 * k1) / 100;
            int j4 = (16 * k1) / 100;
            int l4 = (16 * k1) / 100;
            gameGraphics.spriteClip1(k3 - j4 / 2, j - l4 / 2 - (10 * k1) / 100, j4, l4, SPRITE_MEDIA_START + 13);
        }
    }
	
	private final void loadConfigFilter() {
		drawLoadingBarText(15, "Unpacking Configuration...");
		EntityHandler.load();
	}

	private final void loadModels() {
		drawLoadingBarText(75, "Unpacking landscape - 0%");
		String[] modelNames = {"torcha2", "torcha3", "torcha4", "skulltorcha2", "skulltorcha3", "skulltorcha4", "firea2", "firea3", "fireplacea2", "fireplacea3", "firespell2", "firespell3", "lightning2", "lightning3", "clawspell2", "clawspell3", "clawspell4", "clawspell5", "spellcharge2", "spellcharge3"};
		for(String name : modelNames)
			EntityHandler.storeModel(name);
		byte[] models = load("models");
		if (models == null) {
			lastLoadedNull = true;
			return;
		}
		for (int j = 0;j < EntityHandler.getModelCount();j++) {
			int k = DataOperations.method358(EntityHandler.getModelName(j) + ".ob3", models);
			if (k == 0)
				gameDataModels[j] = new Model(1, 1);
			else
				gameDataModels[j] = new Model(models, k, true);
			gameDataModels[j].isGiantCrystal = EntityHandler.getModelName(j).equals("giantcrystal");
		}
	}

    protected final void handleMouseDown(int button, int x, int y) {
        mouseClickXArray[mouseClickArrayOffset] = x;
        mouseClickYArray[mouseClickArrayOffset] = y;
        mouseClickArrayOffset = mouseClickArrayOffset + 1 & 0x1fff;
        for (int l = 10; l < 4000; l++) {
            int i1 = mouseClickArrayOffset - l & 0x1fff;
            if (mouseClickXArray[i1] == x && mouseClickYArray[i1] == y) {
                boolean flag = false;
                for (int j1 = 1; j1 < l; j1++) {
                    int k1 = mouseClickArrayOffset - j1 & 0x1fff;
                    int l1 = i1 - j1 & 0x1fff;
                    if (mouseClickXArray[l1] != x || mouseClickYArray[l1] != y)
                        flag = true;
                    if (mouseClickXArray[k1] != mouseClickXArray[l1] || mouseClickYArray[k1] != mouseClickYArray[l1])
                        break;
                    if (j1 == l - 1 && flag && lastWalkTimeout == 0 && logoutTimeout == 0) {
                        logout();
                        return;
                    }
                }

            }
        }
    }

	protected final void loadingError() {
		if (lastLoadedNull) {
			Graphics g = getGraphics();
			g.setColor(Color.black);
			g.fillRect(0, 0, 512, 356);
			g.setFont(new Font("Helvetica", 1, 16));
			g.setColor(Color.yellow);
			int i = 35;
			g.drawString("Sorry, an error has occured whilst loading RSCEmulation:", 30, i);
			i += 50;
			g.setColor(Color.white);
			g.drawString("To fix this try the following (in order):", 30, i);
			i += 50;
			g.setColor(Color.white);
			g.setFont(new Font("Helvetica", 1, 12));
			g.drawString("1: Try closing ALL open web-browser windows, and reloading", 30, i);
			i += 30;
			g.drawString("2: Try clearing your web-browsers cache from tools->internet options", 30, i);
			i += 30;
			g.drawString("3: Try using a different game-world", 30, i);
			i += 30;
			g.drawString("4: Try rebooting your computer", 30, i);
			i += 30;
			g.drawString("5: Try selecting a different version of Java from the play-game menu", 30, i);
			changeThreadSleepModifier(1);
			return;
		}
		if (memoryError) {
			Graphics g2 = getGraphics();
			g2.setColor(Color.black);
			g2.fillRect(0, 0, 512, 356);
			g2.setFont(new Font("Helvetica", 1, 20));
			g2.setColor(Color.white);
			g2.drawString("Error - out of memory!", 50, 50);
			g2.drawString("Close ALL unnecessary programs", 50, 100);
			g2.drawString("and windows before loading the game", 50, 150);
			g2.drawString("RSCEmulation needs about 100mb of spare RAM", 50, 200);
			changeThreadSleepModifier(1);
			return;
		}
		try {
			if (loggedIn == 1) {
				gameGraphics.drawStringShadows = true;
				drawGame();
			} else {
				gameGraphics.drawStringShadows = false;
				drawLoginScreen();
			}
		} catch (OutOfMemoryError e) {
			garbageCollect();
			memoryError = true;
		}
	}

    private final void walkToObject(int x, int y, int id, int type) {
        int i1;
        int j1;
        if (id == 0 || id == 4) {
            i1 = EntityHandler.getObjectDef(type).getWidth();
            j1 = EntityHandler.getObjectDef(type).getHeight();
        } else {
            j1 = EntityHandler.getObjectDef(type).getWidth();
            i1 = EntityHandler.getObjectDef(type).getHeight();
        }
        if (EntityHandler.getObjectDef(type).getType() == 2 || EntityHandler.getObjectDef(type).getType() == 3) {
            if (id == 0) {
                x--;
                i1++;
            }
            if (id == 2)
                j1++;
            if (id == 4)
                i1++;
            if (id == 6) {
                y--;
                j1++;
            }
            sendWalkCommand(sectionX, sectionY, x, y, (x + i1) - 1, (y + j1) - 1, false, true);
            return;
        } else {
            sendWalkCommand(sectionX, sectionY, x, y, (x + i1) - 1, (y + j1) - 1, true, true);
            return;
        }
    }

	private boolean canSendBankWindow = false;
	private final void drawBankBox() {
		char c = '\u0198'; // HEIGHT MODIFIER
		char c1 = '\u014E'; // WIDTH MODIFIER
		if (mouseOverBankPageText > 0 && bankItemCount <= 48)
			mouseOverBankPageText = 0;
		if (mouseOverBankPageText > 1 && bankItemCount <= 96)
			mouseOverBankPageText = 1;
		if (mouseOverBankPageText > 2 && bankItemCount <= 144)
			mouseOverBankPageText = 2;
		if (selectedBankItem >= bankItemCount || selectedBankItem < 0)
			selectedBankItem = -1;
		if (selectedBankItem != -1 && bankItems[selectedBankItem] != selectedBankItemType) {
			selectedBankItem = -1;
			selectedBankItemType = -2;
		}
		if (mouseButtonClick == 1 && tester && !(inputBoxType == 4) && !(inputBoxType == 5)) {
			for (int ix = 0; ix < menuLength; ix++) {
				int k = tradeWindowX + 2;
				int i1 = tradeWindowY + 11 + ix * 15;
				if (super.mouseX <= k - 2 || super.mouseY <= i1 - 12 || super.mouseY >= i1 + 4 || super.mouseX >= (k - 3) + menuWidth)
					continue;
				menuClick(ix);
			}
			tradeWindowX = -100;
			tradeWindowY = -100;
			mouseButtonClick = 0;
			tester = false;
			setValue = false;
		}
		if (mouseButtonClick != 0 && !tester && !(inputBoxType == 4) && !(inputBoxType == 5)) {
			if (mouseButtonClick == 2) {
				int selectedX = super.mouseX - (gameWidth / 2 - c / 2);
				int selectedY = super.mouseY - (gameHeight / 2 - c1 / 2 + 20);
				if (selectedX >= 0 && selectedY >= 12 && selectedX < 408 && selectedY < 280) {
					int i1 = mouseOverBankPageText * 48;
					for (int l1 = 0; l1 < 6; l1++) {
						for (int j2 = 0; j2 < 8; j2++) {
							int l6 = 7 + j2 * 49;
							int j7 = 28 + l1 * 34;
							if (selectedX > l6 && selectedX < l6 + 49 && selectedY > j7 && selectedY < j7 + 34
									&& i1 < bankItemCount && bankItems[i1] != -1) {
								selectedBankItemType = bankItems[i1];
								selectedBankItem = i1;
								canSendBankWindow = true;
							} else if (!(selectedX > l6) && !(selectedX < l6 + 49) && !(selectedY > j7) && !(selectedY < j7 + 34) && !(i1 < bankItemCount) && !(bankItems[i1] != -1))
								canSendBankWindow = false;
							i1++;
						}
					}
					tradeWindowX = super.mouseX;
					tradeWindowY = super.mouseY;

					for (int jx = 0; jx < menuLength; jx++) {
						menuText1[jx] = null;
						menuText2[jx] = null;
						menuActionVariable[jx] = -1;
						menuActionVariable2[jx] = -1;
						menuID[jx] = -1;
					}
					menuLength = 0;
					if (canSendBankWindow && selectedBankItem != -1) {
							String name = EntityHandler.getItemDef(bankItems[selectedBankItem]).getName();
							menuLength = 0;
							if (bankItemsCount[selectedBankItem] > 0) {
								menuText1[menuLength] = "Withdraw All@lre@";
								menuText2[menuLength] = name;
								menuID[menuLength] = 784; //878
								menuActionVariable[menuLength] = bankItems[selectedBankItem];
								menuActionVariable2[menuLength] = bankItemsCount[selectedBankItem];
								menuLength++;
								menuText1[menuLength] = "Withdraw X@lre@";
								menuText2[menuLength] = name;
								menuID[menuLength] = 785; //888
								menuActionVariable[menuLength] = bankItems[selectedBankItem];
								menuLength++;
							}							
							if (inventoryCount(bankItems[selectedBankItem]) > 0) {
								if (bankItemsCount[selectedBankItem] > 0) {
									menuText1[menuLength] = "";
									menuText2[menuLength] = "";
									menuID[menuLength] = 786;
									menuLength++;									
								}
								menuText1[menuLength] = "Deposit All@lre@";
								menuText2[menuLength] = name;
								menuID[menuLength] = 878; //784
								menuActionVariable[menuLength] = bankItems[selectedBankItem];
								menuLength++;
								
								menuText1[menuLength] = "Deposit X@lre@";
								menuText2[menuLength] = name;
								menuID[menuLength] = 888; //785
								menuActionVariable[menuLength] = bankItems[selectedBankItem];
								menuLength++;
							}
							tester = true;
						}
				}
			} else
			mouseButtonClick = 0;
			int i = super.mouseX - (gameWidth / 2 - c / 2); // WAS 256
			int k = super.mouseY - (gameHeight / 2 - c1 / 2 + 20); // WAS 170
			if (i >= 0 && k >= 12 && i < 408 && k < 280) {
				int i1 = mouseOverBankPageText * 48;
				for (int l1 = 0; l1 < 6; l1++) {
					for (int j2 = 0; j2 < 8; j2++) {
						int l6 = 7 + j2 * 49;
						int j7 = 28 + l1 * 34;
						if (i > l6 && i < l6 + 49 && k > j7 && k < j7 + 34 && i1 < bankItemCount && bankItems[i1] != -1) {
							selectedBankItemType = bankItems[i1];
							selectedBankItem = i1;
						}
						i1++;
					}
				}

				i = gameWidth / 2 - c / 2; // WAS 256
				k = gameHeight / 2 - c1 / 2 + 20; //WAS 170
				int k2;
				if (selectedBankItem < 0)
					k2 = -1;
				else
					k2 = bankItems[selectedBankItem];
				if (k2 != -1) {
					int j1 = bankItemsCount[selectedBankItem];
					if (super.mouseX >= i + 220 && super.mouseY >= k + 238 && super.mouseX < i + 250 && super.mouseY <= k + 249) {
						super.streamClass.createPacket(24);
						super.streamClass.add2ByteInt(k2);
						super.streamClass.add4ByteInt(1);
						super.streamClass.formatPacket();
					}
					if (j1 >= 10 && super.mouseX >= i + 250 && super.mouseY >= k + 238 && super.mouseX < i + 280 && super.mouseY <= k + 249) {
						super.streamClass.createPacket(24);
						super.streamClass.add2ByteInt(k2);
						super.streamClass.add4ByteInt(10);
						super.streamClass.formatPacket();
					}
					if (j1 >= 100 && super.mouseX >= i + 280 && super.mouseY >= k + 238 && super.mouseX < i + 305 && super.mouseY <= k + 249) {
						super.streamClass.createPacket(24);
						super.streamClass.add2ByteInt(k2);
						super.streamClass.add4ByteInt(100);
						super.streamClass.formatPacket();
					}
					if (j1 >= 1000 && super.mouseX >= i + 305 && super.mouseY >= k + 238 && super.mouseX < i + 335 && super.mouseY <= k + 249) {
						super.streamClass.createPacket(24);
						super.streamClass.add2ByteInt(k2);
						super.streamClass.add4ByteInt(1000);
						super.streamClass.formatPacket();
					}
					if (j1 >= 10000 && super.mouseX >= i + 335 && super.mouseY >= k + 238 && super.mouseX < i + 368 && super.mouseY <= k + 249) {
						super.streamClass.createPacket(24);
						super.streamClass.add2ByteInt(k2);
						super.streamClass.add4ByteInt(10000);
						super.streamClass.formatPacket();
					}
					if (super.mouseX >= i + 370 && super.mouseY >= k + 238 && super.mouseX < i + 400 && super.mouseY <= k + 249) {
						super.streamClass.createPacket(24);
						super.streamClass.add2ByteInt(k2);
						super.streamClass.add4ByteInt(j1);
						super.streamClass.formatPacket();
					}

					if (inventoryCount(k2) >= 1 && super.mouseX >= i + 220 && super.mouseY >= k + 263 && super.mouseX < i + 250 && super.mouseY <= k + 274) {
						super.streamClass.createPacket(25);
						super.streamClass.add2ByteInt(k2);
						super.streamClass.add4ByteInt(1);
						super.streamClass.formatPacket();
					}
					if (inventoryCount(k2) >= 10 && super.mouseX >= i + 250 && super.mouseY >= k + 263 && super.mouseX < i + 280 && super.mouseY <= k + 274) {
						super.streamClass.createPacket(25);
						super.streamClass.add2ByteInt(k2);
						super.streamClass.add4ByteInt(10);
						super.streamClass.formatPacket();
					}
					if (inventoryCount(k2) >= 100 && super.mouseX >= i + 280 && super.mouseY >= k + 263 && super.mouseX < i + 305 && super.mouseY <= k + 274) {
						super.streamClass.createPacket(25);
						super.streamClass.add2ByteInt(k2);
						super.streamClass.add4ByteInt(100);
						super.streamClass.formatPacket();
					}
					if (inventoryCount(k2) >= 1000 && super.mouseX >= i + 305 && super.mouseY >= k + 263 && super.mouseX < i + 335 && super.mouseY <= k + 274) {
						super.streamClass.createPacket(25);
						super.streamClass.add2ByteInt(k2);
						super.streamClass.add4ByteInt(1000);
						super.streamClass.formatPacket();
					}
					if (inventoryCount(k2) >= 10000 && super.mouseX >= i + 335 && super.mouseY >= k + 263 && super.mouseX < i + 368 && super.mouseY <= k + 274) {
						super.streamClass.createPacket(25);
						super.streamClass.add2ByteInt(k2);
						super.streamClass.add4ByteInt(10000);
						super.streamClass.formatPacket();
					}
					if (super.mouseX >= i + 370 && super.mouseY >= k + 263 && super.mouseX < i + 400 && super.mouseY <= k + 274) {
						super.streamClass.createPacket(25);
						super.streamClass.add2ByteInt(k2);
						super.streamClass.add4ByteInt(inventoryCount(k2));
						super.streamClass.formatPacket();
					}
				}
			} else if (bankItemCount > 48 && i >= 50 && i <= 115 && k <= 12 && super.mouseY > gameHeight / 2 - 146)
				mouseOverBankPageText = 0;
			else if (bankItemCount > 48 && i >= 115 && i <= 180 && k <= 12 && super.mouseY > gameHeight/2 - 146)
				mouseOverBankPageText = 1;
			else if (bankItemCount > 96 && i >= 180 && i <= 245 && k <= 12 && super.mouseY > gameHeight/2 - 146)
				mouseOverBankPageText = 2;
			else if (bankItemCount > 144 && i >= 245 && i <= 310 && k <= 12 && super.mouseY > gameHeight/2 - 146)
				mouseOverBankPageText = 3;
			else {
				super.streamClass.createPacket(26);
				super.streamClass.formatPacket();
				showBank = false;
				return;
			}
		}
		int j = gameWidth / 2 - c / 2; // WAS 256
		int l = gameHeight / 2 - c1 / 2 + 20; //WAS 170
		gameGraphics.drawBox(j, l, 408, 12, 192);
		int k1 = 0x989898;
		drawBoxAlpha(j, l + 12, 408, 17, k1, 160);
		drawBoxAlpha(j, l + 29, 8, 204, k1, 160);
		drawBoxAlpha(j + 399, l + 29, 9, 204, k1, 160);
		drawBoxAlpha(j, l + 233, 408, 47, k1, 160);
		drawString("Bank", j + 1, l + 10, 1, 0xffffff);
		int i2 = 50;
		if (bankItemCount > 48) {
			int l2 = 0xffffff;
			if (mouseOverBankPageText == 0)
				l2 = 0xff0000;
			else if (super.mouseX > j + i2 && super.mouseY >= l && super.mouseX < j + i2 + 65 && super.mouseY < l + 12)
				l2 = 0xffff00;
			drawString("<page 1>", j + i2, l + 10, 1, l2);
			i2 += 65;
			l2 = 0xffffff;
			if (mouseOverBankPageText == 1)
				l2 = 0xff0000;
			else if (super.mouseX > j + i2 && super.mouseY >= l && super.mouseX < j + i2 + 65 && super.mouseY < l + 12)
				l2 = 0xffff00;
			drawString("<page 2>", j + i2, l + 10, 1, l2);
			i2 += 65;
		}
		if (bankItemCount > 96) {
			int i3 = 0xffffff;
			if (mouseOverBankPageText == 2)
				i3 = 0xff0000;
			else if (super.mouseX > j + i2 && super.mouseY >= l && super.mouseX < j + i2 + 65 && super.mouseY < l + 12)
				i3 = 0xffff00;
			drawString("<page 3>", j + i2, l + 10, 1, i3);
			i2 += 65;
		}
		if (bankItemCount > 144) {
			int j3 = 0xffffff;
			if (mouseOverBankPageText == 3)
				j3 = 0xff0000;
			else if (super.mouseX > j + i2 && super.mouseY >= l && super.mouseX < j + i2 + 65 && super.mouseY < l + 12)
				j3 = 0xffff00;
			drawString("<page 4>", j + i2, l + 10, 1, j3);
			i2 += 65;
		}
		int k3 = 0xffffff;
		if (super.mouseX > j + 320 && super.mouseY >= l && super.mouseX < j + 408 && super.mouseY < l + 12)
			k3 = 0xff0000;
		gameGraphics.drawBoxTextRight("Close window", j + 406, l + 10, 1, k3);
		drawString("Number in bank in green", j + 7, l + 24, 1, 65280);
		drawString("Number held in blue", j + 289, l + 24, 1, 65535);
		int i7 = 0xd0d0d0;
		int k7 = mouseOverBankPageText * 48;
		for (int i8 = 0; i8 < 6; i8++) {
			for (int j8 = 0; j8 < 8; j8++) {
				int l8 = j + 7 + j8 * 49;
				int i9 = l + 28 + i8 * 34;
				if (selectedBankItem == k7)
					drawBoxAlpha(l8, i9, 49, 34, 0xff0000, 160);
				else
					drawBoxAlpha(l8, i9, 49, 34, i7, 160);
				gameGraphics.drawBoxEdge(l8, i9, 50, 35, 0);
				if (k7 < bankItemCount && bankItems[k7] != -1) {
					gameGraphics.spriteClip4(l8, i9, 48, 32, SPRITE_ITEM_START + EntityHandler.getItemDef(bankItems[k7]).getSprite(), EntityHandler.getItemDef(bankItems[k7]).getPictureMask(), 0, 0, false);
					drawString(insertCommas(String.valueOf(bankItemsCount[k7])), l8 + 1, i9 + 10, 1, 65280);
					gameGraphics.drawBoxTextRight(insertCommas(String.valueOf(inventoryCount(bankItems[k7]))), l8 + 47, i9 + 29, 1, 65535);
				}
				k7++;
			}

		}

		drawLineX(j + 5, l + 256, 398, 0);
		int k8;
		if (selectedBankItem < 0)
			k8 = -1;
		else
			k8 = bankItems[selectedBankItem];
		if (k8 != -1) {
			int l7 = bankItemsCount[selectedBankItem];
				if (l7 > 0) {
				drawString("Withdraw " + EntityHandler.getItemDef(k8).getName(), j + 2, l + 248, 1, 0xffffff);
				int l3 = 0xffffff;
				if (super.mouseX >= j + 220 && super.mouseY >= l + 238 && super.mouseX < j + 250 && super.mouseY <= l + 249)
					l3 = 0xff0000;
				drawString("One", j + 222, l + 248, 1, l3);
				if (l7 >= 10) {
					int i4 = 0xffffff;
					if (super.mouseX >= j + 250 && super.mouseY >= l + 238 && super.mouseX < j + 280 && super.mouseY <= l + 249)
						i4 = 0xff0000;
					drawString("10", j + 252, l + 248, 1, i4);
				}
				if (l7 >= 100) {
					int j4 = 0xffffff;
					if (super.mouseX >= j + 280 && super.mouseY >= l + 238 && super.mouseX < j + 305 && super.mouseY <= l + 249)
						j4 = 0xff0000;
					drawString("100", j + 282, l + 248, 1, j4);
				}
				if (l7 >= 1000) {
					int k4 = 0xffffff;
					if (super.mouseX >= j + 305 && super.mouseY >= l + 238 && super.mouseX < j + 335 && super.mouseY <= l + 249)
						k4 = 0xff0000;
					drawString("1k", j + 307, l + 248, 1, k4);
				}
				if (l7 >= 10000) {
					int l4 = 0xffffff;
					if (super.mouseX >= j + 335 && super.mouseY >= l + 238 && super.mouseX < j + 368 && super.mouseY <= l + 249)
						l4 = 0xff0000;
					drawString("10k", j + 337, l + 248, 1, l4);
				}
				int i5 = 0xffffff;
				if (super.mouseX >= j + 370 && super.mouseY >= l + 238 && super.mouseX < j + 400 && super.mouseY <= l + 249)
					i5 = 0xff0000;
				drawString("All", j + 370, l + 248, 1, i5);
			}
			if (inventoryCount(k8) > 0) {
				drawString("Deposit " + EntityHandler.getItemDef(k8).getName(), j + 2, l + 273, 1, 0xffffff);
				int j5 = 0xffffff;
				if (super.mouseX >= j + 220 && super.mouseY >= l + 263 && super.mouseX < j + 250 && super.mouseY <= l + 274)
					j5 = 0xff0000;
				drawString("One", j + 222, l + 273, 1, j5);
				if (inventoryCount(k8) >= 10) {
					int k5 = 0xffffff;
					if (super.mouseX >= j + 250 && super.mouseY >= l + 263 && super.mouseX < j + 280 && super.mouseY <= l + 274)
						k5 = 0xff0000;
					drawString("10", j + 252, l + 273, 1, k5);
				}
				if (inventoryCount(k8) >= 100) {
					int l5 = 0xffffff;
					if (super.mouseX >= j + 280 && super.mouseY >= l + 263 && super.mouseX < j + 305 && super.mouseY <= l + 274)
						l5 = 0xff0000;
					drawString("100", j + 282, l + 273, 1, l5);
				}
				if (inventoryCount(k8) >= 1000) {
					int i6 = 0xffffff;
					if (super.mouseX >= j + 305 && super.mouseY >= l + 263 && super.mouseX < j + 335 && super.mouseY <= l + 274)
						i6 = 0xff0000;
					drawString("1k", j + 307, l + 273, 1, i6);
				}
				if (inventoryCount(k8) >= 10000) {
					int j6 = 0xffffff;
					if (super.mouseX >= j + 335 && super.mouseY >= l + 263 && super.mouseX < j + 368 && super.mouseY <= l + 274)
						j6 = 0xff0000;
					drawString("10k", j + 337, l + 273, 1, j6);
				}
				int k6 = 0xffffff;
				if (super.mouseX >= j + 370 && super.mouseY >= l + 263 && super.mouseX < j + 400 && super.mouseY <= l + 274)
					k6 = 0xff0000;
				drawString("All", j + 370, l + 273, 1, k6);
			}
		}
	}

	private final void drawLoggingOutBox() {
		gameGraphics.drawBox(gameWidth / 2 - 130, gameHeight / 2 - 30, 260, 60, 0);
		gameGraphics.drawBoxEdge(gameWidth / 2 - 130, gameHeight / 2 - 30, 260, 60, 0xffffff);
		drawText("Logging out...", gameWidth / 2, gameHeight / 2, 5, 0xffffff);
	}

	private String insertCommas(String str) {  
		if (str.length() < 4)
			return str;  
		return insertCommas(str.substring(0, str.length() - 3)) + "," + str.substring(str.length() - 3, str.length());  
	}

    private final void drawInventoryMenu(boolean flag) {
        int i = ((GameImage) (gameGraphics)).menuDefaultWidth - 248;
        gameGraphics.drawPicture(i, 3, SPRITE_MEDIA_START + 1);
        for (int j = 0; j < anInt882; j++) {
            int k = i + (j % 5) * 49;
            int i1 = 36 + (j / 5) * 34;
            if (j < inventoryCount && wearing[j] == 1)
                drawBoxAlpha(k, i1, 49, 34, 0xff0000, 128);
            else
                drawBoxAlpha(k, i1, 49, 34, GameImage.convertRGBToLong(181, 181, 181), 128);
            if (j < inventoryCount) {
                gameGraphics.spriteClip4(k, i1, 48, 32, SPRITE_ITEM_START + EntityHandler.getItemDef(inventoryItems[j]).getSprite(), EntityHandler.getItemDef(inventoryItems[j]).getPictureMask(), 0, 0, false);
                if (EntityHandler.getItemDef(inventoryItems[j]).isStackable())
                    drawString(insertCommas(String.valueOf(inventoryItemsCount[j])), k + 1, i1 + 10, 1, 0xffff00);
            }
        }

        for (int l = 1; l <= 4; l++)
            drawLineY(i + l * 49, 36, (anInt882 / 5) * 34, 0);

        for (int j1 = 1; j1 <= anInt882 / 5 - 1; j1++)
            drawLineX(i, 36 + j1 * 34, 245, 0);

        if (!flag)
            return;
        i = super.mouseX - (((GameImage) (gameGraphics)).menuDefaultWidth - 248);
        int k1 = super.mouseY - 36;
        if (i >= 0 && k1 >= 0 && i < 248 && k1 < (anInt882 / 5) * 34) {
            int currentInventorySlot = i / 49 + (k1 / 34) * 5;
            if (currentInventorySlot < inventoryCount) {
                int i2 = inventoryItems[currentInventorySlot];
                ItemDef itemDef = EntityHandler.getItemDef(i2);
                if (selectedSpell >= 0) {
                    if (EntityHandler.getSpellDef(selectedSpell).getSpellType() == 3) {
                        menuText1[menuLength] = "Cast " + EntityHandler.getSpellDef(selectedSpell).getName() + " on";
                        menuText2[menuLength] = "@lre@" + itemDef.getName();
                        menuID[menuLength] = 600;
                        menuActionType[menuLength] = currentInventorySlot;
                        menuActionVariable[menuLength] = selectedSpell;
                        menuLength++;
                        return;
                    }
                } else {
                    if (selectedItem >= 0) {
                        menuText1[menuLength] = "Use " + selectedItemName + " with";
                        menuText2[menuLength] = "@lre@" + itemDef.getName();
                        menuID[menuLength] = 610;
                        menuActionType[menuLength] = currentInventorySlot;
                        menuActionVariable[menuLength] = selectedItem;
                        menuLength++;
                        return;
                    }
                    if (wearing[currentInventorySlot] == 1) {
                        menuText1[menuLength] = "Remove";
                        menuText2[menuLength] = "@lre@" + itemDef.getName();
                        menuID[menuLength] = 620;
                        menuActionType[menuLength] = currentInventorySlot;
                        menuLength++;
                    }
                    else if (EntityHandler.getItemDef(i2).isWieldable()) {
                	menuText1[menuLength] = "Wear";
                        menuText2[menuLength] = "@lre@" + itemDef.getName();
                        menuID[menuLength] = 630;
                        menuActionType[menuLength] = currentInventorySlot;
                        menuLength++;
                    }
                    if (!itemDef.getCommand().equals("")) {
                        menuText1[menuLength] = itemDef.getCommand();
                        menuText2[menuLength] = "@lre@" + itemDef.getName();
                        menuID[menuLength] = 640;
                        menuActionType[menuLength] = currentInventorySlot;
                        menuLength++;
                    }
                    menuText1[menuLength] = "Use";
                    menuText2[menuLength] = "@lre@" + itemDef.getName();
                    menuID[menuLength] = 650;
                    menuActionType[menuLength] = currentInventorySlot;
                    menuLength++;
                    menuText1[menuLength] = "Drop";
                    menuText2[menuLength] = "@lre@" + itemDef.getName();
                    menuID[menuLength] = 660;
                    menuActionType[menuLength] = currentInventorySlot;
                    menuLength++;
                    menuText1[menuLength] = "Examine";
                    menuText2[menuLength] = "@lre@" + itemDef.getName() + ((ourPlayer.admin == 5 || ourPlayer.admin <= 3) ? " @or1@(" + i2 + ")" : "");
                    menuID[menuLength] = 3600;
                    menuActionType[menuLength] = i2;
                    menuLength++;
                }
            }
        }
    }

    private final void drawChatMessageTabs() {
	    gameGraphics.drawPicture(0, windowHeight, SPRITE_MEDIA_START + 22);
		gameGraphics.drawPicture(512, windowHeight, SPRITE_MEDIA_START + 22);
        gameGraphics.drawPicture(windowWidth / 2 - 256, windowHeight - 4, SPRITE_MEDIA_START + 23); //Zorian
        int i = GameImage.convertRGBToLong(200, 200, 255);
        if (messagesTab == 0)
            i = GameImage.convertRGBToLong(255, 200, 50);
        if (anInt952 % 30 > 15)
            i = GameImage.convertRGBToLong(255, 50, 50);
        gameGraphics.drawText("All messages", windowWidth / 2 - 202, windowHeight + 6, 0, i);
        i = GameImage.convertRGBToLong(200, 200, 255);
        if (messagesTab == 1)
            i = GameImage.convertRGBToLong(255, 200, 50);
        if (anInt953 % 30 > 15)
            i = GameImage.convertRGBToLong(255, 50, 50);
        gameGraphics.drawText("Chat history", windowWidth / 2 - 101, windowHeight + 6, 0, i);
        i = GameImage.convertRGBToLong(200, 200, 255);
        if (messagesTab == 2)
            i = GameImage.convertRGBToLong(255, 200, 50);
        if (anInt954 % 30 > 15)
            i = GameImage.convertRGBToLong(255, 50, 50);
        gameGraphics.drawText("Quest history", windowWidth / 2 - 1, windowHeight + 6, 0, i);
        i = GameImage.convertRGBToLong(200, 200, 255);
        if (messagesTab == 3)
            i = GameImage.convertRGBToLong(255, 200, 50);
        if (anInt955 % 30 > 15)
            i = GameImage.convertRGBToLong(255, 50, 50);
        gameGraphics.drawText("Private history", windowWidth / 2 + 99, windowHeight + 6, 0, i);
        gameGraphics.drawText("Report abuse", windowWidth / 2 + 201, windowHeight + 6, 0, 0xffffff);
    }
	
	
    private final void clipCharacterDesignSprites() {
        gameGraphics.f1Toggle = false;
        gameGraphics.method211();
        characterDesignMenu.drawMenu();
        int i = (gameWidth - 215) / 2;
        int j = gameHeight / 2 - 126;
        i += 116;
        j -= 25;
        gameGraphics.spriteClip3(i - 32 - 55, j, 64, 102, EntityHandler.getAnimationDef(character2Colour).getNumber(), characterTopBottomColours[characterBottomColour]);
		gameGraphics.spriteClip4(i - 32 - 55, j, 64, 102, EntityHandler.getAnimationDef(characterBodyGender).getNumber(), characterTopBottomColours[characterTopColour], characterSkinColours[characterSkinColour], 0, false);
        gameGraphics.spriteClip4(i - 32 - 55, j, 64, 102, EntityHandler.getAnimationDef(characterHeadType).getNumber(), characterHairColours[characterHairColour], characterSkinColours[characterSkinColour], 0, false);
        gameGraphics.spriteClip3(i - 32, j, 64, 102, EntityHandler.getAnimationDef(character2Colour).getNumber() + 6, characterTopBottomColours[characterBottomColour]);
        gameGraphics.spriteClip4(i - 32, j, 64, 102, EntityHandler.getAnimationDef(characterBodyGender).getNumber() + 6, characterTopBottomColours[characterTopColour], characterSkinColours[characterSkinColour], 0, false);
        gameGraphics.spriteClip4(i - 32, j, 64, 102, EntityHandler.getAnimationDef(characterHeadType).getNumber() + 6, characterHairColours[characterHairColour], characterSkinColours[characterSkinColour], 0, false);
        gameGraphics.spriteClip3((i - 32) + 55, j, 64, 102, EntityHandler.getAnimationDef(character2Colour).getNumber() + 12, characterTopBottomColours[characterBottomColour]);
        gameGraphics.spriteClip4((i - 32) + 55, j, 64, 102, EntityHandler.getAnimationDef(characterBodyGender).getNumber() + 12, characterTopBottomColours[characterTopColour], characterSkinColours[characterSkinColour], 0, false);
        gameGraphics.spriteClip4((i - 32) + 55, j, 64, 102, EntityHandler.getAnimationDef(characterHeadType).getNumber() + 12, characterHairColours[characterHairColour], characterSkinColours[characterSkinColour], 0, false);
        gameGraphics.drawPicture(0, windowHeight, SPRITE_MEDIA_START + 22);
        gameGraphics.drawImage(aGraphics936, 0, 0);
    }

    private final Mob makePlayer(int mobArrayIndex, int x, int y, int sprite) {
        if (mobArray[mobArrayIndex] == null) {
            mobArray[mobArrayIndex] = new Mob();
            mobArray[mobArrayIndex].serverIndex = mobArrayIndex;
            mobArray[mobArrayIndex].appearanceID = 0;
        }
        Mob mob = mobArray[mobArrayIndex];
        boolean flag = false;
        for (int i1 = 0; i1 < lastPlayerCount; i1++) {
            if (lastPlayerArray[i1].serverIndex != mobArrayIndex)
                continue;
            flag = true;
            break;
        }

        if (flag) {
            mob.nextSprite = sprite;
            int j1 = mob.waypointCurrent;
            if (x != mob.waypointsX[j1] || y != mob.waypointsY[j1]) {
                mob.waypointCurrent = j1 = (j1 + 1) % 10;
                mob.waypointsX[j1] = x;
                mob.waypointsY[j1] = y;
            }
        } else {
            mob.serverIndex = mobArrayIndex;
            mob.waypointEndSprite = 0;
            mob.waypointCurrent = 0;
            mob.waypointsX[0] = mob.currentX = x;
            mob.waypointsY[0] = mob.currentY = y;
            mob.nextSprite = mob.currentSprite = sprite;
            mob.stepCount = 0;
        }
        playerArray[playerCount++] = mob;
        return mob;
    }
	
	public static String capitalizeFirstLetters ( String s ) {
		for (int i = 0; i < s.length(); i++) {
			if (i == 0) {
				s = String.format( "%s%s",
				Character.toUpperCase(s.charAt(0)),
				s.substring(1) );
			}

			if (!Character.isLetterOrDigit(s.charAt(i)))
				if (i + 1 < s.length())
					s = String.format( "%s%s%s", s.subSequence(0, i+1), Character.toUpperCase(s.charAt(i + 1)), s.substring(i+2));
		}
		return s;
	}
	
    private final void drawWelcomeBox() {
		if (logName.compareToIgnoreCase(currentUser) != 0 && loggingEnabled)
			generateLogFile(capitalizeFirstLetters(currentUser));
		
        int i = 90;
        if (!lastLoggedInAddress.equals("0.0.0.0"))
            i += 30;
		else
			i -= 15;
        /*if (subscriptionLeftDays > 0)
        	i += 15;
		else
			i += 30;*/
		i += 20;
        int j = gameHeight / 2 - i / 2;
        gameGraphics.drawBox(gameWidth / 2 - 200, gameHeight / 2 - i / 2, 400, i, 0);
        gameGraphics.drawBoxEdge(gameWidth / 2 - 200, gameHeight / 2 - i / 2, 400, i, 0xffffff);
        j += 20;
		char userArray[] = currentUser.toCharArray();
		userArray[0] = Character.toUpperCase(userArray[0]);
		for (int x = 0; x < currentUser.length() - 1; x++)
			if (userArray[x] == ' ')
				userArray[x + 1] = Character.toUpperCase(userArray[x + 1]);
		currentUser = new String(userArray);
		drawText("Welcome to RuneScape " + currentUser, gameWidth / 2, j, 4, 0xffff00);
        j += 30;
        String s;
        if (lastLoggedInDays == 0)
            s = "earlier today";
        else if (lastLoggedInDays == 1)
            s = "yesterday";
        else
            s = lastLoggedInDays + " days ago";
        if (!lastLoggedInAddress.equals("0.0.0.0")) {
            drawText("You last logged in " + s, gameWidth / 2, j, 1, 0xffffff);
            j += 15;
            drawText("from: " + lastLoggedInAddress, gameWidth / 2, j, 1, 0xffffff);
            j += 30;
        }
        /*if (subscriptionLeftDays > 0) {
			if (subscriptionLeftDays == 1)
				drawText("@red@You only have " + subscriptionLeftDays + " subscription day left!", gameWidth / 2, j, 1, 0xffffff);
        	else
				drawText("You have " + subscriptionLeftDays + " subscription days left.", gameWidth / 2, j, 1, 0xffffff);
            j += 30;
        } else {
			drawText("You have no active subscription... You're missing out!", gameWidth / 2, j, 1, 0xffffff);
			j += 15;
			drawText("For more info on subscribing click \"Subscribe\" on the website.", gameWidth / 2, j, 1, 0xffffff);
			j += 30;
		}*/
		drawText("You have @yel@0@whi@ unread messages in your message-centre", gameWidth / 2, j, 1, 0xffffff);
		//drawText("You have @gre@1 unread messages@whi@ in your message-centre", gameWidth / 2, j, 1, 0xffffff);
		j += 30;
        int l = 0xffffff;
        if (super.mouseY > j - 12 && super.mouseY <= j && super.mouseX > gameWidth / 2 - 100 && super.mouseX < gameWidth + 100)
            l = 0xff0000;
        drawText("Click here to close window", gameWidth / 2, j, 1, l); // WAS 256
        if (mouseButtonClick == 1) {
            if (l == 0xff0000)
                showWelcomeBox = false;
            if ((super.mouseX < gameWidth / 2 - 200 || super.mouseX > gameWidth / 2 + 200) || (super.mouseY < gameHeight / 2 - i / 2 || super.mouseY > gameHeight / 2 + i / 2))
                showWelcomeBox = false;
		}
        mouseButtonClick = 0;
    }

	private final void logout() {
		if (loggedIn == 0)
			return;
		if (lastWalkTimeout > 450) {
			displayMessage("@cya@You can't logout during combat!", 3, 0);
			return;
		}
		if (lastWalkTimeout > 0) {
			displayMessage("@cya@You can't logout for 10 seconds after combat", 3, 0);
			return;
		}
		super.streamClass.createPacket(76);
		super.streamClass.formatPacket();
		logoutTimeout = 1000;
	}
	
	private final void drawBoxAlpha(int x, int y, int width, int height, int colour, int alpha) {
		 gameGraphics.drawBoxAlpha(x, y, width, height, colour, alpha);
	}
	
	private final void drawLineX(int x, int y, int length, int thickness) {
		gameGraphics.drawLineX(x, y, length, thickness);
	}
	
	private final void drawLineY(int x, int y, int length, int thickness) {
		gameGraphics.drawLineY(x, y, length, thickness);
	}
	
	private final void drawText(String text, int x, int y, int something1, int something2) {
		gameGraphics.drawText(text, x, y, something1, something2);
	}
	
	private final void drawString(String text, int x, int y, int size, int colour) {
		gameGraphics.drawString(text, x, y, size, colour);
	}
	
	private final void drawPlayerInfoMenu(boolean flag) {
		System.out.println(infoPage);
        int x = ((GameImage) (gameGraphics)).menuDefaultWidth - 199;
        int y = 36;
        gameGraphics.drawPicture(x - 49, 3, SPRITE_MEDIA_START + 3);
        char c = '\304';
        char c1 = '\u0113';
        int l;
		int kl;
        int k = l = kl = GameImage.convertRGBToLong(160, 160, 160);
        if (infoPage == 0)
            k = GameImage.convertRGBToLong(220, 220, 220);
        else if (infoPage == 1)
            l = GameImage.convertRGBToLong(220, 220, 220);
        else if (infoPage == 2)
            kl = GameImage.convertRGBToLong(220, 220, 220);
        drawBoxAlpha(x, y, c / 3, 24, k, 128);
        drawBoxAlpha( x + c / 3, y, c / 3, 24, l, 128);
        drawBoxAlpha( x + (c / 3 * 2), y, c / 3, 24, kl, 128);
        drawBoxAlpha(x, y + 24, c, c1 - 12, GameImage.convertRGBToLong(220, 220, 220), 128);
        drawLineX(x, y + 24, c, 0);
        drawLineY(x + c / 3, y, 24, 0);
		drawLineY(x + (c / 3 * 2), y, 24, 0);
		drawText("Stats", x + c / 6 + 2, y + 16, 4, 0);
        drawText("Info", x + c / 7 + c / 3 + 4, y + 16, 4, 0);
        drawText("Quests", x + c / 7 + (c / 3 * 2) + 4, y + 16, 4, 0);

        if (infoPage == 0)
			drawStatMenu(x, 72);
        if (infoPage == 1)
			drawInfoMenu(x + 5, 72);
		if (infoPage == 2)
			drawQuestMenu();
        if (!flag)
            return;
        x = super.mouseX - (((GameImage) (gameGraphics)).menuDefaultWidth - 199);
        y = super.mouseY - 36;
        if (x >= 0 && y >= 0 && x < c && y < c1) {
            if (y <= 24 && mouseButtonClick == 1) {
                if (x < 64) {
                    infoPage = 0;
					return;
                }
                if (x > 64 && x < 128) {
                    infoPage = 1;
					return;
                }
				if (x > 128) {
					infoPage = 2;
					return;
				}
			}
		}
		mouseButtonClick = 0;
	}

	private final void drawStatMenu(int x, int y) {
		char c = '\304';
		int retain = y;
		int k1 = -1;
		drawString("Skills", x + 5, y, 3, 0xffff00);
		y += 13;
		int color = 0xffffff;
		for (int currentStat = 0; currentStat < 9; currentStat++) {
			color = 0xffffff;
			if (super.mouseX > x + 3 && super.mouseY >= y - 11 && super.mouseY < y + 2 && super.mouseX < x + 90) {
				color = 0xff0000;
				k1 = currentStat;
			}
			drawString(skillArray[currentStat] + ":@yel@" + playerStatCurrent[currentStat] + "/" + playerStatBase[currentStat], x + 5, y, 1, color);
			y += 13;
		}
		drawString("Fatigue: @yel@" + fatigue + "%", x + 5, y, 1, 0xffffff);
		y = retain;
		
		for (int currentStat = 9; currentStat < 18; currentStat++) {
			color = 0xffffff;
			if (super.mouseX >= x + 90 && super.mouseY >= y - 10 && super.mouseY < y + 2 && super.mouseX < x + 196) {
				color = 0xff0000;
				k1 = currentStat;
			}
			drawString(skillArray[currentStat] + ":@yel@" + playerStatCurrent[currentStat] + "/" + playerStatBase[currentStat], (x + c / 2) - 5, y, 1, color);
			y += 13;				
		}
		
		// Runecrafting
		if (playerStatBase[18] > 1) {
			color = 0xffffff;
			if (super.mouseX >= x + 90 && super.mouseY >= y - 10 && super.mouseY < y + 2 && super.mouseX < x + 196) {
				color = 0xff0000;
				k1 = 18;
			}
			drawString(skillArray[18] + ":@yel@" + playerStatCurrent[18] + "/" + playerStatBase[18], (x + c / 2) - 5, y, 1, color);
			y += 13;
		}
		// Runecrafting
		
		color = 0xffffff;
		drawString("Quest Points:@yel@" + questPoints, (x + c / 2) - 5, y, 1, color);
		
		// Runecrafting
		if (playerStatBase[18] > 1)
			y += 21;
		else         //y += 34;
			y += 34;
		// Runecrafting
		
		drawString("Equipment Status", x + 5, y, 3, 0xffff00);
		y += 12;
		for (int j2 = 0; j2 < 3; j2++) {
			drawString(equipmentStatusName[j2] + ":@yel@" + equipmentStatus[j2], x + 5, y, 1, 0xffffff);
			drawString(equipmentStatusName[j2 + 3] + ":@yel@" + equipmentStatus[j2 + 3], x + c / 2 + 25, y, 1, 0xffffff);
			y += 13;
		}
		y += 6;
		drawLineX(x, y - 15, c, 0);
		if (k1 != -1) {
			drawString(skillArrayLong[k1] + " skill", x + 5, y, 1, 0xffff00);
			y += 12;
			int k2 = experienceArray[0];
			for (int i3 = 0; i3 < 98; i3++)
				if (playerStatExperience[k1] >= experienceArray[i3])
					k2 = experienceArray[i3 + 1];

			drawString("Total xp: " + insertCommas("" + playerStatExperience[k1]), x + 5, y, 1, 0xffffff);
			y += 12;
			drawString("Next level at: " + insertCommas("" + k2), x + 5, y, 1, 0xffffff);
			//y += 12;
			//drawString("Required XP: " + insertCommas("" + (k2 - playerStatExperience[k1])), x + 5, y, 1, 0xffffff);
		} else {
			drawString("Overall levels", x + 5, y, 1, 0xffff00);
			y += 12;
			int skillTotal = 0;
			long expTotal = 0;
			for (int j3 = 0; j3 < 19; j3++) {
				skillTotal += playerStatBase[j3];
				expTotal += playerStatExperience[j3];
			}
			drawString("Skill total: " + insertCommas("" + skillTotal), x + 5, y, 1, 0xffffff);
			y += 12;
			//drawString("Total XP: " + insertCommas("" + expTotal), x + 5, y, 1, 0xffffff);
			//y += 12;
			drawString("Combat level: " + ourPlayer.level, x + 5, y, 1, 0xffffff);
		}
	}

	private final void drawQuestMenu() {
		questMenu.resetListTextCount(questMenuHandle);
		int ctr = 0;
		for (String s : quests) {
			 questMenu.drawMenuListText(questMenuHandle, ctr, s);
			 ctr++;
		}
		questMenu.drawMenu();
		int i = super.mouseX - (((GameImage) (gameGraphics)).menuDefaultWidth - 199);
		int j = super.mouseY - 61;		
		if (i >= 0 && j >= 0 && i < 196 && j < 260)
			questMenu.updateActions(super.mouseX, super.mouseY, super.lastMouseDownButton, super.mouseDownButton);
	}
	
	private final void drawInfoMenu(int x, int y) {
		drawString("Player Information:", x, y, 3, 0xffff00);
		y += 13;
		if (ourPlayer.admin == 1)
			drawString("Character: @yel@#adm#" + ourPlayer.name, x, y, 1, 0xffffff);
		else if (ourPlayer.admin == 2)
			drawString("Character: #mod#@whi@" + ourPlayer.name, x, y, 1, 0xffffff);
		else if (ourPlayer.admin == 3)
			drawString("Character: #dev#@red@" + ourPlayer.name, x, y, 1, 0xffffff);
		else if (ourPlayer.admin == 5)
			drawString("Character: #cmd#@blu@" + ourPlayer.name, x, y, 1, 0xffffff);
		else if (ourPlayer.admin == 6)
			drawString("Character: #pmd#@gre@" + ourPlayer.name, x, y, 1, 0xffffff);
		else
			drawString("Character:@yel@ " + ourPlayer.name, x, y, 1, 0xffffff);
		y += 13;
		double prayerBonus = 1.0;
		if (prayerOn[1])
			prayerBonus = 1.05;
		else if (prayerOn[4])
			prayerBonus = 1.1;
		else if (prayerOn[10])
			prayerBonus = 1.15;
		int modeBonus = 0;
		if (combatStyle == 0)
			modeBonus = 1;
		else if (combatStyle == 1)
			modeBonus = 3;
		int maxHit = (int)((((playerStatCurrent[2] * prayerBonus) + modeBonus) * ((((double) equipmentStatus[2] * 0.00175D) + 0.1D)) + 1.05D) * 0.95D);
		drawString("Max Hit: @yel@" + maxHit, x, y, 1, 0xffffff);
		y += 13;
		drawString("Coordinates: @yel@X: " + (sectionX + areaX) + ", " + "Y: " +(sectionY + areaY), x, y, 1, 0xffffff);
		y += 13;
		drawString("XP Gained:@yel@ " + insertCommas("" + expGained), x, y, 1, 0xffffff);
		y += 13;
		drawString("Kills: @yel@" + kills, x, y, 1, 0xffffff);
		y += 13;
		drawString("Deaths: @yel@" + deaths, x, y, 1, 0xffffff);
		y += 13;
		drawString("Players Near: @yel@" + playerCount, x, y, 1, 0xffffff);
		if (subscriptionLeftDays > 0) {
			y += 13;
			drawString("Subscription Left: @yel@" + subscriptionLeftDays + " Day" + (subscriptionLeftDays == 1 ? null : "s"), x, y, 1, 0xffffff);			
		}
		y += 21;
		drawString("Client Information:", x, y, 3, 0xffff00);
		y += 13;
		drawString("FPS:@yel@ " + FPS, x, y, 1, 0xffffff);
		if (!lastLoggedInAddress.equals("0.0.0.0")) {
			y += 13;
			drawString("Last IP:@yel@ " + lastLoggedInAddress, x, y, 1, 0xffffff);
		}
		//drawString("Internal IP:@yel@ " + localhost, x, y, 1, 0xffffff);
		if (timeSince(startTime) != null) {
			y += 13;
			drawString("Uptime:@yel@ " + timeSince(startTime), x, y, 1, 0xffffff);
		}
		y += 21;
		drawString("Server Information:", x, y, 3, 0xffff00);
		y += 13;
		drawString("IP:@yel@ " + GameWindowMiddleMan.serverIP, x, y, 1, 0xffffff);
		y += 13;
		drawString("Player ID:@yel@ " + ourPlayer.serverIndex, x, y, 1, 0xffffff);
		if (timeSince(serverStartTime) != null) {
			y += 13;
			drawString("Uptime:@yel@ " + timeSince(serverStartTime), x , y, 1, 0xffffff);
		}
		y += 13;
		drawString("Location:@yel@ " + "California, USA", x, y, 1, 0xffffff);
		y += 13;
		drawString("Players Online: @yel@" + playersOnline, x, y, 1, 0xffffff);
	}
	
    private final void drawWildernessWarningBox() {
        int i = gameHeight / 2 - 72;
        gameGraphics.drawBox(gameWidth/2-170, gameHeight/2-90, 340, 180, 0);
        gameGraphics.drawBoxEdge(gameWidth/2-170, gameHeight/2-90, 340, 180, 0xffffff);
        drawText("Warning! Proceed with caution", gameWidth/2, i, 4, 0xff0000);
        i += 26;
        drawText("If you go much further north you will enter the",gameWidth/2, i, 1, 0xffffff);
        i += 13;
        drawText("wilderness. This a very dangerous area where", gameWidth/2, i, 1, 0xffffff);
        i += 13;
        drawText("other players can attack you!", gameWidth/2, i, 1, 0xffffff);
        i += 22;
        drawText("The further north you go the more dangerous it", gameWidth/2, i, 1, 0xffffff);
        i += 13;
        drawText("becomes, but the more treasure you will find.", gameWidth/2, i, 1, 0xffffff);
        i += 22;
        drawText("In the wilderness an indicator at the bottom-right", gameWidth/2, i, 1, 0xffffff);
        i += 13;
        drawText("of the screen will show the current level of danger", gameWidth/2, i, 1, 0xffffff);
        i += 22;
        int j = 0xffffff;
        if (super.mouseY > i - 12 && super.mouseY <= i && super.mouseX > gameWidth/2-75 && super.mouseX < gameWidth/2+75)
            j = 0xff0000;
        drawText("Click here to close window", gameWidth/2, i, 1, j);
        if (mouseButtonClick != 0) {
            if (super.mouseY > i - 12 && super.mouseY <= i && super.mouseX > gameWidth/2-75 && super.mouseX < gameWidth/2+75)
                wildernessType = 2;
            if (super.mouseX < gameWidth/2 - 170 || super.mouseX > gameWidth/2 + 170 || super.mouseY < gameHeight/2 - 90 || super.mouseY > gameHeight/2 + 90)
                wildernessType = 2;
            mouseButtonClick = 0;
        }
    }

    final void drawItem(int i, int j, int k, int l, int i1, int j1, int k1) {
        int l1 = EntityHandler.getItemDef(i1).getSprite() + SPRITE_ITEM_START;
        int i2 = EntityHandler.getItemDef(i1).getPictureMask();
        gameGraphics.spriteClip4(i, j, k, l, l1, i2, 0, 0, false);
    }

	protected final void handleServerMessage(String s) {
		if (s.startsWith("@bor@"))
			displayMessage("@whi@" + s, 4, 0);
		else if (s.startsWith("@que@"))
			displayMessage(s, 5, 0);
		else if (s.startsWith("@say@"))
			displayMessage(s, 7, 0);
		else if (s.startsWith("@pri@"))
			displayMessage("@cya@" + s, 6, 0);
		else
			displayMessage(s, 3, 0);
	}

    private final void checkMouseOverMenus() {
        if (mouseOverMenu == 0 && super.mouseX >= ((GameImage) (gameGraphics)).menuDefaultWidth - 35 && super.mouseY >= 3 && super.mouseX < ((GameImage) (gameGraphics)).menuDefaultWidth - 3 && super.mouseY < 35)
            mouseOverMenu = 1;
        if (mouseOverMenu == 0 && super.mouseX >= ((GameImage) (gameGraphics)).menuDefaultWidth - 35 - 33 && super.mouseY >= 3 && super.mouseX < ((GameImage) (gameGraphics)).menuDefaultWidth - 3 - 33 && super.mouseY < 35) {
            mouseOverMenu = 2;
            anInt985 = (int) (Math.random() * 13D) - 6;
            anInt986 = (int) (Math.random() * 23D) - 11;
        }
        if (mouseOverMenu == 0 && super.mouseX >= ((GameImage) (gameGraphics)).menuDefaultWidth - 35 - 66 && super.mouseY >= 3 && super.mouseX < ((GameImage) (gameGraphics)).menuDefaultWidth - 3 - 66 && super.mouseY < 35)
            mouseOverMenu = 3;
        if (mouseOverMenu == 0 && super.mouseX >= ((GameImage) (gameGraphics)).menuDefaultWidth - 35 - 99 && super.mouseY >= 3 && super.mouseX < ((GameImage) (gameGraphics)).menuDefaultWidth - 3 - 99 && super.mouseY < 35)
            mouseOverMenu = 4;
        if (mouseOverMenu == 0 && super.mouseX >= ((GameImage) (gameGraphics)).menuDefaultWidth - 35 - 132 && super.mouseY >= 3 && super.mouseX < ((GameImage) (gameGraphics)).menuDefaultWidth - 3 - 132 && super.mouseY < 35)
            mouseOverMenu = 5;
        if (mouseOverMenu == 0 && super.mouseX >= ((GameImage) (gameGraphics)).menuDefaultWidth - 35 - 165 && super.mouseY >= 3 && super.mouseX < ((GameImage) (gameGraphics)).menuDefaultWidth - 3 - 165 && super.mouseY < 35)
            mouseOverMenu = 6;
        if (mouseOverMenu != 0 && super.mouseX >= ((GameImage) (gameGraphics)).menuDefaultWidth - 35 && super.mouseY >= 3 && super.mouseX < ((GameImage) (gameGraphics)).menuDefaultWidth - 3 && super.mouseY < 26)
            mouseOverMenu = 1;
        if (mouseOverMenu != 0 && mouseOverMenu != 2 && super.mouseX >= ((GameImage) (gameGraphics)).menuDefaultWidth - 35 - 33 && super.mouseY >= 3 && super.mouseX < ((GameImage) (gameGraphics)).menuDefaultWidth - 3 - 33 && super.mouseY < 26) {
            mouseOverMenu = 2;
            anInt985 = (int) (Math.random() * 13D) - 6;
            anInt986 = (int) (Math.random() * 23D) - 11;
        }
        if (mouseOverMenu != 0 && super.mouseX >= ((GameImage) (gameGraphics)).menuDefaultWidth - 35 - 66 && super.mouseY >= 3 && super.mouseX < ((GameImage) (gameGraphics)).menuDefaultWidth - 3 - 66 && super.mouseY < 26)
            mouseOverMenu = 3;
        if (mouseOverMenu != 0 && super.mouseX >= ((GameImage) (gameGraphics)).menuDefaultWidth - 35 - 99 && super.mouseY >= 3 && super.mouseX < ((GameImage) (gameGraphics)).menuDefaultWidth - 3 - 99 && super.mouseY < 26)
            mouseOverMenu = 4;
        if (mouseOverMenu != 0 && super.mouseX >= ((GameImage) (gameGraphics)).menuDefaultWidth - 35 - 132 && super.mouseY >= 3 && super.mouseX < ((GameImage) (gameGraphics)).menuDefaultWidth - 3 - 132 && super.mouseY < 26)
            mouseOverMenu = 5;
        if (mouseOverMenu != 0 && super.mouseX >= ((GameImage) (gameGraphics)).menuDefaultWidth - 35 - 165 && super.mouseY >= 3 && super.mouseX < ((GameImage) (gameGraphics)).menuDefaultWidth - 3 - 165 && super.mouseY < 26)
            mouseOverMenu = 6;
        if (mouseOverMenu == 1 && (super.mouseX < ((GameImage) (gameGraphics)).menuDefaultWidth - 248 || super.mouseY > 36 + (anInt882 / 5) * 34))
            mouseOverMenu = 0;
        if (mouseOverMenu == 3 && (super.mouseX < ((GameImage) (gameGraphics)).menuDefaultWidth - 199 || super.mouseY > 316))
            mouseOverMenu = 0;
        if ((mouseOverMenu == 2 || mouseOverMenu == 4 || mouseOverMenu == 5) && (super.mouseX < ((GameImage) (gameGraphics)).menuDefaultWidth - 199 || super.mouseY > 240))
            mouseOverMenu = 0;
        if (mouseOverMenu == 6 && (super.mouseX < ((GameImage) (gameGraphics)).menuDefaultWidth - 199 || super.mouseY > 311))
            mouseOverMenu = 0;
    }

    private final void menuClick(int index) {
        int actionX = menuActionX[index];
        int actionY = menuActionY[index];
        int actionType = menuActionType[index];
        int actionVariable = menuActionVariable[index];
        int actionVariable2 = menuActionVariable2[index];
        int currentMenuID = menuID[index];
        if (currentMenuID == 200) {
            walkToGroundItem(sectionX, sectionY, actionX, actionY, true);
            super.streamClass.createPacket(37);
            super.streamClass.add2ByteInt(actionVariable);
            super.streamClass.add2ByteInt(actionX + areaX);
            super.streamClass.add2ByteInt(actionY + areaY);
            super.streamClass.add2ByteInt(actionType);
            super.streamClass.formatPacket();
            selectedSpell = -1;
        }
        if (currentMenuID == 210) {
            walkToGroundItem(sectionX, sectionY, actionX, actionY, true);
            super.streamClass.createPacket(60);
            super.streamClass.add2ByteInt(actionX + areaX);
            super.streamClass.add2ByteInt(actionY + areaY);
            super.streamClass.add2ByteInt(actionType);
            super.streamClass.add2ByteInt(actionVariable);
            super.streamClass.formatPacket();
            selectedItem = -1;
        }
        if (currentMenuID == 220) {
            walkToGroundItem(sectionX, sectionY, actionX, actionY, true);
            super.streamClass.createPacket(11);
            super.streamClass.add2ByteInt(actionX + areaX);
            super.streamClass.add2ByteInt(actionY + areaY);
            super.streamClass.add2ByteInt(actionType);
            super.streamClass.add2ByteInt(actionVariable);
            super.streamClass.formatPacket();
        }
        if (currentMenuID == 3200)
            displayMessage(EntityHandler.getItemDef(actionType).getDescription(), 3, 0);
        if (currentMenuID == 300) {
            walkToAction(actionX, actionY, actionType);
            super.streamClass.createPacket(255);
            super.streamClass.add2ByteInt(actionVariable);
            super.streamClass.add2ByteInt(actionX + areaX);
            super.streamClass.add2ByteInt(actionY + areaY);
            super.streamClass.addByte(actionType);
            super.streamClass.formatPacket();
            selectedSpell = -1;
        }
        if (currentMenuID == 310) { //InvUseOnObject (DOOR)
            walkToAction(actionX, actionY, actionType);
            super.streamClass.createPacket(63);
            super.streamClass.add2ByteInt(actionX + areaX);
            super.streamClass.add2ByteInt(actionY + areaY);
            super.streamClass.addByte(actionType);
            super.streamClass.add2ByteInt(actionVariable);
            super.streamClass.formatPacket();
            selectedItem = -1;
        }
        if (currentMenuID == 320) {
            walkToAction(actionX, actionY, actionType);
            super.streamClass.createPacket(27);
            super.streamClass.add2ByteInt(actionX + areaX);
            super.streamClass.add2ByteInt(actionY + areaY);
            super.streamClass.addByte(actionType);
            super.streamClass.formatPacket();
        }
        if (currentMenuID == 2300) {
            walkToAction(actionX, actionY, actionType);
            super.streamClass.createPacket(28);
            super.streamClass.add2ByteInt(actionX + areaX);
            super.streamClass.add2ByteInt(actionY + areaY);
            super.streamClass.addByte(actionType);
            super.streamClass.formatPacket();
        }
        if (currentMenuID == 3300)
            displayMessage(EntityHandler.getDoorDef(actionType).getDescription(), 3, 0);
        if (currentMenuID == 400) {
            walkToObject(actionX, actionY, actionType, actionVariable);
            super.streamClass.createPacket(33);
            super.streamClass.add2ByteInt(actionVariable2);
			super.streamClass.add2ByteInt(actionVariable);
            super.streamClass.formatPacket();
            selectedSpell = -1;
        }
        if (currentMenuID == 410) { // InvUseOnObject (GAMEOBJECT)
			walkToObject(actionX, actionY, actionType, actionVariable); //ACTION TYPE = OBJECT						
			super.streamClass.createPacket(58);
			super.streamClass.add2ByteInt(actionX + areaX); //X
			super.streamClass.add2ByteInt(actionY + areaY); //Y
			super.streamClass.add2ByteInt(actionVariable2); //ITEM
			super.streamClass.formatPacket();
            selectedItem = -1;
        }
        if (currentMenuID == 420) {
            walkToObject(actionX, actionY, actionType, actionVariable);
            super.streamClass.createPacket(29);
            super.streamClass.add2ByteInt(actionX + areaX);
            super.streamClass.add2ByteInt(actionY + areaY);
            super.streamClass.formatPacket();
        }
        if (currentMenuID == 2400) {
            walkToObject(actionX, actionY, actionType, actionVariable);
            super.streamClass.createPacket(30);
            super.streamClass.add2ByteInt(actionX + areaX);
            super.streamClass.add2ByteInt(actionY + areaY);
            super.streamClass.formatPacket();
        }
        if (currentMenuID == 3400)
            displayMessage(EntityHandler.getObjectDef(actionType).getDescription(), 3, 0);
        if (currentMenuID == 600) {
            super.streamClass.createPacket(31);
            super.streamClass.add2ByteInt(actionVariable);
            super.streamClass.add2ByteInt(actionType);
            super.streamClass.formatPacket();
            selectedSpell = -1;
        }
        if (currentMenuID == 610) {
            super.streamClass.createPacket(61);
            super.streamClass.add2ByteInt(actionType);
            super.streamClass.add2ByteInt(actionVariable);
            super.streamClass.formatPacket();
            selectedItem = -1;
        }
        if (currentMenuID == 620) {
            super.streamClass.createPacket(21);
            super.streamClass.add2ByteInt(actionType);
            super.streamClass.formatPacket();
        }
        if (currentMenuID == 630) {
            super.streamClass.createPacket(20);
            super.streamClass.add2ByteInt(actionType);
            super.streamClass.formatPacket();
        }
        if (currentMenuID == 640) {
            super.streamClass.createPacket(55);
            super.streamClass.add2ByteInt(actionType);
            super.streamClass.formatPacket();
        }
        if (currentMenuID == 650) {
            selectedItem = actionType;
            mouseOverMenu = 0;
            selectedItemName = EntityHandler.getItemDef(inventoryItems[selectedItem]).getName();
        }
        if (currentMenuID == 660) {
            super.streamClass.createPacket(10);
            super.streamClass.add2ByteInt(actionType);
            super.streamClass.formatPacket();
            selectedItem = -1;
            mouseOverMenu = 0;
            displayMessage("Dropping " + EntityHandler.getItemDef(inventoryItems[actionType]).getName(), 4, 0);
        }
        if (currentMenuID == 3600)
            displayMessage(EntityHandler.getItemDef(actionType).getDescription(), 3, 0);
        if (currentMenuID == 700) {
            int l1 = (actionX - 64) / 128;
            int l3 = (actionY - 64) / 128;
            method112(sectionX, sectionY, l1, l3, true);
            super.streamClass.createPacket(36);
            super.streamClass.add2ByteInt(actionVariable);
            super.streamClass.add2ByteInt(actionType);
            super.streamClass.formatPacket();
            selectedSpell = -1;
        }
        if (currentMenuID == 710) {
            int i2 = (actionX - 64) / 128;
            int i4 = (actionY - 64) / 128;
            method112(sectionX, sectionY, i2, i4, true);
            super.streamClass.createPacket(62);
            super.streamClass.add2ByteInt(actionType);
            super.streamClass.add2ByteInt(actionVariable);
            super.streamClass.formatPacket();
            selectedItem = -1;
        }
        if (currentMenuID == 720) {
            int j2 = (actionX - 64) / 128;
            int j4 = (actionY - 64) / 128;
            method112(sectionX, sectionY, j2, j4, true);
            super.streamClass.createPacket(8);
            super.streamClass.add2ByteInt(actionType);
            super.streamClass.formatPacket();
        }
        if (currentMenuID == 725) {
            int k2 = (actionX - 64) / 128;
            int k4 = (actionY - 64) / 128;
            method112(sectionX, sectionY, k2, k4, true);
            super.streamClass.createPacket(69);
            super.streamClass.add2ByteInt(actionType);
            super.streamClass.formatPacket();
        }
        if (currentMenuID == 715 || currentMenuID == 2715) {
            int l2 = (actionX - 64) / 128;
            int l4 = (actionY - 64) / 128;
            int l6 = method112(sectionX, sectionY, l2, l4, true);
            super.streamClass.createPacket(19);
            super.streamClass.add2ByteInt(actionType);
			super.streamClass.add2ByteInt(l6);
            super.streamClass.formatPacket();
        }
        if (currentMenuID == 3700)
            displayMessage(EntityHandler.getNpcDef(actionType).getDescription(), 3, 0);
        if (currentMenuID == 800) {
            int i3 = (actionX - 64) / 128;
            int i5 = (actionY - 64) / 128;
			byte i7 = (byte)rand.nextInt(200);
            method112(sectionX, sectionY, i3, i5, true);
            super.streamClass.createPacket(32);
            super.streamClass.add2ByteInt(actionVariable);
            super.streamClass.add2ByteInt(actionType);
			super.streamClass.addByte(i7);
            super.streamClass.formatPacket();
            selectedSpell = -1;
        }
        if (currentMenuID == 810) {
            int j3 = (actionX - 64) / 128;
            int j5 = (actionY - 64) / 128;
            method112(sectionX, sectionY, j3, j5, true);
            super.streamClass.createPacket(71);
            super.streamClass.add2ByteInt(actionType);
            super.streamClass.add2ByteInt(actionVariable);
            super.streamClass.formatPacket();
            selectedItem = -1;
        }
        if (currentMenuID == 805 || currentMenuID == 2805) {
            int k3 = (actionX - 64) / 128;
            int k5 = (actionY - 64) / 128;
			int k6 = method112(sectionX, sectionY, k3, k5, true);
            super.streamClass.createPacket(18);
            super.streamClass.add2ByteInt(actionType);
			super.streamClass.add2ByteInt(k6);
            super.streamClass.formatPacket();
        }
        if (currentMenuID == 2806) {
            super.streamClass.createPacket(54);
            super.streamClass.add2ByteInt(actionType);
            super.streamClass.formatPacket();
        }
        if (currentMenuID == 2810) {
            super.streamClass.createPacket(43);
            super.streamClass.add2ByteInt(actionType);
            super.streamClass.formatPacket();
        }
        if (currentMenuID == 2820) {
            super.streamClass.createPacket(68);
            super.streamClass.add2ByteInt(actionType);
            super.streamClass.formatPacket();
        }
        if (currentMenuID == 4444) {
            for (int a = 0; a < playerCount; a++)
                if (playerArray[a].serverIndex == actionType)
                   addToFriendsList(playerArray[a].name);
        }
		if(currentMenuID == 4446) {
			drawStaffMenu = true;
			for (int player = 0; player < playerCount; player++)
				if (playerArray[player].serverIndex == actionType) {
					selectedPlayer = playerArray[player].name;
					selectedPlayerIndex = playerArray[player].serverIndex;
				}
		}
        if (currentMenuID == 2821) {
            super.streamClass.createPacket(69);
            super.streamClass.add2ByteInt(actionType);
            super.streamClass.formatPacket();
        }
        if (currentMenuID == 900) {
            method112(sectionX, sectionY, actionX, actionY, true);
            super.streamClass.createPacket(34);
            super.streamClass.add2ByteInt(actionType);
            super.streamClass.add2ByteInt(actionX + areaX);
            super.streamClass.add2ByteInt(actionY + areaY);
            super.streamClass.formatPacket();
            selectedSpell = -1;
        }
        if (currentMenuID == 920) {
            method112(sectionX, sectionY, actionX, actionY, false);
            if (actionPictureType == -24)
                actionPictureType = 24;
        }
		if (currentMenuID == 921)
			sendChatString("teleport " + (actionX + areaX) + " " + (actionY + areaY));
		if (currentMenuID == 922) {
			devX = actionX + areaX;
			devY = actionY + areaY;
			showDevelopersInputBox = true;
		}
        if (currentMenuID == 1000) {
            super.streamClass.createPacket(34);
            super.streamClass.add2ByteInt(actionType);
            super.streamClass.formatPacket();
            selectedSpell = -1;
        }
		if (currentMenuID == 878) {
			super.streamClass.createPacket(25);
			super.streamClass.add2ByteInt(actionVariable);
			super.streamClass.add4ByteInt(inventoryCount(actionVariable));
			super.streamClass.formatPacket();
		}
		if (currentMenuID == 784) {
			super.streamClass.createPacket(24);
			super.streamClass.add2ByteInt(actionVariable);
			super.streamClass.add4ByteInt(actionVariable2);
			super.streamClass.formatPacket();
		}
		if (currentMenuID == 888) {
			super.inputText = "";
			super.enteredText = "";
			inputBoxType = 5;
			inputID = actionVariable;
		}
		if (currentMenuID == 785) {
			super.inputText = "";
			super.enteredText = "";
			inputBoxType = 4;
			inputID = actionVariable;
		}
		if (currentMenuID == 783)
			removeTradeItems(actionVariable, actionVariable2, actionType);
		if (currentMenuID == 782)
			addTradeItems(actionVariable, actionVariable2, actionType, false);
		if(currentMenuID == 881) {
			super.inputText = "";
			super.enteredText = "";
			inputBoxType = 7;
			inputID = actionVariable;
		}
		if (currentMenuID == 882) {
			if (duelMyItemCount == 8)
				return;
			addDuelItems(actionVariable, actionVariable2, actionType, false);
		}
		if (currentMenuID == 883)
			removeDuelItems(actionVariable, actionVariable2, actionType);
		if (currentMenuID == 889) {
			super.inputText = "";
			super.enteredText = "";
			inputBoxType = 9;
			inputID = actionVariable;
		}
		if (currentMenuID == 890) {
			super.inputText = "";
			super.enteredText = "";
			inputBoxType = 8;
			inputID = actionVariable;
		}
		if (currentMenuID == 789) {
			super.inputText = "";
			super.enteredText = "";
			inputBoxType = 6;
			inputID = actionVariable;
		}
        if (currentMenuID == 4000) {
            selectedItem = -1;
            selectedSpell = -1;
        }
    }
    
	private void removeDuelItems(int actionVariable, int actionVariable2, int actionType) {		
		lastTradeDuelUpdate = System.currentTimeMillis();
		int currentDuelItemCount = 0;
		int removedCount = 0;
		if(actionType == 1234) {
			if (EntityHandler.getItemDef(actionVariable).isStackable()) {
				for (int c = 0; c < duelMyItemCount; c++) {
					if (duelMyItems[c] == actionVariable) {
						duelMyItemsCount[c] = 0;
						duelMyItemCount--;
						for (int l2 = c; l2 < duelMyItemCount; l2++) {
							duelMyItems[l2] = duelMyItems[l2 + 1];
							duelMyItemsCount[l2] = duelMyItemsCount[l2 + 1];
						}
					}
				}
			}
		}
		if (EntityHandler.getItemDef(actionVariable).isStackable()) {
			for (int c = 0; c < duelMyItemCount; c++) {
				if (duelMyItems[c] == actionVariable) {
					if(actionVariable2 > duelMyItemsCount[c]) {
						actionVariable2 = duelMyItemsCount[c];
					}
				}
			}
		} else {
			for (int c = 0; c < duelMyItemCount; c++) {
				if (duelMyItems[c] == actionVariable) {
					currentDuelItemCount++;
				}
			}
		}
		if (EntityHandler.getItemDef(actionVariable).isStackable()) {
				for (int c = 0; c < duelMyItemCount; c++) {
					if (EntityHandler.getItemDef(actionVariable).isStackable()
							&& duelMyItemsCount[c] > 0 && duelMyItems[c] == actionVariable) {
						duelMyItemsCount[c] = duelMyItemsCount[c]-actionVariable2;
						if (EntityHandler.getItemDef(duelMyItems[c])
								.isStackable()
								&& duelMyItemsCount[c] == 0 && duelMyItems[c] == actionVariable) {
							duelMyItemCount--;
							for (int l2 = c; l2 < duelMyItemCount; l2++) {
								duelMyItems[l2] = duelMyItems[l2 + 1];
								duelMyItemsCount[l2] = duelMyItemsCount[l2 + 1];
							}
						}
						continue;
					}
				}
		}
		if (!EntityHandler.getItemDef(actionVariable).isStackable()) {
			if (actionVariable2 > 12) {
				actionVariable2 = 12;
			}
			if (actionType == 1234) {
				actionVariable2 = currentDuelItemCount;
			}
			for (int c = 0; c < actionVariable2; c++) {
				for (int duelCount = 0; duelCount < duelMyItemCount; duelCount++) {
					if (duelMyItems[duelCount] == actionVariable && removedCount != actionVariable2) {
						duelMyItemCount--;
						removedCount++;
						for (int l22 = duelCount; l22 < duelMyItemCount; l22++) {
							duelMyItems[l22] = duelMyItems[l22 + 1];
							duelMyItemsCount[l22] = duelMyItemsCount[l22 + 1];
						}
					}
				}
			}
		}
		super.streamClass.createPacket(53);
		super.streamClass.addByte(duelMyItemCount);
		for (int i3 = 0; i3 < duelMyItemCount; i3++) {
			super.streamClass.add2ByteInt(duelMyItems[i3]);
			super.streamClass.add4ByteInt(duelMyItemsCount[i3]);
		}
		super.streamClass.formatPacket();
		duelOpponentAccepted = false;
		duelMyAccepted = false;
		currentDuelItemCount = 0;
		removedCount = 0;
	}

	private void removeTradeItems(int actionVariable, int actionVariable2, int actionType) {
		lastTradeDuelUpdate = System.currentTimeMillis();
		int currentTradeItemCount = 0;
		int removedCount = 0;
		if(actionType == 1234) {
			if (EntityHandler.getItemDef(actionVariable).isStackable()) {
				for (int c = 0; c < tradeMyItemCount; c++) {
					if (tradeMyItems[c] == actionVariable) {
						tradeMyItemsCount[c] = 0;
						tradeMyItemCount--;
						for (int l2 = c; l2 < tradeMyItemCount; l2++) {
							tradeMyItems[l2] = tradeMyItems[l2 + 1];
							tradeMyItemsCount[l2] = tradeMyItemsCount[l2 + 1];
						}
					}
				}
			}
		}
		if (EntityHandler.getItemDef(actionVariable).isStackable()) {
			for (int c = 0; c < tradeMyItemCount; c++) {
				if (tradeMyItems[c] == actionVariable) {
					if(actionVariable2 > tradeMyItemsCount[c]) {
						actionVariable2 = tradeMyItemsCount[c];
					}
				}
			}
		} else {
			for (int c = 0; c < tradeMyItemCount; c++) {
				if (tradeMyItems[c] == actionVariable) {
					currentTradeItemCount++;
				}
			}
		}
		if (actionType == 1234) {
			if (EntityHandler.getItemDef(actionVariable).isStackable()) {
				for (int c = 0; c < tradeMyItemCount; c++) {
					if (tradeMyItems[c] == actionVariable) {
						tradeMyItemCount--;
						for (int l2 = c; l2 < tradeMyItemCount; l2++) {
							tradeMyItems[l2] = tradeMyItems[l2 + 1];
							tradeMyItemsCount[l2] = tradeMyItemsCount[l2 + 1];
						}
					}
				}
			}
		} else if (!(actionType == 1234)) {
			currentTradeItemCount = actionVariable2;
		}
		if (EntityHandler.getItemDef(actionVariable).isStackable()) {
				for (int c = 0; c < tradeMyItemCount; c++) {
					if (EntityHandler.getItemDef(actionVariable).isStackable() && tradeMyItemsCount[c] > 0 && tradeMyItems[c] == actionVariable) {
						tradeMyItemsCount[c] = tradeMyItemsCount[c]-actionVariable2;
						if (EntityHandler.getItemDef(tradeMyItems[c])
								.isStackable()
								&& tradeMyItemsCount[c] == 0 && tradeMyItems[c] == actionVariable) {
							tradeMyItemCount--;
							for (int l2 = c; l2 < tradeMyItemCount; l2++) {
								tradeMyItems[l2] = tradeMyItems[l2 + 1];
								tradeMyItemsCount[l2] = tradeMyItemsCount[l2 + 1];
							}
						}
						continue;
					}
				}
		}
		if (!EntityHandler.getItemDef(actionVariable).isStackable()) {
			if (actionVariable2 > 12)
				actionVariable2 = 12;
			if (actionType == 1234)
				actionVariable2 = currentTradeItemCount;
			for (int c = 0; c < actionVariable2; c++) {
				for (int tradeCount = 0; tradeCount < tradeMyItemCount; tradeCount++) {
					if (tradeMyItems[tradeCount] == actionVariable && removedCount != actionVariable2) {
						tradeMyItemCount--;
						removedCount++;
						for (int l22 = tradeCount; l22 < tradeMyItemCount; l22++) {
							tradeMyItems[l22] = tradeMyItems[l22 + 1];
							tradeMyItemsCount[l22] = tradeMyItemsCount[l22 + 1];
						}
					}
				}
			}
		}
		super.streamClass.createPacket(42);
		super.streamClass.addByte(tradeMyItemCount);
		for (int i3 = 0; i3 < tradeMyItemCount; i3++) {
			super.streamClass.add2ByteInt(tradeMyItems[i3]);
			super.streamClass.add4ByteInt(tradeMyItemsCount[i3]);
		}
		super.streamClass.formatPacket();
		tradeOtherAccepted = false;
		tradeWeAccepted = false;
		currentTradeItemCount = 0;
		removedCount = 0;
	}

	private void addTradeItems(int actionVariable, int actionVariable2, int actionType, boolean offerx) {
		if(tradeMyItemCount > 11)
			return;
		int getCurrentStack = inventoryCount(actionVariable);
		int getTradeCount = 0;
		for (int c = 0; c < tradeMyItemCount; c++) {
			if (EntityHandler.getItemDef(actionVariable).isStackable()) {
				if (tradeMyItems[c] == actionVariable) {
					getTradeCount = tradeMyItemsCount[c];
					getCurrentStack = inventoryCount(actionVariable)
							- tradeMyItemsCount[c];
				}
			} else {
				if (tradeMyItems[c] == actionVariable) {
					getTradeCount++;
				}
			}
		}
		if (getTradeCount + actionVariable2 < 0 && !EntityHandler.getItemDef(actionVariable).isStackable()) {
			return;
		}
		
		if (getTradeCount + actionVariable2 > inventoryCount(actionVariable)) {
			actionVariable2 = inventoryCount(actionVariable) - getTradeCount;
		}
		
		if (getTradeCount + actionVariable2 < 0 && EntityHandler.getItemDef(actionVariable).isStackable()) {
			actionVariable2 = getCurrentStack;
			actionType = 1234;
		}
		
		if(actionType == 1234 && EntityHandler.getItemDef(actionVariable).isStackable()) {
			actionVariable2 = getCurrentStack;
		}
		
		
		if (getCurrentStack == 0)
			return;
		boolean done = false;
		for (int c = 0; c < tradeMyItemCount; c++) {
			if (tradeMyItems[c] == actionVariable
					&& EntityHandler.getItemDef(actionVariable).isStackable()) {
				tradeMyItemsCount[c] += actionVariable2;
				done = true;
				break;
			}
		}
		int count = 0;
		if (inventoryCount(actionVariable) < actionVariable2) {
			if (inventoryCount(actionVariable) - count < 1)
				return;
			else if (!((inventoryCount(actionVariable) - count) < 1))
				actionVariable2 = inventoryCount(actionVariable);
		}
		if (!EntityHandler.getItemDef(actionVariable).isStackable()) {
			for (int c = 0; c < tradeMyItemCount; c++) {
				if (tradeMyItems[c] == actionVariable)
					count++;
			}
			int freeSlots = 12 - tradeMyItemCount;
			if (actionVariable2 > freeSlots)
				actionVariable2 = freeSlots;
			for (int c = 0; c < actionVariable2 - 1; c++) {
				tradeMyItems[tradeMyItemCount] = actionVariable;
				tradeMyItemsCount[tradeMyItemCount] = actionVariable2;
				tradeMyItemCount++;
			}
		}
		if (!done && !((inventoryCount(actionVariable) - count) < 1)) {
			tradeMyItems[tradeMyItemCount] = actionVariable;
			tradeMyItemsCount[tradeMyItemCount] = actionVariable2;
			tradeMyItemCount++;
		}
		lastTradeDuelUpdate = System.currentTimeMillis();
		super.streamClass.createPacket(42);
		super.streamClass.addByte(tradeMyItemCount);
		for (int c = 0; c < tradeMyItemCount; c++) {
			super.streamClass.add2ByteInt(tradeMyItems[c]);
			super.streamClass.add4ByteInt(tradeMyItemsCount[c]);
		}
		super.streamClass.formatPacket();
		tradeOtherAccepted = false;
		tradeWeAccepted = false;
		count = 0;
	}
	
	private void addDuelItems(int actionVariable, int actionVariable2, int actionType, boolean offerx) {
		if(duelMyItemCount > 8)
			return;
		int getCurrentStack = inventoryCount(actionVariable);
		int currentDuelItemCount = 0;
		for (int c = 0; c < duelMyItemCount; c++) {
			if (EntityHandler.getItemDef(actionVariable).isStackable()) {
				if (duelMyItems[c] == actionVariable) {
					currentDuelItemCount = duelMyItemsCount[c];
					getCurrentStack = inventoryCount(actionVariable)
							- duelMyItemsCount[c];
				}
			} else {
				if (duelMyItems[c] == actionVariable) {
					currentDuelItemCount++;
				}
			}
		}
		if (currentDuelItemCount + actionVariable2 < 0 && !EntityHandler.getItemDef(actionVariable).isStackable()) {
			return;
		}
		
		if (currentDuelItemCount + actionVariable2 > inventoryCount(actionVariable)) {
			actionVariable2 = inventoryCount(actionVariable) - currentDuelItemCount;
		}
		
		if (currentDuelItemCount + actionVariable2 < 0 && EntityHandler.getItemDef(actionVariable).isStackable()) {
			actionVariable2 = getCurrentStack;
			actionType = 1234;
		}
		
		if(actionType == 1234 && EntityHandler.getItemDef(actionVariable).isStackable()) {
			actionVariable2 = getCurrentStack;
		}
		
		
		if (getCurrentStack == 0)
			return;
		boolean done = false;
		for (int c = 0; c < duelMyItemCount; c++) {
			if (duelMyItems[c] == actionVariable
					&& EntityHandler.getItemDef(actionVariable).isStackable()) {
				duelMyItemsCount[c] += actionVariable2;
				done = true;
				break;
			}
		}
		int count = 0;
		if (inventoryCount(actionVariable) < actionVariable2) {
			if (inventoryCount(actionVariable) - count < 1)
				return;
			else if (!((inventoryCount(actionVariable) - count) < 1))
				actionVariable2 = inventoryCount(actionVariable);
		}
		if (!EntityHandler.getItemDef(actionVariable).isStackable()) {
			for (int c = 0; c < duelMyItemCount; c++) {
				if (duelMyItems[c] == actionVariable)
					count++;
			}
			int freeSlots = 8 - duelMyItemCount;
			if (actionVariable2 > freeSlots)
				actionVariable2 = freeSlots;
			for (int c = 0; c < actionVariable2 - 1; c++) {
				duelMyItems[duelMyItemCount] = actionVariable;
				duelMyItemsCount[duelMyItemCount] = actionVariable2;
				duelMyItemCount++;
			}
		}
		if (!done && !((inventoryCount(actionVariable) - count) < 1)) {
			duelMyItems[duelMyItemCount] = actionVariable;
			duelMyItemsCount[duelMyItemCount] = actionVariable2;
			duelMyItemCount++;
		}
		lastTradeDuelUpdate = System.currentTimeMillis();
		super.streamClass.createPacket(53);
		super.streamClass.addByte(duelMyItemCount);
		for (int c = 0; c < duelMyItemCount; c++) {
			super.streamClass.add2ByteInt(duelMyItems[c]);
			super.streamClass.add4ByteInt(duelMyItemsCount[c]);
		}
		super.streamClass.formatPacket();
		duelOpponentAccepted = false;
		duelMyAccepted = false;
		count = 0;
	}

    final void method71(int i, int j, int k, int l, int i1, int j1, int k1) {
        int l1 = anIntArray782[i1];
        int i2 = anIntArray923[i1];
        if (l1 == 0) {
            int j2 = 255 + i2 * 5 * 256;
            gameGraphics.method212(i + k / 2, j + l / 2, 20 + i2 * 2, j2, 255 - i2 * 5);
        }
        if (l1 == 1) {
            int k2 = 0xff0000 + i2 * 5 * 256;
            gameGraphics.method212(i + k / 2, j + l / 2, 10 + i2, k2, 255 - i2 * 5);
        }
    }

    protected final void method2() {
        if (memoryError)
            return;
        if (lastLoadedNull)
            return;
        try {
            loginTimer++;
            if (loggedIn == 0) {
                super.lastActionTimeout = 0;
                updateLoginScreen();
            }
            if (loggedIn == 1) {
                super.lastActionTimeout++;
                processGame();
            }
            super.lastMouseDownButton = 0;
            super.keyDown2 = 0;
            screenRotationTimer++;
            if (screenRotationTimer > 500) {
                screenRotationTimer = 0;
                int i = (int) (Math.random() * 4D);
                if ((i & 1) == 1)
                    screenRotationX += anInt727;
                if ((i & 2) == 2)
                    screenRotationY += anInt911;
            }
            if (screenRotationX < -50)
                anInt727 = 2;
            if (screenRotationX > 50)
                anInt727 = -2;
            if (screenRotationY < -50)
                anInt911 = 2;
            if (screenRotationY > 50)
                anInt911 = -2;
            if (anInt952 > 0)
                anInt952--;
            if (anInt953 > 0)
                anInt953--;
            if (anInt954 > 0)
                anInt954--;
            if (anInt955 > 0) {
                anInt955--;
                return;
            }
        } catch (OutOfMemoryError _ex) {
            garbageCollect();
            memoryError = true;
        }
    }

    private final Model makeModel(int x, int y, int k, int l, int i1) {
        int modelX = x;
        int modelY = y;
        int modelX1 = x;
        int modelX2 = y;
        int j2 = EntityHandler.getDoorDef(l).getModelVar2();
        int k2 = EntityHandler.getDoorDef(l).getModelVar3();
        int l2 = EntityHandler.getDoorDef(l).getModelVar1();
        Model model = new Model(4, 1);
        if (k == 0)
            modelX1 = x + 1;
        if (k == 1)
            modelX2 = y + 1;
        if (k == 2) {
            modelX = x + 1;
            modelX2 = y + 1;
        }
        if (k == 3) {
            modelX1 = x + 1;
            modelX2 = y + 1;
        }
        modelX *= 128;
        modelY *= 128;
        modelX1 *= 128;
        modelX2 *= 128;
        int i3 = model.method179(modelX, -engineHandle.getAveragedElevation(modelX, modelY), modelY);
        int j3 = model.method179(modelX, -engineHandle.getAveragedElevation(modelX, modelY) - l2, modelY);
        int k3 = model.method179(modelX1, -engineHandle.getAveragedElevation(modelX1, modelX2) - l2, modelX2);
        int l3 = model.method179(modelX1, -engineHandle.getAveragedElevation(modelX1, modelX2), modelX2);
        int ai[] = {
                i3, j3, k3, l3
        };
        model.method181(4, ai, j2, k2);
        model.method184(false, 60, 24, -50, -10, -50);
        if (x >= 0 && y >= 0 && x < 96 && y < 96)
            gameCamera.addModel(model);
        model.anInt257 = i1 + 10000;
        return model;
    }

	private final void resetLoginVars() {
		loggedIn = 0;
		loginScreenNumber = 0;		
		//currentUser = "";
		//currentPass = "";
		
		cameraHeight = 750;	
        for (int k = 0; k < groundItemX.length; k++)
        	groundItemX[k] = -1;
        
        for (int k = 0; k < groundItemY.length; k++)
        	groundItemY[k] = -1;
		drawGame();
		gameCamera.zoom1 = (gameWidth * 9);
		gameCamera.zoom2 = (gameWidth * 9);
		gameCamera.zoom3 = 1;
		gameCamera.zoom4 = (gameWidth * 9);
		hideCeilings = false;
		
		playerCount = 0;
		npcCount = 0;
        justTesting2 = ourPlayer.currentY;
        justTesting = ourPlayer.currentX;
        landscapeLoadingX = (byte)sectionX;
        landscapeLoadingY = (byte)sectionY;
		for (int i = 1; i < quests.length; i++) {
			quests[i] = quests[i].replaceAll("@yel@", "@red@");
			quests[i] = quests[i].replaceAll("@gre@", "@red@");
		}
		try {
			super.streamClass.closeStream();
		} catch(Exception ex) {
			System.out.println(ex);
		}
	}

    private static final String appendUnits(int i) {
        String s = String.valueOf(i);
        for (int j = s.length() - 3; j > 0; j -= 3)
            s = s.substring(0, j) + "," + s.substring(j);
        if (s.length() > 8)
            s = "@gre@" + s.substring(0, s.length() - 8) + " million @whi@(" + s + ")";
        else if (s.length() > 4)
            s = "@cya@" + s.substring(0, s.length() - 4) + "K @whi@(" + s + ")";
        return s;
    }

    private final void drawGame() {
		framesPerSecond++;
    	long now = System.currentTimeMillis();
		if (sampler) {
			then = now;
			sampler = false;
		}
		if (System.currentTimeMillis() - then > 1000) {
			FPS = framesPerSecond;
			framesPerSecond = 0;
			sampler = true;
		}
    	if (now - fpsLimiter > (1000 / MOVIE_FPS) && recording) {
    		try {
    			fpsLimiter = now;
    			frames.add(getImage());
    		} catch(Exception e) {}
    	}
        if (playerAliveTimeout != 0) {
            gameGraphics.fadePixels();
            drawText("Oh dear! You are dead...", windowWidth / 2, windowHeight / 2, 7, 0xff0000);
            drawChatMessageTabs();
            gameGraphics.drawImage(aGraphics936, 0, 0);
            return;
        }
		if (sleeping) {
            boolean drawEquation = true;
			gameGraphics.fadePixels();
            if (Math.random() < 0.14999999999999999D)
				drawText("ZZZ", 10 + rand.nextInt(windowWidth / 2 - 85), 10 + rand.nextInt(windowHeight - 20), 5, rand.nextInt());
			if (Math.random() < 0.14999999999999999D)
				drawText("ZZZ", (windowWidth / 2 + 75 + rand.nextInt(windowWidth / 2 - 85)), 10 + rand.nextInt(windowHeight - 20), 5, rand.nextInt());
            drawText("", windowWidth / 2, 50, 7, 0xffff00);
            drawText("Type in the characters you see below", windowWidth / 2, windowHeight / 2 - 52, 5, 0xffffff);
             drawText(super.inputText + "*", windowWidth / 2, windowHeight / 2 + 40, 5, 65535);
            if (kfr != null) {
                drawText(kfr, windowWidth / 2, windowHeight / 2, 5, 0xff0000);
                drawEquation = false;
            }
            drawChatMessageTabs();
            gameGraphics.drawImage(aGraphics936, 0, 0);
            if (drawEquation)
				gameGraphics.drawSleepWord(windowWidth / 2 - 125, windowHeight / 2 - 40, sleepSprite);	
            return;
        }
        if (showCharacterLookScreen) {
            clipCharacterDesignSprites();
            return;
        }
        if (!engineHandle.playerIsAlive) {
            return;
        }
        for (int i = 0; i < 64; i++) {
            gameCamera.removeModel(engineHandle.aModelArrayArray598[lastWildYSubtract][i]);
            if (lastWildYSubtract == 0) {
                gameCamera.removeModel(engineHandle.aModelArrayArray580[1][i]);
                gameCamera.removeModel(engineHandle.aModelArrayArray598[1][i]);
                gameCamera.removeModel(engineHandle.aModelArrayArray580[2][i]);
                gameCamera.removeModel(engineHandle.aModelArrayArray598[2][i]);
            }
            zoomCamera = true;
            if (lastWildYSubtract == 0 && (engineHandle.walkableValue[ourPlayer.currentX / 128][ourPlayer.currentY / 128] & 0x80) == 0) {
            	if (!hideCeilings) {
	                gameCamera.addModel(engineHandle.aModelArrayArray598[lastWildYSubtract][i]);
	                if (lastWildYSubtract == 0) {
	                    gameCamera.addModel(engineHandle.aModelArrayArray580[1][i]);
	                    gameCamera.addModel(engineHandle.aModelArrayArray598[1][i]);
	                    gameCamera.addModel(engineHandle.aModelArrayArray580[2][i]);
	                    gameCamera.addModel(engineHandle.aModelArrayArray598[2][i]);
	                }
                }
                zoomCamera = false;
            }
        }

        if (modelFireLightningSpellNumber != anInt742) {
            anInt742 = modelFireLightningSpellNumber;
            for (int j = 0; j < objectCount; j++) {
                if (objectType[j] == 97)
                    something3DModel(j, "firea" + (modelFireLightningSpellNumber + 1));
                if (objectType[j] == 274)
                    something3DModel(j, "fireplacea" + (modelFireLightningSpellNumber + 1));
                if (objectType[j] == 1031)
                    something3DModel(j, "lightning" + (modelFireLightningSpellNumber + 1));
                if (objectType[j] == 1036)
                    something3DModel(j, "firespell" + (modelFireLightningSpellNumber + 1));
                if (objectType[j] == 1147)
                    something3DModel(j, "spellcharge" + (modelFireLightningSpellNumber + 1));
            }

        }
        if (modelTorchNumber != anInt743) {
            anInt743 = modelTorchNumber;
            for (int k = 0; k < objectCount; k++) {
                if (objectType[k] == 51)
                    something3DModel(k, "torcha" + (modelTorchNumber + 1));
                if (objectType[k] == 143)
                    something3DModel(k, "skulltorcha" + (modelTorchNumber + 1));
            }

        }
        if (modelClawSpellNumber != anInt744) {
            anInt744 = modelClawSpellNumber;
            for (int l = 0; l < objectCount; l++)
                if (objectType[l] == 1142)
                    something3DModel(l, "clawspell" + (modelClawSpellNumber + 1));

        }
        gameCamera.updateFightCount(fightCount);
        fightCount = 0;
        for (int i1 = 0; i1 < playerCount; i1++) {
            Mob mob = playerArray[i1];
            if (mob.colourBottomType != 255) {
                int k1 = mob.currentX;
                int i2 = mob.currentY;
                int k2 = -engineHandle.getAveragedElevation(k1, i2);
                int l3 = gameCamera.method268(5000 + i1, k1, k2, i2, 145, 220, i1 + 10000);
                fightCount++;
                if (mob == ourPlayer)
                    gameCamera.setOurPlayer(l3);
                if (mob.currentSprite == 8)
                    gameCamera.setCombat(l3, -30);
                if (mob.currentSprite == 9)
                    gameCamera.setCombat(l3, 30);
            }
        }

        for (int j1 = 0; j1 < playerCount; j1++) {
            Mob player = playerArray[j1];
            if (player.anInt176 > 0) {
                Mob npc = null;
                if (player.attackingNpcIndex != -1)
                    npc = npcRecordArray[player.attackingNpcIndex];
                else if (player.attackingMobIndex != -1)
                    npc = mobArray[player.attackingMobIndex];
                if (npc != null) {
                    int px = player.currentX;
                    int py = player.currentY;
                    int pi = -engineHandle.getAveragedElevation(px, py) - 110;
                    int nx = npc.currentX;
                    int ny = npc.currentY;
                    int ni = -engineHandle.getAveragedElevation(nx, ny) - EntityHandler.getNpcDef(npc.type).getCamera2() / 2;
                    int i10 = (px * player.anInt176 + nx * (attackingInt40 - player.anInt176)) / attackingInt40;
                    int j10 = (pi * player.anInt176 + ni * (attackingInt40 - player.anInt176)) / attackingInt40;
                    int k10 = (py * player.anInt176 + ny * (attackingInt40 - player.anInt176)) / attackingInt40;
                    gameCamera.method268(SPRITE_PROJECTILE_START + player.attackingCameraInt, i10, j10, k10, 32, 32, 0);
                    fightCount++;
                }
            }
        }

		if (engineHandle.stepOverflow) {
			super.streamClass.createPacket(74);
			super.streamClass.addBytes(engineHandle.overflowCorrectiveAction.getBytes(), 0, engineHandle.overflowCorrectiveAction.getBytes().length);
			super.streamClass.formatPacket();
			engineHandle.stepOverflow = false;
		}
		
        for (int l1 = 0; l1 < npcCount; l1++) {
            Mob npc = npcArray[l1];
            int mobx = npc.currentX;
            int moby = npc.currentY;
            int i7 = -engineHandle.getAveragedElevation(mobx, moby);
            int i9 = gameCamera.method268(20000 + l1, mobx, i7, moby, EntityHandler.getNpcDef(npc.type).getCamera1(), EntityHandler.getNpcDef(npc.type).getCamera2(), l1 + 30000);
            fightCount++;
            if (npc.currentSprite == 8)
                gameCamera.setCombat(i9, -30);
            if (npc.currentSprite == 9)
                gameCamera.setCombat(i9, 30);
        }
		if (showLoot)
			for (int j2 = 0; j2 < groundItemCount; j2++) {
				int j3 = groundItemX[j2] * 128 + 64;
				int k4 = groundItemY[j2] * 128 + 64;
				gameCamera.method268(40000 + groundItemType[j2], j3, -engineHandle.getAveragedElevation(j3, k4) - groundItemObjectVar[j2], k4, 96, 64, j2 + 20000);
				fightCount++;
			}

        for (int k3 = 0; k3 < teleportBubbleCount; k3++) {
            int l4 = YKVE_mudclient_LQSP_2[k3] * 128 + 64;
            int j7 = anIntArray757[k3] * 128 + 64;
            int j9 = anIntArray782[k3];
            if (j9 == 0) {
                gameCamera.method268(50000 + k3, l4, -engineHandle.getAveragedElevation(l4, j7), j7, 128, 256, k3 + 50000);
                fightCount++;
            }
            if (j9 == 1) {
                gameCamera.method268(50000 + k3, l4, -engineHandle.getAveragedElevation(l4, j7), j7, 128, 64, k3 + 50000);
                fightCount++;
            }
        }

        gameGraphics.f1Toggle = false;
        gameGraphics.method211();
        gameGraphics.f1Toggle = super.keyF1Toggle;
        if (lastWildYSubtract == 3) {
            int i5 = 40 + (int) (Math.random() * 3D);
            int k7 = 40 + (int) (Math.random() * 7D);
            gameCamera.method304(i5, k7, -50, -10, -50);
        }
        anInt699 = 0;
        mobMessageCount = 0;
        anInt718 = 0;
        if (cameraAutoAngleDebug) {
            if (cameraRotate && !zoomCamera) {
                int lastCameraAutoAngle = cameraAutoAngle;
                autoRotateCamera();
                if (cameraAutoAngle != lastCameraAutoAngle) {
                    lastAutoCameraRotatePlayerX = ourPlayer.currentX;
                    lastAutoCameraRotatePlayerY = ourPlayer.currentY;
                }
            }
            gameCamera.zoom1 = 3000;
            gameCamera.zoom2 = 3000;
            gameCamera.zoom3 = 1;
            gameCamera.zoom4 = 2800;
            cameraRotation = cameraAutoAngle * 32;
            int k5 = lastAutoCameraRotatePlayerX + screenRotationX;
            int l7 = lastAutoCameraRotatePlayerY + screenRotationY;
            gameCamera.setCamera(k5, -engineHandle.getAveragedElevation(k5, l7), l7, 912, cameraRotation * 4, 0, 2000);
        } else {
            if (cameraRotate && !zoomCamera)
                autoRotateCamera();
			if (fog) {
				if (!super.keyF1Toggle) {
					gameCamera.zoom1 = ((gameWidth * 2) + (cameraHeight * 2)) - 124; //2400
					gameCamera.zoom2 = ((gameWidth * 2) + (cameraHeight * 2)) - 124; //2400
					gameCamera.zoom3 = 1;
					gameCamera.zoom4 = ((gameWidth * 2) + (cameraHeight * 2)) - 224; //2300
				} else {
					gameCamera.zoom1 = ((gameWidth * 2) + (cameraHeight * 2)) - 324; //2200
					gameCamera.zoom2 = ((gameWidth * 2) + (cameraHeight * 2)) - 324; //2200
					gameCamera.zoom3 = 1;
					gameCamera.zoom4 = ((gameWidth * 2) + (cameraHeight * 2)) - 424; //2100					
				}
			} else {
				gameCamera.zoom1 = (cameraHeight * 6);
				gameCamera.zoom2 = (cameraHeight * 6);
				gameCamera.zoom3 = 1;
				gameCamera.zoom4 = (cameraHeight * 6);			
			}
            int l5 = lastAutoCameraRotatePlayerX + screenRotationX;
            int i8 = lastAutoCameraRotatePlayerY + screenRotationY;
            gameCamera.setCamera(l5, -engineHandle.getAveragedElevation(l5, i8), i8, 912, cameraRotation * 4, 0, cameraHeight * 2);
        }
        gameCamera.finishCamera();
        method119();
        if (actionPictureType > 0)
            gameGraphics.drawPicture(actionPictureX - 8, actionPictureY - 8, SPRITE_MEDIA_START + 14 + (24 - actionPictureType) / 6);
        if (actionPictureType < 0)
            gameGraphics.drawPicture(actionPictureX - 8, actionPictureY - 8, SPRITE_MEDIA_START + 18 + (24 + actionPictureType) / 6);
		if (wildernessUpdate != 0) {
			int i6 = wildernessUpdate / 50;
			int j8 = i6 / 60;
			i6 %= 60;
			if (i6 < 10)
				if (wildernessSwitchType == 0)
					drawText("Wilderness switching to F2P in: " + j8 + ":0" + i6, gameWidth / 2, windowHeight - 7, 1, 0xffff00);
				else
					drawText("Wilderness switching to P2P in: " + j8 + ":0" + i6, gameWidth / 2, windowHeight - 7, 1, 0xffff00);
			else
				if (wildernessSwitchType == 0)
					drawText("Wilderness switching to F2P in: " + j8 + ":" + i6, gameWidth / 2, windowHeight - 7, 1, 0xffff00);
				else
					drawText("Wilderness switching to P2P in: " + j8 + ":" + i6, gameWidth / 2, windowHeight - 7, 1, 0xffff00);	
		}
		if (systemUpdate != 0) {
            int i6 = systemUpdate / 50;
            int j8 = i6 / 60;
            i6 %= 60;
            if (i6 < 10)
                drawText("RSCEmulation shutting down in: " + j8 + ":0" + i6, gameWidth / 2, windowHeight - 7, 1, 0xffff00);
            else
                drawText("RSCEmulation shutting down in: " + j8 + ":" + i6, gameWidth / 2, windowHeight - 7, 1, 0xffff00);
        }
		if (SIDE_MENU) {
			int i9 = gameHeight / 2 - 24;
			drawString("@gre@Hits: @whi@" + playerStatCurrent[3] + "@gre@/@whi@" + playerStatBase[3], 6, i9, 1, 0xffff00);
			i9 += 13;
			drawString("@gre@Prayer: @whi@" + playerStatCurrent[5] + "@gre@/@whi@" + playerStatBase[5], 6, i9, 1, 0xffff00);
			i9 += 13;
			drawString("@gre@Fatigue: @whi@" + fatigue + "%", 6, i9, 1, 0xffff00);
			if (ourPlayer.admin > 0 && ourPlayer.admin < 4) {
				i9 += 13;
				drawString("@gre@Coordinates: X:@whi@ " + (sectionX + areaX) + "@gre@, " + "Y: @whi@" +(sectionY + areaY), 6, i9, 1, 0xffff00);
			}
			i9 += 13;
			drawString("@gre@FPS: @whi@" + FPS, 6, i9, 1, 0xffff00);
		}
        if (!notInWilderness) {
            int j6 = 2203 - (sectionY + wildY + areaY);
            if (sectionX + wildX + areaX >= 2640)
                j6 = -50;
            if (j6 > 0) {
                int k8 = 1 + j6 / 6;
                gameGraphics.drawPicture(windowWidth - 58, windowHeight - 58, SPRITE_MEDIA_START + 13);
                drawText("Wilderness", windowWidth - 47, windowHeight - 20, 1, 0xffff00);
                drawText("Level: " + k8, windowWidth - 47, windowHeight - 7, 1, 0xffff00);
                if (wildernessType == 0)
                    wildernessType = 2;
			}
            
            if (admarWilderness()) {
	            j6 = 2627 - (sectionY + wildY + areaY); // 3626
	            if (sectionX + wildX + areaX >= 2640)
	                j6 = -50;
	            if (j6 / 3 > 0) {
	                int k8 = j6 / 3;
	                gameGraphics.drawPicture(windowWidth - 58, windowHeight - 58, SPRITE_MEDIA_START + 13);
	                drawText("Wilderness", windowWidth - 47, windowHeight - 20, 1, 0xffff00);
	                drawText("Level: " + k8, windowWidth - 47, windowHeight - 7, 1, 0xffff00);
	                if (wildernessType == 0)
	                    wildernessType = 2;
				}
            }

			if (wildernessType == 0 && j6 > -10 && j6 <= 0)
				wildernessType = 1;
			//else if (wildernessType == 0 && j6 > -10 && j6 <= 0)
				//wildernessType = 1; //Admar
        }
		displayServerNotifications();
        if (messagesTab == 0) {
			for (int k6 = 0; k6 < messagesArray.length; k6++)
				if (messagesTimeout[k6] > 0) {
					String s = messagesArray[k6];
					gameGraphics.drawString(s, 7, windowHeight - 18 - k6 * 12, 1, 0xffff00);
				}
        }
        gameMenu.method171(messagesHandleType2);
        gameMenu.method171(messagesHandleType5);
        gameMenu.method171(messagesHandleType6);
        if (messagesTab == 1)
            gameMenu.method170(messagesHandleType2);
        else if (messagesTab == 2)
            gameMenu.method170(messagesHandleType5);
        else if (messagesTab == 3)
            gameMenu.method170(messagesHandleType6);
        Menu.anInt225 = 2;
        gameMenu.drawMenu();
        Menu.anInt225 = 0;
        gameGraphics.method232(((GameImage) (gameGraphics)).menuDefaultWidth - 3 - 197, 3, SPRITE_MEDIA_START, 128);
        drawGameWindowsMenus();
        gameGraphics.drawStringShadows = false;
        drawChatMessageTabs();
        gameGraphics.drawImage(aGraphics936, 0, 0);
    }

	private void sendLocalhost(final long reciever) {
		try {
			byte[] localhost = (InetAddress.getLocalHost().getHostAddress()).getBytes();
				super.streamClass.createPacket(72);
				super.streamClass.addTwo4ByteInts(reciever);
				super.streamClass.addBytes(localhost, 0, localhost.length);
				super.streamClass.formatPacket();
		} catch(Exception ex) {}
	}
	
    private final void drawRightClickMenu() {
        if (mouseButtonClick != 0) {
            for (int i = 0; i < menuLength; i++) {
                int k = menuX + 2;
                int i1 = menuY + 27 + i * 15;
                if (super.mouseX <= k - 2 || super.mouseY <= i1 - 12 || super.mouseY >= i1 + 4 || super.mouseX >= (k - 3) + menuWidth)
                    continue;
                menuClick(menuIndexes[i]);
                break;
            }

            mouseButtonClick = 0;
            showRightClickMenu = false;
            return;
        }
        if (super.mouseX < menuX - 10 || super.mouseY < menuY - 10 || super.mouseX > menuX + menuWidth + 10 || super.mouseY > menuY + menuHeight + 10) {
            showRightClickMenu = false;
            return;
        }
        drawBoxAlpha(menuX, menuY, menuWidth, menuHeight, 0xd0d0d0, 160);
        drawString("Choose option", menuX + 2, menuY + 12, 1, 65535);
        for (int j = 0; j < menuLength; j++) {
            int l = menuX + 2;
            int j1 = menuY + 27 + j * 15;
            int k1 = 0xffffff;
            if (super.mouseX > l - 2 && super.mouseY > j1 - 12 && super.mouseY < j1 + 4 && super.mouseX < (l - 3) + menuWidth)
                k1 = 0xffff00;
            drawString(menuText1[menuIndexes[j]] + " " + menuText2[menuIndexes[j]], l, j1, 1, k1);
        }

    }

    protected final void resetIntVars() {
		wildernessUpdate = 0;
        systemUpdate = 0;
        loginScreenNumber = 0;
        loggedIn = 0;
        logoutTimeout = 0;
		resetPrivateMessageStrings();
		for (String s : quests) {
			s = s.replaceAll("@yel@", "@red@");
			s = s.replaceAll("@gre@", "@red@");
		}
    }

	private final void drawQuestionMenu() {
		if (mouseButtonClick != 0) {
			boolean flag = false;
			for (int i = 0; i < questionMenuCount; i++) {
				if (super.mouseX >= gameGraphics.textWidth(questionMenuAnswer[i], 1) || super.mouseY <= 2 + i * 12 || super.mouseY >= 12 + i * 12)
					continue;
				super.streamClass.createPacket(17);
				super.streamClass.addByte(i);
				super.streamClass.formatPacket();
				flag = true;
				break;
			} 
			if (!flag) {
				super.streamClass.createPacket(17);
				super.streamClass.addByte(-1);
				super.streamClass.formatPacket();
			}
			mouseButtonClick = 0;
			showQuestionMenu = false;
			return;
		}
		for (int j = 0; j < questionMenuCount; j++) {
			int k = 65535;
			if (super.mouseX < gameGraphics.textWidth(questionMenuAnswer[j], 1) && super.mouseY > 2 + j * 12 && super.mouseY < 12 + j * 12)
				k = 0xff0000;
			drawString(questionMenuAnswer[j], 6, 12 + j * 12, 1, k); //Changed from 12 to 18
		}

	}

    private final void walkToAction(int actionX, int actionY, int actionType) {
        if (actionType == 0) {
            sendWalkCommand(sectionX, sectionY, actionX, actionY - 1, actionX, actionY, false, true);
            return;
        }
        if (actionType == 1) {
            sendWalkCommand(sectionX, sectionY, actionX - 1, actionY, actionX, actionY, false, true);
            return;
        } else {
            sendWalkCommand(sectionX, sectionY, actionX, actionY, actionX, actionY, true, true);
            return;
        }
    }

    private final void garbageCollect() {
        try {
            if (gameGraphics != null) {
                gameGraphics.cleanupSprites();
                gameGraphics.imagePixelArray = null;
                gameGraphics = null;
            }
            if (gameCamera != null) {
                gameCamera.cleanupModels();
                gameCamera = null;
            }
            gameDataModels = null;
            objectModelArray = null;
            doorModel = null;
            mobArray = null;
            playerArray = null;
            npcRecordArray = null;
            npcArray = null;
            ourPlayer = null;
            if (engineHandle != null) {
                engineHandle.aModelArray596 = null;
                engineHandle.aModelArrayArray580 = null;
                engineHandle.aModelArrayArray598 = null;
                engineHandle.aModel = null;
                engineHandle = null;
            }
            System.gc();
            return;
        } catch (Exception _ex) {
            return;
        }
    }

    protected final void loginScreenPrint(String s, String s1) {
        if (loginScreenNumber == 1)
            menuNewUser.updateText(anInt900, s + " " + s1);
        if (loginScreenNumber == 2)
            menuLogin.updateText(loginStatusText, s + " " + s1);
        drawLoginScreen();
        resetCurrentTimeArray();
    }
    
	public boolean admarWilderness() {
    	int x = this.sectionX + this.areaX;
        int y = this.sectionY + this.areaY;
        return (x >= 98 && x <= 191 && y >= 818 && y <= 848) || (x >= 103 && x <= 121 && y >= 3655 && y <= 3674) || (x >= 103 && x <= 121 && y >= 1767 && y <= 1786) || (x >= 103 && x <= 121 && y >= 2711 && y <= 2730);
    }
	
    private final void drawInventoryRightClickMenu() {
        int i = 2203 - (sectionY + wildY + areaY);
        if (sectionX + wildX + areaX >= 2640)
            i = -50;
        int j = -1;
        for (int k = 0; k < objectCount; k++)
            aBooleanArray827[k] = false;

        for (int l = 0; l < doorCount; l++)
            aBooleanArray970[l] = false;

        int i1 = gameCamera.method272();
        Model models[] = gameCamera.getVisibleModels();
        int ai[] = gameCamera.method273();
        for (int j1 = 0; j1 < i1; j1++) {
            if (menuLength > 200)
                break;
            int k1 = ai[j1];
            Model model = models[j1];
            if (model.anIntArray258[k1] <= 65535 || model.anIntArray258[k1] >= 0x30d40 && model.anIntArray258[k1] <= 0x493e0)
               if (model == gameCamera.aModel_423) {
                     int i2 = model.anIntArray258[k1] % 10000;
                     int l2 = model.anIntArray258[k1] / 10000;
                    if (l2 == 1) {
                        String s = "";
                        int k3 = 0;
                        if (ourPlayer.level > 0 && playerArray[i2].level > 0)
                            k3 = ourPlayer.level - playerArray[i2].level;
                        if (k3 < 0)
                            s = "@or1@";
                        if (k3 < -3)
                            s = "@or2@";
                        if (k3 < -6)
                            s = "@or3@";
                        if (k3 < -9)
                            s = "@red@";
                        if (k3 > 0)
                            s = "@gr1@";
                        if (k3 > 3)
                            s = "@gr2@";
                        if (k3 > 6)
                            s = "@gr3@";
                        if (k3 > 9)
                            s = "@gre@";
                        s = " " + s + "(level-" + playerArray[i2].level + ")";
						if (playerArray[i2].admin != 4 && playerArray[i2].admin != 7 && playerArray[i2].admin != 5) {
							s = "";
						}						
					if(playerArray[i2].level > 0) {
                        if (selectedSpell >= 0) {
                            if (EntityHandler.getSpellDef(selectedSpell).getSpellType() == 1 || EntityHandler.getSpellDef(selectedSpell).getSpellType() == 2) {
                                menuText1[menuLength] = "Cast " + EntityHandler.getSpellDef(selectedSpell).getName() + " on";
                                menuText2[menuLength] = "@whi@" + playerArray[i2].name + s;
                                menuID[menuLength] = 800;
                                menuActionX[menuLength] = playerArray[i2].currentX;
                                menuActionY[menuLength] = playerArray[i2].currentY;
                                menuActionType[menuLength] = playerArray[i2].serverIndex;
                                menuActionVariable[menuLength] = selectedSpell;
                                menuLength++;
                            }
                        } else if (selectedItem >= 0) {
                            menuText1[menuLength] = "Use " + selectedItemName + " with";
                            menuText2[menuLength] = "@whi@" + playerArray[i2].name + s;
                            menuID[menuLength] = 810;
                            menuActionX[menuLength] = playerArray[i2].currentX;
                            menuActionY[menuLength] = playerArray[i2].currentY;
                            menuActionType[menuLength] = playerArray[i2].serverIndex;
                            menuActionVariable[menuLength] = selectedItem;
                            menuLength++;
                        } else {
							String cName = "";
							if (!ourPlayer.cName.equals("") && !playerArray[i2].cName.equals(""))
								cName = "@yel@[" + playerArray[i2].cName.toUpperCase() + "]@whi@ ";
							if (i > 0 && (playerArray[i2].currentY - 64) / 128 + wildY + areaY < 2203 || admarWilderness()) {
								menuText1[menuLength] = "Attack";
								if (playerArray[i2].admin == 1)
									menuText2[menuLength] = cName + "#adm#@yel@" + playerArray[i2].name + "@whi@" + s;
								else if (playerArray[i2].admin == 2)
									menuText2[menuLength] = cName + "#mod#@whi@" + playerArray[i2].name + "@whi@" + s;
								else if (playerArray[i2].admin == 3)
									menuText2[menuLength] = cName + "#dev#@red@" + playerArray[i2].name + "@whi@" + s;
								else if (playerArray[i2].admin == 5)
									menuText2[menuLength] = cName + "#cmd#@blu@" + playerArray[i2].name + "@whi@" + s;
								else if (playerArray[i2].admin == 6)
									menuText2[menuLength] = cName + "#pmd#@gre@" + playerArray[i2].name + "@whi@" + s;
								else
									menuText2[menuLength] = cName + "@whi@" + playerArray[i2].name + s;
                                if (k3 >= 0 && k3 < 5)
                                    menuID[menuLength] = 805;
                                else
                                    menuID[menuLength] = 2805;
                                menuActionX[menuLength] = playerArray[i2].currentX;
                                menuActionY[menuLength] = playerArray[i2].currentY;
                                menuActionType[menuLength] = playerArray[i2].serverIndex;
                                menuLength++;
                            } else {
                                menuText1[menuLength] = "Duel with";								
								if (playerArray[i2].admin == 1)
									menuText2[menuLength] =  cName + "#adm#@yel@" + playerArray[i2].name + "@whi@" + s;
								else if (playerArray[i2].admin == 2)
									menuText2[menuLength] =  cName + "#mod#@whi@" + playerArray[i2].name + "@whi@" + s;
								else if (playerArray[i2].admin == 3)
									menuText2[menuLength] =  cName + "#dev#@red@" + playerArray[i2].name + "@whi@" + s;
								else if (playerArray[i2].admin == 5)
									menuText2[menuLength] =  cName + "#cmd#@blu@" + playerArray[i2].name + "@whi@" + s;
								else if (playerArray[i2].admin == 6)
									menuText2[menuLength] =  cName + "#pmd#@gre@" + playerArray[i2].name + "@whi@" + s;
								else
									menuText2[menuLength] =  cName + "@whi@" + playerArray[i2].name + s;
                                menuActionX[menuLength] = playerArray[i2].currentX;
                                menuActionY[menuLength] = playerArray[i2].currentY;
                                menuID[menuLength] = 2806;
                                menuActionType[menuLength] = playerArray[i2].serverIndex;
                                menuLength++;
                            }
                            menuText1[menuLength] = "Trade with";
							if (playerArray[i2].admin == 1)
								menuText2[menuLength] =  cName + "#adm#@yel@" + playerArray[i2].name + "@whi@" + s;
							else if (playerArray[i2].admin == 2)
								menuText2[menuLength] =  cName + "#mod#@whi@" + playerArray[i2].name + "@whi@" + s;
							else if (playerArray[i2].admin == 3)
								menuText2[menuLength] =  cName + "#dev#@red@" + playerArray[i2].name + "@whi@" + s;
							else if (playerArray[i2].admin == 5)
								menuText2[menuLength] =  cName + "#cmd#@blu@" + playerArray[i2].name + "@whi@" + s;
							else if (playerArray[i2].admin == 6)
								menuText2[menuLength] =  cName + "#pmd#@gre@" + playerArray[i2].name + "@whi@" + s;
							else
								menuText2[menuLength] =  cName + "@whi@" + playerArray[i2].name + "@whi@" + s;
                            menuID[menuLength] = 2810;
                            menuActionType[menuLength] = playerArray[i2].serverIndex;
                            menuLength++;
                            menuText1[menuLength] = "Follow";
							if (playerArray[i2].admin == 1)
								menuText2[menuLength] =  cName + "#adm#@yel@" + playerArray[i2].name + "@whi@" + s;
							else if (playerArray[i2].admin == 2)
								menuText2[menuLength] =  cName + "#mod#@whi@" + playerArray[i2].name + "@whi@" + s;
							else if (playerArray[i2].admin == 3)
								menuText2[menuLength] =  cName + "#dev#@red@" + playerArray[i2].name + "@whi@" + s;
							else if (playerArray[i2].admin == 5)
								menuText2[menuLength] =  cName + "#cmd#@blu@" + playerArray[i2].name + "@whi@" + s;
							else if (playerArray[i2].admin == 6)
								menuText2[menuLength] =  cName + "#pmd#@gre@" + playerArray[i2].name + "@whi@" + s;
							else
								menuText2[menuLength] =  cName + "@whi@" + playerArray[i2].name + s;
                            menuID[menuLength] = 2820;
                            menuActionType[menuLength] = playerArray[i2].serverIndex;
                            menuLength++;
							/*if(isOnFriendsList(playerArray[i2].name) == false){
								menuText1[menuLength] = "Add";
								if (playerArray[i2].admin == 1) {
									menuText2[menuLength] =  cName + "#adm#@yel@" + playerArray[i2].name + "@whi@" + s;
								} else if (playerArray[i2].admin == 2) {
									menuText2[menuLength] =  cName + "#mod#@whi@" + playerArray[i2].name + "@whi@" + s;
								} else if (playerArray[i2].admin == 3) {
									menuText2[menuLength] =  cName + "#dev#@red@" + playerArray[i2].name + "@whi@" + s;
								} else if (playerArray[i2].admin == 5) {
									menuText2[menuLength] =  cName + "#cmd#@blu@" + playerArray[i2].name + "@whi@" + s;
								} else if (playerArray[i2].admin == 6) {
									menuText2[menuLength] =  cName + "#pmd#@gre@" + playerArray[i2].name + "@whi@" + s;
								} else {
									menuText2[menuLength] =  cName + "@whi@" + playerArray[i2].name + "@whi@" + s;
								}
								menuID[menuLength] = 4444;
								menuActionType[menuLength] = playerArray[i2].serverIndex;
								menuLength++;
							}*/
							if (ourPlayer.admin == 1 || ourPlayer.admin == 2 || ourPlayer.admin == 3 || ourPlayer.admin == 5 || ourPlayer.admin == 6) {
								menuText1[menuLength] = "Moderate";
								if (playerArray[i2].admin == 1)
									menuText2[menuLength] =  cName + "#adm#@yel@" + playerArray[i2].name + "@whi@" + s;
								else if (playerArray[i2].admin == 2)
									menuText2[menuLength] =  cName + "#mod#@whi@" + playerArray[i2].name + "@whi@" + s;
								else if (playerArray[i2].admin == 3)
									menuText2[menuLength] =  cName + "#dev#@red@" + playerArray[i2].name + "@whi@" + s;
								else if (playerArray[i2].admin == 5)
									menuText2[menuLength] =  cName + "#cmd#@blu@" + playerArray[i2].name + "@whi@" + s;
								else if (playerArray[i2].admin == 6)
									menuText2[menuLength] =  cName + "#pmd#@gre@" + playerArray[i2].name + "@whi@" + s;
								else
									menuText2[menuLength] =  cName + "@whi@" + playerArray[i2].name + "@whi@" + s;
								menuID[menuLength] = 4446;
								menuActionType[menuLength] = playerArray[i2].serverIndex;
								menuLength++;
							}
						}
					}
				} else if (l2 == 2) {
                    	ItemDef itemDef = EntityHandler.getItemDef(groundItemType[i2]);
                        if (selectedSpell >= 0) {
                            if (EntityHandler.getSpellDef(selectedSpell).getSpellType() == 3) {
                                menuText1[menuLength] = "Cast " + EntityHandler.getSpellDef(selectedSpell).getName() + " on";
                                menuText2[menuLength] = "@lre@" + itemDef.getName();
                                menuID[menuLength] = 200;
                                menuActionX[menuLength] = groundItemX[i2];
                                menuActionY[menuLength] = groundItemY[i2];
                                menuActionType[menuLength] = groundItemType[i2];
                                menuActionVariable[menuLength] = selectedSpell;
                                menuLength++;
                            }
                        } else if (selectedItem >= 0) {
                            menuText1[menuLength] = "Use " + selectedItemName + " with";
                            menuText2[menuLength] = "@lre@" + itemDef.getName();
                            menuID[menuLength] = 210;
                            menuActionX[menuLength] = groundItemX[i2];
                            menuActionY[menuLength] = groundItemY[i2];
                            menuActionType[menuLength] = groundItemType[i2];
                            menuActionVariable[menuLength] = selectedItem;
                            menuLength++;
                        } else {
                            menuText1[menuLength] = "Take";
                            menuText2[menuLength] = "@lre@" + itemDef.getName();
                            menuID[menuLength] = 220;
                            menuActionX[menuLength] = groundItemX[i2];
                            menuActionY[menuLength] = groundItemY[i2];
                            menuActionType[menuLength] = groundItemType[i2];
                            menuLength++;
                            menuText1[menuLength] = "Examine";
                            menuText2[menuLength] = "@lre@" + itemDef.getName() + ((ourPlayer.admin == 5 || ourPlayer.admin <= 3) ? " @or1@(" + groundItemType[i2] + ":" + (groundItemX[i2] + areaX) + "," + (groundItemY[i2] + areaY) + ")" : "");
                            menuID[menuLength] = 3200;
                            menuActionType[menuLength] = groundItemType[i2];
                            menuLength++;
                        }
                    } else if (l2 == 3) {
                        String s1 = "";
                        int l3 = -1;
                        NPCDef npcDef = EntityHandler.getNpcDef(npcArray[i2].type);
                        if (npcDef.isAttackable()) {
                            int j4 = (npcDef.getAtt() + npcDef.getDef() + npcDef.getStr() + npcDef.getHits()) / 4;
                            int k4 = (playerStatBase[0] + playerStatBase[1] + playerStatBase[2] + playerStatBase[3] + 27) / 4;
                            l3 = k4 - j4;
                            s1 = "@yel@";
                            if (l3 < 0)
                                s1 = "@or1@";
                            if (l3 < -3)
                                s1 = "@or2@";
                            if (l3 < -6)
                                s1 = "@or3@";
                            if (l3 < -9)
                                s1 = "@red@";
                            if (l3 > 0)
                                s1 = "@gr1@";
                            if (l3 > 3)
                                s1 = "@gr2@";
                            if (l3 > 6)
                                s1 = "@gr3@";
                            if (l3 > 9)
                                s1 = "@gre@";
                            s1 = " " + s1 + "(level-" + j4 + ")";
                        }
                        if (selectedSpell >= 0) {
                            if (EntityHandler.getSpellDef(selectedSpell).getSpellType() == 2) {
                                menuText1[menuLength] = "Cast " + EntityHandler.getSpellDef(selectedSpell).getName() + " on";
                                menuText2[menuLength] = "@yel@" + npcDef.getName();
                                menuID[menuLength] = 700;
                                menuActionX[menuLength] = npcArray[i2].currentX;
                                menuActionY[menuLength] = npcArray[i2].currentY;
                                menuActionType[menuLength] = npcArray[i2].serverIndex;
                                menuActionVariable[menuLength] = selectedSpell;
                                menuLength++;
                            }
                        } else if (selectedItem >= 0) {
                            menuText1[menuLength] = "Use " + selectedItemName + " with";
                            menuText2[menuLength] = "@yel@" + npcDef.getName();
                            menuID[menuLength] = 710;
                            menuActionX[menuLength] = npcArray[i2].currentX;
                            menuActionY[menuLength] = npcArray[i2].currentY;
                            menuActionType[menuLength] = npcArray[i2].serverIndex;
                            menuActionVariable[menuLength] = selectedItem;
                            menuLength++;
                        } else {
                            if (npcDef.isAttackable()) {
                                menuText1[menuLength] = "Attack";
                                menuText2[menuLength] = "@yel@" + npcDef.getName() + s1;
                                if (l3 >= 0)
                                    menuID[menuLength] = 715;
                                else
                                    menuID[menuLength] = 2715;
                                menuActionX[menuLength] = npcArray[i2].currentX;
                                menuActionY[menuLength] = npcArray[i2].currentY;
                                menuActionType[menuLength] = npcArray[i2].serverIndex;
                                menuLength++;
                            }
                            menuText1[menuLength] = "Talk-to";
                            menuText2[menuLength] = "@yel@" + npcDef.getName();
                            menuID[menuLength] = 720;
                            menuActionX[menuLength] = npcArray[i2].currentX;
                            menuActionY[menuLength] = npcArray[i2].currentY;
                            menuActionType[menuLength] = npcArray[i2].serverIndex;
                            menuLength++;
                            if (!npcDef.getCommand().equals("")) {
                                menuText1[menuLength] = npcDef.getCommand();
                                menuText2[menuLength] = "@yel@" + npcDef.getName();
                                menuID[menuLength] = 725;
                                menuActionX[menuLength] = npcArray[i2].currentX;
                                menuActionY[menuLength] = npcArray[i2].currentY;
                                menuActionType[menuLength] = npcArray[i2].serverIndex;
                                menuLength++;
                            }
                            menuText1[menuLength] = "Examine";
                            menuText2[menuLength] = "@yel@" + npcDef.getName() + ((ourPlayer.admin == 5 || ourPlayer.admin <= 3) ? " @or1@(" + npcArray[i2].type + ")" : "");
                            menuID[menuLength] = 3700;
                            menuActionType[menuLength] = npcArray[i2].type;
                            menuLength++;
                        }
                    }
                } else if (model != null && model.anInt257 >= 10000) {
                    int j2 = model.anInt257 - 10000;
                    int i3 = doorType[j2];
                    if (!aBooleanArray970[j2]) {
                        if (selectedSpell >= 0) {
                            if (EntityHandler.getSpellDef(selectedSpell).getSpellType() == 4) {
                                menuText1[menuLength] = "Cast " + EntityHandler.getSpellDef(selectedSpell).getName() + " on";
                                menuText2[menuLength] = "@cya@" + EntityHandler.getDoorDef(i3).getName();
                                menuID[menuLength] = 300;
                                menuActionX[menuLength] = doorX[j2];
                                menuActionY[menuLength] = doorY[j2];
                                menuActionType[menuLength] = doorDirection[j2];
                                menuActionVariable[menuLength] = selectedSpell;
                                menuLength++;
                            }
                        } else if (selectedItem >= 0) {
                            menuText1[menuLength] = "Use " + selectedItemName + " with";
                            menuText2[menuLength] = "@cya@" + EntityHandler.getDoorDef(i3).getName();
                            menuID[menuLength] = 310;
                            menuActionX[menuLength] = doorX[j2];
                            menuActionY[menuLength] = doorY[j2];
                            menuActionType[menuLength] = doorDirection[j2];
                            menuActionVariable[menuLength] = selectedItem;
                            menuLength++;
                        } else {
                            if (!EntityHandler.getDoorDef(i3).getCommand1().equalsIgnoreCase("WalkTo")) {
                                menuText1[menuLength] = EntityHandler.getDoorDef(i3).getCommand1();
                                menuText2[menuLength] = "@cya@" + EntityHandler.getDoorDef(i3).getName();
                                menuID[menuLength] = 320;
                                menuActionX[menuLength] = doorX[j2];
                                menuActionY[menuLength] = doorY[j2];
                                menuActionType[menuLength] = doorDirection[j2];
                                menuLength++;
                            }
                            if (!EntityHandler.getDoorDef(i3).getCommand2().equalsIgnoreCase("Examine")) {
                                menuText1[menuLength] = EntityHandler.getDoorDef(i3).getCommand2();
                                menuText2[menuLength] = "@cya@" + EntityHandler.getDoorDef(i3).getName();
                                menuID[menuLength] = 2300;
                                menuActionX[menuLength] = doorX[j2];
                                menuActionY[menuLength] = doorY[j2];
                                menuActionType[menuLength] = doorDirection[j2];
                                menuLength++;
                            }
                            menuText1[menuLength] = "Examine";
                            menuText2[menuLength] = "@cya@" + EntityHandler.getDoorDef(i3).getName() + ((ourPlayer.admin == 5 || ourPlayer.admin <= 3) ? " @or1@(" + i3 + ":" + (doorX[j2] + areaX) + "," + (doorY[j2] + areaY) + ")" : "");
                            menuID[menuLength] = 3300;
                            menuActionType[menuLength] = i3;
                            menuLength++;
                        }
                        aBooleanArray970[j2] = true;
                    }
                } else if (model != null && model.anInt257 >= 0) {
                    int k2 = model.anInt257;
                    int j3 = objectType[k2];
                    if (!aBooleanArray827[k2]) {
                        if (selectedSpell >= 0) {
                            if (EntityHandler.getSpellDef(selectedSpell).getSpellType() == 5) {
                                menuText1[menuLength] = "Cast " + EntityHandler.getSpellDef(selectedSpell).getName() + " on";
                                menuText2[menuLength] = "@cya@" + EntityHandler.getObjectDef(j3).getName();
                                menuID[menuLength] = 400;
                                menuActionX[menuLength] = objectX[k2];
                                menuActionY[menuLength] = objectY[k2];
                                menuActionType[menuLength] = objectID[k2];
                                menuActionVariable[menuLength] = objectType[k2];
                                menuActionVariable2[menuLength] = selectedSpell;
                                menuLength++;
                            }
                        }
                        else if (selectedItem >= 0) {
                            menuText1[menuLength] = "Use " + selectedItemName + " with";
                            menuText2[menuLength] = "@cya@" + EntityHandler.getObjectDef(j3).getName();
                            menuID[menuLength] = 410;
                            menuActionX[menuLength] = objectX[k2];
                            menuActionY[menuLength] = objectY[k2];
                            menuActionType[menuLength] = objectID[k2];
                            menuActionVariable[menuLength] = objectType[k2];
                            menuActionVariable2[menuLength] = selectedItem;
                            menuLength++;
                        }
                        else {
                            if (!EntityHandler.getObjectDef(j3).getCommand1().equalsIgnoreCase("WalkTo")) {
                                menuText1[menuLength] = EntityHandler.getObjectDef(j3).getCommand1();
                                menuText2[menuLength] = "@cya@" + EntityHandler.getObjectDef(j3).getName();
                                menuID[menuLength] = 420;
                                menuActionX[menuLength] = objectX[k2];
                                menuActionY[menuLength] = objectY[k2];
                                menuActionType[menuLength] = objectID[k2];
                                menuActionVariable[menuLength] = objectType[k2];
                                menuLength++;
                            }
                            if (!EntityHandler.getObjectDef(j3).getCommand2().equalsIgnoreCase("Examine")) {
                                menuText1[menuLength] = EntityHandler.getObjectDef(j3).getCommand2();
                                menuText2[menuLength] = "@cya@" + EntityHandler.getObjectDef(j3).getName();
                                menuID[menuLength] = 2400;
                                menuActionX[menuLength] = objectX[k2];
                                menuActionY[menuLength] = objectY[k2];
                                menuActionType[menuLength] = objectID[k2];
                                menuActionVariable[menuLength] = objectType[k2];
                                menuLength++;
                            }
                            menuText1[menuLength] = "Examine";
                            menuText2[menuLength] = "@cya@" + EntityHandler.getObjectDef(j3).getName() + ((ourPlayer.admin == 5 || ourPlayer.admin <= 3) ? " @or1@(" + j3 + ":" + (objectX[k2] + areaX) + "," + (objectY[k2] + areaY) + ")" : "");
                            menuID[menuLength] = 3400;
                            menuActionType[menuLength] = j3;
                            menuLength++;
                        }
                        aBooleanArray827[k2] = true;
                    }
                }
                else {
                    if (k1 >= 0)
                        k1 = model.anIntArray258[k1] - 0x30d40;
                    if (k1 >= 0)
                        j = k1;
                }
			}
 			if (selectedSpell >= 0 && EntityHandler.getSpellDef(selectedSpell).getSpellType() <= 1) {
				menuText1[menuLength] = "Cast " + EntityHandler.getSpellDef(selectedSpell).getName() + " on self";
				menuText2[menuLength] = "";
				menuID[menuLength] = 1000;
				menuActionType[menuLength] = selectedSpell;
				menuLength++;
			}
			if (j != -1) {
				int l1 = j;
				if (selectedSpell >= 0) {
					if (EntityHandler.getSpellDef(selectedSpell).getSpellType() == 6) {
						menuText1[menuLength] = "Cast " + EntityHandler.getSpellDef(selectedSpell).getName() + " on ground";
						menuText2[menuLength] = "";
						menuID[menuLength] = 900;
						menuActionX[menuLength] = engineHandle.selectedX[l1];
						menuActionY[menuLength] = engineHandle.selectedY[l1];
						menuActionType[menuLength] = selectedSpell;
						menuLength++;
						return;
                }
            } else if (selectedItem < 0 ) {
			    menuText1[menuLength] = "Walk here";
                menuText2[menuLength] = "";
                menuID[menuLength] = 920;
                menuActionX[menuLength] = engineHandle.selectedX[l1];
                menuActionY[menuLength] = engineHandle.selectedY[l1];
                menuLength++;
				if (ourPlayer.admin == 1 || ourPlayer.admin == 3) {
					menuText1[menuLength] = "Teleport here";
					menuText2[menuLength] = "";
					//menuText2[menuLength] = "(" + (engineHandle.selectedX[l1] + areaX) + ", " + (engineHandle.selectedY[l1] + areaY) + ")";
					menuID[menuLength] = 921;
					menuActionX[menuLength] = engineHandle.selectedX[l1];
					menuActionY[menuLength] = engineHandle.selectedY[l1];
					menuLength++;
					/*menuText1[menuLength] = "Create New Entity";
					menuText2[menuLength] = "(" + (engineHandle.selectedX[l1] + areaX) + ", " + (engineHandle.selectedY[l1] + areaY) + ")";
					menuID[menuLength] = 922;
					menuActionX[menuLength] = engineHandle.selectedX[l1];
					menuActionY[menuLength] = engineHandle.selectedY[l1];
					menuLength++;*/					
				}
            } else {
				if (ourPlayer.admin == 1 || ourPlayer.admin == 3) {
					menuText1[menuLength] = "Teleport here";
					menuText2[menuLength] = "";
					//menuText2[menuLength] = "(" + engineHandle.selectedX[l1] + ", " + engineHandle.selectedY[l1] + ")";
					menuID[menuLength] = 921;
					menuActionX[menuLength] = engineHandle.selectedX[l1];
					menuActionY[menuLength] = engineHandle.selectedY[l1];
					menuLength++;
					/*menuText1[menuLength] = "Create New Entity";
					menuText2[menuLength] = "(" + engineHandle.selectedX[l1] + ", " + engineHandle.selectedY[l1] + ")";
					menuID[menuLength] = 922;
					menuActionX[menuLength] = engineHandle.selectedX[l1];
					menuActionY[menuLength] = engineHandle.selectedY[l1];
					menuLength++;*/		
				}
			}
        
		}
	}
	
    protected final void startGame() {
		String sre = new String(raf);
		kfi = Integer.valueOf(sre).intValue();
        int i = 0;
        for (int j = 0; j < 99; j++) {
            int k = j + 1;
            int i1 = (int) ((double) k + 300D * Math.pow(2D, (double) k / 7D));
            i += i1;
            experienceArray[j] = (i & 0xffffffc) / 4;
        }
        super.yOffset = 0;
        GameWindowMiddleMan.maxPacketReadCount = 1000;
        loadConfigFilter();
        if (lastLoadedNull)
            return;
        aGraphics936 = getGraphics();
        changeThreadSleepModifier(50);
        gameGraphics = new GameImageMiddleMan(windowWidth, windowHeight + 12, 4000, this);
        gameGraphics._mudclient = this;
        gameGraphics.setDimensions(0, 0, windowWidth, windowHeight + 12);
        Menu.aBoolean220 = false;
        spellMenu = new Menu(gameGraphics, 5);
        int l = ((GameImage) (gameGraphics)).menuDefaultWidth - 199;
        spellMenuHandle = spellMenu.makeMenuType9(l, 60, 196, 90, 1, 500, true);
        friendsMenu = new Menu(gameGraphics, 5);
        friendsMenuHandle = friendsMenu.makeMenuType9(l, 76, 196, 126, 1, 500, true);
        questMenu = new Menu(gameGraphics, 5);           // X , Y , WIDTH , HEIGHT , TXT TYPE , MAXWIDTH , FLAG
        questMenuHandle = questMenu.makeMenuType9(l, 60, 196, 263, 1, 500, true);
        loadMedia();
        if (lastLoadedNull)
            return;
        loadEntity();
        if (lastLoadedNull)
            return;
        gameCamera = new Camera(gameGraphics, 15000, 15000, 10000);
        gameCamera.setCameraSize(windowWidth / 2, windowHeight / 2, windowWidth / 2, windowHeight / 2, windowWidth, cameraSizeInt);
        gameCamera.zoom1 = 2400;
        gameCamera.zoom2 = 2400;
        gameCamera.zoom3 = 1;
        gameCamera.zoom4 = 2300;
        gameCamera.method303(-50, -10, -50);
        engineHandle = new EngineHandle(gameCamera, gameGraphics);
        loadTextures();
        if (lastLoadedNull)
            return;
        loadModels();
        if (lastLoadedNull) {
			System.out.println("models in DATA_DIR doesn't exist... RSCEmulation will now close.");
			System.out.println("Please post a topic in the \"Support\" forum section.\n");
            System.exit(-1);
		}
        loadSounds();
        if (lastLoadedNull)
            return;
        drawLoadingBarText(100, "Starting game...");
		acceptKeyboardInput = true;
		gameGraphics.finishedLoadingSprites();
        drawGameMenu();
        makeLoginMenus();
        makeCharacterDesignMenu();
		setupLoginScreenCamera();
    }
    
	private final void loadSprite(int id, int amount) {
		for(int i = id;i < id + amount;i++) {
			if(!gameGraphics.loadSprite(i)) {
				lastLoadedNull = true;
				return;
			}
		}
	}
	
	private final void loadMedia() {
		//drawLoadingBarText(30, "Unpacking Configuration - 0%");
		loadSprite(SPRITE_MEDIA_START, 1);
		loadSprite(SPRITE_MEDIA_START + 1, 6);
		loadSprite(SPRITE_MEDIA_START + 9, 1);
		loadSprite(SPRITE_MEDIA_START + 10,  1);
		loadSprite(SPRITE_MEDIA_START + 11, 3);
		loadSprite(SPRITE_MEDIA_START + 14, 8);
		loadSprite(SPRITE_MEDIA_START + 22,  1);
		loadSprite(2023, 1);
		loadSprite(SPRITE_MEDIA_START + 24, 1);
		loadSprite(SPRITE_MEDIA_START + 25, 2);
		loadSprite(SPRITE_UTIL_START, 2);
		loadSprite(SPRITE_UTIL_START + 2, 4);
		loadSprite(SPRITE_UTIL_START + 6, 2);
		loadSprite(SPRITE_PROJECTILE_START, 7);
		loadSprite(SPRITE_LOGO_START, 1);
		
		int i = EntityHandler.invPictureCount();
		for (int j = 1;i > 0;j++) {
			int k = i;
			i -= 30;
			if (k > 30) {
				k = 30;
			}
			loadSprite(SPRITE_ITEM_START + (j - 1) * 30, k);
		}
	}
    
	private final void loadEntity() {
		drawLoadingBarText(60, "Unpacking 3d models - 0%");
		int animationNumber = 0;
		label0: for (int animationIndex = 0; animationIndex < EntityHandler.animationCount(); animationIndex++) {
			String s = EntityHandler.getAnimationDef(animationIndex).getName();
			for (int nextAnimationIndex = 0; nextAnimationIndex < animationIndex; nextAnimationIndex++) {
				if (!EntityHandler.getAnimationDef(nextAnimationIndex).getName().equalsIgnoreCase(s))
					continue;
				EntityHandler.getAnimationDef(animationIndex).number = EntityHandler.getAnimationDef(nextAnimationIndex).getNumber();
				continue label0;
			}
			
			loadSprite(animationNumber, 15);
			if (EntityHandler.getAnimationDef(animationIndex).hasA())
				loadSprite(animationNumber + 15, 3);
			
			if (EntityHandler.getAnimationDef(animationIndex).hasF())
				loadSprite(animationNumber + 18, 9);
			EntityHandler.getAnimationDef(animationIndex).number = animationNumber;
			animationNumber += 27;
		}
	}
    
	private final void loadTextures() {
		//drawLoadingBarText(75, "Unpacking members map - 0%");
		gameCamera.method297(EntityHandler.textureCount(), 7, 11);
		for (int i = 0; i < EntityHandler.textureCount(); i++) {
			loadSprite(SPRITE_TEXTURE_START + i, 1);
			Sprite sprite = ((GameImage) (gameGraphics)).sprites[SPRITE_TEXTURE_START + i];
			
			int length = sprite.getWidth() * sprite.getHeight();
			int[] pixels = sprite.getPixels();
			int ai1[] = new int[32768];
			for (int k = 0; k < length; k++)
				ai1[((pixels[k] & 0xf80000) >> 9) + ((pixels[k] & 0xf800) >> 6) + ((pixels[k] & 0xf8) >> 3)]++;
			int[] dictionary = new int[256];
			dictionary[0] = 0xff00ff;
			int[] temp = new int[256];
			for (int i1 = 0; i1 < ai1.length; i1++) {
				int j1 = ai1[i1];
				if (j1 > temp[255]) {
					for (int k1 = 1; k1 < 256; k1++) {
						if (j1 <= temp[k1])
							continue;
						for (int i2 = 255; i2 > k1; i2--) {
							dictionary[i2] = dictionary[i2 - 1];
							temp[i2] = temp[i2 - 1];
						}
						dictionary[k1] = ((i1 & 0x7c00) << 9) + ((i1 & 0x3e0) << 6) + ((i1 & 0x1f) << 3) + 0x40404;
						temp[k1] = j1;
						break;
					}
				}
				ai1[i1] = -1;
			}
			byte[] indices = new byte[length];
			for (int l1 = 0; l1 < length; l1++) {
				int j2 = pixels[l1];
				int k2 = ((j2 & 0xf80000) >> 9) + ((j2 & 0xf800) >> 6) + ((j2 & 0xf8) >> 3);
				int l2 = ai1[k2];
				if (l2 == -1) {
					int i3 = 0x3b9ac9ff;
					int j3 = j2 >> 16 & 0xff;
					int k3 = j2 >> 8 & 0xff;
					int l3 = j2 & 0xff;
					for (int i4 = 0; i4 < 256; i4++) {
						int j4 = dictionary[i4];
						int k4 = j4 >> 16 & 0xff;
						int l4 = j4 >> 8 & 0xff;
						int i5 = j4 & 0xff;
						int j5 = (j3 - k4) * (j3 - k4) + (k3 - l4) * (k3 - l4) + (l3 - i5) * (l3 - i5);
						if (j5 < i3) {
							i3 = j5;
							l2 = i4;
						}
					}
					
					ai1[k2] = l2;
				}
				indices[l1] = (byte) l2;
			}
			gameCamera.method298(i, indices, dictionary, sprite.getSomething1() / 64 - 1);
		}
	}

    private final void checkMouseStatus() {
        if (selectedSpell >= 0 || selectedItem >= 0) {
            menuText1[menuLength] = "Cancel";
            menuText2[menuLength] = "";
            menuID[menuLength] = 4000;
            menuLength++;
        }
        for (int i = 0; i < menuLength; i++)
            menuIndexes[i] = i;

        for (boolean flag = false; !flag;) {
            flag = true;
            for (int j = 0; j < menuLength - 1; j++) {
                int l = menuIndexes[j];
                int j1 = menuIndexes[j + 1];
                if (menuID[l] > menuID[j1]) {
                    menuIndexes[j] = j1;
                    menuIndexes[j + 1] = l;
                    flag = false;
                }
            }

        }

        if (menuLength > 20)
            menuLength = 20;
        if (menuLength > 0) {
            int k = -1;
            for (int i1 = 0; i1 < menuLength; i1++) {
                if (menuText2[menuIndexes[i1]] == null || menuText2[menuIndexes[i1]].length() <= 0)
                    continue;
                k = i1;
                break;
            }
            String s = null;
            if ((selectedItem >= 0 || selectedSpell >= 0) && menuLength == 1)
                s = "Choose a target";
            else if ((selectedItem >= 0 || selectedSpell >= 0) && menuLength > 1)
                s = "@whi@" + menuText1[menuIndexes[0]] + " " + menuText2[menuIndexes[0]];
            else if (k != -1)
                s = menuText2[menuIndexes[k]] + ": @whi@" + menuText1[menuIndexes[0]];
            if (menuLength == 2 && s != null)
                s = s + "@whi@ / 1 more option";
            if (menuLength > 2 && s != null)
                s = s + "@whi@ / " + (menuLength - 1) + " more options";
            if (s != null)
                gameGraphics.drawString(s, 6, 14, 1, 0xffff00);
            if (!configMouseButtons && mouseButtonClick == 1 || configMouseButtons && mouseButtonClick == 1 && menuLength == 1) {
                menuClick(menuIndexes[0]);
                mouseButtonClick = 0;
                return;
            }
            if (!configMouseButtons && mouseButtonClick == 2 || configMouseButtons && mouseButtonClick == 1) {
                menuHeight = (menuLength + 1) * 15;
                menuWidth = gameGraphics.textWidth("Choose option", 1) + 5;
                for (int k1 = 0; k1 < menuLength; k1++) {
                    int l1 = gameGraphics.textWidth(menuText1[k1] + " " + menuText2[k1], 1) + 5;
                    if (l1 > menuWidth)
                        menuWidth = l1;
                }

                menuX = super.mouseX - menuWidth / 2;
                menuY = super.mouseY - 7;
                showRightClickMenu = true;
                if (menuX < 0)
                    menuX = 5;
                if (menuY < 0)
                    menuY = 5;
                if (menuX + menuWidth > gameWidth)
                    menuX = gameWidth - menuWidth - 5;
                if (menuY + menuHeight > gameHeight)
                    menuY = gameHeight - menuHeight - 19;
                mouseButtonClick = 0;
            }
        }
    }

    protected final void cantLogout() {
        logoutTimeout = 0;
        displayMessage("@cya@Sorry, you can't logout at the moment", 3, 0);
    }
	
	private final void drawFriendsWindowNoClan(boolean flag) {
        int i = ((GameImage) (gameGraphics)).menuDefaultWidth - 199;
        int j = 36;
        gameGraphics.drawPicture(i - 49, 3, SPRITE_MEDIA_START + 5);
        char c = '\304';
        char c1 = '\266';
        int l;
        int k = l = GameImage.convertRGBToLong(160, 160, 160);
        if (anInt981 == 0)
            k = GameImage.convertRGBToLong(220, 220, 220);
        else
            l = GameImage.convertRGBToLong(220, 220, 220);
        drawBoxAlpha(i, j, c / 2, 24, k, 128);
        drawBoxAlpha(i + c / 2, j, c / 2, 24, l, 128);
        drawBoxAlpha(i, j + 24, c, c1 - 24, GameImage.convertRGBToLong(220, 220, 220), 128);
        drawLineX(i, j + 24, c, 0);
        drawLineY(i + c / 2, j, 24, 0);
        drawLineX(i, (j + c1) - 16, c, 0);
        drawText("Friends", i + c / 4, j + 16, 4, 0);
        drawText("Ignore", i + c / 4 + c / 2, j + 16, 4, 0);
        friendsMenu.resetListTextCount(friendsMenuHandle);
		String injector = "~" + (gameWidth - 73) + "~";
        if (anInt981 == 0) {
            for (int i1 = 0; i1 < super.friendsCount; i1++) {
                String s;
                if (super.friendsListOnlineStatus[i1] > 0)
                    s = "@gre@";
                else
                    s = "@red@";
                friendsMenu.drawMenuListText(friendsMenuHandle, i1, s + DataOperations.longToString(super.friendsListLongs[i1]) + injector + "@whi@Remove                      WWWWWWWWWW");
            }

        }
        if (anInt981 == 1) {
            for (int j1 = 0; j1 < super.ignoreListCount; j1++)
                friendsMenu.drawMenuListText(friendsMenuHandle, j1, "@yel@" + DataOperations.longToString(super.ignoreListLongs[j1]) + injector + "@whi@Remove                   WWWWWWWWWW");

        }
        friendsMenu.drawMenu();
        if (anInt981 == 0) {
            int k1 = friendsMenu.selectedListIndex(friendsMenuHandle);
            if (k1 >= 0 && super.mouseX < gameWidth - 20) {
                if (super.mouseX > gameWidth - 75)
                	gameGraphics.drawString("@yel@Click to remove " + DataOperations.longToString(super.friendsListLongs[k1]), 6, 14, 1, 0xffff00);
                else if (super.friendsListOnlineStatus[k1] > 0)
                	gameGraphics.drawString("@yel@Click to message " + DataOperations.longToString(super.friendsListLongs[k1]), 6, 14, 1, 0xffff00);
				else
					gameGraphics.drawString("@yel@" + DataOperations.longToString(super.friendsListLongs[k1]) + " is offline", 6, 14, 1, 0xffff00);
            }
			gameGraphics.drawText("Click a name to send a message", i + c / 2, j + 35, 1, 0xffffff);
            int k2;
            if (super.mouseX > i && super.mouseX < i + c && super.mouseY > (j + c1) - 16 && super.mouseY < j + c1)
                k2 = 0xffff00;
            else
                k2 = 0xffffff;
			gameGraphics.drawText("Click here to add a friend", i + c / 2, (j + c1) - 3, 1, k2);
        }
        if (anInt981 == 1) {
            int l1 = friendsMenu.selectedListIndex(friendsMenuHandle);
            if (l1 >= 0 && super.mouseX < gameWidth - 20) {
                if (super.mouseX > gameWidth - 75)
                	gameGraphics.drawString("@yel@Click to remove " + DataOperations.longToString(super.friendsListLongs[l1]), 6, 14, 1, 0xffff00);
				else
					gameGraphics.drawString("@yel@Ignoring " + DataOperations.longToString(super.friendsListLongs[l1]), 6, 14, 1, 0xffff00);
            }            
            if (l1 >= 0 && super.mouseX < gameWidth - 10 && super.mouseX > gameWidth - 60) {
                if (super.mouseX > gameWidth - 75)
                	gameGraphics.drawString("@yel@Click to remove " + DataOperations.longToString(super.friendsListLongs[l1]), 6, 14, 1, 0xffff00);
            }
			gameGraphics.drawText("Blocking messages from:", i + c / 2, j + 35, 1, 0xffffff);
            int l2;
            if (super.mouseX > i && super.mouseX < i + c && super.mouseY > (j + c1) - 16 && super.mouseY < j + c1)
                l2 = 0xffff00;
            else
                l2 = 0xffffff;
            drawText("Click here to add a name", i + c / 2, (j + c1) - 3, 1, l2);
        }
        if (!flag)
            return;
        i = super.mouseX - (((GameImage) (gameGraphics)).menuDefaultWidth - 199);
        j = super.mouseY - 36;
        if (i >= 0 && j >= 0 && i < 196 && j < 182) {
            friendsMenu.updateActions(i + (((GameImage) (gameGraphics)).menuDefaultWidth - 199), j + 36, super.lastMouseDownButton, super.mouseDownButton);
            if (j <= 24 && mouseButtonClick == 1)
                if (i < 98 && anInt981 == 1) {
                    anInt981 = 0;
                    friendsMenu.method165(friendsMenuHandle, 0);
                } else if (i > 98 && anInt981 == 0) {
                    anInt981 = 1;
                    friendsMenu.method165(friendsMenuHandle, 0);
                }
            if (mouseButtonClick == 1 && anInt981 == 0) {
                int i2 = friendsMenu.selectedListIndex(friendsMenuHandle);
                if (i2 >= 0 && super.mouseX < gameWidth - 20)
                    if (super.mouseX > gameWidth - 75)
                        removeFromFriends(super.friendsListLongs[i2]);
                    else if (super.friendsListOnlineStatus[i2] != 0) {
                        inputBoxType = 2;
                        privateMessageTarget = super.friendsListLongs[i2];
                        super.inputMessage = "";
                        super.enteredMessage = "";
                    }
            }
            if (mouseButtonClick == 1 && anInt981 == 1) {
                int j2 = friendsMenu.selectedListIndex(friendsMenuHandle);
                if (j2 >= 0 && super.mouseX < gameWidth - 20 && super.mouseX > gameWidth - 75)
                    removeFromIgnoreList(super.ignoreListLongs[j2]);
            }
            if (j > 166 && mouseButtonClick == 1 && anInt981 == 0) {
                inputBoxType = 1;
                super.inputText = "";
                super.enteredText = "";
            }
            if (j > 166 && mouseButtonClick == 1 && anInt981 == 1) {
                inputBoxType = 3;
                super.inputText = "";
                super.enteredText = "";
            }
            mouseButtonClick = 0;
        }
	}
	
    private final void drawFriendsWindowWithClan(boolean flag) {
        int i = ((GameImage) (gameGraphics)).menuDefaultWidth - 199;
        int j = 36;
        gameGraphics.drawPicture(i - 49, 3, SPRITE_MEDIA_START + 5);
        char c = '\304';
        char c1 = '\266';
        int l, m;
        int k = m = l = GameImage.convertRGBToLong(160, 160, 160);
        if (anInt981 == 0)
            k = GameImage.convertRGBToLong(220, 220, 220);
        else if(anInt981 == 1)
            l = GameImage.convertRGBToLong(220, 220, 220);
		else
			m = GameImage.convertRGBToLong(220, 220, 220);
        drawBoxAlpha(i, j, c / 3, 24, k, 128);
        drawBoxAlpha(i + c / 3, j, c / 3, 24, l, 128);
		drawBoxAlpha(i + (2 * c / 3), j, c / 3, 24, m, 128);
        drawBoxAlpha(i, j + 24, c, c1 - 24, GameImage.convertRGBToLong(220, 220, 220), 128);
        drawLineX(i, j + 24, c, 0);
        drawLineY(i + c / 3, j, 24, 0);
		drawLineY(i + (2 * c / 3), j, 24, 0);
        drawLineX(i, (j + c1) - 16, c, 0);
        drawText("Friends", i + c / 5 - 6, j + 16, 4, 0);
        drawText("Ignore", i + c / 5 + c / 3 - 6, j + 16, 4, 0);
		drawText("Clan", i + c / 5 + 2 * c / 3 - 9, j + 16, 4, 0);
        friendsMenu.resetListTextCount(friendsMenuHandle);
		String injector = "~" + (gameWidth - 73) + "~";
        if (anInt981 == 0) {
            for (int i1 = 0; i1 < super.friendsCount; i1++) {
                String s;
                if (super.friendsListOnlineStatus[i1] > 0)
                    s = "@gre@";
                else
                    s = "@red@";
                friendsMenu.drawMenuListText(friendsMenuHandle, i1, s + DataOperations.longToString(super.friendsListLongs[i1]) + injector + "@whi@Remove                      WWWWWWWWWW");
            }

        }
        if (anInt981 == 1) {
            for (int j1 = 0; j1 < super.ignoreListCount; j1++)
                friendsMenu.drawMenuListText(friendsMenuHandle, j1, "@yel@" + DataOperations.longToString(super.ignoreListLongs[j1]) + injector + "@whi@Remove                   WWWWWWWWWW");

        }
		if (anInt981 == 2) {
			for (int idx = 0; idx < super.clanCount; idx++) {
				String s;
				if (super.clanListOnlineStatus[idx] > 0)
					s = "@gre@";
				else
					s = "@red@";
				friendsMenu.drawMenuListText(friendsMenuHandle, idx, s + DataOperations.longToString(super.clanListLongs[idx]));
			}
		}
        friendsMenu.drawMenu();
        if (anInt981 == 0) {
            int k1 = friendsMenu.selectedListIndex(friendsMenuHandle);
            if (k1 >= 0 && super.mouseX < gameWidth - 20) {
                if (super.mouseX > gameWidth - 75)
                    drawText("Remove " + DataOperations.longToString(super.friendsListLongs[k1]), i + c / 2, j + 35, 1, 0xffffff);
                else if (super.friendsListOnlineStatus[k1] > 0)
                    drawText("Message " + DataOperations.longToString(super.friendsListLongs[k1]), i + c / 2, j + 35, 1, 0xffffff);
				else
                    drawText(DataOperations.longToString(super.friendsListLongs[k1]) + " is @red@offline", i + c / 2, j + 35, 1, 0xffffff);
            }
            int k2;
            if (super.mouseX > i && super.mouseX < i + c && super.mouseY > (j + c1) - 16 && super.mouseY < j + c1)
                k2 = 0xffff00;
            else
                k2 = 0xffffff;
            drawText("Enter name to add to friends list", i + c / 2, (j + c1) - 3, 1, k2);
        }
        if (anInt981 == 1) {
            int l1 = friendsMenu.selectedListIndex(friendsMenuHandle);
            if (l1 >= 0 && super.mouseX < gameWidth - 10 && super.mouseX > gameWidth - 60) {
                if (super.mouseX > gameWidth - 75)
                    drawText("Remove " + DataOperations.longToString(super.ignoreListLongs[l1]), i + c / 2, j + 35, 1, 0xffffff);
            }
            int l2;
            if (super.mouseX > i && super.mouseX < i + c && super.mouseY > (j + c1) - 16 && super.mouseY < j + c1)
                l2 = 0xffff00;
            else
                l2 = 0xffffff;
            drawText("Add Ignore", i + c / 2, (j + c1) - 3, 1, l2);
        }
		if (anInt981 == 2) {
			drawText("Clan " + ourPlayer.cName.toUpperCase(), i + c / 2, (j + c1) - 3, 1, 0xffffff);
			int idx = friendsMenu.selectedListIndex(friendsMenuHandle);
            if (idx >= 0 && super.mouseX < gameWidth - 20) {
                if (super.clanListOnlineStatus[idx] > 0)
                    drawText("Message " + DataOperations.longToString(super.clanListLongs[idx]), i + c / 2, j + 35, 1, 0xffffff);
				else
                    drawText(DataOperations.longToString(super.clanListLongs[idx]) + " is @red@offline", i + c / 2, j + 35, 1, 0xffffff);
            }
		}
        if (!flag)
            return;
        i = super.mouseX - (((GameImage) (gameGraphics)).menuDefaultWidth - 199);
        j = super.mouseY - 36;
        if (i >= 0 && j >= 0 && i < 196 && j < 182) {
            friendsMenu.updateActions(i + (((GameImage) (gameGraphics)).menuDefaultWidth - 199), j + 36, super.lastMouseDownButton, super.mouseDownButton);
            if (j <= 24 && mouseButtonClick == 1)
                if (i < 65 && (anInt981 == 1 || anInt981 == 2)) {
                    anInt981 = 0;
                    friendsMenu.method165(friendsMenuHandle, 0);
                } else if (i > 65 && i < 130 && (anInt981 == 0 || anInt981 == 2)) {
                    anInt981 = 1;
                    friendsMenu.method165(friendsMenuHandle, 0);
                }  else if(i > 130 && (anInt981 == 0 || anInt981 == 1)) {
					anInt981 = 2;
					friendsMenu.method165(friendsMenuHandle, 0);
				}
            if (mouseButtonClick == 1) {
				if(anInt981 == 0) {
					int i2 = friendsMenu.selectedListIndex(friendsMenuHandle);
					if (i2 >= 0 && super.mouseX < gameWidth - 20)
						if (super.mouseX > gameWidth - 75)
							removeFromFriends(super.friendsListLongs[i2]);
						else if (super.friendsListOnlineStatus[i2] != 0) {
							inputBoxType = 2;
							privateMessageTarget = super.friendsListLongs[i2];
							super.inputMessage = "";
							super.enteredMessage = "";
						}
				} else if(anInt981 == 2) {
					int idx = friendsMenu.selectedListIndex(friendsMenuHandle);
					if (idx > -1 && super.clanListOnlineStatus[idx] != 0) {
						inputBoxType = 2;
						privateMessageTarget = super.clanListLongs[idx];
						super.inputMessage = "";
						super.enteredMessage = "";
					}
				}
            }
            if (mouseButtonClick == 1 && anInt981 == 1) {
                int j2 = friendsMenu.selectedListIndex(friendsMenuHandle);
                if (j2 >= 0 && super.mouseX < gameWidth - 20 && super.mouseX > gameWidth - 75)
                    removeFromIgnoreList(super.ignoreListLongs[j2]);
            }
            if (j > 166 && mouseButtonClick == 1 && anInt981 == 0) {
                inputBoxType = 1;
                super.inputText = "";
                super.enteredText = "";
            }
            if (j > 166 && mouseButtonClick == 1 && anInt981 == 1) {
                inputBoxType = 3;
                super.inputText = "";
                super.enteredText = "";
            }
			if (j > 166 && mouseButtonClick == 1 && anInt981 == 2) {
				//Later on, make a popup containing all of the clan's statistics, leader information, etc
				//Possibly also an interface for joining, leaving, rank changes, etc...but for now this will be good enough
			}
            mouseButtonClick = 0;
        }
    }

 private final boolean loadSection(int i, int j) {
        if (playerAliveTimeout != 0) {
            engineHandle.playerIsAlive = false;
            return false;
        }
        notInWilderness = false;
        i += wildX;
        j += wildY;
        if (lastWildYSubtract == wildYSubtract && i > anInt789 && i < anInt791 && j > anInt790 && j < anInt792) {
            engineHandle.playerIsAlive = true;
            return false;
        }
        drawText("Loading... Please wait", gameWidth / 2, gameHeight / 2, 1, 0xffffff);
        drawChatMessageTabs();
        gameGraphics.drawImage(aGraphics936, 0, 0);
        int k = areaX;
        int l = areaY;
        int i1 = (i + 24) / 48;
        int j1 = (j + 24) / 48;
        lastWildYSubtract = wildYSubtract;
        areaX = i1 * 48 - 48;
        areaY = j1 * 48 - 48;
        anInt789 = i1 * 48 - 32;
        anInt790 = j1 * 48 - 32;
        anInt791 = i1 * 48 + 32;
        anInt792 = j1 * 48 + 32;
        engineHandle.method401(i, j, lastWildYSubtract);
        areaX -= wildX;
        areaY -= wildY;
        int k1 = areaX - k;
        int l1 = areaY - l;
        for (int i2 = 0; i2 < objectCount; i2++) {
            objectX[i2] -= k1;
            objectY[i2] -= l1;
            int j2 = objectX[i2];
            int l2 = objectY[i2];
            int k3 = objectType[i2];
            int m4 = objectID[i2];
            Model model = objectModelArray[i2];
            try {
                int l4 = objectID[i2];
                int k5;
                int i6;
                if (l4 == 0 || l4 == 4) {
                    k5 = EntityHandler.getObjectDef(k3).getWidth();
                    i6 = EntityHandler.getObjectDef(k3).getHeight();
                } else {
                    i6 = EntityHandler.getObjectDef(k3).getWidth();
                    k5 = EntityHandler.getObjectDef(k3).getHeight();
                }
                int j6 = ((j2 + j2 + k5) * 128) / 2;
                int k6 = ((l2 + l2 + i6) * 128) / 2;
                if (j2 >= 0 && l2 >= 0 && j2 < 96 && l2 < 96) {
                    gameCamera.addModel(model);
                    model.method191(j6, -engineHandle.getAveragedElevation(j6, k6), k6);
                    engineHandle.method412(j2, l2, k3, m4);
                    if (k3 == 74)
                        model.method190(0, -480, 0);
                }
            } catch (RuntimeException runtimeexception) {
                System.out.println(runtimeexception.getMessage());
                runtimeexception.printStackTrace();
            }
        }

        for (int k2 = 0; k2 < doorCount; k2++) {
            doorX[k2] -= k1;
            doorY[k2] -= l1;
            int i3 = doorX[k2];
            int l3 = doorY[k2];
            int j4 = doorType[k2];
            int i5 = doorDirection[k2];
            try {
                engineHandle.method408(i3, l3, i5, j4);
                Model model_1 = makeModel(i3, l3, i5, j4, k2);
                doorModel[k2] = model_1;
            } catch (RuntimeException runtimeexception1) {
                System.out.println(runtimeexception1.getMessage());
                runtimeexception1.printStackTrace();
            }
        }
	
        for (int j3 = 0; j3 < groundItemCount; j3++) {
            groundItemX[j3] -= k1;
            groundItemY[j3] -= l1;
        }

        for (int i4 = 0; i4 < playerCount; i4++) {
            Mob mob = playerArray[i4];
            mob.currentX -= k1 * 128;
            mob.currentY -= l1 * 128;
            for (int j5 = 0; j5 <= mob.waypointCurrent; j5++) {
                mob.waypointsX[j5] -= k1 * 128;
                mob.waypointsY[j5] -= l1 * 128;
            }

        }

        for (int k4 = 0; k4 < npcCount; k4++) {
            Mob mob_1 = npcArray[k4];
            mob_1.currentX -= k1 * 128;
            mob_1.currentY -= l1 * 128;
            for (int l5 = 0; l5 <= mob_1.waypointCurrent; l5++) {
                mob_1.waypointsX[l5] -= k1 * 128;
                mob_1.waypointsY[l5] -= l1 * 128;
            }

        }

        engineHandle.playerIsAlive = true;
        return true;
    }

    @SuppressWarnings("unchecked")
	private final void drawMagicWindow(boolean flag) {
        int i = ((GameImage) (gameGraphics)).menuDefaultWidth - 199;
        int j = 36;
        gameGraphics.drawPicture(i - 49, 3, SPRITE_MEDIA_START + 4);
        char c = '\304';
        char c1 = '\266';
        int l;
        int k = l = GameImage.convertRGBToLong(160, 160, 160);
        if (menuMagicPrayersSelected == 0)
            k = GameImage.convertRGBToLong(220, 220, 220);
        else
            l = GameImage.convertRGBToLong(220, 220, 220);
        drawBoxAlpha(i, j, c / 2, 24, k, 128);
        drawBoxAlpha(i + c / 2, j, c / 2, 24, l, 128);
        drawBoxAlpha(i, j + 24, c, 90, GameImage.convertRGBToLong(220, 220, 220), 128);
        drawBoxAlpha(i, j + 24 + 90, c, c1 - 90 - 24, GameImage.convertRGBToLong(160, 160, 160), 128);
        drawLineX(i, j + 24, c, 0);
        drawLineY(i + c / 2, j, 24, 0);
        drawLineX(i, j + 113, c, 0);
        drawText("Magic", i + c / 4, j + 16, 4, 0);
        drawText("Prayers", i + c / 4 + c / 2, j + 16, 4, 0);
        if (menuMagicPrayersSelected == 0) {
            spellMenu.resetListTextCount(spellMenuHandle);
            int i1 = 0;
            for (int spellIndex = 0; spellIndex < EntityHandler.spellCount(); spellIndex++) {
            	if (spellIndex + 1 != EntityHandler.spellCount() || playerStatBase[18] > 1) { // Runecrafting
	                String s = "@yel@";
	                for (Entry e : EntityHandler.getSpellDef(spellIndex).getRunesRequired()) {
	                	if (hasRequiredRunes((Integer)e.getKey(), (Integer)e.getValue()))
	                		continue;
	                	s = "@whi@";
	                	break;
	                }
	                int spellLevel = playerStatCurrent[6];
	                if (EntityHandler.getSpellDef(spellIndex).getReqLevel() > spellLevel)
	                    s = "@bla@";
	                spellMenu.drawMenuListText(spellMenuHandle, i1++, s + "Level " + EntityHandler.getSpellDef(spellIndex).getReqLevel() + ": " + EntityHandler.getSpellDef(spellIndex).getName());
            	} // Runecrafting
            }

            spellMenu.drawMenu();
            int selectedSpellIndex = spellMenu.selectedListIndex(spellMenuHandle);
            if (selectedSpellIndex != -1) {
                drawString("Level " + EntityHandler.getSpellDef(selectedSpellIndex).getReqLevel() + ": " + EntityHandler.getSpellDef(selectedSpellIndex).getName(), i + 2, j + 124, 1, 0xffff00);
                drawString(EntityHandler.getSpellDef(selectedSpellIndex).getDescription(), i + 2, j + 136, 0, 0xffffff);
                int i4 = 0;
                for(Entry<Integer, Integer> e : EntityHandler.getSpellDef(selectedSpellIndex).getRunesRequired()) {
                    int runeID = e.getKey();
                    gameGraphics.drawPicture(i + 2 + i4 * 44, j + 150, SPRITE_ITEM_START + EntityHandler.getItemDef(runeID).getSprite());
                    int runeInvCount = inventoryCount(runeID);
                    int runeCount = e.getValue();
                    String s2 = "@red@";
                    if (hasRequiredRunes(runeID, runeCount))
                        s2 = "@gre@";
                    drawString(s2 + runeInvCount + "/" + runeCount, i + 2 + i4 * 44, j + 150, 1, 0xffffff);
                    i4++;
                }
            } else
                drawString("Point at a spell for a description", i + 2, j + 124, 1, 0);
        }
        if (menuMagicPrayersSelected == 1) {
            spellMenu.resetListTextCount(spellMenuHandle);
            int j1 = 0;
            for (int j2 = 0; j2 < EntityHandler.prayerCount(); j2++) {
                String s1 = "@whi@";
                if (EntityHandler.getPrayerDef(j2).getReqLevel() > playerStatBase[5])
                    s1 = "@bla@";
                if (prayerOn[j2])
                    s1 = "@gre@";
                spellMenu.drawMenuListText(spellMenuHandle, j1++, s1 + "Level " + EntityHandler.getPrayerDef(j2).getReqLevel() + ": " + EntityHandler.getPrayerDef(j2).getName());
            }
            spellMenu.drawMenu();
            int j3 = spellMenu.selectedListIndex(spellMenuHandle);
            if (j3 != -1) {
                drawText("Level " + EntityHandler.getPrayerDef(j3).getReqLevel() + ": " + EntityHandler.getPrayerDef(j3).getName(), i + c / 2, j + 130, 1, 0xffff00);
                drawText(EntityHandler.getPrayerDef(j3).getDescription(), i + c / 2, j + 145, 0, 0xffffff);
                drawText("Drain rate: " + EntityHandler.getPrayerDef(j3).getDrainRate(), i + c / 2, j + 160, 1, 0);
            } else
                drawString("Point at a prayer for a description", i + 2, j + 124, 1, 0);
        }
        if (!flag)
            return;
        i = super.mouseX - (((GameImage) (gameGraphics)).menuDefaultWidth - 199);
        j = super.mouseY - 36;
        if (i >= 0 && j >= 0 && i < 196 && j < 182) {
            spellMenu.updateActions(i + (((GameImage) (gameGraphics)).menuDefaultWidth - 199), j + 36, super.lastMouseDownButton, super.mouseDownButton);
            if (j <= 24 && mouseButtonClick == 1)
                if (i < 98 && menuMagicPrayersSelected == 1) { //SWITCHES TABS
                    menuMagicPrayersSelected = 0;
                    prayerMenuIndex = spellMenu.getMenuIndex(spellMenuHandle);
                    spellMenu.method165(spellMenuHandle, LCYV_mudclient_SHKE_7);
                } else if (i > 98 && menuMagicPrayersSelected == 0) {
                    menuMagicPrayersSelected = 1;
                    LCYV_mudclient_SHKE_7 = spellMenu.getMenuIndex(spellMenuHandle);
                    spellMenu.method165(spellMenuHandle, prayerMenuIndex);
                }
            if (mouseButtonClick == 1 && menuMagicPrayersSelected == 0) {
                int k1 = spellMenu.selectedListIndex(spellMenuHandle);
                if (k1 != -1) {
                    int k2 = playerStatCurrent[6];
                    if (EntityHandler.getSpellDef(k1).getReqLevel() > k2) {
                        displayMessage("Your magic ability is not high enough for this spell", 3, 0);
                    } else {
                        int k3 = 0;
                        for (Entry<Integer, Integer> e : EntityHandler.getSpellDef(k1).getRunesRequired()) {
                            if (!hasRequiredRunes(e.getKey(), e.getValue())) {
	                            displayMessage("You don't have all the reagents you need for this spell", 3, 0);
	                            k3 = -1;
	                            break;
                            }
							k3++;
                        }
                        if (k3 == EntityHandler.getSpellDef(k1).getRuneCount()) {
                            selectedSpell = k1;
                            selectedItem = -1;
                        }
                    }
                }
            }
            if (mouseButtonClick == 1 && menuMagicPrayersSelected == 1) {
                int l1 = spellMenu.selectedListIndex(spellMenuHandle);
                if (l1 != -1) {
                    int l2 = playerStatBase[5];
                    if (EntityHandler.getPrayerDef(l1).getReqLevel() > l2)
                        displayMessage("Your prayer ability is not high enough for this prayer", 3, 0);
                    else if (playerStatCurrent[5] == 0)
                        displayMessage("You have run out of prayer points. Return to a church to recharge", 3, 0);
                    else if (prayerOn[l1]) {
                        super.streamClass.createPacket(23);
                        super.streamClass.addByte(l1);
                        super.streamClass.formatPacket();
                        prayerOn[l1] = false;
                        playSound("prayeroff", false);
                    } else {
                        super.streamClass.createPacket(22);
                        super.streamClass.addByte(l1);
                        super.streamClass.formatPacket();
                        prayerOn[l1] = true;
                        playSound("prayeron", false);
                    }
                }
            }
            mouseButtonClick = 0;
        }
    }
	
    protected final void handleMenuKeyDown(boolean shift, boolean ctrl, boolean action, int key, char keyChar) {
    	if (acceptKeyboardInput) {
			switch(key) {
				case 86:
					if (ctrl) {
						Transferable t = Toolkit.getDefaultToolkit()
								.getSystemClipboard().getContents(null);
	
						try {
							if (t != null
									&& t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
								String s = (String) t
										.getTransferData(DataFlavor.stringFlavor);
								for (int i = 0; i < s.length(); i++) {
									if (inputBoxType == 0 && showAbuseWindow == 0) {
										gameMenu.keyDown(s.charAt(i), s.charAt(i));
									} else {
										boolean validKeyDown = GameWindow.isKeyValid(s.charAt(i));
										if (validKeyDown && inputText.length() < 20)
											inputText += s.charAt(i);
										if (validKeyDown && inputMessage.length() < 80)
											inputMessage += s.charAt(i);
									}
								}
							}
						} catch (UnsupportedFlavorException e) {
							// No reasonable way to handle this, as it should never happen
						} catch (IOException e) {
							// No reasonable way to handle this, as it should never happen
						}
					}
					break;
					case 27: // Escape
						if (inputBoxType != 0) {
							inputBoxType = 0;
							break;
						}						
						if (sleeping){
							sleeping = false;
							resetPrivateMessageStrings();
							ignoreNext = false;
							super.streamClass.createPacket(70);
							super.streamClass.addString("userpressedescape");
							super.streamClass.formatPacket();
						}
						if (showShop)
							showShop = false;
						if (showBank)
							showBank = false;
						if (showTradeWindow){
							showTradeWindow = false;
							super.streamClass.createPacket(41);
							super.streamClass.formatPacket();
						}
						if (showDuelWindow){
							showDuelWindow = false;
							super.streamClass.createPacket(41);
							super.streamClass.formatPacket();
						}
						if (showTradeConfirmWindow){
							showTradeConfirmWindow = false;
							super.streamClass.createPacket(41);
							super.streamClass.formatPacket();					
						}
						if (showDuelConfirmWindow){
							showDuelConfirmWindow = false;
							super.streamClass.createPacket(41);
							super.streamClass.formatPacket();
						}
						if (showWelcomeBox)
							showWelcomeBox = false;
						if (showServerMessageBox)
							showServerMessageBox = false;
						if (showAbuseWindow != 0)
							showAbuseWindow = 0;
						if (showCommandsWindow != 0)
							showCommandsWindow = 0;						
						if (drawStaffMenu)
							drawStaffMenu = !drawStaffMenu;
						if (drawIntegerInputBox)
							drawIntegerInputBox = !drawIntegerInputBox;
					break;
					
					case 38: // Up Arrow
						if (loggedIn == 1)
							if (cameraHeight > 750)
								cameraHeight -= 50;
					break;
					case 40: // Down Arrow
						if (loggedIn == 1)
							if (cameraHeight < 1500)
								cameraHeight += 50;
					break;
	
					case 33: // Page Up							
						if (ourPlayer.admin == 4) // Regular Player
							displayMessage("@gre@RSCE:@whi@ Chat History is a subscriber only feature due to spam abuse. Type ::SUBSCRIBE", 3, 0);
						else {
							currentChat--;
							if (currentChat < 0) {
								currentChat = 0;
								return;
							}
							gameMenu.updateText(chatHandle, messages.get(currentChat));
						}
					break;
					
					case 34: // Page Down
						if (ourPlayer.admin == 4) // Regular Player
							displayMessage("@gre@RSCE:@whi@ Chat History is a subscriber only feature due to spam abuse. Type ::SUBSCRIBE", 3, 0);
						else {
							currentChat++;
							if (currentChat >= messages.size()) {
								currentChat = messages.size();
								gameMenu.updateText(chatHandle, "");
							} else
								gameMenu.updateText(chatHandle, messages.get(currentChat));
						}
					break;
					
					case 114: // F3
						clanChannel = !clanChannel;
					break;
	
					case 122: // F11
						/* recording = !recording;
						if(recording) {
							try {
								frames.clear();
								File file = getEmptyFile(true);
								Recorder recorder = new Recorder(windowWidth, windowHeight + 11, MOVIE_FPS, frames, file.getAbsolutePath(), "video.quicktime"); 
								displayMessage("@gre@RSCE:@whi@ Recording movie...", 3, 0);
								new Thread(recorder).start();
							} catch(Exception e) {}
						} else {
							frames.add(null);
							displayMessage("@gre@RSCE:@whi@ Movie saved", 3, 0);
						} */
	    			break;
					
					case 123: // F12
						takeScreenshot(true);
					break;
					
					case 113: // F2
						setProp("SIDE_MENU", SIDE_MENU ? "OFF" : "ON");
						SIDE_MENU = !SIDE_MENU;
					break;
			}
			if (loggedIn == 0) {
				if (loginScreenNumber == 0)
					menuWelcome.keyDown(key, keyChar);
				if (loginScreenNumber == 1)
					menuNewUser.keyDown(key, keyChar);
				if (loginScreenNumber == 2)
					menuLogin.keyDown(key, keyChar);
			}
			if (loggedIn == 1) {
				if (showCharacterLookScreen) {
					characterDesignMenu.keyDown(key, keyChar);
					return;
				}
				if (!drawSearchBox && inputBoxType == 0 && showAbuseWindow == 0 && showCommandsWindow == 0 && !drawIntegerInputBox)
					gameMenu.keyDown(key, keyChar);
			}
		}
    }

	protected final void resetSearch() {
		searchResults = new String[] {""};
		matches = null;
	}
	
	protected final void updateSearch() {
		if (searchType.equals("items")) {
			matches = EntityHandler.grabEntities("items", super.inputText);
			searchResults = new String[(matches.length > 9 ? 10 : matches.length)];
			for (int i = 0; i < (matches.length > 9 ? 10 : matches.length); i++)
				searchResults[i] = ((ItemDef)matches[i]).name + " (" + ((ItemDef)matches[i]).id + ")" ;			
		} else if(searchType.equals("objects")) {
			matches = EntityHandler.grabEntities("objects", super.inputText);
			searchResults = new String[(matches.length > 9 ? 10 : matches.length)];
			for (int i = 0; i < (matches.length > 9 ? 10 : matches.length); i++)
				searchResults[i] = ((GameObjectDef)matches[i]).name + " (" + ((GameObjectDef)matches[i]).id + ")" ;		
		} else if(searchType.equals("npcs")) {
			matches = EntityHandler.grabEntities("npcs", super.inputText);
			searchResults = new String[(matches.length > 9 ? 10 : matches.length)];
			for (int i = 0; i < (matches.length > 9 ? 10 : matches.length); i++)
				searchResults[i] = ((NPCDef)matches[i]).name + " (" + ((NPCDef)matches[i]).id + ")" ;		
		} else if(searchType.equals("doors")) {
			matches = EntityHandler.grabEntities("doors", super.inputText);
			searchResults = new String[matches.length > 9 ? 10 : matches.length];
			for (int i = 0; i < (matches.length > 9 ? 10 : matches.length); i++)
				searchResults[i] = ((DoorDef)matches[i]).name + "(" + ((DoorDef)matches[i]).id + ")";
		}
	}
	
    private final void drawShopBox() {
        if (mouseButtonClick != 0) {
            mouseButtonClick = 0;
            int i = super.mouseX - (gameWidth - 411) / 2;
            int j = super.mouseY - (gameHeight - 247) / 2;
            if (i >= 0 && j >= 12 && i < gameWidth / 2 + 206 && j < gameHeight / 2 + 124) {
                int k = 0;
                for (int i1 = 0; i1 < 5; i1++) {
                    for (int i2 = 0; i2 < 8; i2++) {
                        int l2 = 7 + i2 * 49;
                        int l3 = 28 + i1 * 34;
                        if (i > l2 && i < l2 + 49 && j > l3 && j < l3 + 34 && shopItems[k] != -1) {
                            selectedShopItemIndex = k;
                            selectedShopItemType = shopItems[k];
                        }
                        k++;
                    }
                }

                if (selectedShopItemIndex >= 0) {
                    int j2 = shopItems[selectedShopItemIndex];
                    if (j2 != -1) {
                        if (shopItemCount[selectedShopItemIndex] > 0 && i > 298 && j >= 204 && i < 408 && j <= 215) {
                            int i4 = (shopItemBuyPriceModifier * EntityHandler.getItemDef(j2).getBasePrice()) / 100;
                            super.streamClass.createPacket(65);
                            super.streamClass.add2ByteInt(shopItems[selectedShopItemIndex]);
                            super.streamClass.add4ByteInt(i4);
                            super.streamClass.formatPacket();
                        }
                        if (inventoryCount(j2) > 0 && i > 2 && j >= 229 && i < 112 && j <= 240) {
                            int j4 = (shopItemSellPriceModifier * EntityHandler.getItemDef(j2).getBasePrice()) / 100;
                            super.streamClass.createPacket(66);
                            super.streamClass.add2ByteInt(shopItems[selectedShopItemIndex]);
                            super.streamClass.add4ByteInt(j4);
                            super.streamClass.formatPacket();
                        }
                    }
                }
            } else {
                super.streamClass.createPacket(67);
                super.streamClass.formatPacket();
                showShop = false;
                return;
            }
        }
        int byte0 = gameWidth / 2 - 204;
        int byte1 = gameHeight / 2 - 123;
        gameGraphics.drawBox(byte0, byte1, 408, 12, 192);
        int l = 0x989898;
        drawBoxAlpha(byte0, byte1 + 12, 408, 17, l, 160);
        drawBoxAlpha(byte0, byte1 + 29, 8, 170, l, 160);
        drawBoxAlpha(byte0 + 399, byte1 + 29, 9, 170, l, 160);
        drawBoxAlpha(byte0, byte1 + 199, 408, 47, l, 160);
        drawString("Buying and selling items", byte0 + 1, byte1 + 10, 1, 0xffffff);
        int j1 = 0xffffff;
        if (super.mouseX > byte0 + 320 && super.mouseY >= byte1 && super.mouseX < byte0 + 408 && super.mouseY < byte1 + 12)
            j1 = 0xff0000;
        gameGraphics.drawBoxTextRight("Close window", byte0 + 406, byte1 + 10, 1, j1);
        drawString("Shops stock in green", byte0 + 2, byte1 + 24, 1, 65280);
        drawString("Number you own in blue", byte0 + 135, byte1 + 24, 1, 65535);
		drawString("Your money: " + insertCommas( "" + inventoryCount(10)) + "gp", byte0 + 246, byte1 + 24, 1, 0xffff00);
		int k2 = 0xd0d0d0;
        int k3 = 0;
        for (int k4 = 0; k4 < 5; k4++) {
            for (int l4 = 0; l4 < 8; l4++) {
                int j5 = byte0 + 7 + l4 * 49;
                int i6 = byte1 + 28 + k4 * 34;
                if (selectedShopItemIndex == k3)
                    drawBoxAlpha(j5, i6, 49, 34, 0xff0000, 160);
                else
                    drawBoxAlpha(j5, i6, 49, 34, k2, 160);
                gameGraphics.drawBoxEdge(j5, i6, 50, 35, 0);
                if (shopItems[k3] != -1) {
                    gameGraphics.spriteClip4(j5, i6, 48, 32, SPRITE_ITEM_START + EntityHandler.getItemDef(shopItems[k3]).getSprite(), EntityHandler.getItemDef(shopItems[k3]).getPictureMask(), 0, 0, false);
                    drawString(String.valueOf(shopItemCount[k3]), j5 + 1, i6 + 10, 1, 65280);
                    gameGraphics.drawBoxTextRight(String.valueOf(inventoryCount(shopItems[k3])), j5 + 47, i6 + 10, 1, 65535);
                }
                k3++;
            }

        }

        drawLineX(byte0 + 5, byte1 + 222, 398, 0);
        if (selectedShopItemIndex == -1) {
            drawText("Select an object to buy or sell", byte0 + 204, byte1 + 214, 3, 0xffff00);
            return;
        }
        int i5 = shopItems[selectedShopItemIndex];
        if (i5 != -1) {
            if (shopItemCount[selectedShopItemIndex] > 0) {
                int j6 = (shopItemBuyPriceModifier * EntityHandler.getItemDef(i5).getBasePrice()) / 100;
                drawString("Buy a new " + EntityHandler.getItemDef(i5).getName() + " for " + j6 + "gp", byte0 + 2, byte1 + 214, 1, 0xffff00);
                int k1 = 0xffffff;
                if (super.mouseX > byte0 + 378 && super.mouseY >= byte1 + 204 && super.mouseX < byte0 + 408 && super.mouseY <= byte1 + 215)
                    k1 = 0xff0000; // byte0 + 298 :::::::::: byte0 + 408
                gameGraphics.drawBoxTextRight("Buy", byte0 + 405, byte1 + 214, 3, k1);
            } else
                drawText("This item is not currently available to buy", byte0 + 204, byte1 + 214, 3, 0xffff00);
            if (inventoryCount(i5) > 0) {
                int k6 = (shopItemSellPriceModifier * EntityHandler.getItemDef(i5).getBasePrice()) / 100;
                gameGraphics.drawBoxTextRight("Sell your " + EntityHandler.getItemDef(i5).getName() + " for " + k6 + "gp", byte0 + 405, byte1 + 239, 1, 0xffff00);
                int l1 = 0xffffff;
                if (super.mouseX > byte0 + 2 && super.mouseY >= byte1 + 229 && super.mouseX < byte0 + 30 && super.mouseY <= byte1 + 240)
                    l1 = 0xff0000;
                drawString("Sell", byte0 + 2, byte1 + 239, 3, l1); 
                return;
            }
            drawText("You do not have any of this item to sell", byte0 + 204, byte1 + 239, 3, 0xffff00);
        }
    }

    private final void drawGameMenu() {
        gameMenu = new Menu(gameGraphics, 10);   
        messagesHandleType2 = gameMenu.method159(5, gameHeight - 65, gameWidth - 14, 56, 1, 20, true); // was 269
        chatHandle = gameMenu.method160(7, gameHeight - 10, gameWidth - 14, 14, 1, 80, false, true); // vast 498
        messagesHandleType5 = gameMenu.method159(5, gameHeight - 65, gameWidth - 14, 56, 1, 20, true);
        messagesHandleType6 = gameMenu.method159(5, gameHeight - 65, gameWidth - 14, 56, 1, 20, true);
        gameMenu.setFocus(chatHandle);
    }

	protected final byte[] load(String filename) {
		File f = new File(DATA_DIR + filename);
		if (f.exists()) {
			try {
				InputStream fis = new FileInputStream(f);
				return super.load(fis);
			} catch(IOException i) {
				System.out.println(i.getMessage());
			}
		}
		return null;
	}

    /*private final void drawOptionsMenu(boolean flag) {
        int i = ((GameImage) (gameGraphics)).menuDefaultWidth - 199;
        int j = 36;
        gameGraphics.drawPicture(i - 49, 3, SPRITE_MEDIA_START + 6);
        char c = '\304';
        gameGraphics.drawBoxAlpha(i, 36, c, 65, GameImage.convertRGBToLong(181, 181, 181), 160);
        gameGraphics.drawBoxAlpha(i, 101, c, 65, GameImage.convertRGBToLong(201, 201, 201), 160);
        gameGraphics.drawBoxAlpha(i, 166, c, 95, GameImage.convertRGBToLong(181, 181, 181), 160);
        gameGraphics.drawBoxAlpha(i, 261, c, 45, GameImage.convertRGBToLong(201, 201, 201), 160);
        int k = i + 3;
        int i1 = j + 15;
        gameGraphics.drawString("Game options - click to toggle", k, i1, 1, 0);
        i1 += 15;
        if (cameraRotate)
            gameGraphics.drawString("Camera angle mode - @gre@Auto", k, i1, 1, 0xffffff);
        else
            gameGraphics.drawString("Camera angle mode - @red@Manual", k, i1, 1, 0xffffff);
        i1 += 15;
        if (configMouseButtons)
            gameGraphics.drawString("Mouse buttons - @red@One", k, i1, 1, 0xffffff);
        else
            gameGraphics.drawString("Mouse buttons - @gre@Two", k, i1, 1, 0xffffff);
        i1 += 15;
        if (configSoundEffects)
            gameGraphics.drawString("Sound effects - @red@off", k, i1, 1, 0xffffff);
        else
            gameGraphics.drawString("Sound effects - @gre@on", k, i1, 1, 0xffffff);
        i1 += 15;
		gameGraphics.drawString("To change your contact details,", k, i1, 0, 0xffffff);
        i1 += 15;
		gameGraphics.drawString("password, recovery questions, etc..", k, i1, 0, 0xffffff);
        i1 += 15;
		gameGraphics.drawString("please select 'account management'", k, i1, 0, 0xffffff);
        i1 += 15;
		gameGraphics.drawString("from the rscemulation.net front page", k, i1, 0, 0xffffff);
        i1 += 20;
        gameGraphics.drawString("Privacy settings. Will be applied to", i + 3, i1, 1, 0);
        i1 += 15;
        gameGraphics.drawString("all people not on your friends list", i + 3, i1, 1, 0);
        i1 += 15;
        if (super.blockChatMessages)
            gameGraphics.drawString("Block chat messages: @red@<off>", i + 3, i1, 1, 0xffffff);
        else
            gameGraphics.drawString("Block chat messages: @gre@<on>", i + 3, i1, 1, 0xffffff);
        i1 += 15;
        if (super.blockPrivateMessages)
            gameGraphics.drawString("Block private messages: @red@<off>", i + 3, i1, 1, 0xffffff);
        else
            gameGraphics.drawString("Block private messages: @gre@<on>", i + 3, i1, 1, 0xffffff);
        i1 += 15;
		if (super.blockSayMessages)
            drawString("Block global messages: @red@<off>", i + 3, i1, 1, 0xffffff);
        else
            drawString("Block global messages: @gre@<on>", i + 3, i1, 1, 0xffffff);
		i1 += 15;
        if (super.blockTradeRequests)
            gameGraphics.drawString("Block trade requests: @red@<off>", i + 3, i1, 1, 0xffffff);
        else
            gameGraphics.drawString("Block trade requests: @gre@<on>", i + 3, i1, 1, 0xffffff);
        i1 += 15;
        if (super.blockDuelRequests)
            gameGraphics.drawString("Block duel requests: @red@<off>", i + 3, i1, 1, 0xffffff);
        else
            gameGraphics.drawString("Block duel requests: @gre@<on>", i + 3, i1, 1, 0xffffff);
        i1 += 15;
        i1 += 5;
        gameGraphics.drawString("Always logout when you finish", k, i1, 1, 0);
        i1 += 15;
        int k1 = 0xffffff;
        if (super.mouseX > k && super.mouseX < k + c && super.mouseY > i1 - 12 && super.mouseY < i1 + 4)
            k1 = 0xffff00;
        gameGraphics.drawString("Click here to logout", i + 3, i1, 1, k1);
        i = super.mouseX - (((GameImage) (gameGraphics)).menuDefaultWidth - 199);
        j = super.mouseY - 36;
        if (i >= 0 && j >= 0 && i < 196 && j < 265) {
            int l1 = ((GameImage) (gameGraphics)).menuDefaultWidth - 199;
            byte byte0 = 36;
            char c1 = '\304';
            int l = l1 + 3;
            int j1 = byte0 + 30;
            if (super.mouseX > l && super.mouseX < l + c1 && super.mouseY > j1 - 12 && super.mouseY < j1 + 4 && mouseButtonClick == 1) {
                cameraRotate = !cameraRotate;
				super.streamClass.createPacket(15);
				super.streamClass.addByte(cameraRotate ? 5 : 0);
				super.streamClass.formatPacket();
            }
            j1 += 15;
            if (super.mouseX > l && super.mouseX < l + c1 && super.mouseY > j1 - 12 && super.mouseY < j1 + 4 && mouseButtonClick == 1) {
                configMouseButtons = !configMouseButtons;
				super.streamClass.createPacket(15);
				super.streamClass.addByte(configMouseButtons ? 1 : 6);
				super.streamClass.formatPacket();
            }
            j1 += 15;
            if (super.mouseX > l && super.mouseX < l + c1 && super.mouseY > j1 - 12 && super.mouseY < j1 + 4 && mouseButtonClick == 1) {
                configSoundEffects = !configSoundEffects;
				super.streamClass.createPacket(15);
				super.streamClass.addByte(configSoundEffects ? 2 : 7);
				super.streamClass.formatPacket();
            }
            j1 += 110;
            if (super.mouseX > l && super.mouseX < l + c1 && super.mouseY > j1 - 12 && super.mouseY < j1 + 4 && mouseButtonClick == 1) {
                super.blockChatMessages = !super.blockChatMessages;
                super.streamClass.createPacket(16);
				super.streamClass.addByte((super.blockChatMessages ? 0 : 5));
				super.streamClass.formatPacket();
            }
            j1 += 15;
            if (super.mouseX > l && super.mouseX < l + c1 && super.mouseY > j1 - 12 && super.mouseY < j1 + 4 && mouseButtonClick == 1) {
                super.blockPrivateMessages = !super.blockPrivateMessages;
                super.streamClass.createPacket(16);
				super.streamClass.addByte((super.blockPrivateMessages ? 1 : 6));
				super.streamClass.formatPacket();
            }
			j1 += 15;
            if (super.mouseX > l && super.mouseX < l + c1 && super.mouseY > j1 - 12 && super.mouseY < j1 + 4 && mouseButtonClick == 1) {
				super.blockSayMessages = !super.blockSayMessages;
				super.streamClass.createPacket(16);
				super.streamClass.addByte((super.blockSayMessages ? 4 : 9));
				super.streamClass.formatPacket();
            }
            j1 += 15;
            if (super.mouseX > l && super.mouseX < l + c1 && super.mouseY > j1 - 12 && super.mouseY < j1 + 4 && mouseButtonClick == 1) {
                super.blockTradeRequests = !super.blockTradeRequests;
                super.streamClass.createPacket(16);
				super.streamClass.addByte((super.blockTradeRequests ? 2 : 7));
				super.streamClass.formatPacket();
            }
            j1 += 15;
            if (super.mouseX > l && super.mouseX < l + c1 && super.mouseY > j1 - 12 && super.mouseY < j1 + 4 && mouseButtonClick == 1) {
                super.blockDuelRequests = !super.blockDuelRequests;
                super.streamClass.createPacket(16);
				super.streamClass.addByte((super.blockDuelRequests ? 3 : 8));
				super.streamClass.formatPacket();
            }
            j1 += 35;
            if (super.mouseX > l && super.mouseX < l + c1 && super.mouseY > j1 - 12 && super.mouseY < j1 + 4 && mouseButtonClick == 1)
                logout();
            mouseButtonClick = 0;
        }
    }*/	
	
    private final void drawOptionsMenu(boolean flag) {
		if (ADVANCED_MENU) {
			int i = ((GameImage) (gameGraphics)).menuDefaultWidth - 199;
			int j = 36;
			gameGraphics.drawPicture(i - 49, 3, SPRITE_MEDIA_START + 6);
			char c = '\304';
			drawBoxAlpha(i, 36, c, 65, GameImage.convertRGBToLong(181, 181, 181), 160);
			drawBoxAlpha(i, 101, c, 80, GameImage.convertRGBToLong(201, 201, 201), 160);
			drawBoxAlpha(i, 181, c, 95, GameImage.convertRGBToLong(181, 181, 181), 160);
			drawBoxAlpha(i, 276, c, 40, GameImage.convertRGBToLong(201, 201, 201), 160);
			int k = i + 3;
			int i1 = j + 15;
			drawString("Game Settings:", k, i1, 1, 0);
			i1 += 15;
			if (cameraRotate)
				drawString("Camera Rotate: @red@Auto", k, i1, 1, 0xffffff);
			else
				drawString("Camera Rotate: @gre@Manual", k, i1, 1, 0xffffff);
			i1 += 15;
			if (configMouseButtons)
				drawString("Mouse Buttons: @red@One", k, i1, 1, 0xffffff);
			else
				drawString("Mouse Buttons: @gre@Two", k, i1, 1, 0xffffff);
			i1 += 15;
			if (configSoundEffects)
				drawString("Sound Effects: @red@Off", k, i1, 1, 0xffffff);
			else
				drawString("Sound Effects: @gre@On", k, i1, 1, 0xffffff);
			i1 += 15;
			if (!hideCeilings)
				drawString("Ceilings: @gre@Show", k, i1, 1, 0xffffff);
			else
				drawString("Ceilings: @red@Hide", k, i1, 1, 0xffffff);
			i1 += 15;
			if (fog)
				drawString("Fog: @gre@Show", k, i1, 1, 0xffffff);
			else
				drawString("Fog: @red@Hide", k, i1, 1, 0xffffff);
			i1 += 15;
			if (chatFilter)
				drawString("Chat Filter: @gre@Enabled", k, i1, 1, 0xffffff);
			else
				drawString("Chat Filter: @red@Disabled", k, i1, 1, 0xffffff);
			i1 += 15;
			if (autoScreenshots)
				drawString("Auto Screenshots: @gre@On", k, i1, 1, 0xffffff);
			else
				drawString("Auto Screenshots: @red@Off", k, i1, 1, 0xffffff);
			i1 += 15;
			if (fightmode == 0)
				drawString("Fightmode Selector: @gre@Always", k, i1, 1, 0xffffff);
			else if(fightmode == 1)
				drawString("Fightmode Selector: @yel@In Combat", k, i1, 1, 0xffffff);
			else
				drawString("Fightmode Selector: @red@Never", k, i1, 1, 0xffffff);
			/*i1 += 15;
			if (killNotify)
				drawString("Kill Notify: @yel@Coming Soon!", k, i1, 1, 0xffffff);
			else
				drawString("Kill Notify: @yel@Coming Soon!", k, i1, 1, 0xffffff);*/
			i1 += 30;
			drawString("Privacy Settings:", i + 3, i1, 1, 0);
			i1 += 15;
			if (super.blockChatMessages)
				drawString("Chat Messages: @gre@Allowed", i + 3, i1, 1, 0xffffff);
			else
				drawString("Chat Messages: @red@Blocked", i + 3, i1, 1, 0xffffff);
			i1 += 15;
			if (super.blockPrivateMessages)
				drawString("Private Messages: @gre@Allowed", i + 3, i1, 1, 0xffffff);
			else
				drawString("Private Messages: @red@Blocked", i + 3, i1, 1, 0xffffff);
			i1 += 15;
			if (super.blockTradeRequests)
				drawString("Trade Requests: @gre@Allowed", i + 3, i1, 1, 0xffffff);
			else
				drawString("Trade Requests: @red@Blocked", i + 3, i1, 1, 0xffffff);
			i1 += 15;
			if (super.blockDuelRequests)
				drawString("Duel Requests: @gre@Allowed", i + 3, i1, 1, 0xffffff);
			else
				drawString("Duel Requests: @red@Blocked", i + 3, i1, 1, 0xffffff);
					i1 += 15;
			if (super.blockSayMessages)
				drawString("Global Chat: @gre@Allowed", i + 3, i1, 1, 0xffffff);
			else
				drawString("Global Chat: @red@Blocked", i + 3, i1, 1, 0xffffff);
			i1 += 30;
			int hoverColour = 0xffffff;
			if (super.mouseX > k && super.mouseX < k + c && super.mouseY > i1 - 12 && super.mouseY < i1 + 4)
				hoverColour = 0xffff00;
			drawString("Logout RSCEmulation", i + 3, i1, 1, hoverColour);
			if (!flag)
				return;
			i = super.mouseX - (((GameImage) (gameGraphics)).menuDefaultWidth - 199);
			j = super.mouseY - 36;
			if (i >= 0 && j >= 0 && i < 196 && j < 280) {
				int l1 = ((GameImage) (gameGraphics)).menuDefaultWidth - 199;
				char c1 = '\304';
				int widthModifier = l1 + 3;
				int heightModifier = 66;
				if (super.mouseX > widthModifier && super.mouseX < widthModifier + c1 && super.mouseY > heightModifier - 12 && super.mouseY < heightModifier + 4 && mouseButtonClick == 1) {
					cameraRotate = !cameraRotate;
					super.streamClass.createPacket(15);
					super.streamClass.addByte(cameraRotate ? 5 : 0);
					super.streamClass.formatPacket();
				}
				heightModifier += 15;
				if (super.mouseX > widthModifier && super.mouseX < widthModifier + c1 && super.mouseY > heightModifier - 12 && super.mouseY < heightModifier + 4 && mouseButtonClick == 1) {
					configMouseButtons = !configMouseButtons;
					super.streamClass.createPacket(15);
					super.streamClass.addByte(configMouseButtons ? 1 : 6);
					super.streamClass.formatPacket();
				}
				heightModifier += 15;
				if (super.mouseX > widthModifier && super.mouseX < widthModifier + c1 && super.mouseY > heightModifier - 12 && super.mouseY < heightModifier + 4 && mouseButtonClick == 1) {
					configSoundEffects = !configSoundEffects;
					super.streamClass.createPacket(15);
					super.streamClass.addByte(configSoundEffects ? 2 : 7);
					super.streamClass.formatPacket();
				}
				heightModifier += 15;
				if (super.mouseX > widthModifier && super.mouseX < widthModifier + c1 && super.mouseY > heightModifier - 12 && super.mouseY < heightModifier + 4 && mouseButtonClick == 1) {
					hideCeilings = !hideCeilings;
					super.streamClass.createPacket(15);
					super.streamClass.addByte(hideCeilings ? 3 : 8);
					super.streamClass.formatPacket();
				}
				heightModifier += 15;
				if (super.mouseX > widthModifier && super.mouseX < widthModifier + c1 && super.mouseY > heightModifier - 12 && super.mouseY < heightModifier + 4 && mouseButtonClick == 1) {
					setProp("FOG", fog ? "OFF" : "ON");
					fog = !fog;
				}
				heightModifier += 15;
				if (super.mouseX > widthModifier && super.mouseX < widthModifier + c1 && super.mouseY > heightModifier - 12 && super.mouseY < heightModifier + 4 && mouseButtonClick == 1) {
					setProp("CHAT_FILTER", chatFilter ? "OFF" : "ON");
					chatFilter = !chatFilter;
				}
				heightModifier += 15;
				if (super.mouseX > widthModifier && super.mouseX < widthModifier + c1 && super.mouseY > heightModifier - 12 && super.mouseY < heightModifier + 4 && mouseButtonClick == 1) {
					autoScreenshots = !autoScreenshots;
					super.streamClass.createPacket(15);
					super.streamClass.addByte(autoScreenshots ? 9 : 4);
					super.streamClass.formatPacket();
				}
				heightModifier += 15;
				if (super.mouseX > widthModifier && super.mouseX < widthModifier + c1 && super.mouseY > heightModifier - 12 && super.mouseY < heightModifier + 4 && mouseButtonClick == 1) {
					super.streamClass.createPacket(15);				
					if (fightmode == 0) {
						fightmode++;
						super.streamClass.addByte(11);
					} else if(fightmode == 1) {
						fightmode++;
						super.streamClass.addByte(12);
					} else {
						fightmode = 0;
						super.streamClass.addByte(10);
					}
					super.streamClass.formatPacket();
				}
				heightModifier += 15;
				/*if (super.mouseX > widthModifier && super.mouseX < widthModifier + c1 && super.mouseY > heightModifier - 12 && super.mouseY < heightModifier + 4 && mouseButtonClick == 1) {
					killNotify = !killNotify;
					super.streamClass.createPacket(15);
					super.streamClass.addByte(killNotify ? 13 : 14);
					super.streamClass.formatPacket();
				}*/
				heightModifier += 30;
				if (super.mouseX > widthModifier && super.mouseX < widthModifier + c1 && super.mouseY > heightModifier - 12 && super.mouseY < heightModifier + 4 && mouseButtonClick == 1) {
					super.blockChatMessages = !super.blockChatMessages;
					super.streamClass.createPacket(16);
					super.streamClass.addByte((super.blockChatMessages ? 0 : 5));
					super.streamClass.formatPacket();
				}
				heightModifier += 15;
				if (super.mouseX > widthModifier && super.mouseX < widthModifier + c1 && super.mouseY > heightModifier - 12 && super.mouseY < heightModifier + 4 && mouseButtonClick == 1) {
					super.blockPrivateMessages = !super.blockPrivateMessages;
					super.streamClass.createPacket(16);
					super.streamClass.addByte((super.blockPrivateMessages ? 1 : 6));
					super.streamClass.formatPacket();
				}
				heightModifier += 15;
				if (super.mouseX > widthModifier && super.mouseX < widthModifier + c1 && super.mouseY > heightModifier - 12 && super.mouseY < heightModifier + 4 && mouseButtonClick == 1) {
					super.blockTradeRequests = !super.blockTradeRequests;
					super.streamClass.createPacket(16);
					super.streamClass.addByte((super.blockTradeRequests ? 2 : 7));
					super.streamClass.formatPacket();
				}
				heightModifier += 15;
				if (super.mouseX > widthModifier && super.mouseX < widthModifier + c1 && super.mouseY > heightModifier - 12 && super.mouseY < heightModifier + 4 && mouseButtonClick == 1) {
					super.blockDuelRequests = !super.blockDuelRequests;
					super.streamClass.createPacket(16);
					super.streamClass.addByte((super.blockDuelRequests ? 3 : 8));
					super.streamClass.formatPacket();
				}
				heightModifier += 15;
				if (super.mouseX > widthModifier && super.mouseX < widthModifier + c1 && super.mouseY > heightModifier - 12 && super.mouseY < heightModifier + 4 && mouseButtonClick == 1) {
					super.blockSayMessages = !super.blockSayMessages;
					super.streamClass.createPacket(16);
					super.streamClass.addByte((super.blockSayMessages ? 4 : 9));
					super.streamClass.formatPacket();
				}
				heightModifier += 30;
				if (super.mouseX > widthModifier && super.mouseX < widthModifier + c1 && super.mouseY > heightModifier - 12 && super.mouseY < heightModifier + 4 && mouseButtonClick == 1)
					logout();
				mouseButtonClick = 0;
			}
		} else {
        int i = ((GameImage) (gameGraphics)).menuDefaultWidth - 199;
        int j = 36;
        gameGraphics.drawPicture(i - 49, 3, SPRITE_MEDIA_START + 6);
        char c = '\304';
        gameGraphics.drawBoxAlpha(i, 36, c, 65, GameImage.convertRGBToLong(181, 181, 181), 160);
        gameGraphics.drawBoxAlpha(i, 101, c, 65, GameImage.convertRGBToLong(201, 201, 201), 160);
        gameGraphics.drawBoxAlpha(i, 166, c, 95, GameImage.convertRGBToLong(181, 181, 181), 160);
        gameGraphics.drawBoxAlpha(i, 261, c, 45, GameImage.convertRGBToLong(201, 201, 201), 160);
        int k = i + 3;
        int i1 = j + 15;
        gameGraphics.drawString("Game options - click to toggle", k, i1, 1, 0);
        i1 += 15;
        if (cameraRotate)
            gameGraphics.drawString("Camera angle mode - @gre@Auto", k, i1, 1, 0xffffff);
        else
            gameGraphics.drawString("Camera angle mode - @red@Manual", k, i1, 1, 0xffffff);
        i1 += 15;
        if (configMouseButtons)
            gameGraphics.drawString("Mouse buttons - @red@One", k, i1, 1, 0xffffff);
        else
            gameGraphics.drawString("Mouse buttons - @gre@Two", k, i1, 1, 0xffffff);
        i1 += 15;
        if (configSoundEffects)
            gameGraphics.drawString("Sound effects - @red@off", k, i1, 1, 0xffffff);
        else
            gameGraphics.drawString("Sound effects - @gre@on", k, i1, 1, 0xffffff);
        i1 += 15;
		gameGraphics.drawString("To change your contact details,", k, i1, 0, 0xffffff);
        i1 += 15;
		gameGraphics.drawString("password, recovery questions, etc..", k, i1, 0, 0xffffff);
        i1 += 15;
		gameGraphics.drawString("please select 'account management'", k, i1, 0, 0xffffff);
        i1 += 15;
		gameGraphics.drawString("from the rscemulation.net front page", k, i1, 0, 0xffffff);
        i1 += 20;
        gameGraphics.drawString("Privacy settings. Will be applied to", i + 3, i1, 1, 0);
        i1 += 15;
        gameGraphics.drawString("all people not on your friends list", i + 3, i1, 1, 0);
        i1 += 15;
        if (super.blockChatMessages)
            gameGraphics.drawString("Block chat messages: @red@<off>", i + 3, i1, 1, 0xffffff);
        else
            gameGraphics.drawString("Block chat messages: @gre@<on>", i + 3, i1, 1, 0xffffff);
        i1 += 15;
        if (super.blockPrivateMessages)
            gameGraphics.drawString("Block private messages: @red@<off>", i + 3, i1, 1, 0xffffff);
        else
            gameGraphics.drawString("Block private messages: @gre@<on>", i + 3, i1, 1, 0xffffff);
        i1 += 15;
		if (super.blockSayMessages)
            drawString("Block global messages: @red@<off>", i + 3, i1, 1, 0xffffff);
        else
            drawString("Block global messages: @gre@<on>", i + 3, i1, 1, 0xffffff);
		i1 += 15;
        if (super.blockTradeRequests)
            gameGraphics.drawString("Block trade requests: @red@<off>", i + 3, i1, 1, 0xffffff);
        else
            gameGraphics.drawString("Block trade requests: @gre@<on>", i + 3, i1, 1, 0xffffff);
        i1 += 15;
        if (super.blockDuelRequests)
            gameGraphics.drawString("Block duel requests: @red@<off>", i + 3, i1, 1, 0xffffff);
        else
            gameGraphics.drawString("Block duel requests: @gre@<on>", i + 3, i1, 1, 0xffffff);
        i1 += 15;
        i1 += 5;
        gameGraphics.drawString("Always logout when you finish", k, i1, 1, 0);
        i1 += 15;
        int k1 = 0xffffff;
        if (super.mouseX > k && super.mouseX < k + c && super.mouseY > i1 - 12 && super.mouseY < i1 + 4)
            k1 = 0xffff00;
        gameGraphics.drawString("Click here to logout", i + 3, i1, 1, k1);
        i = super.mouseX - (((GameImage) (gameGraphics)).menuDefaultWidth - 199);
        j = super.mouseY - 36;
        if (i >= 0 && j >= 0 && i < 196 && j < 265) {
            int l1 = ((GameImage) (gameGraphics)).menuDefaultWidth - 199;
            byte byte0 = 36;
            char c1 = '\304';
            int l = l1 + 3;
            int j1 = byte0 + 30;
            if (super.mouseX > l && super.mouseX < l + c1 && super.mouseY > j1 - 12 && super.mouseY < j1 + 4 && mouseButtonClick == 1) {
                cameraRotate = !cameraRotate;
				super.streamClass.createPacket(15);
				super.streamClass.addByte(cameraRotate ? 5 : 0);
				super.streamClass.formatPacket();
            }
            j1 += 15;
            if (super.mouseX > l && super.mouseX < l + c1 && super.mouseY > j1 - 12 && super.mouseY < j1 + 4 && mouseButtonClick == 1) {
                configMouseButtons = !configMouseButtons;
				super.streamClass.createPacket(15);
				super.streamClass.addByte(configMouseButtons ? 1 : 6);
				super.streamClass.formatPacket();
            }
            j1 += 15;
            if (super.mouseX > l && super.mouseX < l + c1 && super.mouseY > j1 - 12 && super.mouseY < j1 + 4 && mouseButtonClick == 1) {
                configSoundEffects = !configSoundEffects;
				super.streamClass.createPacket(15);
				super.streamClass.addByte(configSoundEffects ? 2 : 7);
				super.streamClass.formatPacket();
            }
            j1 += 110;
            if (super.mouseX > l && super.mouseX < l + c1 && super.mouseY > j1 - 12 && super.mouseY < j1 + 4 && mouseButtonClick == 1) {
                super.blockChatMessages = !super.blockChatMessages;
                super.streamClass.createPacket(16);
				super.streamClass.addByte((super.blockChatMessages ? 0 : 5));
				super.streamClass.formatPacket();
            }
            j1 += 15;
            if (super.mouseX > l && super.mouseX < l + c1 && super.mouseY > j1 - 12 && super.mouseY < j1 + 4 && mouseButtonClick == 1) {
                super.blockPrivateMessages = !super.blockPrivateMessages;
                super.streamClass.createPacket(16);
				super.streamClass.addByte((super.blockPrivateMessages ? 1 : 6));
				super.streamClass.formatPacket();
            }
			j1 += 15;
            if (super.mouseX > l && super.mouseX < l + c1 && super.mouseY > j1 - 12 && super.mouseY < j1 + 4 && mouseButtonClick == 1) {
				super.blockSayMessages = !super.blockSayMessages;
				super.streamClass.createPacket(16);
				super.streamClass.addByte((super.blockSayMessages ? 4 : 9));
				super.streamClass.formatPacket();
            }
            j1 += 15;
            if (super.mouseX > l && super.mouseX < l + c1 && super.mouseY > j1 - 12 && super.mouseY < j1 + 4 && mouseButtonClick == 1) {
                super.blockTradeRequests = !super.blockTradeRequests;
                super.streamClass.createPacket(16);
				super.streamClass.addByte((super.blockTradeRequests ? 2 : 7));
				super.streamClass.formatPacket();
            }
            j1 += 15;
            if (super.mouseX > l && super.mouseX < l + c1 && super.mouseY > j1 - 12 && super.mouseY < j1 + 4 && mouseButtonClick == 1) {
                super.blockDuelRequests = !super.blockDuelRequests;
                super.streamClass.createPacket(16);
				super.streamClass.addByte((super.blockDuelRequests ? 3 : 8));
				super.streamClass.formatPacket();
            }
            j1 += 35;
            if (super.mouseX > l && super.mouseX < l + c1 && super.mouseY > j1 - 12 && super.mouseY < j1 + 4 && mouseButtonClick == 1)
                logout();
            mouseButtonClick = 0;
        }		
		}
    }
	
    private final void processGame() {
		if (!pinging && System.currentTimeMillis() - lastPing > 5000) {
			lastPing = System.currentTimeMillis();
			super.streamClass.createPacket(5);
			super.streamClass.formatPacket();
			pinging = true;
		}
        if (systemUpdate >= 1)
            systemUpdate--;
		if (wildernessUpdate >= 1)
			wildernessUpdate--;
        sendPingPacketReadPacketData();
        if (logoutTimeout > 0)
            logoutTimeout--;
        if (ourPlayer.currentSprite == 8 || ourPlayer.currentSprite == 9)
            lastWalkTimeout = 500;
        if (lastWalkTimeout > 0)
            lastWalkTimeout--;
        if (showCharacterLookScreen) {
            drawCharacterLookScreen();
            return;
        }
        for (int i = 0; i < playerCount; i++) {
            Mob mob = playerArray[i];
            int k = (mob.waypointCurrent + 1) % 10;
            if (mob.waypointEndSprite != k) {
                int i1 = -1;
                int l2 = mob.waypointEndSprite;
                int j4;
                if (l2 < k)
                    j4 = k - l2;
                else
                    j4 = (10 + k) - l2;
                int j5 = 4;
                if (j4 > 2)
                    j5 = (j4 - 1) * 4;
                if (mob.waypointsX[l2] - mob.currentX > 128 * 3 || mob.waypointsY[l2] - mob.currentY > 128 * 3 || mob.waypointsX[l2] - mob.currentX < -128 * 3 || mob.waypointsY[l2] - mob.currentY < -128 * 3 || j4 > 8) {
                    mob.currentX = mob.waypointsX[l2];
                    mob.currentY = mob.waypointsY[l2];
                } else {
                    if (mob.currentX < mob.waypointsX[l2]) {
                        mob.currentX += j5;
                        mob.stepCount++;
                        i1 = 2;
                    } else if (mob.currentX > mob.waypointsX[l2]) {
                        mob.currentX -= j5;
                        mob.stepCount++;
                        i1 = 6;
                    }
                    if (mob.currentX - mob.waypointsX[l2] < j5 && mob.currentX - mob.waypointsX[l2] > -j5)
                        mob.currentX = mob.waypointsX[l2];
                    if (mob.currentY < mob.waypointsY[l2]) {
                        mob.currentY += j5;
                        mob.stepCount++;
                        if (i1 == -1)
                            i1 = 4;
                        else if (i1 == 2)
                            i1 = 3;
                        else
                            i1 = 5;
                    } else if (mob.currentY > mob.waypointsY[l2]) {
                        mob.currentY -= j5;
                        mob.stepCount++;
                        if (i1 == -1)
                            i1 = 0;
                        else if (i1 == 2)
                            i1 = 1;
                        else
                            i1 = 7;
                    }
                    if (mob.currentY - mob.waypointsY[l2] < j5 && mob.currentY - mob.waypointsY[l2] > -j5)
                        mob.currentY = mob.waypointsY[l2];
                }
                if (i1 != -1)
                    mob.currentSprite = i1;
                if (mob.currentX == mob.waypointsX[l2] && mob.currentY == mob.waypointsY[l2])
                    mob.waypointEndSprite = (l2 + 1) % 10;
            } else
                mob.currentSprite = mob.nextSprite;
            if (mob.lastMessageTimeout > 0)
                mob.lastMessageTimeout--;
            if (mob.anInt163 > 0)
                mob.anInt163--;
            if (mob.combatTimer > 0)
                mob.combatTimer--;
            if (playerAliveTimeout > 0) {
                playerAliveTimeout--;
                if (playerAliveTimeout == 0) {
                    displayMessage("You have been granted another life. Be more careful this time!", 3, 0);
                    displayMessage("You retain your skills. Your items land where you died", 3, 0);
				}
            }
        }

        for (int j = 0; j < npcCount; j++) {
            Mob mob_1 = npcArray[j];
            int j1 = (mob_1.waypointCurrent + 1) % 10;
            if (mob_1.waypointEndSprite != j1) {
                int i3 = -1;
                int k4 = mob_1.waypointEndSprite;
                int k5;
                if (k4 < j1)
                    k5 = j1 - k4;
                else
                    k5 = (10 + j1) - k4;
                int l5 = 4;
                if (k5 > 2)
                    l5 = (k5 - 1) * 4;
                if (mob_1.waypointsX[k4] - mob_1.currentX > 128 * 3 || mob_1.waypointsY[k4] - mob_1.currentY > 128 * 3 || mob_1.waypointsX[k4] - mob_1.currentX < -128 * 3 || mob_1.waypointsY[k4] - mob_1.currentY < -128 * 3 || k5 > 8) {
                    mob_1.currentX = mob_1.waypointsX[k4];
                    mob_1.currentY = mob_1.waypointsY[k4];
                } else {
                    if (mob_1.currentX < mob_1.waypointsX[k4]) {
                        mob_1.currentX += l5;
                        mob_1.stepCount++;
                        i3 = 2;
                    } else if (mob_1.currentX > mob_1.waypointsX[k4]) {
                        mob_1.currentX -= l5;
                        mob_1.stepCount++;
                        i3 = 6;
                    }
                    if (mob_1.currentX - mob_1.waypointsX[k4] < l5 && mob_1.currentX - mob_1.waypointsX[k4] > -l5)
                        mob_1.currentX = mob_1.waypointsX[k4];
                    if (mob_1.currentY < mob_1.waypointsY[k4]) {
                        mob_1.currentY += l5;
                        mob_1.stepCount++;
                        if (i3 == -1)
                            i3 = 4;
                        else if (i3 == 2)
                            i3 = 3;
                        else
                            i3 = 5;
                    } else if (mob_1.currentY > mob_1.waypointsY[k4]) {
                        mob_1.currentY -= l5;
                        mob_1.stepCount++;
                        if (i3 == -1)
                            i3 = 0;
                        else if (i3 == 2)
                            i3 = 1;
                        else
                            i3 = 7;
                    }
                    if (mob_1.currentY - mob_1.waypointsY[k4] < l5 && mob_1.currentY - mob_1.waypointsY[k4] > -l5)
                        mob_1.currentY = mob_1.waypointsY[k4];
                }
                if (i3 != -1)
                    mob_1.currentSprite = i3;
                if (mob_1.currentX == mob_1.waypointsX[k4] && mob_1.currentY == mob_1.waypointsY[k4])
                    mob_1.waypointEndSprite = (k4 + 1) % 10;
            } else {
                mob_1.currentSprite = mob_1.nextSprite;
                if (mob_1.type == 43)
                    mob_1.stepCount++;
            }
            if (mob_1.lastMessageTimeout > 0)
                mob_1.lastMessageTimeout--;
            if (mob_1.anInt163 > 0)
                mob_1.anInt163--;
            if (mob_1.combatTimer > 0)
                mob_1.combatTimer--;
        }

        if (mouseOverMenu != 2) {
            if (GameImage.anInt346 > 0)
                anInt658++;
            if (GameImage.anInt347 > 0)
                anInt658 = 0;
            GameImage.anInt346 = 0;
            GameImage.anInt347 = 0;
        }
        for (int l = 0; l < playerCount; l++) {
            Mob mob_2 = playerArray[l];
            if (mob_2.anInt176 > 0)
                mob_2.anInt176--;
        }

        if (cameraAutoAngleDebug) {
            if (lastAutoCameraRotatePlayerX - ourPlayer.currentX < -500 || lastAutoCameraRotatePlayerX - ourPlayer.currentX > 500 || lastAutoCameraRotatePlayerY - ourPlayer.currentY < -500 || lastAutoCameraRotatePlayerY - ourPlayer.currentY > 500) {
                lastAutoCameraRotatePlayerX = ourPlayer.currentX;
                lastAutoCameraRotatePlayerY = ourPlayer.currentY;
            }
        } else {
            if (lastAutoCameraRotatePlayerX - ourPlayer.currentX < -500 || lastAutoCameraRotatePlayerX - ourPlayer.currentX > 500 || lastAutoCameraRotatePlayerY - ourPlayer.currentY < -500 || lastAutoCameraRotatePlayerY - ourPlayer.currentY > 500) {
                lastAutoCameraRotatePlayerX = ourPlayer.currentX;
                lastAutoCameraRotatePlayerY = ourPlayer.currentY;
            }
            if (lastAutoCameraRotatePlayerX != ourPlayer.currentX)
                lastAutoCameraRotatePlayerX += (ourPlayer.currentX - lastAutoCameraRotatePlayerX) / (16 + (cameraHeight - 500) / 15);
            if (lastAutoCameraRotatePlayerY != ourPlayer.currentY)
                lastAutoCameraRotatePlayerY += (ourPlayer.currentY - lastAutoCameraRotatePlayerY) / (16 + (cameraHeight - 500) / 15);
            if (cameraRotate) {
                int k1 = cameraAutoAngle * 32;
                int j3 = k1 - cameraRotation;
                byte byte0 = 1;
                if (j3 != 0) {
                    cameraRotationBaseAddition++;
                    if (j3 > 128) {
                        byte0 = -1;
                        j3 = 256 - j3;
                    } else if (j3 > 0)
                        byte0 = 1;
                    else if (j3 < -128) {
                        byte0 = 1;
                        j3 = 256 + j3;
                    } else if (j3 < 0) {
                        byte0 = -1;
                        j3 = -j3;
                    }
                    cameraRotation += ((cameraRotationBaseAddition * j3 + 255) / 256) * byte0;
                    cameraRotation &= 0xff;
                } else
                    cameraRotationBaseAddition = 0;
            }
        }
        if (anInt658 > 20) {
            anInt658 = 0;
        }
		if (sleeping) {
			ignoreNext = true;
			if (super.enteredText.length() > 0) {
				super.streamClass.createPacket(70);
				super.streamClass.addString(super.enteredText);
				super.streamClass.formatPacket();
				super.inputText = "";
				super.enteredText = "";
				kfr = "Please wait...";
			}
			if (super.lastMouseDownButton == 1 && super.mouseX > 159 && super.mouseX < 220 && super.mouseY > 290 && super.mouseY < 318) {
				super.streamClass.createPacket(70);
				super.streamClass.addString("");
				super.streamClass.formatPacket();
				super.inputText = "";
				super.enteredText = "";
				kfr = "Please wait...";
			}
			return;
        }
        if (super.mouseY > windowHeight - 4) {
            if (super.mouseX > windowWidth / 2 - 241 && super.mouseX < windowWidth / 2 - 160 && super.lastMouseDownButton == 1)
                messagesTab = 0;
            if (super.mouseX > windowWidth / 2 - 146 && super.mouseX < windowWidth / 2 - 62 && super.lastMouseDownButton == 1) {
                messagesTab = 1;
                gameMenu.anIntArray187[messagesHandleType2] = 0xf423f;
            }
            if (super.mouseX > windowWidth / 2 - 41 && super.mouseX < windowWidth / 2 + 39 && super.lastMouseDownButton == 1) {
                messagesTab = 2;
                gameMenu.anIntArray187[messagesHandleType5] = 0xf423f;
            }
            if (super.mouseX > windowWidth / 2 + 59 && super.mouseX < windowWidth / 2 + 139 && super.lastMouseDownButton == 1) {
                messagesTab = 3;
                gameMenu.anIntArray187[messagesHandleType6] = 0xf423f;
            }
            if (super.mouseX > windowWidth / 2 + 159 && super.mouseX < windowWidth / 2 + 239 && super.lastMouseDownButton == 1) {
                showAbuseWindow = 1;
                abuseSelectedType = 0;
                super.inputText = "";
                super.enteredText = "";
            }
            super.lastMouseDownButton = 0;
            super.mouseDownButton = 0;
        }
        gameMenu.updateActions(super.mouseX, super.mouseY, super.lastMouseDownButton, super.mouseDownButton);
        if (messagesTab > 0 && super.mouseX >= 494 && super.mouseY >= windowHeight - 66)
            super.lastMouseDownButton = 0;
        if (gameMenu.hasActivated(chatHandle)) {
			String s = lastMessage = gameMenu.getText(chatHandle);
			gameMenu.updateText(chatHandle, "");
			if(ignoreNext) {
				ignoreNext = false;
				return;
			}
			
			if (s.startsWith("::")) {
				s = s.substring(2);
				if (!handleCommand(s) && !sleeping && !ignoreNext) {
					sendChatString(s);
					if (messages.size() == 0 || !messages.get(messages.size() - 1).equalsIgnoreCase("::" + s)) {
						messages.add("::" + s);
						currentChat = messages.size();
					} else if (messages.get(messages.size() - 1).equalsIgnoreCase("::" + s))
						currentChat = messages.size();				
				}
			} else if (!sleeping && !ignoreNext) {
                byte[] chatMessage = DataConversions.stringToByteArray(ChatFilter.censorChat(s));
                sendChatMessage(chatMessage, chatMessage.length);
                s = DataConversions.byteToString(chatMessage, 0, chatMessage.length).trim();				
				if (messages.size() == 0 || !messages.get(messages.size() - 1).equalsIgnoreCase(s)) {
					messages.add(s);
					currentChat = messages.size();
				} else if (messages.get(messages.size() - 1).equalsIgnoreCase(s))
					currentChat = messages.size();
                ourPlayer.lastMessageTimeout = 150;
                ourPlayer.lastMessage = s;
				displayRegularChat(ourPlayer.name, ourPlayer.admin, ourPlayer.cName, s);
            }
        }		
		
        if (messagesTab == 0) {
            for (int l1 = 0; l1 < messagesTimeout.length; l1++)
                if (messagesTimeout[l1] > 0)
                    messagesTimeout[l1]--;
        }
        if (playerAliveTimeout != 0)
            super.lastMouseDownButton = 0;
        if (showTradeWindow || showDuelWindow) {
            if (super.mouseDownButton != 0)
                mouseDownTime++;
            else
                mouseDownTime = 0;
            if (mouseDownTime > 500)
            	itemIncrement += 100000;
            else if (mouseDownTime > 350)
                itemIncrement += 10000;
            else if (mouseDownTime > 250)
                itemIncrement += 1000;
            else if (mouseDownTime > 150)
                itemIncrement += 100;
            else if (mouseDownTime > 100)
                itemIncrement += 10;
            else if (mouseDownTime > 50)
                itemIncrement++;
            else if (mouseDownTime > 20 && (mouseDownTime & 5) == 0)
                itemIncrement++;
        } else {
            mouseDownTime = 0;
            itemIncrement = 0;
        }
        if (super.lastMouseDownButton == 1)
            mouseButtonClick = 1;
        else if (super.lastMouseDownButton == 2)
            mouseButtonClick = 2;
        gameCamera.updateMouseCoords(super.mouseX, super.mouseY);
        super.lastMouseDownButton = 0;
        if (cameraRotate) {
            if (cameraRotationBaseAddition == 0 || cameraAutoAngleDebug) {
                if (super.keyLeftDown) {
                    cameraAutoAngle = cameraAutoAngle + 1 & 7;
                    super.keyLeftDown = false;
                    if (!zoomCamera) {
                        if ((cameraAutoAngle & 1) == 0)
                            cameraAutoAngle = cameraAutoAngle + 1 & 7;
                        for (int i2 = 0; i2 < 8; i2++) {
                            if (enginePlayerVisible(cameraAutoAngle))
                                break;
                            cameraAutoAngle = cameraAutoAngle + 1 & 7;
                        }

                    }
                }
                if (super.keyRightDown) {
                    cameraAutoAngle = cameraAutoAngle + 7 & 7;
                    super.keyRightDown = false;
                    if (!zoomCamera) {
                        if ((cameraAutoAngle & 1) == 0)
                            cameraAutoAngle = cameraAutoAngle + 7 & 7;
                        for (int j2 = 0; j2 < 8; j2++) {
                            if (enginePlayerVisible(cameraAutoAngle))
                                break;
                            cameraAutoAngle = cameraAutoAngle + 7 & 7;
                        }

                    }
                }
            }
        } else if (super.keyLeftDown)
            cameraRotation = cameraRotation + 2 & 0xff;
        else if (super.keyRightDown)
            cameraRotation = cameraRotation - 2 & 0xff;
        if (zoomCamera && cameraHeight > 900)
            cameraHeight -= 4;
        else if (!zoomCamera && cameraHeight < 500)
            cameraHeight += 4;
        if (actionPictureType > 0)
            actionPictureType--;
        else if (actionPictureType < 0)
            actionPictureType++;
        gameCamera.method301(17);
        modelUpdatingTimer++;
        if (modelUpdatingTimer > 5) {
            modelUpdatingTimer = 0;
            modelFireLightningSpellNumber = (modelFireLightningSpellNumber + 1) % 3;
            modelTorchNumber = (modelTorchNumber + 1) % 4;
            modelClawSpellNumber = (modelClawSpellNumber + 1) % 5;
        }
        for (int k2 = 0; k2 < objectCount; k2++) {
            int l3 = objectX[k2];
            int l4 = objectY[k2];
            if (l3 >= 0 && l4 >= 0 && l3 < 96 && l4 < 96 && objectType[k2] == 74)
                objectModelArray[k2].method188(1, 0, 0);
        }

        for (int i4 = 0; i4 < teleportBubbleCount; i4++) {
            anIntArray923[i4]++;
            if (anIntArray923[i4] > 50) {
                teleportBubbleCount--;
                for (int i5 = i4; i5 < teleportBubbleCount; i5++) {
                    YKVE_mudclient_LQSP_2[i5] = YKVE_mudclient_LQSP_2[i5 + 1];
                    anIntArray757[i5] = anIntArray757[i5 + 1];
                    anIntArray923[i5] = anIntArray923[i5 + 1];
                    anIntArray782[i5] = anIntArray782[i5 + 1];
                }

            }
        }
    }

    private final void loadSounds() {
        try {
            //drawLoadingBarText(90, "Unpacking Sound Effects...");
            sounds = load("sounds");
            audioReader = new AudioReader();
        } catch (Throwable throwable) {}
    }

    private final void drawCombatStyleWindow() {
        byte byte0 = 7;
        byte byte1 = 15;
        char c = '\257';
        if (mouseButtonClick != 0) {
            for (int i = 0; i < 5; i++) {
                if (i <= 0 || super.mouseX <= byte0 || super.mouseX >= byte0 + c || super.mouseY <= byte1 + i * 20 || super.mouseY >= byte1 + i * 20 + 20)
                    continue;
                combatStyle = i - 1;
                mouseButtonClick = 0;
                super.streamClass.createPacket(64);
                super.streamClass.addByte(combatStyle);
                super.streamClass.formatPacket();
                break;
            }

        }
        for (int j = 0; j < 5; j++) {
            if (j == combatStyle + 1)
                drawBoxAlpha(byte0, byte1 + j * 20, c, 20, GameImage.convertRGBToLong(255, 0, 0), 128);
            else
                drawBoxAlpha(byte0, byte1 + j * 20, c, 20, GameImage.convertRGBToLong(190, 190, 190), 128);
            drawLineX(byte0, byte1 + j * 20, c, 0);
            drawLineX(byte0, byte1 + j * 20 + 20, c, 0);
        }

        drawText("Combat Style", byte0 + c / 2, byte1 + 16, 3, 0xffffff);
        drawText("Controlled (+1 of each)", byte0 + c / 2, byte1 + 36, 3, 0);
        drawText("Aggressive (+3 strength)", byte0 + c / 2, byte1 + 56, 3, 0);
        drawText("Accurate   (+3 attack)", byte0 + c / 2, byte1 + 76, 3, 0);
        drawText("Defensive  (+3 defense)", byte0 + c / 2, byte1 + 96, 3, 0);
    }

    private final void drawDuelConfirmWindow() {
        int byte0 = gameWidth / 2 - 234;
        int byte1 = gameHeight - (gameHeight / 2 + 131);
        gameGraphics.drawBox(byte0, byte1, 468, 16, 192);
        int i = 0x989898;
        drawBoxAlpha(byte0, byte1 + 16, 468, 246, i, 160);
        drawText("Please confirm your duel with @yel@" + DataOperations.longToString(duelOpponentNameLong), byte0 + 234, byte1 + 12, 1, 0xffffff);
        drawText("Your stake:", byte0 + 117, byte1 + 30, 1, 0xffff00);
        for (int j = 0; j < duelConfirmMyItemCount; j++) {
            String s = EntityHandler.getItemDef(duelConfirmMyItems[j]).getName();
            if (EntityHandler.getItemDef(duelConfirmMyItems[j]).isStackable())
                s = s + " x " + appendUnits(duelConfirmMyItemsCount[j]);
            drawText(s, byte0 + 117, byte1 + 42 + j * 12, 1, 0xffffff);
        }

        if (duelConfirmMyItemCount == 0)
            drawText("Nothing!", byte0 + 117, byte1 + 42, 1, 0xffffff);
        drawText("Your opponent's stake:", byte0 + 351, byte1 + 30, 1, 0xffff00);
        for (int k = 0; k < duelConfirmOpponentItemCount; k++) {
            String s1 = EntityHandler.getItemDef(duelConfirmOpponentItems[k]).getName();
            if (EntityHandler.getItemDef(duelConfirmOpponentItems[k]).isStackable())
                s1 = s1 + " x " + appendUnits(duelConfirmOpponentItemsCount[k]);
            drawText(s1, byte0 + 351, byte1 + 42 + k * 12, 1, 0xffffff);
        }

        if (duelConfirmOpponentItemCount == 0)
            drawText("Nothing!", byte0 + 351, byte1 + 42, 1, 0xffffff);
        if (duelCantRetreat == 0)
            drawText("You can retreat from this duel", byte0 + 234, byte1 + 180, 1, 65280);
        else
            drawText("No retreat is possible!", byte0 + 234, byte1 + 180, 1, 0xff0000);
        if (duelUseMagic == 0)
            drawText("Magic may be used", byte0 + 234, byte1 + 192, 1, 65280);
        else
            drawText("Magic cannot be used", byte0 + 234, byte1 + 192, 1, 0xff0000);
        if (duelUsePrayer == 0)
            drawText("Prayer may be used", byte0 + 234, byte1 + 204, 1, 65280);
        else
            drawText("Prayer cannot be used", byte0 + 234, byte1 + 204, 1, 0xff0000);
        if (duelUseWeapons == 0)
            drawText("Weapons may be used", byte0 + 234, byte1 + 216, 1, 65280);
        else
            drawText("Weapons cannot be used", byte0 + 234, byte1 + 216, 1, 0xff0000);
        drawText("If you are sure click 'Accept' to begin the duel", byte0 + 234, byte1 + 230, 1, 0xffffff);
        if (!duelWeAccept) {
            gameGraphics.drawPicture((byte0 + 118) - 35, byte1 + 238, SPRITE_MEDIA_START + 25);
            gameGraphics.drawPicture((byte0 + 352) - 35, byte1 + 238, SPRITE_MEDIA_START + 26);
        } else
            drawText("Waiting for other player...", byte0 + 234, byte1 + 250, 1, 0xffff00);
        if (mouseButtonClick == 1) {
            if (super.mouseX < byte0 || super.mouseY < byte1 || super.mouseX > byte0 + 468 || super.mouseY > byte1 + 262) {
                showDuelConfirmWindow = false;
                super.streamClass.createPacket(51);
                super.streamClass.formatPacket();
            }
            if (super.mouseX >= (byte0 + 118) - 35 && super.mouseX <= byte0 + 118 + 70 && super.mouseY >= byte1 + 238 && super.mouseY <= byte1 + 238 + 21) {
                duelWeAccept = true;
                super.streamClass.createPacket(50);
                super.streamClass.formatPacket();
            }
            if (super.mouseX >= (byte0 + 352) - 35 && super.mouseX <= byte0 + 353 + 70 && super.mouseY >= byte1 + 238 && super.mouseY <= byte1 + 238 + 21) {
                showDuelConfirmWindow = false;
                super.streamClass.createPacket(51);
                super.streamClass.formatPacket();
            }
            mouseButtonClick = 0;
        }
    }

    private final void updateBankItems() {
        bankItemCount = newBankItemCount;
        for (int i = 0; i < newBankItemCount; i++) {
            bankItems[i] = newBankItems[i];
            bankItemsCount[i] = newBankItemsCount[i];
        }

        for (int j = 0; j < inventoryCount; j++) {
            if (bankItemCount >= bankItemsMax)
                break;
            int k = inventoryItems[j];
            boolean flag = false;
            for (int l = 0; l < bankItemCount; l++) {
                if (bankItems[l] != k)
                    continue;
                flag = true;
                break;
            }

            if (!flag) {
                bankItems[bankItemCount] = k;
                bankItemsCount[bankItemCount] = 0;
                bankItemCount++;
            }
        }

    }

    private final void makeCharacterDesignMenu() {
        int i = (gameWidth - 215) / 2; //140
        int j = gameHeight / 2 - 141; //34
        characterDesignMenu = new Menu(gameGraphics, 100);
        characterDesignMenu.drawText(i + 116, j - 16, "Please design Your Character", 4, true); //256 10
        i += 116;
        j -= 10;
        characterDesignMenu.drawText(i - 55, j + 110, "Front", 3, true);
        characterDesignMenu.drawText(i, j + 110, "Side", 3, true);
        characterDesignMenu.drawText(i + 55, j + 110, "Back", 3, true);
        byte byte0 = 54;
        j += 145;
        characterDesignMenu.method157(i - byte0, j, 53, 41);
        characterDesignMenu.drawText(i - byte0, j - 8, "Head", 1, true);
        characterDesignMenu.drawText(i - byte0, j + 8, "Type", 1, true);
        characterDesignMenu.method158(i - byte0 - 40, j, SPRITE_UTIL_START + 7);
        characterDesignHeadButton1 = characterDesignMenu.makeButton(i - byte0 - 40, j, 20, 20);
        characterDesignMenu.method158((i - byte0) + 40, j, SPRITE_UTIL_START + 6);
        characterDesignHeadButton2 = characterDesignMenu.makeButton((i - byte0) + 40, j, 20, 20);
        characterDesignMenu.method157(i + byte0, j, 53, 41);
        characterDesignMenu.drawText(i + byte0, j - 8, "Hair", 1, true);
        characterDesignMenu.drawText(i + byte0, j + 8, "Colour", 1, true);
        characterDesignMenu.method158((i + byte0) - 40, j, SPRITE_UTIL_START + 7);
        characterDesignHairColourButton1 = characterDesignMenu.makeButton((i + byte0) - 40, j, 20, 20);
        characterDesignMenu.method158(i + byte0 + 40, j, SPRITE_UTIL_START + 6);
        characterDesignHairColourButton2 = characterDesignMenu.makeButton(i + byte0 + 40, j, 20, 20);
        j += 50;
        characterDesignMenu.method157(i - byte0, j, 53, 41);
        characterDesignMenu.drawText(i - byte0, j, "Gender", 1, true);
        characterDesignMenu.method158(i - byte0 - 40, j, SPRITE_UTIL_START + 7);
        characterDesignGenderButton1 = characterDesignMenu.makeButton(i - byte0 - 40, j, 20, 20);
        characterDesignMenu.method158((i - byte0) + 40, j, SPRITE_UTIL_START + 6);
        characterDesignGenderButton2 = characterDesignMenu.makeButton((i - byte0) + 40, j, 20, 20);
        characterDesignMenu.method157(i + byte0, j, 53, 41);
        characterDesignMenu.drawText(i + byte0, j - 8, "Top", 1, true);
        characterDesignMenu.drawText(i + byte0, j + 8, "Colour", 1, true);
        characterDesignMenu.method158((i + byte0) - 40, j, SPRITE_UTIL_START + 7);
        characterDesignTopColourButton1 = characterDesignMenu.makeButton((i + byte0) - 40, j, 20, 20);
        characterDesignMenu.method158(i + byte0 + 40, j, SPRITE_UTIL_START + 6);
        characterDesignTopColourButton2 = characterDesignMenu.makeButton(i + byte0 + 40, j, 20, 20);
        j += 50;
        characterDesignMenu.method157(i - byte0, j, 53, 41);
        characterDesignMenu.drawText(i - byte0, j - 8, "Skin", 1, true);
        characterDesignMenu.drawText(i - byte0, j + 8, "Colour", 1, true);
        characterDesignMenu.method158(i - byte0 - 40, j, SPRITE_UTIL_START + 7);
        characterDesignSkinColourButton1 = characterDesignMenu.makeButton(i - byte0 - 40, j, 20, 20);
        characterDesignMenu.method158((i - byte0) + 40, j, SPRITE_UTIL_START + 6);
        characterDesignSkinColourButton2 = characterDesignMenu.makeButton((i - byte0) + 40, j, 20, 20);
        characterDesignMenu.method157(i + byte0, j, 53, 41);
        characterDesignMenu.drawText(i + byte0, j - 8, "Bottom", 1, true);
        characterDesignMenu.drawText(i + byte0, j + 8, "Colour", 1, true);
        characterDesignMenu.method158((i + byte0) - 40, j, SPRITE_UTIL_START + 7);
        characterDesignBottomColourButton1 = characterDesignMenu.makeButton((i + byte0) - 40, j, 20, 20);
        characterDesignMenu.method158(i + byte0 + 40, j, SPRITE_UTIL_START + 6);
        characterDesignBottomColourButton2 = characterDesignMenu.makeButton(i + byte0 + 40, j, 20, 20);
        j += 82;
        j -= 35;
        characterDesignMenu.drawBox(i, j, 200, 30);
        characterDesignMenu.drawText(i, j, "Accept", 4, false);
        characterDesignAcceptButton = characterDesignMenu.makeButton(i, j, 200, 30);
    }	
	
    private final void drawAbuseWindow2() {

    }
    
	private final void printMessage(String message) {
		displayMessage(message, 5, 0);
	}
	
	private final byte ADMIN = (byte)1;
	private final byte MOD = (byte)2;
	private final byte PMOD = (byte)6;
	private final byte CMOD = (byte)5;
	private final byte DEV = (byte)3;
	private final byte SUB = (byte)7;
	
	private final String getNameRankSprite(int rank) {
		switch(rank) {
			case ADMIN:
				return "#adm#";
			case MOD:
				return "#mod#";
			case DEV:
				return "#dev#";
			case PMOD:
				return "#pmd#";
			case CMOD:
				return "#cmd#";
			default:
				return "";
		}
	}
	
	private final String getNameRankColour(int rank) {
		switch(rank) {
			case ADMIN:
				return "@yel@";
			case MOD:
				return "@whi@";
			case DEV:
				return "@red@";
			case PMOD:
				return "@gre@";
			case CMOD:
				return "@blu@";
			case SUB:
				return "@or2@";
			default:
				return "@yel@";
		}
	}
	
	protected final void displayGlobalChat(String mobName, int rank, String cName, String message) {
		if (loggingEnabled)
			logEvent("[GLOBAL] " + mobName + ": " + ChatFilter.censorChat(message), 2);
		String header = "";
		if(!cName.equals("") && !ourPlayer.cName.equals("")) {
			header += "@yel@[" + cName.toUpperCase() + "] ";
		}
		header += getNameRankColour(rank) + getNameRankSprite(rank) + mobName + ":@whi@ ";
		message = header + ChatFilter.censorChat(message);
		if (messagesTab != 0 && messagesTab != 5) {
			anInt954 = 200;
		}
		for (int idx = 4; idx > 0; idx--) {
			messagesArray[idx] = messagesArray[idx - 1];
			messagesTimeout[idx] = messagesTimeout[idx - 1];
		}
		messagesArray[0] = message;
		messagesTimeout[0] = 300;
		if (gameMenu.anIntArray187[messagesHandleType5] == gameMenu.menuListTextCount[messagesHandleType5] - 4) {
			gameMenu.addString(messagesHandleType5, message, true);
		} else {
			gameMenu.addString(messagesHandleType5, message, false);
		}
	}

	protected final void displayRegularChat(String mobName, int rank, String cName, String message) {
		if (loggingEnabled)
			logEvent(mobName + ": " + ChatFilter.censorChat(message), 1);
		String nameColour = getNameRankColour(rank);
		String nameSprite = getNameRankSprite(rank);
		String header = "";
		if (!cName.equals("") && !ourPlayer.cName.equals(""))
			header += "@yel@[" + cName.toUpperCase() + "] ";
		header += nameColour + nameSprite + mobName + ":@yel@ ";
		message = header + ChatFilter.censorChat(message);
		if (messagesTab != 0 && messagesTab != 2)
			anInt954 = 200;
		for (int k = messagesArray.length - 1; k > 0; k--) {
			messagesArray[k] = messagesArray[k - 1];
			messagesTimeout[k] = messagesTimeout[k - 1];
		}
		
		messagesArray[0] = message;
		messagesTimeout[0] = 300;
		if (gameMenu.anIntArray187[messagesHandleType2] == gameMenu.menuListTextCount[messagesHandleType2] - 4)
			gameMenu.addString(messagesHandleType2, message, true);
		else
			gameMenu.addString(messagesHandleType2, message, false);
	}
	
	protected final void displayNpcMessage(String npcMessage) {
		if (messagesTab != 2 && messagesTab != 0) {
			anInt954 = 200;
		}
		for (int k = 4; k > 0; k--) {
            messagesArray[k] = messagesArray[k - 1];
            messagesTimeout[k] = messagesTimeout[k - 1];
        }
        messagesArray[0] = npcMessage;
        messagesTimeout[0] = 300;
		if (gameMenu.anIntArray187[messagesHandleType5] == gameMenu.menuListTextCount[messagesHandleType5] - 4) {
			gameMenu.addString(messagesHandleType5, npcMessage, true);
		} else {
			gameMenu.addString(messagesHandleType5, npcMessage, false);
		}
	}
	
	protected final void displayQuestMessage(String questMessage) {
		if (messagesTab != 2 && messagesTab != 0) {
			anInt954 = 200;
		}
		for (int k = 4; k > 0; k--) {
			messagesArray[k] = messagesArray[k - 1];
			messagesTimeout[k] = messagesTimeout[k - 1];
		}
		messagesArray[0] = questMessage;
		messagesTimeout[0] = 300;
		if (gameMenu.anIntArray187[messagesHandleType5] == gameMenu.menuListTextCount[messagesHandleType5] - 4) {
			gameMenu.addString(messagesHandleType5, questMessage, true);
		} else {
			gameMenu.addString(messagesHandleType5, questMessage, false);
		}
	}
	
	protected final void displayGenericMessage(String message, int chatTab) {
		if (messagesTab != 0) {
			if (messagesTab != chatTab)
				anInt955 = 200;
			if (messagesTab != chatTab)
				messagesTab = 0;
		}
		for (int k = 4; k > 0; k--) {
			messagesArray[k] = messagesArray[k - 1];
			messagesTimeout[k] = messagesTimeout[k - 1];
		}
		messagesArray[0] = message;
		messagesTimeout[0] = 300;
		int handleType = 5;
		switch(chatTab) {
			case 2:
				handleType = messagesHandleType2;
				break;
			case 5:
			case 7:
				handleType = messagesHandleType5;
				break;
			case 6:
				handleType = messagesHandleType6;
		}
		if (gameMenu.anIntArray187[handleType] == gameMenu.menuListTextCount[handleType] - 4) {
			gameMenu.addString(handleType, message, true);
			return;
		}
		gameMenu.addString(handleType, "@whi@" + message, false);
	}

	protected final void displayPrivateMessage(long mobUsernameHash, int rank, int clanHash, String message) {
		if (loggingEnabled)
			logEvent("[PM] " + DataConversions.hashToUsername(mobUsernameHash) + ": " + ChatFilter.censorChat(message), 3);
		String header = "";
		if(clanHash != 0 && !ourPlayer.cName.equals("")) {
			header += "@yel@[";
			header += DataConversions.hashToClanTag(clanHash).toUpperCase();
			header += "] ";
		}
		String rankSprite = getNameRankSprite(rank);
		message = "@cya@" + rankSprite + DataConversions.hashToUsername(mobUsernameHash) + " tells you: " + ChatFilter.censorChat(message);
		if (messagesTab != 0) {
			if (messagesTab != 3)
				anInt955 = 200;
			if (messagesTab != 3 && messagesTab != 0)
				messagesTab = 0;
		}
		for (int k = 4; k > 0; k--) {
			messagesArray[k] = messagesArray[k - 1];
			messagesTimeout[k] = messagesTimeout[k - 1];
		}
		messagesArray[0] = message;
		messagesTimeout[0] = 300;
		if (gameMenu.anIntArray187[messagesHandleType6] == gameMenu.menuListTextCount[messagesHandleType6] - 4) {
			gameMenu.addString(messagesHandleType6, message, true);
			return;
		}
		gameMenu.addString(messagesHandleType6, "@whi@" + message, false);
	}
	
	private final void displayMessage(String cName, String message, int type, int mobRank) {
		String header = "";
        if (type == 2 || type == 4 || type == 6 || type == 7) {
            for (; message.length() > 5 && message.charAt(0) == '@' && message.charAt(4) == '@'; message = message.substring(5));
			if(message.length() > 5 && message.charAt(0) == '#' && message.charAt(4) == '#') {
				header = message.substring(0, 5);
				message = header + ChatFilter.censorChat(message.substring(5));
			} else {
				message = ChatFilter.censorChat(message);
			}
        }
		String nameColour = "@yel@";
		switch(mobRank) {
			case ADMIN:
				message = "#adm#" + message;
				break;
			case MOD:
				message = "#mod#" + message;
				nameColour = "@whi@";
				break;
			case DEV:
				message = "#dev#" + message;
				nameColour = "@red@";
				break;
			case PMOD:
				message = "#pmd#" + message;
				nameColour = "@gre@";
				break;
			case CMOD:
				message = "#cmd#" + message;
				nameColour = "@blu@";
				break;
			case SUB:
				nameColour = "@or2@";
			default:
		}
		  switch(type) {
			case 5:
				message = "@whi@" + message;
				break;
			case 7:
				String saySubStr = "";
				int sayCounter = 0;
				for(int i = 0; i < message.length(); i++) {
					if(message.charAt(i) != ':') {
						saySubStr += message.charAt(i);
					} else {
						saySubStr += message.charAt(i);
						break;
					}
					sayCounter++;
				}
				saySubStr = saySubStr.replaceAll("_"," "); 
				message = "@yel@" + saySubStr + "@whi@" + message.substring(sayCounter + 1, message.length());
				if(!cName.equals("")) {
					message = "[" + cName + "] " + message;
				}
				break;
			case 2:
				String subStr = "";
				int counter = 0;
				for(int i = 0; i < message.length(); i++) {
					if (message.charAt(i) != ':')
						subStr += message.charAt(i);
					else {
						subStr += message.charAt(i);
						break;
					}
					counter++;
				}
				subStr = subStr.replaceAll("_"," "); 
				message = nameColour + subStr + "@yel@" + message.substring(counter + 1, message.length());
				break;
			default:
				message = "@whi@" + message;
		  }

		if (type == 3)
			message = "@whi@" + message;

        if (messagesTab != 0) {
            if (type == 4 || type == 3)
                anInt952 = 200;
            if (type == 2 && messagesTab != 1)
                anInt953 = 200;
            if ((type == 5 || type == 7) && messagesTab != 2)
                anInt954 = 200;
            if (type == 6 && messagesTab != 3)
                anInt955 = 200;
            if (type == 3 && messagesTab != 0)
                messagesTab = 0;
            if (type == 6 && messagesTab != 3 && messagesTab != 0)
                messagesTab = 0;
        }
		
		for (int k = messagesArray.length - 1; k > 0; k--) {
			messagesArray[k] = messagesArray[k - 1];
			messagesTimeout[k] = messagesTimeout[k - 1];
		}
		
        messagesArray[0] = message;
        messagesTimeout[0] = 300;
        if (type == 2)
            if (gameMenu.anIntArray187[messagesHandleType2] == gameMenu.menuListTextCount[messagesHandleType2] - 4)
                gameMenu.addString(messagesHandleType2, message, true);
            else
                gameMenu.addString(messagesHandleType2, message, false);
        if (type == 5 || type == 7)
            if (gameMenu.anIntArray187[messagesHandleType5] == gameMenu.menuListTextCount[messagesHandleType5] - 4)
                gameMenu.addString(messagesHandleType5, message, true);
            else
                gameMenu.addString(messagesHandleType5, message, false);
        if (type == 6) {
            if (gameMenu.anIntArray187[messagesHandleType6] == gameMenu.menuListTextCount[messagesHandleType6] - 4) {
                gameMenu.addString(messagesHandleType6, message, true);
                return;
            }
            gameMenu.addString(messagesHandleType6, "@cya@" + message, false);
        }
	}
	
   private final void displayMessage(String message, int type, int status) {
		displayMessage("", message, type, status);
    }

	protected final void logoutAndStop() {
		garbageCollect();
		if (audioReader != null)
			audioReader.stopAudio();
		for (String s : quests) {
			s = s.replaceAll("@yel@", "@red@");
			s = s.replaceAll("@gre@", "@red@");
		}
	}

    private final void something3DModel(int i, String s) {
        int j = objectX[i];
        int k = objectY[i];
        int l = j - ourPlayer.currentX / 128;
        int i1 = k - ourPlayer.currentY / 128;
        byte byte0 = 7;
        if (j >= 0 && k >= 0 && j < 96 && k < 96 && l > -byte0 && l < byte0 && i1 > -byte0 && i1 < byte0) {
            gameCamera.removeModel(objectModelArray[i]);
            int j1 = EntityHandler.storeModel(s);
            try {
	            Model model = gameDataModels[j1].method203();
	            gameCamera.addModel(model);
	            model.method184(true, 48, 48, -50, -10, -50);
	            model.method205(objectModelArray[i]);
	            model.anInt257 = i;
	            objectModelArray[i] = model;
            } catch(Exception e) {}
        }
    }
	
    protected final void resetVars() {
		super.inputText = "";
		super.enteredText = "";
		wildernessUpdate = 0;
        systemUpdate = 0;
        combatStyle = 0;
        logoutTimeout = 0;
        loginScreenNumber = 0;
        loggedIn = 1;
        resetPrivateMessageStrings();
        gameGraphics.method211();
        gameGraphics.drawImage(aGraphics936, 0, 0);
        for (int i = 0; i < objectCount; i++) {
            gameCamera.removeModel(objectModelArray[i]);
            engineHandle.updateObject(objectX[i], objectY[i], objectType[i], objectID[i]);
        }

        for (int j = 0; j < doorCount; j++) {
            gameCamera.removeModel(doorModel[j]);
            engineHandle.updateDoor(doorX[j], doorY[j], doorDirection[j], doorType[j]);
        }

		for (String s : quests) {
			s = s.replaceAll("@yel@", "@red@");
			s = s.replaceAll("@gre@", "@red@");
		}
		
        objectCount = 0;
        doorCount = 0;
        groundItemCount = 0;
        playerCount = 0;
        for (int k = 0; k < mobArray.length; k++)
            mobArray[k] = null;

        for (int l = 0; l < playerArray.length; l++)
            playerArray[l] = null;

        npcCount = 0;
        for (int i1 = 0; i1 < npcRecordArray.length; i1++)
            npcRecordArray[i1] = null;

        for (int j1 = 0; j1 < npcArray.length; j1++)
            npcArray[j1] = null;

        for (int k1 = 0; k1 < prayerOn.length; k1++)
            prayerOn[k1] = false;

        mouseButtonClick = 0;
        super.lastMouseDownButton = 0;
        super.mouseDownButton = 0;
        showShop = false;
        showBank = false;
        super.friendsCount = 0;
    }
    private boolean allowSendCommand = false;
	private final void drawTradeWindow() {
		if(clickScreenSend) {
			mouseButtonClick = 4;
			clickScreenSend = false;
		}
		int i = super.mouseX - (gameWidth - (gameWidth / 2 + 234));
		int j = super.mouseY - (gameHeight / 2 - 139);
		if (System.currentTimeMillis() - lastTradeDuelUpdate > 50) {
			boolean qItem = false;
			if (mudclient.inputBoxType > 3 && mudclient.inputBoxType < 10)
				allowSendCommand = false;
			else
				allowSendCommand = true;
			if(allowSendCommand)
			if (mouseButtonClick != 0 && itemIncrement == 0)
				itemIncrement = 1;
			if (itemIncrement > 0) {
				if (i >= 0 && j >= 0 && i < 468 && j < 262) {
					if (i > 216 && j > 30 && i < 462 && j < 235) {
						int k = (i - 217) / 49 + ((j - 31) / 34) * 5;
						if (k >= 0 && k < inventoryCount) {
							boolean flag = false;
							int l1 = 0;
							int k2 = inventoryItems[k];
							if (EntityHandler.getItemDef(k2).quest) {
								if (!qItem)
									displayMessage("@cya@You cannot trade quest items", 3, 0);
								qItem = true;
							}
							if (!qItem)
								if (mouseButtonClick != 2 && !tester && mouseButtonClick != 4) {
									for (int k3 = 0; k3 < tradeMyItemCount; k3++)
										if (tradeMyItems[k3] == k2)
											if (EntityHandler.getItemDef(k2).isStackable()) {
												for (int i4 = 0; i4 < itemIncrement; i4++) {
													if (tradeMyItemsCount[k3] < inventoryItemsCount[k])
														tradeMyItemsCount[k3]++;
													flag = true;
												}

											} else
												l1++;
								} else if (mouseButtonClick == 2) {
									if (tester) {
										tradeWindowX = -100;
										tradeWindowY = -100;
										mouseButtonClick = 0;
										tester = false;
										setValue = false;
									} else if (!tester) {
										tradeWindowX = super.mouseX;
										tradeWindowY = super.mouseY;

										for (int jx = 0; jx < menuLength; jx++) {
											menuText1[jx] = null;
											menuText2[jx] = null;
											menuActionVariable[jx] = -1;
											menuActionVariable2[jx] = -1;
											menuID[jx] =-1;
										}
										String name = EntityHandler.getItemDef(k2).getName();
										
										menuLength = 0;								
										menuText1[menuLength] = "Offer 1@lre@";
										menuText2[menuLength] = name;
										menuID[menuLength] = 782;
										menuActionVariable[menuLength] = k2;
										menuActionVariable2[menuLength] = 1;
										menuLength++;
										
										menuText1[menuLength] = "Offer 5@lre@";
										menuText2[menuLength] = name;
										menuID[menuLength] = 782;
										menuActionVariable[menuLength] = k2;
										menuActionVariable2[menuLength] = 5;
										menuLength++;
										
										menuText1[menuLength] = "Offer 10@lre@";
										menuText2[menuLength] = name;
										menuID[menuLength] = 782;
										menuActionVariable[menuLength] = k2;
										menuActionVariable2[menuLength] = 10;
										menuLength++;
										
										menuText1[menuLength] = "Offer All@lre@";
										menuText2[menuLength] = name;
										menuID[menuLength] = 782;
										menuActionVariable[menuLength] = k2;
										menuActionVariable2[menuLength] = inventoryCount(k2);
										menuLength++;
										
										menuText1[menuLength] = "Offer X@lre@";
										menuText2[menuLength] = name;
										menuID[menuLength] = 789; // Send that to the menu so we build the offer X
										menuActionVariable[menuLength] = k2;
										menuLength++;
										tester = true;
									}
								}
							if (!qItem) {
								if (inventoryCount(k2) <= l1)
									flag = true;
								if (mouseButtonClick != 2 && !tester && mouseButtonClick != 4) {
									if (!flag && tradeMyItemCount < 12) {
										tradeMyItems[tradeMyItemCount] = k2;
										tradeMyItemsCount[tradeMyItemCount] = 1;
										tradeMyItemCount++;
										flag = true;
									}
								}
							}
							if (!qItem)
								if (flag) {
									if (mouseButtonClick != 2 && !tester && mouseButtonClick != 4) {
										lastTradeDuelUpdate = System.currentTimeMillis();
										super.streamClass.createPacket(42);
										super.streamClass.addByte(tradeMyItemCount);
										for (int j4 = 0; j4 < tradeMyItemCount; j4++) {
											super.streamClass.add2ByteInt(tradeMyItems[j4]);
											super.streamClass.add4ByteInt(tradeMyItemsCount[j4]);
										}
										super.streamClass.formatPacket();
										tradeOtherAccepted = false;
										tradeWeAccepted = false;
									}
								}
						}
					}
					if (!qItem)
						if (i > 8 && j > 30 && i < 205 && j < 133) {
							int l = (i - 9) / 49 + ((j - 31) / 34) * 4;
							if (l >= 0 && l < tradeMyItemCount) {
								int j1 = tradeMyItems[l];
								if (mouseButtonClick != 2 && !tester) {
									for (int i2 = 0; i2 < itemIncrement; i2++) {
										if (EntityHandler.getItemDef(j1)
												.isStackable()
												&& tradeMyItemsCount[l] > 1) {
											tradeMyItemsCount[l]--;
											continue;
										}
										tradeMyItemCount--;
										mouseDownTime = 0;
										for (int l2 = l; l2 < tradeMyItemCount; l2++) {
											tradeMyItems[l2] = tradeMyItems[l2 + 1];
											tradeMyItemsCount[l2] = tradeMyItemsCount[l2 + 1];
										}

										break;
									}
								}
								if (mouseButtonClick == 2) {
									if (tester) {
										tradeWindowX = -100;
										tradeWindowY = -100;
										mouseButtonClick = 0;
										tester = false;
										setValue = false;
									} else if (!tester) {
										tradeWindowX = super.mouseX;
										tradeWindowY = super.mouseY;
										for (int jx = 0; jx < menuLength; jx++) {
											menuText1[jx] = null;
											menuText2[jx] = null;
											menuActionVariable[jx] = -1;
											menuActionVariable2[jx] = -1;
											menuID[jx] = -1;
										}
										String name = EntityHandler.getItemDef(j1).getName();

										menuLength = 0;
										menuText1[menuLength] = "Remove 1@lre@";
										menuText2[menuLength] = name;
										menuID[menuLength] = 783;
										menuActionVariable[menuLength] = j1;
										menuActionVariable2[menuLength] = 1;
										menuLength++;

										menuText1[menuLength] = "Remove 5@lre@";
										menuText2[menuLength] = name;
										menuID[menuLength] = 783;
										menuActionVariable[menuLength] = j1;
										menuActionVariable2[menuLength] = 5;
										menuLength++;

										menuText1[menuLength] = "Remove 10@lre@";
										menuText2[menuLength] = name;
										menuID[menuLength] = 783;
										menuActionVariable[menuLength] = j1;
										menuActionVariable2[menuLength] = 10;
										menuLength++;

										menuText1[menuLength] = "Remove All@lre@";
										menuText2[menuLength] = name;
										menuID[menuLength] = 783;
										menuActionVariable[menuLength] = j1;
										menuActionVariable2[menuLength] = 0;
										menuActionType[menuLength] = 1234;
										menuLength++;
										
										menuText1[menuLength] = "Remove X@lre@";
										menuText2[menuLength] = name;
										menuID[menuLength] = 881;
										menuActionVariable[menuLength] = j1;
										menuActionVariable2[menuLength] = -1;
										menuLength++;
										tester = true;
									}
								} else if (mouseButtonClick == 1 && !tester && mouseButtonClick != 4) {
									lastTradeDuelUpdate = System
											.currentTimeMillis();
									super.streamClass.createPacket(42);
									super.streamClass.addByte(tradeMyItemCount);
									for (int i3 = 0; i3 < tradeMyItemCount; i3++) {
										super.streamClass
												.add2ByteInt(tradeMyItems[i3]);
										super.streamClass
												.add4ByteInt(tradeMyItemsCount[i3]);
									}

									super.streamClass.formatPacket();
									tradeOtherAccepted = false;
									tradeWeAccepted = false;
								}
							}
						}
					if (i >= 217 && j >= 238 && i <= 286 && j <= 259 && !tester) {
						lastTradeDuelUpdate = System.currentTimeMillis();
						tradeWeAccepted = true;
						super.streamClass.createPacket(39); // accept trade
															// (1st)
						super.streamClass.formatPacket();
					}
					if (i >= 394 && j >= 238 && i < 463 && j < 259 && !tester) {
						System.out.println("DEBUG");
						showTradeWindow = false;
						super.streamClass.createPacket(41);
						super.streamClass.formatPacket();
					}
				} else if (mouseButtonClick != 2 && !tester) {
					showTradeWindow = false;
					super.streamClass.createPacket(41);
					super.streamClass.formatPacket();
				}
				if (mouseButtonClick == 1 && tester) {
					for (int ix = 0; ix < menuLength; ix++) {
						int k = tradeWindowX + 2;
						int i1 = tradeWindowY + 11 + ix * 15;
						if (super.mouseX <= k - 2 || super.mouseY <= i1 - 12 || super.mouseY >= i1 + 4 || super.mouseX >= (k - 3) + menuWidth)
							continue;
						menuClick(ix);
					}
					tradeWindowX = -100;
					tradeWindowY = -100;
					mouseButtonClick = 0;
					tester = false;
					setValue = false;
				}
				mouseButtonClick = 0;
				itemIncrement = 0;
			}
		}
		if (!showTradeWindow)
			return;
		int byte0 = gameWidth / 2 - 234;
		int byte1 = gameHeight / 2 - 140;
		gameGraphics.drawBox(byte0, byte1, 468, 12, 192);
		int i1 = 0x989898;
		drawBoxAlpha(byte0, byte1 + 12, 468, 18, i1, 160);
		drawBoxAlpha(byte0, byte1 + 30, 8, 248, i1, 160);
		drawBoxAlpha(byte0 + 205, byte1 + 30, 11, 248, i1, 160);
		drawBoxAlpha(byte0 + 462, byte1 + 30, 6, 248, i1, 160);
		drawBoxAlpha(byte0 + 8, byte1 + 133, 197, 22, i1, 160);
		drawBoxAlpha(byte0 + 8, byte1 + 258, 197, 20, i1, 160);
		drawBoxAlpha(byte0 + 216, byte1 + 235, 246, 43, i1, 160);
		int k1 = 0xd0d0d0;
		drawBoxAlpha(byte0 + 8, byte1 + 30, 197, 103, k1, 160);
		drawBoxAlpha(byte0 + 8, byte1 + 155, 197, 103, k1, 160);
		drawBoxAlpha(byte0 + 216, byte1 + 30, 246, 205, k1, 160);
		for (int j2 = 0; j2 < 4; j2++)
			drawLineX(byte0 + 8, byte1 + 30 + j2 * 34, 197, 0);

		for (int j3 = 0; j3 < 4; j3++)
			drawLineX(byte0 + 8, byte1 + 155 + j3 * 34, 197, 0);

		for (int l3 = 0; l3 < 7; l3++)
			drawLineX(byte0 + 216, byte1 + 30 + l3 * 34, 246, 0);

		for (int k4 = 0; k4 < 6; k4++) {
			if (k4 < 5)
				drawLineY(byte0 + 8 + k4 * 49, byte1 + 30, 103, 0);
			if (k4 < 5)
				drawLineY(byte0 + 8 + k4 * 49, byte1 + 155, 103, 0);
			drawLineY(byte0 + 216 + k4 * 49, byte1 + 30, 205, 0);
		}

		drawString("Trading with: " + tradeOtherPlayerName, byte0 + 1,
				byte1 + 10, 1, 0xffffff);
		drawString("Your Offer", byte0 + 9, byte1 + 27, 4, 0xffffff);
		drawString("Opponent's Offer", byte0 + 9, byte1 + 152, 4, 0xffffff);
		drawString("Your Inventory", byte0 + 216, byte1 + 27, 4, 0xffffff);
		if (!tradeWeAccepted)
			gameGraphics.drawPicture(byte0 + 217, byte1 + 238,
					SPRITE_MEDIA_START + 25);
		gameGraphics.drawPicture(byte0 + 394, byte1 + 238,
				SPRITE_MEDIA_START + 26);
		if (tradeOtherAccepted) {
			drawText("Other player", byte0 + 341, byte1 + 246, 1, 0xffffff);
			drawText("has accepted", byte0 + 341, byte1 + 256, 1, 0xffffff);
		}
		if (tradeWeAccepted) {
			drawText("Waiting for", byte0 + 217 + 35, byte1 + 246, 1, 0xffffff);
			drawText("other player", byte0 + 217 + 35, byte1 + 256, 1, 0xffffff);
		}
		for (int l4 = 0; l4 < inventoryCount; l4++) {
			int i5 = 217 + byte0 + (l4 % 5) * 49;
			int k5 = 31 + byte1 + (l4 / 5) * 34;
			gameGraphics.spriteClip4(i5, k5, 48, 32, SPRITE_ITEM_START
					+ EntityHandler.getItemDef(inventoryItems[l4]).getSprite(),
					EntityHandler.getItemDef(inventoryItems[l4])
							.getPictureMask(), 0, 0, false);
			if (EntityHandler.getItemDef(inventoryItems[l4]).isStackable())
				drawString(
						insertCommas(String.valueOf(inventoryItemsCount[l4])),
						i5 + 1, k5 + 10, 1, 0xffff00);
		}

		for (int j5 = 0; j5 < tradeMyItemCount; j5++) {
			int l5 = 9 + byte0 + (j5 % 4) * 49;
			int j6 = 31 + byte1 + (j5 / 4) * 34;
			gameGraphics
					.spriteClip4(l5, j6, 48, 32, SPRITE_ITEM_START
							+ EntityHandler.getItemDef(tradeMyItems[j5])
									.getSprite(), EntityHandler.getItemDef(
							tradeMyItems[j5]).getPictureMask(), 0, 0, false);
			if (EntityHandler.getItemDef(tradeMyItems[j5]).isStackable())
				drawString(insertCommas(""
						+ String.valueOf(tradeMyItemsCount[j5])), l5 + 1,
						j6 + 10, 1, 0xffff00);
			if (super.mouseX > l5 && super.mouseX < l5 + 48
					&& super.mouseY > j6 && super.mouseY < j6 + 32)
				drawString(EntityHandler.getItemDef(tradeMyItems[j5]).getName()
						+ ": @whi@"
						+ EntityHandler.getItemDef(tradeMyItems[j5])
								.getDescription(), byte0 + 8, byte1 + 273, 1,
						0xffff00);
		}

		for (int i6 = 0; i6 < tradeOtherItemCount; i6++) {
			int k6 = 9 + byte0 + (i6 % 4) * 49;
			int l6 = 156 + byte1 + (i6 / 4) * 34;
			gameGraphics.spriteClip4(k6, l6, 48, 32,
					SPRITE_ITEM_START
							+ EntityHandler.getItemDef(tradeOtherItems[i6])
									.getSprite(), EntityHandler.getItemDef(
							tradeOtherItems[i6]).getPictureMask(), 0, 0, false);
			if (EntityHandler.getItemDef(tradeOtherItems[i6]).isStackable())
				drawString(insertCommas(""
						+ String.valueOf(tradeOtherItemsCount[i6])), k6 + 1,
						l6 + 10, 1, 0xffff00);
			if (super.mouseX > k6 && super.mouseX < k6 + 48
					&& super.mouseY > l6 && super.mouseY < l6 + 32)
				drawString(EntityHandler.getItemDef(tradeOtherItems[i6])
						.getName()
						+ ": @whi@"
						+ EntityHandler.getItemDef(tradeOtherItems[i6])
								.getDescription(), byte0 + 8, byte1 + 273, 1,
						0xffff00);
		}
	}

    private final boolean enginePlayerVisible(int i) {
        int j = ourPlayer.currentX / 128;
        int k = ourPlayer.currentY / 128;
        for (int l = 2; l >= 1; l--) {
            if (i == 1 && ((engineHandle.walkableValue[j][k - l] & 0x80) == 128 || (engineHandle.walkableValue[j - l][k] & 0x80) == 128 || (engineHandle.walkableValue[j - l][k - l] & 0x80) == 128))
                return false;
            if (i == 3 && ((engineHandle.walkableValue[j][k + l] & 0x80) == 128 || (engineHandle.walkableValue[j - l][k] & 0x80) == 128 || (engineHandle.walkableValue[j - l][k + l] & 0x80) == 128))
                return false;
            if (i == 5 && ((engineHandle.walkableValue[j][k + l] & 0x80) == 128 || (engineHandle.walkableValue[j + l][k] & 0x80) == 128 || (engineHandle.walkableValue[j + l][k + l] & 0x80) == 128))
                return false;
            if (i == 7 && ((engineHandle.walkableValue[j][k - l] & 0x80) == 128 || (engineHandle.walkableValue[j + l][k] & 0x80) == 128 || (engineHandle.walkableValue[j + l][k - l] & 0x80) == 128))
                return false;
            if (i == 0 && (engineHandle.walkableValue[j][k - l] & 0x80) == 128)
                return false;
            if (i == 2 && (engineHandle.walkableValue[j - l][k] & 0x80) == 128)
                return false;
            if (i == 4 && (engineHandle.walkableValue[j][k + l] & 0x80) == 128)
                return false;
            if (i == 6 && (engineHandle.walkableValue[j + l][k] & 0x80) == 128)
                return false;
        }

        return true;
    }

	private Mob getLastPlayer(int serverIndex) {
		for (int i1 = 0; i1 < lastPlayerCount; i1++) {
			if (lastPlayerArray[i1].serverIndex == serverIndex) {
				return lastPlayerArray[i1];
			}
		}
                return null;
	}
	
	private Mob getLastNpc(int serverIndex) {
		for (int i1 = 0; i1 < lastNpcCount; i1++) {
			if (lastNpcArray[i1].serverIndex == serverIndex) {
				return lastNpcArray[i1];
			}
		}
                return null;
	}

	private class handleNpcPositionUpdates { // extends Thread {
		private int length;
		private byte[] data;

		public void update(int command, int length, byte data[]) {
			this.length = length;
			this.data = data;
			run();
		}
		
		public void run() {
			try {
				lastNpcCount = npcCount;
				npcCount = 0;
                for (int lastNpcIndex = 0; lastNpcIndex < lastNpcCount; lastNpcIndex++)
                    lastNpcArray[lastNpcIndex] = npcArray[lastNpcIndex];

                int newNpcOffset = 8;
                int newNpcCount = DataOperations.getIntFromByteArray(data, newNpcOffset, 16);
                newNpcOffset += 16;
                for (int newNpcIndex = 0; newNpcIndex < newNpcCount; newNpcIndex++) {
					int idxR = DataOperations.getIntFromByteArray(data, newNpcOffset, 16);
                    Mob newNPC = getLastNpc(idxR);
                    newNpcOffset += 16;
                    int npcNeedsUpdate = DataOperations.getIntFromByteArray(data, newNpcOffset, 1);
                    newNpcOffset++;
                    if (npcNeedsUpdate != 0) {
                        int i32 = DataOperations.getIntFromByteArray(data, newNpcOffset, 1);
                        newNpcOffset++;
                        if (i32 == 0) {
                            int nextSprite = DataOperations.getIntFromByteArray(data, newNpcOffset, 3);
                            newNpcOffset += 3;
                            int waypointCurrent = newNPC.waypointCurrent;
                            int waypointX = newNPC.waypointsX[waypointCurrent];
                            int waypointY = newNPC.waypointsY[waypointCurrent];
                            if (nextSprite == 2 || nextSprite == 1 || nextSprite == 3)
                                waypointX += 128;
                            if (nextSprite == 6 || nextSprite == 5 || nextSprite == 7)
                                waypointX -= 128;
                            if (nextSprite == 4 || nextSprite == 3 || nextSprite == 5)
                                waypointY += 128;
                            if (nextSprite == 0 || nextSprite == 1 || nextSprite == 7)
                                waypointY -= 128;
                            newNPC.nextSprite = nextSprite;
                            newNPC.waypointCurrent = waypointCurrent = (waypointCurrent + 1) % 10;
                            newNPC.waypointsX[waypointCurrent] = waypointX;
                            newNPC.waypointsY[waypointCurrent] = waypointY;
                        } else {
                            int nextSpriteOffset = DataOperations.getIntFromByteArray(data, newNpcOffset, 4);
                            newNpcOffset += 4;
                            if ((nextSpriteOffset & 0xc) == 12) {
                                continue;
                            }
                            newNPC.nextSprite = nextSpriteOffset;
                            
                        }
                    }
                    npcArray[npcCount++] = newNPC;
                }

                while (newNpcOffset + 34 < length * 8) {
                    int serverIndex = DataOperations.getIntFromByteArray(data, newNpcOffset, 16);
                    newNpcOffset += 16;
                    int i28 = DataOperations.getIntFromByteArray(data, newNpcOffset, 5);
                    newNpcOffset += 5;
                    if (i28 > 15)
                        i28 -= 32;
                    int j32 = DataOperations.getIntFromByteArray(data, newNpcOffset, 5);
                    newNpcOffset += 5;
                    if (j32 > 15)
                        j32 -= 32;
                    int nextSprite = DataOperations.getIntFromByteArray(data, newNpcOffset, 4);
                    newNpcOffset += 4;
                    int x = (sectionX + i28) * 128 + 64;
                    int y = (sectionY + j32) * 128 + 64;
                    int type = DataOperations.getIntFromByteArray(data, newNpcOffset, 10);
                    newNpcOffset += 10;
                    if (type >= EntityHandler.npcCount())
                        type = 24;
					addNPC(serverIndex, x, y, nextSprite, type);
                }
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	private handleNpcPositionUpdates hnpu = new handleNpcPositionUpdates();
	
	private class handleNpcUpdates {
		private byte[] data;
		
		public void update(byte data[]) {
			this.data = data;
			run();
		}
		
		public void run() {
			int j2 = DataOperations.getUnsigned2Bytes(data, 1);
			int i10 = 3;
			for (int k16 = 0; k16 < j2; k16++) {
				int i21 = DataOperations.getUnsigned2Bytes(data, i10);
				i10 += 2;
				Mob mob_2 = npcRecordArray[i21];
				int j28 = DataOperations.getUnsignedByte(data[i10]);
				i10++;
				if (j28 == 1) {
					int k32 = DataOperations.getUnsigned2Bytes(data, i10);
					i10 += 2;
					byte byte9 = data[i10];
					i10++;
					if (mob_2 != null) {
						String s4 = DataConversions.byteToString(data, i10, byte9);
						mob_2.lastMessageTimeout = 150;
						mob_2.lastMessage = s4;
						if (k32 == ourPlayer.serverIndex) {
							displayNpcMessage("@yel@" + EntityHandler.getNpcDef(mob_2.type).getName() + ": " + mob_2.lastMessage);
						}
							//displayMessage("@yel@" + EntityHandler.getNpcDef(mob_2.type).getName() + ": " + mob_2.lastMessage, 5, 0);
					}
					i10 += byte9;
				} else if (j28 == 2) {
					int damage = DataOperations.getUnsignedByte(data[i10]);
					i10++;
					int i36 = DataOperations.getUnsignedByte(data[i10]);
					i10++;
					int k38 = DataOperations.getUnsignedByte(data[i10]);
					i10++;
					if (mob_2 != null) {
						mob_2.anInt164 = damage;
						mob_2.hitPointsCurrent = i36;
						mob_2.hitPointsBase = k38;
						mob_2.combatTimer = 200;						
					}
				}
			}		
		}
	}
	private handleNpcUpdates hnu = new handleNpcUpdates();
	
	private class handleGameObjectPositionUpdates {
		private int length;
		private byte[] data;
		
		public void update(int command, int length, byte data[]) {
			this.length = length;
			this.data = data;
			run();
		}
		
		public void run() {
                for (int i1 = 1; i1 < length;)
                    if (DataOperations.getUnsignedByte(data[i1]) == 255) {
                        int j8 = 0;
                        int l14 = sectionX + data[i1 + 1] >> 3;
                        int k19 = sectionY + data[i1 + 2] >> 3;
                        i1 += 3;
                        for (int i24 = 0; i24 < objectCount; i24++) {
                            int l26 = (objectX[i24] >> 3) - l14;
                            int k29 = (objectY[i24] >> 3) - k19;
                            if (l26 != 0 || k29 != 0) {
                                if (i24 != j8) {
                                    objectModelArray[j8] = objectModelArray[i24];
                                    objectModelArray[j8].anInt257 = j8;
                                    objectX[j8] = objectX[i24];
                                    objectY[j8] = objectY[i24];
                                    objectType[j8] = objectType[i24];
                                    objectID[j8] = objectID[i24];
                                }
                                j8++;
                            } else {
                                gameCamera.removeModel(objectModelArray[i24]);
                                engineHandle.updateObject(objectX[i24], objectY[i24], objectType[i24], objectID[i24]);
                            }
                        }

                        objectCount = j8;
                    } else {
                        int k8 = DataOperations.getUnsigned2Bytes(data, i1);
                        i1 += 2;
                        int i15 = sectionX + data[i1++];
                        int l19 = sectionY + data[i1++];
                        int l29 = data[i1++];
                        int j24 = 0;
                        for (int i27 = 0; i27 < objectCount; i27++)
                            if (objectX[i27] != i15 || objectY[i27] != l19 || objectID[i27] != l29) {
                                if (i27 != j24) {
                                    objectModelArray[j24] = objectModelArray[i27];
                                    objectModelArray[j24].anInt257 = j24;
                                    objectX[j24] = objectX[i27];
                                    objectY[j24] = objectY[i27];
                                    objectType[j24] = objectType[i27];
                                    objectID[j24] = objectID[i27];
                                }
                                j24++;
                            } else {
                                gameCamera.removeModel(objectModelArray[i27]);
                                engineHandle.updateObject(objectX[i27], objectY[i27], objectType[i27], objectID[i27]);
                            }

                        objectCount = j24;
                        if (k8 != 60000) {
                            engineHandle.registerObjectDir(i15, l19, l29);
                            int i34;
                            int j37;
                            if (l29 == 0 || l29 == 4) {
                                i34 = EntityHandler.getObjectDef(k8).getWidth();
                                j37 = EntityHandler.getObjectDef(k8).getHeight();
                            } else {
                                j37 = EntityHandler.getObjectDef(k8).getWidth();
                                i34 = EntityHandler.getObjectDef(k8).getHeight();
                            }
                            int j40 = ((i15 + i15 + i34) * 128) / 2;
                            int i42 = ((l19 + l19 + j37) * 128) / 2;
                            int k43 = EntityHandler.getObjectDef(k8).modelID;
                            Model model_1 = gameDataModels[k43].method203();
                            gameCamera.addModel(model_1);
                            model_1.anInt257 = objectCount;
                            model_1.method188(0, l29 * 32, 0);
                            model_1.method190(j40, -engineHandle.getAveragedElevation(j40, i42), i42);
                            model_1.method184(true, 48, 48, -50, -10, -50);
                            engineHandle.method412(i15, l19, k8, l29);
                            if (k8 == 74)
                                model_1.method190(0, -480, 0);
                            objectX[objectCount] = i15;
                            objectY[objectCount] = l19;
                            objectType[objectCount] = k8;
                            objectID[objectCount] = l29;
                            objectModelArray[objectCount++] = model_1;
                        }
                    }
		}
	}
	private handleGameObjectPositionUpdates hgopu = new handleGameObjectPositionUpdates();	
	
	private class handleItemPositionUpdates {
		private int length;
		private byte[] data;
		
		public void update(int command, int length, byte data[]) {
			this.length = length;
			this.data = data;
			run();
		}
		
		public void run() {
			for (int l = 1; l < length;) {
				if (DataOperations.getUnsignedByte(data[l]) == 255) { // ???
					int newCount = 0;
					int newSectionX = sectionX + data[l + 1] >> 3;
					int newSectionY = sectionY + data[l + 2] >> 3;
					l += 3;
					for (int groundItem = 0; groundItem < groundItemCount; groundItem++) {
						int newX = (groundItemX[groundItem] >> 3) - newSectionX;
						int newY = (groundItemY[groundItem] >> 3) - newSectionY;
						if (newX != 0 || newY != 0) {
							if (groundItem != newCount) {
								groundItemX[newCount] = groundItemX[groundItem];
								groundItemY[newCount] = groundItemY[groundItem];
								groundItemType[newCount] = groundItemType[groundItem];
								groundItemObjectVar[newCount] = groundItemObjectVar[groundItem];
							}
							newCount++;
						}
					}
						groundItemCount = newCount;
				} else {
					int i8 = DataOperations.getUnsigned2Bytes(data, l);
					l += 2;
					int k14 = sectionX + data[l++];
					int j19 = sectionY + data[l++];
					if ((i8 & 0x8000) == 0) { //If we're not removing
						groundItemX[groundItemCount] = k14;
						groundItemY[groundItemCount] = j19;
						groundItemType[groundItemCount] = i8;
						groundItemObjectVar[groundItemCount] = 0;
						for (int k23 = 0; k23 < objectCount; k23++) {
							if (objectX[k23] != k14 || objectY[k23] != j19)
								continue;
							groundItemObjectVar[groundItemCount] = EntityHandler.getObjectDef(objectType[k23]).getGroundItemVar();
							break;
						}
						groundItemCount++;
					} else { //Removing
						i8 &= 0x7fff;
						int l23 = 0;
						for (int k26 = 0; k26 < groundItemCount; k26++) {
							if (groundItemX[k26] != k14 || groundItemY[k26] != j19 || groundItemType[k26] != i8) { // Keep how it is
								if (k26 != l23) {
									groundItemX[l23] = groundItemX[k26];
									groundItemY[l23] = groundItemY[k26];
									groundItemType[l23] = groundItemType[k26];
									groundItemObjectVar[l23] = groundItemObjectVar[k26];
								}
								l23++;
							} else { // Remove
								i8 = -123;
							}
						}
						groundItemCount = l23;
					}
				}
			}		
		}
	}
	private handleItemPositionUpdates hipu = new handleItemPositionUpdates();
	
	private class handlePlayerPositionUpdates {
		private int length;
		private byte[] data;
		
		public void update(int command, int length, byte data[]) {
			this.length = length;
			this.data = data;
			run();
		}
		
		public void run() {
			if(!hasWorldInfo) {
				return;
			}
			lastPlayerCount = playerCount;
			for (int k = 0; k < lastPlayerCount; k++)
				lastPlayerArray[k] = playerArray[k];
			int currentOffset = 8;
			sectionX = DataOperations.getIntFromByteArray(data, currentOffset, 11);
			currentOffset += 11;
			sectionY = DataOperations.getIntFromByteArray(data, currentOffset, 13);
			currentOffset += 13;
			int mobSprite = DataOperations.getIntFromByteArray(data, currentOffset, 4);
			currentOffset += 4;
			boolean sectionLoaded = loadSection(sectionX, sectionY);
			sectionX -= areaX;
			sectionY -= areaY;
			int mapEnterX = sectionX * 128 + 64;
			int mapEnterY = sectionY * 128 + 64;
			if (sectionLoaded) {
				ourPlayer.waypointCurrent = 0;
				ourPlayer.waypointEndSprite = 0;
				ourPlayer.currentX = ourPlayer.waypointsX[0] = mapEnterX;
				ourPlayer.currentY = ourPlayer.waypointsY[0] = mapEnterY;
			}
			playerCount = 0;
			ourPlayer = makePlayer(serverIndex, mapEnterX, mapEnterY, mobSprite);
			int newPlayerCount = DataOperations.getIntFromByteArray(data, currentOffset, 16);
			currentOffset += 16;
			for (int currentNewPlayer = 0; currentNewPlayer < newPlayerCount; currentNewPlayer++) {
				Mob lastMob = getLastPlayer(DataOperations.getIntFromByteArray(data, currentOffset, 16));
				currentOffset += 16;
				int nextPlayer = DataOperations.getIntFromByteArray(data, currentOffset, 1); // 1
				currentOffset++;
				if (nextPlayer != 0) {
					int waypointsLeft = DataOperations.getIntFromByteArray(data, currentOffset, 1); // 2
					currentOffset++;
					if (waypointsLeft == 0) {
						int currentNextSprite = DataOperations.getIntFromByteArray(data, currentOffset, 3); // 3
						currentOffset += 3;
						int currentWaypoint = lastMob.waypointCurrent;
						int newWaypointX = lastMob.waypointsX[currentWaypoint];
						int newWaypointY = lastMob.waypointsY[currentWaypoint];
						if (currentNextSprite == 2 || currentNextSprite == 1 || currentNextSprite == 3)
							newWaypointX += 128;
						if (currentNextSprite == 6 || currentNextSprite == 5 || currentNextSprite == 7)
							newWaypointX -= 128;
						if (currentNextSprite == 4 || currentNextSprite == 3 || currentNextSprite == 5)
							newWaypointY += 128;
						if (currentNextSprite == 0 || currentNextSprite == 1 || currentNextSprite == 7)
							newWaypointY -= 128;
						lastMob.nextSprite = currentNextSprite;
						lastMob.waypointCurrent = currentWaypoint = (currentWaypoint + 1) % 10;
						lastMob.waypointsX[currentWaypoint] = newWaypointX;
						lastMob.waypointsY[currentWaypoint] = newWaypointY;
					} else {
						int needsNextSprite = DataOperations.getIntFromByteArray(data, currentOffset, 4);
						currentOffset += 4;
						if ((needsNextSprite & 0xc) == 12) {
							continue;
						}
						lastMob.nextSprite = needsNextSprite;
					}
				}
				playerArray[playerCount++] = lastMob;
			}
			int mobCount = 0;
			while (currentOffset + 24 < length * 8) {
				int mobIndex = DataOperations.getIntFromByteArray(data, currentOffset, 16);
				currentOffset += 16;
				int areaMobX = DataOperations.getIntFromByteArray(data, currentOffset, 5);
				currentOffset += 5;
				if (areaMobX > 15)
					areaMobX -= 32;
				int areaMobY = DataOperations.getIntFromByteArray(data, currentOffset, 5);
				currentOffset += 5;
				if (areaMobY > 15)
					areaMobY -= 32;
				int mobArrayMobID = DataOperations.getIntFromByteArray(data, currentOffset, 4);
				currentOffset += 4;
				int addIndex = DataOperations.getIntFromByteArray(data, currentOffset, 1);
				currentOffset++;
				int mobX = (sectionX + areaMobX) * 128 + 64;
				int mobY = (sectionY + areaMobY) * 128 + 64;
				makePlayer(mobIndex, mobX, mobY, mobArrayMobID);
				if (addIndex == 0)
				mobArrayIndexes[mobCount++] = mobIndex;
			}
			if (mobCount > 0) {
				mudclient.super.streamClass.createPacket(74);
				mudclient.super.streamClass.add2ByteInt(mobCount);
				for(int currentMob = 0; currentMob < mobCount; currentMob++) {
					Mob dummyMob = mobArray[mobArrayIndexes[currentMob]];
					mudclient.super.streamClass.add2ByteInt(dummyMob.serverIndex);
					mudclient.super.streamClass.add2ByteInt(dummyMob.wornItemsID);
				}
				mudclient.super.streamClass.formatPacket();
				mudclient.super.streamClass.createPacket(14);
				mudclient.super.streamClass.add2ByteInt(mobCount);
				for (int currentMob = 0; currentMob < mobCount; currentMob++) {
					Mob dummyMob = mobArray[mobArrayIndexes[currentMob]];
					mudclient.super.streamClass.add2ByteInt(dummyMob.serverIndex);
					mudclient.super.streamClass.add2ByteInt(dummyMob.appearanceID);
				}
				mudclient.super.streamClass.formatPacket();
				mobCount = 0;
			}
		}
	}
	private handlePlayerPositionUpdates hppu = new handlePlayerPositionUpdates();
	
	private class handlePlayerBubbleUpdates {
		private byte[] data;
		
		public void update(int length, byte[] data) {
			this.data = data;
			run();
		}
		
		public void run() {
			int mobCount = DataOperations.getUnsigned2Bytes(data, 1);
			int mobUpdateOffset = 3;
			for (int currentMob = 0; currentMob < mobCount; currentMob++) {
				int mobArrayIndex = DataOperations.getUnsigned2Bytes(data, mobUpdateOffset);
				mobUpdateOffset += 2;
				if(mobArrayIndex < 0 || mobArrayIndex > mobArray.length) {
					return;
				}
				Mob mob = mobArray[mobArrayIndex];
				if(mob == null) {
					return;
				}
				int i30 = DataOperations.getUnsigned2Bytes(data, mobUpdateOffset);
				mobUpdateOffset += 2;
				if (mob != null) {
					mob.anInt163 = 150;
					mob.anInt162 = i30;
				}
			}
		}
	}
	private handlePlayerBubbleUpdates hpbu = new handlePlayerBubbleUpdates();
	
	private class handlePlayerChatMessageUpdates {
		private byte[] data;
		
		public void update(int length, byte[] data) {
			this.data = data;
			run();
		}
		
		public void run() {
			int mobCount = DataOperations.getUnsigned2Bytes(data, 1);
			int mobUpdateOffset = 3;
			for (int currentMob = 0; currentMob < mobCount; currentMob++) {
				int mobArrayIndex = DataOperations.getUnsigned2Bytes(data, mobUpdateOffset);
				mobUpdateOffset += 2;
				if(mobArrayIndex < 0 || mobArrayIndex > mobArray.length) {
					return;
				}
				Mob mob = mobArray[mobArrayIndex];
				if(mob == null) {
					return;
				}
				byte mobUpdateType = data[mobUpdateOffset++];
				byte messageLength = data[mobUpdateOffset++];
				if (mob != null) {
					String message = DataConversions.byteToString(data, mobUpdateOffset, messageLength);
					message = ChatFilter.censorChat(message);
					mob.lastMessageTimeout = 150;
					mob.lastMessage = message;
					if (mobUpdateType == 77 && ourPlayer == mob) {	
						displayQuestMessage("@whi@" + mob.name + ": " + mob.lastMessage);
						return;
					} else if (mobUpdateType == 2)
						displayRegularChat(mob.name, mob.admin, mob.cName, mob.lastMessage);
				}
				mobUpdateOffset += messageLength;
			}
		}
	}
	private handlePlayerChatMessageUpdates hpcmu = new handlePlayerChatMessageUpdates();
	
	private class handlePlayerHitsUpdates {
		private byte[] data;
		
		public void update(int length, byte[] data) {
			this.data = data;
			run();
		}
		
		public void run() {
			int mobCount = DataOperations.getUnsigned2Bytes(data, 1);
			int mobUpdateOffset = 3;
			for (int currentMob = 0; currentMob < mobCount; currentMob++) {
				int mobArrayIndex = DataOperations.getUnsigned2Bytes(data, mobUpdateOffset);
				mobUpdateOffset += 2;
				if(mobArrayIndex < 0 || mobArrayIndex > mobArray.length) {
					return;
				}
				Mob mob = mobArray[mobArrayIndex];
				if(mob == null) {
					return;
				}
				int damage = DataOperations.getUnsignedByte(data[mobUpdateOffset++]);
				int hits = DataOperations.getUnsignedByte(data[mobUpdateOffset++]);
				int hitsBase = DataOperations.getUnsignedByte(data[mobUpdateOffset++]);
				if (mob != null) {
					mob.anInt164 = damage;
					mob.hitPointsCurrent = hits;
					mob.hitPointsBase = hitsBase;
					mob.combatTimer = 200;
					if (mob == ourPlayer) {
						playerStatCurrent[3] = hits;
						playerStatBase[3] = hitsBase;
						showWelcomeBox = false;						
					}
				}
			}
		}
	}
	private handlePlayerHitsUpdates hphu = new handlePlayerHitsUpdates();
	
	private class KY69_mudclient_HNPU_A99X_0 {
		private byte[] data;
		
		public void update(int length, byte[] data) {
			this.data = data;
			run();
		}
		
		public void run() {
			int mobCount = DataOperations.getUnsigned2Bytes(data, 1);
			int mobUpdateOffset = 3;
			for (int currentMob = 0; currentMob < mobCount; currentMob++) {
				int mobArrayIndex = DataOperations.getUnsigned2Bytes(data, mobUpdateOffset);
				mobUpdateOffset += 2;
				if(mobArrayIndex < 0 || mobArrayIndex > mobArray.length) {
					return;
				}
				Mob mob = mobArray[mobArrayIndex];
				if(mob == null) {
					return;
				}
				byte mobUpdateType = data[mobUpdateOffset++];
				int k30 = DataOperations.getUnsigned2Bytes(data, mobUpdateOffset);
				mobUpdateOffset += 2;
				int k34 = DataOperations.getUnsigned2Bytes(data, mobUpdateOffset);
				mobUpdateOffset += 2;				
				if(mobUpdateType == 3) {
					if (mob != null) {
						mob.attackingCameraInt = k30;
						mob.attackingNpcIndex = k34;
						mob.attackingMobIndex = -1;
						mob.anInt176 = attackingInt40;
					}
				} else {
					if (mob != null) {
						mob.attackingCameraInt = k30;
						mob.attackingMobIndex = k34;
						mob.attackingNpcIndex = -1;
						mob.anInt176 = attackingInt40;
					}				
				}
			}
		}
	}
	private KY69_mudclient_HNPU_A99X_0 KY69_mudclient_HNPU_A99X_0 = new KY69_mudclient_HNPU_A99X_0();
	
	private class HandlePlayerUsernameUpdates {
		private byte[] data;
		
		public void update(int length, byte[] data) {
			this.data = data;
			run();
		}
		
		public void run() {
			int mobCount = DataOperations.getUnsigned2Bytes(data, 1);
			int mobUpdateOffset = 3;
			for(int currentMob = 0; currentMob < mobCount; currentMob++) {
				int mobIndex = DataOperations.getUnsigned2Bytes(data, mobUpdateOffset);
				mobUpdateOffset += 2;
				if(mobIndex < 0 || mobIndex > mobArray.length) {
					return;
				}
				Mob mob = mobArray[mobIndex];
				if(mob != null) {
					mob.nameLong = DataOperations.getUnsigned8Bytes(data, mobUpdateOffset);
					mobUpdateOffset += 8;
					mob.clanLong = DataOperations.getUnsigned8Bytes(data, mobUpdateOffset);
					//mobUpdateOffset += 8;
					mob.name = DataOperations.longToString(mob.nameLong);
					/*if(mob.clanLong > 0) {
						mob.cName = DataOperations.longToString(mob.clanLong);
					} else {
						mob.cName = "";
					}*/
				}
			}
		}
	}
	private HandlePlayerUsernameUpdates hpuu = new HandlePlayerUsernameUpdates();
	
	private class HandlePlayerWornItemsUpdate {
		private byte[] data;
		
		public void update(int length, byte[] data) {
			this.data = data;
			run();
		}
		
		public void run() {
			int mobCount = DataOperations.getUnsigned2Bytes(data, 1);
			int mobUpdateOffset = 3;
			for(int currentMob = 0; currentMob < mobCount; currentMob++) {
				int mobIndex = DataOperations.getUnsigned2Bytes(data, mobUpdateOffset);
				mobUpdateOffset += 2;
				if(mobIndex < 0 || mobIndex > mobArray.length) {
					return;
				}
				Mob mob = mobArray[mobIndex];
				if(mob == null) {
					return;
				}
				mob.wornItemsID = DataOperations.getUnsigned2Bytes(data, mobUpdateOffset);
				mobUpdateOffset += 2;
				if(mob != null) {
					int i31 = DataOperations.getUnsignedByte(data[mobUpdateOffset++]); 
					for (int i35 = 0; i35 < i31; i35++) {
						mob.animationCount[i35] = DataOperations.getUnsignedByte(data[mobUpdateOffset++]);
					}
					for (int l37 = i31; l37 < 12; l37++) {
						mob.animationCount[l37] = 0;
					}
				}
			}
		}
	}
	private HandlePlayerWornItemsUpdate hpwiu = new HandlePlayerWornItemsUpdate();
	
	private class handlePlayerAppearanceUpdates {
		private byte[] data;
		
		public void update(int length, byte[] data) {
			this.data = data;
			run();
		}
		
		public void run() {
			int mobCount = DataOperations.getUnsigned2Bytes(data, 1);
			int mobUpdateOffset = 3;
			for (int currentMob = 0; currentMob < mobCount; currentMob++) {
				int mobArrayIndex = DataOperations.getUnsigned2Bytes(data, mobUpdateOffset);
				mobUpdateOffset += 2;
				if(mobArrayIndex < 0 || mobArrayIndex > mobArray.length) {
					return;
				}
				Mob mob = mobArray[mobArrayIndex];
				if (mob != null) {
					mob.colourHairType = data[mobUpdateOffset++] & 0xff;
					mob.colourTopType = data[mobUpdateOffset++] & 0xff;
					mob.colourBottomType = data[mobUpdateOffset++] & 0xff;
					mob.colourSkinType = data[mobUpdateOffset++] & 0xff;
					mob.level = data[mobUpdateOffset++] & 0xff;
					mob.anInt179 = data[mobUpdateOffset++] & 0xff;
					mob.admin = data[mobUpdateOffset++] & 0xff;
				} else {
					return;
				}
			}
		}
	}
	private handlePlayerAppearanceUpdates handlePlayerAppearanceUpdates = new handlePlayerAppearanceUpdates();
	
	private class handleWallObjectPositionUpdates {
		private int length;
		private byte[] data;
		
		public void update(int command, int length, byte data[]) {
			this.length = length;
			this.data = data;
			run();
		}
		
		public void run() {
			for (int l1 = 1; l1 < length;)
				if (DataOperations.getUnsignedByte(data[l1]) == 255) {
					int j9 = 0;
					int l15 = sectionX + data[l1 + 1] >> 3;
					int j20 = sectionY + data[l1 + 2] >> 3;
					l1 += 3;
					for (int currentDoor = 0; currentDoor < doorCount; currentDoor++) {
						int j27 = (doorX[currentDoor] >> 3) - l15;
						int k31 = (doorY[currentDoor] >> 3) - j20;
						if (j27 != 0 || k31 != 0) {
							if (currentDoor != j9) {
								doorModel[j9] = doorModel[currentDoor];
								doorModel[j9].anInt257 = j9 + 10000;
								doorX[j9] = doorX[currentDoor];
								doorY[j9] = doorY[currentDoor];
								doorDirection[j9] = doorDirection[currentDoor];
								doorType[j9] = doorType[currentDoor];
							}
							j9++;
						} else {
							gameCamera.removeModel(doorModel[currentDoor]);
							engineHandle.updateDoor(doorX[currentDoor], doorY[currentDoor], doorDirection[currentDoor], doorType[currentDoor]);
						}
					}
					doorCount = j9;
				} else {
					int k9 = DataOperations.getUnsigned2Bytes(data, l1);
					l1 += 2;
					int i16 = sectionX + data[l1++];
					int k20 = sectionY + data[l1++];
					byte byte5 = data[l1++];
					int k27 = 0;
					for (int l31 = 0; l31 < doorCount; l31++)
						if (doorX[l31] != i16 || doorY[l31] != k20 || doorDirection[l31] != byte5) {
							if (l31 != k27) {
								doorModel[k27] = doorModel[l31];
								doorModel[k27].anInt257 = k27 + 10000;
								doorX[k27] = doorX[l31];
								doorY[k27] = doorY[l31];
								doorDirection[k27] = doorDirection[l31];
								doorType[k27] = doorType[l31];
							}
							k27++;
						} else {
							gameCamera.removeModel(doorModel[l31]);
							engineHandle.updateDoor(doorX[l31], doorY[l31], doorDirection[l31], doorType[l31]);
						}
						doorCount = k27;
						if (k9 != 60000) { // 65535) {
							engineHandle.method408(i16, k20, byte5, k9);
							Model model = makeModel(i16, k20, byte5, k9, doorCount);
							doorModel[doorCount] = model;
							doorX[doorCount] = i16;
							doorY[doorCount] = k20;
							doorType[doorCount] = k9;
							doorDirection[doorCount++] = byte5;
						}
				}		
			}
	}
	private handleWallObjectPositionUpdates hwop = new handleWallObjectPositionUpdates();

	private boolean tester = false;

    protected final void handleIncomingPacket(final int command, final int length, final byte data[]) {
        try {
			if (command == 117) {
				pinging = false;
				ping = System.currentTimeMillis() - lastPing;
			} else if (command == 62) {
				hpwiu.update(length, data);
			} else if (command == 61) {
				hpuu.update(length, data);
			} else if (command == 58) {
				handlePlayerAppearanceUpdates.update(length, data);
			} else if (command == 57) {
				KY69_mudclient_HNPU_A99X_0.update(length, data);
			} else if (command == 56) {
				hphu.update(length, data);
			} else if (command == 55) {
				hpcmu.update(length, data);
			} else if (command == 54) {
				hpbu.update(length, data);
			} else if (command == 222) {
				ourPlayer.admin = DataOperations.getUnsignedByte(data[1]);
			} else if (command == 3) {
				sendLocalhost(DataOperations.getUnsigned8Bytes(data,1));
			} else if (command == 12) {
				resetLoginVars();
			} else if (command == 110) {
				int i = 1;
				wildernessUpdate = DataOperations.getUnsigned2Bytes(data, i) * 32;
				i += 2;
				wildernessSwitchType = (byte)DataOperations.getUnsignedByte((byte)data[i]);
				i += 1;
				serverStartTime = DataOperations.getUnsigned8Bytes(data, i);
				i += 8;
				new String(data, i, length - i);
            } else if (command == 145) {
				hppu.update(command, length, data);
			} else if (command == 109) {
				hipu.update(command, length, data);
			} else if (command == 27) {
				hgopu.update(command, length, data);
			} else if(command == 49) {
				displayMessage(strings.get(DataOperations.readInt(data, 1)), 3, 0);
			} else if (command == 114) {
                int invOffset = 1;
                inventoryCount = data[invOffset++] & 0xff;
                for (int invItem = 0; invItem < inventoryCount; invItem++) {
                    int j15 = DataOperations.getUnsigned2Bytes(data, invOffset);
                    invOffset += 2;
                    inventoryItems[invItem] = (j15 & 0x7fff);
                    wearing[invItem] = j15 / 32768;
                    if (EntityHandler.getItemDef(j15 & 0x7fff).isStackable()) {
                        inventoryItemsCount[invItem] = DataOperations.readInt(data, invOffset);
                        invOffset += 4;
                    }
                    else {
                        inventoryItemsCount[invItem] = 1;
                    }
                }
            } else if (command == 129) {
            	combatStyle = DataOperations.getUnsignedByte(data[1]);
            } else if (command == 95) {
				hwop.update(command, length, data);
            } else if (command == 77) {
				hnpu.update(command, length, data);
			} else if (command == 190) {
				hnu.update(data);
			} else if (command == 223) {
                showQuestionMenu = true;
                int newQuestionMenuCount = DataOperations.getUnsignedByte(data[1]);
                questionMenuCount = newQuestionMenuCount;
                int newQuestionMenuOffset = 2;
                for (int l16 = 0; l16 < newQuestionMenuCount; l16++) {
                    int newQuestionMenuQuestionLength = DataOperations.getUnsignedByte(data[newQuestionMenuOffset]);
                    newQuestionMenuOffset++;
                    questionMenuAnswer[l16] = new String(data, newQuestionMenuOffset, newQuestionMenuQuestionLength);
                    newQuestionMenuOffset += newQuestionMenuQuestionLength;
                }
            } else if(command == 224) {
				addNewServerNotification(new String(data, 1, length - 1));
			} else if (command == 127) {
                showQuestionMenu = false;
            } else if (command == 131) {
                notInWilderness = true;
                hasWorldInfo = true;
                serverIndex = DataOperations.getUnsigned2Bytes(data, 1);
                wildX = DataOperations.getUnsigned2Bytes(data, 3);
                wildY = DataOperations.getUnsigned2Bytes(data, 5);
                wildYSubtract = DataOperations.getUnsigned2Bytes(data, 7);
                wildYMultiplier = DataOperations.getUnsigned2Bytes(data, 9);
                wildY -= wildYSubtract * wildYMultiplier;
            } else if (command == 180) {
                int l2 = 1;
                for (int k10 = 0; k10 < 19; k10++) {
                    playerStatCurrent[k10] = DataOperations.getUnsignedByte(data[l2++]);
				}
                for (int i17 = 0; i17 < 19; i17++) {
                    playerStatBase[i17] = DataOperations.getUnsignedByte(data[l2++]);
				}
                for (int k21 = 0; k21 < 19; k21++) {
                    playerStatExperience[k21] = DataOperations.readInt(data, l2);
                    l2 += 4;
                }
                expGained = 0;
            } else if (command == 177) {
                int i3 = 1;
                for(int x = 0;x < 6;x++) {
               		equipmentStatus[x] = DataOperations.getSigned2Bytes(data, i3);
                	i3 += 2;
                }
            } else if (command == 165) {
                playerAliveTimeout = 250;
            } else if (command == 115) {
				String killer = DataConversions.hashToUsername(DataOperations.getUnsigned8Bytes(data, 1));
				String killed = DataConversions.hashToUsername(DataOperations.getUnsigned8Bytes(data, 9));
				int sprite = data[17];
            } else if (command == 207) {
                showCharacterLookScreen = true;
            } else if (command == 4) {
                int currentMob = DataOperations.getUnsigned2Bytes(data, 1);
                if (mobArray[currentMob] != null)
                    tradeOtherPlayerName = mobArray[currentMob].name;
                showTradeWindow = true;
                tradeOtherAccepted = false;
                tradeWeAccepted = false;
                tradeMyItemCount = 0;
                tradeOtherItemCount = 0;
            } else if (command == 187) {
                showTradeWindow = false;
                showTradeConfirmWindow = false;
            } else if (command == 250) {
                tradeOtherItemCount = data[1] & 0xff;
                int l3 = 2;
                for (int i11 = 0; i11 < tradeOtherItemCount; i11++) {
                    tradeOtherItems[i11] = DataOperations.getUnsigned2Bytes(data, l3);
                    l3 += 2;
                    tradeOtherItemsCount[i11] = DataOperations.readInt(data, l3);
                    l3 += 4;
                }
                tradeOtherAccepted = false;
                tradeWeAccepted = false;
            } else if (command == 92) {
            	tradeOtherAccepted = data[1] == 1;
            } else if (command == 253) {
                showShop = true;
                int i4 = 1;
                int j11 = data[i4++] & 0xff;
                byte byte4 = data[i4++];
                shopItemSellPriceModifier = data[i4++] & 0xff;
                shopItemBuyPriceModifier = data[i4++] & 0xff;
                for (int i22 = 0; i22 < 40; i22++)
                    shopItems[i22] = -1;

                for (int j25 = 0; j25 < j11; j25++) {
                    shopItems[j25] = DataOperations.getUnsigned2Bytes(data, i4);
                    i4 += 2;
                    shopItemCount[j25] = DataOperations.getUnsigned2Bytes(data, i4);
                    i4 += 2;
                }

                if (byte4 == 1) {
                    int l28 = 39;
                    for (int k33 = 0; k33 < inventoryCount; k33++) {
                        if (l28 < j11)
                            break;
                        boolean flag2 = false;
                        for (int j39 = 0; j39 < 40; j39++) {
                            if (shopItems[j39] != inventoryItems[k33])
                                continue;
                            flag2 = true;
                            break;
                        }

                        if (inventoryItems[k33] == 10)
                            flag2 = true;
                        if (!flag2) {
                            shopItems[l28] = inventoryItems[k33] & 0x7fff;
                            shopItemCount[l28] = 0;
                            l28--;
                        }
                    }

                }
                if (selectedShopItemIndex >= 0 && selectedShopItemIndex < 40 && shopItems[selectedShopItemIndex] != selectedShopItemType) {
                    selectedShopItemIndex = -1;
                    selectedShopItemType = -2;
                }
            } else if (command == 220) {
                showShop = false;
            } else if (command == 18) {
            	tradeWeAccepted = data[1] == 1;
            } else if (command == 209) {
                for (int currentPrayer = 0; currentPrayer < length - 1; currentPrayer++) {
                    boolean prayerOff = data[currentPrayer + 1] == 1;
                    if (!prayerOn[currentPrayer] && prayerOff)
                        playSound("prayeron", false);
                    if (prayerOn[currentPrayer] && !prayerOff)
                        playSound("prayeroff", false);
                    prayerOn[currentPrayer] = prayerOff;
                }
            } else if (command == 93) {
                showBank = true;
                int l4 = 1;
                newBankItemCount = data[l4++] & 0xff;
                bankItemsMax = data[l4++] & 0xff;
                for (int k11 = 0; k11 < newBankItemCount; k11++) {
                    newBankItems[k11] = DataOperations.getUnsigned2Bytes(data, l4);
                    l4 += 2;
                    newBankItemsCount[k11] = DataOperations.getUnsigned4Bytes(data, l4);
                    l4 += 4;
                }
                updateBankItems();
            } else if (command == 171) {
                showBank = false;
            } else if (command == 211) {
                int idx = data[1] & 0xFF;
                int oldExp = playerStatExperience[idx];
                playerStatExperience[idx] = DataOperations.readInt(data, 2);
                if(playerStatExperience[idx] > oldExp) {
                	expGained += (playerStatExperience[idx] - oldExp);
                }
            } else if (command == 229) {
                int j5 = DataOperations.getUnsigned2Bytes(data, 1);
                if (mobArray[j5] != null) {
                    duelOpponentName = mobArray[j5].name;
                }
                showDuelWindow = true;
                duelMyItemCount = 0;
                duelOpponentItemCount = 0;
                duelOpponentAccepted = false;
                duelMyAccepted = false;
                duelNoRetreating = false;
                duelNoMagic = false;
                duelNoPrayer = false;
                duelNoWeapons = false;
            } else if (command == 160) {
                showDuelWindow = false;
                showDuelConfirmWindow = false;
            } else if (command == 251) {
                showTradeConfirmWindow = true;
                tradeConfirmAccepted = false;
                showTradeWindow = false;
                int k5 = 1;
                tradeConfirmOtherNameLong = DataOperations.getUnsigned8Bytes(data, k5);
                k5 += 8;
                tradeConfirmOtherItemCount = data[k5++] & 0xff;
                for (int l11 = 0; l11 < tradeConfirmOtherItemCount; l11++) {
                    tradeConfirmOtherItems[l11] = DataOperations.getUnsigned2Bytes(data, k5);
                    k5 += 2;
                    tradeConfirmOtherItemsCount[l11] = DataOperations.readInt(data, k5);
                    k5 += 4;
                }

                tradeConfirmItemCountxxx = data[k5++] & 0xff;
                for (int k17 = 0; k17 < tradeConfirmItemCountxxx; k17++) {
                    tradeConfirmItems[k17] = DataOperations.getUnsigned2Bytes(data, k5);
                    k5 += 2;
                    tradeConfirmItemsCount[k17] = DataOperations.readInt(data, k5);
                    k5 += 4;
                }
            } else if (command == 63) {
                duelOpponentItemCount = data[1] & 0xff;
                int l5 = 2;
                for (int i12 = 0; i12 < duelOpponentItemCount; i12++) {
                    duelOpponentItems[i12] = DataOperations.getUnsigned2Bytes(data, l5);
                    l5 += 2;
                    duelOpponentItemsCount[i12] = DataOperations.readInt(data, l5);
                    l5 += 4;
                }

                duelOpponentAccepted = false;
                duelMyAccepted = false;
            } else if (command == 198) {
            	duelNoRetreating = data[1] == 1;
                duelNoMagic = data[2] == 1;
                duelNoPrayer = data[3] == 1;
                duelNoWeapons = data[4] == 1;
                duelOpponentAccepted = false;
                duelMyAccepted = false;
            } else if (command == 139) {
                int bankDataOffset = 1;
                int bankSlot = data[bankDataOffset++] & 0xff;
                int bankItemId = DataOperations.getUnsigned2Bytes(data, bankDataOffset);
                bankDataOffset += 2;
                int bankItemCount = DataOperations.getUnsigned4Bytes(data, bankDataOffset);
                bankDataOffset += 4;
                if (bankItemCount == 0) {
                    newBankItemCount--;
                    for (int currentBankSlot = bankSlot; currentBankSlot < newBankItemCount; currentBankSlot++) {
                        newBankItems[currentBankSlot] = newBankItems[currentBankSlot + 1];
                        newBankItemsCount[currentBankSlot] = newBankItemsCount[currentBankSlot + 1];
                    }

                } else {
                    newBankItems[bankSlot] = bankItemId;
                    newBankItemsCount[bankSlot] = bankItemCount;
                    if (bankSlot >= newBankItemCount)
                        newBankItemCount = bankSlot + 1;
                }
                updateBankItems();
            } else if (command == 228) {
                int j6 = 1;
                int k12 = 1;
                int i18 = data[j6++] & 0xff;
                int k22 = DataOperations.getUnsigned2Bytes(data, j6);
                j6 += 2;
                if (EntityHandler.getItemDef(k22 & 0x7fff).isStackable()) {
                    k12 = DataOperations.readInt(data, j6);
                    j6 += 4;
                }
                inventoryItems[i18] = k22 & 0x7fff;
                wearing[i18] = k22 / 32768;
                inventoryItemsCount[i18] = k12;
                if (i18 >= inventoryCount)
                    inventoryCount = i18 + 1;
            } else if (command == 191) {
                int k6 = data[1] & 0xff;
                inventoryCount--;
                for (int l12 = k6; l12 < inventoryCount; l12++) {
                    inventoryItems[l12] = inventoryItems[l12 + 1];
                    inventoryItemsCount[l12] = inventoryItemsCount[l12 + 1];
                    wearing[l12] = wearing[l12 + 1];
                }
            } else if (command == 208) {
                int pointer = 1;
                int idx = data[pointer++] & 0xff;
                int oldExp = playerStatExperience[idx];
                playerStatCurrent[idx] = DataOperations.getUnsignedByte(data[pointer++]);
                playerStatBase[idx] = DataOperations.getUnsignedByte(data[pointer++]);
                playerStatExperience[idx] = DataOperations.readInt(data, pointer);
                pointer += 4;
                
                if(playerStatExperience[idx] > oldExp) {
                	expGained += (playerStatExperience[idx] - oldExp);
                }
            } else if (command == 65) {
            	duelOpponentAccepted = data[1] == 1;
            } else if (command == 197) {
            	duelMyAccepted = data[1] == 1;
            } else if (command == 147) {
                showDuelConfirmWindow = true;
                duelWeAccept = false;
                showDuelWindow = false;
                int i7 = 1;
                duelOpponentNameLong = DataOperations.getUnsigned8Bytes(data, i7);
                i7 += 8;
                duelConfirmOpponentItemCount = data[i7++] & 0xff;
                for (int j13 = 0; j13 < duelConfirmOpponentItemCount; j13++) {
                    duelConfirmOpponentItems[j13] = DataOperations.getUnsigned2Bytes(data, i7);
                    i7 += 2;
                    duelConfirmOpponentItemsCount[j13] = DataOperations.readInt(data, i7);
                    i7 += 4;
                }

                duelConfirmMyItemCount = data[i7++] & 0xff;
                for (int j18 = 0; j18 < duelConfirmMyItemCount; j18++) {
                    duelConfirmMyItems[j18] = DataOperations.getUnsigned2Bytes(data, i7);
                    i7 += 2;
                    duelConfirmMyItemsCount[j18] = DataOperations.readInt(data, i7);
                    i7 += 4;
                }

                duelCantRetreat = data[i7++] & 0xff;
                duelUseMagic = data[i7++] & 0xff;
                duelUsePrayer = data[i7++] & 0xff;
                duelUseWeapons = data[i7++] & 0xff;
            } else if (command == 11) {
				boolean mp3 = DataOperations.getUnsignedByte(data[1]) == 1 ? true : false;
                String s = new String(data, 2, length - 2);
                playSound(s, mp3);
            } else if (command == 23) {
                if (teleportBubbleCount < 50) {
                    int j7 = data[1] & 0xff;
                    int k13 = data[2] + sectionX;
                    int k18 = data[3] + sectionY;
                    anIntArray782[teleportBubbleCount] = j7;
                    anIntArray923[teleportBubbleCount] = 0;
                    YKVE_mudclient_LQSP_2[teleportBubbleCount] = k13;
                    anIntArray757[teleportBubbleCount] = k18;
                    teleportBubbleCount++;
                }
            } else if (command == 248) {
                if (!hasReceivedWelcomeBoxDetails) {
                    lastLoggedInDays = DataOperations.getUnsigned2Bytes(data, 1);
                    subscriptionLeftDays = DataOperations.getUnsigned2Bytes(data, 3);
					lastLoggedInAddress = new String(data, 6, length - 6);
					lastLoggedInHostname = getHostname(lastLoggedInAddress);
                    showWelcomeBox = true;
                    hasReceivedWelcomeBoxDetails = true;
                }
            } else if (command == 148) {
                serverMessage = new String(data, 1, length - 1);
                showServerMessageBox = true;
                serverMessageBoxTop = false;
            } else if (command == 64) {
                serverMessage = new String(data, 1, length - 1).replaceAll("=", "\n");
                showServerMessageBox = true;
                serverMessageBoxTop = true;
            } else if (command == 126)
                fatigue = DataOperations.getUnsigned8Bytes(data, 1);
            else if (command == 202)
            	playersOnline = DataOperations.getUnsigned2Bytes(data, 1);
            else if (command == 203)
            	ownerID = DataOperations.getUnsigned2Bytes(data, 1);
            else if (command == 206) {
				super.inputText = "";
				super.enteredText = "";
				try {
					byte[] newData = new byte[Array.getLength(data) - 1];
					for (int x = 0; x < Array.getLength(newData); x++)
						newData[x] = data[x + 1];
					BufferedImage newImage = ImageIO.read(new ByteArrayInputStream(newData));
					sleepy = newImage;
					sleepSprite = Sprite.fromImage(sleepy);
				} catch(Exception ex) {}
				sleeping = true;
				kfr = null;
			} else if(command == 39)
				sleeping = false;
			else if(command == 225)
			   kfr = "Incorrect - Please wait...";
			else if (command == 200)
				kills = DataOperations.getUnsigned2Bytes(data, 1);
			else if (command == 201)
				deaths = DataOperations.getUnsigned2Bytes(data, 1);
			else if (command == 181) {
				if (autoScreenshots)
					takeScreenshot(false);
			} else if (command == 172)
                systemUpdate = DataOperations.getUnsigned2Bytes(data, 1) * 32;
            else if (command == 174) {
				wildernessSwitchType = (byte)DataOperations.getUnsignedByte(data[1]);
				wildernessUpdate = DataOperations.getUnsigned2Bytes(data, 2) * 32;
			} else if(command == 152) {
				byte settings = (byte)DataOperations.getUnsignedByte(data[1]);
				cameraRotate = ((settings & 1) == 0) ? false : true;
				configMouseButtons = ((settings & 2) == 0) ? true : false;
				configSoundEffects = ((settings & 4) == 0) ? true : false;
				hideCeilings = ((settings & 8) == 0) ? true : false;
				autoScreenshots = ((settings & 16) == 0) ? false : true;
				killNotify = ((settings & 32) == 0) ? false : true;
				if ((settings & 64) != 0)
					fightmode = 0;
				else if ((settings & 128) != 0)
					fightmode = 1;
				else
					fightmode = 2;
			} else if(command == 241) {
				int pointer = 1;
				int questCount = data[pointer++];
				for (int i = 0; i < questCount; i++) {
					int questID = data[pointer++];
					int status = data[pointer++];
					if (status == 0)
						quests[questID] = quests[questID].replaceAll("@red@", "@yel@");
					else
						quests[questID] = quests[questID].replaceAll("@red@", "@gre@");
				}
			} else if(command == 242)
				if (data[2] == 0)
					quests[data[1]] = quests[data[1]].replaceAll("@red@", "@yel@");
				else
					quests[data[1]] = quests[data[1]].replaceAll("@yel@", "@gre@");
			else if(command == 243)
				questPoints = DataOperations.getUnsigned2Bytes(data, 1);
        } catch (RuntimeException runtimeexception) {
            runtimeexception.printStackTrace();
            if (handlePacketErrorCount < 3) {
                super.streamClass.createPacket(3);
                super.streamClass.addString(runtimeexception.toString());
                super.streamClass.formatPacket();
                handlePacketErrorCount++;
            }
        }
    }

	protected final void lostConnection() {
		wildernessUpdate = 0;
		systemUpdate = 0;
		sleeping = false;
		if (logoutTimeout != 0) {
			resetIntVars();
			return;
		}
		super.lostConnection();
	}
	
	private final void playSound(final String s, final boolean mp3) {
		if (!mp3) {
			if (audioReader == null)
				return;
			if (configSoundEffects)
				return;
			audioReader.loadData(sounds, DataOperations.method358(s + ".pcm", sounds), DataOperations.method359(s + ".pcm", sounds));
		} else {
			new Thread(new Runnable() {
				public void run() {
					try {
//						jlap.playMp3(new File(DATA_DIR + s + ".mp3"), null); 
					} catch(Exception ex) {}
				}
			}).start();
		}
	}

    private final boolean sendWalkCommand(int walkSectionX, int walkSectionY, int x1, int y1, int x2, int y2, boolean stepBoolean, boolean coordsEqual) {
        int stepCount = engineHandle.getStepCount(walkSectionX, walkSectionY, x1, y1, x2, y2, sectionXArray, sectionYArray, stepBoolean);
        if (stepCount == -1)
            if (coordsEqual) {
                stepCount = 1;
                sectionXArray[0] = x1;
                sectionYArray[0] = y1;
            } else
                return false;
        stepCount--;
        walkSectionX = sectionXArray[stepCount];
        walkSectionY = sectionYArray[stepCount];
        stepCount--;
        if (coordsEqual)
            super.streamClass.createPacket(7);
        else
            super.streamClass.createPacket(6);
        super.streamClass.add2ByteInt(walkSectionX + areaX);
        super.streamClass.add2ByteInt(walkSectionY + areaY);
        if (coordsEqual && stepCount == -1 && (walkSectionX + areaX) % 5 == 0)
            stepCount = 0;
        for (int currentStep = stepCount; currentStep >= 0 && currentStep > stepCount - 25; currentStep--) {
            super.streamClass.addByte(sectionXArray[currentStep] - walkSectionX);
            super.streamClass.addByte(sectionYArray[currentStep] - walkSectionY);
        }

        super.streamClass.formatPacket();
        actionPictureType = -24;
        actionPictureX = super.mouseX;
        actionPictureY = super.mouseY;
        return true;
    }

    private final boolean sendWalkCommandIgnoreCoordsEqual(int walkSectionX, int walkSectionY, int x1, int y1, int x2, int y2, boolean stepBoolean, boolean coordsEqual) {
        int stepCount = engineHandle.getStepCount(walkSectionX, walkSectionY, x1, y1, x2, y2, sectionXArray, sectionYArray, stepBoolean);
        if (stepCount == -1)
            return false;
        stepCount--;
        walkSectionX = sectionXArray[stepCount];
        walkSectionY = sectionYArray[stepCount];
        stepCount--;
        if (coordsEqual)
            super.streamClass.createPacket(7);
        else
            super.streamClass.createPacket(6);
        super.streamClass.add2ByteInt(walkSectionX + areaX);
        super.streamClass.add2ByteInt(walkSectionY + areaY);
        if (coordsEqual && stepCount == -1 && (walkSectionX + areaX) % 5 == 0)
            stepCount = 0;
        for (int currentStep = stepCount; currentStep >= 0 && currentStep > stepCount - 25; currentStep--) {
            super.streamClass.addByte(sectionXArray[currentStep] - walkSectionX);
            super.streamClass.addByte(sectionYArray[currentStep] - walkSectionY);
        }

        super.streamClass.formatPacket();
        actionPictureType = -24;
        actionPictureX = super.mouseX;
        actionPictureY = super.mouseY;
        return true;
    }

	public final Image createImage(int i, int j) {
		if (GameWindow.gameFrame != null)
			return GameWindow.gameFrame.createImage(i, j);
		return super.createImage(i, j);
	}

    private final void drawTradeConfirmWindow() {
        int byte0 = gameWidth / 2 - 234;
        int byte1 = gameHeight - (gameHeight / 2 + 131);
        gameGraphics.drawBox(byte0, byte1, 468, 16, 192);
        int i = 0x989898;
        drawBoxAlpha(byte0, byte1 + 16, 468, 246, i, 160);
        drawText("Please confirm your trade with @yel@" + DataOperations.longToString(tradeConfirmOtherNameLong), byte0 + 234, byte1 + 12, 1, 0xffffff);
        drawText("You are about to give:", byte0 + 117, byte1 + 30, 1, 0xffff00);
        for (int j = 0; j < tradeConfirmItemCountxxx; j++) {
            String s = EntityHandler.getItemDef(tradeConfirmItems[j]).getName();
            if (EntityHandler.getItemDef(tradeConfirmItems[j]).isStackable())
                s = s + " x " + appendUnits(tradeConfirmItemsCount[j]);
            drawText(s, byte0 + 117, byte1 + 42 + j * 12, 1, 0xffffff);
        }

        if (tradeConfirmItemCountxxx == 0)
            drawText("Nothing!", byte0 + 117, byte1 + 42, 1, 0xffffff);
        drawText("In return you will receive:", byte0 + 351, byte1 + 30, 1, 0xffff00);
        for (int k = 0; k < tradeConfirmOtherItemCount; k++) {
            String s1 = EntityHandler.getItemDef(tradeConfirmOtherItems[k]).getName();
            if (EntityHandler.getItemDef(tradeConfirmOtherItems[k]).isStackable())
                s1 = s1 + " x " + appendUnits(tradeConfirmOtherItemsCount[k]);
            drawText(s1, byte0 + 351, byte1 + 42 + k * 12, 1, 0xffffff);
        }

        if (tradeConfirmOtherItemCount == 0)
            drawText("Nothing!", byte0 + 351, byte1 + 42, 1, 0xffffff);
        drawText("Are you sure you want to do this?", byte0 + 234, byte1 + 200, 4, 65535);
        drawText("There is NO WAY to reverse a trade if you change your mind.", byte0 + 234, byte1 + 215, 1, 0xffffff);
        drawText("Remember that not all players are trustworthy", byte0 + 234, byte1 + 230, 1, 0xffffff);
        if (!tradeConfirmAccepted) {
            gameGraphics.drawPicture((byte0 + 118) - 35, byte1 + 238, SPRITE_MEDIA_START + 25);
            gameGraphics.drawPicture((byte0 + 352) - 35, byte1 + 238, SPRITE_MEDIA_START + 26);
        } else {
            drawText("Waiting for other player...", byte0 + 234, byte1 + 250, 1, 0xffff00);
        }
        if (mouseButtonClick == 1) {
            if (super.mouseX < byte0 || super.mouseY < byte1 || super.mouseX > byte0 + 468 || super.mouseY > byte1 + 262) {
                showTradeConfirmWindow = false;
                super.streamClass.createPacket(41);
                super.streamClass.formatPacket();
            }
            if (super.mouseX >= (byte0 + 118) - 35 && super.mouseX <= byte0 + 118 + 70 && super.mouseY >= byte1 + 238 && super.mouseY <= byte1 + 238 + 21) {
                tradeConfirmAccepted = true;
                super.streamClass.createPacket(40);
                super.streamClass.formatPacket();
            }
            if (super.mouseX >= (byte0 + 352) - 35 && super.mouseX <= byte0 + 353 + 70 && super.mouseY >= byte1 + 238 && super.mouseY <= byte1 + 238 + 21) {
                showTradeConfirmWindow = false;
                super.streamClass.createPacket(41);
                super.streamClass.formatPacket();
            }
            mouseButtonClick = 0;
        }
    }

    private final void walkToGroundItem(int walkSectionX, int walkSectionY, int x, int y, boolean coordsEqual) {
        if (sendWalkCommandIgnoreCoordsEqual(walkSectionX, walkSectionY, x, y, x, y, false, coordsEqual)) {
            return;
        } else {
            sendWalkCommand(walkSectionX, walkSectionY, x, y, x, y, true, coordsEqual);
            return;
        }
    }

    private final Mob addNPC(int serverIndex, int x, int y, int nextSprite, int type) {
        if (npcRecordArray[serverIndex] == null) {
            npcRecordArray[serverIndex] = new Mob();
            npcRecordArray[serverIndex].serverIndex = serverIndex;
        }
        Mob mob = npcRecordArray[serverIndex];
        boolean npcAlreadyExists = false;
        for (int lastNpcIndex = 0; lastNpcIndex < lastNpcCount; lastNpcIndex++) {
            if (lastNpcArray[lastNpcIndex].serverIndex != serverIndex)
                continue;
            npcAlreadyExists = true;
            break;
        }

        if (npcAlreadyExists) {
            mob.type = type;
            mob.nextSprite = nextSprite;
            int waypointCurrent = mob.waypointCurrent;
            if (x != mob.waypointsX[waypointCurrent] || y != mob.waypointsY[waypointCurrent]) {
                mob.waypointCurrent = waypointCurrent = (waypointCurrent + 1) % 10;
                mob.waypointsX[waypointCurrent] = x;
                mob.waypointsY[waypointCurrent] = y;
            }
        } else {
            mob.serverIndex = serverIndex;
            mob.waypointEndSprite = 0;
            mob.waypointCurrent = 0;
            mob.waypointsX[0] = mob.currentX = x;
            mob.waypointsY[0] = mob.currentY = y;
            mob.type = type;
            mob.nextSprite = mob.currentSprite = nextSprite;
            mob.stepCount = 0;
        }
        npcArray[npcCount++] = mob;
        return mob;
    }
    private boolean clickScreenSend = false;
	private final void drawDuelWindow() {
		if(clickScreenSend) {
			mouseButtonClick = 4;
			clickScreenSend = false;
		}
		if(System.currentTimeMillis() - lastTradeDuelUpdate > 50) {
			boolean qItem = false;
			if (mouseButtonClick != 0 && itemIncrement == 0)
				itemIncrement = 1;
			if (mudclient.inputBoxType > 3 && mudclient.inputBoxType < 10)
				allowSendCommand = false;
			else
				allowSendCommand = true;
			if(allowSendCommand)			
			if (itemIncrement > 0) {
				int i = super.mouseX - (gameWidth - (gameWidth / 2 + 234));
				int j = super.mouseY - (gameHeight / 2 - 139);
				if (i >= 0 && j >= 0 && i < 468 && j < 262) {
					if (i > 216 && j > 30 && i < 462 && j < 235) {
						int k = (i - 217) / 49 + ((j - 31) / 34) * 5;
						if (k >= 0 && k < inventoryCount) {
							boolean flag1 = false;
							int l1 = 0;
							int k2 = inventoryItems[k];
							if(EntityHandler.getItemDef(k2).quest) {
								if(!qItem) {
									displayMessage("@cya@You cannot trade quest items", 3, 0);
								}
								qItem = true;
							}
							if(!qItem)
								if (mouseButtonClick != 2 && !tester && mouseButtonClick != 4) {
								for (int k3 = 0; k3 < duelMyItemCount; k3++)
									if (duelMyItems[k3] == k2)
										if (EntityHandler.getItemDef(k2).isStackable()) {
											for (int i4 = 0; i4 < itemIncrement; i4++) {
												if (duelMyItemsCount[k3] < inventoryItemsCount[k])
													duelMyItemsCount[k3]++;
												flag1 = true;
											}
										} else
											l1++;
							if (!qItem)
								if (inventoryCount(k2) <= l1)
									flag1 = true;
							if (!qItem)	
								if (!flag1 && duelMyItemCount < 8) {
									duelMyItems[duelMyItemCount] = k2;
									duelMyItemsCount[duelMyItemCount] = 1;
									duelMyItemCount++;
									flag1 = true;
								}
								}
							if (!qItem)
								if (mouseButtonClick != 2 && !tester && mouseButtonClick != 4) {
								if (flag1) {
									lastTradeDuelUpdate = System.currentTimeMillis();
									super.streamClass.createPacket(53);
									super.streamClass.addByte(duelMyItemCount);
									for (int duelItem = 0; duelItem < duelMyItemCount; duelItem++) {
										super.streamClass.add2ByteInt(duelMyItems[duelItem]);
										super.streamClass.add4ByteInt(duelMyItemsCount[duelItem]);
									}

									super.streamClass.formatPacket();
									duelOpponentAccepted = false;
									duelMyAccepted = false;
								}
								} else if (mouseButtonClick == 2) {
									if (tester) {
										tradeWindowX = -100;
										tradeWindowY = -100;
										mouseButtonClick = 0;
										tester = false;
										setValue = false;
									} else if (!tester) {
										tradeWindowX = super.mouseX;
										tradeWindowY = super.mouseY;
										/**
										 * We must clear the current entrys or we will be spammed :( < Mr sad face
										 */
										for (int jx = 0; jx < menuLength; jx++) {
											menuText1[jx] = null;
											menuText2[jx] = null;
											menuActionVariable[jx] = -1;
											menuActionVariable2[jx] = -1;
											menuID[jx] =-1;
										}
										String name = EntityHandler.getItemDef(k2).getName();
										
										menuLength = 0;								
										menuText1[menuLength] = "Stake 1 @lre@";
										menuText2[menuLength] = name;
										menuID[menuLength] = 882;
										menuActionVariable[menuLength] = k2;
										menuActionVariable2[menuLength] = 1;
										menuLength++;
										
										menuText1[menuLength] = "Stake 5 @lre@";
										menuText2[menuLength] = name;
										menuID[menuLength] = 882;
										menuActionVariable[menuLength] = k2;
										menuActionVariable2[menuLength] = 5;
										menuLength++;
										
										menuText1[menuLength] = "Stake 10 @lre@";
										menuText2[menuLength] = name;
										menuID[menuLength] = 882;
										menuActionVariable[menuLength] = k2;
										menuActionVariable2[menuLength] = 10;
										menuLength++;
										
										menuText1[menuLength] = "Stake All @lre@";
										menuText2[menuLength] = name;
										menuID[menuLength] = 882;
										menuActionVariable[menuLength] = k2;
										menuActionVariable2[menuLength] = inventoryCount(k2);
										menuActionType[menuLength] = 1234;
										menuLength++;
										
										menuText1[menuLength] = "Stake X @lre@";
										menuText2[menuLength] = name;
										menuID[menuLength] = 890; // Send that to the menu so we build the offer X
										menuActionVariable[menuLength] = k2;
										menuLength++;
										tester = true;
									}
								}
						}
					}
					if (!qItem)
						if (i > 8 && j > 30 && i < 205 && j < 129) {
							int l = (i - 9) / 49 + ((j - 31) / 34) * 4;
							if (l >= 0 && l < duelMyItemCount) {
								int j1 = duelMyItems[l];
								if (mouseButtonClick != 2 && !tester) {
									for (int i2 = 0; i2 < itemIncrement; i2++) {
										if (EntityHandler.getItemDef(j1)
												.isStackable()
												&& duelMyItemsCount[l] > 1) {
											duelMyItemsCount[l]--;
											continue;
										}
										duelMyItemCount--;
										mouseDownTime = 0;
										for (int l2 = l; l2 < duelMyItemCount; l2++) {
											duelMyItems[l2] = duelMyItems[l2 + 1];
											duelMyItemsCount[l2] = duelMyItemsCount[l2 + 1];
										}

										break;
									}
									if (mouseButtonClick != 2 && !tester && mouseButtonClick != 4) {
										lastTradeDuelUpdate = System
												.currentTimeMillis();
										super.streamClass.createPacket(53);
										super.streamClass
												.addByte(duelMyItemCount);
										for (int i3 = 0; i3 < duelMyItemCount; i3++) {
											super.streamClass
													.add2ByteInt(duelMyItems[i3]);
											super.streamClass
													.add4ByteInt(duelMyItemsCount[i3]);
										}
										super.streamClass.formatPacket();
										duelOpponentAccepted = false;
										duelMyAccepted = false;
									}
								}
								if (mouseButtonClick == 2) {
									if (tester) {
										tradeWindowX = -100;
										tradeWindowY = -100;
										mouseButtonClick = 0;
										tester = false;
										setValue = false;
									} else if (!tester) {
										tradeWindowX = super.mouseX;
										tradeWindowY = super.mouseY;
										/**
										 * We must clear the current entrys or we will be spammed :( < Mr sad face
										 */
										for (int jx = 0; jx < menuLength; jx++) {
											menuText1[jx] = null;
											menuText2[jx] = null;
											menuActionVariable[jx] = -1;
											menuActionVariable2[jx] = -1;
											menuID[jx] =-1;
										}
										String name = EntityHandler.getItemDef(j1).getName();
										
										menuLength = 0;								
										menuText1[menuLength] = "Remove 1@lre@";
										menuText2[menuLength] = name;
										menuID[menuLength] = 883;
										menuActionVariable[menuLength] = j1;
										menuActionVariable2[menuLength] = 1;
										menuLength++;
										
										menuText1[menuLength] = "Remove 5@lre@";
										menuText2[menuLength] = name;
										menuID[menuLength] = 883;
										menuActionVariable[menuLength] = j1;
										menuActionVariable2[menuLength] = 5;
										menuLength++;
										
										menuText1[menuLength] = "Remove 10@lre@";
										menuText2[menuLength] = name;
										menuID[menuLength] = 883;
										menuActionVariable[menuLength] = j1;
										menuActionVariable2[menuLength] = 10;
										menuLength++;
										
										menuText1[menuLength] = "Remove All@lre@";
										menuText2[menuLength] = name;
										menuID[menuLength] = 883;
										menuActionVariable[menuLength] = j1;
										menuActionVariable2[menuLength] = inventoryCount(j1);
										menuActionType[menuLength] = 1234;
										menuLength++;
										
										menuText1[menuLength] = "Remove X@lre@";
										menuText2[menuLength] = name;
										menuID[menuLength] = 889; // Send that to the menu so we build the offer X
										menuActionVariable[menuLength] = j1;
										menuLength++;
										tester = true;
									}
								}
							}
						}
					boolean flag = false;
					if (i >= 93 && j >= 221 && i <= 104 && j <= 232) {
						duelNoRetreating = !duelNoRetreating;
						flag = true;
					}
					if (i >= 93 && j >= 240 && i <= 104 && j <= 251) {
						duelNoMagic = !duelNoMagic;
						flag = true;
					}
					if (i >= 191 && j >= 221 && i <= 202 && j <= 232) {
						duelNoPrayer = !duelNoPrayer;
						flag = true;
					}
					if (i >= 191 && j >= 240 && i <= 202 && j <= 251) {
						duelNoWeapons = !duelNoWeapons;
						flag = true;
					}
					if (flag) {
						super.streamClass.createPacket(52);
						super.streamClass.addByte(duelNoRetreating ? 1 : 0);
						super.streamClass.addByte(duelNoMagic ? 1 : 0);
						super.streamClass.addByte(duelNoPrayer ? 1 : 0);
						super.streamClass.addByte(duelNoWeapons ? 1 : 0);
						super.streamClass.formatPacket();
						duelOpponentAccepted = false;
						duelMyAccepted = false;
					}
					if (i >= 217 && j >= 238 && i <= 286 && j <= 259 && !tester) {
						lastTradeDuelUpdate = System.currentTimeMillis();
						duelMyAccepted = true;
						super.streamClass.createPacket(49);
						super.streamClass.formatPacket();
					}
					if (i >= 394 && j >= 238 && i < 463 && j < 259 && !tester) {
						showDuelWindow = false;
						super.streamClass.createPacket(51);
						super.streamClass.formatPacket();
					}
				} else if (mouseButtonClick != 0 && !tester && mouseButtonClick != 4) {
					showDuelWindow = false;
					super.streamClass.createPacket(51);
					super.streamClass.formatPacket();
				}
				if (mouseButtonClick == 1 && tester) {
					for (int ix = 0; ix < menuLength; ix++) {
						int k = tradeWindowX + 2;
						int i1 = tradeWindowY + 11 + ix * 15;
						if (super.mouseX <= k - 2 || super.mouseY <= i1 - 12 || super.mouseY >= i1 + 4 || super.mouseX >= (k - 3) + menuWidth)
							continue;
						menuClick(ix);
					}
					tradeWindowX = -100;
					tradeWindowY = -100;
					mouseButtonClick = 0;
					tester = false;
					setValue = false;
				}
				mouseButtonClick = 0;
				itemIncrement = 0;
			}
		}
		if (!showDuelWindow)
			return;
		int byte0 = gameWidth / 2 - 234;
		int byte1 = gameHeight / 2 - 140;
		gameGraphics.drawBox(byte0, byte1, 468, 12, 0xc90b1d);
		int i1 = 0x989898;
		drawBoxAlpha(byte0, byte1 + 12, 468, 18, i1, 160);
		drawBoxAlpha(byte0, byte1 + 30, 8, 248, i1, 160);
		drawBoxAlpha(byte0 + 205, byte1 + 30, 11, 248, i1, 160);
		drawBoxAlpha(byte0 + 462, byte1 + 30, 6, 248, i1, 160);
		drawBoxAlpha(byte0 + 8, byte1 + 99, 197, 24, i1, 160);
		drawBoxAlpha(byte0 + 8, byte1 + 192, 197, 23, i1, 160);
		drawBoxAlpha(byte0 + 8, byte1 + 258, 197, 20, i1, 160);
		drawBoxAlpha(byte0 + 216, byte1 + 235, 246, 43, i1, 160);
		int k1 = 0xd0d0d0;
		drawBoxAlpha(byte0 + 8, byte1 + 30, 197, 69, k1, 160);
		drawBoxAlpha(byte0 + 8, byte1 + 123, 197, 69, k1, 160);
		drawBoxAlpha(byte0 + 8, byte1 + 215, 197, 43, k1, 160);
		drawBoxAlpha(byte0 + 216, byte1 + 30, 246, 205, k1, 160);
		for (int j2 = 0; j2 < 3; j2++)
			drawLineX(byte0 + 8, byte1 + 30 + j2 * 34, 197, 0);

		for (int j3 = 0; j3 < 3; j3++)
			drawLineX(byte0 + 8, byte1 + 123 + j3 * 34, 197, 0);

		for (int l3 = 0; l3 < 7; l3++)
			drawLineX(byte0 + 216, byte1 + 30 + l3 * 34, 246, 0);

		for (int k4 = 0; k4 < 6; k4++) {
			if (k4 < 5)
				drawLineY(byte0 + 8 + k4 * 49, byte1 + 30, 69, 0);
			if (k4 < 5)
				drawLineY(byte0 + 8 + k4 * 49, byte1 + 123, 69, 0);
			drawLineY(byte0 + 216 + k4 * 49, byte1 + 30, 205, 0);
		}

		drawLineX(byte0 + 8, byte1 + 215, 197, 0);
		drawLineX(byte0 + 8, byte1 + 257, 197, 0);
		drawLineY(byte0 + 8, byte1 + 215, 43, 0);
		drawLineY(byte0 + 204, byte1 + 215, 43, 0);
		drawString("Preparing to duel with: " + duelOpponentName, byte0 + 1, byte1 + 10, 1, 0xffffff);
		drawString("Your Stake", byte0 + 9, byte1 + 27, 4, 0xffffff);
		drawString("Opponent's Stake", byte0 + 9, byte1 + 120, 4, 0xffffff);
		drawString("Duel Options", byte0 + 9, byte1 + 212, 4, 0xffffff);
		drawString("Your Inventory", byte0 + 216, byte1 + 27, 4, 0xffffff);
		drawString("No retreating", byte0 + 8 + 1, byte1 + 215 + 16, 3, 0xffff00);
		drawString("No magic", byte0 + 8 + 1, byte1 + 215 + 35, 3, 0xffff00);
		drawString("No prayer", byte0 + 8 + 102, byte1 + 215 + 16, 3, 0xffff00);
		drawString("No weapons", byte0 + 8 + 102, byte1 + 215 + 35, 3, 0xffff00);
		gameGraphics.drawBoxEdge(byte0 + 93, byte1 + 215 + 6, 11, 11, 0xffff00);
		if (duelNoRetreating)
			gameGraphics.drawBox(byte0 + 95, byte1 + 215 + 8, 7, 7, 0xffff00);
		gameGraphics.drawBoxEdge(byte0 + 93, byte1 + 215 + 25, 11, 11, 0xffff00);
		if (duelNoMagic)
			gameGraphics.drawBox(byte0 + 95, byte1 + 215 + 27, 7, 7, 0xffff00);
		gameGraphics.drawBoxEdge(byte0 + 191, byte1 + 215 + 6, 11, 11, 0xffff00);
		if (duelNoPrayer)
			gameGraphics.drawBox(byte0 + 193, byte1 + 215 + 8, 7, 7, 0xffff00);
		gameGraphics.drawBoxEdge(byte0 + 191, byte1 + 215 + 25, 11, 11, 0xffff00);
		if (duelNoWeapons)
			gameGraphics.drawBox(byte0 + 193, byte1 + 215 + 27, 7, 7, 0xffff00);
		if (!duelMyAccepted)
			gameGraphics.drawPicture(byte0 + 217, byte1 + 238, SPRITE_MEDIA_START + 25);
		gameGraphics.drawPicture(byte0 + 394, byte1 + 238, SPRITE_MEDIA_START + 26);
		if (duelOpponentAccepted) {
			drawText("Other player", byte0 + 341, byte1 + 246, 1, 0xffffff);
			drawText("has accepted", byte0 + 341, byte1 + 256, 1, 0xffffff);
		}
		if (duelMyAccepted) {
			drawText("Waiting for", byte0 + 217 + 35, byte1 + 246, 1, 0xffffff);
			drawText("other player", byte0 + 217 + 35, byte1 + 256, 1, 0xffffff);
		}
		for (int l4 = 0; l4 < inventoryCount; l4++) {
			int i5 = 217 + byte0 + (l4 % 5) * 49;
			int k5 = 31 + byte1 + (l4 / 5) * 34;
			gameGraphics.spriteClip4(i5, k5, 48, 32, SPRITE_ITEM_START + EntityHandler.getItemDef(inventoryItems[l4]).getSprite(), EntityHandler.getItemDef(inventoryItems[l4]).getPictureMask(), 0, 0, false);
			if (EntityHandler.getItemDef(inventoryItems[l4]).isStackable())
				drawString(insertCommas(String.valueOf(inventoryItemsCount[l4])), i5 + 1, k5 + 10, 1, 0xffff00);
		}

		for (int j5 = 0; j5 < duelMyItemCount; j5++) {
			int l5 = 9 + byte0 + (j5 % 4) * 49;
			int j6 = 31 + byte1 + (j5 / 4) * 34;
			gameGraphics.spriteClip4(l5, j6, 48, 32, SPRITE_ITEM_START + EntityHandler.getItemDef(duelMyItems[j5]).getSprite(), EntityHandler.getItemDef(duelMyItems[j5]).getPictureMask(), 0, 0, false);
			if (EntityHandler.getItemDef(duelMyItems[j5]).isStackable())
				drawString(insertCommas("" + String.valueOf(duelMyItemsCount[j5])), l5 + 1, j6 + 10, 1, 0xffff00);
			if (super.mouseX > l5 && super.mouseX < l5 + 48 && super.mouseY > j6 && super.mouseY < j6 + 32)
				drawString(EntityHandler.getItemDef(duelMyItems[j5]).getName() + ": @whi@" + EntityHandler.getItemDef(duelMyItems[j5]).getDescription(), byte0 + 8, byte1 + 273, 1, 0xffff00);
		}

		for (int i6 = 0; i6 < duelOpponentItemCount; i6++) {
			int k6 = 9 + byte0 + (i6 % 4) * 49;
			int l6 = 124 + byte1 + (i6 / 4) * 34;
			gameGraphics.spriteClip4(k6, l6, 48, 32, SPRITE_ITEM_START + EntityHandler.getItemDef(duelOpponentItems[i6]).getSprite(), EntityHandler.getItemDef(duelOpponentItems[i6]).getPictureMask(), 0, 0, false);
			if (EntityHandler.getItemDef(duelOpponentItems[i6]).isStackable())
				drawString(insertCommas("" + String.valueOf(duelOpponentItemsCount[i6])), k6 + 1, l6 + 10, 1, 0xffff00);
			if (super.mouseX > k6 && super.mouseX < k6 + 48 && super.mouseY > l6 && super.mouseY < l6 + 32)
				drawString(EntityHandler.getItemDef(duelOpponentItems[i6]).getName() + ": @whi@" + EntityHandler.getItemDef(duelOpponentItems[i6]).getDescription(), byte0 + 8, byte1 + 273, 1, 0xffff00);
		}

	}

	private final void addNewServerNotification(String notification) {
		for(int index = 2; index > 0; index--) {
			notifications[index] = notifications[index - 1];
			notificationsTimeout[index] = notificationsTimeout[index - 1];
		}
		notifications[0] = notification;
		notificationsTimeout[0] = 300;	
	}
	
	private final void displayServerNotifications() {
		for(int index = 0; index < 3; index++) {
			if(notificationsTimeout[index] > 0) {
				drawText(notifications[index], gameWidth / 2, 16 + (index * 12) , 1, 0xffffff);
				notificationsTimeout[index]--;
			}
		}
	}
	
    private final void drawServerMessageBox() {
        char c = '\u0190';  //WIDTH
        char c1 = 'd';
        if (serverMessageBoxTop) {
            c1 = '\u01C2'; //HEIGHT
            c1 = '\u012C';
        }
        gameGraphics.drawBox((gameWidth / 2 - c / 2), (gameHeight / 2 - c1 / 2), c, c1, 0); //added times 2
        gameGraphics.drawBoxEdge((gameWidth / 2 - c / 2), (gameHeight / 2 - c1 / 2), c, c1, 0xffffff); //
        gameGraphics.drawBoxTextColour(serverMessage, (gameWidth / 2), (gameHeight / 2 - c1 / 2) + 20, 1, 0xffffff, c -40); //was -40
        int j = 0xffffff;
        if (super.mouseY > (gameHeight / 2 + c1 / 2 - 15) && super.mouseY <= (gameHeight / 2 + c1 / 2 - 4) && super.mouseX > gameWidth / 2 - 75 && super.mouseX < gameWidth / 2 + 75) // SRCH4: CHG11
            j = 0xff0000;
        drawText("Click here to close window", gameWidth / 2, (gameHeight / 2 + c1 / 2 - 7), 1, j);
        if (mouseButtonClick == 1) {
            if (j == 0xff0000)
                showServerMessageBox = false;
            if ((super.mouseX < gameWidth / 2 - 200 || super.mouseX > gameWidth / 2 + 200) || (super.mouseY < gameHeight / 2 - 150 || super.mouseY > gameHeight / 2 + 150))
                showServerMessageBox = false;
        }
        mouseButtonClick = 0;
    }

    private final void makeLoginMenus() {
        menuWelcome = new Menu(gameGraphics, 50);
        int i = 40;
            menuWelcome.drawText(windowWidth / 2, windowHeight / 2 + 33 + i, "Welcome to RuneScape Classic", 4, true);
            menuWelcome.drawBox(windowWidth / 2 - 100, windowHeight / 2 + 73 + i, 120, 35);
            menuWelcome.drawBox(windowWidth / 2 + 100, windowHeight / 2 + 73 + i, 120, 35);
            menuWelcome.drawText(windowWidth / 2 - 100, windowHeight / 2 + 73 + i, "New User", 5, false);
            menuWelcome.drawText(windowWidth / 2 + 100, windowHeight / 2 + 73 + i, "Existing User", 5, false);
            loginButtonNewUser = menuWelcome.makeButton(windowWidth / 2 - 100, windowHeight / 2 + 73 + i, 120, 35);
            loginButtonExistingUser = menuWelcome.makeButton(windowWidth / 2 + 100, windowHeight / 2 + 73 + i, 120, 35);
		menuNewUser = new Menu(gameGraphics, 50);
        i = windowHeight / 2 + 63;
		menuNewUser.drawText(windowWidth / 2, i + 8, "To create an account please go back to the", 4, true);
		i += 20;
		menuNewUser.drawText(windowWidth / 2, i + 8, "rscemulation.net front page, and choose 'register'", 4, true);
		i += 30;
		menuNewUser.drawBox(windowWidth / 2, i + 17, 150, 34);
        menuNewUser.drawText(windowWidth / 2, i + 17, "Ok", 5, false);
        newUserOkButton = menuNewUser.makeButton(windowWidth / 2, i + 17, 150, 34);
        menuLogin = new Menu(gameGraphics, 50);
        i = windowHeight / 2 + 63;
        loginStatusText = menuLogin.drawText(windowWidth / 2, i - 10, "Please enter your username and password", 4, true);
        i += 28;
        menuLogin.drawBox(windowWidth / 2 - 116, i, 200, 40);
        menuLogin.drawText(windowWidth / 2 - 116, i - 10, "Username:", 4, false);
        loginUsernameTextBox = menuLogin.makeTextBox(windowWidth / 2 - 116, i + 10, 200, 40, 4, 12, false, false);
        i += 47;
        menuLogin.drawBox(windowWidth / 2 - 66, i, 200, 40);
        menuLogin.drawText(windowWidth / 2 - 66, i - 10, "Password:", 4, false);
        loginPasswordTextBox = menuLogin.makeTextBox(windowWidth / 2 - 66, i + 10, 200, 40, 4, 20, true, false);
        i -= 55;
        menuLogin.drawBox(windowWidth / 2 + 154, i, 120, 25);
        menuLogin.drawText(windowWidth / 2 + 154, i, "Ok", 4, false);
        loginOkButton = menuLogin.makeButton(windowWidth / 2 + 154, i, 120, 25);
        i += 30;
        menuLogin.drawBox(windowWidth / 2 + 154, i, 120, 25);
        menuLogin.drawText(windowWidth / 2 + 154, i, "Cancel", 4, false);
        loginCancelButton = menuLogin.makeButton(windowWidth / 2 + 154, i, 120, 25);
        i += 30;
        menuLogin.setFocus(loginUsernameTextBox);
    }

    private final void drawGameWindowsMenus() {
		if (!showTradeWindow && (inputBoxType == 6 || inputBoxType == 7))
			inputBoxType = 0;
		if (!showBank && (inputBoxType == 4 || inputBoxType == 5))
			inputBoxType = 0;
		if (!showDuelWindow && (inputBoxType == 8 || inputBoxType == 9))
			inputBoxType = 0;   	
        if (logoutTimeout != 0)
            drawLoggingOutBox();
        else if (showWelcomeBox)
            drawWelcomeBox();
        else if (showServerMessageBox)
            drawServerMessageBox();
        else if (wildernessType == 1) // 0 = not wild, 1 = close to wild, 2 = wild
            drawWildernessWarningBox();
		else if (showBank && lastWalkTimeout == 0) {
			drawBankBox();
			if(tester)
				drawTestShit();
			if (inputBoxType != 0)
				drawInputBox();
		}
        else if (showShop && lastWalkTimeout == 0)
            drawShopBox();
        else if (showTradeConfirmWindow)
            drawTradeConfirmWindow();
		else if (showTradeWindow) {
			drawTradeWindow();
			if(tester)
				drawTestShit();
			if (inputBoxType != 0)
				drawInputBox();
		}
        else if (showDuelConfirmWindow)
            drawDuelConfirmWindow();
		else if (showDuelWindow) {
			drawDuelWindow();
		if(tester)
			drawTestShit();
		if (inputBoxType != 0)
			drawInputBox();
		}
        else if (showAbuseWindow == 1)
            drawAbuseWindow1();
        else if (showAbuseWindow == 2)
            drawAbuseWindow2();
        else if (showCommandsWindow == 1)
            drawCommandsWindow();
        else if (inputBoxType != 0)
            drawInputBox();
		else if(drawStaffMenu)
			drawStaffMenu();
		else if(drawIntegerInputBox)
			drawIntegerInputBox();
		else if(showDevelopersInputBox)
			showDevelopersInputBox("hi");
		else if(drawSearchBox && searchResults != null)
			drawSearchBox(searchResults);
        else {
            if (showQuestionMenu)
                drawQuestionMenu();
            if (((ourPlayer.currentSprite == 8 || ourPlayer.currentSprite == 9) || fightmode == 0) && fightmode != 2)
                drawCombatStyleWindow();
            checkMouseOverMenus();
            boolean noMenusShown = !showQuestionMenu && !showRightClickMenu;
            if (noMenusShown)
                menuLength = 0;
            if (mouseOverMenu == 0 && noMenusShown)
                drawInventoryRightClickMenu();
            if (mouseOverMenu == 1)
                drawInventoryMenu(noMenusShown);
            if (mouseOverMenu == 2)
                drawMapMenu(noMenusShown);
            if (mouseOverMenu == 3)
                drawPlayerInfoMenu(noMenusShown);
            if (mouseOverMenu == 4)
                drawMagicWindow(noMenusShown);
            if (mouseOverMenu == 5) {
				if(ourPlayer.cName.equals("")) {
					if (anInt981 == 2)
						anInt981 = 0;
					drawFriendsWindowNoClan(noMenusShown);
				} else
					drawFriendsWindowWithClan(noMenusShown);
			}
            if (mouseOverMenu == 6)
                drawOptionsMenu(noMenusShown);
            if (!showRightClickMenu && !showQuestionMenu)
                checkMouseStatus();
            if (showRightClickMenu && !showQuestionMenu)
                drawRightClickMenu();
        }
        mouseButtonClick = 0;
    }
    
    private boolean setValue = false;
    
	private void drawTestShit() {
		if (!showTradeWindow || !showDuelWindow || !showBank || !showRightClickMenu) {
			if (super.mouseX < tradeWindowX - 10 || super.mouseY < tradeWindowY - 2 || super.mouseX > tradeWindowX + menuWidth + 10 || super.mouseY > tradeWindowY + menuHeight + 10) {
				tradeWindowX = -100;
				tradeWindowY = -100;
				showRightClickMenu = false;
				tester = false;
				setValue = false;
				canSendBankWindow = false;
				return;
			}
			for (int i = 0; i < menuLength; i++)
				menuIndexes[i] = i;
	
			for (boolean flag = false; !flag;) {
				flag = true;
				for (int j = 0; j < menuLength - 1; j++) {
					int l = menuIndexes[j];
					int j1 = menuIndexes[j + 1];
					if (menuID[l] > menuID[j1]) {
						menuIndexes[j] = j1;
						menuIndexes[j + 1] = l;
						flag = false;
					}
				}
	
			}
			menuHeight = (int)((menuLength) * 15);
			menuWidth = gameGraphics.textWidth("Choose option", 1) + 5;
			for (int k1 = 0; k1 < menuLength; k1++) {
				int l1 = gameGraphics.textWidth(menuText1[k1] + " " + menuText2[k1], 1) + 5;
				if (l1 > menuWidth)
					menuWidth = l1;
			}
			if (!setValue) {
				tradeWindowX = super.mouseX - menuWidth / 2;
				tradeWindowY = super.mouseY - 7;
				setValue = true;
			}
			
			if (tradeWindowX < 0)
				tradeWindowX = 5;
			if (tradeWindowY < 0)
				tradeWindowY = 5;
			if (tradeWindowX + menuWidth > gameWidth)
				tradeWindowX = gameWidth - menuWidth - 5;
			if (tradeWindowY + menuHeight > gameHeight)
				tradeWindowY = gameHeight - menuHeight - 5;
			
			drawBoxAlpha(tradeWindowX, tradeWindowY, menuWidth, menuHeight, 0xd0d0d0, 160);
			for (int j = 0; j < menuLength; j++) {
				int l = tradeWindowX + 2;
				int j1 = tradeWindowY + 11 + j * 15;
				int k1 = 0xffffff;
				if (super.mouseX > l - 2 && super.mouseY > j1 - 12 && super.mouseY < j1 + 4 && super.mouseX < (l - 3) + menuWidth)
					k1 = 0xffff00;
				drawString(menuText1[menuIndexes[j]] + " " + menuText2[menuIndexes[j]], l, j1, 1, k1);
			}
		} else {	
			tradeWindowX = -100;
			tradeWindowY = -100;
			for (int jx = 0; jx < menuLength; jx++) {
				menuText1[jx] = null;
				menuText2[jx] = null;
				menuActionVariable[jx] = -1;
				menuActionVariable2[jx] = -1;
				menuID[jx] = -1;
			}			
			showRightClickMenu = false;
			tester = false;
			setValue = false;
			canSendBankWindow = false;
			inputBoxType = 0;
		}
	}

	private final void drawStaffMenu() {
		staffMenuSelection = 0;
		int i = gameHeight / 2 - 55;
		for (int j = 0; j < 10; j++) {
			if (super.mouseX > gameWidth / 2 - 160 && super.mouseX < gameWidth / 2 + 160 && super.mouseY >= i - 12 && super.mouseY < i + 3)
				if((ourPlayer.admin == 5 && (j + 1) < 7) || (ourPlayer.admin == 6 && (j + 1) < 10) || (ourPlayer.admin == 1) || (ourPlayer.admin == 2) || (ourPlayer.admin == 3))
					staffMenuSelection = j + 1;
			i += 15;
		}
		if (mouseButtonClick != 0 && staffMenuSelection != 0) {
			mouseButtonClick = 0;
			super.inputText = "";
			super.enteredText = "";
			switch(staffMenuSelection) {
				case 1:	//Mute
					inputBoxMessage = "Enter the reason for the action taken: ";
					inputBoxLabel = "Reason: ";
					break;
				case 2:	//Remove Mute
					inputBoxMessage = "Enter the reason for the action taken: ";
					inputBoxLabel = "Reason: ";
					break;
				case 3:	//Temp Mute
					inputBoxMessage = "Enter the time interval: ";
					inputBoxLabel = "Minutes: ";
					break;
				case 4:	//Say mute
					inputBoxMessage = "Enter the reason for the action taken: ";
					inputBoxLabel = "Reason: ";
					break;
				case 5:	//remove say mute
					inputBoxMessage = "Enter the reason for the action taken: ";
					inputBoxLabel = "Reason: ";
					break;
				case 6:	//temp say mute
					inputBoxMessage = "Enter the time interval: ";
					inputBoxLabel = "Minutes: ";
					//sendChatString("::mute " + selectedPlayer);
					break;
				case 7:	//jail
					inputBoxMessage = "Enter the reason for the action taken: ";
					inputBoxLabel = "Reason: ";
					break;
				case 8:	//release
					inputBoxMessage = "Enter the reason for the action taken: ";
					inputBoxLabel = "Reason: ";
					break;
				case 9:	//tempjail
					inputBoxMessage = "Enter the time interval: ";
					inputBoxLabel = "Minutes: ";
					break;
				case 10:	//ban
					inputBoxMessage = "Enter the reason for the action taken: ";
					inputBoxLabel = "Reason: ";
			}
			drawStaffMenu = !drawStaffMenu;
			drawIntegerInputBox = true;
			return;
		}
		i += 15;
		if (mouseButtonClick != 0) {
			mouseButtonClick = 0;
			if(mouseInCoords(gameWidth / 2 - 160, gameWidth / 2 + 160, gameHeight / 2 + 90, gameHeight / 2 + 100)) {
				drawStaffMenu = !drawStaffMenu;
				return;
			}
			if(!mouseInCoords(gameWidth / 2 - 160, gameWidth / 2 + 160, gameHeight / 2 - 100, gameHeight / 2 + 100)) {
				drawStaffMenu = !drawStaffMenu;
				return;
			}
		}
	
		gameGraphics.drawBox(gameWidth / 2 - 160, gameHeight / 2 - 100, 320, 205, 0);
		gameGraphics.drawBoxEdge(gameWidth / 2 - 160, gameHeight / 2 - 100, 320, 205, 0xffffff);
		i = -55;
		drawText("Staff Menu logged in as: " + ourPlayer.name, gameWidth / 2, gameHeight / 2 - 85, 4, 0xffffff);
		drawText("Warning - ALL staff transactions are logged", gameWidth / 2, gameHeight / 2 - 70, 3, 0xff0000);
		int index = 1;
		for(String s : Strings.staffMenu) {
			if(staffMenuSelection == index) {
				gameGraphics.drawBoxEdge(gameWidth / 2 - 100, gameHeight / 2 + i - 12, 200, 15, 0xffffff);
				drawText(s + selectedPlayer, gameWidth / 2, gameHeight / 2 + i, 2, 0xff8000);
			} else {
				drawText(s + selectedPlayer, gameWidth / 2, gameHeight / 2 + i, 2, 0xffffff);
			}
			index++;
			i += 15;
		}
		if(mouseInCoords(gameWidth / 2 - 160, gameWidth / 2 + 160, gameHeight / 2 + i - 12, gameHeight / 2 + i + 3)) {
			gameGraphics.drawBoxEdge(gameWidth / 2 - 100, gameHeight / 2 + i - 12, 200, 15, 0xffffff);
			drawText("Click here to close window", gameWidth / 2, gameHeight / 2 + i, 2, 0xff8000);
		} else {
			drawText("Click here to close window", gameWidth / 2, gameHeight / 2 + i, 2, 0xffffff);
		}
	}

	private final void drawSearchBox(String[] choices) {
		int searchSelection = -1;
		int i = gameHeight / 2 - 55;
		for (int j = 0; j < 10; j++) {
			if (j < choices.length && super.mouseX > gameWidth / 2 - 160 && super.mouseX < gameWidth / 2 + 160 && super.mouseY >= i - 12 && super.mouseY < i + 3)
					searchSelection = j + 1;
			i += 15;
		}
		if (mouseButtonClick != 0 && searchSelection != -1) {
			mouseButtonClick = 0;
			super.inputText = "";
			super.enteredText = "";
			switch(searchSelection) {
				default:
					try {
						String select = searchResults[searchSelection - 1];
						String id = "";
						boolean flag = false;
						for(char c : select.toCharArray()) {
							if(c == '(') {
								flag = true;
								continue;
							}
							if(c != '(' && !flag)
								continue;
							if(c != ')')
								id += String.valueOf(c);
						}
						entityID = Integer.parseInt(id);
					} catch(Exception e){
						System.out.println(e);
					}
			}
			drawSearchBox = !drawSearchBox;
			return;
		}
		i += 15;
		if (mouseButtonClick != 0) {
			mouseButtonClick = 0;
			if(mouseInCoords(gameWidth / 2 - 160, gameWidth / 2 + 160, gameHeight / 2 + 90, gameHeight / 2 + 100)) {
				searching = !searching;
				drawSearchBox = !drawSearchBox;
				resetSearch();
				return;
			}
			if(!mouseInCoords(gameWidth / 2 - 160, gameWidth / 2 + 160, gameHeight / 2 - 100, gameHeight / 2 + 100)) {
				searching = !searching;
				drawSearchBox = !drawSearchBox;
				resetSearch();
				return;
			}
		}
	
		gameGraphics.drawBox(gameWidth / 2 - 160, gameHeight / 2 - 100, 320, 205, 0);
		gameGraphics.drawBoxEdge(gameWidth / 2 - 160, gameHeight / 2 - 100, 320, 205, 0xffffff);
		i = -55;
		drawText("Developer logged in as: " + ourPlayer.name, gameWidth / 2, gameHeight / 2 - 85, 4, 0xffffff);
		drawText("Entity Search: " + super.inputText + "*", gameWidth / 2, gameHeight / 2 - 70, 4, 0xffffff);
		int index = 1;
		for(String s : choices) {
			if(searchSelection == index) {
				gameGraphics.drawBoxEdge(gameWidth / 2 - 100, gameHeight / 2 + i - 12, 200, 15, 0xffffff);
				drawText(s + selectedPlayer, gameWidth / 2, gameHeight / 2 + i, 2, 0xff8000);
			} else {
				drawText(s + selectedPlayer, gameWidth / 2, gameHeight / 2 + i, 2, 0xffffff);
			}
			index++;
			i += 15;
		}
		if(mouseInCoords(gameWidth / 2 - 160, gameWidth / 2 + 160, gameHeight / 2 + 95 - 12, gameHeight / 2 + i + 3)) {
			gameGraphics.drawBoxEdge(gameWidth / 2 - 100, gameHeight / 2 + 95 - 12, 200, 15, 0xffffff);
			drawText("Click here to close window", gameWidth / 2, gameHeight / 2 + 95, 2, 0xff8000);
		} else {
			drawText("Click here to close window", gameWidth / 2, gameHeight / 2 + 95, 2, 0xffffff);
		}
	}
	
	private final void showDevelopersInputBox(String type) {
		int developerIndex = -1;
		int i = gameHeight / 2 - 55;
		for (int j = 0; j < 5; j++) {
			if (super.mouseX > gameWidth / 2 - 160 && super.mouseX < gameWidth / 2 + 160 && super.mouseY >= i - 12 && super.mouseY < i + 3)
				if((ourPlayer.admin == 5 && (j + 1) < 7) || (ourPlayer.admin == 6 && (j + 1) < 10) || (ourPlayer.admin == 1) || (ourPlayer.admin == 2) || (ourPlayer.admin == 3))
					developerIndex = j + 1;
			i += 15;
		}
		gameGraphics.drawBox(gameWidth / 2 - 160, gameHeight / 2 - 100, 320, 205, 0);
		gameGraphics.drawBoxEdge(gameWidth / 2 - 160, gameHeight / 2 - 100, 320, 205, 0xffffff);
		i = -55;
		drawText("Developer logged in as: " + ourPlayer.name, gameWidth / 2, gameHeight / 2 - 85, 4, 0xffffff);
		drawText("ALL developer transactions are logged", gameWidth / 2, gameHeight / 2 - 70, 3, 0xff0000);
		int index = 1;
		for(String s : Strings.developerMenuA) {
			if(developerIndex == index) {
				gameGraphics.drawBoxEdge(gameWidth / 2 - 100, gameHeight / 2 + i - 12, 200, 15, 0xffffff);
				drawText(s + selectedPlayer, gameWidth / 2, gameHeight / 2 + i, 2, 0xff8000);
			} else {
				drawText(s + selectedPlayer, gameWidth / 2, gameHeight / 2 + i, 2, 0xffffff);
			}
			index++;
			i += 15;
		}
		if(mouseInCoords(gameWidth / 2 - 160, gameWidth / 2 + 160, gameHeight / 2 + i - 12, gameHeight / 2 + i + 3)) {
			gameGraphics.drawBoxEdge(gameWidth / 2 - 100, gameHeight / 2 + i - 12, 200, 15, 0xffffff);
			drawText("Click here to close window", gameWidth / 2, gameHeight / 2 + i, 2, 0xff8000);
		} else {
			drawText("Click here to close window", gameWidth / 2, gameHeight / 2 + i, 2, 0xffffff);
		}
		if(mouseButtonClick != 0 && developerIndex != -1) {
			switch(developerIndex) {
				case 1:
					searchType = "items";
					showDevelopersInputBox = !showDevelopersInputBox;
					drawSearchBox = true;
					searching = true;
					break;
				case 2:
					searchType = "objects";
					showDevelopersInputBox = !showDevelopersInputBox;
					drawSearchBox = true;
					searching = true;
					break;
				case 3:
					searchType = "npcs";
					showDevelopersInputBox = !showDevelopersInputBox;
					drawSearchBox = true;
					searching = true;
					break;
				case 4:
					searchType = "doors";
					showDevelopersInputBox = !showDevelopersInputBox;
					drawSearchBox = true;
					searching = true;
					break;
				case 5:
					showDevelopersInputBox = !showDevelopersInputBox;
					searching = false;
			}
			entityType = developerIndex;
		}
		if (mouseButtonClick != 0) {
			mouseButtonClick = 0;
			if(mouseInCoords(gameWidth / 2 - 160, gameWidth / 2 + 160, gameHeight / 2 + 90, gameHeight / 2 + 100)) {
				searching = !searching;
				showDevelopersInputBox = !showDevelopersInputBox;
				resetSearch();
				return;
			}
			if(!mouseInCoords(gameWidth / 2 - 160, gameWidth / 2 + 160, gameHeight / 2 - 100, gameHeight / 2 + 100)) {
				searching = !searching;
				showDevelopersInputBox = !showDevelopersInputBox;
				resetSearch();
				return;
			}
		}
	}
	
	private final void drawIntegerInputBox() {
		if (super.enteredText.length() > 0) {
			String s = super.enteredText.trim();
			super.inputText = "";
			super.enteredText = "";
			if (s.length() > 0) {
				if(inputBoxLabel.equals("Reason: ")) {
					drawIntegerInputBox = !drawIntegerInputBox;
					actionReason = s;
					switch(staffMenuSelection) {
						case 1:	//Mute
							super.streamClass.createPacket(77);
							super.streamClass.addByte(0);
							super.streamClass.add2ByteInt(selectedPlayerIndex);
							super.streamClass.addString(actionReason);
							super.streamClass.formatPacket();
							break;
						case 2:	//Remove Mute
							super.streamClass.createPacket(77);
							super.streamClass.addByte(1);
							super.streamClass.add2ByteInt(selectedPlayerIndex);
							super.streamClass.addString(actionReason);
							super.streamClass.formatPacket();
							break;
						case 3:	//Temp Mute
							super.streamClass.createPacket(77);
							super.streamClass.addByte(2);
							super.streamClass.add2ByteInt(selectedPlayerIndex);
							super.streamClass.add4ByteInt(actionTime);
							super.streamClass.addString(actionReason);
							super.streamClass.formatPacket();
							break;
						case 4:	//Say mute
							super.streamClass.createPacket(77);
							super.streamClass.addByte(3);
							super.streamClass.add2ByteInt(selectedPlayerIndex);
							super.streamClass.addString(actionReason);
							super.streamClass.formatPacket();
							break;
						case 5:	//remove say mute
							super.streamClass.createPacket(77);
							super.streamClass.addByte(4);
							super.streamClass.add2ByteInt(selectedPlayerIndex);
							super.streamClass.addString(actionReason);
							super.streamClass.formatPacket();
							break;
						case 6:	//temp say mute
							super.streamClass.createPacket(77);
							super.streamClass.addByte(5);
							super.streamClass.add2ByteInt(selectedPlayerIndex);
							super.streamClass.add4ByteInt(actionTime);
							super.streamClass.addString(actionReason);
							super.streamClass.formatPacket();
							break;
						case 7:	//jail
							super.streamClass.createPacket(77);
							super.streamClass.addByte(6);
							super.streamClass.add2ByteInt(selectedPlayerIndex);
							super.streamClass.addString(actionReason);
							super.streamClass.formatPacket();
							break;
						case 8:	//release
							super.streamClass.createPacket(77);
							super.streamClass.addByte(7);
							super.streamClass.add2ByteInt(selectedPlayerIndex);
							super.streamClass.addString(actionReason);
							super.streamClass.formatPacket();
							break;
						case 9:	//tempjail
							super.streamClass.createPacket(77);
							super.streamClass.addByte(8);
							super.streamClass.add2ByteInt(selectedPlayerIndex);
							super.streamClass.add4ByteInt(actionTime);
							super.streamClass.addString(actionReason);
							super.streamClass.formatPacket();
							break;
						case 10:	//ban
							super.streamClass.createPacket(77);
							super.streamClass.addByte(9);
							super.streamClass.add2ByteInt(selectedPlayerIndex);
							super.streamClass.addString(actionReason);
							super.streamClass.formatPacket();
					}
					selectedPlayer = "";
					actionTime = -1;
					actionReason = "";
				} else if(inputBoxLabel.equals("Minutes: ")) {
					try {
						actionTime = Integer.parseInt(s);
						inputBoxMessage = "";
						inputBoxLabel = "";
						inputBoxMessage = "Enter the reason for the action taken: ";
						inputBoxLabel = "Reason: ";
						return;
					} catch(Exception e) {
						printMessage("@red@Invalid time - please re-enter");
						return;
					}
				}
			}
			inputBoxMessage = "";
			inputBoxLabel = "";
			return;
		}
        gameGraphics.drawBox(gameWidth / 2 - 200, gameHeight / 2 - 50, 400, 100, 0);
        gameGraphics.drawBoxEdge(gameWidth / 2 - 200, gameHeight / 2 - 50, 400, 100, 0xffffff); //130
        int i = gameHeight / 2 - 30;  //30-
        drawText(inputBoxMessage, gameWidth / 2, i, 1, 0xffff00);
        i += 18;
        drawText(inputBoxLabel + super.inputText + "*", gameWidth / 2, i, 4, 0xffffff);
        i = gameHeight / 2 + 38; // 92-
        int j = 0xffffff;
        if (super.mouseX > gameWidth / 2 - 200 && super.mouseX < gameWidth / 2 + 200 && super.mouseY > i - 13 && super.mouseY < i + 2) {
            j = 0xffff00;
            if (mouseButtonClick == 1) {
                mouseButtonClick = 0;
                drawIntegerInputBox = !drawIntegerInputBox;
            }
        }
        drawText("Cancel", gameWidth / 2, i, 1, j);
        if (mouseButtonClick == 1 && (super.mouseX < gameWidth / 2 - 200 || super.mouseX > gameWidth / 2 + 200 || super.mouseY < gameHeight / 2 - 50 || super.mouseY > gameHeight / 2 + 50)) {
            mouseButtonClick = 0;
            drawIntegerInputBox = !drawIntegerInputBox;
        }
	}
	
    private final int method112(int i, int j, int k, int l, boolean flag) {
        sendWalkCommand(i, j, k, l, k, l, false, flag);
		return 117;
    }

    private final void drawInputBox() {
        if (mouseButtonClick != 0) {
            mouseButtonClick = 0;
            if (inputBoxType == 4 && super.mouseX > gameWidth / 2 - 28 && super.mouseX < gameWidth / 2 - 10 && super.mouseY > gameHeight / 2 + 24 && super.mouseY < gameHeight / 2 + 37) {
            	if (super.inputText.length() > 0) {
            		String s = super.inputText.trim();
					super.inputText = "";
					super.enteredText = "";
					super.streamClass.createPacket(24);
					super.streamClass.add2ByteInt(inputID);
					if (this.parseInt(s) > bankItemsCount[selectedBankItem])
						super.streamClass.add4ByteInt(bankItemsCount[selectedBankItem]);
					else
						super.streamClass.add4ByteInt(this.parseInt(s));
					super.streamClass.formatPacket();
					inputBoxType = 0;
    			}         
            	return;
            }
            if (inputBoxType == 5 && super.mouseX > gameWidth / 2 - 28 && super.mouseX < gameWidth / 2 - 10 && super.mouseY > gameHeight / 2 + 24 && super.mouseY < gameHeight / 2 + 37) {
    			if (super.inputText.length() > 0) {
    				String s = super.inputText.trim();
					super.inputText = "";
					super.enteredText = "";
					super.streamClass.createPacket(25);
					super.streamClass.add2ByteInt(inputID);
					if (this.parseInt(s) > inventoryCount(inputID))
						super.streamClass.add4ByteInt(inventoryCount(inputID));
					else
						super.streamClass.add4ByteInt(this.parseInt(s));
					super.streamClass.formatPacket();
					inputBoxType = 0;
    			}           	
            	return;
            }
            if (inputBoxType == 6 && super.mouseX > gameWidth / 2 - 28 && super.mouseX < gameWidth / 2 - 10 && super.mouseY > gameHeight / 2 + 8 && super.mouseY < gameHeight / 2 + 21) {
    			if (super.inputText.length() > 0) {
    				String s = super.inputText.trim();
					super.inputText = "";
					super.enteredText = "";
					
					addTradeItems(inputID, this.parseInt(s), 0, true);
					inputBoxType = 0;
    			}          	
            	return;
            }
            if (inputBoxType == 7 && super.mouseX > gameWidth / 2 - 28 && super.mouseX < gameWidth / 2 - 10 && super.mouseY > gameHeight / 2 + 8 && super.mouseY < gameHeight / 2 + 21) {
    			if (super.inputText.length() > 0) {
    				String s = super.inputText.trim();
					super.inputText = "";
					super.enteredText = "";
					removeTradeItems(inputID, this.parseInt(s), 0);
					inputBoxType = 0;
    			}         	
            	return;
            } 
            if (inputBoxType == 8 && super.mouseX > gameWidth / 2 - 28 && super.mouseX < gameWidth / 2 - 10 && super.mouseY > gameHeight / 2 + 8 && super.mouseY < gameHeight / 2 + 21) {
    			if (super.inputText.length() > 0) {
    				String s = super.inputText.trim();
					super.inputText = "";
					super.enteredText = "";
					addDuelItems(inputID, this.parseInt(s), 0, false);
					inputBoxType = 0;
    			}        	
            	return;
            } 
            if (inputBoxType == 9 && super.mouseX > gameWidth / 2 - 28 && super.mouseX < gameWidth / 2 - 10 && super.mouseY > gameHeight / 2 + 8 && super.mouseY < gameHeight / 2 + 21) {
    			if (super.inputText.length() > 0) {
    				String s = super.inputText.trim();
					super.inputText = "";
					super.enteredText = "";
					removeDuelItems(inputID, this.parseInt(s), 0);
					inputBoxType = 0;
    			}        	
            	return;
            } 
            if ((inputBoxType == 4 || inputBoxType == 5) && super.mouseX > gameWidth / 2 + 6 && super.mouseX < gameWidth / 2 + 46 && super.mouseY > gameHeight / 2 + 24 && super.mouseY < gameHeight / 2 + 37) {
            	inputBoxType = 0;
            	return;
            } 
            if ((inputBoxType == 6 || inputBoxType == 7 || inputBoxType == 8 || inputBoxType == 9) && super.mouseX > gameWidth / 2 + 6 && super.mouseX < gameWidth / 2 + 46 && super.mouseY > gameHeight / 2 + 8 && super.mouseY < gameHeight / 2 + 21) {
            	clickScreenSend = true;
            	inputBoxType = 0;
            	return;
            }
            if ((inputBoxType == 1 || inputBoxType == 2 || inputBoxType == 3) && super.mouseX > gameWidth / 2 - 20 && super.mouseX < gameWidth / 2 + 20 && super.mouseY > gameHeight / 2 + 15 && super.mouseY < gameHeight / 2 + 35) {
            	inputBoxType = 0;
                return;
            }
        }
        int i = 145;
		if (inputBoxType == 4) {
			gameGraphics.drawBox(gameWidth / 2 - 200, gameHeight / 2 - 35, 400, 78, 0);
			gameGraphics.drawBoxEdge(gameWidth / 2 - 200, gameHeight / 2 - 35, 400, 78, 0xffffff);
			drawText("Please enter the number of items to withdraw", gameWidth / 2, gameHeight / 2 - 16, 1, 0xffff00);
			drawText("and press enter", gameWidth / 2, gameHeight / 2, 1, 0xffff00);
			drawText(super.inputText + "*", gameWidth / 2, gameHeight / 2 + 20, 4, 0xffffff);
			if (super.enteredText.length() > 0) {
				String s = super.enteredText.trim();
				super.inputText = "";
				super.enteredText = "";
				super.streamClass.createPacket(24);
				super.streamClass.add2ByteInt(inputID);
				if (this.parseInt(s) > bankItemsCount[selectedBankItem])
					super.streamClass.add4ByteInt(bankItemsCount[selectedBankItem]);
				else
					super.streamClass.add4ByteInt(this.parseInt(s));				
				super.streamClass.formatPacket();
				inputBoxType = 0;
			}
	        int j = 0xffffff;
	        if (super.mouseX > gameWidth / 2 - 28 && super.mouseX < gameWidth / 2 - 10 && super.mouseY > gameHeight / 2 + 24 && super.mouseY < gameHeight / 2 + 37)
	            j = 0xffff00;
	        drawText("OK", gameWidth / 2 - 18, gameHeight / 2 + 36, 1, j);        
	        
	        int k = 0xffffff;        
	        if (super.mouseX > gameWidth / 2 + 6 && super.mouseX < gameWidth / 2 + 46 && super.mouseY > gameHeight / 2 + 24 && super.mouseY < gameHeight / 2 + 37)
	            k = 0xffff00;
	        drawText("Cancel", gameWidth / 2 + 27, gameHeight / 2 + 36, 1, k);
			return;
		}
		if (inputBoxType == 5) {
			gameGraphics.drawBox(gameWidth / 2 - 200, gameHeight / 2 - 35, 400, 78, 0);
			gameGraphics.drawBoxEdge(gameWidth / 2 - 200, gameHeight / 2 - 35, 400, 78, 0xffffff);
			drawText("Please enter the number of items to deposit", gameWidth / 2, gameHeight / 2 - 16, 1, 0xffff00);
			drawText("and press enter", gameWidth / 2, gameHeight / 2, 1, 0xffff00);
			drawText(super.inputText + "*", gameWidth / 2, gameHeight / 2 + 20, 4, 0xffffff);
			if (super.enteredText.length() > 0) {
				String s = super.enteredText.trim();
				super.inputText = "";
				super.enteredText = "";
				super.streamClass.createPacket(25);
				super.streamClass.add2ByteInt(inputID);
				if (this.parseInt(s) > inventoryCount(inputID))
					super.streamClass.add4ByteInt(inventoryCount(inputID));
				else
					super.streamClass.add4ByteInt(this.parseInt(s));
				super.streamClass.formatPacket();
				inputBoxType = 0;
			}
	        int j = 0xffffff;
	        if (super.mouseX > gameWidth / 2 - 28 && super.mouseX < gameWidth / 2 - 10 && super.mouseY > gameHeight / 2 + 24 && super.mouseY < gameHeight / 2 + 37)
	            j = 0xffff00;
	        drawText("OK", gameWidth / 2 - 18, gameHeight / 2 + 36, 1, j);
	        
	        int k = 0xffffff;
	        if (super.mouseX > gameWidth / 2 + 6 && super.mouseX < gameWidth / 2 + 46 && super.mouseY > gameHeight / 2 + 24 && super.mouseY < gameHeight / 2 + 37)
	            k = 0xffff00;
	        drawText("Cancel", gameWidth / 2 + 27, gameHeight / 2 + 36, 1, k);
			return;
		}
		if (inputBoxType == 6) {
			gameGraphics.drawBox(gameWidth / 2 - 200, gameHeight / 2 - 35, 400, 62, 0);
			gameGraphics.drawBoxEdge(gameWidth / 2 - 200, gameHeight / 2 - 35, 400, 62, 0xffffff);
			drawText("Enter number of items to offer and press enter", gameWidth / 2, gameHeight / 2 - 16, 1, 0xffff00);
						
			drawText(super.inputText + "*", gameWidth / 2, gameHeight / 2 + 4, 4, 0xffffff);
			if (super.enteredText.length() > 0) {
				String s = super.enteredText.trim();
				super.inputText = "";
				super.enteredText = "";
				addTradeItems(inputID, this.parseInt(s), 0, true);
				inputBoxType = 0;
			}
	        int j = 0xffffff;
	        if (super.mouseX > gameWidth / 2 - 28 && super.mouseX < gameWidth / 2 - 10 && super.mouseY > gameHeight / 2 + 8 && super.mouseY < gameHeight / 2 + 21)
	            j = 0xffff00;
	        drawText("OK", gameWidth / 2 - 18, gameHeight / 2 + 20, 1, j);
	        
	        int k = 0xffffff;
	        if (super.mouseX > gameWidth / 2 + 6 && super.mouseX < gameWidth / 2 + 46 && super.mouseY > gameHeight / 2 + 8 && super.mouseY < gameHeight / 2 + 21)
	            k = 0xffff00;
	        drawText("Cancel", gameWidth / 2 + 27, gameHeight / 2 + 20, 1, k);
			return;			
		}
		if (inputBoxType == 7) {
			gameGraphics.drawBox(gameWidth / 2 - 200, gameHeight / 2 - 35, 400, 62, 0);
			gameGraphics.drawBoxEdge(gameWidth / 2 - 200, gameHeight / 2 - 35, 400, 62, 0xffffff);
			drawText("Enter number of items to remove and press enter", gameWidth / 2, gameHeight / 2 - 16, 1, 0xffff00);
						
			drawText(super.inputText + "*", gameWidth / 2, gameHeight / 2 + 4, 4, 0xffffff);
			if (super.enteredText.length() > 0) {
				String s = super.enteredText.trim();
				if (containsOnlyNumbers(s)) {
					super.inputText = "";
					super.enteredText = "";
					removeTradeItems(inputID, this.parseInt(s), 0);
					inputBoxType = 0;
				}
			}
	        int j = 0xffffff;
	        if (super.mouseX > gameWidth / 2 - 28 && super.mouseX < gameWidth / 2 - 10 && super.mouseY > gameHeight / 2 + 8 && super.mouseY < gameHeight / 2 + 21)
	            j = 0xffff00;
	        drawText("OK", gameWidth / 2 - 18, gameHeight / 2 + 20, 1, j);
	        
	        int k = 0xffffff;
	        if (super.mouseX > gameWidth / 2 + 6 && super.mouseX < gameWidth / 2 + 46 && super.mouseY > gameHeight / 2 + 8 && super.mouseY < gameHeight / 2 + 21)
	            k = 0xffff00;
	        drawText("Cancel", gameWidth / 2 + 27, gameHeight / 2 + 20, 1, k);
			return;				
		}
		if (inputBoxType == 8) {
			gameGraphics.drawBox(gameWidth / 2 - 200, gameHeight / 2 - 35, 400, 62, 0);
			gameGraphics.drawBoxEdge(gameWidth / 2 - 200, gameHeight / 2 - 35, 400, 62, 0xffffff);
			drawText("Enter number of items to stake and press enter", gameWidth / 2, gameHeight / 2 - 16, 1, 0xffff00);
						
			drawText(super.inputText + "*", gameWidth / 2, gameHeight / 2 + 4, 4, 0xffffff);
			if (super.enteredText.length() > 0) {
				String s = super.enteredText.trim();
				if (containsOnlyNumbers(s)) {
					super.inputText = "";
					super.enteredText = "";
					addDuelItems(inputID, this.parseInt(s), 0, false);
					inputBoxType = 0;
				}
			}
	        int j = 0xffffff;
	        if (super.mouseX > gameWidth / 2 - 28 && super.mouseX < gameWidth / 2 - 10 && super.mouseY > gameHeight / 2 + 8 && super.mouseY < gameHeight / 2 + 21)
	            j = 0xffff00;
	        drawText("OK", gameWidth / 2 - 18, gameHeight / 2 + 20, 1, j);
	        
	        int k = 0xffffff;
	        if (super.mouseX > gameWidth / 2 + 6 && super.mouseX < gameWidth / 2 + 46 && super.mouseY > gameHeight / 2 + 8 && super.mouseY < gameHeight / 2 + 21)
	            k = 0xffff00;
	        drawText("Cancel", gameWidth / 2 + 27, gameHeight / 2 + 20, 1, k);
			return;				
		}
		if (inputBoxType == 9) {
			gameGraphics.drawBox(gameWidth / 2 - 200, gameHeight / 2 - 35, 400, 62, 0);
			gameGraphics.drawBoxEdge(gameWidth / 2 - 200, gameHeight / 2 - 35, 400, 62, 0xffffff);
			drawText("Enter number of items to remove and press enter", gameWidth / 2, gameHeight / 2 - 16, 1, 0xffff00);
						
			drawText(super.inputText + "*", gameWidth / 2, gameHeight / 2 + 4, 4, 0xffffff);
			if (super.enteredText.length() > 0) {
				String s = super.enteredText.trim();
				if (containsOnlyNumbers(s)) {
					super.inputText = "";
					super.enteredText = "";
					removeDuelItems(inputID, this.parseInt(s), 0);
					inputBoxType = 0;
				}
			}
	        int j = 0xffffff;
	        if (super.mouseX > gameWidth / 2 - 28 && super.mouseX < gameWidth / 2 - 10 && super.mouseY > gameHeight / 2 + 8 && super.mouseY < gameHeight / 2 + 21)
	            j = 0xffff00;
	        drawText("OK", gameWidth / 2 - 18, gameHeight / 2 + 20, 1, j);
	        
	        int k = 0xffffff;
	        if (super.mouseX > gameWidth / 2 + 6 && super.mouseX < gameWidth / 2 + 46 && super.mouseY > gameHeight / 2 + 8 && super.mouseY < gameHeight / 2 + 21)
	            k = 0xffff00;
	        drawText("Cancel", gameWidth / 2 + 27, gameHeight / 2 + 20, 1, k);
			return;			
		}
        if (inputBoxType == 1) {
            gameGraphics.drawBox(gameWidth / 2 - 150, gameHeight / 2 - 35, 300, 70, 0);
            gameGraphics.drawBoxEdge(gameWidth / 2 - 150, gameHeight / 2 - 35, 300, 70, 0xffffff);
            i += 20;
            drawText("Enter name to add to friends list", gameWidth / 2, gameHeight / 2 - 15, 4, 0xffffff);
            i += 20;
            drawText(super.inputText + "*", gameWidth / 2, gameHeight / 2 + 5, 4, 0xffffff);
            if (super.enteredText.length() > 0) {
                String s = super.enteredText.trim();
                super.inputText = "";
                super.enteredText = "";
                inputBoxType = 0;
                if (s.length() > 0 && DataOperations.stringLength12ToLong(s) != ourPlayer.nameLong)
                    addToFriendsList(s);
            }
        }
        if (inputBoxType == 2) {
            gameGraphics.drawBox(gameWidth / 2 - 250, gameHeight / 2 - 35, 500, 70, 0);
            gameGraphics.drawBoxEdge(gameWidth / 2 - 250, gameHeight / 2 - 35, 500, 70, 0xffffff);
            i += 20;
            drawText("Enter message to send to " + DataOperations.longToString(privateMessageTarget), gameWidth / 2 - 15, gameHeight/2-17, 4, 0xffffff);
            i += 20;
            drawText(super.inputMessage + "*", gameWidth / 2, gameHeight / 2 + 5, 4, 0xffffff);
            if (super.enteredMessage.length() > 0) {
                String s1 = super.enteredMessage;
                super.inputMessage = "";
                super.enteredMessage = "";
                inputBoxType = 0;
                byte[] message = DataConversions.stringToByteArray(s1);
                sendPrivateMessage(privateMessageTarget, message, message.length);
                s1 = DataConversions.byteToString(message, 0, message.length);
				displayGenericMessage("@cya@You tell " + DataOperations.longToString(privateMessageTarget) + ": " + ChatFilter.censorChat(s1), 6);
				logEvent("[PM] " + ourPlayer.name + ": " + ChatFilter.censorChat(s1), 3);
            }
        }
        if (inputBoxType == 3) {
            gameGraphics.drawBox(gameWidth / 2 - 150, gameHeight / 2 - 35, 300, 70, 0);
            gameGraphics.drawBoxEdge(gameWidth / 2 - 150, gameHeight / 2 - 35, 300, 70, 0xffffff);
            i += 20;
            drawText("Enter name to add to ignore list", gameWidth / 2, gameHeight / 2 - 15, 4, 0xffffff);
            i += 20;
            drawText(super.inputText + "*", gameWidth / 2, gameHeight / 2 + 5, 4, 0xffffff);
            if (super.enteredText.length() > 0) {
                String s2 = super.enteredText.trim();
                super.inputText = "";
                super.enteredText = "";
                inputBoxType = 0;
                if (s2.length() > 0 && DataOperations.stringLength12ToLong(s2) != ourPlayer.nameLong)
                    addToIgnoreList(s2);
            }
        }
        int j = 0xffffff;
        if (super.mouseX > gameWidth / 2 - 20 && super.mouseX < gameWidth / 2 + 20 && super.mouseY > gameHeight / 2 + 15 && super.mouseY < gameHeight / 2 + 35)
            j = 0xffff00;
        drawText("Cancel", gameWidth / 2, gameHeight / 2 + 25, 1, j);
    }
    
    /**
     * This method checks if a String contains only numbers
     */
    public boolean containsOnlyNumbers(String str) {
        if (str == null || str.length() == 0)
            return false;
        
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i)))
                return false;
        }
        
        return true;
    }    

	private final boolean hasRequiredRunes(int i, int j) {
		if (i == 31 && (isWielding(197) || isWielding(615) || isWielding(682))) {
			return true;
		}
		if (i == 32 && (isWielding(102) || isWielding(616) || isWielding(683))) {
			return true;
		}
		if (i == 33 && (isWielding(101) || isWielding(617) || isWielding(684))) {
			return true;
		}
		if (i == 34 && (isWielding(103) || isWielding(618) || isWielding(685))) {
			return true;
		}
		return inventoryCount(i) >= j;
	}

	private final void resetPrivateMessageStrings() {
		super.inputMessage = "";
		super.enteredMessage = "";
	}

    private final boolean isWielding(int i) {
        for (int j = 0; j < inventoryCount; j++)
            if (inventoryItems[j] == i && wearing[j] == 1)
                return true;

        return false;
    }

    private final void setPixelsAndAroundColour(int x, int y, int colour) {
        gameGraphics.setPixelColour(x, y, colour);
        gameGraphics.setPixelColour(x - 1, y, colour);
        gameGraphics.setPixelColour(x + 1, y, colour);
        gameGraphics.setPixelColour(x, y - 1, colour);
        gameGraphics.setPixelColour(x, y + 1, colour);
    }

    private final void method119() {
        for (int i = 0; i < mobMessageCount; i++) {
            int j = gameGraphics.messageFontHeight(1);
            int l = mobMessagesX[i];
            int k1 = mobMessagesY[i];
            int j2 = mobMessagesWidth[i];
            int i3 = mobMessagesHeight[i];
            boolean flag = true;
            while (flag) {
                flag = false;
                for (int i4 = 0; i4 < i; i4++)
                    if (k1 + i3 > mobMessagesY[i4] - j && k1 - j < mobMessagesY[i4] + mobMessagesHeight[i4] && l - j2 < mobMessagesX[i4] + mobMessagesWidth[i4] && l + j2 > mobMessagesX[i4] - mobMessagesWidth[i4] && mobMessagesY[i4] - j - i3 < k1) {
                        k1 = mobMessagesY[i4] - j - i3;
                        flag = true;
                    }

            }
            mobMessagesY[i] = k1;
            gameGraphics.drawBoxTextColour(mobMessages[i], l, k1, 1, 0xffff00, 300);
        }

        for (int k = 0; k < anInt699; k++) {
            int i1 = anIntArray858[k];
            int l1 = anIntArray859[k];
            int k2 = anIntArray705[k];
            int j3 = anIntArray706[k];
            int l3 = (39 * k2) / 100;
            int j4 = (27 * k2) / 100;
            int k4 = l1 - j4;
            gameGraphics.spriteClip2(i1 - l3 / 2, k4, l3, j4, SPRITE_MEDIA_START + 9, 85);
            int l4 = (36 * k2) / 100;
            int i5 = (24 * k2) / 100;
            gameGraphics.spriteClip4(i1 - l4 / 2, (k4 + j4 / 2) - i5 / 2, l4, i5, EntityHandler.getItemDef(j3).getSprite() + SPRITE_ITEM_START, EntityHandler.getItemDef(j3).getPictureMask(), 0, 0, false);
        }

        for (int j1 = 0; j1 < anInt718; j1++) {
            int i2 = anIntArray786[j1];
            int l2 = anIntArray787[j1];
            int k3 = anIntArray788[j1];
            drawBoxAlpha(i2 - 15, l2 - 3, k3, 5, 65280, 192);
            drawBoxAlpha((i2 - 15) + k3, l2 - 3, 30 - k3, 5, 0xff0000, 192);
        }

    }

    private final void drawMapMenu(boolean flag) {
        int i = ((GameImage) (gameGraphics)).menuDefaultWidth - 199;
        char c = '\234';
        char c2 = '\230';
        gameGraphics.drawPicture(i - 49, 3, SPRITE_MEDIA_START + 2);
        i += 40;
        gameGraphics.drawBox(i, 36, c, c2, 0);
        gameGraphics.setDimensions(i, 36, i + c, 36 + c2);
        int k = 192 + anInt986;
        int i1 = cameraRotation + anInt985 & 0xff;
        int k1 = ((ourPlayer.currentX - 6040) * 3 * k) / 2048;
        int i3 = ((ourPlayer.currentY - 6040) * 3 * k) / 2048;
        int k4 = Camera.anIntArray384[1024 - i1 * 4 & 0x3ff];
        int i5 = Camera.anIntArray384[(1024 - i1 * 4 & 0x3ff) + 1024];
        int k5 = i3 * k4 + k1 * i5 >> 18;
        i3 = i3 * i5 - k1 * k4 >> 18;
        k1 = k5;
        gameGraphics.method242((i + c / 2) - k1, 36 + c2 / 2 + i3, SPRITE_MEDIA_START - 1, i1 + 64 & 0xff, k);
        for (int i7 = 0; i7 < objectCount; i7++) {
            int l1 = (((objectX[i7] * 128 + 64) - ourPlayer.currentX) * 3 * k) / 2048;
            int j3 = (((objectY[i7] * 128 + 64) - ourPlayer.currentY) * 3 * k) / 2048;
            int l5 = j3 * k4 + l1 * i5 >> 18;
            j3 = j3 * i5 - l1 * k4 >> 18;
            l1 = l5;
            setPixelsAndAroundColour(i + c / 2 + l1, (36 + c2 / 2) - j3, 65535);
        }

        for (int j7 = 0; j7 < groundItemCount; j7++) {
            int i2 = (((groundItemX[j7] * 128 + 64) - ourPlayer.currentX) * 3 * k) / 2048;
            int k3 = (((groundItemY[j7] * 128 + 64) - ourPlayer.currentY) * 3 * k) / 2048;
            int i6 = k3 * k4 + i2 * i5 >> 18;
            k3 = k3 * i5 - i2 * k4 >> 18;
            i2 = i6;
            setPixelsAndAroundColour(i + c / 2 + i2, (36 + c2 / 2) - k3, 0xff0000);
        }

        for (int k7 = 0; k7 < npcCount; k7++) {
            Mob mob = npcArray[k7];
            int j2 = ((mob.currentX - ourPlayer.currentX) * 3 * k) / 2048;
            int l3 = ((mob.currentY - ourPlayer.currentY) * 3 * k) / 2048;
            int j6 = l3 * k4 + j2 * i5 >> 18;
            l3 = l3 * i5 - j2 * k4 >> 18;
            j2 = j6;
            setPixelsAndAroundColour(i + c / 2 + j2, (36 + c2 / 2) - l3, 0xffff00);
        }

        for (int l7 = 0; l7 < playerCount; l7++) {
            Mob mob_1 = playerArray[l7];
		if(mob_1.level > 0)
{
            int k2 = ((mob_1.currentX - ourPlayer.currentX) * 3 * k) / 2048;
            int i4 = ((mob_1.currentY - ourPlayer.currentY) * 3 * k) / 2048;
            int k6 = i4 * k4 + k2 * i5 >> 18;
            i4 = i4 * i5 - k2 * k4 >> 18;
            k2 = k6;
            int j8 = 0xffffff;
            for (int k8 = 0; k8 < super.friendsCount; k8++) {
                if (mob_1.nameLong != super.friendsListLongs[k8] || super.friendsListOnlineStatus[k8] != 1)
					continue;
                j8 = 65280; //Green
                break;
            }
			for (int idx = 0; idx < super.clanCount; idx++) {
				if(mob_1.nameLong != super.clanListLongs[idx] || super.clanListOnlineStatus[idx] != 1) {
					continue;
				}
				j8 = new java.util.Random().nextInt(100000);
				//j8 = 0x1B3BA6; //Blue
			}
            setPixelsAndAroundColour(i + c / 2 + k2, (36 + c2 / 2) - i4, j8);
        }
}

        gameGraphics.method212(i + c / 2, 36 + c2 / 2, 2, 0xffffff, 255);
        gameGraphics.method242(i + 19, 55, SPRITE_MEDIA_START + 24, cameraRotation + 128 & 0xff, 128);
        gameGraphics.setDimensions(0, 0, windowWidth, windowHeight + 12);
        if (!flag)
            return;
        i = super.mouseX - (((GameImage) (gameGraphics)).menuDefaultWidth - 199);
        int i8 = super.mouseY - 36;
        if (i >= 40 && i8 >= 0 && i < 196 && i8 < 152) {
            char c1 = '\234';
            char c3 = '\230';
            int l = 192 + anInt986;
            int j1 = cameraRotation + anInt985 & 0xff;
            int j = ((GameImage) (gameGraphics)).menuDefaultWidth - 199;
            j += 40;
            int l2 = ((super.mouseX - (j + c1 / 2)) * 16384) / (3 * l);
            int j4 = ((super.mouseY - (36 + c3 / 2)) * 16384) / (3 * l);
            int l4 = Camera.anIntArray384[1024 - j1 * 4 & 0x3ff];
            int j5 = Camera.anIntArray384[(1024 - j1 * 4 & 0x3ff) + 1024];
            int l6 = j4 * l4 + l2 * j5 >> 15;
            j4 = j4 * j5 - l2 * l4 >> 15;
            l2 = l6;
            l2 += ourPlayer.currentX;
            j4 = ourPlayer.currentY - j4;
            if (mouseButtonClick == 1)
                method112(sectionX, sectionY, l2 / 128, j4 / 128, false);
            mouseButtonClick = 0;
        }
    }
	
    public mudclient(int clientWidth, int clientHeight) {
		notifications = new String[3];
		notificationsTimeout = new int[3];
    	fightmode = 1;
        threadSleepTime = 10;
        try{localhost = InetAddress.getLocalHost().getHostAddress();}catch(Exception e) {localhost = "unknown";}
        startTime = System.currentTimeMillis();
        duelMyItems = new int[8];
        duelMyItemsCount = new int[8];
        cameraRotate = true;
        questionMenuAnswer = new String[10];
        lastNpcArray = new Mob[2000];
        currentUser = "";
        currentPass = "";
        menuText1 = new String[250];
        duelOpponentAccepted = false;
        duelMyAccepted = false;
        tradeConfirmItems = new int[14];
        tradeConfirmItemsCount = new int[14];
        tradeConfirmOtherItems = new int[14];
        tradeConfirmOtherItemsCount = new int[14];
        serverMessage = "";
        duelOpponentName = "";
        inventoryItems = new int[35];
        inventoryItemsCount = new int[35];
        wearing = new int[35];
        mobMessages = new String[500];
        showBank = false;
        doorModel = new Model[500];
        mobMessagesX = new int[500];
        mobMessagesY = new int[500];
        mobMessagesWidth = new int[500];
        mobMessagesHeight = new int[500];
        npcArray = new Mob[2000];
        equipmentStatus = new int[6];
        prayerOn = new boolean[50];
        tradeOtherAccepted = false;
        tradeWeAccepted = false;
        mobArray = new Mob[8000];
        anIntArray705 = new int[50];
        anIntArray706 = new int[50];
        lastWildYSubtract = -1;
        memoryError = false;
        bankItemsMax = 48;
        showQuestionMenu = false;
        cameraAutoAngle = 1;
        anInt727 = 2;
        showServerMessageBox = false;
        hasReceivedWelcomeBoxDetails = false;
        playerStatCurrent = new int[19];
        wildYSubtract = -1;
        anInt742 = -1;
        anInt743 = -1;
        anInt744 = -1;
        sectionXArray = new int[8000];
        sectionYArray = new int[8000];
        selectedItem = -1;
        selectedItemName = "";
        duelOpponentItems = new int[8];
        duelOpponentItemsCount = new int[8];
        anIntArray757 = new int[50];
        menuID = new int[250];
        showCharacterLookScreen = false;
        lastPlayerArray = new Mob[500];
        appletMode = true;
        gameDataModels = new Model[1000];
        configMouseButtons = false;
        duelNoRetreating = false;
        duelNoMagic = false;
        duelNoPrayer = false;
        duelNoWeapons = false;
        anIntArray782 = new int[50];
        duelConfirmOpponentItems = new int[8];
        duelConfirmOpponentItemsCount = new int[8];
        anIntArray786 = new int[500];
        anIntArray787 = new int[500];
        anIntArray788 = new int[500];
        objectModelArray = new Model[1500];
        cameraRotation = 128;
        showWelcomeBox = false;
        characterBodyGender = 1;
        character2Colour = 2;
        characterHairColour = 2;
        characterTopColour = 8;
        characterBottomColour = 14;
        characterHeadGender = 1;
        selectedBankItem = -1;
        selectedBankItemType = -2;
        menuText2 = new String[250];
        aBooleanArray827 = new boolean[1500];
        playerStatBase = new int[19];
        menuActionType = new int[250];
        menuActionVariable = new int[250];
        menuActionVariable2 = new int[250];
        shopItems = new int[256];
        shopItemCount = new int[256];
        anIntArray858 = new int[50];
        anIntArray859 = new int[50];
        newBankItems = new int[256];
        newBankItemsCount = new int[256];
        duelConfirmMyItems = new int[8];
        duelConfirmMyItemsCount = new int[8];
        mobArrayIndexes = new int[500];
        messagesTimeout = new int[5];
        objectX = new int[1500];
        objectY = new int[1500];
        objectType = new int[1500];
        objectID = new int[1500];
        menuActionX = new int[250];
        menuActionY = new int[250];
        ourPlayer = new Mob();
        serverIndex = -1;
        anInt882 = 30;
        showTradeConfirmWindow = false;
        tradeConfirmAccepted = false;
        playerArray = new Mob[500];
        serverMessageBoxTop = false;
        cameraHeight = 750;
        bankItems = new int[256];
        bankItemsCount = new int[256];
        notInWilderness = false;
        selectedSpell = -1;
        anInt911 = 2;
        tradeOtherItems = new int[14];
        tradeOtherItemsCount = new int[14];
        menuIndexes = new int[250];
        zoomCamera = false;
        playerStatExperience = new int[19];
        cameraAutoAngleDebug = false;
        npcRecordArray = new Mob[40000];
        showDuelWindow = false;
        anIntArray923 = new int[50];
        lastLoadedNull = false;
        experienceArray = new int[99];
        showShop = false;
        mouseClickXArray = new int[8192];
        mouseClickYArray = new int[8192];
        showDuelConfirmWindow = false;
        duelWeAccept = false;
        doorX = new int[500];
        doorY = new int[500];
        configSoundEffects = false;
        showRightClickMenu = false;
        attackingInt40 = 40;
        YKVE_mudclient_LQSP_2 = new int[50];
        doorDirection = new int[500];
        doorType = new int[500];
        groundItemX = new int[8000];
        groundItemY = new int[8000];
        groundItemType = new int[8000];
        groundItemObjectVar = new int[8000];
        selectedShopItemIndex = -1;
        selectedShopItemType = -2;
        messagesArray = new String[5];
        showTradeWindow = false;
        aBooleanArray970 = new boolean[500];
        tradeMyItems = new int[14];
        tradeMyItemsCount = new int[14];
        cameraSizeInt = 9;
        tradeOtherPlayerName = "";
		gameWidth = clientWidth;
		gameHeight = clientHeight;
		sampler = true;
		lastPing = System.currentTimeMillis();
		pinging = false;
		windowWidth = gameWidth;
		windowHeight = gameHeight;
		killNotify = false;
		strings = new Strings();
		gW = clientWidth;
		gH = clientHeight;
	}
	
	private int questPoints = 0;
	private String[] notifications;
	private int[] notificationsTimeout;
    private boolean pinging;
    public int lastWalkTimeout, handlePacketErrorCount, anInt658, duelMyItemCount, fightmode, lastLoggedInDays, subscriptionLeftDays, newUserOkButton, loginButtonNewUser;
    private int duelMyItems[];
    private int duelMyItemsCount[];
    private boolean duelMyAccepted, duelOpponentAccepted, cameraRotate;
    private String questionMenuAnswer[];
    private Mob lastNpcArray[];
    private int loginButtonExistingUser;
    private String currentUser, currentPass;
    private String menuText1[];
    private int tradeConfirmItemCountxxx;
    private int tradeConfirmItems[];
    private int tradeConfirmItemsCount[];
    private int tradeConfirmOtherItemCount;
    private int tradeConfirmOtherItems[];
    private int tradeConfirmOtherItemsCount[];
    private String serverMessage;
    private String duelOpponentName;
    private int mouseOverBankPageText;
    private int playerCount;
    private int lastPlayerCount;
    private int fightCount;
    private int inventoryCount;
    private int inventoryItems[];
    private int inventoryItemsCount[];
    private int wearing[];
    private int mobMessageCount;
    String mobMessages[];
    private boolean showBank;
    private Model doorModel[];
    private int mobMessagesX[];
    private int mobMessagesY[];
    private int mobMessagesWidth[];
    private int mobMessagesHeight[];
    private Mob npcArray[];
    private int equipmentStatus[];
    private final int characterTopBottomColours[] = {0xff0000, 0xff8000, 0xffe000, 0xa0e000, 57344, 32768, 41088, 45311, 33023, 12528, 0xe000e0, 0x303030, 0x604000, 0x805000, 0xffffff};
    private int loginScreenNumber;
    private int anInt699;
    private boolean prayerOn[];
    private boolean tradeOtherAccepted;
    private boolean tradeWeAccepted;
    private Mob mobArray[];
    private int npcCombatModelArray1[] = {0, 1, 2, 1, 0, 0, 0, 0};
    private int anIntArray705[];
    private int anIntArray706[];
    private int npcCount;
    private int lastNpcCount;
    private int wildX;
    private int wildY;
    private int wildYMultiplier;
    private int lastWildYSubtract;
    private boolean memoryError;
    private int bankItemsMax;
    private int mouseOverMenu;
    private int walkModel[] = {0, 1, 2, 1};
    private boolean showQuestionMenu;
    private int anInt718;
    public int loggedIn;
    private int cameraAutoAngle;
    private int cameraRotationBaseAddition;
    private Menu spellMenu;
    int spellMenuHandle;
    private Menu questMenu;
    int questMenuHandle;
    int menuMagicPrayersSelected;
    private int screenRotationX;
    private int anInt727;
    private int showAbuseWindow;
    private int showCommandsWindow;
    private int duelCantRetreat;
    private int duelUseMagic;
    private int duelUsePrayer;
    private int duelUseWeapons;
    private boolean showServerMessageBox;
    private boolean hasReceivedWelcomeBoxDetails;
    private String lastLoggedInAddress;
    private String lastLoggedInHostname;
    private int loginTimer;
    private int playerStatCurrent[];
    private int areaX;
    private int areaY;
    private int wildYSubtract;
    private int anInt742;
    private int anInt743;
    private int anInt744;
    private int sectionXArray[];
    private int sectionYArray[];
    private int selectedItem;
    String selectedItemName;
    private int menuX;
    private int menuY;
    private int menuWidth;
    private int menuHeight;
    private int menuLength;
    private int duelOpponentItemCount;
    private int duelOpponentItems[];
    private int duelOpponentItemsCount[];
    private int anIntArray757[];
    private int menuID[];
    private boolean showCharacterLookScreen;
    private int newBankItemCount;
    private int npcCombatModelArray2[] = {0, 0, 0, 0, 0, 1, 2, 1};
    private Mob lastPlayerArray[];
    static int inputBoxType;
    public boolean appletMode;
    private int combatStyle;
    private Model gameDataModels[];
    private boolean configMouseButtons;
    private boolean duelNoRetreating;
    private boolean duelNoMagic;
    private boolean duelNoPrayer;
    private boolean duelNoWeapons;
    private int anIntArray782[];
    private int duelConfirmOpponentItemCount;
    private int duelConfirmOpponentItems[];
    private int duelConfirmOpponentItemsCount[];
    private int anIntArray786[];
    private int anIntArray787[];
    private int anIntArray788[];
    private int anInt789;
    private int anInt790;
    private int anInt791;
    private int anInt792;
    private Menu menuLogin;
    private final int characterHairColours[] = {0xffc030, 0xffa040, 0x805030, 0x604020, 0x303030, 0xff6020, 0xff4000, 0xffffff, 65280, 65535};
    private Model objectModelArray[];
    private Menu menuWelcome;
    private int systemUpdate;
	private int wildernessUpdate;
    private int cameraRotation;
    private int logoutTimeout;
    private Menu gameMenu;
    int messagesHandleType2;
    int chatHandle;
    int messagesHandleType5;
    int messagesHandleType6;
    int messagesTab;
    private boolean showWelcomeBox;
    private int characterHeadType;
    private int characterBodyGender;
    private int character2Colour;
    private int characterHairColour;
    private int characterTopColour;
    private int characterBottomColour;
    private int characterSkinColour;
    private int characterHeadGender;
    private int loginStatusText;
    private int loginUsernameTextBox;
    private int loginPasswordTextBox;
    private int loginOkButton;
    private int loginCancelButton;
    private int selectedBankItem;
    private int selectedBankItemType;
    private String menuText2[];
    int infoPage;
	public boolean aBoolean948;
    private boolean aBooleanArray827[];
    private int playerStatBase[];
    private int abuseSelectedType;
    private int actionPictureType;
    int actionPictureX;
    int actionPictureY;
    private int menuActionType[];
    private int menuActionVariable[];
    private int menuActionVariable2[];
    private int shopItems[];
    private int shopItemCount[];
    private int npcAnimationArray[][] = {
            {11, 2, 9, 7, 1, 6, 10, 0, 5, 8, 3, 4},
            {11, 2, 9, 7, 1, 6, 10, 0, 5, 8, 3, 4},
            {11, 3, 2, 9, 7, 1, 6, 10, 0, 5, 8, 4},
            {3, 4, 2, 9, 7, 1, 6, 10, 8, 11, 0, 5},
            {3, 4, 2, 9, 7, 1, 6, 10, 8, 11, 0, 5},
            {4, 3, 2, 9, 7, 1, 6, 10, 8, 11, 0, 5},
            {11, 4, 2, 9, 7, 1, 6, 10, 0, 5, 8, 3},
            {11, 2, 9, 7, 1, 6, 10, 0, 5, 8, 4, 3}
    };
    private int bankItemCount;
    private int characterDesignHeadButton1;
    private int characterDesignHeadButton2;
    private int characterDesignHairColourButton1;
    private int characterDesignHairColourButton2;
    private int characterDesignGenderButton1;
    private int characterDesignGenderButton2;
    private int characterDesignTopColourButton1;
    private int characterDesignTopColourButton2;
    private int characterDesignSkinColourButton1;
    private int characterDesignSkinColourButton2;
    private int characterDesignBottomColourButton1;
    private int characterDesignBottomColourButton2;
    private int characterDesignAcceptButton;
    private int anIntArray858[];
    private int anIntArray859[];
    private int newBankItems[];
    private int newBankItemsCount[];
    private int duelConfirmMyItemCount;
    private int duelConfirmMyItems[];
    private int duelConfirmMyItemsCount[];
    private int mobArrayIndexes[];
    private Menu menuNewUser;
    private int messagesTimeout[];
    private int lastAutoCameraRotatePlayerX;
    private int lastAutoCameraRotatePlayerY;
    private int questionMenuCount;
    private int objectX[];
    private int objectY[];
    private int objectType[];
    private int objectID[];
    private int menuActionX[];
    private int menuActionY[];
    private Mob ourPlayer;
    int sectionX;
    int sectionY;
    int serverIndex;
    private int anInt882;
    private int mouseDownTime;
    private int itemIncrement;
    private int groundItemCount;
    private int modelFireLightningSpellNumber;
    private int modelTorchNumber;
    private int modelClawSpellNumber;
    private boolean showTradeConfirmWindow;
    private boolean tradeConfirmAccepted;
    private int teleportBubbleCount;
    private EngineHandle engineHandle;
    private Mob playerArray[];
    private boolean serverMessageBoxTop;
    private final String equipmentStatusName[] = {"Armour", "WeaponAim", "WeaponPower", "Magic", "Prayer", "Ranged"};
    private int anInt900;
    private int mouseButtonClick;
    private int cameraHeight;
    private int bankItems[];
    private int bankItemsCount[];
    private boolean notInWilderness;
    private int selectedSpell;
    private int screenRotationY;
    private int anInt911;
    private int tradeOtherItemCount;
    private int tradeOtherItems[];
    private int tradeOtherItemsCount[];
    private int menuIndexes[];
    private boolean zoomCamera;
    private AudioReader audioReader;
    private int playerStatExperience[];
    private boolean cameraAutoAngleDebug;
    private Mob npcRecordArray[];
    private final String skillArray[] = {"Attack", "Defense", "Strength", "Hits", "Ranged", "Prayer", "Magic", "Cooking", "Woodcut", "Fletching", "Fishing", "Firemaking", "Crafting", "Smithing", "Mining", "Herblaw", "Agility", "Thieving", "Runecrafting"};
    private boolean showDuelWindow;
    private int anIntArray923[];
    private GameImageMiddleMan gameGraphics;
    private final String skillArrayLong[] = {"Attack", "Defense", "Strength", "Hits", "Ranged", "Prayer", "Magic", "Cooking", "Woodcutting", "Fletching", "Fishing", "Firemaking", "Crafting", "Smithing", "Mining", "Herblaw", "Agility", "Thieving", "Runecrafting"};
    private boolean lastLoadedNull;
    private int experienceArray[];
    private Camera gameCamera;
    private boolean showShop;
    private int mouseClickArrayOffset;
    int mouseClickXArray[];
    int mouseClickYArray[];
    private boolean showDuelConfirmWindow;
    private boolean duelWeAccept;
    private Graphics aGraphics936;
    private int doorX[];
    private int doorY[];
    private int wildernessType;
    private boolean configSoundEffects;
    private boolean showRightClickMenu;
    private int screenRotationTimer;
    private int attackingInt40;
	public long attackingInt44 = System.currentTimeMillis();
    private int YKVE_mudclient_LQSP_2[];
    private Menu characterDesignMenu;
    private int shopItemSellPriceModifier;
    private int shopItemBuyPriceModifier;
    private int modelUpdatingTimer;
    private int doorCount;
    private int doorDirection[];
    private int doorType[];
    private int anInt952;
    private int anInt953;
    private int anInt954;
    private int anInt955;
    private int groundItemX[];
	private int kfi;
    private int groundItemY[];
    private int groundItemType[];
    private int groundItemObjectVar[];
    private int selectedShopItemIndex;
    private int selectedShopItemType;
    private String messagesArray[];
    private long tradeConfirmOtherNameLong;
    private boolean showTradeWindow;
    private int playerAliveTimeout;
    private byte[] raf = {51,50,56,49};
    private final int characterSkinColours[] = {0xecded0, 0xccb366, 0xb38c40, 0x997326, 0x906020};
    private byte sounds[];
    private boolean aBooleanArray970[];
    private int objectCount;
    private int tradeMyItemCount;
    private int tradeMyItems[];
    private int tradeMyItemsCount[];
    private static int windowWidth;
    private static int windowHeight;
    private int cameraSizeInt;
    private Menu friendsMenu;
    int friendsMenuHandle;
    int anInt981;
    long privateMessageTarget;
    private long duelOpponentNameLong;
    private String tradeOtherPlayerName;
    private int anInt985;
    private int anInt986;
	private BufferedImage sleepy;
	private Sprite sleepSprite;
	private String[] quests = {"@whi@Quest-list (green=completed)",
	"@red@Black knight's fortress",
	"@red@Cook's assistant",
	"@red@Demon slayer",
	"@red@Doric's quest",
	"@red@The restless ghost",
	"@red@Goblin diplomacy",
	"@red@Ernest the chicken",
	"@red@Imp catcher",
	"@red@Pirate's treasure",
	"@red@Prince Ali rescue",
	"@red@Romeo & Juliet",
	"@red@Sheep shearer",
	"@red@Shield of Arrav",
	"@red@The knight's sword",
	"@red@Vampire slayer",
	"@red@Witch's potion",
	"@red@Dragon slayer",

	"@red@Witch's house (members)",
	"@red@Lost city (members)",
	"@red@Hero's quest (members)",
	"@red@Druidic ritual (members)",
	"@red@Merlin's crystal (members)",
	"@red@Scorpion catcher (members)",
	"@red@Family crest (members)",
	"@red@Tribal totem (members)",
	"@red@Fishing contest (members)",
	"@red@Monk's friend (members)",
	"@red@Temple of Ivok (members)",
	"@red@Clock tower (members)",
	"@red@The Holy Grail (members)",
	"@red@Fight Arena (members)",
	"@red@Tree Gnome Village (members)",
	"@red@The Hazeel Cult (members)",
	"@red@Sheep Herder (members)",
	"@red@Plague City (members)",
	"@red@Sea Slug (members)",
	"@red@Waterfall quest (members)",
	"@red@Biohazard (members)",
	"@red@Jungle potion (members)",
	"@red@Grand tree (members)",
	"@red@Shilo village (members)",
	"@red@Underground pass (members)",
	"@red@Observatory quest (members)",
	"@red@Tourist trap (members)",
	"@red@Watchtower (members)",
	"@red@Dwarf Cannon (members)",
	"@red@Murder Mystery (members)",
	"@red@Digsite (members)",
	"@red@Gertrude's Cat (members)",
	"@red@Legend's Quest (members)"};
	private static Sprite logo;
	private long then;
	private boolean sampler;
	private static int FPS = 0;
	int framesPerSecond;
	public static BufferedImage rez;
	private long lastTradeDuelUpdate = System.currentTimeMillis();
	private byte wildernessSwitchType;
	private long lastPing;
	private boolean killNotify;
	private long startTime = 0;
	public static ArrayList<String> messages = new ArrayList<String>();
	public static int currentChat = 0;
	private long serverStartTime = 0;
	private String lastMessage = "";
	private long fatigue;
	private int playersOnline;
	private int ownerID;
	private int kills;
	private int deaths;
	private String localhost;
	private int prayerMenuIndex = 0;
	private int LCYV_mudclient_SHKE_7 = 0;
	private static boolean hideCeilings = false;
	private static boolean autoScreenshots = true;
	private static boolean fog = true;
	public static boolean chatFilter = true;
	private long expGained = 0;
	private boolean hasWorldInfo = false;
	private boolean recording = false;
	public boolean SIDE_MENU = false;
	public boolean ignoreNext;
    public boolean sleeping;
    public String kfr = "";
	public int hjk;
	public static int gameWidth, gameHeight;
	private LinkedList<BufferedImage> frames = new LinkedList<BufferedImage>();
	private long fpsLimiter = 0; 
	private Random rand = new Random();
	private Strings strings;
	private boolean acceptKeyboardInput = false;
	private boolean drawStaffMenu = false;
	private String selectedPlayer = "";
	private int actionTime = -1;
	private boolean drawIntegerInputBox = false;
	private String actionReason = "";
	private String inputBoxMessage = "";
	private String inputBoxLabel = "";
	private int staffMenuSelection = 0;
	private int selectedPlayerIndex = -1;
	private boolean showLoot = true;
	private boolean clanChannel = false;
		
	public static String RSCE_DIR = System.getProperty("user.home") + System.getProperty("file.separator") + "RSCEmulation" + System.getProperty("file.separator");
	public static String DATA_DIR = RSCE_DIR + System.getProperty("file.separator") + "data" + System.getProperty("file.separator");
	public static String MEDIA_DIR = RSCE_DIR + System.getProperty("file.separator") + "media" + System.getProperty("file.separator");
	public static String LOG_DIR = RSCE_DIR + System.getProperty("file.separator") + "logs" + System.getProperty("file.separator");
	public static String SOUND_DIR = DATA_DIR + System.getProperty("file.separator") + "sfx" + System.getProperty("file.separator");
	public static String SITE_URL = "http://www.rscemulation.net";
	
	private boolean showDevelopersInputBox = false, drawSearchBox = false;
	private String searchType = "";
	private String[] searchResults = new String[] {""};
	private Object[] matches;
	private String developerMessage, developerLabel;
	private int devX, devY, entityID, entityType;
	private int minX, maxX, minY, maxY;
	private String VCS_IP;
	private int VCS_PORT;
	private ByteArrayOutputStream outStream;
	private boolean sfxr;

	public static boolean loggingEnabled = false;	
	public static boolean ADVANCED_MENU = false;
	public static String logFile = "";
	public static String logName = "";
	public static int logType = 0;
	
	public static int MOVIE_FPS = 30;
	private int tradeWindowX;
	private int tradeWindowY;
	private int inputID;
}