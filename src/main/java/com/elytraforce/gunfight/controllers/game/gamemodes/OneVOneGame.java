package com.elytraforce.gunfight.controllers.game.gamemodes;

import com.elytraforce.gunfight.controllers.game.Duel;

public class OneVOneGame implements GameType{
	
	private Duel duel;
	
	public OneVOneGame(Duel duel) {
		this.duel = duel;
	}

	@Override
	public int getMaxPlayers() {
		return 2;
	}
	
	@Override
	public int getMinPlayers() {
		// TODO Auto-generated method stub
		return 2;
	}

	@Override
	public String getDisplayName() {
		return "2v2";
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
		// TODO Auto-generated method stub
		return Type.ONE_V_ONE;
	}

	

}
