/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.test.legacy.action;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.action.dm.ExecuteScriptAction;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * RM Action Execute Script Unit test
 *
 * @author Eva Vasques
 */
public class ExecuteScriptActionTest extends BaseRMTestCase
{
    
    @Override
    protected boolean isCollaborationSiteTest()
    {
        return true;
    }
    
    public void testExecuteScript()
    {

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        String fileOriginalName = (String) nodeService.getProperty(dmDocument, ContentModel.PROP_NAME);

        // Valid Script
        NodeRef validScriptRef = addTempScript("valid-rm-script.js",
                "document.properties.name = \"Valid_\" + document.properties.name;\ndocument.save();");

        // Invalid Script
        NodeRef invalidScriptRef = addTempScript("invalid-rm-script.js",
                "document.properties.name = \"Invalid_\" + document.properties.name;\ndocument.save();", dmFolder);

        // Attempt to execute a script not in RM Scripts folder should fail
        doTestInTransaction(new FailureTest("Script outside proper Data Dictionary folder should not be executed",
                IllegalStateException.class)
        {
            public void run() throws Exception
            {
                executeAction(invalidScriptRef, dmDocument);
            }
        }, dmCollaborator);

        // Scripts in correct data dictionary folder should be executed
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws Exception
            {
                executeAction(validScriptRef, dmDocument);
                return null;
            }

            @Override
            public void test(Void result) throws Exception
            {
                // Assert the script was executed and the document was renamed
                String currentName = (String) nodeService.getProperty(dmDocument, ContentModel.PROP_NAME);
                assertEquals(currentName, "Valid_" + fileOriginalName);
            }
        }, dmCollaborator);

        retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Throwable
            {
                // Set the name back to what it was
                nodeService.setProperty(dmDocument, ContentModel.PROP_NAME, fileOriginalName);
                return null;
            }
        });
    }

    private NodeRef addTempScript(final String scriptFileName, final String javaScript, final NodeRef parentRef)
    {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        return retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {

                NodeRef script = nodeService.getChildByName(parentRef, ContentModel.ASSOC_CONTAINS, scriptFileName);

                if (script == null)
                {
                    // Create the script node reference
                    script = nodeService.createNode(parentRef, ContentModel.ASSOC_CONTAINS,
                            QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, scriptFileName), ContentModel.TYPE_CONTENT)
                            .getChildRef();

                    nodeService.setProperty(script, ContentModel.PROP_NAME, scriptFileName);

                    ContentWriter contentWriter = contentService.getWriter(script, ContentModel.PROP_CONTENT, true);
                    contentWriter.setMimetype(MimetypeMap.MIMETYPE_JAVASCRIPT);
                    contentWriter.setEncoding("UTF-8");
                    contentWriter.putContent(javaScript);

                }
                return script;
            }
        });
    }

    private NodeRef addTempScript(final String scriptFileName, final String javaScript)
    {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        return retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {

                // get the company_home
                NodeRef companyHomeRef = repositoryHelper.getCompanyHome();
                // get the Data Dictionary
                NodeRef dataDictionaryRef = nodeService.getChildByName(companyHomeRef, ContentModel.ASSOC_CONTAINS,
                        "Data Dictionary");
                // get the Scripts Folder
                NodeRef rmFolder = nodeService.getChildByName(dataDictionaryRef, ContentModel.ASSOC_CONTAINS,
                        "Records Management");
                NodeRef scriptsRef = nodeService.getChildByName(rmFolder, ContentModel.ASSOC_CONTAINS,
                        "Records Management Scripts");

                return addTempScript(scriptFileName, javaScript, scriptsRef);
            }
        });
    }

    private void executeAction(NodeRef scriptRef, NodeRef nodeRef)
    {
        Action action = actionService.createAction("rmscript");
        action.setParameterValue(ExecuteScriptAction.PARAM_SCRIPTREF, scriptRef);
        actionService.executeAction(action, nodeRef);
    }

}
