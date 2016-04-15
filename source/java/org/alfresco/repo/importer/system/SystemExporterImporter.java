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
