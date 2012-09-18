/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.repo.web.scripts.quickshare;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
import org.alfresco.model.QuickShareModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.repo.web.scripts.WebScriptUtil;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;


/**
 * QuickShare/PublicView
 * 
 * POST web script to "Share" access to some content (ie. enable unauthenticated access to this node)
 * 
 * Note: authenticated web script
 * 
 * @author janv
 */
public class ShareContentPost extends AbstractQuickShareContent
{
    private static final Log logger = LogFactory.getLog(ShareContentPost.class);
    
    /* package */ static final String ATTR_KEY_SHAREDIDS_ROOT = ".sharedIds";
    
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        if (! isEnabled())
        {
            throw new WebScriptException(HttpServletResponse.SC_FORBIDDEN, "QuickShare is disabled system-wide");
        }
        
        // create map of params (template vars)
        Map<String, String> params = req.getServiceMatch().getTemplateVars();
        final NodeRef nodeRef = WebScriptUtil.getNodeRef(params);
        if (nodeRef == null)
        {
            String msg = "A valid NodeRef must be specified!";
            throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, msg);
        }
        
        try
        {
        	QName typeQName = nodeService.getType(nodeRef);
        	if (! typeQName.equals(ContentModel.TYPE_CONTENT))
        	{
        		throw new InvalidNodeRefException(nodeRef);
        	}
        	
        	final String sharedId;
        	
        	if (! nodeService.getAspects(nodeRef).contains(QuickShareModel.ASPECT_QSHARE))
        	{
        	    UUID uuid = UUIDGenerator.getInstance().generateRandomBasedUUID();
        	    sharedId = Base64.encodeBase64URLSafeString(uuid.toByteArray()); // => 22 chars (eg. q3bEKPeDQvmJYgt4hJxOjw)
        	    
            	Map<QName,Serializable> props = new HashMap<QName,Serializable>(2);
            	props.put(QuickShareModel.PROP_QSHARE_SHAREDID, sharedId);
            	props.put(QuickShareModel.PROP_QSHARE_SHAREDBY, AuthenticationUtil.getRunAsUser());
            	
            	nodeService.addAspect(nodeRef, QuickShareModel.ASPECT_QSHARE, props);
            	
            	final NodeRef tenantNodeRef = tenantService.getName(nodeRef);
            	
            	TenantUtil.runAsDefaultTenant(new TenantRunAsWork<Void>()
    	        {
            	    public Void doWork() throws Exception
    	            {
            	        attributeService.setAttribute(tenantNodeRef, ATTR_KEY_SHAREDIDS_ROOT, sharedId);
    	                return null;
    	            }
    	        });
            	
            	if (logger.isInfoEnabled())
                {
                    logger.info("QuickShare - shared content: "+sharedId+" ["+nodeRef+"]");
                }
            }
        	else
        	{
        	    sharedId = (String)nodeService.getProperty(nodeRef, QuickShareModel.PROP_QSHARE_SHAREDID);
        	    
        	    if (logger.isDebugEnabled())
                {
                    logger.debug("QuickShare - content already shared: "+sharedId+" ["+nodeRef+"]");
                }
        	}
        	
            Map<String, Object> model = new HashMap<String, Object>(1);
            model.put("sharedId", sharedId);
            return model;
        }
        catch (InvalidNodeRefException inre)
        {
        	throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Unable to find node: " + nodeRef);
        }
    }
}