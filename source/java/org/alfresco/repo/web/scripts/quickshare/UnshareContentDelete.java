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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
import org.alfresco.model.QuickShareModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;


/**
 * QuickShare/PublicView
 * 
 * DELETE web script to "Unshare" access to some content (ie. disable unauthenticated access to this node)
 * 
 * Note: authenticated web script
 * 
 * @author janv
 */
public class UnshareContentDelete extends AbstractQuickShareContent implements NodeServicePolicies.BeforeDeleteNodePolicy
{
    private static final Log logger = LogFactory.getLog(ShareContentPost.class);
    
    protected PolicyComponent policyComponent;
    
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }
    
    /**
     * The initialise method
     */
    public void init()
    {
        // Register interest in the beforeDeleteNode policy - note: currently for content only !!
        policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "beforeDeleteNode"),
                ContentModel.TYPE_CONTENT,
                new JavaBehaviour(this, "beforeDeleteNode"));
    }
    
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        if (! isEnabled())
        {
            throw new WebScriptException(HttpServletResponse.SC_FORBIDDEN, "QuickShare is disabled system-wide");
        }
        
        // create map of params (template vars)
        Map<String, String> params = req.getServiceMatch().getTemplateVars();
        final String sharedId = params.get("shared_id");
        if (sharedId == null)
        {
            throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "A valid sharedId must be specified !");
        }
        
        try
        {
            Pair<String, NodeRef> pair = getTenantNodeRefFromSharedId(attributeService, tenantService, sharedId);
            final String tenantDomain = pair.getFirst();
            final NodeRef nodeRef = pair.getSecond();
            
            TenantUtil.runAsSystemTenant(new TenantRunAsWork<Void>()
            {
                public Void doWork() throws Exception
                {
                    QName typeQName = nodeService.getType(nodeRef);
                    if (! typeQName.equals(ContentModel.TYPE_CONTENT))
                    {
                        throw new InvalidNodeRefException(nodeRef);
                    }
                    
                    String nodeSharedId = (String)nodeService.getProperty(nodeRef, QuickShareModel.PROP_QSHARE_SHAREDID);
                    
                    if (! EqualsHelper.nullSafeEquals(nodeSharedId, sharedId))
                    {
                        logger.warn("SharedId mismatch: expected="+sharedId+",actual="+nodeSharedId);
                    }
                    
                    nodeService.removeAspect(nodeRef, QuickShareModel.ASPECT_QSHARE);
                    
                    return null;
                }
            }, tenantDomain);
            
            removeSharedId(sharedId);
        	
        	if (logger.isInfoEnabled())
            {
                logger.info("QuickShare - unshared content: "+sharedId+" ["+nodeRef+"]");
            }
        	
            Map<String, Object> model = new HashMap<String, Object>(1);
            model.put("success", Boolean.TRUE);
            return model;
        }
        catch (InvalidNodeRefException inre)
        {
            logger.error("Unable to find: "+sharedId+" ["+inre.getNodeRef()+"]");
        	throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Unable to find: "+sharedId);
        }
    }
    
    // behaviour - currently registered for content only !!
    // note: will remove "share" even if node is only being archived (ie. moved to trash) => a subsequent restore will *not* restore the "share"
    public void beforeDeleteNode(final NodeRef beforeDeleteNodeRef)
    {
        AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
        {
            public Void doWork() throws Exception
            {
                String sharedId = (String)nodeService.getProperty(beforeDeleteNodeRef, QuickShareModel.PROP_QSHARE_SHAREDID);
                if (sharedId != null)
                {
                    Pair<String, NodeRef> pair = getTenantNodeRefFromSharedId(attributeService, tenantService, sharedId);
                    
                    @SuppressWarnings("unused")
                    final String tenantDomain = pair.getFirst();
                    final NodeRef nodeRef = pair.getSecond();
                    
                    // note: deleted nodeRef might not match, eg. for upload new version -> checkin -> delete working copy
                    if (nodeRef.equals(beforeDeleteNodeRef))
                    {
                        removeSharedId(sharedId);
                    }
                }
                return null;
            }
        });
    }

    private void removeSharedId(final String sharedId)
    {
        TenantUtil.runAsDefaultTenant(new TenantRunAsWork<Void>()
        {
            public Void doWork() throws Exception
            {
                attributeService.removeAttribute(ShareContentPost.ATTR_KEY_SHAREDIDS_ROOT, sharedId);
                return null;
            }
        });
    }
}