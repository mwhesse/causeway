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
package org.apache.causeway.core.config;

import java.util.Collections;
import java.util.Map;

import org.apache.causeway.core.config.applib.RestfulPathProvider;
import org.apache.causeway.core.config.beans.IsisBeanFactoryPostProcessorForSpring;
import org.apache.causeway.core.config.beans.IsisBeanTypeRegistryDefault;
import org.apache.causeway.core.config.converters.PatternsConverter;
import org.apache.causeway.core.config.datasources.DataSourceIntrospectionService;
import org.apache.causeway.core.config.environment.IsisLocaleInitializer;
import org.apache.causeway.core.config.environment.CausewaySystemEnvironment;
import org.apache.causeway.core.config.environment.IsisTimeZoneInitializer;
import org.apache.causeway.core.config.validators.PatternOptionalStringConstraintValidator;
import org.apache.causeway.core.config.viewer.web.WebAppContextPath;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import lombok.Data;

@Configuration
@Import({

    // @Component's
    PatternsConverter.class,
    IsisBeanFactoryPostProcessorForSpring.class,
    IsisLocaleInitializer.class,
    IsisTimeZoneInitializer.class,
    PatternOptionalStringConstraintValidator.class,
    RestfulPathProvider.class,

    // @Service's
    DataSourceIntrospectionService.class,
    IsisBeanTypeRegistryDefault.class,
    CausewaySystemEnvironment.class,
    WebAppContextPath.class,

})
@EnableConfigurationProperties({
        CausewayConfiguration.class,
        DatanucleusConfiguration.class,
        EclipselinkConfiguration.class,
        EclipselinkConfiguration.Weaving.class,
        EclipselinkConfiguration.DdlGeneration.class,
        EclipselinkConfiguration.Jdbc.BatchWriting.class,
        EclipselinkConfiguration.Jdbc.CacheStatements.class,
        RestEasyConfiguration.class,
        CausewayModuleCoreConfig.ConfigProps.class,
})
public class CausewayModuleCoreConfig {

    public static final String NAMESPACE = "isis.config";

    @ConfigurationProperties(prefix = "", ignoreUnknownFields = true)
    @Data
    public static class ConfigProps {
        private Map<String, String> isis = Collections.emptyMap();
        private Map<String, String> resteasy = Collections.emptyMap();
        private Map<String, String> datanucleus = Collections.emptyMap();
        private Map<String, String> eclipselink = Collections.emptyMap();
    }

}
