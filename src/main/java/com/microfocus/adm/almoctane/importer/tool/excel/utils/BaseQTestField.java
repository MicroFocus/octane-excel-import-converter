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

public enum BaseQTestField {
    MODULE("Module"),
    NAME("Name"),
    ID("Id"),
    ATTACHMENTS("Attachments"),
    STATUS("Status"),
    TYPE("Type"),
    DESCRIPTION("Description"),
    PRECONDITION("Precondition"),
    TEST_STEP_NUMBER("Test Step #"),
    TEST_STEP_DESCRIPTION("Test Step Description"),
    TEST_STEP_EXPECTED_RESULT("Test Step Expected Result"),
    TEST_STEP_ATTACHMENT("Test Step Attachment"),
    REQUIREMENT_IDS("Requirement Ids"),
    REQUIREMENTS("Requirements"),
    VERSION("Version"),
    ASSIGNED_TO("Assigned To"),
    PRIORITY("Priority"),
    EPIC_LINK("Epic Link");

    private final String typeName;

    BaseQTestField(String name) {
        this.typeName = name;
    }

    @Override
    public String toString() {
        return typeName;
    }
}
