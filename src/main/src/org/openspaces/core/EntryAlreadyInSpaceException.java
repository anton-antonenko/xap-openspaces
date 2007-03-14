package org.openspaces.core;

import org.springframework.dao.DataIntegrityViolationException;

/**
 * This exception is thrown when write operation is rejected when the entry (or another with same
 * UID) is already in space.
 * 
 * @author kimchy
 */
public class EntryAlreadyInSpaceException extends DataIntegrityViolationException {

    private static final long serialVersionUID = -8553568598873283849L;

    private com.j_spaces.core.client.EntryAlreadyInSpaceException e;

    public EntryAlreadyInSpaceException(com.j_spaces.core.client.EntryAlreadyInSpaceException e) {
        super(e.getMessage(), e);
        this.e = e;
    }

    /**
     * Returns the rejected entry UID.
     */
    public String getUID() {
        return e.getUID();
    }

    /**
     * Returns the rejected entry class name.
     */
    public String getClassName() {
        return e.getClassName();
    }
}
