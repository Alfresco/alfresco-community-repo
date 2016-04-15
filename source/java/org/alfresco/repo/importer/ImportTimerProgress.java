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
package org.alfresco.repo.importer;

import java.io.Serializable;
import java.util.Date;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.view.ImporterProgress;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Import Progress that provides import metrics.
 * 
 * @author davidc
 */
public class ImportTimerProgress implements ImporterProgress
{
    private Date start = null;
    private long nodeCreateCount = 0;
    private long propCount = 0;
    private long contentCount = 0;
    private long nodeLinkedCount = 0;
    private long aspectAdded = 0;
    private long permissionCount = 0;
    
    private Log logger = LogFactory.getLog(ImportTimerProgress.class);;
    
    /**
     * Construct
     */
    public ImportTimerProgress()
    {
    }

    /**
     * Construct
     * 
     * @param logger Log
     */
    public ImportTimerProgress(Log logger)
    {
        this.logger = logger;
    }
    
    /*
     *  (non-Javadoc)
     * @see org.alfresco.service.cmr.view.ImporterProgress#started()
     */
    public void started()
    {
        start = new Date();
        nodeCreateCount = 0;
        propCount = 0;
        contentCount = 0;
        nodeLinkedCount = 0;
        aspectAdded = 0;
        permissionCount = 0;
        
        if (logger.isDebugEnabled())
            logger.debug("Import started at " + start + " (" + start.getTime() + ")");
    }

    /*
     *  (non-Javadoc)
     * @see org.alfresco.service.cmr.view.ImporterProgress#completed()
     */
    public void completed()
    {
        if (logger.isDebugEnabled())
        {
            Date end = new Date();
            logger.debug("Import completed at " + end + " (" + end.getTime() + ")");
            dumpStats(end);
        }
    }

    /*
     *  (non-Javadoc)
     * @see org.alfresco.service.cmr.view.ImporterProgress#error(java.lang.Throwable)
     */
    public void error(Throwable e)
    {
        if (logger.isDebugEnabled())
        {
            Date end = new Date();
            logger.debug("Import completed at " + end + " (" + end.getTime() + ")");
            logger.debug("Error occured at " + end + " (" + end.getTime() + ")");
            logger.debug("Exception: " + e.toString());
            dumpStats(end);
        }
    }

    /*
     *  (non-Javadoc)
     * @see org.alfresco.service.cmr.view.ImporterProgress#nodeCreated(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, org.alfresco.service.namespace.QName)
     */
    public void nodeCreated(NodeRef nodeRef, NodeRef parentRef, QName assocName, QName childName)
    {
        nodeCreateCount++;
    }

    /*
     *  (non-Javadoc)
     * @see org.alfresco.service.cmr.view.ImporterProgress#nodeLinked(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, org.alfresco.service.namespace.QName)
     */
    public void nodeLinked(NodeRef nodeRef, NodeRef parentRef, QName assocName, QName childName)
    {
        nodeLinkedCount++;
    }

    /*
     *  (non-Javadoc)
     * @see org.alfresco.service.cmr.view.ImporterProgress#contentCreated(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    public void contentCreated(NodeRef nodeRef, String sourceUrl)
    {
        contentCount++;
    }

    /*
     *  (non-Javadoc)
     * @see org.alfresco.service.cmr.view.ImporterProgress#propertySet(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName, java.io.Serializable)
     */
    public void propertySet(NodeRef nodeRef, QName property, Serializable value)
    {
        propCount++;
    }

    /*
     *  (non-Javadoc)
     * @see org.alfresco.service.cmr.view.ImporterProgress#permissionSet(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.security.AccessPermission)
     */
    public void permissionSet(NodeRef nodeRef, AccessPermission permission)
    {
        permissionCount++;
    }

    /*
     *  (non-Javadoc)
     * @see org.alfresco.service.cmr.view.ImporterProgress#aspectAdded(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    public void aspectAdded(NodeRef nodeRef, QName aspect)
    {
        aspectAdded++;
    }

    /**
     * Dump statistics 
     * 
     * @param end Date
     */
    private void dumpStats(Date end)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Import duration: " + (end.getTime() - start.getTime()) + " ms (Note: excluding commit time)");
            logger.debug(" Nodes created: " + nodeCreateCount);
            logger.debug(" Nodes linked: " + nodeLinkedCount);
            logger.debug(" Aspects Added: " + aspectAdded);
            logger.debug(" Properties set: " + propCount);
            logger.debug(" Content set: " + contentCount);
            logger.debug(" Permissions set: " + permissionCount);
        }
    }
    
}
