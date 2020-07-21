/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */

package org.alfresco.service.cmr.search;

import java.io.Serializable;
import java.util.List;

/**
 * @author Jamal Kaabi-Mofrad
 * @since 5.0
 */
public class SpellCheckResult implements Serializable
{
    private static final long serialVersionUID = -4270859221984496771L;

    private final String resultName;
    private final List<String> results;
    private final boolean searchedFor;
    private final boolean spellCheckExist;

    public SpellCheckResult(String resultName, List<String> results, boolean searchedFor)
    {
        this.resultName = resultName;
        this.results = results;
        this.searchedFor = searchedFor;
        this.spellCheckExist = (resultName == null) ? false : true;
    }

    /**
     * @return the resultName
     */
    public String getResultName()
    {
        return this.resultName;
    }

    /**
     * @return the results
     */
    public List<String> getResults()
    {
        return this.results;
    }

    /**
     * @return the searchedFor
     */
    public boolean isSearchedFor()
    {
        return this.searchedFor;
    }

    /**
     * @return the spellCheckExist
     */
    public boolean isSpellCheckExist()
    {
        return this.spellCheckExist;
    }
}
