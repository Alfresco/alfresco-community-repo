/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
package org.apache.abdera.ext.cmis;

import java.math.BigDecimal;
import java.util.Date;

import javax.xml.namespace.QName;

import org.apache.abdera.factory.Factory;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.ElementWrapper;


/**
 * CMIS Property for the Abdera ATOM library.
 * 
 * Encapsulates access and modification of CMIS extension values to CMIS
 * Property.
 * 
 * NOTE: Potentially, this extension can be contributed to Abdera upon
 *       publication of CMIS.  This is why it is organised under a
 *       non-Alfresco Java package.  It follows the conventions of all
 *       other Abdera extensions.
 * 
 * @author davidc
 */
public abstract class CMISProperty extends ElementWrapper
{
    /**
     * @param internal
     */
    public CMISProperty(Element internal)
    {
        super(internal);
    }

    /**
     * @param factory
     * @param qname
     */
    public CMISProperty(Factory factory, QName qname)
    {
        super(factory, qname);
    }

    /**
     * Gets the property type
     * 
     * @return  type
     */
    public abstract String getType();

    /**
     * Gets the property name
     * 
     * @return  name
     */
    public String getName()
    {
        return getAttributeValue(CMISConstants.PROPERTY_NAME);
    }
    
    /**
     * Is property value null?
     * 
     * @return  true => null
     */
    public boolean isNull()
    {
        return getFirstChild(CMISConstants.PROPERTY_VALUE) == null ? true : false;
    }
    
    /**
     * Gets property value (as String)
     * 
     * @return  property value (or null, if not specified)
     */
    public String getValue()
    {
        Element child = getFirstChild(CMISConstants.PROPERTY_VALUE);
        if (child != null)
        {
            return child.getText();
        }
        return null;
    }

    /**
     * Gets String value
     * 
     * @return  string value
     */
    public String getStringValue()
    {
        return getValue();
    }

    /**
     * Gets Decimal value
     * 
     * @return  decimal value
     */
    public BigDecimal getDecimalValue()
    {
        return new BigDecimal(getValue());
    }

    /**
     * Gets Integer value
     * 
     * @return  integer value
     */
    public int getIntegerValue()
    {
        return new Integer(getValue());
    }

    /**
     * Gets Boolean value
     * 
     * @return  boolean value
     */
    public boolean getBooleanValue()
    {
        return Boolean.valueOf(getValue());
    }

    /**
     * Gets Date value
     * 
     * @return  date value
     */
    public Date getDateValue()
    {
        // TODO: 
        return null;
    }

    
    /**
     * String Property 
     */
    public static class CMISPropertyString extends CMISProperty
    {
        public CMISPropertyString(Element internal)
        {
            super(internal);
        }

        public CMISPropertyString(Factory factory, QName qname)
        {
            super(factory, qname);
        }

        /* (non-Javadoc)
         * @see org.apache.abdera.ext.cmis.CMISProperty#getType()
         */
        @Override
        public String getType()
        {
            return CMISConstants.PROP_TYPE_STRING;
        }
    }

    /**
     * Decimal Property
     */
    public static class CMISPropertyDecimal extends CMISProperty
    {
        public CMISPropertyDecimal(Element internal)
        {
            super(internal);
        }

        public CMISPropertyDecimal(Factory factory, QName qname)
        {
            super(factory, qname);
        }

        /* (non-Javadoc)
         * @see org.apache.abdera.ext.cmis.CMISProperty#getType()
         */
        @Override
        public String getType()
        {
            return CMISConstants.PROP_TYPE_DECIMAL;
        }
    }

    /**
     * Integer Property
     */
    public static class CMISPropertyInteger extends CMISProperty
    {
        public CMISPropertyInteger(Element internal)
        {
            super(internal);
        }

        public CMISPropertyInteger(Factory factory, QName qname)
        {
            super(factory, qname);
        }

        /* (non-Javadoc)
         * @see org.apache.abdera.ext.cmis.CMISProperty#getType()
         */
        @Override
        public String getType()
        {
            return CMISConstants.PROP_TYPE_INTEGER;
        }
    }

    /**
     * Boolean Property
     */
    public static class CMISPropertyBoolean extends CMISProperty
    {
        public CMISPropertyBoolean(Element internal)
        {
            super(internal);
        }

        public CMISPropertyBoolean(Factory factory, QName qname)
        {
            super(factory, qname);
        }

        /* (non-Javadoc)
         * @see org.apache.abdera.ext.cmis.CMISProperty#getType()
         */
        @Override
        public String getType()
        {
            return CMISConstants.PROP_TYPE_BOOLEAN;
        }
    }

    /**
     * DateTime Property
     */
    public static class CMISPropertyDateTime extends CMISProperty
    {
        public CMISPropertyDateTime(Element internal)
        {
            super(internal);
        }

        public CMISPropertyDateTime(Factory factory, QName qname)
        {
            super(factory, qname);
        }

        /* (non-Javadoc)
         * @see org.apache.abdera.ext.cmis.CMISProperty#getType()
         */
        @Override
        public String getType()
        {
            return CMISConstants.PROP_TYPE_DATETIME;
        }
    }

    /**
     * URI Property
     */
    public static class CMISPropertyUri extends CMISPropertyString
    {
        public CMISPropertyUri(Element internal)
        {
            super(internal);
        }

        public CMISPropertyUri(Factory factory, QName qname)
        {
            super(factory, qname);
        }

        /* (non-Javadoc)
         * @see org.apache.abdera.ext.cmis.CMISProperty.CMISPropertyString#getType()
         */
        @Override
        public String getType()
        {
            return CMISConstants.PROP_TYPE_URI;
        }
    }

    /**
     * ID Property
     */
    public static class CMISPropertyId extends CMISPropertyString
    {
        public CMISPropertyId(Element internal)
        {
            super(internal);
        }

        public CMISPropertyId(Factory factory, QName qname)
        {
            super(factory, qname);
        }

        /* (non-Javadoc)
         * @see org.apache.abdera.ext.cmis.CMISProperty.CMISPropertyString#getType()
         */
        @Override
        public String getType()
        {
            return CMISConstants.PROP_TYPE_ID;
        }
    }

    /**
     * XML Property
     */
    public static class CMISPropertyXml extends CMISPropertyString
    {
        public CMISPropertyXml(Element internal)
        {
            super(internal);
        }

        public CMISPropertyXml(Factory factory, QName qname)
        {
            super(factory, qname);
        }

        /* (non-Javadoc)
         * @see org.apache.abdera.ext.cmis.CMISProperty.CMISPropertyString#getType()
         */
        @Override
        public String getType()
        {
            return CMISConstants.PROP_TYPE_XML;
        }
    }

    /**
     * HTML Property
     */
    public static class CMISPropertyHtml extends CMISPropertyString
    {
        public CMISPropertyHtml(Element internal)
        {
            super(internal);
        }

        public CMISPropertyHtml(Factory factory, QName qname)
        {
            super(factory, qname);
        }

        /* (non-Javadoc)
         * @see org.apache.abdera.ext.cmis.CMISProperty.CMISPropertyString#getType()
         */
        @Override
        public String getType()
        {
            return CMISConstants.PROP_TYPE_HTML;
        }
    }
}
