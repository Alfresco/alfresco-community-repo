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

import java.io.Serializable;

import org.alfresco.cmis.CMISAllowedActionEnum;
import org.alfresco.cmis.CMISDictionaryModel;
import org.alfresco.cmis.CMISInvalidArgumentException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.chemistry.abdera.ext.CMISAllowableActions;

/**
 * This evaluator determines an action availability in accordance with collection of {@link PropertyDescriptor} and in accordance with rules of checking of the object properties.
 * The rules are:<br />
 * - should each specific property in the list satisfy all the conditions, which are specified in appropriate {@link PropertyDescriptor} instance or at least 1 property
 * satisfaction is enough.<br />
 * <br />
 * This evaluator is generic, because it is used in the scope of {@link CompositeActionEvaluator}
 * 
 * @author Dmitry Velichkevich
 * @see PropertyDescriptor
 */
public class PropertyActionEvaluator<ValidatingObjectType> extends AbstractActionEvaluator<ValidatingObjectType>
{
    private PropertyDescriptor[] propertyIdsAndExpectedValues;

    private boolean allPropertiesConcur;

    private boolean defaultAllowing;

    /**
     * Constructor
     * 
     * @param serviceRegistry - {@link ServiceRegistry} instance
     * @param action - {@link CMISAllowableActions} enumeration value, which determines the action to check
     * @param allPropertiesConcur - {@link Boolean} value, which determines: <code>true</code> - each specific object property should satisfy all the conditions of appropriate
     *        {@link PropertyDescriptor}, <code>false</code> - at least 1 object property satisfaction is enough
     * @param defaultAllowing - {@link Boolean} value, which determines availability of action for several special cases (invalid object id, empty collection of the aspects etc.)
     * @param propertyIdsAndExpectedValues - {@link PropertyDescriptor}... collection, which specifies all the properties, which should be validated on an object
     */
    public PropertyActionEvaluator(ServiceRegistry serviceRegistry, CMISAllowedActionEnum action, boolean allPropertiesConcur, boolean defaultAllowing,
            PropertyDescriptor... propertyIdsAndExpectedValues)
    {
        super(serviceRegistry, action);
        this.propertyIdsAndExpectedValues = propertyIdsAndExpectedValues;
        this.allPropertiesConcur = allPropertiesConcur;
    }

    @Override
    public boolean isAllowed(ValidatingObjectType object)
    {
        boolean result = defaultAllowing;

        if (null != propertyIdsAndExpectedValues)
        {
            for (PropertyDescriptor descriptor : propertyIdsAndExpectedValues)
            {
                if ((null != descriptor) && (null != descriptor.getPropertyId()))
                {
                    Serializable left = null;

                    try
                    {
                        if (object instanceof NodeRef)
                        {
                            left = getServiceRegistry().getCMISService().getProperty((NodeRef) object, descriptor.getPropertyId());
                        }
                        else
                        {
                            if (object instanceof AssociationRef)
                            {
                                left = getServiceRegistry().getCMISService().getProperty((AssociationRef) object, descriptor.getPropertyId());
                            }
                            else
                            {
                                return false;
                            }
                        }
                    }
                    catch (CMISInvalidArgumentException e)
                    {
                        throw new RuntimeException(e.toString(), e);
                    }

                    result = descriptor.satisfies(left);

                    if ((allPropertiesConcur && !result) || (!allPropertiesConcur && result))
                    {
                        break;
                    }
                }
            }
        }

        return result;
    }

    /**
     * This class encapsulates description of object property to validate some actual object property against defined condition. This, in turn, allows determine, if some action is
     * allowable for an object in accordance with value or values of the property or properties.<br />
     * <br />
     * <b>N.B.:</b><code>null</code> expected value is supported! <br />
     * The class introduces the following fields:<br />
     * - property definition id (subject to reference is {@link CMISDictionaryModel}; {@link PropertyDescriptor#getPropertyId()}); - expected property value
     * {@link PropertyDescriptor#getPropertyValue()}; - may property be equal to <code>null</code> {@link PropertyDescriptor#isNullExpected()}
     * 
     * @author Dmitry Velichkevich
     * @see CMISDictionaryModel
     */
    public static class PropertyDescriptor
    {
        private String propertyId;

        private Serializable propertyValue;

        private boolean nullExpected;

        /**
         * Constructor
         * 
         * @param propertyId - {@link String} value, which determines property definition id (subject to reference is {@link CMISDictionaryModel})
         * @param propertyValue - {@link Serializable} instance, which specifies expected property value
         * @param nullExpected - {@link Boolean} value, which determines: <code>true</code> - property may be <code>null</code>, <code>false</code> - property can't be equal to
         *        <code>null</code> (this leads to ignoring {@link PropertyDescriptor#getPropertyValue()} value)
         */
        public PropertyDescriptor(String propertyId, Serializable propertyValue, boolean nullExpected)
        {
            this.propertyId = propertyId;
            this.propertyValue = propertyValue;
            this.nullExpected = nullExpected;
        }

        /**
         * Getter
         * 
         * @return {@link String} value, which represents one of the {@link CMISDictionaryModel} property definition ids
         */
        public String getPropertyId()
        {
            return propertyId;
        }

        /**
         * Getter
         * 
         * @return {@link Serializable} instance, which specifies expected property value
         */
        public Serializable getPropertyValue()
        {
            return propertyValue;
        }

        /**
         * Getter
         * 
         * @return {@link Boolean} value, which determines: <code>true</code> - property may be <code>null</code>, <code>false</code> - property can't be equal to <code>null</code>
         *         (this leads to ignoring {@link PropertyDescriptor#getPropertyValue()} value)
         */
        public boolean isNullExpected()
        {
            return nullExpected;
        }

        /**
         * This method checks whether specified <code>value</code> satisfies to all the defined conditions in current instance of {@link PropertyDescriptor}
         * 
         * @param value - {@link Serializable} instance, which represents actual value of some object property
         * @return {@link Boolean} value, which determines: <code>true</code> - specified <code>value</code> satisfies to all the defined conditions, <code>false</code> - specified
         *         <code>value</code> doesn't satisfy to all the defined conditions
         */
        public boolean satisfies(Serializable value)
        {
            if (!nullExpected)
            {
                return null != value;
            }

            return (null != value) ? (value.equals(propertyValue)) : (null == propertyValue);
        }
    }
}
