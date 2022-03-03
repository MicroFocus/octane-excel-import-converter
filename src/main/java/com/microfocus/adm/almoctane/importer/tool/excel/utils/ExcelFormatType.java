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

import java.util.Set;

/**
 * Converter excel format types.
 */
public enum ExcelFormatType {
    QTEST, OCTANE, UNKNOWN;

    private static final Set<String> VALID_TYPES = Sets.newHashSet(QTEST.toString());

    public static Set<String> validTypes() {
        return VALID_TYPES;
    }
}
