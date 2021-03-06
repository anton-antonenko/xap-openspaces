/*******************************************************************************
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.openspaces.grid.gsm.machines.exceptions;

import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.internal.machine.events.DefaultElasticMachineProvisioningFailureEvent;
import org.openspaces.admin.internal.pu.elastic.events.InternalElasticProcessingUnitFailureEvent;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementFailure;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementLoggerBehavior;

/**
 * @author elip
 *
 */
public class FailedToStopMachineException extends MachinesSlaEnforcementInProgressException implements SlaEnforcementFailure, SlaEnforcementLoggerBehavior {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private String agentUid;

    /**
     * @param pu
     */
    public FailedToStopMachineException(ProcessingUnit pu, GridServiceAgent agent, Exception exception) { 
        super(pu, message(agent) + " Cause: " + exception.getMessage(), exception);
        this.agentUid = agent.getUid();
    }

    /* (non-Javadoc)
     * @see org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementFailure#toEvent()
     */
    @Override
    public InternalElasticProcessingUnitFailureEvent toEvent() {
        DefaultElasticMachineProvisioningFailureEvent event = new DefaultElasticMachineProvisioningFailureEvent(); 
        event.setFailureDescription(getMessage());
        event.setProcessingUnitName(getProcessingUnitName());
        return event;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((agentUid == null) ? 0 : agentUid.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        FailedToStopMachineException other = (FailedToStopMachineException) obj;
        if (agentUid == null) {
            if (other.agentUid != null)
                return false;
        } else if (!agentUid.equals(other.agentUid))
            return false;
        return true;
    }
    
    private static String message(GridServiceAgent agent) {
        String errorMessage = "Failed shutting down machine with ip " 
                + agent.getMachine().getHostAddress() + " of agent " + agent.getUid()
                + "Shutdown this machine manually";
        return errorMessage;
    }
    
    @Override
    public boolean isAlwaysLogStackTrace() {
        return true;
    }

    @Override
    public boolean isAlwaysLogDuplicateException() {
        return true;
    }


}
