package com.elytraforce.gunfight.controllers.game.gamemodes;

import com.elytraforce.gunfight.controllers.game.Duel;
import com.elytraforce.gunfight.controllers.game.gamemodes.GameType.Type;

public class TwoVTwoBomb implements GameType{

	private Duel duel;
	
	public TwoVTwoBomb(Duel duel) {
		this.duel = duel;
	}

	@Override
	public int getMaxPlayers() {
		// TODO Auto-generated method stub
		return 4;
	}

	@Override
	public int getMinPlayers() {
		// TODO Auto-generated method stub
		return 4;
	}

	@Override
	public String getDisplayName() {
		// TODO Auto-generated method stub
		return "2v2 Bomb";
	}

	@Override
	public void startup() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean requiresMaxPlayers() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Type getType() {
		// TODO Auto-generated method stub
		return Type.TWO_V_TWO;
	}
}
