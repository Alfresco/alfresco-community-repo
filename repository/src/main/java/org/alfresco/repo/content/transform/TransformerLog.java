/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.content.transform;

import java.util.Date;
import java.util.Deque;

import org.apache.commons.logging.Log;

import org.alfresco.api.AlfrescoPublicApi;  

/**
 * Implementation of a {@link Log} that logs messages to a structure accessible via
 * {@link TransformerConfigMBean#getTransformationLog(int)}.<p>
 * 
 * @author Alan Davis
 */
@AlfrescoPublicApi
public class TransformerLog extends TransformerLogger<String>
{
    /**
     * {@inheritDoc}<p>
     * Returns 100 as this is currently held in memory.
     */
    @Override
    protected int getUpperMaxEntries()
    {
        return 1000;
    }

    /**
     * Overridden to specify the property name that specifies the maximum number of entries.
     */
    @Override
    protected String getPropertyName()
    {
        return "transformer.log.entries";
    }

    @Override
    protected void addOrModify(Deque<String> entries, Object message)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(TransformerLogger.DATE_FORMAT.format(new Date()));
        sb.append(' ');
        sb.append(message);

        entries.add(sb.toString());
    }
}
