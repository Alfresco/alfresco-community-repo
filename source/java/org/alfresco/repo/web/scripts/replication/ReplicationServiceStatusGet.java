/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.web.scripts.replication;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.replication.ReplicationService;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * @author Neil Mc Erlean
 * @since 3.5
 */
public class ReplicationServiceStatusGet extends DeclarativeWebScript
{
    public static final String ENABLED = "enabled";

    private ReplicationService replicationService;
    
    /**
     * Used to inject the {@link ReplicationService}.
     * 
     * @param replicationService
     */
    public void setReplicationService(ReplicationService replicationService)
    {
        this.replicationService = replicationService;
    }
    
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<String, Object>();
        boolean serviceEnabled = replicationService.isEnabled();
        
        model.put(ENABLED, serviceEnabled);
        
        return model;
    }
}