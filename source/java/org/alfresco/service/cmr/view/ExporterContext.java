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
