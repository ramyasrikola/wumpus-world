package wumpusworld;

import java.util.HashMap;
import java.util.Vector;

/**
 * Starting class for the Wumpus World program. The program
 * has three options: 1) Run a GUI where the Wumpus World can be
 * solved step by step manually or by an agent, or 2) run
 * a simulation with random worlds over a number of games,
 * or 3) run a simulation over the worlds read from a map file.
 * 
 * @author Johan Hagelbäck
 */
public class WumpusWorld {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        WumpusWorld ww = new WumpusWorld();
    }
    
    /**
     * Starts the program.
     * 
     */
    public WumpusWorld()
    {
        String option = Config.getOption();
        
        if (option.equalsIgnoreCase("gui"))
        {
            showGUI();
        }
        if (option.equalsIgnoreCase("sim"))
        {
            runSimulator();
        }
        if (option.equalsIgnoreCase("simdb"))
        {
            runSimulatorDB();
        }
    }
    
    /**
     * Starts the program in GUI mode.
     */
    private void showGUI()
    {
        GUI g = new GUI(this);
    }
    
    /**
     * Starts the program in simulator mode with
     * maps read from a data file.
     */
    private void runSimulatorDB()
    {
        // Blank
    }
    
    /**
     * Starts the program in simulator mode
     * with random maps.
     */
    private void runSimulator()
    {
        // Blank
    }
    
    /**
     * Runs the solver agent for the specified Wumpus
     * World.
     * 
     * @param w Wumpus World
     * @return Achieved score
     */
    public int runSimulation(int index, World w, HashMap<MyAgent.State, double[]> qTable)
    {
        int actions = 0;
        MyAgent a = new MyAgent(w, qTable);
        while (!w.gameOver() && actions <= 1000)
        {
            a.doAction();
            actions++;
        }
        int score = w.getScore();
        System.out.println("Simulation " + index + " ended after " + actions + " actions. Score " + score);
        return score;
    }
}
