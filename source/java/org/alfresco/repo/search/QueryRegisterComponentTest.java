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
