/*
 * (c) Copyright 2022 Micro Focus or one of its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microfocus.adm.almoctane.importer.tool.excel.configuration;

import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Mapping utils.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MappingsUtils {

    /**
     * @param filePath The path to the *.json file.
     * @param cls      The class which will be used to parse the input JSON.
     * @param <T>      The type of mapping that will be returned.
     *
     * @return The parsed JSON.
     */
    public static <T> T getMapping(String filePath, Class<?> cls) throws IOException {
        if (MappingsUtils.class.getClassLoader().getResource(filePath) == null) {
            throw new FileNotFoundException("Could not read the file " + filePath + " because it does not exist.");
        }

        try (InputStream fileInputStream = MappingsUtils.class.getClassLoader().getResourceAsStream(filePath)) {
            return new JsonMapper()
                    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                    .readerFor(cls)
                    .with(JsonReadFeature.ALLOW_TRAILING_COMMA)
                    .with(JsonReadFeature.ALLOW_JAVA_COMMENTS)
                    .readValue(fileInputStream);
        } catch (Exception e) {
            throw new IOException("Could not read the file " + filePath + " because: " + e);
        }
    }

}
