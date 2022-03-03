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
package com.microfocus.adm.almoctane.importer.tool.excel.utils;

import com.google.common.collect.Sets;
import com.microfocus.adm.almoctane.importer.tool.excel.configuration.ConversionInfoContainer;
import com.microfocus.adm.almoctane.importer.tool.excel.configuration.ConversionMappings;
import com.microfocus.adm.almoctane.importer.tool.excel.configuration.ConversionProperties;
import com.microfocus.adm.almoctane.importer.tool.excel.configuration.FieldMapping;
import com.microfocus.adm.almoctane.importer.tool.excel.converter.AbstractConverter;
import com.microfocus.adm.almoctane.importer.tool.excel.converter.QTestConverter;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Checks the integrity of the given {@link ConversionProperties} and {@link ConversionMappings}.
 */
public class IntegrityChecker {

    private final IntegrityHandler integrityHandler;

    private final ConversionProperties conversionProperties;
    private final ConversionMappings conversionMappings;

    private Set<String> inputHeaderNames;

    public IntegrityChecker(ConversionInfoContainer infoContainer) {
        this.integrityHandler = new IntegrityHandler();
        this.conversionProperties = infoContainer.getConversionProperties();
        this.conversionMappings = infoContainer.getConversionMappings();
    }

    /**
     * Checks the integrity of the given {@link ConversionProperties} and {@link ConversionMappings}.
     */
    public void checkIntegrity() {
        checkConversionProperties();

        checkConversionMappings();

        integrityHandler.promptUserIntegrityStatus();
    }

    /**
     * Checks the integrity of the given {@link ConversionProperties}.
     */
    private void checkConversionProperties() {
        checkInputFile();

        checkOutputFile();
    }

    /**
     * If an input file was specified, it exists and it can be written to.
     */
    private void checkInputFile() {
        String inputFilePath = conversionProperties.getInputFilePath();
        if (inputFilePath != null) {
            File inputFile = new File(inputFilePath);
            if (!inputFile.exists()) {
                integrityHandler.logError("Input file '{}' doesn't exist.", inputFilePath);
            } else if (isLocked(inputFile)) {
                integrityHandler.logError("Input file '{}' is already in use.", inputFilePath);
            } else {
                try (Workbook inputWorkbook = WorkbookFactory.create(inputFile)) {
                    Sheet inputSheet = inputWorkbook.getSheetAt(QTestConverter.INPUT_SHEET_INDEX);
                    this.inputHeaderNames = AbstractConverter.getHeaderNameToIndex(inputSheet).keySet();
                } catch (IOException e) {
                    integrityHandler.logError(e);
                }
            }
        } else {
            integrityHandler.logError("No input file was provided.");
        }
    }

    /**
     * If an output file was specified, it doesn't exist or if the output file should be overridden and if it can be written to.
     */
    private void checkOutputFile() {
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
            integrityHandler.logError("Unsupported input file format type, supported formats are: {}.", ExcelFormatType.validTypes());
        }
    }

    /**
     * Checks the integrity of the given {@link ConversionMappings}.
     * If all fields have exactly one target field specified.
     */
    private void checkConversionMappings() {
        Map<String, FieldMapping> fieldNameToFieldMapping = conversionMappings.getFieldNameToFieldMapping();

        if (inputHeaderNames != null) {
            Set<String> unknownInputFields = Sets.difference(fieldNameToFieldMapping.keySet(), inputHeaderNames);
            if (!unknownInputFields.isEmpty()) {
                integrityHandler.logError("Unknown fields mapped from input file: '{}', valid field names are: '{}'.",
                        String.join("', '", unknownInputFields), String.join("', '", inputHeaderNames));
            }
        }

        fieldNameToFieldMapping.entrySet().stream()
                .filter(entry -> entry.getValue().getTarget() == null)
                .forEach(entry -> integrityHandler.logWarning("Mapping will be ignored for input field with name '{}'"
                        + " because no target output field name was specified.", entry.getKey()));

        Map<String, Set<String>> targetFieldNameToInputFieldNames = fieldNameToFieldMapping.entrySet().stream()
                .filter(entry -> entry.getValue().getTarget() != null)
                .collect(Collectors.groupingBy(entry -> entry.getValue().getTarget(),
                        Collectors.mapping(Map.Entry::getKey, Collectors.toSet())));

        targetFieldNameToInputFieldNames.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .forEach(entry -> integrityHandler.logError("Target field {} is used by multiple input field mappings.", entry.getKey()));
    }

    /**
     * @param file The file that will be tested if it is locked.
     *
     * @return true if the file is locked, false otherwise.
     */
    private static boolean isLocked(File file) {
        if (!file.exists()) {
            return false;
        } else {
            try (FileChannel channel = new RandomAccessFile(file, "rw").getChannel()) {
                channel.lock().release();
                return false;
            } catch (IOException ex) {
                return true;
            }
        }
    }

}
