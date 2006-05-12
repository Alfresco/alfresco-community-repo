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
package org.alfresco.repo.importer.system;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.alfresco.repo.admin.patch.PatchDaoService;
import org.alfresco.repo.domain.AppliedPatch;
import org.alfresco.repo.domain.hibernate.VersionCounterDaoComponentImpl;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;


/**
 * Exporter and Importer of Repository System Information
 * 
 * @author davidc
 */
public class SystemExporterImporter
{
    // dependencies
    private NodeService nodeService;
    private PatchDaoService patchDao;
    private VersionCounterDaoComponentImpl versionCounterDao;
    
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setPatchDao(PatchDaoService patchDaoService)
    {
        this.patchDao = patchDaoService;
    }
    
    public void setVersionDao(VersionCounterDaoComponentImpl versionCounterDao)
    {
        this.versionCounterDao = versionCounterDao;
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
        List<AppliedPatch> patches = patchDao.getAppliedPatches();
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

        // capture version counters
        List<StoreRef> storeRefs = nodeService.getStores();
        for (StoreRef storeRef : storeRefs)
        {
            VersionCounterInfo versionCounterInfo = new VersionCounterInfo();
            int versionCount = versionCounterDao.currentVersionNumber(storeRef);
            versionCounterInfo.storeRef = storeRef.toString();
            versionCounterInfo.count = versionCount;
            systemInfo.versionCounters.add(versionCounterInfo);
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
            AppliedPatch patch = patchDao.newAppliedPatch(patchInfo.id);
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
        }

        // apply version counters
        for (VersionCounterInfo versionCounterInfo : systemInfo.versionCounters)
        {
            StoreRef storeRef = new StoreRef(versionCounterInfo.storeRef);
            versionCounterDao.setVersionNumber(storeRef, versionCounterInfo.count);
        }
    }
    
}
