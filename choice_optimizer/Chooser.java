package choice_optimizer;

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

/**
 * An interface describing Choosers to be used in a choice optimization algorithm.
 *
 * @author Matthew Mussomele
 */
public interface Chooser { 

    /**
     * Checks if the given item is a valid assignment to this Chooser
     * 
     * @param item the Item to check for validity
     * @return True is the Item is valid
     */
    boolean eligibleItem(Item item);

    /**
     * Returns the preference weight of the given Item to this Chooser
     * 
     * @param item the Item to get the weigth of
     * @return The weight of the Item as an integer, lower should be more prefered
     */
    int itemWeight(Item item);

    @Override String toString();
    @Override boolean equals(Object other);
    @Override int hashCode();

    /**
     * ChooserBuilder class should be used to allow Chooser intances to be immutable.
     */
    public interface ChooserBuilder {

        /**
         * Gives the given Item the given preference value with this ChooserBuilder
         * 
         * @param item The item to give a preference value to
         * @param priority The preference value of the Item
         */
        void putPreference(Item item, Integer priority);

        /**
         * Builds and returns a Chooser instance from this builder
         * 
         * @return a new Chooser instance
         */
        Chooser build();

    }

}
