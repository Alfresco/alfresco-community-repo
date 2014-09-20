/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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

package org.alfresco.repo.jscript;

import java.io.Serializable;
import java.util.List;

/**
 * @author Jamal Kaabi-Mofrad
 * @since 5.0
 */
public class ScriptSpellCheckResult implements Serializable
{
    private static final long serialVersionUID = -5947320933438147267L;

    private final String originalSearchTerm;
    private final String resultName;
    private final List<String> results;
    private final boolean searchedFor;
    private final boolean spellCheckExist;

    public ScriptSpellCheckResult(String originalSearchTerm, String resultName, boolean searchedFor,
                List<String> results, boolean spellCheckExist)
    {
        this.originalSearchTerm = originalSearchTerm;
        this.resultName = resultName;
        this.results = results;
        this.searchedFor = searchedFor;
        this.spellCheckExist = spellCheckExist;
    }

    /**
     * @return the originalSearchTerm
     */
    public String getOriginalSearchTerm()
    {
        return this.originalSearchTerm;
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
