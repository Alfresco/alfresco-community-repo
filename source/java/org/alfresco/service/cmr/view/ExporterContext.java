package org.alfresco.service.cmr.view;

import java.util.Date;

import org.alfresco.service.cmr.repository.NodeRef;

public interface ExporterContext
{

    public String getExportedBy();
    
    public Date getExportedDate();
    
    public String getExporterVersion();
    
    public NodeRef getExportOf();
    
    public NodeRef getExportParent();
    
}
