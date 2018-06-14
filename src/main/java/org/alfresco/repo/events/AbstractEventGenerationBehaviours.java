/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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
package org.alfresco.repo.events;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourDefinition;
import org.alfresco.repo.policy.ClassBehaviourBinding;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author steveglover
 *
 */
public abstract class AbstractEventGenerationBehaviours
{
    protected static Log logger = LogFactory.getLog(AbstractEventGenerationBehaviours.class);

    protected Set<String> includeEventTypes;
    protected PolicyComponent policyComponent;

    protected List<BehaviourDefinition<ClassBehaviourBinding>> behaviours = new LinkedList<>();

    protected void addBehaviour(BehaviourDefinition<ClassBehaviourBinding> binding)
    {
        behaviours.add(binding);

        logger.debug("Added policy binding " + binding);
    }

    protected void removeBehaviour(BehaviourDefinition<ClassBehaviourBinding> binding)
    {
        removeBehaviourImpl(binding);

        behaviours.remove(binding);
    }

    protected void removeBehaviourImpl(BehaviourDefinition<ClassBehaviourBinding> binding)
    {
        this.policyComponent.removeClassDefinition(binding);

        logger.debug("Removed policy binding " + binding);
    }

    public void cleanUp()
    {
        for(BehaviourDefinition<ClassBehaviourBinding> binding : behaviours)
        {
            removeBehaviourImpl(binding);
        }
    }

    public void setIncludeEventTypes(String includeEventTypesStr)
    {
        StringTokenizer st = new StringTokenizer(includeEventTypesStr, ",");
        this.includeEventTypes = new HashSet<String>();
        while(st.hasMoreTokens())
        {
            String eventType = st.nextToken().trim();
            this.includeEventTypes.add(eventType);
        }
    }

    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    protected boolean includeEventType(String eventType)
    {
        return includeEventTypes.contains(eventType);
    }


    /**
     * Bind a class policy to a JavaBehaviour if a specific event type is enabled
     * 
     * @param policyName the policy to implement or in other words the one we bind to the JavaBehaviour
     * @param eventTypeToCheck implement the policy only if the event is supported
     */
    protected void bindClassPolicy(QName policyName, String eventTypeToCheck)
    {
        bindClassPolicy(policyName, ContentModel.TYPE_BASE, eventTypeToCheck);
    }
    
    /**
     * Bind a class policy to a JavaBehaviour if a specific event type is enabled
     * 
     * @param policyName the policy to implement or in other words the one we bind to the JavaBehaviour
     * @param className the class to bind to
     * @param eventTypeToCheck implement the policy only if the event is supported
     */
    protected void bindClassPolicy(QName policyName, QName className, String eventTypeToCheck)
    {
        if (eventTypeToCheck != null && !includeEventType(eventTypeToCheck))
        {
            return;
        }
        
        BehaviourDefinition<ClassBehaviourBinding> binding =
                this.policyComponent.bindClassBehaviour(
                        policyName,
                        className,
                        new JavaBehaviour(this, policyName.getLocalName()));
        addBehaviour(binding);
    }
    
    /**
     * Bind an association policy to a JavaBehaviour if a specific event type is enabled
     * 
     * @param policyName the policy to implement or in other words the one we bind to the JavaBehaviour
     * @param eventTypeToCheck implement the policy only if this event type is enabled
     */
    protected void bindAssociationPolicy(QName policyName, String eventTypeToCheck)
    {
        if(!includeEventType(eventTypeToCheck))
        {
            return;
        }
        
        this.policyComponent.bindAssociationBehaviour(
                policyName,
                ContentModel.TYPE_BASE,
                new JavaBehaviour(this, policyName.getLocalName()));
    }    
    
    
    /**
     * Bind a class policy to a JavaBehaviour.
     * 
     * @param policyName the policy to implement or in other words the one we bind to the JavaBehaviour
     */
    protected void bindClassPolicy(QName policyName)
    {
        bindClassPolicy(policyName, null);
    }
}
