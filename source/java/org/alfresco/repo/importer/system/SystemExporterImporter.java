/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.importer.system;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.alfresco.repo.admin.patch.AppliedPatch;
import org.alfresco.repo.domain.patch.AppliedPatchDAO;
import org.alfresco.service.cmr.repository.NodeService;


/**
 * Exporter and Importer of Repository System Information
 * 
 * @author davidc
 */
public class SystemExporterImporter
{
    // dependencies
    private NodeService nodeService;
    private AppliedPatchDAO appliedPatchDAO;
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setAppliedPatchDAO(AppliedPatchDAO appliedPatchDAO)
    {
        this.appliedPatchDAO = appliedPatchDAO;
    }
    
    /**
     * Export Repository System Information
     * 
     * @param exportStream  output stream to export to
     */
    public void exportSystem(OutputStream exportStream)
    {
        SystemInfo systemInfo = new SystemInfo();
        
        // capture applied patches
        List<AppliedPatch> patches = appliedPatchDAO.getAppliedPatches();
        for (AppliedPatch patch : patches)
        {
            PatchInfo patchInfo = new PatchInfo();
            patchInfo.appliedOnDate = patch.getAppliedOnDate();
            patchInfo.appliedToSchema = patch.getAppliedToSchema();
            patchInfo.appliedToServer = patch.getAppliedToServer();
            patchInfo.description = patch.getDescription();
            patchInfo.fixesFromSchema = patch.getFixesFromSchema();
            patchInfo.fixesToSchema = patch.getFixesToSchema();
            patchInfo.id = patch.getId();
            patchInfo.report = patch.getReport();
            patchInfo.succeeded = patch.getSucceeded();
            patchInfo.targetSchema = patch.getTargetSchema();
            patchInfo.wasExecuted = patch.getWasExecuted();
            systemInfo.patches.add(patchInfo);
        }
        
        systemInfo.toXML(exportStream);
    }
    
    
    /**
     * Import Repository System Information
     * 
     * @param importStream  input stream to import from
     */
    public void importSystem(InputStream importStream)
    {
        SystemInfo systemInfo = SystemInfo.createSystemInfo(importStream);
        
        // apply patch info
        for (PatchInfo patchInfo : systemInfo.patches)
        {
            AppliedPatch patch = new AppliedPatch();
            patch.setId(patchInfo.id);
            patch.setAppliedOnDate(patchInfo.appliedOnDate);
            patch.setAppliedToSchema(patchInfo.appliedToSchema);
            patch.setAppliedToServer(patchInfo.appliedToServer);
            patch.setDescription(patchInfo.description);
            patch.setFixesFromSchema(patchInfo.fixesFromSchema);
            patch.setFixesToSchema(patchInfo.fixesToSchema);
            patch.setReport(patchInfo.report);
            patch.setSucceeded(patchInfo.succeeded);
            patch.setTargetSchema(patchInfo.targetSchema);
            patch.setWasExecuted(patchInfo.wasExecuted);
            appliedPatchDAO.createAppliedPatch(patch);
        }
    }
}
