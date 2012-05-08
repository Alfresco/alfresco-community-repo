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
package org.alfresco.repo.transfer.script;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.jscript.ValueConverter;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.transfer.TransferDefinition;
import org.alfresco.service.cmr.transfer.TransferService;
import org.alfresco.service.cmr.transfer.TransferTarget;
import org.alfresco.service.namespace.QName;

/**
 * Java Script Transfer Service.   Adapts the Java Transfer Service to
 * Java Script.
 *
 * @author Mark Rogers
 */
public class ScriptTransferService extends BaseScopableProcessorExtension
{
    private TransferService transferService;
    
    private ServiceRegistry serviceRegistry;
    
    ValueConverter valueConverter = new ValueConverter();
    
    // Which aspects to exclude
    private List<QName> excludedAspects = new ArrayList<QName>();

    /**
     * @param transferService
     */
    public void setTransferService(TransferService transferService)
    {
        this.transferService = transferService;
    }

    /**
     * 
     * @return
     */
    public TransferService getTransferService()
    {
        return transferService;
    }
    
    /**
     * create a transfer target
     */
    
    /**
     * Get the transfer targets for the specified group
     */
    public ScriptTransferTarget[] getTransferTargetsByGroup(String groupName)
    {
        
        Set<TransferTarget> values = transferService.getTransferTargets(groupName);
        
        ScriptTransferTarget[] retVal = new ScriptTransferTarget[values.size()];
        
        int i = 0;
        for(TransferTarget value : values)
        {
            retVal[i++] = new ScriptTransferTarget(value);
        }
        return retVal;
    }
    
    public ScriptTransferTarget[] getAllTransferTargets()
    {
        Set<TransferTarget> values = transferService.getTransferTargets();
        
        ScriptTransferTarget[] retVal = new ScriptTransferTarget[values.size()];
        
        int i = 0;
        for(TransferTarget value : values)
        {
            retVal[i++] = new ScriptTransferTarget(value);
        }
        
        return retVal;
    }
    
    public ScriptTransferTarget getTransferTarget(String name)
    {
        TransferTarget value = transferService.getTransferTarget(name);
        
        if(value != null)
        {
            return new ScriptTransferTarget(value);
        }
        return null;
    }
    
    /**
     * Transfer a set of nodes, with no callback.
     * <p>
     * Nodes are to be locked read only on target.
     * 
     * @param targetName the name of the target to transfer to
     * @param nodes the nodes to transfer - Java Script Array of either ScriptNodes, NodeRef or String 
     * @return node ref of transfer report.  
     */
    @SuppressWarnings("unchecked")
    public ScriptNode transferReadOnly(String targetName, Object nodesToTransfer)
    {
        Object nodesObject = valueConverter.convertValueForJava(nodesToTransfer);
        
        TransferDefinition toTransfer = new TransferDefinition();
        toTransfer.setReadOnly(true);
        toTransfer.setExcludedAspects(excludedAspects);
        
        Collection<NodeRef> nodeCollection = new ArrayList<NodeRef>();
        
        if(nodesObject instanceof Collection)
        {
            for(Object value : (Collection)nodesObject)
            {
                if(value instanceof NodeRef)
                {
                    nodeCollection.add((NodeRef)value);
                }
                else if (value instanceof String)
                {
                    nodeCollection.add(new NodeRef((String)value));
                }
                else
                {        
                    throw new IllegalArgumentException("transfer: unknown type in collection: " + value.getClass().getName());
                } 
            }
           
        }
        else if(nodesObject instanceof NodeRef)
        {
            nodeCollection.add((NodeRef)nodesObject);
        }
        else if (nodesObject instanceof String)
        {
            nodeCollection.add(new NodeRef((String)nodesObject));
        }
        else
        {   
            throw new IllegalArgumentException("transfer: unexpected type for nodes :" + nodesObject.getClass().getName());
        }
        
        toTransfer.setNodes(nodeCollection);
        NodeRef reportNode = transferService.transfer(targetName, toTransfer);
        
        return new ScriptNode(reportNode, serviceRegistry, getScope());
    }
    /**
     * Transfer a set of nodes, with no callback
     * <p>
     * Nodes are not locked on the target.
     * 
     * @param targetName the name of the target to transfer to
     * @param nodes the nodes to transfer - Java Script Array of either ScriptNodes, NodeRef or String 
     * @return node ref of transfer report.  
     */
    @SuppressWarnings("unchecked")
    public ScriptNode transfer(String targetName, Object nodesToTransfer)
    {
        Object nodesObject = valueConverter.convertValueForJava(nodesToTransfer);
        
        TransferDefinition toTransfer = new TransferDefinition();
        toTransfer.setExcludedAspects(excludedAspects);
        Collection<NodeRef> nodeCollection = new ArrayList<NodeRef>();
        
        if(nodesObject instanceof Collection)
        {
            for(Object value : (Collection)nodesObject)
            {
                if(value instanceof NodeRef)
                {
                    nodeCollection.add((NodeRef)value);
                }
                else if (value instanceof String)
                {
                    nodeCollection.add(new NodeRef((String)value));
                }
                else
                {        
                    throw new IllegalArgumentException("transfer: unknown type in collection: " + value.getClass().getName());
                } 
            }
           
        }
        else if(nodesObject instanceof NodeRef)
        {
            nodeCollection.add((NodeRef)nodesObject);
        }
        else if (nodesObject instanceof String)
        {
            nodeCollection.add(new NodeRef((String)nodesObject));
        }
        else
        {   
            throw new IllegalArgumentException("transfer: unexpected type for nodes :" + nodesObject.getClass().getName());
        }
        
        toTransfer.setNodes(nodeCollection);
        NodeRef reportNode = transferService.transfer(targetName, toTransfer);
        
        return new ScriptNode(reportNode, serviceRegistry, getScope());
    }
    
    /**
     * Remove a set of nodes, with no callback
     * <p>
     * Nodes are not locked on the target.
     * 
     * @param targetName the name of the target to transfer to
     * @param nodes the nodes to transfer - Java Script Array of either ScriptNodes, NodeRef or String 
     * @return node ref of transfer report.  
     */
    @SuppressWarnings("unchecked")
    public ScriptNode remove(String targetName, Object nodesToRemove)
    {
        Object nodesObject = valueConverter.convertValueForJava(nodesToRemove);
        
        TransferDefinition toTransfer = new TransferDefinition();
        toTransfer.setExcludedAspects(excludedAspects);
        Collection<NodeRef> nodeCollection = new ArrayList<NodeRef>();
        
        if(nodesObject instanceof Collection)
        {
            for(Object value : (Collection)nodesObject)
            {
                if(value instanceof NodeRef)
                {
                    nodeCollection.add((NodeRef)value);
                }
                else if (value instanceof String)
                {
                    nodeCollection.add(new NodeRef((String)value));
                }
                else
                {        
                    throw new IllegalArgumentException("transfer: unknown type in collection: " + value.getClass().getName());
                } 
            }
           
        }
        else if(nodesObject instanceof NodeRef)
        {
            nodeCollection.add((NodeRef)nodesObject);
        }
        else if (nodesObject instanceof String)
        {
            nodeCollection.add(new NodeRef((String)nodesObject));
        }
        else
        {   
            throw new IllegalArgumentException("transfer: unexpected type for nodes :" + nodesObject.getClass().getName());
        }
        
        toTransfer.setNodesToRemove(nodeCollection);
        NodeRef reportNode = transferService.transfer(targetName, toTransfer);
        
        return new ScriptNode(reportNode, serviceRegistry, getScope());
    }


    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }

    public ServiceRegistry getServiceRegistry()
    {
        return serviceRegistry;
    }

    public void setExcludedAspects(String[] excludedAspects)
    {
        for (String aspect : excludedAspects)
        {
            this.excludedAspects.add(QName.createQName(aspect));
        }
    }
}
