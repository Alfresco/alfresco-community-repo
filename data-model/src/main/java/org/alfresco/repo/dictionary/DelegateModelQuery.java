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

import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.QName;

/**
 * Model query that delegates its search if itself cannot find the model item required.
 * 
 * @author David Caruana
 *
 */
/* package */ class DelegateModelQuery implements ModelQuery
{

    private ModelQuery query;
    private ModelQuery delegate;

    /**
     * Construct
     * 
     * @param query
     *            ModelQuery
     * @param delegate
     *            ModelQuery
     */
    /* package */ DelegateModelQuery(ModelQuery query, ModelQuery delegate)
    {
        this.query = query;
        this.delegate = delegate;
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.repo.dictionary.impl.ModelQuery#getPropertyType(org.alfresco.repo.ref.QName) */
    public DataTypeDefinition getDataType(QName name)
    {
        DataTypeDefinition def = query.getDataType(name);
        if (def == null)
        {
            def = delegate.getDataType(name);
        }
        return def;
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.repo.dictionary.ModelQuery#getDataType(java.lang.Class) */
    public DataTypeDefinition getDataType(Class javaClass)
    {
        DataTypeDefinition def = query.getDataType(javaClass);
        if (def == null)
        {
            def = delegate.getDataType(javaClass);
        }
        return def;
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.repo.dictionary.impl.ModelQuery#getType(org.alfresco.repo.ref.QName) */
    public TypeDefinition getType(QName name)
    {
        TypeDefinition def = query.getType(name);
        if (def == null)
        {
            def = delegate.getType(name);
        }
        return def;
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.repo.dictionary.impl.ModelQuery#getAspect(org.alfresco.repo.ref.QName) */
    public AspectDefinition getAspect(QName name)
    {
        AspectDefinition def = query.getAspect(name);
        if (def == null)
        {
            def = delegate.getAspect(name);
        }
        return def;
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.repo.dictionary.impl.ModelQuery#getClass(org.alfresco.repo.ref.QName) */
    public ClassDefinition getClass(QName name)
    {
        ClassDefinition def = query.getClass(name);
        if (def == null)
        {
            def = delegate.getClass(name);
        }
        return def;
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.repo.dictionary.impl.ModelQuery#getProperty(org.alfresco.repo.ref.QName) */
    public PropertyDefinition getProperty(QName name)
    {
        PropertyDefinition def = query.getProperty(name);
        if (def == null)
        {
            def = delegate.getProperty(name);
        }
        return def;
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.repo.dictionary.impl.ModelQuery#getAssociation(org.alfresco.repo.ref.QName) */
    public AssociationDefinition getAssociation(QName name)
    {
        AssociationDefinition def = query.getAssociation(name);
        if (def == null)
        {
            def = delegate.getAssociation(name);
        }
        return def;
    }

    /* (non-Javadoc)
     * 
     * @see ModelQuery#getConstraint(QName) */
    public ConstraintDefinition getConstraint(QName name)
    {
        ConstraintDefinition def = query.getConstraint(name);
        if (def == null)
        {
            def = delegate.getConstraint(name);
        }
        return def;
    }
}
