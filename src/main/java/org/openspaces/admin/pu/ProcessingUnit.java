/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.admin.pu;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.gigaspaces.admin.quiesce.QuiesceState;
import org.openspaces.admin.AdminAware;
import org.openspaces.admin.StatisticsMonitor;
import org.openspaces.admin.application.Application;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.pu.dependency.ProcessingUnitDependencies;
import org.openspaces.admin.pu.dependency.ProcessingUnitDependency;
import org.openspaces.admin.pu.elastic.config.ScaleStrategyConfig;
import org.openspaces.admin.pu.events.BackupGridServiceManagerChangedEventManager;
import org.openspaces.admin.pu.events.ManagingGridServiceManagerChangedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceAddedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceLifecycleEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceProvisionStatusChangedEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceProvisionStatusChangedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceRemovedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceStatisticsChangedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitSpaceCorrelatedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitStatusChangedEventManager;
import org.openspaces.admin.quiesce.QuiesceDetails;
import org.openspaces.admin.quiesce.QuiesceRequest;
import org.openspaces.admin.quiesce.QuiesceResult;
import org.openspaces.admin.space.Space;
import org.openspaces.admin.zone.config.RequiredZonesConfig;
import org.openspaces.core.properties.BeanLevelProperties;

/**
 * A processing unit holds one or more {@link org.openspaces.admin.pu.ProcessingUnitInstance}s.
 *
 * @author kimchy
 * @author itaif
 */
public interface ProcessingUnit extends Iterable<ProcessingUnitInstance>, AdminAware, StatisticsMonitor {

    /**
     * @return Returns the handle to all the different processing units.
     */
    ProcessingUnits getProcessingUnits();
    
    /**
     * @return Returns the name of the processing unit.
     */
    String getName();

    /**
     * @return Returns the number of required instances as defined in the processing unit's SLA.
     * If there are backups, it will only return the number of primary instances and not the number of backup.
     * @deprecated - since 9.5.0 - please use the following alternatives:
     * For retrieving planned number of partitions use {@link #getPlannedNumberOfPartitions()}.
     * Otherwise use {@link #getPlannedNumberOfInstances()} for retrieving planned number of
     * stateful (primary+backup Space instances) -or- stateless pu instances.
     * <p/>
     * <b>Note</b> that this method does not count the number of running instances, but rather the number of planned
     * instances for the processing unit. To count the number of currently <b>discovered</b> running processing unit
     * instances use the method {@link #getInstances()}.
     */
    @Deprecated
    int getNumberOfInstances();

    /**
     * For stateful processing unit (primary+backup Space) will return the planned partition count - i.e. the number of planned primary
     * instances (excluding backups). If called for a stateless processing unit, will return the planned number of instances.
     * <p/>
     * <b>Note</b> that this method does not count the number of running instances, but rather the number of planned
     * primary instances for the processing unit. To count the number of currently <b>discovered</b> running processing unit partitions
     * use the method {@link #getPartitions()}.
     *
     * @return Returns the number of planned primary instances excluding backup instances.
     * @since 9.6
     */
    int getPlannedNumberOfPartitions();

    /**
     * For stateful processing unit (primary+backup Space) will return the <b>current</b> planned number of instances (including backups).
     * For stateless processing unit will return the number of <b>current</b> planned instances. The plan denoted by the initial SLA may
     * change due to increments/decrements either performed manually or enforced by the elasticity properties (e.g. scaling rules, re-balancing, etc).
     * <p/>
     * <b>Note</b> that this method does not count the number of running instances, but rather the number of planned
     * number of instances for the processing unit. To count the number of currently <b>discovered</b> running processing unit instances
     * use the method {@link #getInstances()}.
     *
     * @return Returns the number of required instances as defined in the processing unit's <b>current</b> SLA.
     * @since 9.5.0
     */
    int getPlannedNumberOfInstances();

    /**
     * @return Returns the number of backups (if the topology is a backup one) per instance, as defined in the
     * processing unit's SLA. Note that this method does not return he number of running backup instances, but
     * rather the number of planned backup instances per primary as defined by the initial SLA.
     */
    int getNumberOfBackups();

    /**
     * @return Returns the total required number of instances as defined in the processing SLA.
     * If there are no backups, will return{@link #getNumberOfInstances()}. If there are backups,
     * will return {@link #getNumberOfInstances()} * ({@link #getNumberOfBackups()}  + 1)
     * @deprecated - since 9.6.0 - please use {@link #getPlannedNumberOfInstances()}
     * <p/>
     * <b>Note</b> that this method does not count the number of running instances, but rather the total number of planned
     * instances for the processing unit. To count the number of active processing unit instances please use the method
     * {@link #getInstances()}.
     */
    @Deprecated
    int getTotalNumberOfInstances();

    /**
     * @return Returns the number of instances of this processing unit that can run within a VM.
     *
     * <p>In case of a partitioned with backup topology, it applies on a per partition level (meaning that a
     * primary and backup will not run on the same VM).
     *
     * <p>In case of a non backup based topology, it applies on the number of instances of the whole processing
     * unit that can run on the same VM).
     */
    int getMaxInstancesPerVM();

    /**
     * @return Returns the number of instances of this processing unit that can run within a Machine.
     *
     * <p>In case of a partitioned with backup topology, it applies on a per partition level (meaning that a
     * primary and backup will not run on the same Machine).
     *
     * <p>In case of a non backup based topology, it applies on the number of instances of the whole processing
     * unit that can run on the same Machine). 
     */
    int getMaxInstancesPerMachine();

    /**
     * @return true if isolation is required per virtual machine. No processing unit instances can run on the same
     * virtual machine. Default false.
     * @since 10.1.0
     */
    boolean isRequiresIsolation();

    /**
     * @return Returns a map containing the zone name and the maximum number of instances for that zone.
     */
    Map<String, Integer> getMaxInstancesPerZone();

    /**
     * @return Returns the list of zones this processing units are required to run on. If there is more than
     * one zone, the processing unit can run on either of the zones.
     * @deprecated This method is deprecated in favor of {@link #getRequiredContainerZones()}
     */
    @Deprecated
    String[] getRequiredZones();

    /**
     * @return the @{link GridServiceContainer} zones that can satisfy this processing unit.
     * 
     * For example:
     * boolean gscMatch = pu.getRequiredContainerZones().isSatisfiedBy(gsc.getExactZones());
     */
    RequiredZonesConfig getRequiredContainerZones();
    
    /**
     * @return Returns the deployment status of the processing unit.
     */
    DeploymentStatus getStatus();

    /**
     * @return Return the deploy time properties of the processing unit.
     */
    BeanLevelProperties getBeanLevelProperties();
    
    /**
     * @return Returns the type of processing unit: stateless, stateful, mirror, web.
     * @since 8.0.3
     */
    ProcessingUnitType getType();

    /**
     * Waits till at least the provided number of Processing Unit Instances are up.
     *
     * @return {@code true} if discovered the required number of instances within the default timeout and {@code false}
     * if the waiting time elapsed before the discovery took place
     */
    boolean waitFor(int numberOfProcessingUnitInstances);

    /**
     * Waits till at least the provided number of Processing Unit Instances are up for the specified timeout.
     *
     * @return {@code true} if discovered the required number of instances within the specified timeout and {@code false}
     * if the waiting time elapsed before the discovery took place
     */
    boolean waitFor(int numberOfProcessingUnitInstances, long timeout, TimeUnit timeUnit);

    /**
     * Waits till an embedded Space is correlated with the processing unit.
     *
     * @return {@code true} if Space was correlated within the default timeout and {@code false}
     * if the waiting time elapsed before correlation took place
     */
    Space waitForSpace();

    /**
     * Waits till an embedded Space is correlated with the processing unit for the specified timeout.
     *
     * @return {@code true} if Space was correlated within the specified timeout and {@code false}
     * if the waiting time elapsed before correlation took place
     */
    Space waitForSpace(long timeout, TimeUnit timeUnit);

    /**
     * Waits till there is a managing {@link org.openspaces.admin.gsm.GridServiceManager} for the processing unit.
     *
     * @return {@code true} if GSM was discovered within the default timeout and {@code false}
     * if the waiting time elapsed before discovery took place
     */
    GridServiceManager waitForManaged();

    /**
     * Waits till there is a managing {@link org.openspaces.admin.gsm.GridServiceManager} for the processing unit
     * for the specified timeout.
     *
     * @return {@code true} if GSM was discovered within the specified timeout and {@code false}
     * if the waiting time elapsed before discovery took place
     */
    GridServiceManager waitForManaged(long timeout, TimeUnit timeUnit);

    /**
     * @return Returns <code>true</code> if this processing unit allows to increment instances on it.
     */
    boolean canIncrementInstance();

    /**
     * @return Returns <code>true</code> if this processing unit allows to decrement instances on it.
     */
    boolean canDecrementInstance();

    /**
     * Increments the number of processing unit instances.
     * Does not apply for partitioned nor replicated topologies.
     */
    void incrementInstance();

    /**
     * Removes a randomly chosen instance from the processing unit, and decrements the number of instances. 
     * For finer control use {@link ProcessingUnitInstance#decrement()} instead.
     * Does not apply for partitioned nor replicated topologies.
     * @since 10.1.0 decrements planned instances before decrementing an actual instance
     */
    void decrementInstance();

    /**
     * @return Returns <code>true</code> if there is a managing GSM for it.
     */
    boolean isManaged();

    /**
     * @return Returns the managing (primary) GSM for the processing unit.
     */
    GridServiceManager getManagingGridServiceManager();

    /**
     * @return Returns the backup GSMs for the processing unit.
     */
    GridServiceManager[] getBackupGridServiceManagers();

    /**
     * @return Returns the backup GSM matching the provided UID.
     */
    GridServiceManager getBackupGridServiceManager(String gridServiceManagerUID);

    /**
     * @see ProcessingUnit#undeployAndWait()
     * @see ProcessingUnit#undeployAndWait(long, TimeUnit) 
     */
    void undeploy();

    /**
     * Un-deploys the processing unit and waits until all instances have been undeployed.
     * In case of an Elastic processing unit, also waits for containers to shutdown.
     * 
     * <p>The undeployment process will wait indefinitely and return when all processing units have undeployed.
     * 
     * @see ProcessingUnit#undeployAndWait(long, TimeUnit)
     * @see ProcessingUnit#undeploy()
     * @since 8.0.5
     */
    void undeployAndWait();
    
    /**
     * Undeploy the processing unit and wait until all instances have been undeployed.
     * In case of an Elastic processing unit, it waits until all containers have been removed.
     * 
     * <p>The undeployment process will wait for the given timeout and return when all processing units have undeployed or timeout expired.
     * 
     * @return True if un-deploy completed successfully within the specified timeout. False if undeploy is still in progress.
     * @see ProcessingUnit#undeployAndWait()
     * @see ProcessingUnit#undeploy()
     * @since 8.0.5
     */
    boolean undeployAndWait(long timeout, TimeUnit timeunit);
    
    /**
     * @return Returns the (first) embedded space within a processing unit. Returns <code>null</code> if
     * no embedded space is defined within the processing unit or if no processing unit instance
     * has been added to the processing unit.
     */
    Space getSpace();

    /**
     * @return Returns all the embedded spaces within a processing unit. Returns an empty array if there
     * are no embedded spaces defined within the processing unit, or none has been associated with
     * the processing unit yet.
     */
    Space[] getSpaces();

    /**
     * @return Returns the processing unit instances currently discovered.
     */
    ProcessingUnitInstance[] getInstances();

    /**
     * @return Returns the processing unit partitions of this processing unit.
     */
    ProcessingUnitPartition[] getPartitions();

    /**
     * @return Returns a processing unit partition based on the specified partition id.
     */
    ProcessingUnitPartition getPartition(int partitionId);

    /**
     * @return Returns an event manager allowing to register {@link org.openspaces.admin.pu.events.ProcessingUnitInstanceAddedEventListener}s.
     */
    ProcessingUnitInstanceAddedEventManager getProcessingUnitInstanceAdded();

    /**
     * @return Returns an event manager allowing to register {@link org.openspaces.admin.pu.events.ProcessingUnitInstanceRemovedEventListener}s.
     */
    ProcessingUnitInstanceRemovedEventManager getProcessingUnitInstanceRemoved();

    /**
     * Adds a {@link ProcessingUnitInstanceLifecycleEventListener}.
     */
    void addLifecycleListener(ProcessingUnitInstanceLifecycleEventListener eventListener);

    /**
     * Removes a {@link ProcessingUnitInstanceLifecycleEventListener}.
     */
    void removeLifecycleListener(ProcessingUnitInstanceLifecycleEventListener eventListener);

    /**
     * @return Returns an event manger allowing to listen for {@link org.openspaces.admin.pu.events.ManagingGridServiceManagerChangedEvent}s.
     */
    ManagingGridServiceManagerChangedEventManager getManagingGridServiceManagerChanged();

    /**
     * @return Returns an event manager allowing to listen for {@link org.openspaces.admin.pu.events.BackupGridServiceManagerChangedEvent}s.
     */
    BackupGridServiceManagerChangedEventManager getBackupGridServiceManagerChanged();

    /**
     * @return Returns an event manager allowing to listen for {@link org.openspaces.admin.pu.events.ProcessingUnitStatusChangedEvent}s.
     */
    ProcessingUnitStatusChangedEventManager getProcessingUnitStatusChanged();

    /**
     * @return Returns an event manager allowing to listen for {@link org.openspaces.admin.pu.events.ProcessingUnitSpaceCorrelatedEvent}s.
     */
    ProcessingUnitSpaceCorrelatedEventManager getSpaceCorrelated();

    /**
     * @return Returns a processing unit instance statistics change event manger allowing to register for
     * events of {@link org.openspaces.admin.pu.events.ProcessingUnitInstanceStatisticsChangedEvent}.
     *
     * <p>Note, in order to receive events, the virtual machines need to be in a "statistics" monitored
     * state.
     */
    ProcessingUnitInstanceStatisticsChangedEventManager getProcessingUnitInstanceStatisticsChanged();
    
    /**
     * @return Returns an event manager allowing to register {@link ProcessingUnitInstanceProvisionStatusChangedEventListener}s.
     * @since 8.0.6
     */
    ProcessingUnitInstanceProvisionStatusChangedEventManager getProcessingUnitInstanceProvisionStatusChanged();
    
    /**
     * @return Returns an event manager allowing to register {@link ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventListener}s.
     * @since 8.0.6
     */
    ProcessingUnitInstanceMemberAliveIndicatorStatusChangedEventManager getProcessingUnitInstanceMemberAliveIndicatorStatusChanged();
            
    /**
     * Modifies the processing unit scalability strategy.
     * 
     * This method is only available if the processing unit deployment is elastic  
     * 
     * @since 8.0
     * @see ProcessingUnit#scaleAndWait(ScaleStrategyConfig)
     * @see ProcessingUnit#scaleAndWait(ScaleStrategyConfig, long, TimeUnit)
     */
    void scale(ScaleStrategyConfig strategyConfig);
    
    /**
     * Modifies the processing unit scalability strategy and waits until scale is complete
     * 
     * This method is only available if the processing unit deployment is elastic  
     * 
     * @since 8.0.5
     * @see ProcessingUnit#scale(ScaleStrategyConfig)
     */
    void scaleAndWait(ScaleStrategyConfig strategyConfig);

    /**
     * Modifies the processing unit scalability strategy and waits until scale is complete
     * 
     * This method is only available if the processing unit deployment is elastic  
     * 
     * @return <code>false</code> if timeout occurred before scale operation has completed.
     * 
     * @since 8.0.5
     * @see ProcessingUnit#scale(ScaleStrategyConfig)
     */
    boolean scaleAndWait(ScaleStrategyConfig strategyConfig, long timeout, TimeUnit timeUnit);
    
    /**
     * @return the application that this processing unit is associated with or null if this
     *         processing unit is not part of an application
     * 
     * @since 8.0.3
     */
    Application getApplication();

    /**
     * @return the dependencies this processing unit has on other processing units.
     * @since 8.0.6
     */
    ProcessingUnitDependencies<ProcessingUnitDependency> getDependencies();

    /**
     * Requests a quiesce request from the GSM.
     * If the request ended successfully ({@link #waitFor(com.gigaspaces.admin.quiesce.QuiesceState)} returned true)
     * all space instances and listeners will switch to quiesced mode.
     * If the GSM rejects the request an exception with the rejection failure will be thrown.
     * (precondition: the processing unit is intact)
     * @param request with the quiesce description
     * @return {@link org.openspaces.admin.quiesce.QuiesceRequest} if the request was approved by the server
     */
    QuiesceResult quiesce(QuiesceRequest request);

    /**
     * Requests a unquiesce request from the GSM.
     * If the request ended successfully ({@link #waitFor(com.gigaspaces.admin.quiesce.QuiesceState)} returned true)
     * all space instances and listeners will switch to unquiesced mode.
     * If the GSM rejects the request an exception with the rejection failure will be thrown.
     * (precondition: the processing unit is intact)
     * @param request with the quiesce description
     * @return {@link org.openspaces.admin.quiesce.QuiesceRequest} if the request was approved by the server
     */
    void unquiesce(QuiesceRequest request);

    /**
     * @param desiredState
     * @param timeout
     * @param timeUnit
     * @return {@code true} if the processing unit reached to desired as well as all instances in the requested timeout, {@code false} otherwise.
     */
    boolean waitFor(QuiesceState desiredState, long timeout, TimeUnit timeUnit);

    /**
     * Same as {@link #waitFor(com.gigaspaces.admin.quiesce.QuiesceState, long, java.util.concurrent.TimeUnit)} but with endless timeout
     */
    boolean waitFor(QuiesceState desiredState);

    /**
     * @return the quiesce details of the processing unit - {@link QuiesceDetails}
     */
    QuiesceDetails getQuiesceDetails();
}
