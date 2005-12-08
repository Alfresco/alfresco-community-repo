/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.dictionary;

import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.QName;

/**
 * Model query that delegates its search if itself cannot find the model
 * item required.
 * 
 * @author David Caruana
 *
 */
/*package*/ class DelegateModelQuery implements ModelQuery
{

    private ModelQuery query;
    private ModelQuery delegate;
    
    
    /**
     * Construct
     * 
     * @param query
     * @param delegate
     */
    /*package*/ DelegateModelQuery(ModelQuery query, ModelQuery delegate)
    {
        this.query = query;
        this.delegate = delegate;
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.impl.ModelQuery#getPropertyType(org.alfresco.repo.ref.QName)
     */
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
     * @see org.alfresco.repo.dictionary.ModelQuery#getDataType(java.lang.Class)
     */
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
     * @see org.alfresco.repo.dictionary.impl.ModelQuery#getType(org.alfresco.repo.ref.QName)
     */
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
     * @see org.alfresco.repo.dictionary.impl.ModelQuery#getAspect(org.alfresco.repo.ref.QName)
     */
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
     * @see org.alfresco.repo.dictionary.impl.ModelQuery#getClass(org.alfresco.repo.ref.QName)
     */
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
     * @see org.alfresco.repo.dictionary.impl.ModelQuery#getProperty(org.alfresco.repo.ref.QName)
     */
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
     * @see org.alfresco.repo.dictionary.impl.ModelQuery#getAssociation(org.alfresco.repo.ref.QName)
     */
    public AssociationDefinition getAssociation(QName name)
    {
        AssociationDefinition def = query.getAssociation(name);
        if (def == null)
        {
            def = delegate.getAssociation(name);
        }
        return def;
    }
    
}
