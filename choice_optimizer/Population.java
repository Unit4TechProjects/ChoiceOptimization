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

/**
 * An interface for describing a population used in a genetic algorithm.
 * This interface is intended to be extended for solving choice optimization problems.
 *
 * @author Matthew Mussomele
 */
public interface Population<C extends Chooser, I extends Item> {

    /**
     * Seed a Population instance with a mapping of Choosers to Items
     * @param chooserList the list of choosers to be used in the seed
     * @param itemList the list of items to be used in the seed
     */
    void seed(Collection<C> chooserList, Collection<I> itemList);

    /**
     * Starts the running of a genetic algorithm on this Population
     * 
     * @return The best Mapping of Choosers to Items found during evolution
     */
    Mapping evolve();

}
