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
package org.alfresco.cmis;

import java.util.List;

/**
 * Service for manipulating with <b>Change Log Events</b> by <b>Change Log Tokens</b>. Also this service exposes some methods for describing <b>Auditing</b> features
 * 
 * @author Dmitry Velichkevich
 */
public interface CMISChangeLogService
{
    /**
     * @param changeLogToken - {@link String} value that represents some <b>Change Log Token</b>
     * @param maxItems - {@link Integer} value that determines required amount of entries
     * @return {@link CMISChangeLog} instance that describes entry mapped to specified <b>Change Log Token</b>
     * @throws CMISInvalidArgumentException 
     */
    public CMISChangeLog getChangeLogEvents(String changeLogToken, Integer maxItems) throws CMISInvalidArgumentException;

    /**
     * @return {@link String} value that represents <b>Change Log Token</b> which is currently actual
     */
    public String getLastChangeLogToken();
    
    /**
     * Gets the change log token maxItems entries before the given one
     * 
     * @param currentPageToken
     *            a change log token
     * @param maxItems
     *            the number of entries to skip backwards
     * @return the change log token maxItems entries before currentPageToken
     */
    public String getPreviousPageChangeLogToken(String currentPageToken, Integer maxItems);

    /**
     * Assuming that currentPageToken begins a page of maxItems entries, gets the token at the start of the final page.
     * 
     * @param currentPageToken
     *            a change log token
     * @param maxItems
     *            the number of entries per page
     * @return the change log token at the start of the final page
     */
    public String getLastPageChangeLogToken(String currentPageToken, Integer maxItems);

    /**
     * @return {@link CMISCapabilityChanges} <b>enum</b> value that specifies currently configured <b>Auditing</b> feature mode
     */
    public CMISCapabilityChanges getCapability();

    /**
     * @return {@link List}&lt;{@link CMISBaseObjectTypeIds}&gt; collection instance that contains configured value for Changes On Type capability
     * @see CMISBaseObjectTypeIds
     */
    public List<CMISBaseObjectTypeIds> getChangesOnTypeCapability();

    /**
     * @return always <b>true</b>
     */
    public boolean getChangesIncomplete();
}
