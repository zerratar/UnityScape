namespace Assets.RSC.Models
{
	public class InventoryItem : Item
	{
		public InventoryItem(int itemId, int itemEquipped)
		{
			this.ItemId = itemId;
			this.EquipId = itemEquipped;			
			if (this.EquipId > 0)
				this.IsEquipped = true;			

		}
		public int EquipId { get; set; }
		public bool IsEquipped { get; set; }
		public int Amount { get; set; }

	}
}
