/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.cmis.rest;

import java.util.Set;
import java.util.TreeSet;

import javax.xml.namespace.QName;

import org.apache.abdera.factory.Factory;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.ExtensibleElementWrapper;
import org.apache.chemistry.abdera.ext.CMISConstants;
import org.apache.chemistry.abdera.ext.CMISProperties;

/**
 * Alfresco CMIS extension for controlling aspects and their properties.
 * 
 * @author dward
 */
public class SetAspectsExtension extends ExtensibleElementWrapper
{
    /** The Alfresco extension namespace. */
    private static final String NAMESPACE = "http://www.alfresco.org";

    /** The name of this element. */
    public static final QName QNAME = new QName(NAMESPACE, "setAspects");

    /** The name of the element containing the aspect properties. */
    public static final QName PROPERTIES = new QName(NAMESPACE, "properties");

    /** The name of the element containing the aspects to add. */
    private static final QName ASPECTS_TO_ADD = new QName(NAMESPACE, "aspectsToAdd");

    /** The name of the element containing the aspects to remove. */
    private static final QName ASPECTS_TO_REMOVE = new QName(NAMESPACE, "aspectsToRemove");


    /**
     * The Constructor.
     * 
     * @param internal
     *            the internal element
     */
    public SetAspectsExtension(Element internal)
    {
        super(internal);
    }

    /**
     * The Constructor.
     * 
     * @param factory
     *            the factory
     */
    public SetAspectsExtension(Factory factory)
    {
        super(factory, CMISConstants.OBJECT);
    }

    /**
     * Gets the aspects to add.
     * 
     * @return the aspects to add
     */
    public Set<String> getAspectsToAdd()
    {
        Set<String> aspects = new TreeSet<String>();
        for (Element aspect : this)
        {
            if (aspect.getQName().equals(ASPECTS_TO_ADD))
            {
                aspects.add(aspect.getText());
            }
        }
        return aspects;
    }

    /**
     * Gets the aspects to remove.
     * 
     * @return the aspects to remove
     */
    public Set<String> getAspectsToRemove()
    {
        Set<String> aspects = new TreeSet<String>();
        for (Element aspect : this)
        {
            if (aspect.getQName().equals(ASPECTS_TO_REMOVE))
            {
                aspects.add(aspect.getText());
            }
        }
        return aspects;
    }

    /**
     * Gets all aspect properties
     * 
     * @return aspect properties
     */
    public CMISProperties getProperties()
    {
        return (CMISProperties) getFirstChild(PROPERTIES);
    }
}
