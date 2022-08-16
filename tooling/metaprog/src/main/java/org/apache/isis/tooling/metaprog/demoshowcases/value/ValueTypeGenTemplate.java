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
package org.apache.isis.tooling.metaprog.demoshowcases.value;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.base._Files;
import org.apache.isis.commons.internal.base._Refs;
import org.apache.isis.commons.internal.base._Text;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Singular;
import lombok.Value;
import lombok.val;

@RequiredArgsConstructor
public class ValueTypeGenTemplate {

    @Value @Builder
    public static class Config {
        final File outputRootDir;
        final String showcaseName;
        final String javaPackage;
        @Singular final Map<String, String> templateVariables = new HashMap<>();
    }

    @RequiredArgsConstructor
    enum Generator {
        DOC(".adoc"),
        JAVA(".java"),
        LAYOUT(".layout.xml");
        final String fileSuffix;
    }

    @RequiredArgsConstructor
    enum Sources {
        HOLDER("holder/%sHolder", Generator.JAVA),
        HOLDER2("holder/%sHolder2", Generator.JAVA),
        HOLDER_ACTION_RETURNING("holder/%sHolder_actionReturning", Generator.JAVA),
        HOLDER_ACTION_RETURNING_COLLECTION("holder/%sHolder_actionReturningCollection", Generator.JAVA),
        HOLDER_MIXIN_PROPERTY("holder/%sHolder_mixinProperty", Generator.JAVA),
        HOLDER_UPDATE_READONLY_OPTIONAL_PROPERTY("holder/%sHolder_updateReadOnlyOptionalProperty", Generator.JAVA),
        HOLDER_updateReadOnlyProperty("holder/%sHolder_updateReadOnlyProperty", Generator.JAVA),
        HOLDER_updateReadOnlyPropertyWithChoices("holder/%sHolder_updateReadOnlyPropertyWithChoices", Generator.JAVA),
        COLLECTION("%ss", Generator.JAVA),
        JDO("jdo/%sJdo", Generator.JAVA),
        JDO_ENTITIES("jdo/%sJdoEntities", Generator.JAVA),
        JPA("jpa/%sJpa", Generator.JAVA),
        JPA_ENTITIES("jpa/%sJpaEntities", Generator.JAVA),
        ENTITY("persistence/%sEntity", Generator.JAVA),
        SEEDING("persistence/%sSeeding", Generator.JAVA),
        SAMPLES("samples/%sSamples", Generator.JAVA),
        VIEWMODEL("vm/%sVm", Generator.JAVA),

        COMMON_DOC("%ss-common", Generator.DOC),
        DESCRIPTION("%ss-description", Generator.DOC),
        JDO_DESCRIPTION("jdo/%sJdo-description", Generator.DOC),
        JPA_DESCRIPTION("jpa/%sJpa-description", Generator.DOC),
        VIEWMODEL_DESCRIPTION("vm/%sVm-description", Generator.DOC),

        COLLECTION_LAYOUT("%ss", Generator.LAYOUT),
        ENTITY_LAYOUT("persistence/%sEntity", Generator.LAYOUT),
        VIEWMODEL_LAYOUT("vm/%sVm", Generator.LAYOUT)

        ;
        private final String pathTemplate;
        private final Generator generator;
        private final File file(final Config config) {
            return new File(config.getOutputRootDir(),
                    String.format(pathTemplate, config.getShowcaseName())
                    + generator.fileSuffix)
                    .getAbsoluteFile();
        }
        private final File template() {
            return new File("src/main/resources",
                    String.format(pathTemplate, "Template")
                    + generator.fileSuffix)
                    .getAbsoluteFile();
        }
        private final String javaPackage(final Config config) {
            return Optional.ofNullable(new File(String.format(pathTemplate, "X")).getParent())
                    .map(path->path.replace('/', '.'))
                    .map(suffix->config.javaPackage + "." + suffix)
                    .orElse(config.javaPackage);
        }
    }

    final Config config;

    public void generate(final Consumer<File> onSourceGenerated) {

        for(var source: Sources.values()) {
            val template = source.template();

            _Assert.assertTrue(template.exists(), ()->String.format("template %s not found", template));

            val genTarget = source.file(config);

            val templateVars = new HashMap<String, String>();
            templateVars.putAll(config.templateVariables);
            templateVars.put("java-package", source.javaPackage(config));
            templateVars.put("showcase-name", config.showcaseName);
            templateVars.put("showcase-type", "java.util.UUID");
            templateVars.put("showcase-value-semantics-provider",
                    "org.apache.isis.core.metamodel.valuesemantics.UUIDValueSemantics");

            generateFromTemplate(templateVars, template, genTarget);
            onSourceGenerated.accept(genTarget);
        }

    }

    private void generateFromTemplate(
            final Map<String, String> templateVars, final File template, final File genTarget) {
        val templateLines = _Text.readLinesFromFile(template, StandardCharsets.UTF_8);

        _Files.makeDir(genTarget.getParentFile());

        _Text.writeLinesToFile(templateLines
                .map(line->templateProcessor(templateVars, line)),
                genTarget, StandardCharsets.UTF_8);
    }

    private String templateProcessor(final Map<String, String> templateVars, final String line) {
        val lineRef = _Refs.stringRef(line);
        templateVars.forEach((key, value)->{
            lineRef.update(s->s.replace("/*${" + key + "}*/", value));
        });
        return lineRef.getValue();
    }

}