namespace Assets.RSC
{
	using System;
	using System.Timers;

	using Assets.RSC.IO;
	using Assets.RSC.Managers;
	using Assets.RSC.Models;
	using Assets.RSC.Network;

	using UnityEngine;

	using Camera = Assets.RSC.Models.Camera;

	public class Mudclient : GameClient
	{
		public static string[] SkillName = {
			"Attack", "Defense", "Strength", "Hits", "Ranged", "Prayer", "Magic", "Cooking", "Woodcut", "Fletching", 
			"Fishing", "Firemaking", "Crafting", "Smithing", "Mining", "Herblaw", "Agility", "Thieving", "Unknown"
		};

		public static int gridSize = 128;

		public static int SectionX = 121;

		public static int SectionY = 647;

		public static int AreaX = 0;

		public static int AreaY = 0;

		public static int PositionX { get { return SectionX + AreaX; } }

		public static int PositionY { get { return SectionY + AreaY; } }

		public static bool loadArea = false;

		private Timer _pinger, _gameUpdater;

		private int minimapRandomRotationX = 0, minimapRandomRotationY = 0, cameraRotation = 0;

		public Texture2D MinimapImage;

		public event EventHandler OnDisplayLoginMessage;

		public string LoginMessageRow1, LoginMessageRow2;

		public static int wildX;

		public static int wildY;

		public static int layerIndex;

		public static int lastLayerIndex;

		public static int layerModifier;

		public static bool needsClear;

		public static bool hasWorldInfo;

		public static int sectionWidth;

		public static int sectionHeight;

		public static int sectionPosX;

		public static int sectionPosY;

		public static Mudclient Instance;

		public Mudclient(string ip, int port)
			: base(ip, port)
		{
			Instance = this;

			MobManager.MyPlayer = new Player();

			_pinger = new Timer();
			_pinger.Interval = 500;
			_pinger.Elapsed += _pinger_Elapsed;

			_gameUpdater = new Timer();
			_gameUpdater.Interval = 100;
			_gameUpdater.Elapsed += _gameUpdater_Elapsed;
		}

		void _gameUpdater_Elapsed(object sender, ElapsedEventArgs e)
		{
			// drawMinimapMenu(false);

			// MinimapImage = DataLoader.FromGamePixels();

			//MainForm.Instance.BeginInvoke(
			//	() =>
			//	{
			//		if (MinimapImage != null) { 
			//		MainForm.Instance.pbMinimap.Image = MinimapImage;
			//		MainForm.Instance.Invalidate();
			//		Application.DoEvents();
			//		}
			//	});


		}

		void _pinger_Elapsed(object sender, ElapsedEventArgs e)
		{
			sendPingPacket();
		}


		public override void initVars()
		{
			_pinger.Start();
			// _gameUpdater.Start();
		}

		public override void loginScreenPrint(string s1, string s2)
		{
			LoginMessageRow1 = s1;
			LoginMessageRow2 = s2;
			if (OnDisplayLoginMessage != null) OnDisplayLoginMessage(this, new EventArgs());
		}

		public override void resetIntVars()
		{
			_pinger.Stop();
			// _gameUpdater.Stop();
		}

		public override void cantLogout()
		{
		}

		public override void handlePacket(int command, int length, sbyte[] data)
		{
			// sbyte[] to byte[]
			// (byte[])(Array) signed;

			// byte[] to sbyte[]
			// (sbyte[]) (Array) unsigned;

			var cmd = (ServerPacketTypes)command;
			Debug.Log("Packet Recieved: " + cmd + " " + command + " - " + length);

			// var udata = (byte[])(Array)data;


			if (command == 117)
			{
				lastPing = CurrentTimeMillis();
				streamClass.createPacket(5);
				streamClass.formatPacket();
			}

			if (cmd == ServerPacketTypes.SET_SERVER_INDEX)
			{
				ServerPacketHandler.InitiateServerInfo(data, length);
			}

			if (cmd == ServerPacketTypes.SET_SLEEP_IMAGE)
			{
				// var image = MainForm.GetSleepImage(udata);
			}

			if (cmd == ServerPacketTypes.SET_SKILLS)
			{
				//	Debug.Log("Before stats update");

				ServerPacketHandler.UpdatePlayerSkills(data, length);
				// Debug.Log("Player stats updated: " + MobManager.MyPlayer.StatCurrent[0] + " " + MobManager.MyPlayer.StatCurrent[1] + MobManager.MyPlayer.StatCurrent[2] + " " + MobManager.MyPlayer.StatCurrent[3] + " " + MobManager.MyPlayer.StatCurrent[4] + "...");


			}

			if (cmd == ServerPacketTypes.PLAYER_APPEARANCE_UPDATE)
			{
				ServerPacketHandler.UpdatePlayerAppearance(data, length);
			}

			if (cmd == ServerPacketTypes.PLAYER_POS_UPDATE)
			{
				ServerPacketHandler.UpdatePlayerPositionData(data, length);
			}

			if (cmd == ServerPacketTypes.NPC_POS_UPDATE)
			{
				ServerPacketHandler.UpdateNpcPositionData(data, length);
			}

			if (cmd == ServerPacketTypes.ITEM_POS_UPDATE)
			{
				ServerPacketHandler.UpdateItemPositionData(data, length);
			}


			if (cmd == ServerPacketTypes.SET_INVENTORY_ITEMS)
			{
				ServerPacketHandler.SetInventoryItems(data, length);
			}

			if (cmd == ServerPacketTypes.RECIEVE_PRIVATE_MESSAGE)
			{
				var tmpMsg = ChatMessage.bytesToString(data, 1, length - 2);

				// string tmpBytes = "[" + string.Join(",", data) + "]";

			}


		}


		internal static bool LoadSection(int x, int y)
		{
			loadArea = false;
			x += wildX;
			y += wildY;
			if (lastLayerIndex == layerIndex && x > sectionWidth && x < sectionPosX && y > sectionHeight && y < sectionPosY)
			{
				// engineHandle.playerIsAlive = true;
				return false; // false;
			}

			// gameGraphics.drawText("Loading... Please wait", 256, 192, 1, 0xffffff);

			// drawChatMessageTabs();


			//gameGraphics.drawImage(spriteBatch, 0, 0);
			int l = AreaX;
			int i1 = AreaY;
			int xBase = (x + 24) / 48;
			int yBase = (y + 24) / 48;
			lastLayerIndex = layerIndex;
			AreaX = xBase * 48 - 48;
			AreaY = yBase * 48 - 48;
			sectionWidth = xBase * 48 - 32;
			sectionHeight = yBase * 48 - 32;
			sectionPosX = xBase * 48 + 32;
			sectionPosY = yBase * 48 + 32;

			Engine.loadSection(x, y, lastLayerIndex);


			AreaX -= wildX;
			AreaY -= wildY;
			int offsetX = AreaX - l;
			int offsetY = AreaY - i1;

			
			
			//for (int j2 = 0; j2 < objectCount; j2++)
			//{
			//	objectX[j2] -= offsetX;
			//	objectY[j2] -= offsetY;
			//	int objX = objectX[j2];
			//	int objY = objectY[j2];
			//	int objType = objectType[j2];
			//	Models.GameObject _obj = objectArray[j2];
			//	try
			//	{
			//		int objDir = objectRotation[j2];
			//		int objWidth;
			//		int objHeight;
			//		if (objDir == 0 || objDir == 4)
			//		{
			//			objWidth = Data.objectWidth[objType];
			//			objHeight = Data.objectHeight[objType];
			//		}
			//		else
			//		{
			//			objHeight = Data.objectWidth[objType];
			//			objWidth = Data.objectHeight[objType];
			//		}
			//		int flatObjX = ((objX + objX + objWidth) * gridSize) / 2;
			//		int flatObjY = ((objY + objY + objHeight) * gridSize) / 2;
			//		if (objX >= 0 && objY >= 0 && objX < 96 && objY < 96)
			//		{
			//			gameCamera.addModel(_obj);
			//			_obj.setPosition(flatObjX, -engineHandle.getAveragedElevation(flatObjX, flatObjY), flatObjY);
			//			engineHandle.createObject(objX, objY, objType, objDir);
			//			if (objType == 74)
			//				_obj.offsetPosition(0, -480, 0);
			//		}
			//	}
			//	catch (Exception runtimeexception)
			//	{
			//		Console.WriteLine("Loc Error: " + runtimeexception.ToString());
			//		Console.WriteLine("x:" + j2 + " obj:" + _obj);
			//		//runtimeexception.printStackTrace();
			//	}
			//}

			//for (int wallIndex = 0; wallIndex < wallObjectCount; wallIndex++)
			//{
			//	wallObjectX[wallIndex] -= offsetX;
			//	wallObjectY[wallIndex] -= offsetY;
			//	int wallX = wallObjectX[wallIndex];
			//	int wallY = wallObjectY[wallIndex];
			//	int wallId = wallObjectID[wallIndex];
			//	int wallDir = wallObjectDirection[wallIndex];
			//	try
			//	{
			//		engineHandle.createWall(wallX, wallY, wallDir, wallId);
			//		Models.GameObject wallObject = makeWallObject(wallX, wallY, wallDir, wallId, wallIndex);
			//		wallObjectArray[wallIndex] = wallObject;
			//	}
			//	catch (Exception runtimeexception1)
			//	{
			//		Console.WriteLine("Bound Error: " + runtimeexception1.ToString());
			//	}
			//}

			//for (int k3 = 0; k3 < groundItemCount; k3++)
			//{
			//	groundItemX[k3] -= offsetX;
			//	groundItemY[k3] -= offsetY;
			//}

			//for (int j4 = 0; j4 < playerCount; j4++)
			//{
			//	Mob f1 = playerArray[j4];
			//	f1.currentX -= offsetX * gridSize;
			//	f1.currentY -= offsetY * gridSize;
			//	for (int l5 = 0; l5 <= f1.waypointCurrent; l5++)
			//	{
			//		f1.waypointsX[l5] -= offsetX * gridSize;
			//		f1.waypointsY[l5] -= offsetY * gridSize;
			//	}

			//}

			//for (int i5 = 0; i5 < npcCount; i5++)
			//{
			//	Mob f2 = npcArray[i5];
			//	f2.currentX -= offsetX * gridSize;
			//	f2.currentY -= offsetY * gridSize;
			//	for (int k6 = 0; k6 <= f2.waypointCurrent; k6++)
			//	{
			//		f2.waypointsX[k6] -= offsetX * gridSize;
			//		f2.waypointsY[k6] -= offsetY * gridSize;
			//	}

			//}

			//engineHandle.playerIsAlive = true;
			return true;
		}


		public void drawMinimapMenu(bool canClick)
		{
			int windowWidth = 512, windowHeight = 346;
			int gridSize = 128;

			int x = ((GameImage)(DataLoader.GameGraphics)).gameWidth - 199;
			int tarWidth = 156;//'æ';//(char)234;//'\u234';
			int tarHeight = 152;// '~';//(char)230;//'\u230';
			DataLoader.GameGraphics.drawPicture(x - 49, 3, DataLoader.baseInventoryPic + 2);
			x += 40;
			DataLoader.GameGraphics.drawBox(x, 36, tarWidth, tarHeight, 0);
			DataLoader.GameGraphics.setDimensions(x, 36, x + tarWidth, 36 + tarHeight);
			int j1 = 192 + minimapRandomRotationY;
			int l1 = cameraRotation + minimapRandomRotationX & 0xff;
			int j2 = ((MobManager.MyPlayer.currentX - 6040) * 3 * j1) / 2048;
			int l3 = ((MobManager.MyPlayer.currentY - 6040) * 3 * j1) / 2048;
			int j5 = Camera.rotationDampingValue[1024 - l1 * 4 & 0x3ff];
			int l5 = Camera.rotationDampingValue[(1024 - l1 * 4 & 0x3ff) + 1024];
			int j6 = l3 * j5 + j2 * l5 >> 18;
			l3 = l3 * l5 - j2 * j5 >> 18;
			j2 = j6;
			DataLoader.GameGraphics.drawMinimapPic((x + tarWidth / 2) - j2, 36 + tarHeight / 2 + l3, DataLoader.baseInventoryPic - 1, l1 + 64 & 0xff, j1);
			for (int l7 = 0; l7 < ObjectManager.GameObjects.Count; l7++)
			{
				var go = ObjectManager.GameObjects[l7];
				int k2 = (((go.X * gridSize + 64) - MobManager.MyPlayer.currentX) * 3 * j1) / 2048;
				int i4 = (((go.Y * gridSize + 64) - MobManager.MyPlayer.currentY) * 3 * j1) / 2048;
				int k6 = i4 * j5 + k2 * l5 >> 18;
				i4 = i4 * l5 - k2 * j5 >> 18;
				k2 = k6;
				drawMinimapObject(x + tarWidth / 2 + k2, (36 + tarHeight / 2) - i4, 65535);
			}

			for (int i8 = 0; i8 < ItemManager.GroundItems.Count; i8++)
			{
				var gi = ItemManager.GroundItems[i8];
				int l2 = (((gi.X * gridSize + 64) - MobManager.MyPlayer.currentX) * 3 * j1) / 2048;
				int j4 = (((gi.Y * gridSize + 64) - MobManager.MyPlayer.currentY) * 3 * j1) / 2048;
				int l6 = j4 * j5 + l2 * l5 >> 18;
				j4 = j4 * l5 - l2 * j5 >> 18;
				l2 = l6;
				drawMinimapObject(x + tarWidth / 2 + l2, (36 + tarHeight / 2) - j4, 0xff0000);
			}

			for (int j8 = 0; j8 < MobManager.npcCount; j8++)
			{
				Mob f1 = MobManager.npcArray[j8];
				int i3 = ((f1.currentX - MobManager.MyPlayer.currentX) * 3 * j1) / 2048;
				int k4 = ((f1.currentY - MobManager.MyPlayer.currentY) * 3 * j1) / 2048;
				int i7 = k4 * j5 + i3 * l5 >> 18;
				k4 = k4 * l5 - i3 * j5 >> 18;
				i3 = i7;
				drawMinimapObject(x + tarWidth / 2 + i3, (36 + tarHeight / 2) - k4, 0xffff00);
			}

			for (int k8 = 0; k8 < MobManager.playerCount; k8++)
			{
				Mob f2 = MobManager.playerArray[k8]; // playerArray[k8];
				int j3 = ((f2.currentX - MobManager.MyPlayer.currentX) * 3 * j1) / 2048;
				int l4 = ((f2.currentY - MobManager.MyPlayer.currentY) * 3 * j1) / 2048;
				int j7 = l4 * j5 + j3 * l5 >> 18;
				l4 = l4 * l5 - j3 * j5 >> 18;
				j3 = j7;
				int i9 = 0xffffff;
				// Change color if friend
				/* for (int j9 = 0; j9 < base.friendsCount; j9++)
				{
					if (f2.nameHash != base.friendsList[j9] || base.friendsWorld[j9] != 99)
						continue;
					i9 = 65280;
					break;
				}*/

				drawMinimapObject(x + tarWidth / 2 + j3, (36 + tarHeight / 2) - l4, i9);
			}

			// compass
			DataLoader.GameGraphics.drawCircle(x + tarWidth / 2, 36 + tarHeight / 2, 2, 0xffffff, 255);
			DataLoader.GameGraphics.drawMinimapPic(x + 19, 55, DataLoader.baseInventoryPic + 24, cameraRotation + 128 & 0xff, 128);
			DataLoader.GameGraphics.setDimensions(0, 0, windowWidth, windowHeight + 12);




			/* if (!canClick)
				return;
			l = base.mouseX - (((GameImage)(gameGraphics)).gameWidth - 199);
			int l8 = base.mouseY - 36;
			if (l >= 40 && l8 >= 0 && l < 196 && l8 < 152)
			{
				int c2 = 156;//'\u234';
				int c4 = 152;//'\u230';
				int k1 = 192 + minimapRandomRotationY;
				int i2 = cameraRotation + minimapRandomRotationX & 0xff;
				int i1 = ((GameImage)(gameGraphics)).gameWidth - 199;
				i1 += 40;
				int k3 = ((base.mouseX - (i1 + c2 / 2)) * 16384) / (3 * k1);
				int i5 = ((base.mouseY - (36 + c4 / 2)) * 16384) / (3 * k1);
				int k5 = Camera.rotationDampingValue[1024 - i2 * 4 & 0x3ff];
				int i6 = Camera.rotationDampingValue[(1024 - i2 * 4 & 0x3ff) + 1024];
				int k7 = i5 * k5 + k3 * i6 >> 15;
				i5 = i5 * i6 - k3 * k5 >> 15;
				k3 = k7;
				k3 += ourPlayer.currentX;
				i5 = ourPlayer.currentY - i5;
				if (mouseButtonClick == 1)
					walkTo1Tile(sectionX, sectionY, k3 / 128, i5 / 128, false);
				mouseButtonClick = 0;
			}
			*/
		}

		public void drawMinimapObject(int x, int y, int color)
		{
			DataLoader.GameGraphics.drawMinimapPixel(x, y, color);
			DataLoader.GameGraphics.drawMinimapPixel(x - 1, y, color);
			DataLoader.GameGraphics.drawMinimapPixel(x + 1, y, color);
			DataLoader.GameGraphics.drawMinimapPixel(x, y - 1, color);
			DataLoader.GameGraphics.drawMinimapPixel(x, y + 1, color);
		}


		public override void displayMessage(string s1)
		{
		}

		internal void Disconnect()
		{
			base.Disconnect();
		}

		public static bool TerrainUpdateNecessary { get; set; }
	}
}
