using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
namespace Assets.RSC.Network
{
	using System.IO;
	using System.Net.Sockets;
	// using System.Numerics;
	using System.Threading;

	using Assets.RSC.Models;

	using Assets.RSC.IO;

	using UnityEngine;

	using Exception = System.Exception;
	using Random = System.Random;
	using String = System.String;
	using Thread = System.Threading.Thread;

	public abstract class GameClient
	{
		public int ServerPort { get; set; }
		public string ServerIP { get; set; }

		public static Random ran = new Random();
		public static int maxPacketReadCount;
		public String username;
		private String password;
		public StreamClass streamClass;
		public sbyte[] packetData;
		public int reconnectTries;
		public long lastPing;
		public int friendsCount;
		public long[] friendsList;
		public int[] friendsWorld;
		public int ignoresCount;
		public long[] ignoresList;
		public int blockChat;
		public int blockPrivate;
		public int blockTrade;
		public int blockDuel;
		public long sessionId;
		public int socketTimeout;

		public event EventHandler OnConnectSuccess;
		public event EventHandler OnConnectFailed;

		private static BigInteger key = new BigInteger("1370158896620336158431733257575682136836100155721926632321599369132092701295540721504104229217666225601026879393318399391095704223500673696914052239029335", 10);
		private static BigInteger modulus = new BigInteger("1549611057746979844352781944553705273443228154042066840514290174539588436243191882510185738846985723357723362764835928526260868977814405651690121789896823", 10);
		public bool reconnecting { get; set; }


		private static bool isConnecting = false;
		private Thread connectionThread;

		public static byte[] pingpacket = new byte[] { 82, 83, 67, 69 };

		public GameClient()
		{
			username = "";
			password = "";
			packetData = new sbyte[10000];
			friendsList = new long[40];
			friendsWorld = new int[400];
			ignoresList = new long[200];
		}

		public GameClient(string ip, int port)
			: this()
		{
			this.ServerIP = ip; this.ServerPort = port;
		}

		public void connect(String user, String pass, bool reconnecting)
		{
			if (isConnecting)
			{
				isConnecting = !reconnecting;
			}

			if (socketTimeout > 0)
			{
				loginScreenPrint("Please wait...", "Connecting to server");
				try
				{
					Thread.Sleep(2000);
				}
				catch (Exception _ex) { }
				loginScreenPrint("Sorry! The server is currently full.", "Please try again later");
				return;
			}
			try
			{
				// if (isConnecting && !reconnecting) return;
				isConnecting = true;

				username = user;
				password = pass;

				connectionThread = new Thread(new ThreadStart(DoConnect));
				connectionThread.Start();

			}
			catch (Exception e)
			{
				Debug.Log(e.ToString());
				// e.printStackTrace();
			}

		}

		public TcpClient makeSocket(string address, int port)
		{
			var socket = new TcpClient(address, port);
			socket.SendTimeout = 30000;
			socket.NoDelay = true;
			return socket;
		}

		private void DoConnect()
		{
			try
			{
				//username = user;
				var user = DataOperations.formatString(username, 20);
				// password = pass;
				var pass = DataOperations.formatString(password, 20);
				if (user.Trim().Length == 0)
				{
					loginScreenPrint("You must enter both a username", "and a password - Please try again");
					return;
				}
				if (reconnecting)
					gameBoxPrint("Connection lost! Please wait...", "Attempting to re-establish");
				else
					loginScreenPrint("Please wait...", "Connecting to server");
				streamClass = new StreamClass(makeSocket(this.ServerIP, this.ServerPort));
				streamClass.maxPacketReadCount = maxPacketReadCount;


				long l = DataOperations.nameToHash(user);
				streamClass.createPacket(2); // 32 old, 2 RSCE
				streamClass.addByte((int)(l >> 16 & 31L));
				streamClass.addString(UTF8Encoding.UTF8.GetString(pingpacket));
				streamClass.flush();

				long sessionId = streamClass.readLong();


				if (sessionId == 0L)
				{
					//     loginScreenPrint("Login server offline.", "Please try again in a few mins");
					//     return;
				}
				Debug.Log("Verb: Session id: " + sessionId);
				int[] sessionKeys = new int[4];
				sessionKeys[0] = (int)(ran.NextDouble() * 99999999D);
				sessionKeys[1] = (int)(ran.NextDouble() * 99999999D);
				sessionKeys[2] = (int)(sessionId >> 32);
				sessionKeys[3] = (int)sessionId;


				var dataEnc = new LoginDataEncryption(new byte[500]); // 117 old, 500 RSCE
				dataEnc.offset = 0;
				// dataEnc.addByte(reconnecting ? 1 : 0);
				// dataEnc.addInt(Config.CLIENT_VERSION);
				dataEnc.addInt(sessionKeys[0]);
				dataEnc.addInt(sessionKeys[1]);
				dataEnc.addInt(sessionKeys[2]);
				dataEnc.addInt(sessionKeys[3]);
				dataEnc.addInt(512); // Client Width, wth is it here?
				dataEnc.addString(user);
				dataEnc.addString(pass);
				dataEnc.encryptPacketWithKeys(key, modulus);
				streamClass.createPacket(75); // old 77, RSCE 75
				streamClass.addByte(reconnecting ? 1 : 0);
				// streamClass.addShort( /* Config.CLIENT_VERSION */ );
				
				streamClass.addShort(1024);

				/*Additional Fake bytes ?*/

				/* streamClass.addByte(64);
				streamClass.addByte(17);
				streamClass.addByte(212);
				streamClass.addByte(246);*/

				// streamClass.addBytes(data, 0, data.Length);
				streamClass.addBytes(dataEnc.packet, 0, dataEnc.offset);
				streamClass.flush();



				int loginResponse = streamClass.read();


				Debug.Log("login response:" + loginResponse);

				if (loginResponse == 99)
				{
					if (OnConnectSuccess != null) OnConnectSuccess(this, new EventArgs());

					reconnectTries = 0;
					initVars();
					return;
				}
				if (loginResponse == 0)
				{
					if (OnConnectSuccess != null) OnConnectSuccess(this, new EventArgs());

					reconnectTries = 0;
					initVars();
					return;
				}
				if (loginResponse == 1)
				{
					reconnectTries = 0;
					return;
				}
				if (reconnecting)
				{
					user = "";
					pass = "";
					resetIntVars();
					return;
				}

				if (OnConnectFailed != null) OnConnectFailed(this, new EventArgs());

				if (loginResponse == -1)
				{
					loginScreenPrint("Error unable to login.", "Server timed out");
				}
				else if (loginResponse == 2)
				{
					loginScreenPrint("Invalid username or password.", "Try again, or create a new account");
				}
				else if (loginResponse == 3)
				{
					loginScreenPrint("That username is already logged in.", "Wait 60 seconds then retry");
				}
				else if (loginResponse == 4)
				{
					loginScreenPrint("The client has been updated.", "Please download the newest one");
				}
				else if (loginResponse == 5)
				{
					loginScreenPrint("Error unable to login.", "Server rejected session");
				}
				else if (loginResponse == 6)
				{
					loginScreenPrint("Character banned.", "Please post a topic in \"Offence Appeal\"");
				}
				else if (loginResponse == 7)
				{
					loginScreenPrint("Failed to decode character.", "Please post a topic in \"Support\"");
				}
				else if (loginResponse == 8)
				{
					loginScreenPrint("IP Already in use.", "You may only login once at a time");
				}
				else if (loginResponse == 9)
				{
					loginScreenPrint("Account already in use.", "You may only login to one character at a time");
				}
				else if (loginResponse == 10)
				{
					loginScreenPrint("Server full!.", "Please try again later.");
				}
				else if (loginResponse == 11)
				{
					loginScreenPrint("Character banned.", "Please post a topic in \"Offence Appeal\"");
				}
				else if (loginResponse == 12)
				{
					loginScreenPrint("IP banned.", "Please post a topic in \"Offence Appeal\"");
				}
				else if (loginResponse == 13)
				{
					loginScreenPrint("Client dimensions are too large.", "Please subscribe if you want a larger client.");
				}
				else
				{
					loginScreenPrint("Error unable to login.", "Unrecognised response code.");
				}

				//if (reconnectTries > 0)
				//{
				//	try
				//	{
				//		Thread.Sleep(2500);
				//	}
				//	catch (Exception _ex) { }
				//	reconnectTries--;
				//	connect(username, password, reconnecting);
				//}
				//if (reconnecting)
				//{
				//	username = "";
				//	password = "";
				//	resetIntVars();
				//}
				//else
				//{
				//	loginScreenPrint("Sorry! Unable to connect.", "Check internet settings or try another world");
				//}
			}
			catch { loginScreenPrint("Sorry! Unable to connect.", "Check internet settings or try another world"); }
		}

		public void requestLogout()
		{
			if (streamClass != null)
				try
				{
					streamClass.createPacket(39);
					streamClass.flush();
				}
				catch (IOException _ex) { }
			username = "";
			password = "";
			resetIntVars();
			loginScreenPrint("Please enter your usename and password", "");
		}

		public virtual void lostConnection()
		{
			Debug.Log("Lost connection");
			//connect(username, password, true);
			loginScreenPrint("Please enter your usename and password", "");
		}

		protected void gameBoxPrint(String s1, String s2)
		{

			//Font font = new Font("Helvetica", 1, 15);
			char c = '\u0200';
			char c1 = '\u0158';
			// g.setColor(Color.Black);

			//g.fillRect(c / 2 - 140, c1 / 2 - 25, 280, 50, Color.Black);

			//g.setColor(Color.White);
			//g.drawRect(c / 2 - 140, c1 / 2 - 25, 280, 50, Color.White);
			//drawString(s1/*, font*/, c / 2, c1 / 2 - 10, Color.White);
			//drawString(s2/*, font*/, c / 2, c1 / 2 + 10, Color.White);
		}

		protected void sendPingPacket()
		{
			long l = CurrentTimeMillis();
			if (streamClass.hasData())
				lastPing = l;
			if (l - lastPing > 5000L)
			{
				lastPing = l;
				streamClass.createPacket(5);
				streamClass.formatPacket();
			}
			try
			{
				streamClass.writePacket(20);
			}
			catch (IOException _ex)
			{
				lostConnection();
				return;
			}
			int packetLength = streamClass.readPacket(packetData);
			if (packetLength > 0)
				handlePacket(packetData[0] & 0xff, packetLength);
		}

		public virtual void handlePacket(int command, int length)
		{
			if (command == 48)
			{
				var s1 = Encoding.UTF8.GetString((byte[])(Array)packetData, 1, length - 1);
				//String s1 = new String(packetData, 1, length - 1);
				displayMessage(s1);
				return;
			}
			if (command == 222) // 
			{
				requestLogout();
				return;
			}
			if (command == 136)
			{
				cantLogout();
				return;
			}
			if (command == 249)
			{
				friendsCount = DataOperations.getByte((sbyte)packetData[1]);
				for (int i = 0; i < friendsCount; i++)
				{
					friendsList[i] = DataOperations.getLong(packetData, 2 + i * 9);
					friendsWorld[i] = DataOperations.getByte(packetData[10 + i * 9]);
				}

				reOrderFriendsList();
				return;
			}
			if (command == 25)
			{
				long friend = DataOperations.getLong(packetData, 1);
				int status = packetData[9] & 0xff;
				for (int j1 = 0; j1 < friendsCount; j1++)
					if (friendsList[j1] == friend)
					{
						if (friendsWorld[j1] == 0 && status != 0)
							displayMessage("@pri@" + DataOperations.hashToName(friend) + " has logged in");
						if (friendsWorld[j1] != 0 && status == 0)
							displayMessage("@pri@" + DataOperations.hashToName(friend) + " has logged out");
						friendsWorld[j1] = status;
						length = 0;
						reOrderFriendsList();
						return;
					}

				friendsList[friendsCount] = friend;
				friendsWorld[friendsCount] = status;
				friendsCount++;
				reOrderFriendsList();
				return;
			}
			if (command == 2)
			{
				ignoresCount = DataOperations.getByte(packetData[1]);
				for (int j = 0; j < ignoresCount; j++)
					ignoresList[j] = DataOperations.getLong(packetData, 2 + j * 8);

				return;
			}
			if (command == 158)
			{
				blockChat = packetData[1];
				blockPrivate = packetData[2];
				blockTrade = packetData[3];
				blockDuel = packetData[4];
				return;
			}
			if (command == 170)
			{
				long l1 = DataOperations.getLong(packetData, 1);
				String s = ChatMessage.bytesToString(packetData, 9, length - 9);
				displayMessage("@pri@" + DataOperations.hashToName(l1) + ": tells you " + s);
				return;
			}
			//if (command == 211)
			//{// TODO remove?
			//	streamClass.createPacket(69);
			//	streamClass.addByte(0);// scar.exe, etc
			//	streamClass.formatPacket();
			//	return;
			//}
			//if (command == 1)
			//{// TODO remove?
			//	//bluePoints
			//	//redPoints
			//	return;
			//}
			handlePacket(command, length, packetData);
		}



		private void reOrderFriendsList()
		{
			bool flag = true;
			while (flag)
			{
				flag = false;
				for (int i = 0; i < friendsCount - 1; i++)
					if (friendsWorld[i] < friendsWorld[i + 1])
					{
						int j = friendsWorld[i];
						friendsWorld[i] = friendsWorld[i + 1];
						friendsWorld[i + 1] = j;
						long l = friendsList[i];
						friendsList[i] = friendsList[i + 1];
						friendsList[i + 1] = l;
						flag = true;
					}

			}
		}

		protected void sendUpdatedPrivacyInfo(int blockChat, int blockPrivate, int blockTrade, int blockDuel)
		{
			streamClass.createPacket(176);
			streamClass.addByte(blockChat);
			streamClass.addByte(blockPrivate);
			streamClass.addByte(blockTrade);
			streamClass.addByte(blockDuel);
			streamClass.formatPacket();
		}

		protected void addIgnore(String arg0)
		{
			long l = DataOperations.nameToHash(arg0);
			streamClass.createPacket(25);
			streamClass.addLong(l);
			streamClass.formatPacket();
			for (int i = 0; i < ignoresCount; i++)
				if (ignoresList[i] == l)
					return;

			if (ignoresCount >= ignoresList.Length - 1)
			{
				return;
			}
			else
			{
				ignoresList[ignoresCount++] = l;
				return;
			}
		}

		protected void removeIgnore(long arg0)
		{
			streamClass.createPacket(108);
			streamClass.addLong(arg0);
			streamClass.formatPacket();
			for (int i = 0; i < ignoresCount; i++)
				if (ignoresList[i] == arg0)
				{
					ignoresCount--;
					for (int j = i; j < ignoresCount; j++)
						ignoresList[j] = ignoresList[j + 1];

					return;
				}

		}

		protected void addFriend(String arg0)
		{
			streamClass.createPacket(168);
			streamClass.addLong(DataOperations.nameToHash(arg0));
			streamClass.formatPacket();
			long l = DataOperations.nameToHash(arg0);
			for (int i = 0; i < friendsCount; i++)
				if (friendsList[i] == l)
					return;

			if (friendsCount >= friendsList.Length - 1)
			{
				return;
			}
			else
			{
				friendsList[friendsCount] = l;
				friendsWorld[friendsCount] = 0;
				friendsCount++;
				return;
			}
		}

		protected void removeFriend(long arg0)
		{
			streamClass.createPacket(52);
			streamClass.addLong(arg0);
			streamClass.formatPacket();
			for (int i = 0; i < friendsCount; i++)
			{
				if (friendsList[i] != arg0)
					continue;
				friendsCount--;
				for (int j = i; j < friendsCount; j++)
				{
					friendsList[j] = friendsList[j + 1];
					friendsWorld[j] = friendsWorld[j + 1];
				}

				break;
			}

			displayMessage("@pri@" + DataOperations.hashToName(arg0) + " has been removed from your friends list");
		}

		protected void sendPrivateMessage(long l, byte[] abyte0, int i)
		{
			streamClass.createPacket(254);
			streamClass.addLong(l);
			streamClass.addBytes(abyte0, 0, i);
			streamClass.formatPacket();
		}

		protected void sendChatMessage(byte[] abyte0, int i)
		{
			streamClass.createPacket(145);
			streamClass.addBytes(abyte0, 0, i);
			streamClass.formatPacket();
		}

		protected void sendCommand(String s1)
		{
			streamClass.createPacket(90);
			streamClass.addString(s1);
			streamClass.formatPacket();
		}



		public abstract void loginScreenPrint(String s1, String s2);
		public abstract void initVars();

		public abstract void resetIntVars();

		public abstract void cantLogout();

		public abstract void handlePacket(int i, int j, sbyte[] abyte0);

		public abstract void displayMessage(String s1);
		private static readonly DateTime Jan1st1970 = new DateTime(1970, 1, 1, 0, 0, 0, DateTimeKind.Utc);

		public static long CurrentTimeMillis()
		{
			return (long)(DateTime.UtcNow - Jan1st1970).TotalMilliseconds;
		}

		internal void Disconnect()
		{
			streamClass.Disconnect();
		}
	}
}
