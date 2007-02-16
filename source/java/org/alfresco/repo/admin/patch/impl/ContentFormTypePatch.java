/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * http://www.alfresco.com/legal/licensing
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
