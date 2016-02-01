
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

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.io.File;

/**
 * A class meant for performing error checking and data validation on inputs to the algorithm.
 * @author Matthew Mussomele
 *
 */
public class ErrorChecker {

    public static final int NO_BOUND = -1;
    private static final double PERC_ALLOWED_UNDOABLE = .5;
    
    private static int[] cantDosByRA;
    private static int[] cantDosByDuty;
    private static int[] prefSumPerRA;

    /**
     * Private constructor to enforce noninstantiability
     */
    private ErrorChecker() {
        throw new AssertionError();
    }
    
    /**
     * Checks if a given integer with value 'val' is within the allowed bounds.
     * 
     * @param var the name of the variable that is being tested
     * @param val the value of the variable that is being tested
     * @param lower the lower bound on the integer's allowed values (inclusive)
     * @param upper the upper bound on the integer's allowed values (inclusive)
     */
    public static void inBounds(String var, int val, int lower, int upper) {
        if (upper == NO_BOUND) {
            if (lower == NO_BOUND) {
                throw new RuntimeException("At least one bound must be defined.");
            } else {
                overBound(var, val, lower);
            }
        } else if (lower == NO_BOUND) {
            underBound(var, val, upper);
        } else if (val > upper || val < lower) {
            throw new IllegalArgumentException(String.format("The given value %d is out of the "
                                                + "bounds %d <= x <= %d", val, lower, upper));
        }
    }
    
    /**
     * Assigns the integer at index j of cantDosByRA to how many duties the RA at index j of raList 
     * has labeled undoable, and assigns the integer at index i of cantDosByDuty to how many RAs 
     * have labeled the Duty at index i of dutyList undoable.
     *
     * @param raList an <code>ArrayList</code> containing all of the RAs
     * @param dutyList an <code>ArrayList</code> containing all of the duties
     */
    public static void evalPrefs(ArrayList<RA> raList, ArrayList<Duty> dutyList) {
        cantDosByRA = new int[raList.size()];
        cantDosByDuty = new int[dutyList.size()];
        prefSumPerRA = new int[raList.size()];
        int dutiesToAssign = 0;
        for (int i = 0; i < dutyList.size(); i++) {
            for (int j = 0; j < raList.size(); j++) {
                if (i == 0) {
                    dutiesToAssign += raList.get(j).requiredDuties();
                }
                if (!raList.get(j).eligibleItem(dutyList.get(i))) {
                    cantDosByDuty[i]++;
                    cantDosByRA[j]++;
                } else {
                    prefSumPerRA[j] += raList.get(j).itemWeight(dutyList.get(i));
                }
            }
        }
        if (dutiesToAssign != dutyList.size()) {
            throw new RuntimeException("The number of duties to assign to each RA does not sum to "
                                                   + "the number of duties that need assignment. ");
        }
    }
    
    /**
     * Checks and makes sure the preferences are (probably) of the form 1, 2, ..., n. 
     */
    public static void checkConsistency(ArrayList<RA> raList) {
        for (int i = 0; i < prefSumPerRA.length; i++) {
            if (prefSumPerRA[i] != (sumTo(cantDosByDuty.length - cantDosByRA[i]))) {
                throw new IllegalArgumentException("One or more RAs have not labeled their doable "
                    + "duties from 1 to n, where n is totalDuties - (duties labeled as cant do). "
                    + "Please revise the preferences.");
            }
        }
    }
    
    /**
     * Finds the sum of all integers 0...n
     *     
     * @param  n The number to sum to
     * @return   the sum of all integers 0...n
     */
    private static int sumTo(int n) {
        return n * (n + 1) / 2;
    }
    
    /**
     * Checks to see if the set of preferences has made the duties impossible to schedule by 
     * standard scheduling rules.
     */
    public static void checkImpossible() {
        for (int i = 0; i < cantDosByDuty.length; i++) {
            if (cantDosByDuty[i] == cantDosByRA.length) {
                throw new IllegalArgumentException("One or more duties have been listed as undoable"
                + " by every RA.\nPlease run the algorithm again with the 'allowBads' argument or "
                + "change the preference values.\nIt is advised to resolve this issue by changing "
                + "the preferences, as running the algorithm with 'allowBads' drastically increases"
                + " the time required to find a good solution.");
            }
        }
    }
    
    /**
     * Checks to see if one or more RAs have labeled too many duties as undoable. 
     * This is to prevent RAs from labeled every duty but the ones they really want as undoable.
     * @param raList an <code>ArrayList</code> containing all of the RAs
     */
    public static void checkGreedy(ArrayList<RA> raList) {
        ArrayList<String> badRAs = new ArrayList<String>();
        for (int i = 0; i < cantDosByRA.length; i++) {
            if (cantDosByRA[i] >= PERC_ALLOWED_UNDOABLE * cantDosByDuty.length) {
                badRAs.add(raList.get(i).toString());
            }
        }
        if (badRAs.size() > 0) {
            throw new IllegalArgumentException(String.format("The following RAs have listed too "
                + "many duties as undoable: %s\nRun the algorithm again with the ALLOW_GREEDY "
                + "option, or change the preference input.", badRAs.toString()));
        }
    }
    
    /**
     * Checks if val >= lower
     * 
     * @param var the name of the variable that is being tested
     * @param val the value of the variable that is being tested
     * @param lower the lower bound on the integer's allowed values (inclusive)
     */
    public static void overBound(String var, int val, int lower) {
        if (val >= lower) {
            return;
        }
        throw new IllegalArgumentException(String.format("Value %d is less than lower "
                                                                    + "bound of %d.", val, lower));
    }
    
    /**
     * Checks if val <= upper
     * 
     * @param var the name of the variable that is being tested
     * @param val the value of the variable that is being tested
     * @param upper the upper bound on the integer's allowed values (inclusive)
     */
    public static void underBound(String var, int val, int upper) {
        if (val <= upper) {
            return;
        }
        throw new IllegalArgumentException(String.format("Value %d is greater than upper "
                                                                    + "bound of %d.", val, upper));
    }
    
    /**
     * Prints a thrown exception to an error log file and exits. If recording the exception fails,
     * a message is printed.
     * @param e A thrown exception to be recorded.
     */
    public static void printExceptionToLog(Exception e) {
        String dateTime = (new SimpleDateFormat("MM-dd-yyyy-hh:mm:ss")).format(new Date());
        File log = new File("error_log.txt");
        PrintWriter out = null; 
        try {
            if (!log.exists()) {
                log.createNewFile();
            } 
            out = new PrintWriter(new FileWriter(log, true));
            String message = dateTime + "\n";
            int len = message.length();
            for (int i = 0; i < len; i += 1) {
                message += "_";
            }
            out.println(message);
            e.printStackTrace(out);
            out.println("\n");

        } catch (IOException ioe) {
            System.out.println("Error logging failed.");
        } finally {
            out.close();
            try {
                Scheduler.LOGGER.logError();
                Scheduler.LOGGER.close();
            } catch (NullPointerException npe) {
                System.out.println("Execution log not instantiated.");
            }
            System.exit(-1);
        }
    }

}
