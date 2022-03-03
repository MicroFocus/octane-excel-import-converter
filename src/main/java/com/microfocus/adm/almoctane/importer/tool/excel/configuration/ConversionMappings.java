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

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.LinkedHashMap;

/**
 * The mapping needed to specify what field from source excel goes to what field from destination excel and with what properties.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConversionMappings {

    @JsonProperty("field_mappings")
    private LinkedHashMap<String, FieldMapping> fieldNameToFieldMapping;

    public static ConversionMappings getMappings(String filePath) throws IOException {
        return MappingsUtils.getMapping(filePath, ConversionMappings.class);
    }

}