package com.elytraforce.gunfight.controllers.game.gamemodes;

import com.elytraforce.gunfight.controllers.game.Duel;

public class ThreeVThreeGame implements GameType{
	
	private Duel duel;
	
	public ThreeVThreeGame(Duel duel) {
		this.duel = duel;
	}

	@Override
	public int getMaxPlayers() {
		// TODO Auto-generated method stub
		return 6;
	}

	@Override
	public int getMinPlayers() {
		// TODO Auto-generated method stub
		return 6;
	}

	@Override
	public String getDisplayName() {
		// TODO Auto-generated method stub
		return "3v3";
	}

	@Override
	public void startup() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean requiresMaxPlayers() {
		return false;
	}

	@Override
	public Type getType() {
		return Type.THREE_V_THREE;
	}

}
