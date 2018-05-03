using System;
using System.Linq;
namespace Assets.RSC.Models
{
	public class Inventory
	{
		public static Inventory Instance { get; set; }

		public InventoryItem[] Items { get; set; }

		static Inventory()
		{
			Instance = new Inventory();
		}

		public Inventory()
		{
			Items = new InventoryItem[30];
		}

		public InventoryItem this[int slot]
		{
			get
			{
				return Items[slot];
			}
			set
			{
				Items[slot] = value;
				if (value == null)
					ReorderItems();
			}
		}

		public void Clear()
		{
			for (var slot = 0; slot < Items.Length; slot++)
				Items[slot] = null;
		}

		public void Add(InventoryItem item)
		{
			var slotIndex = Items.Count(i => i != null);
			Items[slotIndex] = item;
			ReorderItems();
		}

		public void Remove(InventoryItem item)
		{
			var slot = Array.IndexOf(Items, item);
			if (slot >= 0)
				Items[slot] = null;
			ReorderItems();
		}

		public void Remove(int slot)
		{
			if (slot < Items.Length)
				Items[slot] = null;
			ReorderItems();
		}

		private void ReorderItems()
		{
			var invItems = Items.Where(i => i != null).ToList();
			for (int slot = 0; slot < invItems.Count; slot++)
			{
				Items[slot] = invItems[slot];
			}
		}
	}


}
