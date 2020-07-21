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

package org.alfresco.repo.content.filestore;

import java.util.Random;

import org.alfresco.repo.content.ContentStore;
import org.alfresco.util.GUID;

/**
 * Content URL provider for file stores which allows routing content from a store to a selection of filesystem volumes.
 * Content is randomly distributed on configured volumes.
 * Content URL format is <b>store://volume/year/month/day/hour/minute/GUID.bin</b>,
 * As {@link TimeBasedFileContentUrlProvider TimeBasedFileContentUrlProvider} can be configured to include provision for 
 * splitting data into buckets within <b>minute</b> range
 * @author Andreea Dragoi
 */
class VolumeAwareContentUrlProvider extends TimeBasedFileContentUrlProvider
{
    private String[] volumes;
    private Random random = new Random();
   
    /**
     * @param volumeNames(name of volumes separated by comma)
     */
    public VolumeAwareContentUrlProvider(String volumeNames)
    {
        if (volumeNames == null || volumeNames.isEmpty())
        {
            throw new IllegalArgumentException("Invalid volumeNames argument");
        }
        this.volumes = volumeNames.split(",");
    }
    
    @Override
    public String createNewFileStoreUrl()
    {
        StringBuilder sb = new StringBuilder(20);
        sb.append(FileContentStore.STORE_PROTOCOL)
                .append(ContentStore.PROTOCOL_DELIMITER)
                .append(chooseVolume()).append("/")
                .append(TimeBasedFileContentUrlProvider.createTimeBasedPath(bucketsPerMinute))
                .append(GUID.generate()).append(".bin");
        String newContentUrl = sb.toString();
        return newContentUrl;
    }
    
    private String chooseVolume()
    {
        int volumesNum = volumes.length;
        return volumes[random.nextInt(volumesNum)];
    }

}
