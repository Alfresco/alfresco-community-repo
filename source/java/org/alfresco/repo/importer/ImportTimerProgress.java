/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.importer;

import java.io.Serializable;
import java.util.Date;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.view.ImporterProgress;
import org.alfresco.service.namespace.QName;


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
        System.out.println("Import started at " + start + " (" + start.getTime() + ")");
    }

    /*
     *  (non-Javadoc)
     * @see org.alfresco.service.cmr.view.ImporterProgress#completed()
     */
    public void completed()
    {
        Date end = new Date();
        System.out.println("Import completed at " + end + " (" + end.getTime() + ")");
        dumpStats(end);
    }

    /*
     *  (non-Javadoc)
     * @see org.alfresco.service.cmr.view.ImporterProgress#error(java.lang.Throwable)
     */
    public void error(Throwable e)
    {
        Date end = new Date();
        System.out.println("Error occured at " + end + " (" + end.getTime() + ")");
        System.out.println("Exception: " + e.toString());
        dumpStats(end);
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
     * @param end
     */
    private void dumpStats(Date end)
    {
        System.out.println("Import duration: " + (end.getTime() - start.getTime()) + " ms (Note: excluding commit time)");
        System.out.println(" Nodes created: " + nodeCreateCount);
        System.out.println(" Nodes linked: " + nodeLinkedCount);
        System.out.println(" Aspects Added: " + aspectAdded);
        System.out.println(" Properties set: " + propCount);
        System.out.println(" Content set: " + contentCount);
        System.out.println(" Permissions set: " + permissionCount);
    }
    
}
