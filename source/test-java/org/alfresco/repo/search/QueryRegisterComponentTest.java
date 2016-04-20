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
