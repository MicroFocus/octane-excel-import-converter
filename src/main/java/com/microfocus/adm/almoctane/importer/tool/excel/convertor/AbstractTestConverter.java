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
import com.microfocus.adm.almoctane.importer.tool.excel.utils.BaseOctaneField;
import com.microfocus.adm.almoctane.importer.tool.excel.utils.ConversionException;
import com.microfocus.adm.almoctane.importer.tool.excel.utils.EntityType;
import com.microfocus.adm.almoctane.importer.tool.excel.utils.StepType;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.poi.ss.usermodel.Row;

import java.io.IOException;
import java.util.List;

public abstract class AbstractTestConverter extends AbstractConverter {

    private static final String MANUAL_TESTS = "manual tests";

    protected AbstractTestConverter(ConversionInfoContainer infoContainer, int inputSheetIndex) throws IOException {
        super(infoContainer, inputSheetIndex, MANUAL_TESTS);
    }

    @Override
    protected List<String> getMandatoryOutputHeaders() {
        List<String> baseOutputHeaders = super.getMandatoryOutputHeaders();
        baseOutputHeaders.add(BaseOctaneField.STEP_TYPE.toString());
        baseOutputHeaders.add(BaseOctaneField.STEP_DESCRIPTION.toString());
        return baseOutputHeaders;
    }

    protected void addManualTest(Row row) {
        Row testRow = createRow(EntityType.MANUAL_TEST);

        inputFieldNameToOutputFieldName.forEach((inputFieldName, outputFieldName) -> {
            try {
                setCellValue(testRow, outputFieldName, getMappedCellValue(row, inputFieldName));
            } catch (ConversionException ex) {
                log.error("Could not add test row because: {}", ex.getMessage());
            }
        });
    }

    protected void addSimpleStep(Row row, String descriptionColumnName) {
        addStep(StepType.SIMPLE, row, descriptionColumnName);
    }

    protected void addValidationStep(Row row, String descriptionColumnName) {
        addStep(StepType.VALIDATION, row, descriptionColumnName);
    }

    @SuppressWarnings("unused")
    protected void addCallStep(Row row, String descriptionColumnName) {
        throw new NotImplementedException();
    }

    protected void addStep(StepType stepType, Row row, String descriptionColumnName) {
        String description = getCellValue(row, descriptionColumnName);
        addStep(stepType, description);
    }

    // UNIQUE_ID, TYPE, STEP_TYPE and STEP_DESCRIPTION are the only fields that are used by a test step
    protected void addStep(StepType stepType, String description) {
        Row stepRow = createRow(EntityType.STEP);
        setCellValue(stepRow, BaseOctaneField.STEP_TYPE.toString(), stepType.toString());
        try {
            setCellValue(stepRow, BaseOctaneField.STEP_DESCRIPTION.toString(), cleanStepDescription(description));
        } catch (ConversionException ex) {
            log.error("Could not add test step row because: {}", ex.getMessage());
        }
    }

    // converting invalid step description characters
    private static String cleanStepDescription(String description) {
        return description.replace('-', '•');
    }

}
