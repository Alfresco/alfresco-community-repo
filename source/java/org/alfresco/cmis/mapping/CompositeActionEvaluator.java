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
package org.alfresco.cmis.mapping;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.cmis.CMISAllowedActionEnum;
import org.alfresco.cmis.mapping.ParentTypeActionEvaluator.ParentTypeEnum;
import org.alfresco.cmis.mapping.PropertyActionEvaluator.PropertyDescriptor;
import org.alfresco.cmis.mapping.TypeAttributeActionEvaluator.TypeDefinitionAttributeEnum;
import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.chemistry.abdera.ext.CMISAllowableActions;

/**
 * This action evaluator introduces simplified interface to construct set of conditions to determine, if an action is available for a specified object. It is generic evaluator,
 * which doesn't limit validation for any type of the objects. Also it introduces a possibility to build chain of the conditions.<br />
 * <br />
 * Evaluator allows specifying logical operation to calculate the final result, using the specified conditions: conjunction (AND operation; <b>DEFAULT</b> value) or disjunction (OR
 * operation).<br />
 * <br />
 * <b>N.B.:</b> each {@link CompositeActionEvaluator} may include other {@link CompositeActionEvaluator} conditions!
 * 
 * @author Dmitry Velichkevich
 */
public class CompositeActionEvaluator<ObjectType> extends AbstractActionEvaluator<ObjectType>
{
    private List<AbstractActionEvaluator<ObjectType>> conditions = new LinkedList<AbstractActionEvaluator<ObjectType>>();

    private boolean defaultAllowing;

    private boolean requiresDisjunction;

    /**
     * Constructor
     * 
     * @param defaultAllowing - {@link Boolean} value, which determines availability of action for several special cases (invalid object id, empty collection of the aspects etc.)
     * @param serviceRegistry - {@link ServiceRegistry} instance
     * @param action - {@link CMISAllowableActions} enumeration value, which determines the action to check
     */
    public CompositeActionEvaluator(boolean defaultAllowing, ServiceRegistry serviceRegistry, CMISAllowedActionEnum action)
    {
        super(serviceRegistry, action);
        this.defaultAllowing = defaultAllowing;
    }

    @Override
    public boolean isAllowed(ObjectType object)
    {
        boolean result = defaultAllowing;

        for (AbstractActionEvaluator<ObjectType> evaluator : conditions)
        {
            result = evaluator.isAllowed(object);

            if ((requiresDisjunction && result) || (!requiresDisjunction && !result))
            {
                break;
            }
        }

        return result;
    }

    /**
     * Adds {@link PermissionActionEvaluator} condition to the list
     * 
     * @param permissions - {@link String}... collection, which specifies all the permissions, which should be checked
     * @return {@link CompositeActionEvaluator} - self-instance, which allows making chain of conditions
     */
    public CompositeActionEvaluator<ObjectType> addPermissionCondition(String... permissions)
    {
        if (null != permissions)
        {
            conditions.add(new PermissionActionEvaluator<ObjectType>(getServiceRegistry(), getAction(), defaultAllowing, permissions));
        }

        return this;
    }

    /**
     * Add {@link PropertyActionEvaluator} condition to the list. This method implies, that each property from the <code>propertyIdsAndExpectedValues</code> collection must satisfy
     * conditions, which are specified in each {@link PropertyDescriptor} instance
     * 
     * @param propertyIdsAndExpectedValues - {@link PropertyDescriptor} collection, which specifies property ids and expected values or if value should not be <code>null</code>
     * @return {@link CompositeActionEvaluator} - self-instance, which allows making chain of conditions
     */
    public CompositeActionEvaluator<ObjectType> addPropertyCondition(PropertyDescriptor... propertyIdsAndExpectedValues)
    {
        if (null != propertyIdsAndExpectedValues)
        {
            conditions.add(new PropertyActionEvaluator<ObjectType>(getServiceRegistry(), getAction(), true, defaultAllowing, propertyIdsAndExpectedValues));
        }

        return this;
    }

    /**
     * Adds {@link TypeAttributeActionEvaluator} condition to the list. This method implies, that <code>attribute</code> value may be null and it is not {@link Map} or
     * {@link Collection}
     * 
     * @param attribute - {@link TypeDefinitionAttributeEnum} enumeration value, which determines attribute of object type definition, which should be validated
     * @param comparator - {@link Comparable} instance, which encapsulates the logic of checking a value, received from the object type definition in accordance with
     *        <code>attribute</code> parameter
     * @return {@link CompositeActionEvaluator} - self-instance, which allows making chain of conditions
     */
    public CompositeActionEvaluator<ObjectType> addTypeAttributeCondition(TypeDefinitionAttributeEnum attribute, Comparable<Object> comparator)
    {
        if ((null != attribute) && (null != comparator))
        {
            conditions.add(new TypeAttributeActionEvaluator<ObjectType>(attribute, new Pair<Object, Comparable<Object>>(null, comparator), true, defaultAllowing,
                    getServiceRegistry(), getAction()));
        }

        return this;
    }

    /**
     * Adds {@link ObjectLockedActionEvaluator} condition to the list. This method implies, that object should not have <code>lockType</code> lock
     * 
     * @param lockType - {@link LockType} enumeration value, which determines lock, required to check
     * @return {@link CompositeActionEvaluator} - self-instance, which allows making chain of conditions
     */
    public CompositeActionEvaluator<ObjectType> addLockCondition(LockType lockType)
    {
        conditions.add(new ObjectLockedActionEvaluator<ObjectType>(lockType, false, getServiceRegistry(), getAction()));
        return this;
    }

    /**
     * Adds {@link AspectActionEvaluator} condition to the list. This method sets only 1 {@link ContentModel#ASPECT_WORKING_COPY} aspect id and it implies
     * 
     * @param pwcExpected - {@link Boolean} value, which determines: <code>true</code> - object should be PWC, <code>false</code> - object should not be PWC
     * @return {@link CompositeActionEvaluator} - self-instance, which allows making chain of conditions
     */
    public CompositeActionEvaluator<ObjectType> addPwcCondition(boolean pwcExpected)
    {
        conditions.add(new AspectActionEvaluator<ObjectType>(getServiceRegistry(), getAction(), pwcExpected, true, defaultAllowing, ContentModel.ASPECT_WORKING_COPY));
        return this;
    }

    /**
     * Adds {@link AspectActionEvaluator} condition to the list. This method implies, that all aspects should satisfy to the <code>expected</code> condition
     * 
     * @param expected - {@link Boolean} value, which determines: <code>true</code> - aspects appliance is expected, <code>false</code> - aspects absence is expected
     * @param aspects - {@link QName}... collection, which specifies all the aspects, which should be validated
     * @return {@link CompositeActionEvaluator} - self-instance, which allows making chain of conditions
     */
    public CompositeActionEvaluator<ObjectType> addAspectCondition(boolean expected, QName... aspects)
    {
        conditions.add(new AspectActionEvaluator<ObjectType>(getServiceRegistry(), getAction(), expected, true, defaultAllowing, aspects));
        return this;
    }

    /**
     * Adds {@link ParentTypeActionEvaluator} condition to the list. This method implies, that parent of the <code>parentType</code> type is expected
     * 
     * @param parentType - {@link ParentTypeEnum} enumeration value, which determines, validation for which parent is required: for {@link ParentTypeEnum#MULTI_FILED}, for
     *        {@link ParentTypeEnum#REPOSITORY_ROOT} or for {@link ParentTypeEnum#PRIMARY_REPOSITORY_ROOT}
     * @return {@link CompositeActionEvaluator} - self-instance, which allows making chain of conditions
     */
    public CompositeActionEvaluator<ObjectType> addParentTypeCondition(ParentTypeEnum parentType)
    {
        conditions.add(new ParentTypeActionEvaluator<ObjectType>(getServiceRegistry(), getAction(), parentType, true));
        return this;
    }

    /**
     * Adds any condition to the list, if it is not a <code>null</code>
     * 
     * @param condition - {@link AbstractActionEvaluator} instance, which determines some condition
     * @return {@link CompositeActionEvaluator} - self-instance, which allows making chain of conditions
     */
    public CompositeActionEvaluator<ObjectType> addCondition(AbstractActionEvaluator<ObjectType> condition)
    {
        if (null != condition)
        {
            conditions.add(condition);
        }

        return this;
    }
}
