namespace Assets.RSC.Models
{
	using Assets.RSC.IO;

	using UnityEngine;

	public class Item
	{
		public int ItemId { get; set; }

		public string Name
		{
			get
			{
				if (Data.itemName != null && Data.itemName.Length > ItemId)
				{
					return Data.itemName[ItemId];
				}
				return "DATA NOT LOADED";
			}
		}

		public string Description
		{
			get
			{
				if (Data.itemDescription != null && Data.itemDescription.Length > ItemId)
				{
					return Data.itemDescription[ItemId];
				}
				return "DATA NOT LOADED";
			}
		}
		public bool Stackable { get; set; }

		public Texture2D Image
		{
			get
			{
				if (ItemPictureIndex == 0)
					ItemPictureIndex = DataLoader.baseItemPicture + ItemId;

				if (DataLoader.LoadedIcons != null && DataLoader.LoadedIcons.ContainsKey(ItemPictureIndex))
				{
					return DataLoader.LoadedIcons[ItemPictureIndex];
				}
				return null;
			}
		}

		public int ItemPictureIndex { get; set; }
		public int ItemPictureMask { get; set; }
	}
}
