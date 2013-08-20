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
import java.util.Collection;
import java.util.Map;

import org.alfresco.cmis.CMISAllowedActionEnum;
import org.alfresco.cmis.CMISInvalidArgumentException;
import org.alfresco.cmis.CMISTypeDefinition;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.util.Pair;
import org.apache.chemistry.abdera.ext.CMISAllowableActions;

/**
 * This evaluator determines an action availability in accordance with value of object type definition attribute and in accordance with rules of checking of the object type
 * definition attribute value. The rules are:<br />
 * - attribute value may be <code>null</code> or it cannot. <code>getSecond()</code> of the {@link TypeAttributeActionEvaluator#comparator} field is ignored, if it cannot be equal
 * to <code>null</code> <br />
 * <br />
 * This evaluator is generic, because it is used in the scope of {@link CompositeActionEvaluator}. <br />
 * <br />
 * 
 * @author Dmitry Velichkevich
 * @see TypeAttributeActionEvaluator#comparator
 */
public class TypeAttributeActionEvaluator<ObjectType> extends AbstractActionEvaluator<ObjectType>
{
    private TypeDefinitionAttributeEnum attribute;

    /**
     * This field contains descriptor to extract and compare desired attribute value from object type definition. The following attribute value data types are supported:<br />
     * - descendants of {@link Serializable} interface;<br />
     * - descendants of {@link Collection} interface;<br />
     * - descendatns of {@link Map} interface.<br />
     * <br />
     * Result of the <code>getFirst()</code> method of the <code>comparator</code> field must contain the following value:<br />
     * - this value is completely ignored, if attribute data type is descendant of {@link Serializable};<br />
     * - {@link Integer} number, which determines index of element in collection. Element will be received, using {@link Collection#toArray()}[({@link Integer})
     * <code>comparator.getFirst()</code>];<br />
     * - an appropriate object, which may be used as a key to receive attribute value, if attribute data type is descendant of the {@link Map}<br />
     * <br />
     * Result of <code>getSecond()</code> method of the <code>comparator</code> field MUST NOT be <code>null</code>! This {@link Comparable} instance MUST encapsulate the logic of
     * additional processing of extracted actual attribute value (for example, if extracted value has some custom data type), if necessary. And it should encapsulate the logic of
     * comparing an actual value of the attribute with the expected. Expected value is also a subject of encapsulation of the {@link Comparable}
     */
    private Pair<Object, Comparable<Object>> comparator;

    private boolean nullExpected;

    private boolean defaultAllowing;

    /**
     * Constructor
     * 
     * @param attribute - {@link TypeDefinitionAttributeEnum} enumeration value, which specifies method of {@link CMISTypeDefinition} to receive attribute, which should be
     *        validated
     * @param comparator - {@link Pair}&lt;{@link Object}, {@link Comparable}&lt;{@link Object}&gt;&gt; instance. See {@link TypeAttributeActionEvaluator#comparator} for more
     *        details
     * @param nullExpected - {@link Boolean} value, which determines: <code>true</code> - attribute value may be <code>null</code>, <code>false</code> - attribute value can't be
     *        equal to <code>null</code> (this leads to ignoring <code>getSecond()</code> of the {@link TypeAttributeActionEvaluator#comparator} field)
     * @param defaultAllowing - {@link Boolean} value, which determines availability of action for several special cases (invalid object id, empty collection of the aspects etc.)
     * @param serviceRegistry - {@link ServiceRegistry} instance
     * @param action - {@link CMISAllowableActions} enumeration value, which determines the action to check
     */
    public TypeAttributeActionEvaluator(TypeDefinitionAttributeEnum attribute, Pair<Object, Comparable<Object>> comparator, boolean nullExpected, boolean defaultAllowing,
            ServiceRegistry serviceRegistry, CMISAllowedActionEnum action)
    {
        super(serviceRegistry, action);
        this.attribute = attribute;
        this.comparator = comparator;
        this.nullExpected = nullExpected;
        this.defaultAllowing = defaultAllowing;
    }

    @Override
    public boolean isAllowed(ObjectType object)
    {
        boolean result = defaultAllowing;

        if ((null != object) && (null != attribute) && (null != comparator) && (null != comparator.getSecond()))
        {
            CMISTypeDefinition typeDefinition = null;

            try
            {
                typeDefinition = getServiceRegistry().getCMISService().getTypeDefinition(object);
            }
            catch (CMISInvalidArgumentException e)
            {
                throw new RuntimeException(e.toString(), e);
            }

            result = attribute.satisfies(typeDefinition, comparator, nullExpected, defaultAllowing);
        }

        return result;
    }

    /**
     * This enumeration encapsulates the logic of object type definition attributes comparing. It uses Java Reflection mechanism to get actual attribute value, which should be
     * validated
     * 
     * @author Dmitry Velichkevich
     */
    public static enum TypeDefinitionAttributeEnum
    {
        /**
         * {@link CMISTypeDefinition#isPublic()}
         */
        PUBLIC("isPublic", true),

        /**
         * {@link CMISTypeDefinition#getTypeId()}
         */
        TYPE_ID("getTypeId", true),

        /**
         * {@link CMISTypeDefinition#getQueryName()}
         */
        QUERY_NAME("getQueryName", true),

        /**
         * {@link CMISTypeDefinition#getDisplayName()}
         */
        DISPLAY_NAME("getDisplayName", true),

        /**
         * {@link CMISTypeDefinition#getParentType()}
         */
        PARENT_TYPE("getParentType", false),

        /**
         * {@link CMISTypeDefinition#getSubTypes(boolean)}
         */
        SUB_TYPES("getSubTypes", false),

        /**
         * {@link CMISTypeDefinition#getBaseType()}
         */
        BASE_TYPE("getBaseType", false),

        /**
         * {@link CMISTypeDefinition#getDescription()}
         */
        DESCRIPTION("getDescription", true),

        /**
         * {@link CMISTypeDefinition#isCreatable()}
         */
        CREATABLE("isCreatable", true),

        /**
         * {@link CMISTypeDefinition#isFileable()}
         */
        FILEABLE("isFileable", true),

        /**
         * {@link CMISTypeDefinition#isQueryable()}
         */
        QUERYABLE("isQueryable", true),

        /**
         * {@link CMISTypeDefinition#isFullTextIndexed()}
         */
        FULL_TEXT_INDEXED("isFullTextIndexed", true),

        /**
         * {@link CMISTypeDefinition#isControllablePolicy()}
         */
        CONTROLLABLE_POLICY("isControllablePolicy", true),

        /**
         * {@link CMISTypeDefinition#isControllableACL()}
         */
        CONTROLLABLE_ACL("isControllableACL", true),

        /**
         * {@link CMISTypeDefinition#isIncludedInSuperTypeQuery()}
         */
        INCLUDED_IN_SUPER_TYPE_QUERY("isIncludedInSuperTypeQuery", true),

        /**
         * {@link CMISTypeDefinition#isVersionable()}
         */
        VERSIONABLE("isVersionable", true),

        /**
         * {@link CMISTypeDefinition#getContentStreamAllowed()}
         */
        CONTENT_STREAM_ALLOWED("getContentStreamAllowed", true),

        /**
         * {@link CMISTypeDefinition#getAllowedSourceTypes()}
         */
        ALLOWED_SOURCE_TYPES("getAllowedSourceTypes", false),

        /**
         * {@link CMISTypeDefinition#getAllowedTargetTypes()}
         */
        ALLOWED_TARGET_TYPES("getAllowedTargetTypes", false),

        /**
         * {@link CMISTypeDefinition#getPropertyDefinitions()}
         */
        PROPERTY_DEFINITIONS("getPropertyDefinitions", false),

        /**
         * {@link CMISTypeDefinition#getOwnedPropertyDefinitions()}
         */
        OWNED_PROPERTY_DEFINITIONS("getOwnedPropertyDefinitions", false);

        private String methodName;

        private boolean primitiveType;

        /**
         * Constructor
         * 
         * @param methodName - {@link String} value, which specifies name of the method, which returns desired attribute value
         * @param primitiveType - {@link Boolean} value, which determines: <code>true</code> - attribute value data type IS NOT descendant of {@link Collection} or {@link Map},
         *        <code>false</code> - attribute value data type IS descendant of {@link Collection} or {@link Map}
         */
        private TypeDefinitionAttributeEnum(String methodName, boolean primitiveType)
        {
            this.methodName = methodName;
            this.primitiveType = primitiveType;
        }

        /**
         * This method determines, if object data type definition contains attribute, which satisfies to condition to allow some action
         * 
         * @param typeDefinition - {@link CMISTypeDefinition} instance of the object, which should be checked
         * @param comparator - {@link Pair}&lt;{@link Object}, {@link Comparable}&lt;{@link Object}&gt;&gt; instance. See {@link TypeAttributeActionEvaluator#comparator} for more
         *        details
         * @param nullExpected - {@link Boolean} value, which determines: <code>true</code> - attribute value may be <code>null</code>, <code>false</code> - attribute value can't
         *        be equal to <code>null</code> (this leads to ignoring <code>getSecond()</code> of the <code>comparator</code> attribute)
         * @param defaultAllowing - {@link Boolean} value, which determines availability of action for several special cases (invalid object id, empty collection of the aspects
         *        etc.)
         * @return - {@link Boolean} value, which determines: <code>true</code> - actual attribute value satisfies all the conditions of the <code>comparator</code> parameter,
         *         <code>false</code> - actual attribute value doesn't satisfy conditions of the <code>comparator</code> parameter
         */
        @SuppressWarnings("unchecked")
        public boolean satisfies(CMISTypeDefinition typeDefinition, Pair<Object, Comparable<Object>> comparator, boolean nullExpected, boolean defaultAllowing)
        {
            if (null == typeDefinition)
            {
                return defaultAllowing;
            }

            Object actualValue = null;
            try
            {
                actualValue = typeDefinition.getClass().getMethod(methodName).invoke(typeDefinition);
            }
            catch (Exception e)
            {
                throw new RuntimeException("Interface of '" + CMISTypeDefinition.class.getName() + "' has been modified!");
            }

            if ((null == actualValue) && !primitiveType)
            {
                return defaultAllowing;
            }

            if (actualValue instanceof Map)
            {
                actualValue = ((Map) actualValue).get(comparator.getFirst());
            }
            else
            {
                if (actualValue instanceof Collection)
                {
                    actualValue = ((Collection) actualValue).toArray()[(Integer) comparator.getFirst()];
                }
            }

            if (!nullExpected)
            {
                return null != actualValue;
            }

            return 0 == comparator.getSecond().compareTo(actualValue);
        }
    }
}
