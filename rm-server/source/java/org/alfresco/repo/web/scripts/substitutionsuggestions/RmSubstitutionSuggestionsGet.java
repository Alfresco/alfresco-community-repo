/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.repo.web.scripts.substitutionsuggestions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.action.parameter.ParameterProcessorComponent;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Implementation for Java backed webscript to get substitution suggestions
 * given a text fragment (e.g. date.month for 'mon').
 *
 * @author Mark Hibbins
 * @since 2.2
 */
public class RmSubstitutionSuggestionsGet extends DeclarativeWebScript
{
    private final static String FRAGMENT_PARAMETER = "fragment";
    private final static String PATH_PARAMETER = "path";

    private final static String SUBSTITUTIONS_MODEL_KEY = "substitutions";

    private ParameterProcessorComponent parameterProcessorComponent;

    public void setParameterProcessorComponent(ParameterProcessorComponent parameterProcessorComponent)
    {
        this.parameterProcessorComponent = parameterProcessorComponent;
    }

    /**
     * @see org.springframework.extensions.webscripts.DeclarativeWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest,
     *      org.springframework.extensions.webscripts.Status,
     *      org.springframework.extensions.webscripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        String fragment = req.getParameter(FRAGMENT_PARAMETER);
        String path = req.getParameter(PATH_PARAMETER);

        List<String> substitutionSuggestions = new ArrayList<String>();

        substitutionSuggestions.addAll(getSubPathSuggestions(path, fragment));
        substitutionSuggestions.addAll(this.parameterProcessorComponent.getSubstitutionSuggestions(fragment));

        Map<String, Object> model = new HashMap<String, Object>();
        model.put(SUBSTITUTIONS_MODEL_KEY, substitutionSuggestions);

        return model;
    }

    private List<String> getSubPathSuggestions(final String path, final String fragment) {
        List<String> pathSuggestions = new ArrayList<String>();
        if(path != null)
        {
            // TODO - populate path suggestions
        }
        return pathSuggestions;
    }
}