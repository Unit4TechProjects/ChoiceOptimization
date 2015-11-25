package duty_scheduler;

/**
 * Copyright (C) 2015 Matthew Mussomele, Amit Akula
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

import java.util.Calendar;

import choice_optimizer.AbstractItem;
import choice_optimizer.Item;

/**
 * A class used to represent a day on which RA Duty takes place. 
 * This class is immutable.
 *
 * @author Matthew Mussomele
 */
public class Duty extends AbstractItem {

    private static final int MIN_YEAR = 2014;
    private static final int MAX_MONTH = 11;
    private static final int MIN_MONTH = 0;
    
    private Calendar date;

    /**
     * Creates a new Duty instance that represents the date month/day/year. 
     * 
     * @param  year  The year of this date. Must be later than 2014.
     * @param  month The month of this date. Must be in between 0 and 11, inclusive.
     * @param  day   The day of this date. It is trusted that this date is valid. 
     */
    public Duty(int year, int month, int day) {
        date = Calendar.getInstance();
        try {
            ErrorChecker.inBounds("year", year, MIN_YEAR, -1);
            ErrorChecker.inBounds("month", month, MIN_MONTH, MAX_MONTH);
        } catch (IllegalArgumentException e) {
            ErrorChecker.printExceptionToLog(e);
        
        } 
        date.set(year, month, day, 0, 0, 0); //Note: the constructor trusts that the day is valid
        date.set(Calendar.MILLISECOND, 0);
        String formatString = "%d-";
        if (month < 10) {
            formatString += "0";
        }
        formatString += "%d-";
        if (day < 10) {
            formatString += "0";
        }
        formatString += "%d";
        stringRep = String.format(formatString, year, month, day);
    }

    /**
     * Returns the time represented by this Duty.
     * 
     * @return the milliseconds since the Epoch
     */
    public long getTime() {
        return date.getTimeInMillis();
    }

    /**
     * Compares this Duty to another by comparing the dates they represent.
     *         
     * @param  other another object to compare against 
     * @return       0 if they represent the same time, 1 if this Duty is later, -1 otherwise.
     */
    @Override public int compareTo(Item other) {
        if (other instanceof Duty) {
            long difference = getTime() - ((Duty) other).getTime();
            if (difference > 0) {
                return 1;
            } else if (difference < 0) {
                return -1;
            } else {
                return 0;
            }
        } else {
            ErrorChecker.printExceptionToLog(new IllegalArgumentException("Can only compare "
                                             + "Duty instances to other Duty instances."));
            return 0;
        }
    }

}
