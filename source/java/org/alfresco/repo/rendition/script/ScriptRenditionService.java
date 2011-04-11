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
package org.alfresco.repo.rendition.script;

import java.util.List;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.rendition.RenderingEngineDefinition;
import org.alfresco.service.cmr.rendition.RenditionDefinition;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Script object representing the rendition service.
 * 
 * @author Neil McErlean
 */
public class ScriptRenditionService extends BaseScopableProcessorExtension
{
    private static Log logger = LogFactory.getLog(ScriptRenditionService.class);
    
    /** The Services registry */
    private ServiceRegistry serviceRegistry;
    private RenditionService renditionService;

    /**
     * Set the service registry
     * 
     * @param serviceRegistry the service registry.
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
        this.renditionService = serviceRegistry.getRenditionService();
    }
    
    private RenditionDefinition loadRenditionDefinitionImpl(String shortOrLongFormQName)
    {
        final QName renditionName = createQName(shortOrLongFormQName);
        
        // Rendition Definitions are persisted underneath the Data Dictionary for which Group ALL
        // has Consumer access by default. However, we cannot assume that that access level applies for all deployments. See ALF-7334.
        RenditionDefinition rendDefn = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<RenditionDefinition>()
            {
                @Override
                public RenditionDefinition doWork() throws Exception
                {
                    return renditionService.loadRenditionDefinition(renditionName);
                }
            }, AuthenticationUtil.getSystemUserName());
        return rendDefn;
    }


    /**
     * Creates a new {@link ScriptRenditionDefinition} and sets the rendition name and
     * the rendering engine name to the specified values.
     * 
     * @param renditionName A unique identifier used to specify the created
     *            {@link ScriptRenditionDefinition}.
     * @param renderingEngineName The name of the rendering engine associated
     *            with this {@link ScriptRenditionDefinition}.
     * @return the created {@link ScriptRenditionDefinition}.
     * @see org.alfresco.service.cmr.rendition.RenditionService#createRenditionDefinition(QName, String)
     */
    public ScriptRenditionDefinition createRenditionDefinition(String renditionName, String renderingEngineName)
    {
    	QName renditionQName = createQName(renditionName);
    	
    	if (logger.isDebugEnabled())
    	{
    		StringBuilder msg = new StringBuilder();
    		msg.append("Creating ScriptRenditionDefinition [")
    		   .append(renditionName).append(", ")
    		   .append(renderingEngineName).append("]");
    		logger.debug(msg.toString());
    	}

        RenderingEngineDefinition engineDefinition = renditionService.getRenderingEngineDefinition(renderingEngineName);
        RenditionDefinition rendDef = renditionService.createRenditionDefinition(renditionQName, renderingEngineName);
        
        return new ScriptRenditionDefinition(serviceRegistry, this.getScope(), engineDefinition, rendDef);
    }

    /**
     * This method renders the specified source node using the specified saved
     * rendition definition.
     * @param sourceNode the source node to be rendered.
     * @param renditionDefQName the rendition definition to be used e.g. "cm:doclib" or
     *                          "{http://www.alfresco.org/model/content/1.0}imgpreview"
     * @return the rendition scriptnode.
     * @see org.alfresco.service.cmr.rendition.RenditionService#render(org.alfresco.service.cmr.repository.NodeRef, RenditionDefinition)
     */
    public ScriptNode render(ScriptNode sourceNode, String renditionDefQName)
    {
        if (logger.isDebugEnabled())
        {
            StringBuilder msg = new StringBuilder();
            msg.append("Rendering source node '")
                .append(sourceNode)
                .append("' with renditionDef '").append(renditionDefQName)
                .append("'");
            logger.debug(msg.toString());
        }
        
        RenditionDefinition rendDef = loadRenditionDefinitionImpl(renditionDefQName);
        ChildAssociationRef result = this.renditionService.render(sourceNode.getNodeRef(), rendDef);
        NodeRef renditionNode = result.getChildRef();
        
        if (logger.isDebugEnabled())
        {
            StringBuilder msg = new StringBuilder();
            msg.append("Rendition: ").append(renditionNode);
            logger.debug(msg.toString());
        }
        
        return new ScriptNode(renditionNode, serviceRegistry);
    }
    
    public ScriptNode render(ScriptNode sourceNode, ScriptRenditionDefinition scriptRenditionDef)
    {
        if (logger.isDebugEnabled())
        {
            StringBuilder msg = new StringBuilder();
            msg.append("Rendering source node '")
                .append(sourceNode)
                .append("' with renditionDefQName '").append(scriptRenditionDef)
                .append("'");
            logger.debug(msg.toString());
        }

        ChildAssociationRef chAssRef = this.renditionService.render(sourceNode.getNodeRef(),
                scriptRenditionDef.getRenditionDefinition());
        
        NodeRef renditionNode = chAssRef.getChildRef();
		if (logger.isDebugEnabled())
        {
            StringBuilder msg = new StringBuilder();
            msg.append("Rendition: ").append(renditionNode);
            logger.debug(msg.toString());
        }

        return new ScriptNode(renditionNode, serviceRegistry);
    }


    /**
     * This method gets all the renditions of the specified node.
     * 
     * @param node the source node
     * @return an array of the renditions.
     * @see org.alfresco.service.cmr.rendition.RenditionService#getRenditions(org.alfresco.service.cmr.repository.NodeRef)
     */
    public ScriptNode[] getRenditions(ScriptNode node)
    {
        List<ChildAssociationRef> renditions = this.renditionService.getRenditions(node.getNodeRef());

        ScriptNode[] renditionObjs = new ScriptNode[renditions.size()];
        for (int i = 0; i < renditions.size(); i++)
        {
            renditionObjs[i] = new ScriptNode(renditions.get(i).getChildRef(), serviceRegistry);
        }
        
        return renditionObjs;
    }

    /**
     * This method gets all the renditions of the specified node filtered by
     * MIME-type prefix. Renditions whose MIME-type string startsWith the prefix
     * will be returned.
     * 
     * @param node the source node for the renditions
     * @param mimeTypePrefix a prefix to check against the rendition MIME-types.
     *            This must not be null and must not be an empty String
     * @return an array of the filtered renditions.
     * @see org.alfresco.service.cmr.rendition.RenditionService#getRenditions(org.alfresco.service.cmr.repository.NodeRef)
     */
    public ScriptNode[] getRenditions(ScriptNode node, String mimeTypePrefix)
    {
        List<ChildAssociationRef> renditions = this.renditionService.getRenditions(node.getNodeRef(), mimeTypePrefix);

        ScriptNode[] results = new ScriptNode[renditions.size()];
        for (int i = 0; i < renditions.size(); i++)
        {
            results[i] = new ScriptNode(renditions.get(i).getChildRef(), serviceRegistry);
        }
        
        return results;
    }

    /**
     * This method gets the rendition of the specified node identified by
     * the provided rendition name.
     * 
     * @param node the source node for the renditions
     * @param renditionName the renditionName used to identify a rendition. e.g. cm:doclib or
     *                          "{http://www.alfresco.org/model/content/1.0}imgpreview"
     * @return the parent association for the rendition or <code>null</code> if there is no such rendition.
     * @see org.alfresco.service.cmr.rendition.RenditionService#getRenditionByName(org.alfresco.service.cmr.repository.NodeRef, QName)
     */
    public ScriptNode getRenditionByName(ScriptNode node, String renditionName)
    {
        QName qname = createQName(renditionName);
        ChildAssociationRef result = this.renditionService.getRenditionByName(node.getNodeRef(), qname);
        
        return result == null ? null : new ScriptNode(result.getChildRef(), serviceRegistry);
    }
    
    /**
     * This method takes a string representing a QName and converts it to a QName
     * object.
     * @param qnameString the string can be either a short or long form qname.
     * @return a QName object
     */
    private QName createQName(String qnameString)
    {
        QName result;
        if (qnameString.startsWith(Character.toString(QName.NAMESPACE_BEGIN)))
        {
            // It's a long-form qname string
            result = QName.createQName(qnameString);
        }
        else
        {
            // It's a short-form qname string
            result = QName.createQName(qnameString, serviceRegistry.getNamespaceService());
        }
        return result;
    }

}
