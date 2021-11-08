/*
 * (c) Copyright 2021 Micro Focus or one of its affiliates.
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

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedHashMap;

@Getter
public class ConversionMappings {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConversionMappings.class);

    @JsonProperty("field_mappings")
    private LinkedHashMap<String, FieldMapping> fieldNameToFieldMapping;

    private ConversionMappings() {
    }

    public static ConversionMappings getMappings(String filePath) throws IOException {
        return MappingsUtils.getMapping(filePath, ConversionMappings.class);
    }

}