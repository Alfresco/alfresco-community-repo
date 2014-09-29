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

package org.alfresco.repo.search.impl.solr.facet.handler;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * A simple handler to get the full user name from the userID.
 * 
 * @author Jamal Kaabi-Mofrad
 * @since 5.0
 */
public class UserNameDisplayHandler extends AbstractFacetLabelDisplayHandler
{

    public UserNameDisplayHandler(Set<String> supportedFieldFacets)
    {
        ParameterCheck.mandatory("supportedFieldFacets", supportedFieldFacets);

        this.supportedFieldFacets = Collections.unmodifiableSet(new HashSet<>(supportedFieldFacets));
    }

    @Override
    public FacetLabel getDisplayLabel(String value)
    {
        String name = null;

        final NodeRef personRef = serviceRegistry.getPersonService().getPersonOrNull(value);
        if (personRef != null)
        {
            NodeService nodeService = serviceRegistry.getNodeService();
            final String firstName = (String) nodeService.getProperty(personRef, ContentModel.PROP_FIRSTNAME);
            final String lastName = (String) nodeService.getProperty(personRef, ContentModel.PROP_LASTNAME);
            name = (firstName != null ? firstName + " " : "") + (lastName != null ? lastName : "");
        }
        return new FacetLabel(value, name == null ? value : name.trim(), -1);
    }
}
