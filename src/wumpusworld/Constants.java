package wumpusworld;


public class Constants {

    private Constants() {
            // restrict instantiation
    }

    protected static final float REWARD_EATEN = -1.0f;
    protected static final float REWARD_GOLD = 1.0f;
    protected static final float REWARD_PIT = -0.5f;
    protected static final float REWARD_WUMPUS_KILLED = 0.1f;
    protected static final float REWARD_ARROW_MISSED = -0.1f;
    protected static final float REWARD_EXPLORED_TILE = 0.2f;
    protected static final float REWARD_BUMPING_INTO_WALL = -0.1f;
    protected static final float REWARD_FIRING_WITHOUT_AMMO = -0.1f;
    protected static final float REWARD_TURNING = -0.01f;
    
    protected static final double EPSILON = 0.01; // Exploration factor E-Greedy technique
    
    protected static final float ALPHA = 0.1f;
    protected static final float GAMMA = 0.5f;
    
	// Neighbours and extended neighbour offsets
    protected static final int[][] N_OFFSETS = { { 1, 0 }, { 0, 1 }, { -1, 0 }, { 0, -1 } };
    protected static final int[][] EN_OFFSETS = { { 2, 0 }, { 1, 1 }, { 0, 2 }, { -1, 1 }, { -2, 0 }, { -1, -1 },
			{ 0, -2 }, { 1, -1 } };
}