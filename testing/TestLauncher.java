package testing;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import java.io.FilenameFilter;
import java.io.File;
    
/**
 * Finds and runs all tests in a generic testing package (This file must belong to it).
 * Compatible with any project to allow new JUnit test files to just be dropped straight into the
 * testing folder and run along with the others.
 *
 * Notes: If you are using this file for a project other than ChoiceOptimizationAlgorithm, you will need
 *        to change the fields of EXCLUDES below if you have files that match TEST_MATCHER that you
 *        do not want to be ran. 
 *        If you don't follow the test naming convention specified by TEST_MATCHER, you should also change 
 *        that to match your convention.
 *        If your testing package has a different name than "testing", change TEST_PACKAGE and the package
 *        decalaration at the start of this file to reflect that.
 *
 * @author Matthew Mussomele
 */
public class TestLauncher {

    private static final double       SECS_PER_MILLI = .001;

    /* The three fields below may not work with your project. Please consult the notes in the class header for more information. */
    private static final String[]           EXCLUDES = new String[]{"TestLauncher.*", "TestUtils.*"};
    private static final String         TEST_MATCHER = "(Test.*|.*Test)\\.class";
    private static final String         TEST_PACKAGE = "testing";

    private static final String            CLASS_EXT = ".class";
    private static final String                EMPTY = "";
    private static final String                  RAN = "Ran %d tests.";
    private static final String               PASSED = "\tAll passed.";
    private static final String     PASSED_FOR_CLASS = "\tAll tests for %s passed.";
    private static final String     FAILED_FOR_CLASS = "\tFailed %d tests out of %d for %s.";
    private static final String               FAILED = "\t%d tests failed. ";
    private static final String  RUNNING_CLASS_TESTS = "Running tests for %s";
    private static final String                 TIME = "\tTime: %.3f";
    private static final String          TEST_HEADER = "%d) %s";
    private static final String           BAD_ASSERT = "    Assertion failed";
    private static final String          NO_RUNNABLE = "java.lang.Exception: No runnable methods";
    private static final String             NO_TESTS = "\tTest file had no runnable methods.";
    private static final String     FINISHED_TESTING = "All tests completed. See report above for details.";
    private static final String        JUNIT_PACKAGE = "org.junit.";
    private static final String          ANT_PACKAGE = "org.apache.tools.ant.";

    private static final File         TESTING_FOLDER = new File(TestLauncher.class.getProtectionDomain().getCodeSource().getLocation().getPath(), TEST_PACKAGE);

    private static final FilenameFilter FILE_FILTER = new FilenameFilter() {
        /**
         * Checks if a file within a directory should be filtered out by this FilenameFilter
         * 
         * @param  dir  The directory in which the file is contained
         * @param  name The name of the file
         * @return      True if the file should NOT be filtered out
         */
        public boolean accept(File dir, String name) {
            return notExcluded(name) && name.matches(TEST_MATCHER);
        }

        /**
         * Checks if the given file has been excluded from the files matching TEST_MATCHER
         * 
         * @param  fileName The filename to check
         * @return          True if the file has NOT been excluded
         */
        private boolean notExcluded(String fileName) {
            for (String exclude : EXCLUDES) {
                if (fileName.matches(exclude)) {
                    return false;
                }
            }
            return true;
        }
    };

    private static int testCount = 0;
    private static int failCount = 0;

    /**
     * Constructs the string you would pass to the 'java' command if you were running the given class from the command line
     *
     * example:
     *     getClassRef("testing", "TestFoo.class") --> testing.TestFoo
     *         
     * @param  pack     The package that the given class file belongs to
     * @param  fileName The file name of the class file
     * @return          A reformatted command-line friendly version of the class name
     */
    private static String getClassRef(String pack, String fileName) {
        return String.format("%s.%s", pack, fileName.replace(CLASS_EXT, EMPTY));
    }

    /**
     * Gets all of the files in a directory that match FILE_FILTER's rules
     * 
     * @param  directory The directory to search
     * @return           An array of all files matching FILE_FILTER inside the given directory
     */
    private static String[] getTestClassFileNames(File directory) {
        return directory.list(FILE_FILTER);
    }

    /**
     * Runs all test files who's names are contained in the classNames array
     * 
     * @param classNames The list of class file names to run
     */
    private static void runAllTests(String[] classNames) {
        for (String className : classNames) {
            runClassTest(loadClass(className));
        }
        System.out.print(String.format(RAN, testCount));
        if (failCount == 0) {
            System.out.println(PASSED);
        } else {
            System.out.println(String.format(FAILED, failCount));
        }
    }

    /**
     * Loads and returns the Class that corresponds to className
     *
     * @param className The name of the class to load
     * @return A Class object referring to the class called className
     */
    private static Class<?> loadClass(String className) {
        try {
            return Class.forName(getClassRef(TEST_PACKAGE, className));
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * Runs the tests contained within a single Class
     * 
     * @param testClass The Class who's tests to run
     */
    private static void runClassTest(Class<?> testClass) {
        if (testClass == null) {
            return;
        }
        System.out.println(String.format(RUNNING_CLASS_TESTS, testClass.toString()));
        Result result = JUnitCore.runClasses(testClass);
        testCount += result.getRunCount();
        printTestResults(result, testClass.toString());
    }

    /**
     * Prints the results of a JUnit Result object, obtained from running a test file
     * 
     * @param result        The Result object returned by JUnit after running a test file
     * @param testClassName The name of the test class who's results are being printed
     */
    private static void printTestResults(Result result, String testClassName) {
        System.out.println(String.format(TIME, result.getRunTime() * SECS_PER_MILLI));
        if (result.getFailureCount() > 0) {
            failCount += result.getFailureCount();
            int i = 1;
            System.out.println(String.format(FAILED, result.getFailureCount()));
            for (Failure failure : result.getFailures()) {
                Throwable e = failure.getException();
                if (!e.toString().equals(NO_RUNNABLE)) {
                    System.out.println(failure.toString());
                    System.out.println(String.format(TEST_HEADER, i++, failure.getTestHeader()));
                    printTestException(e);
                    printStackTrace(e);
                } else {
                    System.out.println(NO_TESTS);
                }
            }
            System.out.println(String.format(FAILED_FOR_CLASS, 
                                             result.getFailureCount(),
                                             result.getRunCount(),
                                             testClassName
                                             ));
        } else {
            System.out.println(String.format(PASSED_FOR_CLASS, testClassName));
        }
        System.out.println();
    }

    /**
     * Prints the details of the exception thrown by a failed JUnit test
     *     
     * @param e An exception thrown by a failed JUnit test
     */
    private static void printTestException(Throwable e) {
        if (e instanceof AssertionError) {
            if (e.getMessage() == null) {
                System.out.println(BAD_ASSERT);
            } else {
                System.out.println(String.format("    %s", e.getMessage()));
            }
        } else {
            if (e.getCause() != null) {
                e = e.getCause();
            }
            System.out.println(String.format("    %s", e));
        }
    }

    /**
     * Prints the stack trace of an exception if it doesn't belong to the JUnit package for debugging
     * 
     * @param e The exception to print the stack trace of
     */
    private static void printStackTrace(Throwable e) {
        for (StackTraceElement frame : e.getStackTrace()) {
            if (frame.getClassName().startsWith(JUNIT_PACKAGE) || frame.getClassName().startsWith(ANT_PACKAGE)) {
                continue;
            } 
            printPosition(frame);
        }
    }

    /**
     * Prints frame's position in the file.
     * @param frame the frame to print
     */
    private static void printPosition(StackTraceElement frame) {
        if (frame.isNativeMethod()) {
            System.out.println(String.format("    at %s.%s (native method)",
                               frame.getClassName(),
                               frame.getMethodName()));
        } else {
            System.out.println(String.format("    at %s.%s:%d (%s)",
                               frame.getClassName(),
                               frame.getMethodName(),
                               frame.getLineNumber(),
                               frame.getFileName()));
        }
    }

    /**
     * Runs JUnit tests and prints verbose information about their results.
     * PrintPosition function and knowledge of JUnit API taken from Josh Hug's 
     * jh61b package. See https://github.com/Berkeley-CS61B/skeleton/tree/master/lib
     * @param args command line args, this function takes none
     */
    public static void main(String[] args) {
        String[] testFiles = getTestClassFileNames(TESTING_FOLDER);
        runAllTests(testFiles);
        System.out.println(FINISHED_TESTING);
    }

}
