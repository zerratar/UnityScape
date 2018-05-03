namespace Assets.RSC.Models
{
	using Assets.RSC.IO;

	using UnityEngine;

	public class Mob
	{
		public int WornItemsID;

		public int ServerID;

		public int ServerIndex { get; set; }

		public int X { get; set; }

		public int Y { get; set; }


		public int currentX { get; set; }

		public int currentY { get; set; }

		public int Type { get; set; }

		public int WaypointCurrent { get; set; }

		public int NextSprite { get; set; }

		public int CurrentSprite { get; set; }

		public int[] WaypointsX { get; set; }

		public int[] WaypointsY { get; set; }

		public string Name
		{
			get
			{
				if (Data.npcName != null)
				{
					return Data.npcName[Type];
				}
				return "DATA NOT LOADED";
			}
		}

		public Texture2D NextSpriteImage
		{
			get
			{
				try
				{
					if (DataLoader.LoadedSprites != null)
					{
						return DataLoader.LoadedSprites[NextSprite];
					}
				}
				catch
				{
				}
				return null;
			}
		}

		public int HairColour { get; set; }

		public int TopColour { get; set; }

		public int BottomColour { get; set; }

		public int SkinColour { get; set; }

		public int Level { get; set; }

		public int PlayerSkulled { get; set; }

		public int IsAdmin { get; set; }

		public int NpcId { get; set; }

		public int WaypointsEndSprite { get; set; }

		public int StepCount { get; set; }

		public override string ToString()
		{
			return this.Name + " - sid " + this.ServerIndex + " - id " + this.Type;
		}

		public Mob()
		{
			WaypointsX = new int[500];
			WaypointsY = new int[500];
		}

		internal static Mob Create(int serverIndex, int x, int y, int nextSprite, int type)
		{
			var mob = new Mob();
			mob.ServerIndex = serverIndex;
			mob.X = x;
			mob.Y = y;
			mob.NextSprite = nextSprite;
			mob.Type = type;
			return mob;
		}
	}
}
