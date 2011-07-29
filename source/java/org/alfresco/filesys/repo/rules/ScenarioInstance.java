/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.filesys.repo.rules;

/**
 * A scenario instance is an active scenario.   It has a ranking, an 
 * evaluate method and knows whether it is complete.
 * <p>
 * The evaluate method is called repeatedly as operations are processed. 
 */
public interface ScenarioInstance
{
    enum Ranking
    {
        LOW,   // Bottom priority
        MEDIUM, 
        HIGH,
        
    };
    
    /**
     * Get the Ranking
     * @return
     */
    public Ranking getRanking();
    
    /**
     * evaluate the scenario against the current operation
     * 
     * @param operation
     */
    public Command evaluate(Operation operation);
    
    /**
     * Is the scenario complete?
     *
     * @return
     */
    public boolean isComplete();     
    
    
}
