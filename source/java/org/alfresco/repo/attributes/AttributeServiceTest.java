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
package org.alfresco.repo.attributes;

import java.io.Serializable;

import junit.framework.TestCase;

import org.alfresco.service.cmr.attributes.AttributeService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

/**
 * {@link AttributeService}
 * 
 * @author Derek Hulley
 */
public class AttributeServiceTest extends TestCase
{
    private static final Serializable[] KEY_AAA = new Serializable[] {"a", "a", "a"};
    private static final Serializable[] KEY_AAB = new Serializable[] {"a", "a", "b"};
    private static final Serializable[] KEY_AAC = new Serializable[] {"a", "a", "c"};
    private static final Serializable VALUE_AAA_STRING = "aaa";
    private static final Serializable VALUE_AAB_STRING = "aab";
    private static final Serializable VALUE_AAC_STRING = "aac";
    
    private ApplicationContext ctx;
    private AttributeService attributeService;
    
    @Override
    protected void setUp() throws Exception
    {
        ctx = ApplicationContextHelper.getApplicationContext();
        attributeService = (AttributeService) ctx.getBean("AttributeService");
    }

    @Override
    protected void tearDown() throws Exception
    {
    }
    
    public void testBasic() throws Exception
    {
        attributeService.removeAttribute(KEY_AAA);
        attributeService.removeAttribute(KEY_AAB);
        attributeService.removeAttribute(KEY_AAC);
        
        assertFalse(attributeService.exists(KEY_AAA));
        assertFalse(attributeService.exists(KEY_AAB));
        assertFalse(attributeService.exists(KEY_AAC));
        
        attributeService.setAttribute(VALUE_AAA_STRING, KEY_AAA);
        attributeService.setAttribute(VALUE_AAB_STRING, KEY_AAB);
        attributeService.setAttribute(VALUE_AAC_STRING, KEY_AAC);

        assertTrue(attributeService.exists(KEY_AAA));
        assertTrue(attributeService.exists(KEY_AAB));
        assertTrue(attributeService.exists(KEY_AAC));
        
        assertEquals(VALUE_AAA_STRING, attributeService.getAttribute(KEY_AAA));
        assertEquals(VALUE_AAB_STRING, attributeService.getAttribute(KEY_AAB));
        assertEquals(VALUE_AAC_STRING, attributeService.getAttribute(KEY_AAC));
//        
//        attributeService.removeAttribute(KEY_AAA);
//        attributeService.removeAttribute(KEY_AAB);
//        attributeService.removeAttribute(KEY_AAC);
    }
}
