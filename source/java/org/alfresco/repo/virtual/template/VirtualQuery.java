/* 
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see http://www.gnu.org/licenses/.
 */

package org.alfresco.repo.virtual.template;

import java.util.List;
import java.util.Set;

import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.virtual.ActualEnvironment;
import org.alfresco.repo.virtual.VirtualizationException;
import org.alfresco.repo.virtual.ref.Reference;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

/**
 * A virtual nodes query used in virtualization processes.
 * 
 * @author Bogdan Horje
 */
public interface VirtualQuery
{
    /**
     * @param actualEnvironment
     * @param files
     * @param folders
     * @param pattern
     * @param ignoreTypeQNames
     * @param searchTypeQNames
     * @param ignoreAspectQNames
     * @param sortProps
     * @param pagingRequest
     * @return -
     * @throws VirtualizationException
     * @deprecated will be replaced by
     *             {@link #perform(ActualEnvironment, VirtualQueryConstraint,Reference)}
     *             once complex constrains are implemented
     */
    PagingResults<Reference> perform(ActualEnvironment actualEnvironment, boolean files, boolean folders,
                String pattern,  Set<QName> searchTypeQNames,Set<QName> ignoreTypeQNames, Set<QName> ignoreAspectQNames,
                List<Pair<QName, Boolean>> sortProps, PagingRequest pagingRequest, Reference parentReference)
                throws VirtualizationException;

    PagingResults<Reference> perform(ActualEnvironment actualEnvironment, VirtualQueryConstraint constraint,
                PagingRequest pagingRequest, Reference parentReference) throws VirtualizationException;

    String getQueryString();

    String getLanguage();

    String getStoreRef();
}
