package eventrecorder.action;

import eventrecorder.data.LogEntry;

/**
 * Abstract class of the Command pattern.
 *
 * @author Andre Muehlenbrock
 */

public abstract class Action {
    protected LogEntry entry;
    private boolean addToHistory;

    /**
     * Constructor.
     *
     * @param entry
     * @param hidden    Should undo/redo be possible?
     */

    public Action(LogEntry entry, boolean addToHistory){
        this.entry = entry;
        this.addToHistory = addToHistory;
    }


    /**
     * Execute and redo functionality of the action
     * @return true if it worked.
     */

    public abstract boolean executeAction();

    /**
     * undo functionality of the action
     * @return true if it worked.
     */

    public abstract boolean undoAction();

    /**
     * @return Returns the effected LogEntry.
     */

    public LogEntry getAffectedLogEntry(){
        return entry;
    }

    public boolean shouldBeAddedToHistory(){
        return addToHistory;
    }
}
