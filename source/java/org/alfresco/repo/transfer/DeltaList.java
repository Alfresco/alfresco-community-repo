/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
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
package org.alfresco.repo.transfer;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;


/**
 * Details back from reading the manifest to say what is required to fulfill the manifest.
 *
 * @author Mark Rogers
 */
public class DeltaList
{
    /**
     * The set of requiredParts
     */
    
    private TreeSet<String> requiredParts = new TreeSet<String>();
    
    /**
     * get the list of URLs reqired by the manifest.
     * @return the list of required URLs
     */
    public Set<String> getRequiredParts()
    {
        return requiredParts;
    }
     
}
