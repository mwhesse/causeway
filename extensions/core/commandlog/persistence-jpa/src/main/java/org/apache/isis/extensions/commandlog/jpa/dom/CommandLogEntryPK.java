/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.isis.extensions.commandlog.jpa.dom;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embeddable;

import org.springframework.stereotype.Component;

import org.apache.isis.applib.services.bookmark.IdStringifier;
import org.apache.isis.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.isis.persistence.jpa.integration.typeconverters.java.util.JavaUtilUuidConverter;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@EqualsAndHashCode(of = {"interactionId"})
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class CommandLogEntryPK implements Serializable {

    private static final long serialVersionUID = 1L;

    @Convert(converter = JavaUtilUuidConverter.class)
    @Column(name = CommandLogEntry.InteractionId.NAME, nullable = CommandLogEntry.InteractionId.NULLABLE, length = CommandLogEntry.InteractionId.MAX_LENGTH)
    @Getter(AccessLevel.PACKAGE)
    private UUID interactionId;

    @Override
    public String toString() {
        return interactionId != null ? interactionId.toString() : null;
    }


    @Component
    public static class Stringifier extends IdStringifier.Abstract<CommandLogEntryPK> {

        public Stringifier() {
            super(CommandLogEntryPK.class);
        }

        @Override
        public String enstring(CommandLogEntryPK value) {
            return value.getInteractionId().toString();
        }

        @Override
        public CommandLogEntryPK destring(@NonNull String stringified, @NonNull Class<?> targetEntityClass) {
            return new CommandLogEntryPK(UUID.fromString(stringified));
        }
    }
}