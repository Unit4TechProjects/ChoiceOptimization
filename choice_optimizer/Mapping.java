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

import java.util.Collection;
import java.util.ArrayList;

/**
 * An interface for describing a mapping used in a choice optimization algorithm.
 * This interface is intended to be extended for solving choice optimization problems.
 *
 * @author Matthew Mussomele
 */
public interface Mapping<K extends Chooser, V extends Item> extends Comparable<Mapping<K, V>> {

    /**
     * Gets the list of items assigned to the given Chooser
     * 
     * @param chooser The chooser instance to get the assignments of 
     * @return A Collection of the given Chooser's assignments
     */
    Collection<V> getAssignments(K chooser);

    /**
     * Returns a defensively mutated copy of this Mapping. For use with genetic evolution.
     * 
     * @return A new, mutated version of this mapping
     */
    Mapping<K, V> mutate();

    /**
     * Gets the cost of this Mapping.
     * 
     * @return the cost of this mapping as a double
     */
    double getCost();

    @Override boolean equals(Object other);
    @Override int compareTo(Mapping<K, V> other);
    @Override int hashCode();

    /**
     * A builder interface for Mappings to allow for easy immutability.
     */
    public interface MappingBuilder<K extends Chooser, V extends Item> {

        /**
         * Assigns the given Item to the given Chooser
         * 
         * @param chooser The Chooser to assign the Item to
         * @param item The Item to assign to the Chooser
         */
        void putAssignment(K chooser, V item);

        /**
         * Assigns a list of Item instance to the given Chooser.
         * 
         * @param chooser The Chooser to assign the Item to
         * @param items The Items to assign to the Chooser
         */
        void putAssignmentList(K chooser, ArrayList<V> items);

        /**
         * Builds a new Mapping instance from this MappingBuilder
         * 
         * @return a new Mapping instance
         */
        Mapping<K, V> build();

    }

}
