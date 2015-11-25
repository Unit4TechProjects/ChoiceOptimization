package duty_scheduler;

/**
 * Copyright (C) 2015 Matthew Mussomele
 *
 *  This file is part of ChoiceOptimizationAlgorithm
 *  
 *  ChoiceOptimizationAlgorithm is free software: you can redistribute it 
 *  and/or modify it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation, either version 3 of the 
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import java.util.Date;
import java.util.HashSet;

import java.text.SimpleDateFormat;

/**
 * Need to add JavaDoc comments
 */

public class Logger {

    private PrintWriter LOG = null;

    public Logger() {
        try {
             LOG = new PrintWriter(
                       String.format(
                           "scheduler_%s.log",
                           (new SimpleDateFormat("MM-dd-yyyy-hh:mm")).format(new Date())
                       )
                   );
         } catch (FileNotFoundException e) {
            ErrorChecker.printExceptionToLog(e);
         }
    }

    public void logOptionAttempt(String optionName, String optionValue) {
        if (LOG != null) {
            LOG.print(
                String.format(
                    "Attempting to set field \"%s\" to the value %s...   ",
                    optionName,
                    optionValue
                )
            );
        }
    }

    public void logSuccess() {
        if (LOG != null) {
            LOG.println("Succeeded.");
        }
    }

    public void logFailure() {
        if (LOG != null) {
            LOG.println("Failed.");
        }
    }

    public void logDefaultingFields(HashSet<String> optionFields) {
        if (LOG != null) {
            for (String unsetField : optionFields) {
                LOG.println(
                    String.format(
                        "Option %s unspecified... %sUsing default value.",
                        unsetField,
                        getTabs(unsetField)
                    )
                );
            }
        }
    }

    public void logError() {
        if (LOG != null) {
            LOG.println("Encountered an error... Exiting.");
        }
    }

    public void logFinishedExecution(String resultsFile) {
        if (LOG != null) {
            LOG.println(
                String.format(
                    "Execution complete. See %s for results.",
                    resultsFile
                )
            );
        }
    }

    public void close() {
        if (LOG != null) {
            LOG.close();
        }
    }

    private static String getTabs(String text) {
        String result = "";
        int tabsToInsert = 7 - (text.length() / 4);
        for (int i = 0; i < tabsToInsert; i += 1) {
            result += "\t";
        }
        if (text.length() % 4 == 0) {
            result += "\t";
        }
        return result;
    }

}