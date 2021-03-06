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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.File;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import java.text.SimpleDateFormat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.Date;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Main class for running the RA duty scheduling algorithm. 
 * 
 * @author Matthew Mussomele
 */
public class Scheduler {

    static final Logger LOGGER = new Logger();

    private static final long NANOS_PER_SEC = 1000000000;
    private static final double MUTATE_DEFAULT = 0.21602435146951632;
    private static final int HRS_PER_DAY = 24;
    private static final int MINS_PER_HR = 60;
    private static final int SECS_PER_MINUTE = 60;
    private static final int MILLIS_PER_SEC = 1000;

    /* Matches dates formatted like mm/dd/yyyy, allowing for '-' or '.' in place of '/' */
    private static final String DATE_REGEX = "[0-9]{1,2}([/\\-\\.])[0-9]{1,2}\\1[0-9]{4}";

    static final int INVALID_ITEM_PRIORITY = 0;
    static final int SEED_COUNT;
    static final int EVOLVE_ITERS;
    static final int NUM_RUNS;
    static final int RESOURCE_FACTOR;
    static final int ALLOWED_SEED_ATTEMPTS;
    static final double MUTATION_CHANCE;
    static final boolean ALLOW_ILLEGALS;
    static final boolean ALLOW_GREEDY;
    static final boolean CONSIDER_ADJACENTS;
    static final boolean ANALYZE;
    static final String DATA_FILE;
    static final String ANALYTICS_FILE;

    private static final HashSet<String> optionFields = createHashSet(
                                                            "SEED_COUNT",
                                                            "EVOLVE_ITERS",
                                                            "NUM_RUNS",
                                                            "RESOURCE_FACTOR",
                                                            "ALLOWED_SEED_ATTEMPTS",
                                                            "MUTATION_CHANCE",
                                                            "ALLOW_ILLEGALS",
                                                            "ALLOW_GREEDY",
                                                            "CONSIDER_ADJACENTS",
                                                            "ANALYZE",
                                                            "DATA_FILE",
                                                            "ANALYTICS_FILE"
                                                        );
    
    private static ArrayList<RA> raList;
    private static ArrayList<Duty> dutyList;
    private static HashMap<String, Duty> dutyLookup;
    private static double[][] analytics;

    /**
     * The following initializer reads all necessary data from the config file. If the file
     * is absent the program uses the default global package constant values. If the file is
     * formatted improperly, then the program logs an exception and exits.
     */
    static {
        int defaultsc = 30;
        int defaultei = 1000;
        int defaultnr = 20;
        int defaultrf = 10;
        int defaultasa = 1000;
        double defaultmc = MUTATE_DEFAULT;
        boolean defaultai = false;
        boolean defaultag = false;
        boolean defaultca = true;
        boolean defaultv = false;
        String defaultdf = "data.json";
        String defaultaf = "analytics.txt";
        BufferedReader reader = null;
        try {
            int lineNumber = 0;
            reader = new BufferedReader(new FileReader("scheduler.config"));
            String line = reader.readLine();
            while (line != null) {
                String[] data = line.split("=");
                String fieldName = data[0];
                String fieldValue = data[1];
                if (optionFields.contains(fieldName)) {
                    optionFields.remove(fieldName);
                    LOGGER.logOptionAttempt(fieldName, fieldValue);
                }
                switch (fieldName) {
                    case "SEED_COUNT":
                        defaultsc = Integer.parseInt(fieldValue);
                        if (defaultsc % 2 == 1 || defaultsc <= 0) {
                            throw new IllegalArgumentException("SEED_COUNT must be positive and "
                                                                + "even.");
                        }
                        break;
                    case "EVOLVE_ITERS":
                        defaultei = Integer.parseInt(fieldValue);
                        if (defaultei <= 0) {
                            throw new IllegalArgumentException("EVOLVE_ITERS must be positive.");
                        }
                        break;
                    case "NUM_RUNS":
                        defaultnr = Integer.parseInt(fieldValue);
                        if (defaultnr <= 0) {
                            throw new IllegalArgumentException("NUM_RUNS must be positive.");
                        }
                        break;
                    case "RESOURCE_FACTOR":
                        defaultrf = Integer.parseInt(fieldValue);
                        if (defaultrf <= 1) {
                            throw new IllegalArgumentException("RESOURCE_FACTOR must be greater "
                                                                + "than one");
                        }
                        break;
                    case "ALLOWED_SEED_ATTEMPTS":
                        defaultasa = Integer.parseInt(fieldValue);
                        if (defaultasa <= 0) {
                            throw new IllegalArgumentException("ALLOWED_SEED_ATTEMPTS must be "
                                                                + "positive.");
                        }
                        break;
                    case "MUTATION_CHANCE":
                        defaultmc = Double.parseDouble(fieldValue);
                        if (defaultmc <= 0 || defaultmc >= 1) {
                            throw new IllegalArgumentException("MUTATION_CHANCE must be within "
                                                                + "(0, 1)");
                        }
                        break;
                    case "ALLOW_ILLEGALS":
                        defaultai = Boolean.parseBoolean(fieldValue);
                        break;
                    case  "ALLOW_GREEDY":
                        defaultag = Boolean.parseBoolean(fieldValue);
                        break;
                    case "CONSIDER_ADJACENTS":
                        defaultca = Boolean.parseBoolean(fieldValue);
                        break;
                    case "ANALYZE":
                        defaultv = Boolean.parseBoolean(fieldValue);
                        break;
                    case "DATA_FILE":
                        if (!fieldValue.endsWith(".csv")) {
                            throw new IllegalArgumentException(String.format("Data file must " 
                                    + "be a csv file, was a .%s.", fieldValue.split(".")[1]));
                        } else {
                            defaultdf = fieldValue;
                        }
                        break;
                    case "ANALYTICS_FILE":
                        defaultaf = fieldValue;
                        break;
                    default:
                        throw new IllegalArgumentException(String.format("Invalid field name" 
                                    + " %s on line %d.", fieldName, lineNumber));
                }
                LOGGER.logSuccess();
                lineNumber += 1;
                line = reader.readLine();
            }
        } catch (IOException e) {
            System.out.println("Using default values.");
        } catch (IllegalArgumentException e) {
            LOGGER.logFailure();
            ErrorChecker.printExceptionToLog(e);
        } finally {
            LOGGER.logDefaultingFields(optionFields);
            SEED_COUNT = defaultsc;
            EVOLVE_ITERS = defaultei;
            NUM_RUNS = defaultnr;
            RESOURCE_FACTOR = defaultrf;
            ALLOWED_SEED_ATTEMPTS = defaultasa;
            MUTATION_CHANCE = defaultmc;
            ALLOW_ILLEGALS = defaultai;
            ALLOW_GREEDY = defaultag;
            CONSIDER_ADJACENTS = defaultca;
            ANALYZE = defaultv;
            DATA_FILE = defaultdf;
            ANALYTICS_FILE = defaultaf;
            dutyList = new ArrayList<Duty>();
            raList = new ArrayList<RA>();
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                System.out.println("Could not close config reader.");
            }
        }
    }

    /**
     * Private construction to prevent instantiation.
     */
    private Scheduler() {
        throw new AssertionError();
    }

    /**
     * Reads in a JSON data file and constructs RA and Duty instances from it. 
     */
    private static void parseData(boolean primary) {
        raList = new ArrayList<>();
        dutyList = new ArrayList<>();
        dutyLookup = new HashMap<>();
        Charset ascii = StandardCharsets.US_ASCII;
        BufferedReader reader = null;
        try {
            Path inFile = Paths.get(DATA_FILE);
            reader = Files.newBufferedReader(inFile, ascii);
            String[] headings = reader.readLine().split(",");
            int dateStartIndex = -1, dateEndIndex = -1, nameIndex = -1, dutyCountIndex = -1;
            for (int i = 0; i < headings.length; i++) {
                if (Pattern.matches(DATE_REGEX, headings[i])) {
                    if (dateStartIndex == -1) {
                        dateStartIndex = i;
                    }
                    String[] fields = headings[i].split("[/\\-\\.]");
                    Duty d = new Duty(Integer.parseInt(fields[2]),
                                      Integer.parseInt(fields[0]),
                                      Integer.parseInt(fields[1]),
                                      "/");
                    dutyLookup.put(headings[i], d);
                    dutyList.add(d);
                } else {
                    if (dateStartIndex != -1 && dateEndIndex == -1) {
                        dateEndIndex = i;
                    }
                    if (headings[i].equals("Name")) {
                        nameIndex = i;
                    } else if (headings[i].equals("Duties")) {
                        dutyCountIndex = i;
                    }
                }
            }

            String line = reader.readLine();
            while (line != null) {
                String[] values = line.split(",");
                RA.RABuilder builder = new RA.RABuilder(values[nameIndex],
                                                        dutyList.size(),
                                                        Integer.parseInt(values[dutyCountIndex]));
                for (int i = dateStartIndex; i <= dateEndIndex; i++) {
                    builder.putPreference(dutyLookup.get(headings[i]), Integer.parseInt(values[i]));
                }
                raList.add(builder.build());
                line = reader.readLine();
            }

            ErrorChecker.evalPrefs(raList, dutyList);
            if (primary) {
                ErrorChecker.checkConsistency(raList);
            }
            if (!ALLOW_ILLEGALS) {
                ErrorChecker.checkImpossible();
            } 
            if (!ALLOW_GREEDY) {
                ErrorChecker.checkGreedy(raList);
            }
        } catch (RuntimeException e) {
            ErrorChecker.printExceptionToLog(e);
        } catch (IOException e) {
            ErrorChecker.printExceptionToLog(e);
        }
    }

    /**
     * Runs the choice optimization algorithm on the data and finds a good schedule
     * 
     * @return The best schedule found
     */
    private static Schedule run() {
        Schedule best = null;
        Schedule localBest = null;
        Generation thisGen = null;
        if (ANALYZE) {
            analytics = new double[NUM_RUNS][];
        }
        for (int i = 0; i < NUM_RUNS; i += 1) {
            thisGen = new Generation();
            thisGen.seed(raList, dutyList);
            localBest = thisGen.evolve();
            if (ANALYZE) {
                analytics[i] = thisGen.getHistory();
            }
            if (best == null || localBest.getCost() < best.getCost()) {
                best = localBest;
            }
        }
        return best;
    }

    /**
     * Creates a new HashSet containing the given strings
     * 
     * @param  elements the strings to add to the hashset
     * @return          a new hashset containing the given strings
     */
    private static HashSet<String> createHashSet(String... elements) {
        HashSet<String> newSet = new HashSet<String>();
        for (String element : elements) {
            newSet.add(element);
        }
        return newSet;
    }

    /**
     * Generates a String representation of the runtime of the algorithm
     * 
     * @param nanos The number of nanoseconds that the algorithm took to run
     * @return A string describing how long the algorithm took to finish execution
     */
    private static String runTime(long nanos) {
        long minutes = TimeUnit.NANOSECONDS.toMinutes(nanos) 
                        - (TimeUnit.NANOSECONDS.toHours(nanos) * MINS_PER_HR);
        long seconds = TimeUnit.NANOSECONDS.toSeconds(nanos) 
                        - (TimeUnit.NANOSECONDS.toMinutes(nanos) * SECS_PER_MINUTE);
        long millis = TimeUnit.NANOSECONDS.toMillis(nanos)
                        - (TimeUnit.NANOSECONDS.toSeconds(nanos) * MILLIS_PER_SEC);
        return String.format("Time Elapsed: %d minutes, %d seconds, %d milliseconds", 
                             minutes, seconds, millis);
    }

    /**
     * Prints the results of the algorithm to a file
     * 
     * @param best The best Schedule found during the run
     * @param runTimeReport A String describing the runtime of the algorithm
     */
    private static void printResults(Schedule best, String runTimeReport, boolean secondary) {
        String loc = DATA_FILE.substring(0, DATA_FILE.lastIndexOf(".")).replaceAll(" ", "_");
        String resultsFile = "schedule_" 
                        + (new SimpleDateFormat("MM-dd-yyyy-hh:mm")).format(new Date());
        if (secondary) {
            resultsFile += "-secondary.txt";
        } else {
            resultsFile += loc + "-primary.txt";
        }
        PrintWriter dataOut = null;
        PrintWriter tempOut = null;
        try {
            dataOut = new PrintWriter(resultsFile);
            tempOut = new PrintWriter(loc + ".temp");
            dataOut.println(runTimeReport);
            dataOut.println("Duty Assignments:\n\n");
            dataOut.println(best.toString());
            tempOut.println(best.toCSV());
        } catch (IOException e) {
            ErrorChecker.printExceptionToLog(e);
        } finally {
            dataOut.close();
            tempOut.close();
            LOGGER.logFinishedExecution(resultsFile);
            LOGGER.close();
        }
    }

    /**
     * Prints analytics data to a space separated file called analytics.txt
     */
    private static void printAnalytics() {
        PrintWriter analysisOut = null;
        try {
            analysisOut = new PrintWriter(ANALYTICS_FILE);
            for (double[] generationData : analytics) {
                for (double dataPoint : generationData) {
                    analysisOut.print(String.format("%.3f ", dataPoint));
                }
                analysisOut.println();
            }
        } catch (IOException e) {
            ErrorChecker.printExceptionToLog(e);
        } finally {
            analysisOut.close();
        }
    }

    /**
     * Main
     * 
     * @param args Command line arguments 
     */
    public static void main(String[] args) {
        boolean secondary = false;
        if (args.length == 1) {
            secondary = args[0].equals("-s");
        }
        try {
            long timeElapsed = System.nanoTime();
            parseData(!secondary);
            System.out.println("Parsed Data. Running...");
            Schedule best = run();
            System.out.println("Finished Run. Writing Results...");
            printResults(best, runTime(System.nanoTime() - timeElapsed), secondary);
            if (ANALYZE) {
                printAnalytics();
            }
        } catch (Exception e) {
            ErrorChecker.printExceptionToLog(e);
        }
    }

}
