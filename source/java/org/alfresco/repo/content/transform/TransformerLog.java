/*
 * Copyright (C) 2013 Alfresco Software Limited.
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
package org.alfresco.repo.content.transform;

import java.util.Date;
import java.util.Deque;

import org.apache.commons.logging.Log;

/**
 * Implementation of a {@link Log} that logs messages to a structure accessible via
 * {@link TransformerConfigMBean#getTransformationLog(int)}.<p>
 * 
 * @author Alan Davis
 */
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
        return TransformerConfig.LOG_ENTRIES;
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
