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
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.microfocus.adm.almoctane.importer.tool.excel.utils.ExcelFormatType;
import lombok.Getter;

import java.io.IOException;

@Getter
public class ConversionProperties {

    @JsonProperty("input.file.path")
    private String inputFilePath;

    @JsonSetter(nulls = Nulls.SKIP)
    @JsonProperty("input.file.format.type")
    private ExcelFormatType inputFileFormatType = ExcelFormatType.QTEST;

    @JsonProperty("output.file.path")
    private String outputFilePath;

    private ConversionProperties() {
    }

    public static ConversionProperties getProperties(String filePath) throws IOException {
        return PropertiesUtils.getProperties(filePath, ConversionProperties.class);
    }

}
