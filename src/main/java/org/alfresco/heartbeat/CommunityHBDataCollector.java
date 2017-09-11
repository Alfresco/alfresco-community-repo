/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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
package org.alfresco.heartbeat;

import org.alfresco.service.cmr.repository.HBDataCollectorService;
import org.springframework.context.ApplicationContext;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class CommunityHBDataCollector extends HBBaseDataCollector {



    public CommunityHBDataCollector (HBDataCollectorService dataCollectorService)
    {
        super(dataCollectorService);
    }




    @Override
    public List<HBData> collectData()
    {

        String timeStamp = new SimpleDateFormat("YYYY-MM-dd'T'HH:mm:ss.sss'Z'").format(new Date());

        // Collect some data
        HBData data = new HBData("Community_sys_id","Community_collector_id","Community_collector_v", timeStamp);

        return Arrays.asList(data);
    }
}
