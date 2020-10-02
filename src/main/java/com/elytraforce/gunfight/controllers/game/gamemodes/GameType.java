package com.elytraforce.gunfight.controllers.game.gamemodes;

public interface GameType {

	public int getMaxPlayers();
	
	public int getMinPlayers();
	
	public String getDisplayName();
	
	public void startup();
	
	public boolean requiresMaxPlayers();
	
	public Type getType();
	
	enum Type {
		ONE_V_ONE,
	    TWO_V_TWO,
	    THREE_V_THREE,
		TWO_V_TWO_BOMB,
		THREE_V_THREE_BOMB;
	}
	
	
}
