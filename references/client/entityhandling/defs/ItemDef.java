package org.rscemulation.client.entityhandling.defs;

public class ItemDef extends EntityDef {
	public String command;
	public int basePrice;
	public int sprite;
	public boolean stackable;
	public boolean wieldable;
	public int pictureMask;
	public boolean quest;
	
	public ItemDef(String name, String description, String command, int basePrice, int sprite, boolean stackable, boolean wieldable, int pictureMask, boolean quest, int id) {
		super(name, description, id);
		this.command = command;
		this.basePrice = basePrice;
		this.sprite = sprite;
		this.stackable = stackable;
		this.wieldable = wieldable;
		this.pictureMask = pictureMask;
		this.quest = quest;
		this.id = id;
	}
	
	public String getCommand() {
		return command;
	}

	public int getSprite() {
		return sprite;
	}

	public int getBasePrice() {
		return basePrice;
	}
	public boolean isStackable() {
		return stackable;
	}
	public boolean isWieldable() {
		return wieldable;
	}
	public int getPictureMask() {
		if (id == 1335 || id == 1336)
			return (int)(Math.random() * 167772150);
		else
			return pictureMask;
	}
}
