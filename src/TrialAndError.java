import java.awt.Graphics;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Maze Assignment: TrialAndError.java
 * 
 * A basic agent that attempts to walk through a maze, one step at a time,
 * systematically exploring the options, doubling back on reaching dead ends.
 * 
 * @author Matthew Stone
 * @version 2.0
 */

public class TrialAndError extends Agent {

	/**
	 * Class information
	 */

	/** Tag for XML element */
	static final String XML_NAME = "tryer";

	/** Size in display */
	static final int TRYER_SIZE = 15;

	/**
	 * Record that allows XML files to set default attributes for
	 * trial-and-error agent
	 */
	static FixedAgentAttributes defaultFixedAgentAttributes = new Agent.FixedAgentAttributes(
			false, false);

	/**
	 * Record that allows XML files to set default state for trial-and-error
	 * agent
	 */
	static DynamicAgentAttributes defaultDynamicAgentAttributes = new Agent.DynamicAgentAttributes(
			0, 0, Agent.Heading.EAST, false, null);

	/**
	 * Instance members
	 */
	private boolean[][] visited;
	private boolean[][] explored;
	private Agent.Heading[][] went;

	/**
	 * Constructor: initialize general agent fields to describe agent that will
	 * explore a maze by trial-and-error search.
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
	public TrialAndError(World w, int id, Attributes atts, Locator loc)
			throws SAXException {
		myWorld = w;
		this.id = id;
		form = new FixedAgentAttributes(atts, defaultFixedAgentAttributes, loc);
		status = new DynamicAgentAttributes(atts,
				defaultDynamicAgentAttributes, loc);
		int size = w.getDimension();
		visited = new boolean[size][size];
		explored = new boolean[size][size];
		went = new Agent.Heading[size][size];
		for (int i = 0; i < size; i++)
			for (int j = 0; j < size; j++) {
				visited[i][j] = false;
				explored[i][j] = false;
				went[i][j] = Agent.Heading.NORTH;
			}
	}

	/**
	 * Output an XML element describing the current state of this
	 * trial-and-error agent.
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
	 * Draw a trial-and-error agent as a solid triangle pointing in the
	 * direction of the agent's heading.
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

		double s = TRYER_SIZE;
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

	/**
	 * Update agent's todo list.
	 * 
	 * @param ps
	 *            A description of everything the agent can see
	 */
	@Override
	public void deliberate(List<Percept> ps) {

		// TODO: Include any new code here...
		todo = new LinkedList<Intention>();
		todo.add(new Intention(Intention.ActionType.STEP));
	}
}
