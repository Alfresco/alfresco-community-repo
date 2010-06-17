/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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

/*-----------------------------------------------------------------------------
*  Author  Jon Cox  <jcox@alfresco.com>
*  File    SandboxConstants.java
*----------------------------------------------------------------------------*/

package org.alfresco.wcm.sandbox;

import org.alfresco.service.namespace.QName;

/**
*  WCM sandbox constants
*/
public class  SandboxConstants
{
    // system property keys for sandbox identification and DNS virtualisation mapping
    public final static String PROP_BACKGROUND_LAYER        = ".background-layer.";
    public final static String PROP_SANDBOXID               = ".sandbox-id.";
    public final static String PROP_DNS                     = ".dns.";
    public final static String PROP_SANDBOX_STORE_PREFIX    = ".sandbox.store.";
    
    // sandbox type
    public final static QName PROP_SANDBOX_STAGING_MAIN              = QName.createQName(null, ".sandbox.staging.main");
    public final static QName PROP_SANDBOX_STAGING_PREVIEW           = QName.createQName(null, ".sandbox.staging.preview");
    public final static QName PROP_SANDBOX_AUTHOR_MAIN               = QName.createQName(null, ".sandbox.author.main");
    public final static QName PROP_SANDBOX_AUTHOR_PREVIEW            = QName.createQName(null, ".sandbox.author.preview");
    public final static QName PROP_SANDBOX_WORKFLOW_MAIN             = QName.createQName(null, ".sandbox.workflow.main");
    public final static QName PROP_SANDBOX_WORKFLOW_PREVIEW          = QName.createQName(null, ".sandbox.workflow.preview");
    public final static QName PROP_SANDBOX_AUTHOR_WORKFLOW_MAIN      = QName.createQName(null, ".sandbox.author.workflow.main");
    public final static QName PROP_SANDBOX_AUTHOR_WORKFLOW_PREVIEW   = QName.createQName(null, ".sandbox.author.workflow.preview");
    
    public final static QName PROP_WEBSITE_NAME                      = QName.createQName(null, ".website.name");
    public final static QName PROP_AUTHOR_NAME                       = QName.createQName(null, ".author.name");
    public final static QName PROP_WEB_PROJECT_NODE_REF              = QName.createQName(null, ".web_project.noderef");
    public final static QName PROP_WEB_PROJECT_PREVIEW_PROVIDER      = QName.createQName(null, ".web_project.previewprovider");
    
    public final static QName PROP_LAST_DEPLOYMENT_ID                = QName.createQName(null, ".deployment.id");
}
