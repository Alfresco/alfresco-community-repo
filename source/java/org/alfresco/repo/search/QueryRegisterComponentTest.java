/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.search;

import junit.framework.TestCase;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

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
