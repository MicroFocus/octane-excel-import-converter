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

import java.io.IOException;

/**
 * Common interface for all excel entity converters.
 */
@SuppressWarnings("UnnecessaryInterfaceModifier")
public interface Converter {

    public static final int MAXIMUM_COLUMN_WIDTH = 20000;
    public static final String DEFAULT = "default";

    /**
     * Converts the input worksheet into an output worksheet kept in memory.
     */
    public void convert();

    /**
     * The output workbook that was kept in memory will be written to the output file.
     *
     * @throws IOException If any write fails.
     */
    public void write() throws IOException;

}
