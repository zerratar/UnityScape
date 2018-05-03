package org.rscemulation.client;


final class Mob {

    Mob() {
        waypointsX = new int[10];
        waypointsY = new int[10];
        animationCount = new int[12];
        level = -1;
    }
	/**
	* Appearance Values
	*/
    public int admin;
    public long nameLong;
	public long clanLong = 0;
    public String name;
	public String cName = "";
    public int serverIndex;
    public int appearanceID;
	public int wornItemsID;

    public int currentX;
    public int currentY;
    public int stepCount;
    public int currentSprite;
    public int nextSprite;
    public int waypointEndSprite;
    public int waypointCurrent;
    public int waypointsX[];
    public int waypointsY[];
    public int animationCount[];
    public String lastMessage;
    public int lastMessageTimeout;
    public int type;	
    public int hitPointsCurrent;
    public int hitPointsBase;
    public int combatTimer;
    public int level;
    public int colourHairType;
    public int colourTopType;
    public int colourBottomType;
    public int colourSkinType;
    public int attackingCameraInt;
    public int attackingMobIndex;
    public int attackingNpcIndex;
    public int anInt162;
    public int anInt163;
    public int anInt164;
    public int anInt176;
    public int anInt179;	
}
