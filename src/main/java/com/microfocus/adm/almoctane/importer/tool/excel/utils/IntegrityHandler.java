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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

public class IntegrityHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(IntegrityHandler.class);
    private int errorsFound;
    private int warningsFound;

    public IntegrityHandler() {
        this.errorsFound = 0;
        this.warningsFound = 0;
    }

    public void logError(String error, Object... objects) {
        LOGGER.error(error, objects);
        this.errorsFound += 1;
    }

    public void logError(Throwable throwable) {
        LOGGER.error("Unknown error: \n", throwable);
        this.errorsFound += 1;
    }

    public void logWarning(String warning, Object... objects) {
        LOGGER.warn(warning, objects);
        this.warningsFound += 1;
    }

    public void stopOnCriticalError() {
        if (errorsFound > 0) {
            LOGGER.error("Errors encountered: {} and warnings encountered: {}. Conversion can't start.", errorsFound, warningsFound);
            System.exit(0);
        }
    }

    public void promptUserIntegrityStatus() {
        if (errorsFound > 0) {
            LOGGER.error("Errors encountered: {} and warnings encountered: {}. Conversion can't start.", errorsFound, warningsFound);
            System.exit(0);
        } else if (warningsFound > 0) {
            LOGGER.info("Warnings encountered: {}. Continue? [YES/NO]", warningsFound);
            Scanner sc = new Scanner(System.in);
            while (true) {
                String line = sc.nextLine().trim().toUpperCase();
                switch (line) {
                    case "YES":
                        LOGGER.info("Continuing........");
                        return;
                    case "NO":
                        LOGGER.info("Stopping the conversion.");
                        System.exit(0);
                        return;
                    default:
                        LOGGER.info("Wrong input, please enter a valid answer. [YES/NO]");
                }
            }
        }
    }

}
