package org.rscemulation.client;

import java.applet.Applet;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import org.rscemulation.loader.AppletLoader;

public class GameWindow extends Applet implements Runnable {
	public static final Color BAR_COLOUR = new Color(132, 132, 132);
	public static final Font fontTimesRoman = new Font("TimesRoman", 0, 15);
	public static final Font fontHelvetica1 = new Font("Helvetica", 1, 13);
	public static final Font fontHelvetica0 = new Font("Helvetica", 0, 12);
	private boolean actionDown;
	private boolean shiftDown;

	private final void drawLoadingLogo() {
		int j = (appletWidth - 281) / 2;
		int k = (appletHeight - 148) / 2;
		loadingGraphics.setColor(Color.black);
		loadingGraphics.drawImage(loadingLogo, j, k, this);
		loadFonts();
	}

	private final void drawLoadingScreen(int i, String s) {
		try {
			int j = (appletWidth - 281) / 2;
			int k = (appletHeight - 148) / 2;
			loadingGraphics.setColor(Color.black);
			loadingGraphics.fillRect(0, 0, appletWidth, appletHeight);
			loadingGraphics.drawImage(loadingLogo, j, k, this);
			j += 2;
			k += 90;
			anInt16 = i;
			loadingBarText = s;
			loadingGraphics.setColor(new Color(132, 132, 132));
			loadingGraphics.drawRect(j - 2, k - 2, 280, 23);
			loadingGraphics.fillRect(j, k, (277 * i) / 100, 20);
			loadingGraphics.setColor(new Color(198, 198, 198));
			drawString(loadingGraphics, s, fontTimesRoman, j + 138, k + 10);
			drawString(loadingGraphics, "Created by JAGeX - visit www.jagex.com", fontHelvetica1, j + 138, k + 30);
			drawString(loadingGraphics, "\251 2001-2009 Jagex Ltd", fontHelvetica1, j + 138, k + 44);
			if (loadingString != null) {
				loadingGraphics.setColor(Color.white);
				drawString(loadingGraphics, loadingString, fontHelvetica1, j + 138, k - 120);
				return;
			}
		} catch (Exception _ex) {}
	}

	public void handleScroll(int x) {

	}

	protected final void drawLoadingBarText(int i, String s) {
		try {
			int j = (appletWidth - 281) / 2;
			int k = (appletHeight - 148) / 2;
			j += 2;
			k += 90;
			anInt16 = i;
			loadingBarText = s;
			int l = (277 * i) / 100;
			loadingGraphics.setColor(BAR_COLOUR);
			loadingGraphics.fillRect(j, k, l, 20);
			loadingGraphics.setColor(Color.black);
			loadingGraphics.fillRect(j + l, k, 277 - l, 20);
			loadingGraphics.setColor(new Color(198, 198, 198));
			drawString(loadingGraphics, s, fontTimesRoman, j + 138, k + 10);
			return;
		} catch (Exception _ex) {
			return;
		}
	}

	protected void startGame() {
	}

	protected synchronized void method2() {
	}

	protected void logoutAndStop() {
	}

	protected synchronized void loadingError() {
	}

	protected final void createWindow(int width, int height, String title,
			boolean resizable) {
		appletMode = true;
		appletWidth = width;
		appletHeight = height;
		gameFrame = new GameFrame(this, width, height, title, resizable, false);
		loadingScreen = 1;
		gameWindowThread = new Thread(this);
		gameWindowThread.start();
		gameWindowThread.setPriority(1);
	}

	public void setLogo(Image logo) {
		loadingLogo = logo;
	}

	protected final void changeThreadSleepModifier(int i) {
		threadSleepModifier = 1000 / i;
	}

	protected final void resetCurrentTimeArray() {
		for (int i = 0; i < 10; i++)
			currentTimeArray[i] = 0L;
	}

	public final synchronized boolean keyDown(boolean shift, boolean ctrl,
			boolean action, int key, char keyChar, KeyEvent e) {
		actionDown = action;
		shiftDown = shift;
		controlDown = ctrl;
		keyDown = key;
		keyDown2 = key;

		handleMenuKeyDown(shift, ctrl, action, key, keyChar);

		lastActionTimeout = 0;
		if (controlDown && key == 86) {
			return true;
		}
		
		if (key == 37)
			keyLeftDown = true;
		if (key == 39)
			keyRightDown = true;
		if (key == 38)
			keyUpDown = true;
		if (key == 40)
			keyDownDown = true;
		if ((char) key == ' ')
			keySpaceDown = true;
		if ((char) key == 'n' || (char) key == 'm')
			keyNMDown = true;
		if ((char) key == 'N' || (char) key == 'M')
			keyNMDown = true;
		if ((char) key == '{')
			keyLeftBraceDown = true;
		if ((char) key == '}')
			keyRightBraceDown = true;
		if (key == 112) // F1
			keyF1Toggle = !keyF1Toggle;
		if (actionDown)
			return true;
		if (actionDown && shiftDown)
			return true;
		if(controlDown) // Add other ctrl + w/e above this
			return true;
		boolean validKeyDown = isKeyValid(key);
		if (key == 8 && inputText.length() > 0) // backspace
			inputText = inputText.substring(0, inputText.length() - 1);
		if (key == 8 && inputMessage.length() > 0) // backspace
			inputMessage = inputMessage.substring(0, inputMessage.length() - 1);
		if (key == 10 || key == 13) { // enter/return
			enteredText = inputText;
			enteredMessage = inputMessage;
		}
		if (mudclient.inputBoxType > 3 && mudclient.inputBoxType < 10) {
			if (!Character.isDigit(keyChar))
				return false;
			if (inputText.length() > 9)
				return false;
			inputText += keyChar;
			if (inputText.length() == 10)
				parseInt(inputText);
			return true;
		}
		if(key == 222 && !e.isShiftDown()) 
			inputText += "'";
		if(key == 222 && e.isShiftDown()) 
			inputText += "@";
		if (validKeyDown && inputText.length() < 20)
			inputText += keyChar;
		if (validKeyDown && inputMessage.length() < 80)
			inputMessage += keyChar;
		if (searching)
			updateSearch();
		return true;
	}
	private long start;
	public int parseIntNewBuild(final String s) {
		// Check for a sign.
		int num = 0;
		int sign = -1;
		final int len = s.length();
		final char ch = s.charAt(0);
		if (ch == '-')
			sign = 1;
		else
			num = '0' - ch;

		// Build the number.
		int i = 1;
		while (i < len)
			num = num * 10 + '0' - s.charAt(i++);
		return sign * num;
	}
	
	/**
	 * 
	 * @param s
	 *            input of the string to get the int value ID
	 * @return Returns the value ID
	 */
	public int parseInt(final String s) {
		/**
		 * Make sure that the string is not null before we carry on this god
		 * damn stupid test
		 */
		if (s == null)
			throw new NumberFormatException("Null string");
		/**
		 * Checks for a sign
		 */
		int num = 0;
		int sign = -1;
		final int len = s.length();
		final char ch = s.charAt(0);
		if (ch == '-') {
			if (len == 1)
				throw new NumberFormatException("Missing digits:  " + s);
			sign = 1;
		} else {
			final int d = ch - '0';
			if (d < 0 || d > 9)
				throw new NumberFormatException("Malformed:  " + s);
			num = -d;
		}
		/**
		 * Starts Building the number
		 */
		final int max = (sign == -1) ? -Integer.MAX_VALUE : Integer.MIN_VALUE;
		final int multmax = max / 10;
		int i = 1;
		while (i < len) {
			int d = s.charAt(i++) - '0';
			if (d < 0 || d > 9)
				throw new NumberFormatException("Malformed:  " + s);
			if (num < multmax)
				/**
				 * So we do not go over the max int value we will return the max
				 * int :( sad face
				 */
				return Integer.MAX_VALUE;
			num *= 10;
			if (num < (max + d))
				throw new NumberFormatException("Over/underflow:  " + s);
			num -= d;
		}
		return sign * num;
	}
	
	public static boolean isKeyValid(int key) {
		boolean validKeyDown = false;
		for (int j = 0; j < charSet.length(); j++) {
			if (key != charSet.charAt(j))
				continue;
			validKeyDown = true;
			break;
		}
		return validKeyDown;
	}

	protected void updateSearch() {
	}

	protected void handleMenuKeyDown(boolean shift, boolean ctrl, boolean action, int key, char keyChar) {
	}

	public final synchronized boolean keyUp(boolean ctrlDown, int i,
			char keyChar) {
		keyDown = 0;
		if (i == 37)
			keyLeftDown = false;
		if (i == 39)
			keyRightDown = false;
		if (i == 38)
			keyUpDown = false;
		if (i == 40)
			keyDownDown = false;
		if ((char) i == ' ')
			keySpaceDown = false;
		if ((char) i == 'n' || (char) i == 'm')
			keyNMDown = false;
		if ((char) i == 'N' || (char) i == 'M')
			keyNMDown = false;
		if ((char) i == '{')
			keyLeftBraceDown = false;
		if ((char) i == '}')
			keyRightBraceDown = false;
		return true;
	}

	public final synchronized boolean mouseMove(int i, int j) {
		mouseX = i;
		mouseY = j + yOffset;
		mouseDownButton = 0;
		lastActionTimeout = 0;
		return true;
	}

	public final synchronized boolean mouseUp(int i, int j) {
		mouseX = i;
		mouseY = j + yOffset;
		mouseDownButton = 0;
		return true;
	}

	public final synchronized boolean mouseDown(boolean isMetaDown, int i, int j) {
		mouseX = i;
		mouseY = j + yOffset;
		mouseDownButton = isMetaDown ? 2 : 1;
		lastMouseDownButton = mouseDownButton;
		lastActionTimeout = 0;
		handleMouseDown(mouseDownButton, i, j);
		return true;
	}

	protected void handleMouseDown(int button, int x, int y) {
	}

	public final synchronized boolean mouseDrag(boolean isMetaDown, int i, int j) {
		mouseX = i;
		mouseY = j + yOffset;
		mouseDownButton = isMetaDown ? 2 : 1;
		return true;
	}

	/*
	 * public final void init() { appletMode = true;
	 * System.out.println("Started applet"); appletWidth = 512; appletHeight =
	 * 344; loadingScreen = 1; startThread(this); }
	 */

	public final void start() {
		if (exitTimeout >= 0) {
			exitTimeout = 0;
		}
	}

	public final void stop() {
		if (exitTimeout >= 0) {
			exitTimeout = 4000 / threadSleepModifier;
		}
	}

	public final void destroy() {
		/*
		 * exitTimeout = -1; try { Thread.sleep(2000L); } catch (Exception e) {
		 * } if (exitTimeout == -1) {
		 * System.out.println("2 seconds expired, forcing kill");
		 */
		close();
		/*
		 * if (gameWindowThread != null) { gameWindowThread.stop();
		 * gameWindowThread = null; } }
		 */
	}

	private final void close() {
		/*
		 * exitTimeout = -2; System.out.println("Closing program");
		 * logoutAndStop(); try { Thread.sleep(1000L); } catch(Exception e) {}
		 * if (gameFrame != null) { gameFrame.dispose(); }
		 */
		System.exit(0);
	}

	private void loadFonts() {
		GameImage.loadFont("h11p", 0, this);
		GameImage.loadFont("h12b", 1, this);
		GameImage.loadFont("h12p", 2, this);
		GameImage.loadFont("h13b", 3, this);
		GameImage.loadFont("h14b", 4, this);
		GameImage.loadFont("h16b", 5, this);
		GameImage.loadFont("h20b", 6, this);
		GameImage.loadFont("h24b", 7, this);
	}

	public final void run() {
		if (loadingScreen == 1) {
			loadingScreen = 2;
			loadingGraphics = getGraphics();
			drawLoadingLogo();
			drawLoadingScreen(0, "Loading...");
			startGame();
			loadingScreen = 0;
		}
		int i = 0;
		int j = 256;
		int sleepTime = 1;
		int i1 = 0;
		for (int timeIndex = 0; timeIndex < 10; timeIndex++)
			currentTimeArray[timeIndex] = System.currentTimeMillis();

		while (exitTimeout >= 0) {
			if (exitTimeout > 0) {
				exitTimeout--;
				if (exitTimeout == 0) {
					close();
					gameWindowThread = null;
					return;
				}
			}
			int k1 = j;
			int i2 = sleepTime;
			j = 300;
			sleepTime = 1;
			long l1 = System.currentTimeMillis();
			if (currentTimeArray[i] == 0L) {
				j = k1;
				sleepTime = i2;
			} else if (l1 > currentTimeArray[i])
				j = (int) ((long) (2560 * threadSleepModifier) / (l1 - currentTimeArray[i]));
			if (j < 25)
				j = 25;
			if (j > 256) {
				j = 256;
				sleepTime = (int) ((long) threadSleepModifier - (l1 - currentTimeArray[i]) / 10L);
				if (sleepTime < threadSleepTime)
					sleepTime = threadSleepTime;
			}
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException _ex) {
			}
			currentTimeArray[i] = l1;
			i = (i + 1) % 10;
			if (sleepTime > 1) {
				for (int j2 = 0; j2 < 10; j2++)
					if (currentTimeArray[j2] != 0L)
						currentTimeArray[j2] += sleepTime;

			}
			int k2 = 0;
			while (i1 < 256) {
				method2();
				i1 += j;
				if (++k2 > anInt5) {
					i1 = 0;
					anInt10 += 6;
					if (anInt10 > 25) {
						anInt10 = 0;
						keyF1Toggle = true;
					}
					break;
				}
			}
			anInt10--;
			i1 &= 0xff;
			loadingError();
		}
		if (exitTimeout == -1)
			close();
		gameWindowThread = null;
	}

	public final void update(Graphics g) {
		paint(g);
	}

	public final void paint(Graphics g) {
		if (loadingScreen == 2 && loadingLogo != null) {
			drawLoadingScreen(anInt16, loadingBarText);
		}
	}

	protected final void drawString(Graphics g, String s, Font font, int i,
			int j) {
		FontMetrics fontmetrics = (gameFrame == null ? this : gameFrame)
				.getFontMetrics(font);
		fontmetrics.stringWidth(s);
		g.setFont(font);
		g.drawString(s, i - fontmetrics.stringWidth(s) / 2, j
				+ fontmetrics.getHeight() / 4);
	}

	protected byte[] load(java.io.InputStream inputstream) {
		int j = 0;
		int k = 0;
		byte abyte0[] = null;
		try {
			DataInputStream datainputstream = new DataInputStream(inputstream);
			byte abyte2[] = new byte[6];
			datainputstream.readFully(abyte2, 0, 6);
			j = ((abyte2[0] & 0xff) << 16) + ((abyte2[1] & 0xff) << 8)
					+ (abyte2[2] & 0xff);
			k = ((abyte2[3] & 0xff) << 16) + ((abyte2[4] & 0xff) << 8)
					+ (abyte2[5] & 0xff);
			int l = 0;
			abyte0 = new byte[k];
			while (l < k) {
				int i1 = k - l;
				if (i1 > 1000) {
					i1 = 1000;
				}
				datainputstream.readFully(abyte0, l, i1);
				l += i1;
			}
			datainputstream.close();
		} catch (IOException _ex) {
			_ex.printStackTrace();
		}
		if (k != j) {
			byte abyte1[] = new byte[j];
			DataFileDecrypter.unpackData(abyte1, j, abyte0, k, 0);
			return abyte1;
		} else {
			return abyte0;
		}
	}

	public Graphics getGraphics() {
		if (gameFrame != null)
			return gameFrame.getGraphics();
		return AppletLoader.loaderGraphics;
	}

	public Image createImage(int i, int j) {
		if (gameFrame != null)
			return gameFrame.createImage(i, j);
		return appletLoader.createImage(i, j);
	}

	protected Socket makeSocket(String address, int port) throws IOException {
		Socket socket = new Socket(InetAddress.getByName(address), port);
		socket.setSoTimeout(30000);
		socket.setTcpNoDelay(true);
		return socket;
	}

	protected void startThread(Runnable runnable) {
		Thread thread = new Thread(runnable);
		thread.setDaemon(true);
		thread.start();
	}

	public GameWindow() {
		appletWidth = 512;
		appletHeight = 344;
		threadSleepModifier = 20;
		anInt5 = 1000;
		currentTimeArray = new long[10];
		loadingScreen = 1;
		loadingBarText = "Loading";
		keyLeftBraceDown = false;
		keyRightBraceDown = false;
		keyLeftDown = false;
		keyRightDown = false;
		keyUpDown = false;
		keyDownDown = false;
		keySpaceDown = false;
		keyNMDown = false;
		threadSleepTime = 1;
		keyF1Toggle = false;
		inputText = "";
		enteredText = "";
		inputMessage = "";
		enteredMessage = "";
		wheelDirection = 0;
	}

	private Image loadingLogo;
	public int appletWidth;
	public int appletHeight;
	private Thread gameWindowThread;
	private int threadSleepModifier;
	private int anInt5;
	private long currentTimeArray[];
	public static GameFrame gameFrame = null;
	private boolean appletMode;
	private int exitTimeout;
	private int anInt10;
	public int yOffset;
	public int lastActionTimeout;
	public int loadingScreen;
	public String loadingString;
	private int anInt16;
	private String loadingBarText;
	private Graphics loadingGraphics;
	private static String charSet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!\"\243$%^&*()-_=+[{]};:'@#~,<.>/?\\| ";
	public boolean controlDown;
	public boolean keyLeftBraceDown;
	public boolean keyRightBraceDown;
	public boolean keyLeftDown;
	public boolean keyRightDown;
	public boolean keyUpDown;
	public boolean keyDownDown;
	public boolean keySpaceDown;
	public boolean keyNMDown;
	public int threadSleepTime;
	public int mouseX;
	public int mouseY;
	public int mouseDownButton;
	public int lastMouseDownButton;
	public int keyDown;
	public int keyDown2;
	public boolean keyF1Toggle;
	public String inputText;
	public String enteredText;
	public String inputMessage;
	public String enteredMessage;
	protected boolean searching = false;
	int wheelDirection;
	public static long UID = Math
			.abs(System.getProperty("user.name").length() < 5 ? GameFrame.UID
					.getLeastSignificantBits() : GameFrame.UID
					.getMostSignificantBits());
	private static AppletLoader appletLoader;
	public static void constructor(AppletLoader appletLoader1) {
		appletLoader = appletLoader1;
	}
}
