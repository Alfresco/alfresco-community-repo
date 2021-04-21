/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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
    private String problemsPatternFileUrl;
    
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
    
    public String getProblemPatternsFileUrl()
    {
        return problemsPatternFileUrl;
    }

    /**
     * Set the URL of the upgrade scriptUrl to execute.  This is the full URL of the
     * file, e.g. <b>classpath:alfresco/patch/scripts/upgrade-1.4/${hibernate.dialect.class}/patchAlfrescoSchemaUpdate-1.4-2.sql</b>
     * where the <b>${hibernate.dialect.class}</b> placeholder will be substituted with the Hibernate
     * <code>Dialect</code> as configured for the system.
     * 
     * @param script the script
     */
    public void setScriptUrl(String script)
    {
        this.scriptUrl = script;
    }

    /**
     * Set the URL of the problems pattern file to accompany the upgrade script.  This is the full URL of the
     * file, e.g. <b>classpath:alfresco/patch/scripts/upgrade-1.4/${hibernate.dialect.class}/patchAlfrescoSchemaUpdate-1.4-2-problems.txt</b>
     * where the <b>${hibernate.dialect.class}</b> placeholder will be substituted with the Hibernate
     * <code>Dialect</code> as configured for the system.
     *
     * @param problemsFile the problems file
     */
    public void setProblemsPatternFileUrl(String problemsFile)
    {
        this.problemsPatternFileUrl = problemsFile;
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
