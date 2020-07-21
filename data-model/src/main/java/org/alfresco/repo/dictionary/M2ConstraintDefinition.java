/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.repo.dictionary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.repo.dictionary.constraint.NumericRangeConstraint;
import org.alfresco.repo.dictionary.constraint.RegexConstraint;
import org.alfresco.repo.dictionary.constraint.RegisteredConstraint;
import org.alfresco.repo.dictionary.constraint.StringLengthConstraint;
import org.alfresco.repo.i18n.StaticMessageLookup;
import org.alfresco.service.cmr.dictionary.Constraint;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.cmr.i18n.MessageLookup;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.EqualsHelper;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.PropertyAccessException;

/**
 * Compiled Property Constraint
 * 
 * @author Derek Hulley. janv
 */
/* package */class M2ConstraintDefinition implements ConstraintDefinition
{
    private static final String PROP_SHORT_NAME = "shortName";
    private static final String PROP_TITLE = "title";
    private static final String PROP_DESCRIPTION = "description";
    
    public static final String ERR_CYCLIC_REF = "d_dictionary.constraint.err.cyclic_ref";
    public static final String ERR_TYPE_AND_REF = "d_dictionary.constraint.err.type_and_ref";
    public static final String ERR_TYPE_OR_REF = "d_dictionary.constraint.err.type_or_ref";
    public static final String ERR_REF_NOT_FOUND = "d_dictionary.constraint.err.ref_not_found";
    public static final String ERR_ANON_NEEDS_PROPERTY = "d_dictionary.constraint.err.anon_needs_property";
    public static final String ERR_INVALID_TYPE = "d_dictionary.constraint.err.invalid_type";
    public static final String ERR_SIMPLE_AND_LIST = "d_dictionary.constraint.err.property_simple_and_list";
    public static final String ERR_CONSTRUCT_FAILURE = "d_dictionary.constraint.err.construct_failure";
    public static final String ERR_PROPERTY_MISMATCH = "d_dictionary.constraint.err.property_mismatch";
    public static final String ERR_RESERVED_PROPERTY = "d_dictionary.constraint.err.reserved_property";
    public static final String ERR_NAMESPACE_NOT_DEFINED = "d_dictionary.constraint.err.namespace_not_defined";

    private static int anonPropCount = 0;

    private ModelDefinition model;
    private NamespacePrefixResolver prefixResolver;
    private M2Constraint m2Constraint;
    private QName name;
    private Constraint constraint;
    private boolean resolving;
    private transient MessageLookup staticMessageLookup = new StaticMessageLookup();

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
            m2Constraint.setName(this.name.getPrefixedQName(prefixResolver).toPrefixString());
        }
        else
        {
            this.name = QName.createQName(m2Constraint.getName(), prefixResolver);
            if (!model.isNamespaceDefined(name.getNamespaceURI()))
            {
                throw new DictionaryException(ERR_NAMESPACE_NOT_DEFINED, name.toPrefixString(), name.getNamespaceURI(), model.getName().toPrefixString());
            }
        }
    }

    /* package */synchronized void resolveDependencies(ModelQuery query, boolean enableConstraintClassLoading)
    {
        if (resolving)
        {
            throw new DictionaryException(ERR_CYCLIC_REF, name.toPrefixString());
        }
        // prevent circular references
        try
        {
            resolving = true;
            resolveInternal(query, enableConstraintClassLoading);
        }
        finally
        {
            resolving = false;
        }
    }

    private synchronized void resolveInternal(ModelQuery query, boolean enableConstraintClassLoading)
    {
        if (constraint != null)
        {
            // already been resolved
            return;
        }
        
        String shortName = name.toPrefixString();
        String ref = m2Constraint.getRef();
        String type = m2Constraint.getType();
        
        String title = m2Constraint.getTitle();
        String description = m2Constraint.getDescription();
        
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
            constraintDef.resolveDependencies(query, enableConstraintClassLoading);
            // just use the constraint provided by the referenced definition
            this.constraint = constraintDef.getConstraint();
            
            //use real constraint name instead of anonymous name
            //TODO Fix backed out - breaks DictionaryDAOTest
            //this.name = constraintDef.getName();
     
            
            if (m2Constraint.getTitle() == null)
            {
                m2Constraint.setTitle(constraintDef.getTitle(null));
            }
            
            if (m2Constraint.getDescription() == null)
            {
                m2Constraint.setDescription(constraintDef.getDescription(null));
            }
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
                    if(enableConstraintClassLoading)
                    {
                        @SuppressWarnings("unchecked")
                        Class clazz = Class.forName(type);
                        constraint = (Constraint) clazz.newInstance();
                    }
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
            
            if(constraint != null)
            {
                // property setters
                BeanWrapper beanWrapper = new BeanWrapperImpl(constraint);
                List<M2NamedValue> constraintNamedValues = m2Constraint.getParameters();

                if (constraintNamedValues != null)
                {
                    for (M2NamedValue namedValue : constraintNamedValues)
                    {
                        String namedValueName = namedValue.getName();
                        // Check for reserved properties
                        if (namedValueName.equals(PROP_SHORT_NAME))
                        {
                            throw new DictionaryException(ERR_RESERVED_PROPERTY, PROP_SHORT_NAME, namedValueName);
                        }

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
                            beanWrapper.setPropertyValue(namedValueName, value);
                        }
                        catch (PropertyAccessException e)
                        {
                            throw new DictionaryException(ERR_PROPERTY_MISMATCH, e, namedValueName, shortName);
                        }
                        catch (InvalidPropertyException e)
                        {
                            throw new DictionaryException(ERR_PROPERTY_MISMATCH, e, namedValueName, shortName);
                        }
                    }

                    // Pass in the short name as a special property, if it is available
                    if (beanWrapper.isWritableProperty(PROP_SHORT_NAME))
                    {
                        try
                        {
                            beanWrapper.setPropertyValue(PROP_SHORT_NAME, shortName);
                        }
                        catch (PropertyAccessException e)
                        {
                            throw new DictionaryException(ERR_PROPERTY_MISMATCH, e, shortName, shortName);
                        }
                        catch (InvalidPropertyException e)
                        {
                            throw new DictionaryException(ERR_PROPERTY_MISMATCH, e, shortName, shortName);
                        }
                    }

                    if ((title != null) && (beanWrapper.isWritableProperty(PROP_TITLE)))
                    {
                        beanWrapper.setPropertyValue(PROP_TITLE, title);
                    }

                    if ((title != null) && (beanWrapper.isWritableProperty(PROP_DESCRIPTION)))
                    {
                        beanWrapper.setPropertyValue(PROP_DESCRIPTION, description);
                    }
                }

            // now initialize
            constraint.initialize();
            }
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

    @Override
    public ModelDefinition getModel()
    {
        return model;
    }

    @Override
    public QName getName()
    {
        return name;
    }
    
    @Override
    public String getTitle()
    {
        return getTitle(staticMessageLookup);
    }

    @Override
    public String getDescription()
    {
        return getDescription(staticMessageLookup);
    }

    @Override
    public String getTitle(MessageLookup messageLookup)
    {
        String value = M2Label.getLabel(model, messageLookup, "constraint", name, "title"); 
        if (value == null)
        {
            value = m2Constraint.getTitle();
        }
        return value;
    }
    
    @Override
    public String getDescription(MessageLookup messageLookup)
    {
        String value = M2Label.getLabel(model, messageLookup, "constraint", name, "description"); 
        if (value == null)
        {
            value = m2Constraint.getDescription();
        }
        return value;
    }

    @Override
    public Constraint getConstraint()
    {
        return constraint;
    }
    
    @Override
    public QName getRef()
    {
        QName refQName = null;
        String ref = m2Constraint.getRef();
        if (ref != null)
        {
            refQName = QName.createQName(ref, prefixResolver);
        }
        return refQName;
    }
    
    /**
     * Well-known constraint types
     */
    public static enum ConstraintType
    {
        REGISTERED
        {
            @Override
            protected Constraint newInstance()
            {
                return new RegisteredConstraint();
            }
        },
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
    
    /* package */ M2ModelDiff diffConstraint(ConstraintDefinition conDef)
    {
        M2ModelDiff modelDiff = null;
        boolean isUpdated = false;
        boolean isUpdatedIncrementally = false;
        
        if (this == conDef)
        {
            modelDiff = new M2ModelDiff(name, M2ModelDiff.TYPE_CONSTRAINT, M2ModelDiff.DIFF_UNCHANGED);
            return modelDiff;
        }
        
        // check name - cannot be null
        if (! name.equals(conDef.getName()))
        { 
            isUpdated = true;
        }
        
        // check title
        if (! EqualsHelper.nullSafeEquals(getTitle(null), conDef.getTitle(null), false))
        { 
            isUpdatedIncrementally = true;
        }
        
        // check description
        if (! EqualsHelper.nullSafeEquals(getDescription(null), conDef.getDescription(null), false))
        { 
            isUpdatedIncrementally = true;
        }
        
        // check type string
        if (! EqualsHelper.nullSafeEquals(getConstraint().getType(), conDef.getConstraint().getType()))
        { 
            isUpdated = true;
        }
        
        if (isUpdated)
        {
            modelDiff = new M2ModelDiff(name, M2ModelDiff.TYPE_CONSTRAINT, M2ModelDiff.DIFF_UPDATED);
        }
        else if (isUpdatedIncrementally)
        {
            modelDiff = new M2ModelDiff(name, M2ModelDiff.TYPE_CONSTRAINT, M2ModelDiff.DIFF_UPDATED_INC);
        }
        else
        {
            modelDiff = new M2ModelDiff(name, M2ModelDiff.TYPE_CONSTRAINT, M2ModelDiff.DIFF_UNCHANGED);
        }
        
        return modelDiff;
    }
    
    /*package*/ static Collection<M2ModelDiff> diffConstraintLists(Collection<ConstraintDefinition> previousConstraints, Collection<ConstraintDefinition> newConstraints)
    {
        List<M2ModelDiff> modelDiffs = new ArrayList<M2ModelDiff>();
        
        for (ConstraintDefinition previousConstraint : previousConstraints)
        {
            boolean found = false;
            for (ConstraintDefinition newConstraint : newConstraints)
            {
                if (newConstraint.getName().equals(previousConstraint.getName()))
                {
                    modelDiffs.add(((M2ConstraintDefinition)previousConstraint).diffConstraint(previousConstraint));
                    found = true;
                    break;
                }
            }
            
            if (! found)
            {
                modelDiffs.add(new M2ModelDiff(previousConstraint.getName(), M2ModelDiff.TYPE_CONSTRAINT, M2ModelDiff.DIFF_DELETED));
            }
        }
        
        for (ConstraintDefinition newConstraint : newConstraints)
        {
            boolean found = false;
            for (ConstraintDefinition previousConstraint : previousConstraints)
            {
                if (newConstraint.getName().equals(previousConstraint.getName()))
                {
                    found = true;
                    break;
                }
            }
            
            if (! found)
            {
                modelDiffs.add(new M2ModelDiff(newConstraint.getName(), M2ModelDiff.TYPE_CONSTRAINT, M2ModelDiff.DIFF_CREATED));
            }
        }
        
        return modelDiffs;
    }
}
