import java.awt.Graphics;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Maze Assignment: Follower.java
 * 
 * A basic agent that attempts to walk through a maze, following the wall to its
 * left, turning and stepping in one go.
 * 
 * @author Matthew Stone
 * @version 1.0
 */

public class Follower extends Agent {

	/**
	 * Class information
	 */

	/** Tag for XML element */
	static final String XML_NAME = "follower";

	/** Size in display */
	static final int FOLLOWER_SIZE = 15;

	/** Record that allows XML files to set wall-follower default attributes */
	static FixedAgentAttributes defaultFixedAgentAttributes = new Agent.FixedAgentAttributes(
			false, false);

	/** Record that allows XML files to set wall-follower default state */
	static DynamicAgentAttributes defaultDynamicAgentAttributes = new Agent.DynamicAgentAttributes(
			0, 0, Agent.Heading.EAST, false, null);

	/**
	 * Instance members
	 */

	/**
	 * Constructor: initialize general agent fields to describe agent that will
	 * follow a light source.
	 * 
	 * @param w
	 *            world to which agent belongs
	 * @param id
	 *            number to identify agent in its world
	 * @param atts
	 *            SAX attributes corresponding to XML agent spec
	 * @param loc
	 *            file information for error messages
	 * @throws SAXException
	 *             if data is formatted incorrectly
	 */
	public Follower(World w, int id, Attributes atts, Locator loc)
			throws SAXException {
		myWorld = w;
		this.id = id;
		form = new FixedAgentAttributes(atts, defaultFixedAgentAttributes, loc);
		status = new DynamicAgentAttributes(atts,
				defaultDynamicAgentAttributes, loc);
	}

	/**
	 * Output an XML element describing the current state of this follower.
	 * 
	 * @param out
	 *            an open file to write to, wrapped in BufferedWriter
	 *            convenience class
	 */
	public void log(BufferedWriter out) throws IOException {
		out.write("   <" + XML_NAME + " " + ID_PARAM + OPEN
				+ Integer.toString(id) + CLOSE + "\n     ");
		form.log(out);
		out.write("    ");
		status.log(out, form.debug);
		out.write("    />\n");
	}

	/*
	 * Draw a wall-follower as a solid triangle pointing in the direction of the
	 * agent's heading.
	 * 
	 * @param g object to control drawing mechanism
	 * 
	 * @see Agent#draw(java.awt.Graphics)
	 */
	@Override
	public void draw(Graphics g) {
		int[] xpoints = new int[3];
		int[] ypoints = new int[3];

		double pointAngle = 0;
		switch (status.heading) {
		case NORTH:
			pointAngle = -Math.PI / 2;
			break;
		case EAST:
			pointAngle = 0;
			break;
		case WEST:
			pointAngle = Math.PI;
			break;
		case SOUTH:
			pointAngle = Math.PI / 2;
			break;
		}
		double baseAngle = pointAngle - Math.PI / 2;

		double s = FOLLOWER_SIZE;
		int baseOffsetX = (int) Math.round(2 * s * Math.cos(baseAngle) / 3);
		int baseOffsetY = (int) Math.round(2 * s * Math.sin(baseAngle) / 3);

		int x0 = ((int) Math.round(0 - baseOffsetX / 2 - s
				* Math.cos(pointAngle) / 3));
		int y0 = ((int) Math.round(0 - baseOffsetY / 2 - s
				* Math.sin(pointAngle) / 3));

		xpoints[0] = x0;
		xpoints[1] = x0 + baseOffsetX;
		xpoints[2] = x0 + baseOffsetX / 2
				+ (int) Math.round(s * Math.cos(pointAngle));

		ypoints[0] = y0;
		ypoints[1] = y0 + baseOffsetY;
		ypoints[2] = y0 + baseOffsetY / 2
				+ (int) Math.round(s * Math.sin(pointAngle));

		myWorld.fillPolygon(status.locX, status.locY, xpoints, ypoints, 3, g);

	}

	// TODO: Include any new instance variables here...
	boolean foundWall = false;

	/**
	 * Update agent's todo list.
	 * 
	 * @param ps
	 *            A description of everything the agent can see
	 */
	@Override
	public void deliberate(List<Percept> ps) {
		boolean left = isBlocked(ps, Direction.LEFT);
		boolean right = isBlocked(ps, Direction.RIGHT);
		boolean ahead = isBlocked(ps, Direction.AHEAD);
		boolean behind = isBlocked(ps, Direction.BEHIND);

		// TODO: Include any new code here...
		todo = new LinkedList<Intention>();
		
		if (!foundWall) {
			if (ahead) {
				foundWall = true;
				todo.add(new Intention(Intention.ActionType.TURN_RIGHT));
				todo.add(new Intention(Intention.ActionType.STEP));
				return;
			} else if (left) {
				foundWall = true;
				todo.add(new Intention(Intention.ActionType.STEP));
				return;
			} else if (right) {
				foundWall = true;
				todo.add(new Intention(Intention.ActionType.TURN_BACK));
				todo.add(new Intention(Intention.ActionType.STEP));
				return;
			} else if (behind) {
				foundWall = true;
				todo.add(new Intention(Intention.ActionType.TURN_LEFT));
				todo.add(new Intention(Intention.ActionType.STEP));
				return;
			} else {
				todo.add(new Intention(Intention.ActionType.STEP));
				return;
			}
		}

		if (foundWall) {
			if (left && !ahead) {
				todo.add(new Intention(Intention.ActionType.STEP));
			}

			if (left && ahead) {
				todo.add(new Intention(Intention.ActionType.TURN_RIGHT));
				todo.add(new Intention(Intention.ActionType.STEP));
			}

			if (!left) {
				todo.add(new Intention(Intention.ActionType.TURN_LEFT));
				todo.add(new Intention(Intention.ActionType.STEP));
			}
		}

	}
}