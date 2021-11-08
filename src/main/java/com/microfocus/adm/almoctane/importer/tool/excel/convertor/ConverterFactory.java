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
package com.microfocus.adm.almoctane.importer.tool.excel.convertor;

import com.microfocus.adm.almoctane.importer.tool.excel.configuration.ConversionInfoContainer;
import com.microfocus.adm.almoctane.importer.tool.excel.utils.ConversionException;
import com.microfocus.adm.almoctane.importer.tool.excel.utils.ExcelFormatType;

import java.io.IOException;

public class ConverterFactory {

    private ConverterFactory() {
    }

    public static Converter getConverter(ConversionInfoContainer infoContainer) throws IOException {
        ExcelFormatType inputFileFormat = infoContainer.getProperties().getInputFileFormatType();
        switch (inputFileFormat) {
            case QTEST:
                return new QTestConverter(infoContainer);
            case UNKNOWN:
            default:
                throw new ConversionException("Input file format type '" + inputFileFormat + "' wasn't provided or it isn't supported.");
        }
    }

}
