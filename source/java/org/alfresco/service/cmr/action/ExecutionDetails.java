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
package org.alfresco.service.cmr.action;

import java.util.Date;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Holds all the details available to the 
 *  {@link ActionTrackingService} on a currently
 *  executing Action.
 *  
 * @author Nick Burch
 */
public class ExecutionDetails {
    /* 
     * Transient as all the info is held in the key,
     *  we don't need to also hold a 2nd copy of it 
     */
    private transient ExecutionSummary executionSummary;
    private NodeRef persistedActionRef;
    private String runningOn;
    private Date startedAt;
    private boolean cancelRequested;
 }