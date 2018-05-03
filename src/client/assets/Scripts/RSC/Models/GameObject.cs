namespace Assets.RSC.Models
{
	using System.Collections.Generic;

	public class GameObject
	{
		public static List<GameObject> LoadedObjects = new List<GameObject>();

		public int ObjectId { get; set; }

		public GameObject()
		{
		}

		public GameObject(int width, int height)
		{

		}

		public GameObject(sbyte[] modelData, int offset, bool unknown)
		{

		}

		public bool IsGiantCrystal { get; set; }

		public int X { get; set; }

		public int Y { get; set; }
	}
}
