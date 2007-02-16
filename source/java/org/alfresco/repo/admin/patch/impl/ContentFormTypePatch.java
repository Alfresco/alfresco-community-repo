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
package org.alfresco.repo.admin.patch.impl;

import java.util.ArrayList;
import java.util.Collection;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.service.cmr.admin.PatchException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;

/**
 * Patch to update the type of all WCM content form folders to 'wca:formfolder'.
 * 
 * @author Kevin Roast
 */
public class ContentFormTypePatch extends AbstractPatch
{
    private final static String MSG_RESULT = "patch.contentFormFolderType.result";
    
    private ImporterBootstrap importerBootstrap;
    
    public void setImporterBootstrap(ImporterBootstrap importerBootstrap)
    {
        this.importerBootstrap = importerBootstrap;
    }

    /**
     * Ensure that required common properties have been set
     */
    protected void checkCommonProperties() throws Exception
    {
        if (searchService == null)
        {
            throw new PatchException("'searchService' property has not been set");
        }
        if (nodeService == null)
        {
            throw new PatchException("'nodeService' property has not been set");
        }
        if (importerBootstrap == null)
        {
            throw new PatchException("'importerBootstrap' property has not been set");
        }
    }
    
    /**
     * @see org.alfresco.repo.admin.patch.AbstractPatch#applyInternal()
     */
    @Override
    protected String applyInternal() throws Exception
    {
        checkCommonProperties();
        
        int count = 0;
        for (NodeRef formRef : getForms())
        {
            // update folder type to 'wcm:formfolder'
            this.nodeService.setType(formRef, WCMAppModel.TYPE_FORMFOLDER);
            count++;
        }
        
        return I18NUtil.getMessage(MSG_RESULT, new Object[] {Integer.toString(count)});
    }
    
    /** 
     * @return all existing web form folders - marked with the 'wcm:form' aspect.
     */
    private Collection<NodeRef> getForms()
    {
        SearchParameters sp = new SearchParameters();
        sp.addStore(this.importerBootstrap.getStoreRef());
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("ASPECT:\"" + WCMAppModel.ASPECT_FORM + "\"");
        ResultSet rs = this.searchService.query(sp);
        Collection<NodeRef> result = new ArrayList<NodeRef>(rs.length());
        for (ResultSetRow row : rs)
        {
            result.add(row.getNodeRef());
        }
        
        return result;
    }
}
