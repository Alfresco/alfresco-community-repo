/*-----------------------------------------------------------------------------
*  Copyright 2007 Alfresco Inc.
*  
*  This program is free software; you can redistribute it and/or modify
*  it under the terms of the GNU General Public License as published by
*  the Free Software Foundation; either version 2 of the License, or
*  (at your option) any later version.
*  
*  This program is distributed in the hope that it will be useful, but
*  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
*  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
*  for more details.
*  
*  You should have received a copy of the GNU General Public License along
*  with this program; if not, write to the Free Software Foundation, Inc.,
*  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.  As a special
*  exception to the terms and conditions of version 2.0 of the GPL, you may
*  redistribute this Program in connection with Free/Libre and Open Source
*  Software ("FLOSS") applications as described in Alfresco's FLOSS exception.
*  You should have received a copy of the text describing the FLOSS exception,
*  and it is also available here:   http://www.alfresco.com/legal/licensing
*  
*  
*  Author  Jon Cox  <jcox@alfresco.com>
*  File    SandboxConstants.java
*----------------------------------------------------------------------------*/

package org.alfresco.sandbox;
import org.alfresco.service.namespace.QName;

/**
*  AVM sandbox constants
*/
public class  SandboxConstants
{
    // system property keys for sandbox identification and DNS virtualisation mapping
    public final static String PROP_BACKGROUND_LAYER        = ".background-layer.";
    public final static String PROP_SANDBOXID               = ".sandbox-id.";
    public final static String PROP_DNS                     = ".dns.";
    public final static String PROP_SANDBOX_STORE_PREFIX    = ".sandbox.store.";
    
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
    public final static QName PROP_LINK_VALIDATION_REPORT            = QName.createQName(null, ".link.validation.report");
    public final static QName PROP_LAST_DEPLOYMENT_ID                = QName.createQName(null, ".deployment.id");
}
