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
package com.microfocus.adm.almoctane.importer.tool.excel.converter;

import com.google.common.collect.Lists;
import com.microfocus.adm.almoctane.importer.tool.excel.configuration.ConversionInfoContainer;
import com.microfocus.adm.almoctane.importer.tool.excel.configuration.ConversionMappings;
import com.microfocus.adm.almoctane.importer.tool.excel.configuration.ConversionProperties;
import com.microfocus.adm.almoctane.importer.tool.excel.configuration.FieldMapping;
import com.microfocus.adm.almoctane.importer.tool.excel.configuration.RegexMapping;
import com.microfocus.adm.almoctane.importer.tool.excel.utils.BaseOctaneField;
import com.microfocus.adm.almoctane.importer.tool.excel.utils.EntityType;
import lombok.extern.slf4j.Slf4j;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Common abstract class for all excel entity converters.
 */
@Slf4j
public abstract class AbstractConverter implements Converter {

    protected final ConversionProperties conversionProperties;
    protected final ConversionMappings conversionMappings;
    protected final Map<String, String> inputFieldNameToOutputFieldName;
    protected final Sheet inputSheet;
    protected final Sheet outputSheet;
    protected final Map<String, Integer> inputHeaderNameToIndex;
    protected final Map<String, Integer> outputHeaderNameToIndex;

    protected AbstractConverter(ConversionInfoContainer infoContainer, int inputSheetIndex, String outputSheetName)
            throws IOException {
        this.conversionProperties = infoContainer.getConversionProperties();
        this.conversionMappings = infoContainer.getConversionMappings();

        this.inputFieldNameToOutputFieldName = getInputFieldNameToOutputFieldName(conversionMappings.getFieldNameToFieldMapping());

        Workbook inputWorkbook = getInputWorkbook(conversionProperties.getInputFilePath());
        Workbook outputWorkbook = getOutputWorkbook(conversionProperties.getOutputFilePath());

        this.inputSheet = inputWorkbook.getSheetAt(inputSheetIndex);
        List<String> outputHeaders = getOutputHeaders(getMandatoryOutputHeaders(), inputFieldNameToOutputFieldName);
        this.outputSheet = createOutputSheetWithHeaders(outputWorkbook, outputSheetName, outputHeaders);

        this.inputHeaderNameToIndex = getHeaderNameToIndex(inputSheet);
        this.outputHeaderNameToIndex = getHeaderNameToIndex(outputSheet);
    }

    /**
     * @return The mandatory output headers for all entity converters.
     */
    protected List<String> getMandatoryOutputHeaders() {
        return Lists.newArrayList(BaseOctaneField.UNIQUE_ID.toString(), BaseOctaneField.TYPE.toString());
    }

    /**
     * Converts the value of a source field to the Octane value format.
     *
     * @param fieldValue The value of the source field that has to be converted.
     * @param fieldName  The name of the source field that has to be converted.
     *
     * @return An octane specific value for the value of the source field.
     */
    protected String convertField(String fieldValue, String fieldName) {
        FieldMapping fieldMapping = conversionMappings.getFieldNameToFieldMapping().get(fieldName);
        if (fieldMapping != null) {
            String separator = fieldMapping.getMappingsSeparator();
            if (separator != null) {
                return Arrays.stream(fieldValue.split(separator))
                        .map(String::trim)
                        .map(singleFieldValue -> getMappedValue(fieldMapping, singleFieldValue, fieldName))
                        .filter(StringUtils::isNotEmpty)
                        .collect(Collectors.joining(","));
            } else {
                return getMappedValue(fieldMapping, fieldValue, fieldName);
            }
        } else {
            log.debug("No field mapping was found for field with name '{}'.", fieldName);
            return fieldValue;
        }
    }

    /**
     * Converts the source field value using the mapping and regex mapping properties.
     *
     * @param fieldMapping The mapping of the source field.
     * @param fieldValue   The value of the source field that has to be converted.
     * @param fieldName    The name of the source field that has to be converted.
     *
     * @return An octane specific value for the value of the source field.
     */
    private String getMappedValue(FieldMapping fieldMapping, String fieldValue, String fieldName) {
        // converting using mapping
        Map<String, String> mappings = fieldMapping.getMappings();
        String convertedValue = mappings.get(fieldValue);
        if (convertedValue != null) {
            log.debug("Mapped field with name '{}' from '{}' to '{}'.", fieldName, fieldValue, convertedValue);
            return convertedValue;
        } else {
            String defaultValue = mappings.get(DEFAULT);
            if (defaultValue != null) {
                log.debug("Default mapped field with name '{}' from '{}' to '{}'.", fieldName, fieldValue, defaultValue);
                return defaultValue;
            }
        }

        // converting using regex mapping
        List<RegexMapping> regexMappings = fieldMapping.getRegexMappings();
        for (RegexMapping regexMapping : regexMappings) {
            Pattern pattern = regexMapping.getPattern();
            Matcher matcher = pattern.matcher(fieldValue);
            if (matcher.matches()) {
                String replacedValue = matcher.replaceAll(regexMapping.getReplacement());
                log.debug("Mapped using regex '{}' field with name '{}' from '{}' to '{}'.", pattern, fieldName, fieldValue, replacedValue);
                return replacedValue;
            }
        }

        // returns the value unchanged if no mapping or regex mapping matches (could be the case when no mappings are specified)
        log.debug("Unchanged value, no mapping or regex mapping for field with name '{}' and value '{}'.", fieldName, fieldValue);
        return fieldValue;
    }

    /**
     * @param entityType The value of the {@link BaseOctaneField#TYPE} column.
     *
     * @return A newly created {@link Row} on a new excel row. The {@link BaseOctaneField#UNIQUE_ID} will be filled with the row number.
     */
    protected Row createRow(EntityType entityType) {
        int uniqueId = outputSheet.getLastRowNum() + 1;
        return createRow(uniqueId, entityType);
    }

    /**
     * @param uniqueId   The value of the {@link BaseOctaneField#UNIQUE_ID} column.
     * @param entityType The value of the {@link BaseOctaneField#TYPE} column.
     *
     * @return A newly created {@link Row} on a new excel row.
     */
    protected Row createRow(int uniqueId, EntityType entityType) {
        int outputRowIndex = outputSheet.getLastRowNum() + 1;
        Row stepRow = outputSheet.createRow(outputRowIndex);

        setCellValue(stepRow, BaseOctaneField.UNIQUE_ID.toString(), uniqueId);
        setCellValue(stepRow, BaseOctaneField.TYPE.toString(), entityType.toString());

        return stepRow;
    }

    /**
     * @param row        The row that contains the wanted column.
     * @param columnName The column name where the value is.
     *
     * @return The value from the given row and column converted to the Octane format.
     */
    protected String getMappedCellValue(Row row, String columnName) {
        String cellValue = getCellValue(row, columnName);
        return convertField(cellValue.trim(), columnName);
    }

    /**
     * @param row        The row that contains the wanted column.
     * @param columnName The column name where the value is.
     *
     * @return The value from the given row and column.
     */
    protected String getCellValue(Row row, String columnName) {
        Cell cell = row.getCell(inputHeaderNameToIndex.get(columnName));
        return cell != null ? cell.getStringCellValue() : "";
    }

    /**
     * @param row        The row that the value will be set to.
     * @param columnName The column name that the value will be set to.
     * @param value      The string value that will be set at the given row and column.
     */
    protected void setCellValue(Row row, String columnName, String value) {
        row.createCell(outputHeaderNameToIndex.get(columnName)).setCellValue(value);
    }

    /**
     * @param row        The row that the value will be set to.
     * @param columnName The column name that the value will be set to.
     * @param value      The integer value that will be set at the given row and column.
     */
    protected void setCellValue(Row row, String columnName, Integer value) {
        row.createCell(outputHeaderNameToIndex.get(columnName)).setCellValue(value);
    }

    /**
     * The output workbook that was kept in memory will be written to the output file.
     * Some styling will be added.
     *
     * @throws IOException If any write fails.
     */
    @Override
    public void write() throws IOException {
        Workbook outputWorkbook = outputSheet.getWorkbook();

        CellStyle headerStyle = outputWorkbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // adding header style and limiting column width to MAXIMUM_COLUMN_WIDTH
        if (outputSheet.getPhysicalNumberOfRows() > 0) {
            Row headerRow = outputSheet.getRow(outputSheet.getFirstRowNum());
            for (int columnNumber = headerRow.getFirstCellNum(); columnNumber < headerRow.getLastCellNum(); columnNumber++) {
                headerRow.getCell(columnNumber).setCellStyle(headerStyle);

                updateColumnWidth(outputSheet, columnNumber);
            }
        }

        OutputStream fileOutputStream = FileUtils.openOutputStream(new File(conversionProperties.getOutputFilePath()));
        outputWorkbook.write(fileOutputStream);
        outputWorkbook.close();
    }

    /**
     * @param sheet        The sheet that the update will be done to.
     * @param columnNumber The column number that will have its width updated.
     */
    private static void updateColumnWidth(Sheet sheet, int columnNumber) {
        sheet.autoSizeColumn(columnNumber);
        if (sheet.getColumnWidth(columnNumber) > MAXIMUM_COLUMN_WIDTH) {
            for (Row row : sheet) {
                Cell cell = row.getCell(columnNumber);
                if (cell != null) {
                    cell.getCellStyle().setWrapText(true);
                }
            }
            sheet.setColumnWidth(columnNumber, MAXIMUM_COLUMN_WIDTH);
        }
    }

    /**
     * @param fieldNameToFieldMapping The mapping of fields.
     *
     * @return A map from the name of the input column name to the output column name.
     */
    private static Map<String, String> getInputFieldNameToOutputFieldName(Map<String, FieldMapping> fieldNameToFieldMapping) {
        return fieldNameToFieldMapping.entrySet().stream()
                .filter(entry -> entry.getValue().getTarget() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getTarget(), (a, b) -> a, LinkedHashMap::new));
    }

    /**
     * @param inputFilePath The path where the workbook will be created to.
     *
     * @return A new workbook that contains the data from the input file.
     *
     * @throws IOException If the read fails.
     */
    private static Workbook getInputWorkbook(String inputFilePath) throws IOException {
        File file = new File(inputFilePath);
        if (file.exists()) {
            return WorkbookFactory.create(file);
        } else {
            throw new FileNotFoundException("The specified input file could not be found.");
        }
    }

    /**
     * @param outputFilePath The path where the file will be created to.
     *
     * @return A new empty workbook.
     */
    private static Workbook getOutputWorkbook(String outputFilePath) {
        if (outputFilePath.endsWith(".xlsx")) {
            return new XSSFWorkbook();
        } else if (outputFilePath.endsWith(".xls")) {
            return new HSSFWorkbook();
        } else {
            throw new IllegalArgumentException("The specified output file is not an Excel file.");
        }
    }

    /**
     * @param mandatoryOutputHeaders          The required header names.
     * @param inputFieldNameToOutputFieldName A map from the name of the input column name to the output column name.
     *
     * @return The list of headers that will be used to create the output file.
     */
    private static List<String> getOutputHeaders(List<String> mandatoryOutputHeaders, Map<String, String> inputFieldNameToOutputFieldName) {
        Set<String> outputFieldNames = new LinkedHashSet<>();
        outputFieldNames.addAll(mandatoryOutputHeaders);
        outputFieldNames.addAll(inputFieldNameToOutputFieldName.values());
        return new ArrayList<>(outputFieldNames);
    }

    /**
     * @param outputWorkbook The output workbook.
     * @param sheetName      The sheet name.
     * @param headers        The headers.
     *
     * @return A new sheet created in the output workbook with the given sheet name and with the given headers.
     */
    private static Sheet createOutputSheetWithHeaders(Workbook outputWorkbook, String sheetName, List<String> headers) {
        Sheet sheet = outputWorkbook.createSheet(sheetName);
        Row headerRow = sheet.createRow(0);

        for (int i = 0; i < headers.size(); i++) {
            headerRow.createCell(i).setCellValue(headers.get(i));
        }
        return sheet;
    }

    /**
     * @param sheet The given sheet.
     *
     * @return A map from the column name to its index.
     */
    public static Map<String, Integer> getHeaderNameToIndex(Sheet sheet) {
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
