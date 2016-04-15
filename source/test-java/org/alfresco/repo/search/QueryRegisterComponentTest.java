/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.search;

import junit.framework.TestCase;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.ApplicationContextHelper;
import org.junit.experimental.categories.Category;
import org.springframework.context.ApplicationContext;

@Category(OwnJVMTestsCategory.class)
public class QueryRegisterComponentTest extends TestCase
{
    static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    private DictionaryService dictionaryService;
    private NamespaceService namespaceService;

    public QueryRegisterComponentTest()
    {
        super();
    }
    
    public QueryRegisterComponentTest(String arg0)
    {
        super(arg0);
    }
    
    public void setUp()
    {
      
        dictionaryService = (DictionaryService) ctx.getBean("dictionaryService");
        namespaceService = (NamespaceService) ctx.getBean("namespaceService");
       
    }

    public void testLoad()
    {
        QueryRegisterComponentImpl qr = new QueryRegisterComponentImpl();
        qr.setNamespaceService(namespaceService);
        qr.setDictionaryService(dictionaryService);
        qr.loadQueryCollection("testQueryRegister.xml");
        
        assertNotNull(qr.getQueryDefinition(QName.createQName("alf", "query1", namespaceService)));
        assertEquals("lucene", qr.getQueryDefinition(QName.createQName("alf", "query1", namespaceService)).getLanguage());
        assertEquals("http://www.trees.tulip/barking/woof", qr.getQueryDefinition(QName.createQName("alf", "query1", namespaceService)).getNamespacePrefixResolver().getNamespaceURI("tulip"));
        assertEquals("+QNAME:$alf:query-parameter-name", qr.getQueryDefinition(QName.createQName("alf", "query1", namespaceService)).getQuery());
        assertEquals(2, qr.getQueryDefinition(QName.createQName("alf", "query1", namespaceService)).getQueryParameterDefs().size());
    }

}
