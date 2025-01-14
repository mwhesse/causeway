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
package org.apache.causeway.commons.internal.base;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import lombok.experimental.UtilityClass;

/**
 * package private utility for {@link _Bytes}
 */
@UtilityClass
class _Bytes_ZLibCompressor {

    static byte[] compress(final byte[] input) throws IOException {
        final int BUFFER_SIZE = Math.max(256, input.length); // at least 256

        final ByteArrayOutputStream os = new ByteArrayOutputStream(BUFFER_SIZE);
        final DeflaterOutputStream zlibOs = new DeflaterOutputStream(os);
        zlibOs.write(input);
        zlibOs.close();

        return os.toByteArray();
    }

    static byte[] decompress(final byte[] compressed) throws IOException {

        final int BUFFER_SIZE = 512;

        final ByteArrayInputStream is = new ByteArrayInputStream(compressed);
        final InflaterInputStream zlibIs = new InflaterInputStream(is, new Inflater(), BUFFER_SIZE);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final byte[] data = new byte[BUFFER_SIZE];
        int bytesRead;
        while ((bytesRead = zlibIs.read(data)) != -1) {
            baos.write(data, 0, bytesRead);
        }
        zlibIs.close();
        return baos.toByteArray();
    }

}
