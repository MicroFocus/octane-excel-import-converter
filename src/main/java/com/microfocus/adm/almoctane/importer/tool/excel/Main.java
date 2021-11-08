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
package com.microfocus.adm.almoctane.importer.tool.excel;

import com.microfocus.adm.almoctane.importer.tool.excel.configuration.ConversionInfoContainer;
import com.microfocus.adm.almoctane.importer.tool.excel.configuration.ConversionMappings;
import com.microfocus.adm.almoctane.importer.tool.excel.configuration.ConversionProperties;
import com.microfocus.adm.almoctane.importer.tool.excel.convertor.Converter;
import com.microfocus.adm.almoctane.importer.tool.excel.convertor.ConverterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {

    private static final Logger LOGGER = getLogger();

    public static void main(String[] args) {
        try {
            // TODO validate properties and mappings
            ConversionProperties properties = ConversionProperties.getProperties("converter.properties");
            ConversionMappings mappings = ConversionMappings.getMappings("mapping.json");

            ConversionInfoContainer infoContainer = new ConversionInfoContainer(properties, mappings);

            Converter converter = ConverterFactory.getConverter(infoContainer);

            converter.convert();

            converter.write();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            System.exit(1);
        }
    }

    private static Logger getLogger() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss");
        System.setProperty("com.microfocus.adm.almoctane.importer.tool.excel.support.start.date.time", dateFormat.format(new Date()));
        return LoggerFactory.getLogger(Main.class);
    }

}
