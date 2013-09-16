/**
 * Maze Assignment: Intention.java
 *
 * Class for representing an agent's intentions.
 * So far allows for an agent's intention to
 * change speed or change direction.
 * 
 * @author Matthew Stone
 * @version 1.0
 *
 */
public class Intention {

    /**
     * Class information
     */

    /**
     * Enumerates the different kinds of things
     * that an agent can intend to do.
     * "description" allows intentions to be
     * pretty-printed.
     */
    public static enum ActionType {
        TURN_LEFT ("turn left"),
        TURN_RIGHT ("turn right"),
        TURN_BACK ("turn back"),
        STEP ("step");
        public final String description;
        private ActionType(String d) {
            description = d;
        }
    }

    /**
     * Instance members
     */
    
    /** what kind of thing does this intention describe */
    private ActionType type;
 
    /**
     * Constructor
     * 
     * @param type what to do 
     */
    public Intention(ActionType type) {
        this.type = type;
    }

    /**
     * Accessor
     * @return type of action inteded
     */
    public ActionType getType() {
        return type;
    }

}
