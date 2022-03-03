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

import lombok.AllArgsConstructor;

/**
 * Mandatory Octane manual test fields.
 */
@AllArgsConstructor
public enum BaseOctaneField {
    UNIQUE_ID("unique_id"),
    TYPE("type"),
    TEST_TYPE("test_type"),
    STEP_TYPE("step_type"),
    STEP_DESCRIPTION("step_description");

    private final String typeName;

    @Override
    public String toString() {
        return typeName;
    }
}
