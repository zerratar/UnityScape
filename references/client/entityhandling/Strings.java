package org.rscemulation.client.entityhandling;

import java.util.ArrayList;

public class Strings {

	public static String[] abuseChatA = new String[] {"This form is for reporting players who are breaking our rules", "Using it sends a snapshot of the last 60 secs of activity to us", "If you misuse this form, you will be banned.", "First indicate which of our 12 rules is being broken. For a detailed", "explanation of each rule please read the manual on our website."};
	public static final String[] staffMenu = new String[] {"Mute: ", "Revoke Mute: ", "Temporary Mute: ", "Say-Mute: ", "Revoke Say-Mute: ", "Temporary Say-Mute: ", "Jail: ", "Release: ", "Temporary Jail: ", "Ban: "};
	public static final String[] developerMenuA = new String[] {"Item", "Object", "NPC", "Door", "Landscape (coming soon)"};
	private ArrayList<String> strings;
	public Strings() {
		strings = new ArrayList<String>();
	/**
	* Begin Thieving Strings [POINTER START = 1]
	*/		
		strings.add("You search for traps on the chest");
		strings.add("You find a trap on the chest..");
		strings.add("You disable the trap");
		strings.add("You open the chest");
		strings.add("You find treasure inside!");
		strings.add("This chest has yet to be added to the thieving class.");
		strings.add("You use your skills to disable the trap");
		strings.add("You have activated a trap on the chest!");
		strings.add("There is nothing to steal as this current time");
		strings.add("It looks like that chest has already been looted.");
		strings.add("You do not have a high enough thieving level to steal from this chest.");
		strings.add("This stall has yet to be added to the thieving class.");
		strings.add("You do not have a high enough thieving level to unlock this.");
		strings.add("You fail to unlock the door");
		strings.add("This door has yet to be added to the thiving class.");
		strings.add("You are under attack!");
		strings.add("This Npc has yet to be added to the thieving class.");
	
	/**
	* End Thieving Strings
	*/
	}
	
	public String get(int pointer) {
		return strings.get(pointer);
	}
}