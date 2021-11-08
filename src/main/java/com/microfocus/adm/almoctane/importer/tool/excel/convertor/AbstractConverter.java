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
import com.microfocus.adm.almoctane.importer.tool.excel.configuration.ConversionMappings;
import com.microfocus.adm.almoctane.importer.tool.excel.configuration.ConversionProperties;
import com.microfocus.adm.almoctane.importer.tool.excel.configuration.FieldMapping;
import com.microfocus.adm.almoctane.importer.tool.excel.utils.BaseOctaneField;
import com.microfocus.adm.almoctane.importer.tool.excel.utils.EntityType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public abstract class AbstractConverter implements Converter {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected final ConversionProperties properties;
    protected final ConversionMappings mappings;
    protected final Map<String, String> inputFieldNameToOutputFieldName;
    protected final Sheet inputSheet;
    protected final Sheet outputSheet;
    protected final Map<String, Integer> inputHeaderNameToIndex;
    protected final Map<String, Integer> outputHeaderNameToIndex;

    protected AbstractConverter(ConversionInfoContainer infoContainer, int inputSheetIndex, String outputSheetName)
            throws IOException {
        this.properties = infoContainer.getProperties();
        this.mappings = infoContainer.getMappings();

        this.inputFieldNameToOutputFieldName = getInputFieldNameToOutputFieldName(mappings.getFieldNameToFieldMapping());

        Workbook inputWorkbook = getInputWorkbook(properties.getInputFilePath());
        Workbook outputWorkbook = getOutputWorkbook(properties.getOutputFilePath());

        this.inputSheet = inputWorkbook.getSheetAt(inputSheetIndex);
        List<String> outputHeaders = getOutputHeaders(getMandatoryOutputHeaders(), inputFieldNameToOutputFieldName);
        this.outputSheet = createOutputSheetWithHeaders(outputWorkbook, outputSheetName, outputHeaders);

        this.inputHeaderNameToIndex = getHeaderNameToIndex(inputSheet);
        this.outputHeaderNameToIndex = getHeaderNameToIndex(outputSheet);
    }

    protected List<String> getMandatoryOutputHeaders() {
        return Arrays.asList(BaseOctaneField.UNIQUE_ID.toString(), BaseOctaneField.TYPE.toString());
    }

    protected String convertField(String fieldValue, String fieldName) {
        FieldMapping fieldMapping = mappings.getFieldNameToFieldMapping().get(fieldName);
        if (fieldMapping != null) {
            Map<String, String> mapping = fieldMapping.getMapping();
            String separator = fieldMapping.getMappingSeparator();
            if (separator != null) {
                return Arrays.stream(fieldValue.split(separator))
                        .map(String::trim)
                        .map(singleFieldValue -> getMappedValue(mapping, singleFieldValue, fieldName))
                        .filter(StringUtils::isNotEmpty)
                        .collect(Collectors.joining(","));
            } else {
                return getMappedValue(mapping, fieldValue, fieldName);
            }
        } else {
            log.debug("No field mapping was found for field with name '{}'.", fieldName);
            return fieldValue;
        }
    }

    private String getMappedValue(Map<String, String> mapping, String fieldValue, String fieldName) {
        String convertedValue = mapping.get(fieldValue);
        if (convertedValue != null) {
            log.debug("Converted field with name '{}' from '{}' to '{}'.", fieldName, fieldValue, convertedValue);
            return convertedValue;
        } else {
            String defaultValue = mapping.get(DEFAULT);
            if (defaultValue != null) {
                log.debug("Converted field with name '{}' from '{}' to '{}' using the default value.", fieldName, fieldValue, defaultValue);
                return defaultValue;
            } else {
                log.debug("No mapping for field with name '{}' and value '{}', the value will remain unchanged.", fieldName, fieldValue);
                return fieldValue;
            }
        }
    }

    protected Row createRow(EntityType entityType) {
        int uniqueId = outputSheet.getLastRowNum() + 1;
        return createRow(uniqueId, entityType);
    }

    protected Row createRow(int uniqueId, EntityType entityType) {
        int outputRowIndex = outputSheet.getLastRowNum() + 1;
        Row stepRow = outputSheet.createRow(outputRowIndex);

        setCellValue(stepRow, BaseOctaneField.UNIQUE_ID.toString(), uniqueId);
        setCellValue(stepRow, BaseOctaneField.TYPE.toString(), entityType.toString());

        return stepRow;
    }

    protected String getMappedCellValue(Row row, String columnName) {
        String cellValue = getCellValue(row, columnName);
        return convertField(cellValue.trim(), columnName);
    }

    protected String getCellValue(Row row, String columnName) {
        return row.getCell(inputHeaderNameToIndex.get(columnName)).getStringCellValue();
    }

    protected void setCellValue(Row row, String columnName, String value) {
        row.createCell(outputHeaderNameToIndex.get(columnName)).setCellValue(value);
    }

    protected void setCellValue(Row row, String columnName, Integer value) {
        row.createCell(outputHeaderNameToIndex.get(columnName)).setCellValue(value);
    }

    @Override
    public void write() throws IOException {
        Workbook outputWorkbook = outputSheet.getWorkbook();

        CellStyle headerStyle = outputWorkbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // adding header style and limiting column width to MAXIMUM_COLUMN_WIDTH
        if (outputSheet.getPhysicalNumberOfRows() > 0) {
            Row headerRow = outputSheet.getRow(outputSheet.getFirstRowNum());
            for (int cellNum = headerRow.getFirstCellNum(); cellNum < headerRow.getLastCellNum(); cellNum++) {
                headerRow.getCell(cellNum).setCellStyle(headerStyle);

                updateColumnWidth(outputSheet, cellNum);
            }
        }

        OutputStream fileOutputStream = FileUtils.openOutputStream(new File(properties.getOutputFilePath()));
        outputWorkbook.write(fileOutputStream);
        outputWorkbook.close();
    }

    private static void updateColumnWidth(Sheet sheet, int cellNum) {
        sheet.autoSizeColumn(cellNum);
        if (sheet.getColumnWidth(cellNum) > MAXIMUM_COLUMN_WIDTH) {
            for (Row row : sheet) {
                Cell cell = row.getCell(cellNum);
                if (cell != null) {
                    cell.getCellStyle().setWrapText(true);
                }
            }
            sheet.setColumnWidth(cellNum, MAXIMUM_COLUMN_WIDTH);
        }
    }

    private static Map<String, String> getInputFieldNameToOutputFieldName(Map<String, FieldMapping> fieldNameToFieldMapping) {
        return fieldNameToFieldMapping.entrySet().stream()
                .filter(entry -> entry.getValue().getTarget() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getTarget(), (a, b) -> a, LinkedHashMap::new));
    }

    private static Workbook getInputWorkbook(String inputFilePath) throws IOException {
        File file = new File(inputFilePath);
        if (file.exists()) {
            return WorkbookFactory.create(file);
        } else {
            throw new FileNotFoundException("The specified input file could not be found.");
        }
    }

    private static Workbook getOutputWorkbook(String outputFilePath) {
        if (outputFilePath.endsWith(".xlsx")) {
            return new XSSFWorkbook();
        } else if (outputFilePath.endsWith(".xls")) {
            return new HSSFWorkbook();
        } else {
            throw new IllegalArgumentException("The specified output file is not an Excel file.");
        }
    }

    private static List<String> getOutputHeaders(List<String> mandatoryOutputHeaders, Map<String, String> inputFieldNameToOutputFieldName) {
        Set<String> outputFieldNames = new LinkedHashSet<>();
        outputFieldNames.addAll(mandatoryOutputHeaders);
        outputFieldNames.addAll(inputFieldNameToOutputFieldName.values());
        return new ArrayList<>(outputFieldNames);
    }

    private static Sheet createOutputSheetWithHeaders(Workbook outputWorkbook, String sheetName, List<String> headers) {
        Sheet sheet = outputWorkbook.createSheet(sheetName);
        Row headerRow = sheet.createRow(0);

        for (int i = 0; i < headers.size(); i++) {
            headerRow.createCell(i).setCellValue(headers.get(i));
        }
        return sheet;
    }

    private static Map<String, Integer> getHeaderNameToIndex(Sheet sheet) {
        Iterator<Row> rowIterator = sheet.iterator();
        if (rowIterator.hasNext()) {
            Row headerRow = rowIterator.next();
            return StreamSupport.stream(headerRow.spliterator(), false)
                    .collect(Collectors.toMap(Cell::getStringCellValue, Cell::getColumnIndex));
        } else {
            return Collections.emptyMap();
        }
    }

}
