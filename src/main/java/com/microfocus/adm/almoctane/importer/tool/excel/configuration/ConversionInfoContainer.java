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

import com.microfocus.adm.almoctane.importer.tool.excel.utils.IntegrityChecker;
import lombok.Getter;

@Getter
public class ConversionInfoContainer {

    private final ConversionProperties conversionProperties;
    private final ConversionMappings conversionMappings;

    public ConversionInfoContainer(ConversionProperties conversionProperties, ConversionMappings conversionMappings) {
        this.conversionProperties = conversionProperties;
        this.conversionMappings = conversionMappings;

        new IntegrityChecker(this).checkIntegrity();
    }

}
