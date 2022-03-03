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

import lombok.extern.slf4j.Slf4j;

import java.util.Scanner;

/**
 * Used to restrict the execution of code based on the errors and warnings encountered.
 */
@Slf4j
public class IntegrityHandler {

    private int errorsFound;
    private int warningsFound;

    public IntegrityHandler() {
        this.errorsFound = 0;
        this.warningsFound = 0;
    }

    // logs the error and increases the number of errors encountered
    public void logError(String error, Object... objects) {
        log.error(error, objects);
        this.errorsFound += 1;
    }

    // logs the error and increases the number of errors encountered
    public void logError(Throwable throwable) {
        log.error("Unknown error: \n", throwable);
        this.errorsFound += 1;
    }

    // logs the warning and increases the number of warnings encountered
    public void logWarning(String warning, Object... objects) {
        log.warn(warning, objects);
        this.warningsFound += 1;
    }

    /**
     * If at least one error was found it logs the warnings and errors encountered and stops the whole program.
     */
    public void stopOnCriticalError() {
        if (errorsFound > 0) {
            log.error("Errors encountered: {} and warnings encountered: {}. Conversion can't start.", errorsFound, warningsFound);
            System.exit(0);
        }
    }

    /**
     * If at least one error was found it logs the warnings and errors encountered and stops the whole program.
     * If at least one warning was found it asks the user if it should continue or stop the program.
     */
    public void promptUserIntegrityStatus() {
        if (errorsFound > 0) {
            log.error("Errors encountered: {} and warnings encountered: {}. Conversion can't start.", errorsFound, warningsFound);
            System.exit(0);
        } else if (warningsFound > 0) {
            log.info("Warnings encountered: {}. Continue? [YES/NO]", warningsFound);
            Scanner sc = new Scanner(System.in);
            while (true) {
                String line = sc.nextLine().trim().toUpperCase();
                switch (line) {
                    case "YES":
                        log.info("Continuing........");
                        return;
                    case "NO":
                        log.info("Stopping the conversion.");
                        System.exit(0);
                        return;
                    default:
                        log.info("Wrong input, please enter a valid answer. [YES/NO]");
                }
            }
        }
    }

}
