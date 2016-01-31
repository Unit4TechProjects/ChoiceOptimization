package testing;

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

import duty_scheduler.RA;
import duty_scheduler.RA.RABuilder;
import duty_scheduler.Duty;
import duty_scheduler.Schedule;
import duty_scheduler.Schedule.ScheduleBuilder;

import java.util.ArrayList;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit Testing Class for the duty_scheduler.Schedule class.
 *
 * @author Matthew Mussomele
 */
public class TestSchedule {

    private static final int THIS_YEAR = 2015;

    private ScheduleBuilder testBuilder;
    private Schedule test;
    private ArrayList<RA> raList;
    private ArrayList<Duty> dutyList;

    /**
     * Fill the four instance variables with some simple test instances before every test.
     */
    @Before public void setUp() {
        dutyList = new ArrayList<Duty>();
        for (int i = 0; i < 6; i += 1) {
            dutyList.add(new Duty(THIS_YEAR, 1, i + 1, "/"));
        }
        raList = new ArrayList<RA>();
        for (int i = 0; i < 2; i += 1) {
            RABuilder builder = new RABuilder(String.format("RA%d", i), 6, 3);
            for (int j = 0; j < 6; j += 1) {
                if (i == 0) {
                    builder.putPreference(dutyList.get(j), j);
                } else {
                    builder.putPreference(dutyList.get(j), 5 - j);
                }
            }
            raList.add(builder.build());
        }
        ScheduleBuilder builder = new ScheduleBuilder(raList.size(), dutyList.size());
        for (int i = 0; i < 3; i += 1) {
            builder.putAssignment(raList.get(0), dutyList.get(5 - i));
        }
        for (int i = 0; i < 3; i += 1) {
            builder.putAssignment(raList.get(1), dutyList.get(i));
        }
        testBuilder = builder;
        test = builder.build();
    }

    /**
     * Tests that the ScheduleBuilder works and properly returns a Schedule instance
     */
    @Test public void testBuild() {
        for (int i = 0; i < raList.size(); i += 1) {
            assertTrue(testBuilder.doneAssigning(raList.get(i)));
        }
        assertNotNull(test);
    }

    /**
     * Tests that the built Schedule's basic functions perform to the requirements of the package.
     * These tests are to ensure that these methods are not changed by a programmer in such a way 
     * that would cause Schedules to be lost in a Hash or Tree based structure.
     */
    @Test public void testBasics() {
        for (int i = 0; i < raList.size(); i += 1) {
            for (Duty duty : test.getAssignments(raList.get(i))) {
                assertTrue(dutyList.contains(duty));
            }
        }
        assertTrue(test.equals(test));
        assertEquals(0, test.compareTo(test));
    }

}
