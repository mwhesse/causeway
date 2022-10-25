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
package org.apache.causeway.persistence.jdo.datanucleus;

import org.apache.causeway.persistence.jdo.datanucleus.config.DatanucleusSettings;
import org.apache.causeway.persistence.jdo.datanucleus.mixins.Persistable_datanucleusVersionLong;
import org.apache.causeway.persistence.jdo.datanucleus.mixins.Persistable_datanucleusVersionTimestamp;
import org.apache.causeway.persistence.jdo.datanucleus.mixins.Persistable_downloadJdoMetadata;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import lombok.extern.log4j.Log4j2;

/**
 * @since 2.0 {@index}
 */
@Configuration
@Import({
    // modules
    CausewayModulePersistenceJdoDatanucleus.class,

    // @Mixin's
    Persistable_datanucleusVersionLong.class,
    Persistable_datanucleusVersionTimestamp.class,
    Persistable_downloadJdoMetadata.class,

})
@EnableConfigurationProperties(DatanucleusSettings.class)
@Log4j2
public class CausewayModulePersistenceJdoDatanucleusMixins {

}