namespace Assets.RSC.Managers
{
	using System.Collections.Generic;
	using System.Linq;

	using Assets.RSC.Models;

	public class MobManager
	{
		public static Player MyPlayer { get; set; }

		public static int playerCount;
		public static int lastPlayerCount;
		public static Mob[] playerBufferArray = new Mob[4000];
		public static Mob[] npcAttackingArray = new Mob[5000];
		public static Mob[] playerArray = new Mob[500];
		public static Mob[] lastPlayerArray = new Mob[500];
		public static Mob[] npcArray = new Mob[500];
		public static Mob[] lastNpcArray = new Mob[500];
		public static int lastNpcCount = 0;
		public static int[] playerBufferArrayIndexes = new int[500];
		public static int npcCount;

		public static Mob GetLastNpc(int serverIndex)
		{
			for (int i1 = 0; i1 < lastNpcCount; i1++)
			{
				if (lastNpcArray[i1].ServerIndex == serverIndex)
				{
					return lastNpcArray[i1];
				}
			}
			return null;
			/*
			var lastNpc = LastNpcList.FirstOrDefault(i => i.ServerIndex == serverIndex);
			if (lastNpc == null)
			{
				lastNpc = new Mob();
				lastNpc.ServerIndex = serverIndex;
				LastNpcList.Add(lastNpc);
			}
			return lastNpc; */
			//  NpcList
		}



		public static Mob GetLastPlayer(int serverIndex)
		{
			for (int i1 = 0; i1 < lastPlayerCount; i1++)
			{
				if (lastPlayerArray[i1] != null)
					if (lastPlayerArray[i1].ServerIndex == serverIndex)
					{
						return lastPlayerArray[i1];
					}
			}
			return new Player();
			//  NpcList
		}


		public static Mob CreateNPC(int index, int x, int y, int sprite, int id)
		{
			if (index > npcAttackingArray.Length) return null;
			if (npcAttackingArray[index] == null)
			{
				npcAttackingArray[index] = new Mob();
				npcAttackingArray[index].ServerIndex = index;
			}
			Mob f1 = npcAttackingArray[index];
			bool flag = false;
			for (int l = 0; l < lastNpcCount; l++)
			{
				if (lastNpcArray[l].ServerIndex != index)
					continue;
				flag = true;
				break;
			}

			if (flag)
			{
				f1.NpcId = id;
				f1.NextSprite = sprite;
				int i1 = f1.WaypointCurrent;
				if (x != f1.WaypointsX[i1] || y != f1.WaypointsY[i1])
				{
					f1.WaypointCurrent = i1 = (i1 + 1) % 10;
					f1.WaypointsX[i1] = x;
					f1.WaypointsY[i1] = y;
				}
			}
			else
			{
				f1.ServerIndex = index;
				f1.WaypointsEndSprite = 0;
				f1.WaypointCurrent = 0;
				f1.WaypointsX[0] = f1.currentX = x;
				f1.WaypointsY[0] = f1.currentY = y;
				f1.NpcId = id;
				f1.NextSprite = f1.CurrentSprite = sprite;
				f1.StepCount = 0;
			}
			npcArray[npcCount++] = f1;
			return f1;
		}
		/* internal static void CreateNPC(int serverIndex, int x, int y, int nextSprite, int type)
		{
			Mob.Create(serverIndex, x, y, nextSprite, type);
		} */

		internal static Mob CreatePlayer(int index, int x, int y, int sprite)
		{
			if (playerBufferArray[index] == null)
			{
				playerBufferArray[index] = new Mob();
				playerBufferArray[index].ServerIndex = index;
				playerBufferArray[index].ServerID = 0;
			}
			Mob existingPlayer = playerBufferArray[index];
			bool flag = false;
			for (int l = 0; l < lastPlayerCount; l++)
			{
				if (lastPlayerArray[l].ServerIndex != index)
					continue;
				flag = true;
				break;
			}

			if (flag)
			{
				existingPlayer.NextSprite = sprite;
				int i1 = existingPlayer.WaypointCurrent;
				if (x != existingPlayer.WaypointsX[i1] || y != existingPlayer.WaypointsY[i1])
				{
					existingPlayer.WaypointCurrent = i1 = (i1 + 1) % 10;
					existingPlayer.WaypointsX[i1] = x;
					existingPlayer.WaypointsY[i1] = y;
				}
			}
			else
			{
				existingPlayer.ServerIndex = index;
				existingPlayer.WaypointsEndSprite = 0;
				existingPlayer.WaypointCurrent = 0;
				existingPlayer.WaypointsX[0] = existingPlayer.currentX = x;
				existingPlayer.WaypointsY[0] = existingPlayer.currentY = y;
				existingPlayer.NextSprite = existingPlayer.CurrentSprite = sprite;
				existingPlayer.StepCount = 0;
			}
			playerArray[playerCount++] = existingPlayer;
			return existingPlayer;
		}
	}
}
