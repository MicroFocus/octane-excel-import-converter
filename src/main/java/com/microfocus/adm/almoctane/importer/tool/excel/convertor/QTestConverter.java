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
import com.microfocus.adm.almoctane.importer.tool.excel.utils.BaseQTestField;
import org.apache.poi.ss.usermodel.Row;

import java.io.IOException;
import java.util.Iterator;

public class QTestConverter extends AbstractTestConverter {

    public static final int INPUT_SHEET_INDEX = 1;

    public QTestConverter(ConversionInfoContainer infoContainer) throws IOException {
        super(infoContainer, INPUT_SHEET_INDEX);
    }

    @Override
    public void convert() {
        Iterator<Row> testCasesRowIterator = inputSheet.iterator();
        if (testCasesRowIterator.hasNext()) {
            testCasesRowIterator.next(); // skip header row

            String lastId = null;
            while (testCasesRowIterator.hasNext()) {
                Row row = testCasesRowIterator.next();

                String currentId = getCellValue(row, BaseQTestField.ID.toString());
                if (!currentId.equals(lastId)) {
                    addManualTest(row);
                    lastId = currentId;
                }
                addSimpleStep(row, BaseQTestField.TEST_STEP_DESCRIPTION.toString());
                addValidationStep(row, BaseQTestField.TEST_STEP_EXPECTED_RESULT.toString());
            }
        }
    }

}
