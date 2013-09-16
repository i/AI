

/**
 * Maze Assignment: Percept.java
 *
 * Class for representing distance readings in the maze world
 * 
 * @author Matthew Stone
 * @version 1.0
 *
 */
public class Percept {
    
    /**
     * Percepts give qualitatively different information about
     * what was seen; the different possibilities are given
     * as the value of the ObjectCategory type.
     */
    static enum ObjectCategory {
        /** An inert object that must be avoided */
        OBSTACLE;
    }
        
    /** What was seen */
    private ObjectCategory objectCategory;
    /** How far away the object sits */
    private int distance;
    /** Where perceived obstacle is relative to you (AHEAD, LEFT, RIGHT, BEHIND) */
    private Agent.Direction direction;

    /**
     * Constructor for percept object
     * 
     * @param c what was seen
     * @param dis how far perceived agent was
     * @param dir where perceived agent is relative to you
     */
    public Percept(ObjectCategory c, int dis, Agent.Direction dir) {
        objectCategory = c;
        distance = dis;
        direction = dir;
    }
    
    /**
     * Accessor
     * @return what was seen
     */
    public ObjectCategory getObjectCategory() {
        return objectCategory;
    }

    /**
     * Accessor
     * @return how far perceived agent was
     */
    public int getDistance() {
        return distance;
    }
    
    /**
     * Accessor
     * @return where perceived agent is relative to you
     */
    public Agent.Direction getDirection() {
        return direction;
    }
}
