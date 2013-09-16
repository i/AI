import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Maze Assignment: World.java
 *
 * World calculates and displays the dynamics of an agent
 * acting in a world environment.
 * 
 * @author Matthew Stone
 * @version 1.0
 */

public class World extends Canvas {

    /**
     * Static class definitions
     */

    /**
     * Java AWT components are required to be serializable,
     * and therefore require a long int id indicating what 
     * version of the code the serialization comes from.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constants specifying the format by which 
     * World constructs are encoded as XML documents.
     */

    /** XML namespace */
    static final String XMLNS = 
        "http://perceptualscience.rutgers.edu/maze";

    /** Element tag for world objects */
    static final String XML_NAME = "world";

    /** Attribute name for world width */
    static final String WIDTH_PARAM = "width";
  
    /** Value used when no width specified */
    static final int DEFAULT_WIDTH = 500;

    /** Attribute name for world height */
    static final String HEIGHT_PARAM = "height";

    /** Value used when no height specified */
    static final int DEFAULT_HEIGHT = 500;

    /** Attribute name for cells in maze */
    static final String CELL_PARAM = "cells";

    /** Value used when no cells specified */
    static final int DEFAULT_CELLS = 10;
    
    /** Attribute name for XML document recording world history */
    static final String LOGFILE_PARAM = "logfile";

    /** Boolean attribute says whether to do simulation */
    static final String RUNNABLE_PARAM = "runnable";

    /** Boolean attribute for wheter to visualize debugging info */
    static final String DEBUG_PARAM = "debug";

    /** Element tag for delay in replaying log data */
    static final String WAIT_NAME = "wait";

    /** Attribute name for amount of delay, in milliseconds */
    static final String WAIT_INTERVAL = "time";

    /** Attribute name for amount of delay, in milliseconds */
    static final String REPLAY_INTERVAL = "replay";

    /** Delay initiated by waiting when nothing specified, gives 50fps */
    static final int DEFAULT_WAIT = 50;

    /** Element tag for state giving snapshot of world history */
    static final String STATE_NAME = "state";

    /** Attribute for index of state */
    static final String STEP_NAME = "step";

    /** Element tag for death */
    static final String DIE_NAME = "kill";

    /** Drawing parameter: How thick walls should look */
    static final int WALL_THICKNESS = 8;
    
    /** Drawing parameter: What color walls should be */
    static final Color WALL_COLOR = Color.GREEN;
    
    /** Drawing parameter: What color agents should be */
    static final Color AGENT_COLOR = Color.BLUE;
    
    /**
     * Instance members
     */

    /** All the active entities that "live" in the world */
    private List<Agent> agents;
    /** How big the maze is */
    private int cells;
    /** How long a cell in the maze is */
    private int cellWidth;
    /** Where there are horizontal walls */
    private boolean[][] beams;
    /** Where there are vertical walls */
    private boolean[][] poles;
    
    /** Where dynamaics history should be written, null means don't write */
    private String logfile;
    /** If runnable is false this is inert history data */
    private boolean runnable;
    /** Default amount of time to wait between steps of simulation */
    private int delay;
    /** Time to wait between steps in replay */
    private int replay;
    /** Whether to visualize debugging info */
    private boolean debug;
    /** Whether any agent has bumped into a wall in this time step */
    private boolean bumped;
    /** How many steps of simulation have been run */
    private int stepCount;

    /**
     * Instance code
     */

    /**
     * Constructor for new environments
     * 
     * @param width horizontal extent of the environment
     * @param height vertical extent of the environment
     * @param log file name to record dynamics history
     * @param run true to get new dynamics, false to replay old ones
     * @param wait number of milliseconds to delay between simulation steps
     */
    public World(int width, int height, int c, String log, boolean run, int wait, int rep, boolean debug) {
        setSize(width, height);
        cells = c;
        beams = new boolean[cells][cells+1];
        poles = new boolean[cells+1][cells];
        for (int i = 0; i < cells; i++) {
        	for (int j = 0; j < cells + 1; j++) {
        		beams[i][j] = poles[j][i] = false;
        	}
        }
        cellWidth = Math.min(width / (cells+2), height / (cells + 2));
        logfile = log;
        runnable = run;
        delay = wait;
        replay = rep;
        agents = new LinkedList<Agent>();
        this.debug = debug;
        stepCount = 0;
        bumped = false;
    }

    /**
     * Getters and setters
     */

    /**
     * Return the number of possible locations (along x and y)
     * in the maze
     */
    public int getDimension() {
        return cells;   
    }
    
    /**
     * Get what step the simulation has gotten to
     * @return current step value of simulation
     */
    public int getStepCount() {
        return stepCount;
    }

    /**
     * Set what step the simulation has gotten to
     * @returns current step value of simulation
     */
    public void setStepCount(int s) {
        stepCount = s;
    }

    /**
     * Should this environment display new dynamics
     * @return true if yes, false if replaying old data
     */
    public boolean isRunnable() {
        return runnable;
    }

    /**
     * @return amount of time in milliseconds to wait between simulation steps
     */
    public int getDelay() {
        return delay;
    }

    /**
     * Attach a new agent to the world environment
     * @param a agent object to add
     */
    public void addAgent(Agent a) {
        agents.add(a);
    }

    /**
     * Remove an agent from the world environment.
     * Useful if a has died or been eaten.
     * 
     * @param a agent object to remove.
     */
    public void removeAgent(Agent a) {
        agents.remove(a);
    }

    /**
     * Find the agent by the specified id
     * 
     * @param id creation index for some agent associated with the world
     * @return agent object if found, null otherwise
     */
    public Agent getAgent(int id) {
        for (Agent a: agents) {
            if (id == a.getId()) {
                return a;
            }
        }
        return null;
    }
    
    /**
     * Add a beam 
     * 
     * @param x coordinate of left corner of beam
     * @param y coordinate of beam
     */
    public void addBeam(int x, int y) {
    	if (x >= cells || y >= cells + 1)
    		System.err.println("Beam coordinates " + x + " " + y + " are out of range.\n");
    	else
    		beams[x][y] = true;
    }
    
    /**
     * Add a pole 
     * 
     * @param x coordinate of corner of pole
     * @param y coordinate of top corner of pole
     */
    public void addPole(int x, int y) {
    	if (x >= cells + 1 || y >= cells)
    		System.err.println("Beam coordinates " + x + " " + y + " are out of range.\n");
    	else
    		poles[x][y] = true;
    }
    
    /**
     * Producing change logs
     */

    /**
     * Open XML log file - if world is supposed to have one -
     * and write header information giving world parameters.
     * Then describe each of the agents in the world,
     * in complete detail, giving the initial state
     * of the simulation.
     */
    public void startLogging() {
        if (logfile != null) {
            try {
                BufferedWriter out = new BufferedWriter(new FileWriter(logfile, false));
                out.write("<?xml version=\"1.0\"?>\n\n");
                out.write("<" + XML_NAME + 
                        " xmlns=\"" + XMLNS +
                        "\" " + WIDTH_PARAM +
                        "=\"" + Integer.toString(getWidth()) +
                        "\" " + HEIGHT_PARAM +
                        "=\"" + Integer.toString(getHeight()) +
                        "\" " + CELL_PARAM +
                        "=\"" + Integer.toString(cells) +
                        "\" " + RUNNABLE_PARAM + 
                        "=\"false\" " + DEBUG_PARAM +
                        "=\"true\" >\n"
                );
                for (int i = 0; i < cells; i++)
                	for (int j = 0; j < cells + 1; j++) {
                		if (beams[i][j])
                			out.write("<" + MazeReader.BEAM_NAME +
                					" " + MazeReader.X_PARAM +
                					"=\"" + Integer.toString(i) +
                					"\" " + MazeReader.Y_PARAM +
                					"=\"" + Integer.toString(j) +
                					"\" />\n");
                	}
                for (int i = 0; i < cells + 1; i++)
                	for (int j = 0; j < cells; j++) {
                		if (poles[i][j])
                			out.write("<" + MazeReader.POLE_NAME +
                					" " + MazeReader.X_PARAM +
                					"=\"" + Integer.toString(i) +
                					"\" " + MazeReader.Y_PARAM +
                					"=\"" + Integer.toString(j) +
                					"\" />\n");
                	}
                out.write("  <" + STATE_NAME + " " +
                        STEP_NAME + "=\"" + Integer.toString(stepCount) + "\" >\n");
                for (Agent a: agents) {
                    a.log(out);
                }
                out.write("  </" + STATE_NAME + ">\n");
                out.write("  <" + WAIT_NAME + " " + WAIT_INTERVAL + "=\"" +
                        Integer.toString(replay) + "\"/>\n");
                out.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * Open XML log file - if world is supposed to have one -
     * and write final close ending main XML element.
     */ 
    public void finishLogging() {
        if (logfile != null) {
            try {
                BufferedWriter out = new BufferedWriter(new FileWriter(logfile, true));
                out.write("</" + XML_NAME + ">\n\n");
                out.close();
            } catch (IOException e) {
            }
            logfile = null;
        }
    }

    /**
     * Open XML log file - if world is supposed to have one -
     * and append state description describing the dynamic parameters
     * of all the agents in the environment at the current
     * time step.
     */
    private void logStep() {
        if (logfile != null) {
            try {
                BufferedWriter out = new BufferedWriter(new FileWriter(logfile, true));
                out.write("  <" + STATE_NAME + " " +
                        STEP_NAME + "=\"" + Integer.toString(stepCount) + "\">\n");
                for (Agent a: agents) {
                    a.changelog(out);
                }
                out.write("  </" + STATE_NAME + ">\n");
                out.write("  <" + WAIT_NAME + " " + WAIT_INTERVAL + "=\"" +
                        Integer.toString(replay) + "\"/>\n");
                out.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * Open XML log file - if world is supposed to have one -
     * and append instructions to remove display of agent a
     * for subsequent steps of the simulation.
     * @param a agent that should not be rendered in future steps
     */
    private void logDeath(Agent a) {
        if (logfile != null) {
            try {
                BufferedWriter out = new BufferedWriter(new FileWriter(logfile, true));
                out.write("  <" + DIE_NAME + " " + Agent.ID_PARAM + "=\"" + Integer.toString(a.getId()) + "\" />\n");
                out.close();
            } catch (IOException e) {
            }
        }
    }


    /**
     * Callback method to redisplay the world
     */
    public void paint(Graphics g) {
        if (debug) {
        	String message = Integer.toString(stepCount);
        	for (Agent a: agents) {
		    String m = a.getMessage();
		    if (a.getBumped() || m != null) {
			message += " Agent " + Integer.toString(a.getId()) + ":";
			if (m != null) {
			    message += " " + m;
			}
			if (a.getBumped()) {
			    message += " OUCH!";
			}
		    }
		}
        	
            g.setColor(Color.BLACK);
            g.drawString(message, 3, getHeight() - 3);
        }

        g.setColor(WALL_COLOR);
        for (int i = 0; i < cells; i++) {
        	for (int j = 0; j < cells + 1; j++) {
        		if (beams[i][j])
        			g.fillRect(cellWidth * (i+1), 
        					cellWidth * (j+1),
        					cellWidth, 
        					WALL_THICKNESS);
        	}
        }

        for (int i = 0; i < cells + 1; i++) {
        	for (int j = 0; j < cells; j++) {
        		if (poles[i][j])
        			g.fillRect(cellWidth * (i+1),
        					cellWidth * (j+1),
        					WALL_THICKNESS,
        					cellWidth);
        	}
        }

        g.setColor(AGENT_COLOR);
        for (Agent a: agents) {
            a.draw(g);
        }
    }

    /**
     * Wrapper for fillPolygon method of Graphics object
     * for use in agent drawing
     */
    public void fillPolygon(int x, int y, int[] xpoints, int[] ypoints, int numPoints, Graphics g) {
        int xcenter = (x + 1) * cellWidth + cellWidth / 2;
        int ycenter = (y + 1) * cellWidth + cellWidth / 2;
        for (int i = 0; i < numPoints; i++) {
        	xpoints[i] += xcenter;
        	ypoints[i] += ycenter;
        }	
        g.fillPolygon(xpoints, ypoints, numPoints);
        
    }

    /**
     * Wrapper for fillOval method of graphics object
     */
    public void fillOval(int x, int y, int width, int height, Graphics g) {
        int xcenter = (x + 1) * cellWidth + cellWidth / 2 - width/2;
        int ycenter = (y + 1) * cellWidth + cellWidth / 2 - height/2;
        g.fillOval(xcenter, ycenter, width, height);
    }

    /**
     * Wrapper for fillRect method of graphics object
     */
    public void fillRect(int x, int y, int width, int height, Graphics g) {
        int xcenter = (x + 1) * cellWidth + cellWidth / 2 - width/2;
        int ycenter = (y + 1) * cellWidth + cellWidth / 2 - height/2;
        g.fillRect(xcenter, ycenter, width, height);
    }

    /**
     * Wrapper for drawLine method of graphics object
     */
    public void drawLine(int x, int y, int x1, int y1, int x2, int y2, Graphics g) {
        int xcenter = (x + 1) * cellWidth + cellWidth / 2;
        int ycenter = (y + 1) * cellWidth + cellWidth / 2;
        x1 += xcenter;
        x2 += xcenter;
        y1 += ycenter;
        y2 += ycenter;
        g.drawLine(x1, y1, x2, y2);
    }

    /**
     * Constructing percepts
     * The logic needs to examine whether 
     * a wall is present next to the agent
     * at a specific bearing, given the
     * direction the agent is headed.
     */
    
    /**
     * construct a percept showing a wall
     * present ahead of the agent if
     * this is the case.
     * 
     * @param agent whose observations are being modeled
     * @return percept of wall, or null if no wall is ahead
     */
    protected Percept lookAhead(Agent a) {
    	Agent.Heading h = a.getHeading();
    	int x = a.getLocX();
    	int y = a.getLocY();
    	boolean wall = false;
    	switch (h) {
    	case NORTH:
    		wall = beams[x][y]; break;
    	case WEST:
    		wall = poles[x][y]; break;
    	case SOUTH:
    		wall = beams[x][y+1]; break;
    	case EAST:
    		wall = poles[x+1][y]; break;
    	}
    	Percept result = null;
    	if (wall)
    		result = new Percept(Percept.ObjectCategory.OBSTACLE, 1, Agent.Direction.AHEAD);
    	return result;
    }
    
    /**
     * construct a percept showing a wall
     * present to the left of the agent if
     * this is the case.
     * 
     * @param agent whose observations are being modeled
     * @return percept of wall, or null if no wall is to the left
     */
    protected Percept lookLeft(Agent a) {
    	Agent.Heading h = a.getHeading();
    	int x = a.getLocX();
    	int y = a.getLocY();
    	boolean wall = false;
    	switch (h) {
    	case NORTH:
    		wall = poles[x][y]; break;
    	case WEST:
    		wall = beams[x][y+1]; break;
    	case SOUTH:
    		wall = poles[x+1][y]; break;
    	case EAST:
    		wall = beams[x][y]; break;
    	}
    	Percept result = null;
    	if (wall)
    		result = new Percept(Percept.ObjectCategory.OBSTACLE, 1, Agent.Direction.LEFT);
    	return result;
    }

    /**
     * construct a percept showing a wall
     * present to the right of the agent if
     * this is the case.
     * 
     * @param agent whose observations are being modeled
     * @return percept of wall, or null if no wall is to the right
     */
    protected Percept lookRight(Agent a) {
    	Agent.Heading h = a.getHeading();
    	int x = a.getLocX();
    	int y = a.getLocY();
    	boolean wall = false;
    	switch (h) {
    	case NORTH:
    		wall = poles[x+1][y]; break;
    	case WEST:
    		wall = beams[x][y]; break;
    	case SOUTH:
    		wall = poles[x][y]; break;
    	case EAST:
    		wall = beams[x][y+1]; break;
    	}
    	Percept result = null;
    	if (wall)
    		result = new Percept(Percept.ObjectCategory.OBSTACLE, 1, Agent.Direction.RIGHT);
    	return result;
    }

    /**
     * construct a percept showing a wall
     * present behind of the agent if
     * this is the case.
     * 
     * @param agent whose observations are being modeled
     * @return percept of wall, or null if no wall is ahead
     */
    protected Percept lookBehind(Agent a) {
        Agent.Heading h = a.getHeading();
        int x = a.getLocX();
        int y = a.getLocY();
        boolean wall = false;
        switch (h) {
        case SOUTH:
            wall = beams[x][y]; break;
        case EAST:
            wall = poles[x][y]; break;
        case NORTH:
            wall = beams[x][y+1]; break;
        case WEST:
            wall = poles[x+1][y]; break;
        }
        Percept result = null;
        if (wall)
            result = new Percept(Percept.ObjectCategory.OBSTACLE, 1, Agent.Direction.BEHIND);
        return result;
    }
    
    /**
     * Run a step of deliberation on Agent a.
     * Construct the percept A gets now and
     * feed it to A's deliberation method.
     * Override this method to add visibility checks
     * and other aspects of simulated visual cognition.
     * 
     * @param a One of the agents in the world
     */
    protected void makeAgentThink(Agent a) {

        List<Percept> ps = new LinkedList<Percept>();
        Percept p = null;
        
        // A can see adjacent walls in all directions
        p = lookAhead(a);
        if (p != null)
        	ps.add(p);
        
        p = lookLeft(a);
        if (p != null)
        	ps.add(p);
        
        p = lookRight(a);
        if (p != null)
        	ps.add(p);
        
        p = lookBehind(a);
        if (p != null)
            ps.add(p);
        
        a.deliberate(ps);
    }

    /**
     * Process the simulated input to agent A's effectors
     * designed to get A to location (newX, newY) in the world.
     * Does not allow the agent to go through walls.
     * 
     * @param a Agent who wants to move
     * @param newX Desired updated horizontal coordinate
     * @param newY Desired updated vertical coordinate
     */
    public void tryToMove(Agent a, int newX, int newY) {
    	int x = a.getLocX();
    	int y = a.getLocY();
    	if (newX > x) {
    		for (int i = x + 1; i <= newX; i++)
    			if (poles[i][y]) {
    				newX = i - 1;
    				bumped = true;
    			}
    	}
    	else if (newX < x) {
    		for (int i = x; i > newX; i--)
    			if (poles[i][y]) {
    				newX = i;
    				bumped = true;
    			}
    	}
    	if (newY > y) {
    		for (int i = y + 1; i <= newY; i++)
    			if (beams[x][i]) {
    				newY = i - 1;
    				bumped = true;
    			}
    	}
    	else if (newY < y) {
    		for (int i = y; i > newY; i--)
    			if (beams[x][i]) {
    				newY = i;
    				bumped = true;
    			}
    	}
    	
    	a.setLocX(newX);
    	a.setLocY(newY);
    	
    	// You escape!
    	if (newX < 0 || newX >= cells || newY < 0 || newY >= cells)
    		runnable = false;
    }

    /**
     * Remove all the agents from the world that
     * are no longer alive, and log their deaths.
     */
    private void removeCorpses() {
        LinkedList<Agent> alive = new LinkedList<Agent>();
        for (Agent a: agents) {
            if (a.isAlive())
                alive.add(a);
            else
                logDeath(a);
        }

        agents = alive;
    }

    /**
     * Carry out a step of simulation, 
     * in which all the agents perceive, deliberate, and act,
     * and the environment carries out the effects of
     * this behavior.
     */
    public void stepWorld() {
        stepCount++;
        
        // For each living agent, figure out what there is to do based on
        // the current state of the world
        for (Agent agent: agents) {
            if (agent.isAlive()) {
		agent.setBumped(false);
		agent.setMessage(null);
                makeAgentThink(agent);
            }
        }

        // For each living agent, update the state of each agent based
        // on their decisions
        for (Agent agent: agents) {
            if (agent.isAlive()) {
                agent.act();
            }
        }

        // Give feedback to the designer of the world
        repaint();  
        logStep();
        removeCorpses();
    }
}
