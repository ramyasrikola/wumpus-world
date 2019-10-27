package wumpusworld;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Random;
import static wumpusworld.Constants.*;

public class MyAgent {
    private static final String Q_FILE_PATH = "Q.dat";
    
    private static final int TOTAL_ACTIONS = 4;
    private World previous_world;

	public enum ACTION {
		FORWARD(0, World.A_MOVE), TURN_LEFT(1, World.A_TURN_LEFT), TURN_RIGHT(2, World.A_TURN_RIGHT),
		SHOOT(3, World.A_SHOOT);

		private int id;
		private String action;

		ACTION(int id, String a) {
			this.id = id;
			this.action = a;
		}

		public int getID() {
			return id;
		}

		public String getAction() {
			return this.action;
		}
		
		public static String getActionFromId(int id) {
		    for (ACTION action : values()) {
		        if (action.id == id) {
		            return action.action;
		        }
		    }
		    return ""; // TODO
		}
	}

	public enum TILE_TYPE {
		WALL(0), NORMAL(1), UNEXPLORED(3), PIT(4), WUMPUS(5);
		
		private int id;
		
		TILE_TYPE(int id) {
			this.id = id;
		}
		
		public int getID() {
			return id;
		}
	}
	
	public enum PERCEPT {
		BREEZY(1), STENCH(2);
		
		private int id;
		
		PERCEPT(int id) {
			this.id = id;
		}
		
		public int getID() {
			return id;
		}
	}
    
    public static class State implements Serializable {
    	
		private static final long serialVersionUID = 1L;

        public int direction;
        public int percepts;
        public int type;
        public int[] neighbour_type = new int[4];
        public int[] neighbour_percepts = new int[4];
        public int[] n2n_type = new int[8];
        public int[] n2n_percepts = new int[8];
        public boolean wumpus_alive;
        public boolean has_arrow;
        
        public State() {
            
        }
        
		public void loadStateFromWorld(World w) {
			
	        int x = w.getPlayerX(); 
	        int y = w.getPlayerY();

	        this.direction = (byte) w.getDirection();
	        if (w.hasBreeze(x, y))
	            this.percepts = PERCEPT.BREEZY.getID();
	        if (w.hasStench(x, y))
	            this.percepts += PERCEPT.STENCH.getID() << 4;
	        this.has_arrow = w.hasArrow();
	        this.wumpus_alive = w.wumpusAlive();
	        
	        if (w.hasPit(x, y))
	            this.type = TILE_TYPE.PIT.getID();
	        if (w.hasWumpus(x, y))
	            this.type += TILE_TYPE.WUMPUS.getID() << 4;
	        
	        // Check type and hazards of neighbours.
	        for (int i = 0; i < 4; ++i) {
	            int nx = x + N_OFFSETS[i][0];
	            int ny = y + N_OFFSETS[i][1];
	            
	            if (w.isValidPosition(nx, ny)) {
	                if (!w.isUnknown(nx, ny)) {
	                    this.neighbour_type[i] = TILE_TYPE.NORMAL.getID();
	                    
	                    if (w.hasPit(nx, ny))
	                        this.neighbour_type[i] += TILE_TYPE.PIT.getID() << 4;
	                    if (w.hasWumpus(nx, ny))
	                        this.neighbour_type[i] += TILE_TYPE.WUMPUS.getID() << 5;
	                    
	                    if (w.hasBreeze(nx, ny))
	                        this.neighbour_percepts[i] = PERCEPT.BREEZY.getID();
	                    if (w.hasStench(nx, ny))
	                        this.neighbour_percepts[i] += PERCEPT.STENCH.getID() << 4;
	                    
	                } else {
	                    this.neighbour_type[i] = TILE_TYPE.UNEXPLORED.getID();
	                }
	            } else {
	                this.neighbour_type[i] = TILE_TYPE.WALL.getID();
	            }
	        }
	        
	        // Check percepts in our neighbours' neighbours.
	        for (int i = 0; i < 8; ++i) {
	            int nx = x + EN_OFFSETS[i][0];
	            int ny = y + EN_OFFSETS[i][1];
	            
	            if (w.isValidPosition(nx, ny)) {
	                if (!w.isUnknown(nx, ny)) {
	                    this.n2n_type[i] = TILE_TYPE.NORMAL.getID();
	                    
	                    if (w.hasPit(nx, ny))
	                        this.n2n_type[i] += TILE_TYPE.PIT.getID() << 4;
	                    if (w.hasWumpus(nx, ny))
	                        this.n2n_type[i] += TILE_TYPE.WUMPUS.getID() << 5;
	                    
	                    if (w.hasBreeze(nx, ny))
	                        this.n2n_percepts[i] = PERCEPT.BREEZY.getID();
	                    if (w.hasStench(nx, ny))
	                        this.n2n_percepts[i] += PERCEPT.STENCH.getID() << 4;
	                } else {
	                    this.n2n_type[i] = TILE_TYPE.UNEXPLORED.getID();
	                    this.n2n_percepts[i] = 0;
	                }
	            } else {
	                this.n2n_type[i] = TILE_TYPE.WALL.getID();;
	                this.n2n_percepts[i] = 0;
	            }
	        }
		}
		
        @Override
        public String toString() {
            return Integer.toString(hashCode());
        }

        @Override
        public int hashCode() {
        	return Objects.hash(
        	    this.direction , this.percepts , this.type,
        	    Arrays.hashCode(this.neighbour_type), Arrays.hashCode(this.neighbour_percepts),
        	    Arrays.hashCode(this.n2n_type), this.wumpus_alive, this.has_arrow
        	);
        }

        @Override
        public boolean equals(Object obj) {

            if (obj instanceof State) {
              State other = (State) obj; 
              return Objects.equals(direction, other.direction) && Objects.equals(percepts, other.percepts) &&
                  Objects.equals(type, other.type) && Arrays.equals(neighbour_type, other.neighbour_type) &&
                  Arrays.equals(neighbour_percepts, other.neighbour_percepts) && Arrays.equals(n2n_type, other.n2n_type) &&
                  Arrays.equals(n2n_percepts, other.n2n_percepts) && Objects.equals(wumpus_alive, other.wumpus_alive) &&
                  Objects.equals(has_arrow, other.has_arrow);
            }
            return false;
        }
    }
    
    private World w;
    private HashMap<State, double[]> Q;
    private boolean writeQOnGameEnd;
    
    public MyAgent(World world) {
        w = world;
        Q = readQTable();
        writeQOnGameEnd = false;
    }
    
    public MyAgent(World world, HashMap<State, double[]> Q) {
        w = world;
        this.Q = Q;
        writeQOnGameEnd = false;
    }
    
    public void checkDefaultActions(int x, int y) {
    	if (w.hasGlitter(x, y)) {
            w.doAction(World.A_GRAB);
        } else if (w.hasPit(x, y)) {
            w.doAction(World.A_CLIMB);
        }
    }
    
    
    public void doAction() {
        
        // Check the world first if the game is over then return
        if (w.gameOver())
        {
            return;
        }
        
        int currentX = w.getPlayerX();
        int currentY = w.getPlayerY();
        
        checkDefaultActions(currentX, currentY);
        
        // Find the best action to do in our current state.
       
        State s1 = new State();
        s1.loadStateFromWorld(w);

        double[] possible_actions;

        //lookup the Q table for actions posible for given loaded state
        possible_actions = (Q.containsKey(s1)) ?  Q.get(s1) : new double[TOTAL_ACTIONS] ;
        Q.put(s1,possible_actions);

        int best_action = mostOptimalAction(possible_actions);

         //create a cloned previous world before performing optimal action
        this.previous_world = w.clone();
        
        // Do the current best action.
        w.doAction(ACTION.getActionFromId(best_action));

        
        int nextX = w.getPlayerX();
        int nextY = w.getPlayerY();
        
        checkDefaultActions(nextX, nextY);

        // after making the action, find out if we are rewarded in the new state.
        State s2 = new State();
        s2.loadStateFromWorld(w);
        double reward = getReward(best_action);

        
        double[] possible_actions2;
        
        //lookup the Q table for actions posible for given loaded state
        possible_actions2 = (Q.containsKey(s2)) ?  Q.get(s2) : new double[TOTAL_ACTIONS] ;
        Q.put(s2,possible_actions2);
        
        
        // Calculate the new Q-value for the taken action.
        double max = Double.NEGATIVE_INFINITY;
        for (int a2 = 0; a2 < possible_actions2.length; ++a2)
            max = Math.max(max, possible_actions2[a2]);
        possible_actions[best_action] = possible_actions[best_action] + ALPHA * (reward + GAMMA * max - possible_actions[best_action]);
        
        // If the game has ended, write the Q matrix to file.
        if (w.gameOver())
        {
            if (writeQOnGameEnd)
                saveQTable(Q);
        }
        
    }
    
    private int mostOptimalAction(double[] qValues) {
        ArrayList<Integer> optimal = new ArrayList<>();
        
        double max = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < qValues.length; ++i) {
           if (qValues[i] > max) {
        	   optimal.clear();
        	   optimal.add(i);
        	   max = qValues[i];
           }
           else if (qValues[i] == max) {
        	   optimal.add(i);
           }
         }
        
        
        Random random = new Random();
        // E-Greedy approach
        if (random.nextDouble() <= (1 - EPSILON)) 
            return optimal.get(random.nextInt(optimal.size()));
        else
            return random.nextInt(qValues.length);
    }
    
    private double getReward( int action) {
        
        //loads the cloned world before best_action performed
        World previous = this.previous_world; 
        
        int currentX = w.getPlayerX();
        int currentY = w.getPlayerY();
        
        int prevX = previous.getPlayerX();
        int prevY = previous.getPlayerY();
        
        
        if (action == ACTION.TURN_LEFT.getID() || action == ACTION.TURN_RIGHT.getID())
            return REWARD_TURNING;
        if (action == ACTION.FORWARD.getID() && currentX == prevX && currentY == prevY)
            return REWARD_BUMPING_INTO_WALL;
        if (action == ACTION.SHOOT.getID() && !previous.hasArrow())
            return REWARD_FIRING_WITHOUT_AMMO;
        
        if (w.hasWumpus(currentX, currentY))
            return REWARD_EATEN;
        if (w.hasGold())
            return REWARD_GOLD;
        if (w.hasPit(currentX, currentY) && !previous.hasPit(prevX, prevY))
            return REWARD_PIT;
        if (!w.hasArrow() && previous.hasArrow()) {
            if (!w.wumpusAlive())
                return REWARD_WUMPUS_KILLED;
            else
                return REWARD_ARROW_MISSED;
        }
        
        if (previous.isUnknown(currentX, currentY))
            return REWARD_EXPLORED_TILE;
        
        return 0.0;
    }

    public static HashMap<State, double[]> readQTable() {
        HashMap<State, double[]> Q = new HashMap<>();
        
        int states = 0;
        try (ObjectInputStream fis = new ObjectInputStream(new FileInputStream(new File(Q_FILE_PATH)))) {
            while (true) {
            	//System.err.println("Hello");
            	State s = (State) fis.readObject();

                double[] q_values = new double[TOTAL_ACTIONS];
                for (int i = 0; i < TOTAL_ACTIONS; ++i) {
                    q_values[i] = fis.readDouble();
                }
                states += 1;
                Q.put(s, q_values);
            }
        } catch (FileNotFoundException ex) {
        	System.err.println("File not found");
        } catch (IOException ex) {
        	System.err.println("IO Exception");
        } catch (ClassNotFoundException ex) {
        	System.err.println("Class not found");
	        Q.clear();
	    }
        
        System.out.println("Loaded Q Table with states: " + Q.size());
        return Q;
    }
    
    public static void saveQTable(HashMap<State, double[]> Q) {
    	System.out.println("Q Table is of size: " + Q.size());
        try (ObjectOutputStream fos = new ObjectOutputStream(new FileOutputStream(new File(Q_FILE_PATH), false))) {
            for (Entry<State, double[]> entry : Q.entrySet()) {
            	fos.writeObject(entry.getKey());
                for (int i = 0; i < TOTAL_ACTIONS; ++i) {
                    fos.writeDouble(entry.getValue()[i]);
                }
            }
            fos.close();
            System.err.println("Wrote Q-Matrix to " + Q_FILE_PATH);
        } catch (IOException ex) {
            System.err.println("Failed to write Q-Matrix to " + Q_FILE_PATH);
        }
    }
}
