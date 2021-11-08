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
package com.microfocus.adm.almoctane.importer.tool.excel.configuration;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsSchema;
import org.apache.commons.lang3.StringUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class PropertiesUtils {

    private static final SimpleModule treatEmptyStringsAsNullModule = createTreatEmptyStringsAsNullModule();

    private PropertiesUtils() {
    }

    /**
     * @param filePath The path to the properties file.
     * @param cls      The class which will be used to parse the properties.
     * @param <T>      The type of mapping that will be returned.
     *
     * @return The parsed JSON.
     */
    public static <T> T getProperties(String filePath, Class<?> cls) throws IOException {
        if (PropertiesUtils.class.getClassLoader().getResource(filePath) == null) {
            throw new FileNotFoundException("Could not read the file " + filePath + " because it does not exist.");
        }

        try (InputStream fileInputStream = PropertiesUtils.class.getClassLoader().getResourceAsStream(filePath)) {
            return JavaPropsMapper.builder()
                    .addModule(treatEmptyStringsAsNullModule)
                    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                    .enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)
                    .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                    .build()
                    .readerFor(cls)
                    .with(JavaPropsSchema.emptySchema().withoutPathSeparator())
                    .readValue(fileInputStream);
        } catch (Exception e) {
            throw new IOException("Could not read the file " + filePath + " because: " + e);
        }
    }

    private static SimpleModule createTreatEmptyStringsAsNullModule() {
        return new SimpleModule()
                .addDeserializer(String.class, new StdDeserializer<String>(String.class) {
                    @Override
                    public String deserialize(JsonParser parser, DeserializationContext context) throws IOException {
                        String result = StringDeserializer.instance.deserialize(parser, context);
                        if (StringUtils.isEmpty(result)) {
                            return null;
                        }
                        return result;
                    }
                });
    }

}
