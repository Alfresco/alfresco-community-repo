/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.repo.policy.annotation;

import java.lang.reflect.Method;

import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * Annotated behaviour bean post processor.
 * <p>
 * Registers the annotated methods on behaviour beans with the policy component.
 *
 * @author Roy Wetherall
 * @since 5.0
 */
public class AnnotatedBehaviourPostProcessor implements BeanPostProcessor
{
    /** logger */
    private static Log logger = LogFactory.getLog(AnnotatedBehaviourPostProcessor.class);

    /** policy component */
    private PolicyComponent policyComponent;

    /** namespace service */
    private NamespaceService namespaceService;

    /**
     * @param policyComponent   policy component
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    /**
     * @param namespaceService  namespace service
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    /**
     * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessAfterInitialization(java.lang.Object, java.lang.String)
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName)
    {
        // register annotated behavior methods
        registerBehaviours(bean, beanName);

        // return the bean
        return bean;
    }

    /**
     * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessBeforeInitialization(java.lang.Object, java.lang.String)
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName)
    {
        // do nothing
        return bean;
    }

    /**
     * Register behaviours.
     *
     * @param bean      bean
     * @param beanName  bean name
     */
    private void registerBehaviours(Object bean, String beanName)
    {
        if (bean.getClass().isAnnotationPresent(BehaviourBean.class))
        {
            BehaviourBean behaviourBean = bean.getClass().getAnnotation(BehaviourBean.class);

            if (logger.isDebugEnabled())
            {
                logger.debug("Annotated behaviour post processing for " + beanName);
            }

            Method[] methods = bean.getClass().getMethods();
            for (Method method : methods)
            {
                if (method.isAnnotationPresent(Behaviour.class))
                {
                    registerBehaviour(behaviourBean, bean, beanName, method);
                }
            }
        }
    }

    /**
     * Register behaviour.
     *
     * @param behaviourBean behaviour bean annotation
     * @param bean          bean
     * @param beanName      bean name
     * @param method        method
     */
    private void registerBehaviour(BehaviourBean behaviourBean, Object bean, String beanName, Method method)
    {
        Behaviour behaviour = method.getAnnotation(Behaviour.class);
        QName policy = resolvePolicy(behaviour.policy(), method);
        QName type = resolveType(behaviourBean, behaviour);

        // assert that the policy and type have been set!!
        ParameterCheck.mandatory("policy", policy);
        if (!behaviour.isService())
        {
            ParameterCheck.mandatory("type", type);
        }

        if (logger.isDebugEnabled())
        {
            if (!behaviour.isService())
            {
                logger.debug("   ... binding " + behaviour.kind() + " behaviour for " + beanName + "." + method.getName() +
                                   " for policy " + policy.toString() +
                                   " and type " + type.toString());
            }
            else
            {
                logger.debug("   ... binding " + behaviour.kind() + " service behaviour for " + beanName + "." + method.getName() +
                                   " for policy " + policy.toString());
            }
        }

        // create java behaviour object
        JavaBehaviour javaBehaviour = new JavaBehaviour(bean, method.getName(), behaviour.notificationFrequency());

        // determine whether we should register the behaviour
        if (bean instanceof BehaviourRegistry && !behaviour.name().isEmpty())
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("   ... adding behaviour to registry with name " + behaviour.name());
            }

            ((BehaviourRegistry)bean).registerBehaviour(behaviour.name(), javaBehaviour);
        }

        // deal with class behaviours
        if (BehaviourKind.CLASS.equals(behaviour.kind()))
        {
            if (!behaviour.isService())
            {
                // bind class behaviour for given type
                policyComponent.bindClassBehaviour(policy, type, javaBehaviour);
            }
            else
            {
                // bind class service behaviour
                policyComponent.bindClassBehaviour(policy, bean, javaBehaviour);
            }
        }
        // deal with association behaviours
        else if (BehaviourKind.ASSOCIATION.equals(behaviour.kind()))
        {
            if (!behaviour.isService())
            {
                // bind association behaviour for given type and assoc type
                policyComponent.bindAssociationBehaviour(policy,
                                                         type,
                                                         toQName(behaviour.assocType()),
                                                         javaBehaviour);
            }
            else
            {
                // bind association service behaviour
                policyComponent.bindAssociationBehaviour(policy, bean, javaBehaviour);
            }
        }
    }

    /**
     * Resolve the policy qname, defaulting to the qualified name of the method if none specified.
     *
     * @param policyName     policy name
     * @param method         method
     * @return {@link QName} qualified name of the policy
     */
    private QName resolvePolicy(String policyName, Method method)
    {
        QName policy = null;
        if (policyName.isEmpty())
        {
            policy = QName.createQName(NamespaceService.ALFRESCO_URI, method.getName());
        }
        else
        {
            policy = toQName(policyName);
        }

        return policy;
    }

    /**
     *
     * @param behaviourBean
     * @param typeName
     * @return
     */
    private QName resolveType(BehaviourBean behaviourBean, Behaviour behaviour)
    {
        QName type = null;
        if (!behaviour.isService())
        {
            if (behaviour.type().isEmpty())
            {
                // get default
                type = toQName(behaviourBean.defaultType());
            }
            else
            {
                // convert set
                type = toQName(behaviour.type());
            }
        }
        return type;
    }

    /**
     *
     * @param name
     * @return
     */
    private QName toQName(String name)
    {
        return QName.createQName(name, namespaceService);
    }
}
