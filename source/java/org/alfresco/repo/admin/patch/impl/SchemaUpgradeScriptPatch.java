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

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.service.cmr.admin.PatchException;

/**
 * This patch ensures that an upgrade scriptUrl has been executed.  Upgrade scripts
 * should create an entry for the patch with the required ID and execution status
 * so that the code in this class is never called.  If called, an exception message
 * is always generated.
 * 
 * @author Derek Hulley
 */
public class SchemaUpgradeScriptPatch extends AbstractPatch
{
    private static final String MSG_NOT_EXECUTED = "patch.schemaUpgradeScript.err.not_executed";
    
    private String scriptUrl;
    
    public SchemaUpgradeScriptPatch()
    {
    }

    /**
     * @return Returns the URL of the scriptUrl that has to have been run
     */
    public String getScriptUrl()
    {
        return scriptUrl;
    }
    
    /**
     * Set the URL of the upgrade scriptUrl to execute.  This is the full URL of the
     * file, e.g. <b>classpath:alfresco/patch/scripts/upgrade-1.4/${hibernate.dialect.class}/patchAlfrescoSchemaUpdate-1.4-2.sql</b>
     * where the <b>${hibernate.dialect.class}</b> placeholder will be substituted with the Hibernate
     * <code>Dialect</code> as configured for the system.
     * 
     * @param scriptUrl the scriptUrl filename
     */
    public void setScriptUrl(String script)
    {
        this.scriptUrl = script;
    }

    protected void checkProperties()
    {
        super.checkProperties();
        checkPropertyNotNull(scriptUrl, "scriptUrl");
    }

    /**
     * @see #MSG_NOT_EXECUTED
     */
    @Override
    protected String applyInternal() throws Exception
    {
        throw new PatchException(MSG_NOT_EXECUTED, scriptUrl);
    }
}
