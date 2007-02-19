/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.repo.action.executer;

import java.io.File;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.importer.ACPImportPackageHandler;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.view.ImporterService;
import org.alfresco.service.cmr.view.Location;
import org.alfresco.util.TempFileProvider;

/**
 * Importer action executor
 * 
 * @author gavinc
 */
public class ImporterActionExecuter extends ActionExecuterAbstractBase
{
    public static final String NAME = "import";
    public static final String PARAM_ENCODING = "encoding";
    public static final String PARAM_DESTINATION_FOLDER = "destination";

    private static final String TEMP_FILE_PREFIX = "alf";
    private static final String TEMP_FILE_SUFFIX = ".acp";
    
    /**
     * The importer service
     */
    private ImporterService importerService;
	
    /**
     * The node service
     */
    private NodeService nodeService;
    
    /**
     * The content service
     */
    private ContentService contentService;
    
    /**
     * Sets the ImporterService to use
     * 
     * @param importerService The ImporterService
     */
	public void setImporterService(ImporterService importerService) 
	{
		this.importerService = importerService;
	}
    
    /**
     * Sets the NodeService to use
     * 
     * @param nodeService The NodeService
     */
    public void setNodeService(NodeService nodeService)
    {
       this.nodeService = nodeService;
    }
    
    /**
     * Sets the ContentService to use
     * 
     * @param contentService The ContentService
     */
    public void setContentService(ContentService contentService)
    {
       this.contentService = contentService;
    }

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuter#execute(org.alfresco.repo.ref.NodeRef, org.alfresco.repo.ref.NodeRef)
     */
    public void executeImpl(Action ruleAction, NodeRef actionedUponNodeRef)
    {
        if (this.nodeService.exists(actionedUponNodeRef) == true)
        {
           // The node being passed in should be an Alfresco content package
           ContentReader reader = this.contentService.getReader(actionedUponNodeRef, ContentModel.PROP_CONTENT);
           if (reader != null)
           {
               if (MimetypeMap.MIMETYPE_ACP.equals(reader.getMimetype()))
               {
                   File zipFile = null;
                   try
                   {
                       // unfortunately a ZIP file can not be read directly from an input stream so we have to create
                       // a temporary file first
                       zipFile = TempFileProvider.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX);
                       reader.getContent(zipFile);
                      
                       ACPImportPackageHandler importHandler = new ACPImportPackageHandler(zipFile, 
                             (String)ruleAction.getParameterValue(PARAM_ENCODING));
                       NodeRef importDest = (NodeRef)ruleAction.getParameterValue(PARAM_DESTINATION_FOLDER);
                      
                       this.importerService.importView(importHandler, new Location(importDest), null, null);
                   }
                   finally
                   {
                      // now the import is done, delete the temporary file
                      if (zipFile != null)
                      {
                         zipFile.delete();
                      }
                   }
               }
           }
        }
    }

	/**
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
	 */
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) 
	{
        paramList.add(new ParameterDefinitionImpl(PARAM_DESTINATION_FOLDER, DataTypeDefinition.NODE_REF, 
              true, getParamDisplayLabel(PARAM_DESTINATION_FOLDER)));
        paramList.add(new ParameterDefinitionImpl(PARAM_ENCODING, DataTypeDefinition.TEXT, 
              true, getParamDisplayLabel(PARAM_ENCODING)));
	}

}
