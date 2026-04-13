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
package org.alfresco.opencmis.dictionary;

import java.util.Collection;

import org.alfresco.repo.dictionary.DictionaryComponent;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.QName;

/**
 * A DictionaryComponent that uses a QNameFilter to constrain what is returned.
 *
 * @author Gethin James
 */
public class FilteredDictionaryComponent extends DictionaryComponent
{
    QNameFilter filter;

    @Override
    public Collection<QName> getAllTypes()
    {
        return filter.filterQName(super.getAllTypes());
    }

    @Override
    public Collection<QName> getSubTypes(QName superType, boolean follow)
    {
        return filter.filterQName(super.getSubTypes(superType, follow));
    }

    @Override
    public Collection<QName> getAllAspects()
    {
        return filter.filterQName(super.getAllAspects());
    }

    @Override
    public Collection<QName> getAllAssociations()
    {
        return filter.filterQName(super.getAllAssociations());
    }

    @Override
    public Collection<QName> getSubAspects(QName superAspect, boolean follow)
    {
        return filter.filterQName(super.getSubAspects(superAspect, follow));
    }

    @Override
    public TypeDefinition getType(QName name)
    {
        if (filter.isExcluded(name)) return null;  //Don't return an excluded type
        return super.getType(name);
    }
    
    @Override
    public AspectDefinition getAspect(QName name)
    {
        if (filter.isExcluded(name)) return null;  //Don't return an excluded type
        return super.getAspect(name);
    }
    
    public void setFilter(QNameFilter filter)
    {
        this.filter = filter;
    }

}
