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
package org.alfresco.service.cmr.view;

import java.util.Date;

import org.alfresco.service.cmr.repository.NodeRef;


public interface ExporterContext
{
    /**
     * Gets who initiated the export
     *
     * @return String
     */
    public String getExportedBy();
    
    /**
     * Gets date at which export occured
     * 
     * @return Date
     */
    public Date getExportedDate();
    
    /**
     * Gets version number of exporter
     * 
     * @return String
     */
    public String getExporterVersion();
    
    /**
     * Gets active node for export
     * 
     * @return NodeRef 
     */
    public NodeRef getExportOf();
    
    /**
     * Gets parent of exporting node
     * 
     * @return NodeRef 
     */    
    public NodeRef getExportParent();
    
    /**
     * Gets list of nodes for export
     * 
     * @return NodeRef[]
     */
    public NodeRef[] getExportList();
    
    /**
     * Gets list of parents for exporting nodes
     * 
     * @return NodeRef[]
     */
    public NodeRef[] getExportParentList();
    
    /**
     * Return true if there is active node for export
     * 
     * @return boolean
     */
    public boolean canRetrieve();
    
    /**
     * Set next active node from list
     * 
     * @return int
     */
    public int setNextValue();
    
    /**
     * Set first active node 
     */
    public void resetContext();
    
}
