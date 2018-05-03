package org.rscemulation.client;

import org.rscemulation.client.util.DataConversions;
import org.rscemulation.client.util.ChatFilter;
import java.awt.*;
import java.net.*;
import java.io.*;
import java.io.IOException;
import java.math.BigInteger;

public abstract class GameWindowMiddleMan extends GameWindow {
	
    protected final void login(String user, String pass, boolean reconnecting) {
        if (socketTimeout > 0) {
            loginScreenPrint("Please wait...", "Connecting to server");
            try {
                Thread.sleep(2000L);
            } catch (Exception _ex) {} 
            System.out.println("Accessed");
            loginScreenPrint("Sorry! The server is currently full.", "Please try again later");
            return;
        }
        try {
            username = user;
            user = DataOperations.addCharacters(user, 20);
            password = pass;
            pass = DataOperations.addCharacters(pass, 20);
            if (user.trim().length() == 0) {
                loginScreenPrint("You must enter both a username", "and a password - Please try again");
                return;
            }
            if (reconnecting)
                gameBoxPrint("Connection lost! Please wait...", "Attempting to re-establish");
            else
                loginScreenPrint("Please wait...", "Connecting to server");
            streamClass = new StreamClass(makeSocket(serverIP, port), this);
			streamClass.maxPacketReadCount = maxPacketReadCount;
            long l = DataOperations.stringLength12ToLong(user);
            streamClass.createPacket(2);
			streamClass.addByte((int) (l >> 16 & 31L));
            streamClass.addString(new String(pingpacket));
            streamClass.finalisePacket();
            long sessionID = streamClass.read8ByteLong();
            if (sessionID == 0L) {
                loginScreenPrint("Login server offline.", "Please try again in a few mins");
                return;
            }
            int sessionRotationKeys[] = new int[4];
            sessionRotationKeys[0] = (int) (Math.random() * 99999999D);
            sessionRotationKeys[1] = (int) (Math.random() * 99999999D);
            sessionRotationKeys[2] = (int) (sessionID >> 32);
            sessionRotationKeys[3] = (int) sessionID;
            DataEncryption dataEncryption = new DataEncryption(new byte[500]);
            dataEncryption.offset = 0;
            dataEncryption.add4ByteInt(sessionRotationKeys[0]);
            dataEncryption.add4ByteInt(sessionRotationKeys[1]);
            dataEncryption.add4ByteInt(sessionRotationKeys[2]);
            dataEncryption.add4ByteInt(sessionRotationKeys[3]);
            dataEncryption.add4ByteInt(gW);
            dataEncryption.addString(user);
            dataEncryption.addString(pass);
            dataEncryption.encryptPacketWithKeys(key, modulus);
            streamClass.createPacket(75);
            if (reconnecting)
                streamClass.addByte(1);
            else
                streamClass.addByte(0);
            streamClass.add2ByteInt(clientVersion);
            streamClass.addBytes(dataEncryption.packet, 0, dataEncryption.offset);
            streamClass.finalisePacket();
            int loginResponse = streamClass.readInputStream();
            if (loginResponse == 99) {
                reconnectTries = 0;
                resetVars();
            } else if (loginResponse == 0) {
                reconnectTries = 0;
                resetVars();
            } else if (loginResponse == 1) {
                reconnectTries = 0;
            } else if (reconnecting) {
                //user = "";
                //pass = "";
                resetIntVars();
            } else if (loginResponse == -1) {
                loginScreenPrint("Error unable to login.", "Server timed out");
            } else if (loginResponse == 2) {
                loginScreenPrint("Invalid username or password.", "Try again, or create a new account");
            } else if (loginResponse == 3) {
                loginScreenPrint("That username is already logged in.", "Wait 60 seconds then retry");
            } else if (loginResponse == 4) {
                loginScreenPrint("The client has been updated.", "Please download the newest one");
            } else if (loginResponse == 5) {
                loginScreenPrint("Error unable to login.", "Server rejected session");
            } else if (loginResponse == 6) {
                loginScreenPrint("Character banned.", "Please post a topic in \"Offence Appeal\"");
            } else if (loginResponse == 7) {
                loginScreenPrint("Failed to decode character.", "Please post a topic in \"Support\"");
            } else if (loginResponse == 8) {
                loginScreenPrint("IP Already in use.", "You may only login once at a time");
            } else if (loginResponse == 9) {
                loginScreenPrint("Account already in use.", "You may only login to one character at a time");
            } else if (loginResponse == 10) {
                loginScreenPrint("Server full!.", "Please try again later.");
            } else if (loginResponse == 11) {
                loginScreenPrint("Character banned.", "Please post a topic in \"Offence Appeal\"");
            } else if (loginResponse == 12) {
                loginScreenPrint("IP banned.", "Please post a topic in \"Offence Appeal\"");
            } else if (loginResponse == 13) {
				loginScreenPrint("Client dimensions are too large.", "Please subscribe if you want a larger client.");
			} else {
				loginScreenPrint("Error unable to login.", "Unrecognised response code.");
			}
        } catch (Exception exception) {
        	exception.printStackTrace();
			loginScreenPrint("Sorry! Unable to connect.", "Check internet settings or try another world");
        }
    }

    protected void lostConnection() {
		resetIntVars();
    }

    protected final void gameBoxPrint(String s, String s1) {
        Graphics g = getGraphics();
        Font font = new Font("Helvetica", 1, 15);
        char c = '\u0200';
        char c1 = '\u0158';
        g.setColor(Color.black);
        g.fillRect(gW / 2 - 140, gH / 2 - 25, 280, 50);
        g.setColor(Color.white);
        g.drawRect(gW / 2 - 140, gH / 2 - 25, 280, 50);
        drawString(g, s, font, gW / 2, gH / 2 - 10);
        drawString(g, s1, font, gW / 2, gH / 2 + 10);
    }

    protected final void sendPingPacketReadPacketData() {
        long l = System.currentTimeMillis();
        if (streamClass.containsData())
            lastPing = l;
        if (l - lastPing > 5000) {
            lastPing = l;
            streamClass.createPacket(5);
            streamClass.formatPacket();
        }
        try {
            streamClass.writePacket(20);
        }
        catch (IOException _ex) {
			System.out.println("Connection lost");
            lostConnection();
            return;
        }
        int packetLength = streamClass.readPacket(packetData);
        if (packetLength > 0) {
            checkIncomingPacket(packetData[0] & 0xff, packetLength);
        }
    }

    protected final void checkIncomingPacket(int command, int length) {
		if (command == 48) {
            String s = new String(packetData, 1, length - 1);
            handleServerMessage(s);
        }
        if (command == 136) {
            cantLogout();
            return;
        }
		if (command == 234) { //Clan Removal / Addition
			int operationType = packetData[1] & 0xFF;
			long additionOrRemoval = DataOperations.getUnsigned8Bytes(packetData, 2);
			if(operationType == 0) { //Remove
				removeFromClan(additionOrRemoval);
			} else if(operationType == 1) { //Add
				int onlineStatus = packetData[10] & 0xFF;
				addToClan(additionOrRemoval, onlineStatus);
			}
			return;
		}
		if (command == 235) {
			String mobName = DataConversions.hashToUsername(DataOperations.getUnsigned8Bytes(packetData, 1));
			int rank = packetData[9] & 0xFF;
			String cName = DataConversions.hashToClanTag(DataOperations.getUnsigned4Bytes(packetData, 10));
			String message = new String(packetData, 14, length - 14);
			displayGlobalChat(mobName, rank, cName, message);
		}
		if (command == 233) {	//Initial Clanlist upon login
			clanCount = DataOperations.getUnsigned2Bytes(packetData, 1);
			for(int idx = 0; idx < clanCount; idx++) {
				clanListLongs[idx] = DataOperations.getUnsigned8Bytes(packetData, 3 + idx * 9);
                clanListOnlineStatus[idx] = DataOperations.getUnsignedByte(packetData[11 + idx * 9]);
			}
			reOrderClanListByOnlineStatus();
			return;
		}
        if (command == 249) {
            friendsCount = DataOperations.getUnsignedByte(packetData[1]);
            for (int k = 0; k < friendsCount; k++) {
                friendsListLongs[k] = DataOperations.getUnsigned8Bytes(packetData, 2 + k * 9);
                friendsListOnlineStatus[k] = DataOperations.getUnsignedByte(packetData[10 + k * 9]);
            }

            reOrderFriendsListByOnlineStatus();
            return;
        }
        if (command == 25) {
            long friend = DataOperations.getUnsigned8Bytes(packetData, 1);
            int status = packetData[9] & 0xff;
            for (int i2 = 0; i2 < friendsCount; i2++)
                if (friendsListLongs[i2] == friend) {
                    if (friendsListOnlineStatus[i2] == 0 && status != 0)
                       displayGenericMessage("@pri@@cya@" + DataOperations.longToString(friend) + " has logged in", 6);
                    if (friendsListOnlineStatus[i2] != 0 && status == 0)
                        displayGenericMessage("@pri@@cya@" + DataOperations.longToString(friend) + " has logged out", 6);
                    friendsListOnlineStatus[i2] = status;
                    length = 0;
                    reOrderFriendsListByOnlineStatus();
                    return;
                }

            friendsListLongs[friendsCount] = friend;
            friendsListOnlineStatus[friendsCount] = status;
            friendsCount++;
            reOrderFriendsListByOnlineStatus();
            return;
        }
		if (command == 24) {	//ClanMate Update
			long clanMate = DataOperations.getUnsigned8Bytes(packetData, 1);
			int status = packetData[9] & 0xFF;
			for (int idx = 0; idx < clanCount; idx++) {
				if(clanListLongs[idx] == clanMate) {
					clanListOnlineStatus[idx] = status;
					length = 0;
					reOrderClanListByOnlineStatus();
					return;
				}
			}
			return;
		}
        if (command == 2) {
            ignoreListCount = DataOperations.getUnsignedByte(packetData[1]);
            for (int i1 = 0; i1 < ignoreListCount; i1++) {
                ignoreListLongs[i1] = DataOperations.getUnsigned8Bytes(packetData, 2 + i1 * 8);
            }
            return;
        }
        if (command == 158) {
            blockChatMessages = ((packetData[1] & 1) != 0 ? true : false);
            blockPrivateMessages = ((packetData[1] & 2) != 0 ? true : false);
            blockTradeRequests = ((packetData[1] & 4) != 0 ? true : false);
            blockDuelRequests = ((packetData[1] & 8) != 0 ? true : false);
            blockSayMessages = ((packetData[1] & 16) != 0 ? true : false);
            return;
        }
		if (command == 170) {		//SERVER -> CLIENT PACKETS! HAVEN'T EVEN STARTED YET CHG # 2
            long mobUsernameHash = DataOperations.getUnsigned8Bytes(packetData, 1);
			int rank = packetData[9] & 0xFF;
			int clanHash = DataOperations.getUnsigned4Bytes(packetData, 10);
            String message = DataConversions.byteToString(packetData, 9, length - 9);
			displayPrivateMessage(mobUsernameHash, rank, clanHash, message);
            //handleServerMessage("@pri@@cya@" + DataOperations.longToString(user) + " tells you: " + s1, user);
			/**
					s.addLong(usernameHash);
		s.addByte((byte)rank);
		s.addInt(clan);
		s.addBytes(message);
			*/
            return;
        } else {
            handleIncomingPacket(command, length, packetData);
            return;
        }
    }
	
	private final void reOrderClanListByOnlineStatus() {
        boolean flag = true;
        while (flag) {
            flag = false;
            for (int i = 0; i < clanCount - 1; i++)
                if (clanListOnlineStatus[i] < clanListOnlineStatus[i + 1]) {
                    int j = clanListOnlineStatus[i];
                    clanListOnlineStatus[i] = clanListOnlineStatus[i + 1];
                    clanListOnlineStatus[i + 1] = j;
                    long l = clanListLongs[i];
                    clanListLongs[i] = clanListLongs[i + 1];
                    clanListLongs[i + 1] = l;
                    flag = true;
                }

        }
    }
	
    private final void reOrderFriendsListByOnlineStatus() {
        boolean flag = true;
        while (flag) {
            flag = false;
            for (int i = 0; i < friendsCount - 1; i++)
                if (friendsListOnlineStatus[i] < friendsListOnlineStatus[i + 1]) {
                    int j = friendsListOnlineStatus[i];
                    friendsListOnlineStatus[i] = friendsListOnlineStatus[i + 1];
                    friendsListOnlineStatus[i + 1] = j;
                    long l = friendsListLongs[i];
                    friendsListLongs[i] = friendsListLongs[i + 1];
                    friendsListLongs[i + 1] = l;
                    flag = true;
                }

        }
    }

    protected final void addToIgnoreList(String s) {
        long l = DataOperations.stringLength12ToLong(s);
        streamClass.createPacket(46);
        streamClass.addTwo4ByteInts(l);
        streamClass.formatPacket();
        for (int i = 0; i < ignoreListCount; i++)
            if (ignoreListLongs[i] == l)
                return;

        if (ignoreListCount >= ignoreListLongs.length - 1) {
            return;
        } else {
            ignoreListLongs[ignoreListCount++] = l;
            return;
        }
    }

    protected final void removeFromIgnoreList(long l) {
        streamClass.createPacket(47);
        streamClass.addTwo4ByteInts(l);
        streamClass.formatPacket();
        for (int i = 0; i < ignoreListCount; i++)
            if (ignoreListLongs[i] == l) {
                ignoreListCount--;
                for (int j = i; j < ignoreListCount; j++)
                    ignoreListLongs[j] = ignoreListLongs[j + 1];

                return;
            }

    }

	protected final void addToClan(long member, int online) {
		clanListLongs[clanCount] = member;
		clanListOnlineStatus[clanCount] = online;
		clanCount++;
	}
	
    protected final void addToFriendsList(String s) {
        streamClass.createPacket(44);
        streamClass.addTwo4ByteInts(DataOperations.stringLength12ToLong(s));
        streamClass.formatPacket();
        long l = DataOperations.stringLength12ToLong(s);
        for (int i = 0; i < friendsCount; i++)
            if (friendsListLongs[i] == l)
                return;

        if (friendsCount >= friendsListLongs.length - 1) {
            return;
        } else {
            friendsListLongs[friendsCount] = l;
            friendsListOnlineStatus[friendsCount] = 0;
            friendsCount++;
            return;
        }
    }

	protected final void removeFromClan(long member) {
		for (int idx = 0; idx < clanCount; idx++) {
			if(clanListLongs[idx] != member) {
				continue;
			}
			clanCount--;
			for(int innerIdx = idx; innerIdx < clanCount; innerIdx++) {
				clanListLongs[innerIdx] = clanListLongs[innerIdx + 1];
				clanListOnlineStatus[innerIdx] = clanListOnlineStatus[innerIdx + 1];
			}
			break;
		}
	}
	
    protected final void removeFromFriends(long l) {
        streamClass.createPacket(45);
        streamClass.addTwo4ByteInts(l);
        streamClass.formatPacket();
        for (int i = 0; i < friendsCount; i++) {
            if (friendsListLongs[i] != l)
                continue;
            friendsCount--;
            for (int j = i; j < friendsCount; j++) {
                friendsListLongs[j] = friendsListLongs[j + 1];
                friendsListOnlineStatus[j] = friendsListOnlineStatus[j + 1];
            }

            break;
        }

        handleServerMessage("@pri@@cya@" + DataOperations.longToString(l) + " has been removed from your friends list");
    }

    protected final void sendPrivateMessage(long user, byte message[], int messageLength) {
        streamClass.createPacket(48);
        streamClass.addTwo4ByteInts(user);
        streamClass.addBytes(message, 0, messageLength);
        streamClass.formatPacket();
    }

    protected final void sendChatMessage(byte abyte0[], int i) {
        streamClass.createPacket(9);
        streamClass.addBytes(abyte0, 0, i);
        streamClass.formatPacket();
    }

    protected final void sendChatString(String s) {
        streamClass.createPacket(12);
        streamClass.addString(s);
        streamClass.formatPacket();
    }
    
	protected abstract void loginScreenPrint(String s, String s1);
    protected abstract void resetVars();
    protected abstract void resetIntVars();
    protected abstract void cantLogout();
    protected abstract void handleIncomingPacket(int command, int length, byte[] abyte0);
    protected abstract void handleServerMessage(String s);
	protected abstract void displayPrivateMessage(long mobUsernameHash, int rank, int clanHash, String message);
	protected abstract void displayNpcMessage(String npcMessage);
	protected abstract void displayGenericMessage(String message, int chatTab);
	protected abstract void displayGlobalChat(String mobName, int rank, String cName, String message);
	
    public GameWindowMiddleMan() {
        username = "";
        password = "";
        packetData = new byte[10000];
        friendsListLongs = new long[400];
        friendsListOnlineStatus = new int[400];
        ignoreListLongs = new long[200];
		clanListLongs = new long[100];
		clanListOnlineStatus = new int[100];
    }

    public static int clientVersion = 155;
    public static int maxPacketReadCount;
	public static String serverIP;
	protected byte[] pingpacket = {82,83,67,69};
    String username;
    String password;
    protected StreamClass streamClass;
    protected byte[] packetData;
    int reconnectTries;
    long lastPing;
	protected long ping;
    long lastPinging;
	public int clanCount;
	public long[] clanListLongs;
	public int[] clanListOnlineStatus;
    public int friendsCount;
    public long[] friendsListLongs;
    public int[] friendsListOnlineStatus;
    public int ignoreListCount;
    public long[] ignoreListLongs;
    public boolean blockChatMessages;
    public boolean blockSayMessages;
    public boolean blockPrivateMessages;
    public boolean blockTradeRequests;
    public boolean blockDuelRequests;
    private static BigInteger key = new BigInteger("1370158896620336158431733257575682136836100155721926632321599369132092701295540721504104229217666225601026879393318399391095704223500673696914052239029335");
    private static BigInteger modulus = new BigInteger("1549611057746979844352781944553705273443228154042066840514290174539588436243191882510185738846985723357723362764835928526260868977814405651690121789896823");
    public int socketTimeout;
    public static int port;
	public static int gW;
	public static int gH;
}
