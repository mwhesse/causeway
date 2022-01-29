/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package demoapp.dom.services.core.eventbusservice;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.ObjectSupport;

@DomainObject(logicalTypeName = "demo.EventLogEntry")
public abstract class EventLogEntry {

    @ObjectSupport public String title() {
        return getEvent();
    }

    public abstract String getEvent();
    public abstract void setEvent(String event);

    public abstract Acknowledge getAcknowledge();
    public abstract void setAcknowledge(Acknowledge acknowledge);


    // demonstrating 2 methods of changing a property ...
    // - inline edit
    // - via action

    public static enum Acknowledge {
        IGNORE,
        CRITICAL
    }

}
