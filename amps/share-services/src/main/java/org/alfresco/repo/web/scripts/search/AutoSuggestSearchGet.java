/*
 * Copyright 2005 - 2020 Alfresco Software Limited.
 *
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of the paid license agreement will prevail.
 * Otherwise, the software is provided under the following open source license terms:
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
package org.alfresco.repo.web.scripts.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.search.SuggesterParameters;
import org.alfresco.service.cmr.search.SuggesterResult;
import org.alfresco.service.cmr.search.SuggesterService;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * This class is the controller for the <i>auto-suggest-search.get</i> web
 * script.
 * 
 * @author Jamal Kaabi-Mofrad
 * @since 5.0
 */
public class AutoSuggestSearchGet extends DeclarativeWebScript
{
    private static final Log logger = LogFactory.getLog(AutoSuggestSearchGet.class);

    private static final String TERM = "t";
    private static final String LIMIT = "limit";
    private static final String SUGGESTIONS = "suggestions";

    private SuggesterService suggesterService;

    public void setSuggesterService(SuggesterService suggesterService)
    {
        this.suggesterService = suggesterService;
    }

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {

        List<SearchSuggestionData> list = new ArrayList<>();
        Map<String, Object> model = new HashMap<String, Object>(1);
        model.put(SUGGESTIONS, list);

        if (!suggesterService.isEnabled())
        {
            return model;
        }

        String term = req.getParameter(TERM);
        int limit = getLimit(req.getParameter(LIMIT));

        if (term == null || term.isEmpty())
        {
            return model;
        }

        SuggesterResult result = suggesterService.getSuggestions(new SuggesterParameters(term, limit, false));
        List<Pair<String, Integer>> suggestedTerms = result.getSuggestions();
        for (Pair<String, Integer> pair : suggestedTerms)
        {
            list.add(new SearchSuggestionData(pair.getFirst(), pair.getSecond()));

        }

        if (logger.isDebugEnabled())
        {
            logger.debug("Suggested terms for the [" + term + "] are: " + list);
        }

        return model;
    }

    private int getLimit(String limit)
    {
        if (limit == null)
        {
            return -1;
        }
        try
        {
            return Integer.parseInt(limit);
        }
        catch (NumberFormatException ne)
        {
            return -1;
        }
    }
}
