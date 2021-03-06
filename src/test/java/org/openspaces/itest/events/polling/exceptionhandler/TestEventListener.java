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
package org.openspaces.itest.events.polling.exceptionhandler;

import org.openspaces.core.GigaSpace;
import org.openspaces.events.EventTemplate;
import org.openspaces.events.SpaceDataEventListener;
import org.springframework.transaction.TransactionStatus;

public class TestEventListener implements SpaceDataEventListener<Object> {

    private volatile int messageCounter = 0;

    private volatile int throwExceptionTillCounter = -1;

    public void reset() {
        this.messageCounter = 0;
        this.throwExceptionTillCounter = -1;
    }

    public void setThrowExceptionTillCounter(int throwExceptionTillCounter) {
        this.throwExceptionTillCounter = throwExceptionTillCounter;
    }

    public void onEvent(Object data, GigaSpace gigaSpace, TransactionStatus txStatus, Object source) {
        messageCounter++;
        if (messageCounter < throwExceptionTillCounter) {
            throw new RuntimeException("FAIL");
        }
    }

    public int getMessageCounter() {
        return messageCounter;
    }

    @EventTemplate
    public Object getMySpecialTemplate() {
        return new Object();
    }

}
