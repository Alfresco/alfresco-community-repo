/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.module.org_alfresco_module_rm.patch;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanNameAware;

/**
 * This patch creates a new "Records Management Behavior Scripts" folder and moves existing behavior scripts from the old "Records Management Scripts" folder to the new folder.
 * This is to compensate for any non-behavior RM scripts so that they can live in the old "Records Management Scripts" folder for its intended purpose and be picked up by the
 * execute script rule action.
 * 
 * @author Craig Tan
 * @since 2.1
 */
public class RMv21BehaviorScriptsPatch extends ModulePatchComponent implements BeanNameAware
{
    /** logger */
    private static Log logger = LogFactory.getLog(RMv21BehaviorScriptsPatch.class); 
    
    /** rm config folder root lookup */
    protected static final NodeRef RM_CONFIG = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "rm_config_folder");

    /** old behavior scripts folder root lookup */
    protected static final NodeRef OLD_BEHAVIOR_SCRIPTS_FOLDER = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "rm_scripts");

    /** new behavior scripts folder root lookup */
    protected static NodeRef newBehaviorScriptsFolder = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "rm_behavior_scripts");

    /** Node Service */
    private NodeService nodeService;

    /** File Folder Service */
    private FileFolderService fileFolderService;
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }


    @Override
    protected void executePatch() throws Throwable
    {        
        // check that the rm config root has been correctly bootstrapped
        if (nodeService.exists(RM_CONFIG) == false)
        {
            throw new AlfrescoRuntimeException("Unable to complete the patch because RM config root folder does not exist in the data dictionary.");
        }
    
        // check that the behavior scripts folder exists
        if (nodeService.exists(newBehaviorScriptsFolder) == false)
        {
            if (logger.isDebugEnabled() == true)
            {
                logger.debug(" ... creating RM Behavior Scripts folder");
            }
            
            String newBehaviorScriptsFolderName = "Records Management Behavior Scripts";
            String newBehaviorScriptsNodeUUID = "rm_behavior_scripts";
            String newBehaviorScriptsAssocQName = "records_management_behavior_scripts";

            Map<QName, Serializable> newBehaviorScriptsFolderProps = new HashMap<QName, Serializable>();
            newBehaviorScriptsFolderProps.put(ContentModel.PROP_NODE_UUID, newBehaviorScriptsNodeUUID);
            newBehaviorScriptsFolderProps.put(ContentModel.PROP_NAME, newBehaviorScriptsFolderName);
            newBehaviorScriptsFolderProps.put(ContentModel.PROP_TITLE, newBehaviorScriptsFolderName);
            newBehaviorScriptsFolderProps.put(ContentModel.PROP_DESCRIPTION, "Scripts intended for execution in response to RM events.");

            newBehaviorScriptsFolder = nodeService.createNode(RM_CONFIG, ContentModel.ASSOC_CONTAINS,
                    QName.createQName(NamespaceService.CONTENT_MODEL_PREFIX, newBehaviorScriptsAssocQName),
                    ContentModel.TYPE_FOLDER, newBehaviorScriptsFolderProps).getChildRef();
        }

        // move to the new behavior scripts folder if the old behavior scripts folder exists and contains files
        if (nodeService.exists(OLD_BEHAVIOR_SCRIPTS_FOLDER) == true)
        {

            // run the following code as System
            AuthenticationUtil.runAs(new RunAsWork<Object>()
            {
                public Object doWork()
                {
                    RetryingTransactionCallback<Void> callback = new RetryingTransactionCallback<Void>()
                    {
                        public Void execute() throws Throwable
                        {
                            // Update the description of the old Scripts folder.
                            nodeService.setProperty(OLD_BEHAVIOR_SCRIPTS_FOLDER, ContentModel.PROP_DESCRIPTION, "Scripts specific to RM that can also be executed by RM rules.");

                            // Move files from RM Scripts folder to RM Behavior Scripts folder.
                            List<FileInfo> oldBehaviorScripts = fileFolderService.listFiles(OLD_BEHAVIOR_SCRIPTS_FOLDER);

                            if (oldBehaviorScripts != null && oldBehaviorScripts.isEmpty() != true)
                            {
                                if (logger.isDebugEnabled() == true)
                                {
                                    logger.debug(" ... moving files from RM Scripts folder to RM Behavior Scripts folder");
                                }

                                for (FileInfo script : oldBehaviorScripts)
                                {
                                    fileFolderService.moveFrom(script.getNodeRef(), OLD_BEHAVIOR_SCRIPTS_FOLDER, RMv21BehaviorScriptsPatch.newBehaviorScriptsFolder, script.getName());

                                    if (logger.isDebugEnabled() == true)
                                    {
                                        logger.debug(" ...... moved " + script.getName());
                                    }
                                }
                            }
                            return null;
                        }
                    };

                    retryingTransactionHelper.doInTransaction(callback);
                    return null;
                }
            }, AuthenticationUtil.getSystemUserName());

        }
    }

}
