/*******************************************************************************
 * 
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
 *  
 ******************************************************************************/
package org.openspaces.admin.internal.transport.events;

import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.support.GroovyHelper;
import org.openspaces.admin.transport.events.TransportStatisticsChangedEvent;
import org.openspaces.admin.transport.events.TransportStatisticsChangedEventListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author kimchy
 */
public class DefaultTransportStatisticsChangedEventManager implements InternalTransportStatisticsChangedEventManager {

    private final InternalAdmin admin;

    private final List<TransportStatisticsChangedEventListener> listeners = new CopyOnWriteArrayList<TransportStatisticsChangedEventListener>();

    public DefaultTransportStatisticsChangedEventManager(InternalAdmin admin) {
        this.admin = admin;
    }

    public void transportStatisticsChanged(final TransportStatisticsChangedEvent event) {
        for (final TransportStatisticsChangedEventListener listener : listeners) {
            admin.raiseEvent(listener, new Runnable() {
                public void run() {
                    listener.transportStatisticsChanged(event);
                }
            });
        }
    }

    public void add(TransportStatisticsChangedEventListener eventListener) {
        listeners.add(eventListener);
    }

    public void remove(TransportStatisticsChangedEventListener eventListener) {
        listeners.remove(eventListener);
    }

    public void plus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            add(new ClosureTransportStatisticsChangedEventListener(eventListener));
        } else {
            add((TransportStatisticsChangedEventListener) eventListener);
        }
    }

    public void leftShift(Object eventListener) {
        plus(eventListener);
    }

    public void minus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            remove(new ClosureTransportStatisticsChangedEventListener(eventListener));
        } else {
            remove((TransportStatisticsChangedEventListener) eventListener);
        }
    }

    public void rightShift(Object eventListener) {
        minus(eventListener);
    }
}
