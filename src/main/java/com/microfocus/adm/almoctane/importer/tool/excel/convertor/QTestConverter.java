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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.microfocus.adm.almoctane.importer.tool.excel.configuration.ConversionInfoContainer;
import com.microfocus.adm.almoctane.importer.tool.excel.utils.BaseOctaneField;
import com.microfocus.adm.almoctane.importer.tool.excel.utils.BaseQTestField;
import com.microfocus.adm.almoctane.importer.tool.excel.utils.ConversionException;
import com.microfocus.adm.almoctane.importer.tool.excel.utils.EntityType;
import com.microfocus.adm.almoctane.importer.tool.excel.utils.StepType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.IOException;

public class QTestConverter extends AbstractConverter {

    private final Sheet testCasesInputSheet;
    private final Sheet manualTestsOutputSheet;
    private int manualTestsOutputRowIndex;

    public QTestConverter(ConversionInfoContainer infoContainer) throws IOException {
        super(infoContainer);
        this.testCasesInputSheet = inputWorkbook.getSheetAt(1); // sheet with test cases
        this.manualTestsOutputSheet = outputWorkbook.getSheetAt(0);
        this.manualTestsOutputRowIndex = 1;
    }

    @Override
    public void convert() {
        if (testCasesInputSheet.getPhysicalNumberOfRows() > 1) {
            int firstRowNum = testCasesInputSheet.getFirstRowNum();

            BiMap<Integer, String> inputHeaderIndexToName = HashBiMap.create();
            for (Cell cell : testCasesInputSheet.getRow(firstRowNum)) {
                inputHeaderIndexToName.put(cell.getColumnIndex(), cell.getStringCellValue());
            }

            // maybe add a class for converting test steps
            Integer idColumnIndex = inputHeaderIndexToName.inverse().get(BaseQTestField.ID.name);
            String lastId = null;
            for (int rowNumber = firstRowNum + 1; rowNumber < testCasesInputSheet.getLastRowNum(); rowNumber++) {
                Row row = testCasesInputSheet.getRow(rowNumber);
                String currentId = row.getCell(idColumnIndex).getStringCellValue();
                if (!currentId.equals(lastId)) {
                    try {
                        addTest(inputHeaderIndexToName, outputHeaderIndexToName, row);
                    } catch (ConversionException ce) {
                        LOGGER.error("Could not add test row because: " + ce.getMessage());
                    }
                    lastId = currentId;
                }
                try {
                    // TODO split in 2 methods
                    addSteps(inputHeaderIndexToName, outputHeaderIndexToName, row);
                } catch (ConversionException ce) {
                    LOGGER.error("Could not add test step row because: " + ce.getMessage());
                }
            }
        }
    }

    private void addTest(BiMap<Integer, String> inputHeaderIndexToName, BiMap<Integer, String> outputHeaderIndexToName, Row row) {
        Integer inputNameIndex = inputHeaderIndexToName.inverse().get(BaseQTestField.NAME.name);
        String inputName = row.getCell(inputNameIndex).getStringCellValue();

        Integer inputModuleIndex = inputHeaderIndexToName.inverse().get(BaseQTestField.MODULE.name);
        String inputModule = row.getCell(inputModuleIndex).getStringCellValue();
        String convertedInputModule = convertField(inputModule.trim(), BaseQTestField.MODULE.name);

        Integer inputStatusIndex = inputHeaderIndexToName.inverse().get(BaseQTestField.STATUS.name);
        String inputStatus = row.getCell(inputStatusIndex).getStringCellValue();
        String convertedInputStatus = convertField(inputStatus.trim(), BaseQTestField.STATUS.name);

        Integer outputUniqueIdIndex = outputHeaderIndexToName.inverse().get(BaseOctaneField.UNIQUE_ID.name);
        Integer outputTypeIndex = outputHeaderIndexToName.inverse().get(BaseOctaneField.TYPE.name);
        Integer outputNameIndex = outputHeaderIndexToName.inverse().get(BaseOctaneField.NAME.name);
        Integer outputProductAreasIndex = outputHeaderIndexToName.inverse().get(BaseOctaneField.PRODUCT_AREAS.name);
        Integer outputPhaseIndex = outputHeaderIndexToName.inverse().get(BaseOctaneField.PHASE.name);

        Row simpleStepRow = manualTestsOutputSheet.createRow(manualTestsOutputRowIndex);
        simpleStepRow.createCell(outputUniqueIdIndex).setCellValue(manualTestsOutputRowIndex);
        simpleStepRow.createCell(outputTypeIndex).setCellValue(EntityType.MANUAL_TEST.name);
        simpleStepRow.createCell(outputNameIndex).setCellValue(inputName);
        simpleStepRow.createCell(outputProductAreasIndex).setCellValue(convertedInputModule);
        simpleStepRow.createCell(outputPhaseIndex).setCellValue(convertedInputStatus);
        manualTestsOutputRowIndex++;
    }

    // adds a simple and a validation step
    private void addSteps(BiMap<Integer, String> inputHeaderIndexToName, BiMap<Integer, String> outputHeaderIndexToName, Row row) {
        Integer inputSimpleStepDescriptionIndex = inputHeaderIndexToName.inverse().get(BaseQTestField.TEST_STEP_DESCRIPTION.name);
        String inputSimpleStepDescription = cleanStepDescription(row.getCell(inputSimpleStepDescriptionIndex).getStringCellValue());

        Integer outputUniqueIdIndex = outputHeaderIndexToName.inverse().get(BaseOctaneField.UNIQUE_ID.name);
        Integer outputTypeIndex = outputHeaderIndexToName.inverse().get(BaseOctaneField.TYPE.name);
        Integer outputStepTypeIndex = outputHeaderIndexToName.inverse().get(BaseOctaneField.STEP_TYPE.name);
        Integer outputStepDescriptionIndex = outputHeaderIndexToName.inverse().get(BaseOctaneField.STEP_DESCRIPTION.name);

        Row simpleStepRow = manualTestsOutputSheet.createRow(manualTestsOutputRowIndex);
        simpleStepRow.createCell(outputUniqueIdIndex).setCellValue(manualTestsOutputRowIndex);
        simpleStepRow.createCell(outputTypeIndex).setCellValue(EntityType.STEP.name);
        simpleStepRow.createCell(outputStepTypeIndex).setCellValue(StepType.SIMPLE.name);
        simpleStepRow.createCell(outputStepDescriptionIndex).setCellValue(inputSimpleStepDescription);
        manualTestsOutputRowIndex++;

        Integer validationStepDescriptionIndex = inputHeaderIndexToName.inverse().get(BaseQTestField.TEST_STEP_EXPECTED_RESULT.name);
        String validationStepDescription = cleanStepDescription(row.getCell(validationStepDescriptionIndex).getStringCellValue());
        Row validationStepRow = manualTestsOutputSheet.createRow(manualTestsOutputRowIndex);

        validationStepRow.createCell(outputUniqueIdIndex).setCellValue(manualTestsOutputRowIndex);
        validationStepRow.createCell(outputTypeIndex).setCellValue(EntityType.STEP.name);
        validationStepRow.createCell(outputStepTypeIndex).setCellValue(StepType.VALIDATION.name);
        validationStepRow.createCell(outputStepDescriptionIndex).setCellValue(validationStepDescription);
        manualTestsOutputRowIndex++;
    }

    // converting invalid step description characters
    private static String cleanStepDescription(String description) {
        return description.replace('-', '•');
    }

}
