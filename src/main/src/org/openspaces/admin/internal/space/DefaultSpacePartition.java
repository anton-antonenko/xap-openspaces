package org.openspaces.admin.internal.space;

import com.gigaspaces.cluster.activeelection.SpaceMode;
import org.openspaces.admin.space.Space;
import org.openspaces.admin.space.SpaceInstance;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kimchy
 */
public class DefaultSpacePartition implements InternalSpacePartition {

    private final Space space;

    private final int partitionId;

    private final Map<String, SpaceInstance> spaceInstances = new ConcurrentHashMap<String, SpaceInstance>();

    public DefaultSpacePartition(Space space, int partitionId) {
        this.space = space;
        this.partitionId = partitionId;
    }

    public Space getSpace() {
        return this.space;
    }

    public SpaceInstance getPrimary() {
        for (SpaceInstance spaceInstance : this) {
            if (spaceInstance.getMode() == SpaceMode.PRIMARY) {
                return spaceInstance;
            }
        }
        return null;
    }

    public SpaceInstance getBackup() {
        for (SpaceInstance spaceInstance : this) {
            if (spaceInstance.getMode() == SpaceMode.BACKUP) {
                return spaceInstance;
            }
        }
        return null;
    }

    public Iterator<SpaceInstance> iterator() {
        return spaceInstances.values().iterator();
    }

    public SpaceInstance[] getInstances() {
        return spaceInstances.values().toArray(new SpaceInstance[0]);
    }

    public int getPartitiondId() {
        return this.partitionId;
    }

    public void addSpaceInstance(SpaceInstance spaceInstance) {
        spaceInstances.put(spaceInstance.getUid(), spaceInstance);
    }

    public void removeSpaceInstance(String uid) {
        spaceInstances.remove(uid);
    }
}
