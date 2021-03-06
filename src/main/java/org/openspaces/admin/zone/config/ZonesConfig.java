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
package org.openspaces.admin.zone.config;

import java.util.Map;
import java.util.Set;

/**
 * @author elip
 * @since 9.1.0
 */
public interface ZonesConfig {
    
    /**
     * @return the internal key/value string representation
     */
    Map<String, String> getProperties();
    
    /**
     * Checks the content of this config is valid.
     * @throws IllegalStateException - if state is found to be illegal
     */
    void validate() throws IllegalStateException;
    
    /**
     * returns the zones attached to this config.
     * @return
     */
    Set<String> getZones();
    
    /**
     * @param zones A unique list of labels(tags) that represent different zones 
     */
    void setZones(Set<String> zones);
    
    /**
     * @return true iff the specified zones satisfies or equals this zones requirement
     */
    boolean isSatisfiedBy(ExactZonesConfig zoneStatisticsConfig);

}
