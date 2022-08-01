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
package org.apache.isis.extensions.executionlog.jdo.dom;

import java.io.Serializable;
import java.util.StringTokenizer;
import java.util.UUID;

import org.springframework.stereotype.Component;

import org.apache.isis.applib.services.bookmark.IdStringifier;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;

@EqualsAndHashCode(of = {"interactionId", "sequence"})
@NoArgsConstructor
public class ExecutionLogEntryPK implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String SEPARATOR = "_";

    @Getter @Setter
    public UUID interactionId;
    @Getter @Setter
    public int sequence;

    public ExecutionLogEntryPK(final String value) {
        val token = new StringTokenizer (value, SEPARATOR);
        this.interactionId = UUID.fromString(token.nextToken());
        this.sequence = Integer.parseInt(token.nextToken());
    }

    @Override
    public String toString() {
        return interactionId + SEPARATOR + sequence;
    }

    @Component
    public static class Stringifier extends IdStringifier.Abstract<ExecutionLogEntryPK> {

        public Stringifier() {
            super(ExecutionLogEntryPK.class);
        }

        @Override
        public String enstring(final ExecutionLogEntryPK value) {
            return value.toString();
        }

        @Override
        public ExecutionLogEntryPK destring(
                final @NonNull String stringified,
                final Class<?> targetEntityClassIfAny) {
            return new ExecutionLogEntryPK(stringified);
        }
    }
}