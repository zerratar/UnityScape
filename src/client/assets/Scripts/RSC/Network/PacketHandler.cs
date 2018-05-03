using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Assets.RSC.Network
{

	using Assets.RSC.IO;
	using Assets.RSC.Managers;
	using Assets.RSC.Models;

	using UnityEngine;

	public class ServerPacketHandler
	{
		/*
			SectionX and SectionY is basically         
			Our starting X,Y. But with nothing better to go with
			The one that is default used is the one we get from starting pos in lumbridge
			(Grabbed from my XNA Client :P for quick ref)
		 */




		public static void SetInventoryItems(sbyte[] packetData, int length)
		{


			var inv = Inventory.Instance;

			inv.Clear();

			int off = 1;
			var inventoryItemsCount = packetData[off++] & 0xff;
			for (int item = 0; item < inventoryItemsCount; item++)
			{
				int data = DataOperations.getShort(packetData, off);
				off += 2;

				var itemIndex = data & 0x7fff;
				var invItem = new InventoryItem(itemIndex, data / 32768);

				invItem.ItemPictureIndex = DataLoader.baseItemPicture + Data.itemInventoryPicture[itemIndex];
				invItem.ItemPictureMask = Data.itemPictureMask[itemIndex];

				// inventoryItems[item] = data & 0x7fff;
				// inventoryItemEquipped[item] = data / 32768;
				if (Data.itemStackable[data & 0x7fff] == 0)
				{
					invItem.Amount = DataOperations.getInt(packetData, off);
					// inventoryItemCount[item] = DataOperations.getInt(packetData, off);
					off += 4;
				}
				else
				{
					invItem.Amount = 1;
					// inventoryItemCount[item] = 1;
				}

				inv.Add(invItem);
			}
		}

		public static void UpdateItemPositionData(sbyte[] packetData, int length)
		{

			/* if (needsClear)
			{
				for (int i = 0; i < groundItemID.Length; i++)
				{
					groundItemX[i] = -1;
					groundItemY[i] = -1;
					groundItemID[i] = -1;
					groundItemObjectVar[i] = -1;
				}
				groundItemCount = 0;
				needsClear = false;
			} */



			//for (int off = 1; off < length; )
			//	if (DataOperations.getByte(packetData[off]) == 255)
			//	{
			//		int newCount = 0;
			//		int newSectionX = SectionX + packetData[off + 1] >> 3;
			//		int newSectionY = SectionY + packetData[off + 2] >> 3;
			//		off += 3;
			//		for (int groundItem = 0; groundItem < groundItemCount; groundItem++)
			//		{
			//			int newX = (groundItemX[groundItem] >> 3) - newSectionX;
			//			int newY = (groundItemY[groundItem] >> 3) - newSectionY;
			//			if (newX != 0 || newY != 0)
			//			{
			//				if (groundItem != newCount)
			//				{
			//					groundItemX[newCount] = groundItemX[groundItem];
			//					groundItemY[newCount] = groundItemY[groundItem];
			//					groundItemID[newCount] = groundItemID[groundItem];
			//					groundItemObjectVar[newCount] = groundItemObjectVar[groundItem];
			//				}
			//				newCount++;
			//			}
			//		}

			//		groundItemCount = newCount;
			//	}
			//	else
			//	{
			//		int newID = DataOperations.getShort(packetData, off);
			//		off += 2;
			//		int newX = SectionX + packetData[off++];
			//		int newY = SectionY + packetData[off++];
			//		if ((newID & 0x8000) == 0)
			//		{
			//			groundItemX[groundItemCount] = newX;
			//			groundItemY[groundItemCount] = newY;
			//			groundItemID[groundItemCount] = newID;
			//			groundItemObjectVar[groundItemCount] = 0;

			//			for (int l23 = 0; l23 < objectCount; l23++)
			//			{
			//				if (objectX[l23] != newX || objectY[l23] != newY)
			//					continue;
			//				groundItemObjectVar[groundItemCount] = Data.Data.objectGroundItemVar[objectType[l23]];
			//				break;
			//			}

			//			groundItemCount++;
			//		}
			//		else
			//		{
			//			newID &= 0x7fff;
			//			int updateIndex = 0;
			//			for (int currentItemIndex = 0; currentItemIndex < groundItemCount; currentItemIndex++)
			//				if (groundItemX[currentItemIndex] != newX || groundItemY[currentItemIndex] != newY || groundItemID[currentItemIndex] != newID)
			//				{
			//					if (currentItemIndex != updateIndex)
			//					{
			//						groundItemX[updateIndex] = groundItemX[currentItemIndex];
			//						groundItemY[updateIndex] = groundItemY[currentItemIndex];
			//						groundItemID[updateIndex] = groundItemID[currentItemIndex];
			//						groundItemObjectVar[updateIndex] = groundItemObjectVar[currentItemIndex];
			//					}
			//					updateIndex++;
			//				}
			//				else
			//				{
			//					newID = -123;
			//				}

			//			groundItemCount = updateIndex;
			//		}
			//	}

		}

		public static void UpdateNpcPositionData(sbyte[] data, int length)
		{
			try
			{
				MobManager.lastNpcCount = MobManager.npcCount;
				MobManager.npcCount = 0;

				for (int lastNpcIndex = 0; lastNpcIndex < MobManager.lastNpcCount; lastNpcIndex++)
					MobManager.lastNpcArray[lastNpcIndex] = MobManager.npcArray[lastNpcIndex];

				//	MobManager.LastNpcList = MobManager.NpcList.ToList(); // .ToList() to force it to make it into a copy

				//	MobManager.NpcList.Clear();


				int newNpcOffset = 8;
				int newNpcCount = DataOperations.getBits(data, newNpcOffset, 16);

				newNpcOffset += 16;

				for (int newNpcIndex = 0; newNpcIndex < newNpcCount; newNpcIndex++)
				{
					int serverIndex = DataOperations.getBits(data, newNpcOffset, 16);
					Mob newNPC = MobManager.GetLastNpc(serverIndex);
					newNpcOffset += 16;
					int npcNeedsUpdate = DataOperations.getBits(data, newNpcOffset, 1);
					newNpcOffset++;
					if (npcNeedsUpdate != 0)
					{
						int i32 = DataOperations.getBits(data, newNpcOffset, 1);
						newNpcOffset++;
						if (i32 == 0)
						{
							int nextSprite = DataOperations.getBits(data, newNpcOffset, 3);
							newNpcOffset += 3;
							int waypointCurrent = newNPC.WaypointCurrent;
							int waypointX = newNPC.WaypointsX[waypointCurrent];
							int waypointY = newNPC.WaypointsY[waypointCurrent];
							if (nextSprite == 2 || nextSprite == 1 || nextSprite == 3)
								waypointX += 128;
							if (nextSprite == 6 || nextSprite == 5 || nextSprite == 7)
								waypointX -= 128;
							if (nextSprite == 4 || nextSprite == 3 || nextSprite == 5)
								waypointY += 128;
							if (nextSprite == 0 || nextSprite == 1 || nextSprite == 7)
								waypointY -= 128;
							newNPC.NextSprite = nextSprite;
							newNPC.WaypointCurrent = waypointCurrent = (waypointCurrent + 1) % 10;
							newNPC.WaypointsX[waypointCurrent] = waypointX;
							newNPC.WaypointsY[waypointCurrent] = waypointY;
						}
						else
						{
							int nextSpriteOffset = DataOperations.getBits(data, newNpcOffset, 4);
							newNpcOffset += 4;
							if ((nextSpriteOffset & 0xc) == 12)
							{
								continue;
							}
							newNPC.NextSprite = nextSpriteOffset;
						}
					}

					MobManager.npcArray[MobManager.npcCount++] = newNPC;
					//Mudclient.npcCount++;
				}



				while (newNpcOffset + 34 < length * 8)
				{
					int serverIndex = DataOperations.getBits(data, newNpcOffset, 16);
					newNpcOffset += 16;
					int offsetX = DataOperations.getBits(data, newNpcOffset, 5);
					newNpcOffset += 5;
					if (offsetX > 15)
						offsetX -= 32;
					int offsetY = DataOperations.getBits(data, newNpcOffset, 5);
					newNpcOffset += 5;
					if (offsetY > 15)
						offsetY -= 32;
					int nextSprite = DataOperations.getBits(data, newNpcOffset, 4);
					newNpcOffset += 4;
					int x = (Mudclient.SectionX + offsetX) * 128 + 64;
					int y = (Mudclient.SectionY + offsetY) * 128 + 64;
					int type = DataOperations.getBits(data, newNpcOffset, 10);
					newNpcOffset += 10;

					if (type >= Data.npcCount)
						type = 24;

					MobManager.CreateNPC(serverIndex, x, y, nextSprite, type);

					/* addNPC(serverIndex, x, y, nextSprite, type); */
				}
			}
			catch { }
		}

		public static void UpdatePlayerPositionData(sbyte[] packetData, int length)
		{
			try
			{
				if (!Mudclient.hasWorldInfo)
					return;

				MobManager.lastPlayerCount = MobManager.playerCount;
				for (int k = 0; k < MobManager.lastPlayerCount; k++)
					MobManager.lastPlayerArray[k] = MobManager.playerArray[k];

				int off = 8;
				Mudclient.SectionX = DataOperations.getBits(packetData, off, 11);
				off += 11;
				Mudclient.SectionY = DataOperations.getBits(packetData, off, 13);
				off += 13;
				int sprite = DataOperations.getBits(packetData, off, 4);

				off += 4;

				// boolean sectionLoaded = loadSection(sectionX, sectionY);
				Debug.Log("Before Load");

				var sectionLoaded = Mudclient.LoadSection(Mudclient.SectionX, Mudclient.SectionY);

				Mudclient.SectionX -= Mudclient.AreaX;
				Mudclient.SectionY -= Mudclient.AreaY;

				int mapEnterX = Mudclient.SectionX * 128 + 64;
				int mapEnterY = Mudclient.SectionY * 128 + 64;



				if (sectionLoaded)
				{
					Mudclient.TerrainUpdateNecessary = true;

					if (MobManager.MyPlayer == null)
						MobManager.MyPlayer = new Player();

					MobManager.MyPlayer.CurrentSprite = sprite;
					MobManager.MyPlayer.currentX = mapEnterX;
					MobManager.MyPlayer.currentY = mapEnterY;

					// MobManager.MyPlayer.SectionX
				}

				MobManager.playerCount = 0;


				int newPlayerCount = DataOperations.getBits(packetData, off, 16);
				// off += 8;
				off += 16;
				for (int currentNewPlayer = 0; currentNewPlayer < newPlayerCount; currentNewPlayer++)
				{
					//Mob mob = lastPlayerArray[currentNewPlayer + 1];
					Mob mob = MobManager.GetLastPlayer(DataOperations.getBits(packetData, off, 16));
					off += 16;
					int playerAtTile = DataOperations.getBits(packetData, off, 1);
					off++;
					if (playerAtTile != 0)
					{
						int waypointsLeft = DataOperations.getBits(packetData, off, 1);
						off++;
						if (waypointsLeft == 0)
						{
							int currentNextSprite = DataOperations.getBits(packetData, off, 3);
							off += 3;
							int currentWaypoint = mob.WaypointCurrent;
							int newWaypointX = mob.WaypointsX[currentWaypoint];
							int newWaypointY = mob.WaypointsY[currentWaypoint];
							if (currentNextSprite == 2 || currentNextSprite == 1 || currentNextSprite == 3)
								newWaypointX += Mudclient.gridSize;
							if (currentNextSprite == 6 || currentNextSprite == 5 || currentNextSprite == 7)
								newWaypointX -= Mudclient.gridSize;
							if (currentNextSprite == 4 || currentNextSprite == 3 || currentNextSprite == 5)
								newWaypointY += Mudclient.gridSize;
							if (currentNextSprite == 0 || currentNextSprite == 1 || currentNextSprite == 7)
								newWaypointY -= Mudclient.gridSize;
							mob.NextSprite = currentNextSprite;
							mob.WaypointCurrent = currentWaypoint = (currentWaypoint + 1) % 10;
							mob.WaypointsX[currentWaypoint] = newWaypointX;
							mob.WaypointsY[currentWaypoint] = newWaypointY;
						}
						else
						{
							int needsNextSprite = DataOperations.getBits(packetData, off, 4);
							off += 4;
							if ((needsNextSprite & 0xc) == 12)
							{
								continue;
							}
							mob.NextSprite = needsNextSprite;
						}
					}
					MobManager.playerArray[MobManager.playerCount++] = mob;
				}

				int mobCount = 0;
				while (off + 24 < length * 8)
				{
					int mobIndex = DataOperations.getBits(packetData, off, 16);
					off += 16;
					int areaMobX = DataOperations.getBits(packetData, off, 5);
					off += 5;
					if (areaMobX > 15)
						areaMobX -= 32;
					int areaMobY = DataOperations.getBits(packetData, off, 5);
					off += 5;
					if (areaMobY > 15)
						areaMobY -= 32;
					int mobSprite = DataOperations.getBits(packetData, off, 4);
					off += 4;
					int addIndex = DataOperations.getBits(packetData, off, 1);
					off++;
					int mobX = (Mudclient.SectionX + areaMobX) * Mudclient.gridSize + 64;
					int mobY = (Mudclient.SectionY + areaMobY) * Mudclient.gridSize + 64;
					MobManager.CreatePlayer(mobIndex, mobX, mobY, mobSprite);
					if (addIndex == 0)
						MobManager.playerBufferArrayIndexes[mobCount++] = mobIndex;
				}
				if (mobCount > 0)
				{
					Mudclient.Instance.streamClass.createPacket(74);
					Mudclient.Instance.streamClass.addShort(mobCount);
					for (int currentMob = 0; currentMob < mobCount; currentMob++)
					{
						Mob dummyMob = MobManager.playerBufferArray[MobManager.playerBufferArrayIndexes[currentMob]];
						Mudclient.Instance.streamClass.addShort(dummyMob.ServerIndex);
						Mudclient.Instance.streamClass.addShort(dummyMob.WornItemsID);
					}
					Mudclient.Instance.streamClass.formatPacket();

					Mudclient.Instance.streamClass.createPacket(14); // 83
					Mudclient.Instance.streamClass.addShort(mobCount);
					for (int k40 = 0; k40 < mobCount; k40++)
					{
						Mob f5 = MobManager.playerBufferArray[MobManager.playerBufferArrayIndexes[k40]];
						Mudclient.Instance.streamClass.addShort(f5.ServerIndex);
						Mudclient.Instance.streamClass.addShort(f5.ServerID);
					}

					Mudclient.Instance.streamClass.formatPacket();
					mobCount = 0;
				}

			}
			catch (Exception exc) { Debug.LogException(exc); }
			
		}

		internal static void UpdatePlayerSkills(sbyte[] packetData, int length)
		{
			if (MobManager.MyPlayer == null)
				MobManager.MyPlayer = new Player();

			int off = 1;
			for (int stat = 0; stat < Mudclient.SkillName.Length; stat++)
			{
				MobManager.MyPlayer.StatCurrent[stat] = DataOperations.getByte(packetData[off++]);
			}
			// off++;
			for (int stat = 0; stat < Mudclient.SkillName.Length; stat++)
			{
				MobManager.MyPlayer.StatBase[stat] = DataOperations.getByte(packetData[off++]);
			}
			// off++;
			for (int stat = 0; stat < Mudclient.SkillName.Length; stat++)
			{
				MobManager.MyPlayer.StatExp[stat] = DataOperations.getInt(packetData, off); //get int
				off += 4;
			}
		}

		internal static void UpdatePlayerAppearance(sbyte[] packetData, int length)
		{
			int newMobCount = DataOperations.getShort(packetData, 1);
			int off = 3;
			for (int current = 0; current < newMobCount; current++)
			{
				int index = DataOperations.getShort(packetData, off);
				off += 2;
				if (index < 0)// || index > playerBufferArray.Length)
					return;



				Mob mob = MobManager.GetLastPlayer(index);// playerBufferArray[index];
				if (mob == null)
					return;

				mob.HairColour = packetData[off++] & 0xff;
				mob.TopColour = packetData[off++] & 0xff;
				mob.BottomColour = packetData[off++] & 0xff;
				mob.SkinColour = packetData[off++] & 0xff;
				mob.Level = packetData[off++] & 0xff;
				mob.PlayerSkulled = packetData[off++] & 0xff;
				mob.IsAdmin = packetData[off++] & 0xff;
				// var isadmin = // TODO to skip the admin flag (should it be removed)
			}
		}

		internal static void InitiateServerInfo(sbyte[] packetData, int length)
		{
			if (MobManager.MyPlayer == null)
				MobManager.MyPlayer = new Player();


			Mudclient.loadArea = true;
			MobManager.MyPlayer.ServerIndex = DataOperations.getUnsigned2Bytes(packetData, 1);
			Mudclient.wildX = DataOperations.getUnsigned2Bytes(packetData, 3);
			Mudclient.wildY = DataOperations.getUnsigned2Bytes(packetData, 5);
			Mudclient.layerIndex = DataOperations.getUnsigned2Bytes(packetData, 7);
			Mudclient.layerModifier = DataOperations.getUnsigned2Bytes(packetData, 9);
			Mudclient.wildY -= Mudclient.layerIndex * Mudclient.layerModifier;
			Mudclient.needsClear = true;
			Mudclient.hasWorldInfo = true;
		}
	}

	public class ClientPacketHandler
	{

		public static void LoginTest(byte[] data, int length)
		{
			int offset = 1;

			var nameHashByte = DataOperations.getByte(data, offset++);

			var pingPacket = BitConverter.ToString(data, offset);

			// var reconnecting = DataOperations.getByte(data, offset++);
			// var clientVersion = DataOperations.getShort(data, offset);
			/*
				Rest of packet is encrypted
			 */
		}
		public static void Login(byte[] data, int length)
		{
			int offset = 1;
			var reconnecting = DataOperations.getByte(data, offset++);
			var clientVersion = DataOperations.getShort(data, offset);

			/* -- First bytes are always:
				75
				0
				0
				155
				41
				165
				20
				232
				64			 
			 */


			/*
				Rest of packet is encrypted
			 */
		}
		public static List<Point> WalkTo(byte[] data, int length)
		{
			try
			{
				var wayPoints = new List<Point>();
				var packIndex = 1;

				if (data.Length < 4) return wayPoints;




				var startXAreaX = DataOperations.getShort(data, packIndex); // BitConverter.ToInt16(data, packIndex) & 0xff;
				packIndex += 2;

				var startYAreaY = DataOperations.getShort(data, packIndex);//BitConverter.ToInt16(data, packIndex + 2) & 0xff;
				packIndex += 2;

				var areaX = startXAreaX - Mudclient.SectionX;
				var areaY = startYAreaY - Mudclient.SectionY;

				var p = new Point();//ServerPacketHandler.SectionX + areaX, ServerPacketHandler.SectionY + areaY);
				p.X = startXAreaX;
				p.Y = startYAreaY;
				wayPoints.Add(p);


				var left = (data.Length) - packIndex;

				if (left % 2 == 0)
				{
					for (int i = 0; i < left; i++)
					{
						var x = DataOperations.getByte(data, packIndex + i);
						var y = DataOperations.getByte(data, packIndex + i); //data[packIndex + (++i)];
						var po = new Point();
						po.X = x + Mudclient.SectionX;
						po.Y = y + Mudclient.SectionY;
						wayPoints.Add(po);
					}
				}
				return wayPoints;
			}
			catch
			{
				return new List<Point>();
			}
		}

		public static GroundItem GetDroppedItemData(byte[] data, int length)
		{
			return new GroundItem()
			{
				//ID = BitConverter.ToInt32(data, 0) & 0xff,
				//xCoordinate = BitConverter.ToInt16(data, 3) & 0xff,
				//yCoordinate = BitConverter.ToInt16(data, 7) & 0xff
			};
		}
	}
}
