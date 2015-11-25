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

import java.util.HashMap;
import java.util.Collection;
import java.util.ArrayList;

/**
 * An abstract class representing assignment mappings in a choice optimization algorithm.
 * Built for extension.
 *
 * @author Matthew Mussomele
 */
public abstract class AbstractMapping<K extends Chooser, V extends Item> implements Mapping<K, V> {

    private static final int SHIFT_BY = 32;

    protected HashMap<K, ArrayList<V>> mappings;
    protected double cost;
    protected int items;

    /**
     * Returns a defensive copy of the chooser's schedule.
     * 
     * @param  chooser A chooser instance
     * @return         A defensive copy of the list of Item instances assigned to chooser.
     */
    public Collection<V> getAssignments(K chooser) {
        return new ArrayList<V>(mappings.get(chooser));
    }

    /**
     * Returns a defensively mutated copy of this Mapping. For use with genetic evolution.
     * 
     * @return A new, mutated version of this mapping
     */
    public abstract Mapping<K, V> mutate();
    
    /**
     * Gets the 'cost' of a mapping. The cost is some function of the Chooser assignments. 
     * Also known as the fitness.
     *         
     * @return the cost of this mapping
     */
    public double getCost() {
        return cost;
    }
    
    /**
     * Compares a Mapping to another Object for equality.
     * @param  other The Object to be compared against
     * @return       true if they are both Mappings and their costs are equal, false otherwise
     */
    @Override public boolean equals(Object other) {
        if (other instanceof AbstractMapping) {
            AbstractMapping o = (AbstractMapping) other;
            return (new Double(cost)).equals(o.getCost()) && mappings.equals(o.mappings);
        } else {
            return false;
        }
    }
    
    /**
     * Compares a Mapping to another by their costs.
     * 
     * @param  other The Mapping to compare this against.
     * @return       A value less than 0 if this is smaller, greater than if this is bigger, else 0 
     */
    @Override public int compareTo(Mapping<K, V> other) {
        return Double.compare(cost, other.getCost());
    }

    /**
     * Returns a hash of this Mapping.
     * 
     * @return a hash of this Mapping.
     */
    @Override public int hashCode() {
        long converted =  Double.doubleToLongBits(this.cost);
        return (int) (converted ^ (converted >>> SHIFT_BY));
    }

    /**
     * An abstract class used to build Mapping instances.
     */
    public abstract static class AbstractMappingBuilder<K extends Chooser, V extends Item> 
                                                        implements Mapping.MappingBuilder<K, V> {

        protected HashMap<K, ArrayList<V>> map;

        /**
         * Assigns the given Item to the given Chooser in this schedule.
         * 
         * @param chooser   The Chooser to assign the Item to
         * @param item      The Item to assign
         */
        public void putAssignment(K chooser, V item) {
            if (chooser == null || item == null) {
                throw new NullPointerException("Cannot have null Chooser or Item");
            }
            if (!map.containsKey(chooser)) {
                map.put(chooser, new ArrayList<V>());
            }
            map.get(chooser).add(item);
        }

        /**
         * Assigns all the Item instances in an ArrayList to the given Chooser
         * 
         * @param chooser     The Chooser to assign the Items to
         * @param list        The list of Item instances to be assigned. 
         */
        public void putAssignmentList(K chooser, ArrayList<V> list) {
            if (list == null) {
                throw new NullPointerException("Cannot assign a null list.");
            }
            map.put(chooser, list);
        }

        /**
         * Builds a new Mapping instance from this MappingBuilder
         * 
         * @return a new Mapping instance
         */
        public abstract Mapping<K, V> build();

    }

}
