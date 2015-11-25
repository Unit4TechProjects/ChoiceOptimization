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

import duty_scheduler.Duty;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit Testing Class for the duty_scheduler.Duty class.
 *
 * @author Amit Akula, Matthew Mussomele
 * 
 */
public class TestDuty {


    private static final int THIS_YEAR = 2015;

	private Duty feb_first;
	public Duty feb_second;
	private Duty feb_third;
	private Duty feb_fourth;


    /**
     * Create an RA object to run tests on.
     */
    @Before public void setUp() {
        feb_first = new Duty(THIS_YEAR, 1, 1);
        feb_second = new Duty(THIS_YEAR, 1, 2);
        feb_third = new Duty(THIS_YEAR, 1, 3);
        feb_fourth = new Duty(THIS_YEAR, 1, 4);
    }	


    /**
     * Test basic functions like getTime(), compareTo()
     */
    @Test public void testBasics() {
        assertEquals(0, feb_first.compareTo(new Duty(THIS_YEAR, 1, 1)));
        assertEquals(0, feb_fourth.compareTo(feb_fourth));

        assertEquals(1, feb_second.compareTo(feb_first));
        assertEquals(1, feb_fourth.compareTo(feb_third));

        assertEquals(-1, feb_first.compareTo(feb_second));
        assertEquals(-1, feb_third.compareTo(feb_fourth));

    }


    /**
     * Test that illegal duties are properly handled.
     */
    
    @Test(expected=Exception.class)
    public void testEligible1() {
        Duty illegal_duty = new Duty(THIS_YEAR, 50, 1);
    }

    @Test(expected=Exception.class)
    public void testEligible2() {
        Duty illegal_duty = new Duty(1400, 1, 1);
    }

}
