import java.awt.Graphics;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Maze Assignment: Agent.java
 * 
 * Class description for something that (may) perceive its environment, decide
 * what to do, and do it.
 * 
 * @author Matthew Stone
 * @version 1.0
 */
public abstract class Agent {

	/**
	 * Class information
	 */

	/**
	 * The maze world is discrete, and perception and action are all simulated
	 * in terms of four cardinal directions.
	 */
	static enum Heading {
		// Declarations (enum constructor calls)
		NORTH("north", 0, -1), SOUTH("south", 0, 1), EAST("east", 1, 0), WEST(
				"west", -1, 0);

		/** Name of a heading */
		public final String description;
		/** Change in map x coordinate with one step in this heading */
		public final int dx;
		/** Change in map y coordinate with one step in this heading */
		public final int dy;

		/** Enum constructor */
		private Heading(String d, int x, int y) {
			description = d;
			dx = x;
			dy = y;
		}

		/** Opposite direction */
		public Heading opposite() {
			Heading r = NORTH;
			switch (this) {
			case NORTH:
				r = SOUTH;
				break;
			case SOUTH:
				r = NORTH;
				break;
			case EAST:
				r = WEST;
				break;
			case WEST:
				r = EAST;
				break;
			}
			return r;
		}
	}

	/**
	 * For the agent itself, keeping track of the cardinal direction it is
	 * facing implicitly involves memory; If the agent lacks this implicit
	 * memory, it can only perceive and act relative to its current heading;
	 * directions describe these relative moves.
	 */
	static enum Direction {
		/** The cardinal direction the agent is facing */
		AHEAD("ahead"),
		/** The cardinal direction to the left of the agent */
		LEFT("left"),
		/** The cardinal direction to the right of the agent */
		RIGHT("right"),
		/** The cardinal direction behind the agent */
		BEHIND("behind");
		public final String description;

		private Direction(String d) {
			description = d;
		}
	}

	/** Convenience variables for reading and writing XML code */
	static final String OPEN = "=\"";
	static final String CLOSE = "\" ";

	/** Attribute name for integer giving identity of agent to change */
	static final String ID_PARAM = "id";

	/** XML element tag name for updates */
	static final String UPDATE = "update";

	/**
	 * Attributes of the agent that change only slowly, if at all. Organized in
	 * a class to facilitate input and output.
	 */
	static class FixedAgentAttributes {
		static final String DEBUG_PARAM = "debug";
		/** Attribute name to show that behavior reflects "extensions" */
		static final String EXTENSIONS_PARAM = "with-extensions";

		/** Should we visualize debugging information? */
		public boolean debug;
		/** Should we have vanilla or extended behavior? */
		public boolean withExtensions;

		/**
		 * Constructor for fully specified fixed agent attributes
		 * 
		 * @param g
		 *            should debugging visualization happen
		 * @param e
		 *            should the agent extensions be activated
		 */
		FixedAgentAttributes(boolean g, boolean e) {
			debug = g;
			withExtensions = e;
		}

		/**
		 * Copy constructor
		 * 
		 * @param a
		 *            attributes to copy
		 */
		FixedAgentAttributes(FixedAgentAttributes a) {
			debug = a.debug;
			withExtensions = a.withExtensions;
		}

		/**
		 * Initialize or reinitialize agent attributes based on XML data.
		 * 
		 * Utility function that should be called when each instance of a
		 * subclass of agent is created, to make sure that as we add new
		 * functionality to the agent simulation, the agents that are created
		 * have sensible default behavior.
		 * 
		 * @param atts
		 *            SAX attribute structure derived from XML data
		 * @param defaults
		 *            values to use when attributes are unspecified
		 * @param locator
		 *            file information for reporting errors
		 * @throws SAXException
		 *             in case data is formatted wrong
		 */
		public void set(Attributes atts, FixedAgentAttributes defaults,
				Locator locator) throws SAXException {
			debug = MazeReader.getBoolParam(atts, DEBUG_PARAM, defaults.debug,
					locator);
			withExtensions = MazeReader.getBoolParam(atts, EXTENSIONS_PARAM,
					defaults.withExtensions, locator);
		}

		/**
		 * Change agent attributes where they are specified in XML data
		 * 
		 * @param atts
		 *            SAX attribute structure derived from XML data
		 * @param locator
		 *            file information for reporting errors
		 * @throws SAXException
		 *             in case data is formatted wrong
		 */
		public void update(Attributes atts, Locator locator)
				throws SAXException {
			set(atts, this, locator);
		}

		/**
		 * Write XML attributes specifying the agent attribute structure to the
		 * file specified by out.
		 * 
		 * @param out
		 *            destination file
		 * @throws IOException
		 *             if writing fails
		 */
		public void log(BufferedWriter out) throws IOException {
			out.write(DEBUG_PARAM + OPEN + Boolean.toString(debug) + CLOSE
					+ EXTENSIONS_PARAM + OPEN
					+ Boolean.toString(withExtensions) + CLOSE + "\n");
		}

		/**
		 * Constructor based on XML data
		 * 
		 * @param atts
		 *            XML data where attributes are described
		 * @param defaults
		 *            what to use for unspecified attributes
		 * @param locator
		 *            for reporting errors
		 * @throws SAXException
		 *             in case of data format problems
		 */
		FixedAgentAttributes(Attributes atts, FixedAgentAttributes defaults,
				Locator locator) throws SAXException {
			set(atts, defaults, locator);
		}

	}

	/** object that provides customizable defaults for new agent parameters */
	static FixedAgentAttributes defaultFixedAgentAttributes = new Agent.FixedAgentAttributes(
			false, false);

	/** Class to hold default values for dynamically changing parameters */
	static class DynamicAgentAttributes {
		/** Attribute name for horizontal coordinate of agent @see locX */
		static final String X_PARAM = "x";
		/** Attribute name for vertical coordinate of agent @see locY */
		static final String Y_PARAM = "y";
		/** Attribute name for direction agent is heading @see heading */
		static final String HEADING_PARAM = "heading";
		/** Attribute name for whether agent has hit a wall @see bumped */
		static final String BUMPED_PARAM = "bumped";
		/** Attribute name for any debug output @see message */
		static final String MESSAGE_PARAM = "message";

		/** Position of the agent horizontally within the world */
		public int locX;
		/** Position of the agent vertically within the world */
		public int locY;
		/** Angle that the agent is facing */
		public Heading heading;
		/** Whether agent has bumped the wall */
		public boolean bumped;
		/** Any debug message for the agent */
		public String message;

		/**
		 * Constructor for fully specified agent attributes
		 * 
		 * @param x
		 *            horizontal coordinate of agent
		 * @param y
		 *            vertical coordinate of agent
		 * @param h
		 *            direction agent is facing
		 * @param b
		 *            whether agent has bumed the wall
		 * @param m
		 *            debug message for agent
		 */
		DynamicAgentAttributes(int x, int y, Heading h, Boolean b, String m) {
			locX = x;
			locY = y;
			heading = h;
			bumped = b;
			message = m;
		}

		/**
		 * Copy constructor
		 * 
		 * @param a
		 *            attributes to mirror
		 */
		DynamicAgentAttributes(DynamicAgentAttributes a) {
			locX = a.locX;
			locY = a.locY;
			heading = a.heading;
			bumped = a.bumped;
			message = a.message;
		}

		/**
		 * Initialize or reinitialize agent attributes based on XML data
		 * 
		 * @param atts
		 *            SAX attribute structure derived from XML data
		 * @param defaults
		 *            values to use when attributes are unspecified
		 * @param locator
		 *            file information for reporting errors
		 * @throws SAXException
		 *             in case data is formatted wrong
		 */
		public void set(Attributes atts, DynamicAgentAttributes defaults,
				Locator locator) throws SAXException {
			locX = MazeReader
					.getIntParam(atts, X_PARAM, defaults.locX, locator);
			locY = MazeReader
					.getIntParam(atts, Y_PARAM, defaults.locY, locator);
			heading = Heading.valueOf(MazeReader.getStringParam(atts,
					HEADING_PARAM, defaults.heading.toString(), locator));
			bumped = MazeReader.getBoolParam(atts, BUMPED_PARAM,
					defaults.bumped, locator);
			message = MazeReader.getStringParam(atts, MESSAGE_PARAM,
					defaults.message, locator);
		}

		/**
		 * Change agent attributes where they are specified in XML data
		 * 
		 * @param atts
		 *            SAX attribute structure derived from XML data
		 * @param locator
		 *            file information for reporting errors
		 * @throws SAXException
		 *             in case data is formatted wrong
		 */
		public void update(Attributes atts, Locator locator)
				throws SAXException {
			set(atts, this, locator);
		}

		/**
		 * Write XML attributes specifying the agent attribute structure to the
		 * file given by out.
		 * 
		 * @param out
		 *            destination file
		 * @throws IOException
		 *             if writing fails
		 */
		public void log(BufferedWriter out, boolean debug) throws IOException {
			String last = "\n";

			if (message != null && debug) {
				last = MESSAGE_PARAM + OPEN + message + CLOSE + "\n";
			}

			out.write(X_PARAM + OPEN + Integer.toString(locX) + CLOSE + Y_PARAM
					+ OPEN + Integer.toString(locY) + CLOSE + HEADING_PARAM
					+ OPEN + heading.toString() + CLOSE + BUMPED_PARAM + OPEN
					+ Boolean.toString(bumped) + CLOSE + last);
		}

		/**
		 * Constructor based on XML data
		 * 
		 * @param atts
		 *            XML data where attributes are specified
		 * @param defaults
		 *            what to use for unspecified attributes
		 * @param locator
		 *            for reporting errors
		 * @throws SAXException
		 *             in case of data format problems
		 */
		DynamicAgentAttributes(Attributes atts,
				DynamicAgentAttributes defaults, Locator locator)
				throws SAXException {
			set(atts, defaults, locator);
		}
	}

	/** object that provides defaults for new agent parameters */
	static DynamicAgentAttributes defaultDynamicAgentAttributes = new Agent.DynamicAgentAttributes(
			1, 1, Heading.NORTH, false, null);

	/**
	 * Instance members
	 */

	/** The world the agent belongs to */
	protected World myWorld = null;

	/** A numerical code that identifies this agent within the world */
	protected int id = 0;

	/** Is this agent still functioning or has something killed it? */
	protected boolean isAlive = true;

	/** What are the general characteristics of the agent */
	protected FixedAgentAttributes form;

	/** What is the agent doing right now */
	protected DynamicAgentAttributes status;

	/** Actions computed by deliberation that have yet to be acted on */
	protected List<Intention> todo = null;

	/** Preserve last status for debugging visualization */
	protected DynamicAgentAttributes lastStatus = null;

	/**
	 * Accessor methods
	 */

	/**
	 * @return current horizontal coordinate of agent
	 */
	public int getLocX() {
		return status.locX;
	}

	/**
	 * @return current vertical coordinate of agent
	 */
	public int getLocY() {
		return status.locY;
	}

	/**
	 * @return direction agent is currently facing
	 */
	public Heading getHeading() {
		return status.heading;
	}

	/**
	 * @return whether the agent has just hit a wall
	 */
	public Boolean getBumped() {
		return status.bumped;
	}

	/**
	 * @return agent's current debugging status message
	 */
	public String getMessage() {
		return status.message;
	}

	/**
	 * @return unique integer identifying agent in the world
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return true if the agent is still treated as alive for the purposes of
	 *         simulation.
	 */
	public boolean isAlive() {
		return isAlive;
	}

	/**
	 * Mark an agent as dead.
	 */
	public void die() {
		isAlive = false;
	}

	/**
	 * Update the agent's current horizontal coordinate to x
	 * 
	 * @param x
	 *            in grid cells
	 */
	public void setLocX(int x) {
		status.locX = x;
	}

	/**
	 * Update the agent's current vertical coordinate to y
	 * 
	 * @param y
	 *            in grid cells
	 */
	public void setLocY(int y) {
		status.locY = y;
	}

	/**
	 * Update the agent's current direction of travel to heading
	 * 
	 * @param h
	 *            one of the four cardinal directions
	 */
	public void setHeading(Heading h) {
		status.heading = h;
	}

	/**
	 * Update the agent's current status as bumped
	 * 
	 * @param b
	 *            true or false
	 */
	public void setBumped(Boolean b) {
		status.bumped = b;
	}

	/**
	 * Update the agent's current status message
	 * 
	 * @param s
	 *            string message
	 */
	public void setMessage(String s) {
		status.message = s;
	}

	/**
	 * Helper function for perception.
	 * 
	 * Return true if one of the percepts in the passed list of percepts shows
	 * that you can't proceed in the direction d.
	 * 
	 * @param ps
	 * @param d
	 * @return
	 */
	protected boolean isBlocked(List<Percept> ps, Agent.Direction d) {
		for (Percept p : ps)
			if (p.getDirection() == d && p.getDistance() < 2)
				return true;

		return false;
	}

	/**
	 * Helper function for perception.
	 * 
	 * Return true if, based on all the information you perceive, you must
	 * conclude that there is no neighboring obstacle in direction d.
	 * 
	 * @param ps
	 * @param d
	 * @return
	 */
	protected boolean isOpen(List<Percept> ps, Agent.Direction d) {
		return !isBlocked(ps, d);
	}

	/**
	 * Helper function for perception
	 * 
	 * Return true if moving in the specified direction would take you outside
	 * of the map
	 * 
	 * @param d
	 * @return
	 */
	protected boolean isEscape(Agent.Direction d) {
		int x = status.locX;
		int y = status.locY;
		Agent.Heading h = getHeadingFor(d);

		int newx = x + h.dx;
		int newy = y + h.dy;

		return (newx < 0 || newy < 0 || newx >= myWorld.getDimension() || newy >= myWorld
				.getDimension());
	}

	/**
	 * Helper functions for action, decision making and user interface these are
	 * linked with the way the simulation works and so tend not to be overridden
	 * in subclasses
	 */

	/**
	 * Give the heading that's to the left of heading h
	 */
	public Heading leftOf(Heading h) {
		switch (h) {
		case NORTH:
			return Heading.WEST;
		case EAST:
			return Heading.NORTH;
		case SOUTH:
			return Heading.EAST;
		case WEST:
			return Heading.SOUTH;
		}

		return Heading.SOUTH;
	}

	/**
	 * Give the direction that corresponds to heading h
	 * 
	 * @param h
	 *            : a heading
	 * @return the direction of h relative to the agent
	 */
	protected Agent.Direction getDirectionFor(Agent.Heading h) {
		if (status.heading == h)
			return Agent.Direction.AHEAD;
		if (h == leftOf(status.heading))
			return Agent.Direction.LEFT;
		if (h == leftOf(leftOf(status.heading)))
			return Agent.Direction.BEHIND;
		return Agent.Direction.RIGHT;
	}

	/**
	 * Give the heading that corresponds to direction d
	 * 
	 * @param d
	 *            : a direction
	 * @return the absolute heading corresponding to d given where the agent is
	 *         facing
	 */
	protected Agent.Heading getHeadingFor(Agent.Direction d) {
		if (Agent.Direction.AHEAD == d)
			return status.heading;
		if (Agent.Direction.LEFT == d)
			return leftOf(status.heading);
		if (Agent.Direction.BEHIND == d)
			return leftOf(leftOf(status.heading));
		return leftOf(leftOf(leftOf(status.heading)));
	}

	/**
	 * Add a turning action that will face the agent in the specified direction
	 * 
	 * @param d
	 */
	protected void makeTurn(Agent.Direction d) {
		switch (d) {
		case AHEAD:
			return;
		case LEFT:
			todo.add(new Intention(Intention.ActionType.TURN_LEFT));
			break;
		case RIGHT:
			todo.add(new Intention(Intention.ActionType.TURN_RIGHT));
			break;
		case BEHIND:
			todo.add(new Intention(Intention.ActionType.TURN_BACK));
			break;
		}
		return;
	}

	/**
	 * Update the agent's intentions to include a turning action that will face
	 * the agent in the specified heading
	 * 
	 * @param h
	 */
	protected void planTurnTo(Agent.Heading h) {
		makeTurn(getDirectionFor(h));
	}

	/**
	 * Change the heading of the agent in response to a command to turn left.
	 */
	private void turnLeft() {
		status.heading = leftOf(status.heading);
	}

	/**
	 * Negotiate with the world to move the agent a step of size given by
	 * forwardV in the direction given by the current heading.
	 */
	private void step() {
		int newLocX = status.locX + status.heading.dx;
		int newLocY = status.locY + status.heading.dy;
		myWorld.tryToMove(this, newLocX, newLocY);
	}

	/**
	 * Carry out the motion actions given by the agent's todo list. That
	 * includes one turning action, and one step. Then move the agent one step.
	 */
	public void act() {
		Set<Intention.ActionType> done = new HashSet<Intention.ActionType>();

		lastStatus = new DynamicAgentAttributes(status);

		for (Intention a : todo) {
			Intention.ActionType t = a.getType();
			if (done.contains(t)) {
				System.err.println("Error: repeated action of " + t.description
						+ " ignored");
			}
			done.add(t);
			switch (t) {
			case TURN_LEFT:
				turnLeft();
				break;
			case TURN_BACK:
				turnLeft();
				turnLeft();
				break;
			case TURN_RIGHT:
				turnLeft();
				turnLeft();
				turnLeft();
				break;
			case STEP:
				step();
				break;
			}
		}
	}

	/**
	 * Important methods that subclasses tend to override The XML details are
	 * already handled in skeleton code
	 */

	/**
	 * Write a complete XML description of the agent
	 * 
	 * @param out
	 *            destination channel for XML element
	 * @throws IOException
	 *             in case writing fails
	 */
	public abstract void log(BufferedWriter out) throws IOException;

	/**
	 * Write an XML description of the dynamic properties of the agent
	 * 
	 * @param out
	 *            destination channel for XML element
	 * @throws IOException
	 *             in case writing fails
	 */
	public void changelog(BufferedWriter out) throws IOException {
		out.write("   <" + UPDATE + " " + ID_PARAM + OPEN
				+ Integer.toString(id) + CLOSE + "\n    ");
		status.log(out, form.debug);
		out.write("    />\n");
	}

	/**
	 * Change the parameters of this agent to reflect the information in the
	 * passed XML specification
	 * 
	 * @param atts
	 *            SAX attributes derived from XML data
	 * @param loc
	 *            file information for reporting errors
	 * @throws SAXException
	 *             in case of data format problems
	 */
	public void update(Attributes atts, Locator loc) throws SAXException {
		form.update(atts, loc);
		status.update(atts, loc);
	}

	/**
	 * renders a picture of the agent into the world display
	 * 
	 * @param g
	 *            graphics information
	 */
	public abstract void draw(Graphics g);

	/**
	 * Method each agent uses to update its internal todo list on the basis of a
	 * new round of perceptual information
	 * 
	 * @param ps
	 *            Specification of the other agents in the world from the
	 *            agent's perspective
	 */
	public abstract void deliberate(List<Percept> ps);
}
