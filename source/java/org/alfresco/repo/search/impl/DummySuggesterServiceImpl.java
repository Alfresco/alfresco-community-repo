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

package org.alfresco.repo.search.impl;

import org.alfresco.repo.search.impl.lucene.SolrSuggesterResult;
import org.alfresco.service.cmr.search.SuggesterParameters;
import org.alfresco.service.cmr.search.SuggesterResult;
import org.alfresco.service.cmr.search.SuggesterService;

/**
 * Dummy Suggester
 *
 * @author Jamal Kaabi-Mofrad
 * @since 5.0
 */
public class DummySuggesterServiceImpl implements SuggesterService
{

    @Override
    public boolean isEnabled()
    {
        return false;
    }

    @Override
    public SuggesterResult getSuggestions(SuggesterParameters suggesterParameters)
    {
        return new SolrSuggesterResult();
    }

}
