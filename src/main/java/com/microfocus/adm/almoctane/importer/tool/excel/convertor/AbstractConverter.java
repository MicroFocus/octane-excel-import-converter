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

import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import com.microfocus.adm.almoctane.importer.tool.excel.configuration.ConversionInfoContainer;
import com.microfocus.adm.almoctane.importer.tool.excel.configuration.ConversionProperties;
import com.microfocus.adm.almoctane.importer.tool.excel.utils.BaseOctaneField;
import com.microfocus.adm.almoctane.importer.tool.excel.utils.BaseQTestField;
import com.microfocus.adm.almoctane.importer.tool.excel.utils.ConversionException;
import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class AbstractConverter implements Converter {

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    private static final int MAXIMUM_COLUMN_WIDTH = 15000;

    protected final ConversionProperties properties;
    protected final List<String> manualTestHeaderValues;
    protected final HashBiMap<Integer, String> outputHeaderIndexToName;
    protected final Workbook inputWorkbook;
    protected final Workbook outputWorkbook;
    private final Map<String, Map<String, String>> fieldNameToFieldMapping;

    public AbstractConverter(ConversionInfoContainer infoContainer) throws IOException {
        this.properties = infoContainer.getProperties();
        // TODO add and handle custom fields
        this.manualTestHeaderValues = Arrays.stream(BaseOctaneField.values()).map(field -> field.name).collect(Collectors.toList());
        this.outputHeaderIndexToName = IntStream.range(0, manualTestHeaderValues.size()).boxed()
                .collect(Collectors.toMap(Function.identity(), manualTestHeaderValues::get, (a, b) -> a, HashBiMap::create));
        this.inputWorkbook = getInputWorkbook(properties.getInputFilePath());
        this.outputWorkbook = getOutputWorkbook(properties.getOutputFilePath());

        // should be fetched from properties
        this.fieldNameToFieldMapping = ImmutableMap.<String, Map<String, String>>builder()
                .put(BaseQTestField.MODULE.name, ImmutableMap.<String, String>builder()
                        .put("MD-630 01. System Testing", "System Testing")
                        .put("MD-631 02. SiT", "SiT")
                        .put("MD-632 03. Regression", "Regression")
                        .put("MD-633 04. Outputs", "Outputs")
                        .build())
                .put(BaseQTestField.STATUS.name, ImmutableMap.<String, String>builder()
                        .put("Ready For Baseline", "Ready For Baseline")
                        .put("New", "New")
                        .build())
                .build();
    }

    protected String convertField(String fieldValue, String name) {
        Map<String, String> fieldMapping = fieldNameToFieldMapping.get(name);
        if (fieldMapping == null) {
            throw new ConversionException("Missing field mapping for field with name '" + name + "'.");
        } else {
            String convertedValue = fieldMapping.get(fieldValue);
            if (convertedValue != null) {
                return convertedValue;
            } else {
                throw new ConversionException("Missing mapping for field with name '" + name + "' and value '" + fieldValue + "'.");
            }
        }
    }

    @Override
    public void write() throws IOException {
        CellStyle headerStyle = outputWorkbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Integer stepDescriptionIndex = outputHeaderIndexToName.inverse().get(BaseOctaneField.STEP_DESCRIPTION.name);

        for (Sheet sheet : outputWorkbook) {
            int sheetRowCount = sheet.getPhysicalNumberOfRows();
            if (sheetRowCount > 0) {
                Row headerRow = sheet.getRow(sheet.getFirstRowNum());

                for (Cell cell : headerRow) {
                    cell.setCellStyle(headerStyle);
                }

                for (Row row : sheet) {
                    Cell cell = row.getCell(stepDescriptionIndex);
                    if (cell != null) {
                        cell.getCellStyle().setWrapText(true);
                    }
                }
                for (int cellNum = headerRow.getFirstCellNum(); cellNum < headerRow.getLastCellNum(); cellNum++) {
                    sheet.autoSizeColumn(cellNum);
                    int columnWidth = sheet.getColumnWidth(cellNum);
                    if (columnWidth > MAXIMUM_COLUMN_WIDTH) {
                        sheet.setColumnWidth(cellNum, MAXIMUM_COLUMN_WIDTH);
                    }
                }
            }
        }

        OutputStream fileOutputStream = FileUtils.openOutputStream(new File(properties.getOutputFilePath()));
        outputWorkbook.write(fileOutputStream);
        outputWorkbook.close();
    }

    private Workbook getInputWorkbook(String inputFilePath) throws IOException {
        File file = new File(inputFilePath);
        if (file.exists()) {
            return WorkbookFactory.create(file);
        } else {
            throw new FileNotFoundException("The specified input file could not be found.");
        }
    }

    private Workbook getOutputWorkbook(String outputFilePath) {
        Workbook workbook;
        if (outputFilePath.endsWith(".xlsx")) {
            workbook = new XSSFWorkbook();
        } else if (outputFilePath.endsWith(".xls")) {
            workbook = new HSSFWorkbook();
        } else {
            throw new IllegalArgumentException("The specified output file is not an Excel file.");
        }

        Sheet manualTestsSheet = workbook.createSheet("manual tests");
        Row manualTestsHeader = manualTestsSheet.createRow(0);

        for (int i = 0; i < manualTestHeaderValues.size(); i++) {
            manualTestsHeader.createCell(i).setCellValue(manualTestHeaderValues.get(i));
        }

        return workbook;
    }

}
