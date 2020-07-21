/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>. */
package org.alfresco.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Matches a path against a set of regular expression filters
 *
 */
public class PatternFilter
{
    private List<Pattern> patterns;
    
    /**
     * A list of regular expressions that represent patterns of files.
     * 
     * @param regexps list of regular expressions
     * 
     * @see String#matches(java.lang.String)
     */
    public void setPatterns(List<String> regexps)
    {
        this.patterns = new ArrayList<Pattern>(regexps.size());
        for(String regexp : regexps)
        {
            this.patterns.add(Pattern.compile(regexp));
        }
    }

    public boolean isFiltered(String path)
    {
        // check against all the regular expressions
        boolean matched = false;

        for (Pattern regexp : patterns)
        {
            if(!regexp.matcher(path).matches())
            {
                // it is not a match - try next one
                continue;
            }
            else
            {
                matched = true;
                break;
            }
        }

        return matched;
    }
}
