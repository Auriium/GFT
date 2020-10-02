package com.elytraforce.gunfight.controllers.game.gamemodes;

public interface GameType {

	public int getMaxPlayers();
	
	public int getMinPlayers();
	
	public String getDisplayName();
	
	public void startup();
	
	public boolean requiresMaxPlayers();
	
	public Type getType();
	
	enum Type {
		ONE_V_ONE(new OneVOneGame(null)),
		TWO_V_TWO(new TwoVTwoGame(null)),
	    THREE_V_THREE(new ThreeVThreeGame(null)),
		TWO_V_TWO_BOMB(new TwoVTwoBomb(null)),
		THREE_V_THREE_BOMB(new ThreeVThreeBomb(null));
		
		private GameType type;
		
		Type(GameType type) {
			this.type = type;
		}
		
		public GameType getGameType() {
			return type;
		}
	}
	
	
}
