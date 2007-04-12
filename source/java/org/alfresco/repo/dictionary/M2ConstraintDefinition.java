/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.dictionary;

import java.util.List;

import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.repo.dictionary.constraint.NumericRangeConstraint;
import org.alfresco.repo.dictionary.constraint.RegexConstraint;
import org.alfresco.repo.dictionary.constraint.StringLengthConstraint;
import org.alfresco.service.cmr.dictionary.Constraint;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.PropertyAccessException;

/**
 * Compiled Property Constraint
 * 
 * @author Derek Hulley
 */
/* package */class M2ConstraintDefinition implements ConstraintDefinition
{
    public static final String ERR_CYCLIC_REF = "d_dictionary.constraint.err.cyclic_ref";

    public static final String ERR_TYPE_AND_REF = "d_dictionary.constraint.err.type_and_ref";

    public static final String ERR_TYPE_OR_REF = "d_dictionary.constraint.err.type_or_ref";

    public static final String ERR_REF_NOT_FOUND = "d_dictionary.constraint.err.ref_not_found";

    public static final String ERR_ANON_NEEDS_PROPERTY = "d_dictionary.constraint.err.anon_needs_property";

    public static final String ERR_INVALID_TYPE = "d_dictionary.constraint.err.invalid_type";

    public static final String ERR_SIMPLE_AND_LIST = "d_dictionary.constraint.err.property_simple_and_list";

    public static final String ERR_CONSTRUCT_FAILURE = "d_dictionary.constraint.err.construct_failure";

    public static final String ERR_PROPERTY_MISMATCH = "d_dictionary.constraint.err.property_mismatch";

    private static int anonPropCount = 0;

    private ModelDefinition model;

    private NamespacePrefixResolver prefixResolver;

    private M2Constraint m2Constraint;

    private QName name;

    private Constraint constraint;

    private boolean resolving;

    /* package */M2ConstraintDefinition(M2PropertyDefinition m2PropertyDef, M2Constraint m2Constraint,
            NamespacePrefixResolver prefixResolver)
    {
        this(m2PropertyDef.getModel(), m2PropertyDef, m2Constraint, prefixResolver);
    }

    /* package */M2ConstraintDefinition(ModelDefinition modelDefinition, M2PropertyDefinition m2PropertyDef,
            M2Constraint m2Constraint, NamespacePrefixResolver prefixResolver)
    {
        this.model = modelDefinition;
        this.m2Constraint = m2Constraint;
        this.prefixResolver = prefixResolver;

        String constraintName = m2Constraint.getName();
        if (constraintName == null)
        {
            // the constraint is anonymous, so it has to be defined within the context of a property
            if (m2PropertyDef == null)
            {
                throw new DictionaryException(ERR_ANON_NEEDS_PROPERTY);
            }
            // pick the name up from the property and some anonymous value
            String localName = m2PropertyDef.getName().getLocalName() + "_anon_" + (++anonPropCount);
            this.name = QName.createQName(m2PropertyDef.getName().getNamespaceURI(), localName);
            m2Constraint.setName(localName);
        }
        else
        {
            this.name = QName.createQName(m2Constraint.getName(), prefixResolver);
        }
    }

    /* package */synchronized void resolveDependencies(ModelQuery query)
    {
        if (resolving)
        {
            throw new DictionaryException(ERR_CYCLIC_REF, name.toPrefixString());
        }
        // prevent circular references
        try
        {
            resolving = true;
            resolveInternal(query);
        }
        finally
        {
            resolving = false;
        }
    }

    private synchronized void resolveInternal(ModelQuery query)
    {
        if (constraint != null)
        {
            // already been resolved
            return;
        }
        String shortName = name.toPrefixString();
        String ref = m2Constraint.getRef();
        String type = m2Constraint.getType();
        if (ref != null && type != null)
        {
            throw new DictionaryException(ERR_TYPE_AND_REF, shortName);
        }
        else if (ref == null && type == null)
        {
            throw new DictionaryException(ERR_TYPE_OR_REF, shortName);
        }
        else if (ref != null)
        {
            // resolve the reference name
            QName qnameRef = QName.createQName(ref, prefixResolver);
            // ensure that the reference exists in the model
            M2ConstraintDefinition constraintDef = (M2ConstraintDefinition) query.getConstraint(qnameRef);
            if (constraintDef == null)
            {
                throw new DictionaryException(ERR_REF_NOT_FOUND, ref, shortName);
            }
            // make sure that the constraint definition has itself been resolved
            constraintDef.resolveDependencies(query);
            // just use the constraint provided by the referenced definition
            this.constraint = constraintDef.getConstraint();
        }
        else
        {
            // we have to build the constraint from the type
            try
            {
                ConstraintType constraintType = ConstraintType.valueOf(type);
                constraint = constraintType.newInstance();
            }
            catch (IllegalArgumentException e)
            {
                // try to establish it as a class
                try
                {
                    Class clazz = Class.forName(type);
                    constraint = (Constraint) clazz.newInstance();
                }
                catch (ClassNotFoundException ee)
                {
                    throw new DictionaryException(ERR_INVALID_TYPE, type, shortName);
                }
                catch (ClassCastException ee)
                {
                    throw new DictionaryException(ERR_INVALID_TYPE, type, shortName);
                }
                catch (Exception ee)
                {
                    throw new DictionaryException(ERR_CONSTRUCT_FAILURE, type, shortName);
                }
            }
            // property setters
            BeanWrapper beanWrapper = new BeanWrapperImpl(constraint);
            List<M2NamedValue> constraintNamedValues = m2Constraint.getParameters();
            
            if (constraintNamedValues != null)
            {
                for (M2NamedValue namedValue : constraintNamedValues)
                {
                    Object value = null;
                    if (namedValue.getSimpleValue() != null && namedValue.getListValue() != null)
                    {
                        throw new DictionaryException(ERR_SIMPLE_AND_LIST, shortName, namedValue.getName());
                    }
                    else if (namedValue.getSimpleValue() != null)
                    {
                        value = namedValue.getSimpleValue();
                    }
                    else if (namedValue.getListValue() != null)
                    {
                        value = namedValue.getListValue();
                    }
                    try
                    {
                        beanWrapper.setPropertyValue(namedValue.getName(), value);
                    }
                    catch (PropertyAccessException e)
                    {
                        throw new DictionaryException(ERR_PROPERTY_MISMATCH, e, namedValue.getName(), shortName);
                    }
                    catch (InvalidPropertyException e)
                    {
                        throw new DictionaryException(ERR_PROPERTY_MISMATCH, e, namedValue.getName(), shortName);
                    }
                }
            }
            // now initialize
            constraint.initialize();
        }
    }

    /**
     * @see #getName()
     */
    @Override
    public String toString()
    {
        return getName().toString();
    }

    public ModelDefinition getModel()
    {
        return model;
    }

    public QName getName()
    {
        return name;
    }

    public Constraint getConstraint()
    {
        return constraint;
    }

    /**
     * Well-known constraint types
     */
    public static enum ConstraintType
    {
        REGEX
        {
            @Override
            protected Constraint newInstance()
            {
                return new RegexConstraint();
            }
        },
        MINMAX
        {
            @Override
            protected Constraint newInstance()
            {
                return new NumericRangeConstraint();
            }
        },
        LENGTH
        {
            @Override
            protected Constraint newInstance()
            {
                return new StringLengthConstraint();
            }
        },
        LIST
        {
            @Override
            protected Constraint newInstance()
            {
                return new ListOfValuesConstraint();
            }
        };

        /**
         * @return Returns the constraint implementation
         */
        protected abstract Constraint newInstance();
    }
}
