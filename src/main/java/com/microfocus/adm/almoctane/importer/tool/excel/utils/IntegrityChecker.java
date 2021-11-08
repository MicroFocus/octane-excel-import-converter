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
package com.microfocus.adm.almoctane.importer.tool.excel.utils;

import com.microfocus.adm.almoctane.importer.tool.excel.configuration.ConversionInfoContainer;
import com.microfocus.adm.almoctane.importer.tool.excel.configuration.ConversionMappings;
import com.microfocus.adm.almoctane.importer.tool.excel.configuration.ConversionProperties;
import com.microfocus.adm.almoctane.importer.tool.excel.configuration.FieldMapping;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class IntegrityChecker {

    private final IntegrityHandler integrityHandler;

    private final ConversionProperties conversionProperties;
    private final ConversionMappings conversionMappings;

    public IntegrityChecker(ConversionInfoContainer infoContainer) {
        this.integrityHandler = new IntegrityHandler();
        this.conversionProperties = infoContainer.getConversionProperties();
        this.conversionMappings = infoContainer.getConversionMappings();
    }

    public void checkIntegrity() {
        checkConversionProperties();

        checkConversionMappings();

        integrityHandler.promptUserIntegrityStatus();
    }

    private void checkConversionProperties() {
        String inputFilePath = conversionProperties.getInputFilePath();
        if (inputFilePath != null) {
            File inputFile = new File(inputFilePath);
            if (!inputFile.exists()) {
                integrityHandler.logError("Input file '{}' doesn't exist.", inputFilePath);
            } else if (isLocked(inputFile)) {
                integrityHandler.logError("Input file '{}' is already in use.", inputFilePath);
            }
        } else {
            integrityHandler.logError("No input file was provided.");
        }

        String outputFilePath = conversionProperties.getOutputFilePath();
        if (outputFilePath != null) {
            File outputFile = new File(outputFilePath);
            if (outputFile.exists()) {
                integrityHandler.logWarning("Output file '{}' already exists, after conversion it will be overridden.", outputFilePath);
                if (isLocked(outputFile)) {
                    integrityHandler.logError("Output file '{}' is already in use.", outputFilePath);
                }
            }
        } else {
            integrityHandler.logError("No output file was provided.");
        }

        ExcelFormatType inputFileFormatType = conversionProperties.getInputFileFormatType();
        if (inputFileFormatType == ExcelFormatType.UNKNOWN) {
            integrityHandler.logError("Unsupported input file format type, supported formats are: {}", ExcelFormatType.validTypes());
        }
    }

    private void checkConversionMappings() {
        Map<String, FieldMapping> fieldNameToFieldMapping = conversionMappings.getFieldNameToFieldMapping();
        fieldNameToFieldMapping.forEach((inputFieldName, fieldMapping) -> {
            if (fieldMapping.getTarget() == null) {
                integrityHandler.logWarning("No target specified for input field name '{}', mapping will be ignored.", inputFieldName);
            }
        });

        Map<String, Set<String>> targetFieldNameToInputFieldNames = fieldNameToFieldMapping.entrySet().stream()
                .filter(entry -> entry.getValue().getTarget() != null)
                .collect(Collectors.groupingBy(entry -> entry.getValue().getTarget(),
                        Collectors.mapping(Map.Entry::getKey, Collectors.toSet())));

        targetFieldNameToInputFieldNames.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .forEach(entry -> integrityHandler.logError("Target field {} is used by multiple input field mappings.", entry.getKey()));
    }

    private static boolean isLocked(File inputFile) {
        if (!inputFile.exists()) {
            return false;
        } else {
            try (FileChannel channel = new RandomAccessFile(inputFile, "rw").getChannel()) {
                channel.lock().release();
                return false;
            } catch (IOException ex) {
                return true;
            }
        }
    }

}
