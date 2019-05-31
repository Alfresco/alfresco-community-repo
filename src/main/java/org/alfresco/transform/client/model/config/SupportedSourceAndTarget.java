/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2019 Alfresco Software Limited
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
package org.alfresco.transform.client.model.config;

/**
 * Represents a single source and target combination supported by a transformer. File extensions are used to keep the
 * json human readable. Each par also has an optional maximum size for the source content.
 */
public class SupportedSourceAndTarget
{
    private String sourceMediaType;
    private long maxSourceSizeBytes = -1;
    private String targetMediaType;
    private int priority = 50;

    public SupportedSourceAndTarget()
    {
    }

    public SupportedSourceAndTarget(String sourceMediaType, String targetMediaType, long maxSourceSizeBytes)
    {
        this(sourceMediaType, targetMediaType, maxSourceSizeBytes, 50);
    }

    public SupportedSourceAndTarget(String sourceMediaType, String targetMediaType, long maxSourceSizeBytes, int priority)
    {
        setSourceMediaType(sourceMediaType);
        setMaxSourceSizeBytes(maxSourceSizeBytes);
        setTargetMediaType(targetMediaType);
        setPriority(priority);
    }

    public String getSourceMediaType()
    {
        return sourceMediaType;
    }

    public void setSourceMediaType(String sourceMediaType)
    {
        this.sourceMediaType = sourceMediaType;
    }

    public long getMaxSourceSizeBytes()
    {
        return maxSourceSizeBytes;
    }

    public void setMaxSourceSizeBytes(long maxSourceSizeBytes)
    {
        this.maxSourceSizeBytes = maxSourceSizeBytes;
    }

    public String getTargetMediaType()
    {
        return targetMediaType;
    }

    public void setTargetMediaType(String targetMediaType)
    {
        this.targetMediaType = targetMediaType;
    }

    public int getPriority()
    {
        return priority;
    }

    public void setPriority(int priority)
    {
        this.priority = priority;
    }
}
