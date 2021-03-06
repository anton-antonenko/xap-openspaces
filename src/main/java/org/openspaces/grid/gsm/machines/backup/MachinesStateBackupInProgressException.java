package org.openspaces.grid.gsm.machines.backup;

import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.grid.gsm.machines.exceptions.GridServiceAgentSlaEnforcementInProgressException;

public class MachinesStateBackupInProgressException extends GridServiceAgentSlaEnforcementInProgressException {

    private static final long serialVersionUID = 1L;

    public MachinesStateBackupInProgressException(ProcessingUnit pu) {
        super(pu, "Machines state is being stored in the Space.");
    }
}
