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

import java.util.ArrayList;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit Testing Class for the duty_scheduler.RA class.
 *
 * @author Matthew Mussomele
 */
public class TestRA {

    private static final int THIS_YEAR = 2015;

    private RA test;
    private ArrayList<Duty> duties;

    /**
     * Create an RA object to run tests on.
     */
    @Before public void setUp() {
        RABuilder builder = new RABuilder("Bob", 4, 2);
        duties = new ArrayList<Duty>();
        duties.add(new Duty(THIS_YEAR, 1, 1));
        duties.add(new Duty(THIS_YEAR, 1, 2));
        duties.add(new Duty(THIS_YEAR, 1, 3));
        duties.add(new Duty(THIS_YEAR, 1, 4));
        for (int i = 0; i < duties.size(); i += 1) {
            builder.putPreference(duties.get(i), i);
        }
        test = builder.build();
    }

    /**
     * Test basic functions like requiredDuties(), toString(), hashCode() and equals().
     */
    @Test public void testBasics() {
        assertEquals(2, test.requiredDuties());
        assertEquals("Bob", test.toString());
        assertEquals("Bob".hashCode(), test.hashCode());
        assertTrue(test.equals(test));
    }

    /**
     * Test that itemWeight(...) works correctly.
     */
    @Test public void testWeights() {
        assertEquals(Integer.MAX_VALUE, test.itemWeight(duties.get(0)));
        for (int i = 1; i < duties.size(); i += 1) {
            assertEquals(i, test.itemWeight(duties.get(i)));
        }
    }

    /**
     * Test that eligibleItem(...) works correctly.
     */
    @Test public void testEligible() {
        assertFalse(test.eligibleItem(duties.get(0)));
        for (int i = 1; i < duties.size(); i += 1) {
            assertTrue(test.eligibleItem(duties.get(i)));
        }
    }

}
