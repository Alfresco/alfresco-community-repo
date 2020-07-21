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

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.alfresco.repo.content.ContentStore;
import org.alfresco.util.GUID;

/**
 * Default Content URL provider for file stores.
 * Content URL format is <b>store://year/month/day/hour/minute/GUID.bin</b>,
 * but can be configured to include provision for splitting data into 
 * buckets within <b>minute</b> range through bucketsPerMinute property :
 * <b>store://year/month/day/hour/minute/bucket/GUID.bin</b> <br>
 * <ul>
 *   <li> <b>store://</b>: prefix identifying an Alfresco content stores
 *                         regardless of the persistence mechanism. </li>
 *   <li> <b>year</b>: year </li>
 *   <li> <b>month</b>: 1-based month of the year </li>
 *   <li> <b>day</b>: 1-based day of the month </li>
 *   <li> <b>hour</b>: 0-based hour of the day </li>
 *   <li> <b>minute</b>: 0-based minute of the hour </li>
 *   <li> <b>bucket</b>: 0-based bucket depending second of minute </li>
 *   <li> <b>GUID</b>: A unique identifier </li>
 * </ul>
 * <p>
 * @author Andreea Dragoi
 */

class TimeBasedFileContentUrlProvider implements FileContentUrlProvider
{
    protected int bucketsPerMinute = 0;

    public void setBucketsPerMinute(int bucketsPerMinute)
    {
        this.bucketsPerMinute = bucketsPerMinute;
    }
    
    @Override
    public String createNewFileStoreUrl()
    {
        return createNewFileStoreUrl(bucketsPerMinute);
    }
    
    public static String createTimeBasedPath(int bucketsPerMinute){
        Calendar calendar = new GregorianCalendar();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;  // 0-based
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        // create the URL
        StringBuilder sb = new StringBuilder(20);
        sb.append(year).append('/')
          .append(month).append('/')
          .append(day).append('/')
          .append(hour).append('/')
          .append(minute).append('/');
        
        if (bucketsPerMinute != 0)
        {
            long seconds = System.currentTimeMillis() % (60 * 1000);
            int actualBucket = (int) seconds / ((60 * 1000) / bucketsPerMinute);
            sb.append(actualBucket).append('/');
        }
        //done
        return sb.toString();
    }
    
    public static String createNewFileStoreUrl(int minuteBucketCount)
    {
        StringBuilder sb = new StringBuilder(20);
        sb.append(FileContentStore.STORE_PROTOCOL);
        sb.append(ContentStore.PROTOCOL_DELIMITER);
        sb.append(createTimeBasedPath(minuteBucketCount));
        sb.append(GUID.generate()).append(".bin");
        return sb.toString();
    }
}
