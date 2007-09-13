package org.alfresco.repo.dictionary;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.cache.EhCacheAdapter;
import org.alfresco.repo.dictionary.M2ModelDiff;
import org.alfresco.repo.tenant.SingleTServiceImpl;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.namespace.QName;

public class DiffModelTest extends TestCase
{
    public static final String MODEL1_XML = 
        "<model name=\"test1:model1\" xmlns=\"http://www.alfresco.org/model/dictionary/1.0\">" +
        
        "   <description>Another description</description>" +
        "   <author>Alfresco</author>" +
        "   <published>2007-08-01</published>" +
        "   <version>1.0</version>" +
        
        "   <imports>" +
        "      <import uri=\"http://www.alfresco.org/model/dictionary/1.0\" prefix=\"d\"/>" +
        "   </imports>" +
        
        "   <namespaces>" +
        "      <namespace uri=\"http://www.alfresco.org/model/test1/1.0\" prefix=\"test1\"/>" +
        "   </namespaces>" +

        "   <types>" +
       
        "      <type name=\"test1:type1\">" +
        "        <title>Base</title>" +
        "        <description>The Base Type 1</description>" +
        "        <properties>" +
        "           <property name=\"test1:prop1\">" +
        "              <type>d:text</type>" +
        "           </property>" +
        "           <property name=\"test1:prop2\">" +
        "              <type>d:int</type>" +
        "           </property>" +        
        "        </properties>" +
        "      </type>" +
        
        "      <type name=\"test1:type2\">" +
        "        <title>Base</title>" +
        "        <description>The Base Type 2</description>" +
        "        <properties>" +
        "           <property name=\"test1:prop3\">" +
        "              <type>d:text</type>" +
        "           </property>" +
        "           <property name=\"test1:prop4\">" +
        "              <type>d:int</type>" +
        "           </property>" +          
        "        </properties>" +
        "      </type>" +
        
        "      <type name=\"test1:type3\">" +
        "        <title>Base</title>" +
        "        <description>The Base Type 3</description>" +
        "        <properties>" +
        "           <property name=\"test1:prop5\">" +
        "              <type>d:text</type>" +
        "           </property>" +
        "           <property name=\"test1:prop6\">" +
        "              <type>d:int</type>" +
        "           </property>" +          
        "        </properties>" +
        "      </type>" +
        
        "   </types>" +
        
        "   <aspects>" +
        
        "      <aspect name=\"test1:aspect1\">" +
        "        <title>Base</title>" +
        "        <description>The Base Aspect 1</description>" +
        "        <properties>" +
        "           <property name=\"test1:prop9\">" +
        "              <type>d:text</type>" +
        "           </property>" +
        "           <property name=\"test1:prop10\">" +
        "              <type>d:int</type>" +
        "           </property>" +        
        "        </properties>" +
        "      </aspect>" +
        
        "      <aspect name=\"test1:aspect2\">" +
        "        <title>Base</title>" +
        "        <description>The Base Aspect 2</description>" +
        "        <properties>" +
        "           <property name=\"test1:prop11\">" +
        "              <type>d:text</type>" +
        "           </property>" +
        "           <property name=\"test1:prop12\">" +
        "              <type>d:int</type>" +
        "           </property>" +        
        "        </properties>" +
        "      </aspect>" +
        
        "      <aspect name=\"test1:aspect3\">" +
        "        <title>Base</title>" +
        "        <description>The Base Aspect 3</description>" +
        "        <properties>" +
        "           <property name=\"test1:prop13\">" +
        "              <type>d:text</type>" +
        "           </property>" +
        "           <property name=\"test1:prop14\">" +
        "              <type>d:int</type>" +
        "           </property>" +        
        "        </properties>" +
        "      </aspect>" +
              
        "   </aspects>" +        
        
        "</model>";
    
    public static final String MODEL1_UPDATE1_XML = 
        "<model name=\"test1:model1\" xmlns=\"http://www.alfresco.org/model/dictionary/1.0\">" +
        
        "   <description>Another description</description>" +
        "   <author>Alfresco</author>" +
        "   <published>2007-08-01</published>" +
        "   <version>1.0</version>" +
        
        "   <imports>" +
        "      <import uri=\"http://www.alfresco.org/model/dictionary/1.0\" prefix=\"d\"/>" +
        "   </imports>" +
        
        "   <namespaces>" +
        "      <namespace uri=\"http://www.alfresco.org/model/test1/1.0\" prefix=\"test1\"/>" +
        "   </namespaces>" +

        "   <types>" +
       
        "      <type name=\"test1:type1\">" +
        "        <title>Base</title>" +
        "        <description>The Base Type 1</description>" +
        "        <properties>" +
        "           <property name=\"test1:prop1\">" +
        "              <type>d:text</type>" +
        "           </property>" +
        "           <property name=\"test1:prop2\">" +
        "              <type>d:int</type>" +
        "           </property>" +        
        "        </properties>" +
        "      </type>" +
        
        "      <type name=\"test1:type3\">" +
        "        <title>Base</title>" +
        "        <description>The Base Type 3</description>" +
        "        <properties>" +
        "           <property name=\"test1:prop5\">" +
        "              <type>d:text</type>" +
        "           </property>" +   
        "        </properties>" +
        "      </type>" +
        
        "      <type name=\"test1:type4\">" +
        "        <title>Base</title>" +
        "        <description>The Base Type 4</description>" +
        "        <properties>" +
        "           <property name=\"test1:prop7\">" +
        "              <type>d:text</type>" +
        "           </property>" +
        "           <property name=\"test1:prop8\">" +
        "              <type>d:int</type>" +
        "           </property>" +          
        "        </properties>" +
        "      </type>" +
        
        "   </types>" +
        
        "   <aspects>" +
        
        "      <aspect name=\"test1:aspect1\">" +
        "        <title>Base</title>" +
        "        <description>The Base Aspect 1</description>" +
        "        <properties>" +
        "           <property name=\"test1:prop9\">" +
        "              <type>d:text</type>" +
        "           </property>" +
        "           <property name=\"test1:prop10\">" +
        "              <type>d:int</type>" +
        "           </property>" +        
        "        </properties>" +
        "      </aspect>" +
        
        "      <aspect name=\"test1:aspect3\">" +
        "        <title>Base</title>" +
        "        <description>The Base Aspect 3</description>" +
        "        <properties>" +
        "           <property name=\"test1:prop13\">" +
        "              <type>d:int</type>" +
        "           </property>" + 
        "           <property name=\"test1:prop14\">" +
        "              <type>d:int</type>" +
        "           </property>" + 
        "        </properties>" +
        "      </aspect>" +
        
        "      <aspect name=\"test1:aspect4\">" +
        "        <title>Base</title>" +
        "        <description>The Base Aspect 4</description>" +
        "        <properties>" +
        "           <property name=\"test1:prop15\">" +
        "              <type>d:text</type>" +
        "           </property>" +
        "           <property name=\"test1:prop16\">" +
        "              <type>d:int</type>" +
        "           </property>" +        
        "        </properties>" +
        "      </aspect>" +    
        
        "   </aspects>" +        
        
        "</model>";
    
    public static final String MODEL2_XML = 
        "<model name=\"test1:model2\" xmlns=\"http://www.alfresco.org/model/dictionary/1.0\">" +
        
        "   <description>Another description</description>" +
        "   <author>Alfresco</author>" +
        "   <published>2007-08-01</published>" +
        "   <version>1.0</version>" +
        
        "   <imports>" +
        "      <import uri=\"http://www.alfresco.org/model/dictionary/1.0\" prefix=\"d\"/>" +
        "   </imports>" +
        
        "   <namespaces>" +
        "      <namespace uri=\"http://www.alfresco.org/model/test1/1.0\" prefix=\"test1\"/>" +
        "   </namespaces>" +

        "   <types>" +
       
        "      <type name=\"test1:type1\">" +
        "        <title>Base</title>" +
        "        <description>The Base Type 1</description>" +
        "        <properties>" +
        "           <property name=\"test1:prop1\">" +
        "              <type>d:text</type>" +
        "           </property>" +
        "           <property name=\"test1:prop2\">" +
        "              <type>d:int</type>" +
        "           </property>" +        
        "        </properties>" +
        "      </type>" +
        
        "   </types>" +
        
        "   <aspects>" +
        
        "      <aspect name=\"test1:aspect1\">" +
        "        <title>Base</title>" +
        "        <description>The Base Aspect 1</description>" +
        "        <properties>" +
        "           <property name=\"test1:prop9\">" +
        "              <type>d:text</type>" +
        "           </property>" +
        "           <property name=\"test1:prop10\">" +
        "              <type>d:int</type>" +
        "           </property>" +        
        "        </properties>" +
        "      </aspect>" +
        
        "   </aspects>" +
        
        "</model>";
    
    public static final String MODEL2_EXTRA_PROPERTIES_XML = 
        "<model name=\"test1:model2\" xmlns=\"http://www.alfresco.org/model/dictionary/1.0\">" +
        
        "   <description>Another description</description>" +
        "   <author>Alfresco</author>" +
        "   <published>2007-08-01</published>" +
        "   <version>1.0</version>" +
        
        "   <imports>" +
        "      <import uri=\"http://www.alfresco.org/model/dictionary/1.0\" prefix=\"d\"/>" +
        "   </imports>" +
        
        "   <namespaces>" +
        "      <namespace uri=\"http://www.alfresco.org/model/test1/1.0\" prefix=\"test1\"/>" +
        "   </namespaces>" +

        "   <types>" +
       
        "      <type name=\"test1:type1\">" +
        "        <title>Base</title>" +
        "        <description>The Base Type 1</description>" +
        "        <properties>" +
        "           <property name=\"test1:prop1\">" +
        "              <type>d:text</type>" +
        "           </property>" +
        "           <property name=\"test1:prop2\">" +
        "              <type>d:int</type>" +
        "           </property>" +       
        "           <property name=\"test1:prop3\">" +
        "              <type>d:date</type>" +
        "           </property>" +   
        "        </properties>" +
        "      </type>" +
        
        "   </types>" +
        
        "   <aspects>" +
        
        "      <aspect name=\"test1:aspect1\">" +
        "        <title>Base</title>" +
        "        <description>The Base Aspect 1</description>" +
        "        <properties>" +
        "           <property name=\"test1:prop11\">" +
        "              <type>d:boolean</type>" +
        "           </property>" +         
        "           <property name=\"test1:prop9\">" +
        "              <type>d:text</type>" +
        "           </property>" +
        "           <property name=\"test1:prop10\">" +
        "              <type>d:int</type>" +
        "           </property>" +        
        "        </properties>" +
        "      </aspect>" +
        
        "   </aspects>" +
        
        "</model>";
    
    public static final String MODEL3_XML = 
        "<model name=\"test1:model3\" xmlns=\"http://www.alfresco.org/model/dictionary/1.0\">" +
        
        "   <description>Another description</description>" +
        "   <author>Alfresco</author>" +
        "   <published>2007-08-01</published>" +
        "   <version>1.0</version>" +
        
        "   <imports>" +
        "      <import uri=\"http://www.alfresco.org/model/dictionary/1.0\" prefix=\"d\"/>" +
        "   </imports>" +
        
        "   <namespaces>" +
        "      <namespace uri=\"http://www.alfresco.org/model/test1/1.0\" prefix=\"test1\"/>" +
        "   </namespaces>" +

        "   <types>" +
       
        "      <type name=\"test1:type1\">" +
        "        <title>Base</title>" +
        "        <description>The Base Type 1</description>" +
        "        <properties>" +
        "           <property name=\"test1:prop1\">" +
        "              <type>d:text</type>" +
        "           </property>" +
        "           <property name=\"test1:prop2\">" +
        "              <type>d:int</type>" +
        "           </property>" +        
        "        </properties>" +
        "      </type>" +
        
        "   </types>" +
        
        "   <aspects>" +
        
        "      <aspect name=\"test1:aspect1\">" +
        "        <title>Base</title>" +
        "        <description>The Base Aspect 1</description>" +
        "        <properties>" +
        "           <property name=\"test1:prop9\">" +
        "              <type>d:text</type>" +
        "           </property>" +
        "           <property name=\"test1:prop10\">" +
        "              <type>d:int</type>" +
        "           </property>" +        
        "        </properties>" +
        "      </aspect>" +
        
        "   </aspects>" +
        
        "</model>";
    
    public static final String MODEL3_EXTRA_TYPES_AND_ASPECTS_XML = 
        "<model name=\"test1:model3\" xmlns=\"http://www.alfresco.org/model/dictionary/1.0\">" +
        
        "   <description>Another description</description>" +
        "   <author>Alfresco</author>" +
        "   <published>2007-08-01</published>" +
        "   <version>1.0</version>" +
        
        "   <imports>" +
        "      <import uri=\"http://www.alfresco.org/model/dictionary/1.0\" prefix=\"d\"/>" +
        "   </imports>" +
        
        "   <namespaces>" +
        "      <namespace uri=\"http://www.alfresco.org/model/test1/1.0\" prefix=\"test1\"/>" +
        "   </namespaces>" +

        "   <types>" +
       
        "      <type name=\"test1:type1\">" +
        "        <title>Base</title>" +
        "        <description>The Base Type 1</description>" +
        "        <properties>" +
        "           <property name=\"test1:prop1\">" +
        "              <type>d:text</type>" +
        "           </property>" +
        "           <property name=\"test1:prop2\">" +
        "              <type>d:int</type>" +
        "           </property>" +       
        "        </properties>" +
        "      </type>" +
        
        "      <type name=\"test1:type2\">" +
        "        <title>Base</title>" +
        "        <description>The Base Type 2</description>" +
        "        <properties>" +
        "           <property name=\"test1:prop3\">" +
        "              <type>d:text</type>" +
        "           </property>" +
        "           <property name=\"test1:prop4\">" +
        "              <type>d:int</type>" +
        "           </property>" +          
        "        </properties>" +
        "      </type>" +
        
        "   </types>" +
        
        "   <aspects>" +
        
        "      <aspect name=\"test1:aspect1\">" +
        "        <title>Base</title>" +
        "        <description>The Base Aspect 1</description>" +
        "        <properties>" +     
        "           <property name=\"test1:prop9\">" +
        "              <type>d:text</type>" +
        "           </property>" +
        "           <property name=\"test1:prop10\">" +
        "              <type>d:int</type>" +
        "           </property>" +        
        "        </properties>" +
        "      </aspect>" +
        
        "      <aspect name=\"test1:aspect2\">" +
        "        <title>Base</title>" +
        "        <description>The Base Aspect 2</description>" +
        "        <properties>" +
        "           <property name=\"test1:prop11\">" +
        "              <type>d:text</type>" +
        "           </property>" +
        "           <property name=\"test1:prop12\">" +
        "              <type>d:int</type>" +
        "           </property>" +        
        "        </properties>" +
        "      </aspect>" +
        
        "   </aspects>" +
        
        "</model>";
    
    public static final String MODEL4_XML = 
        "<model name=\"test1:model4\" xmlns=\"http://www.alfresco.org/model/dictionary/1.0\">" +
        
        "   <description>Another description</description>" +
        "   <author>Alfresco</author>" +
        "   <published>2007-08-01</published>" +
        "   <version>1.0</version>" +
        
        "   <imports>" +
        "      <import uri=\"http://www.alfresco.org/model/dictionary/1.0\" prefix=\"d\"/>" +
        "   </imports>" +
        
        "   <namespaces>" +
        "      <namespace uri=\"http://www.alfresco.org/model/test1/1.0\" prefix=\"test1\"/>" +
        "   </namespaces>" +

        "   <types>" +
       
        "      <type name=\"test1:type1\">" +
        "        <title>Base</title>" +
        "        <description>The Base Type 1</description>" +
        "        <properties>" +
        "           <property name=\"test1:prop1\">" +
        "              <type>d:text</type>" +
        "           </property>" +
        "        </properties>" +
        "      </type>" +
        
        "   </types>" +
        
        "   <aspects>" +
        
        "      <aspect name=\"test1:aspect1\">" +
        "        <title>Base</title>" +
        "        <description>The Base Aspect 1</description>" +
        "        <properties>" +
        "           <property name=\"test1:prop9\">" +
        "              <type>d:text</type>" +
        "           </property>" +     
        "        </properties>" +
        "      </aspect>" +
        
        "   </aspects>" +
        
        "</model>";
    
    public static final String MODEL4_EXTRA_DEFAULT_ASPECT_XML = 
        "<model name=\"test1:model4\" xmlns=\"http://www.alfresco.org/model/dictionary/1.0\">" +
        
        "   <description>Another description</description>" +
        "   <author>Alfresco</author>" +
        "   <published>2007-08-01</published>" +
        "   <version>1.0</version>" +
        
        "   <imports>" +
        "      <import uri=\"http://www.alfresco.org/model/dictionary/1.0\" prefix=\"d\"/>" +
        "   </imports>" +
        
        "   <namespaces>" +
        "      <namespace uri=\"http://www.alfresco.org/model/test1/1.0\" prefix=\"test1\"/>" +
        "   </namespaces>" +

        "   <types>" +
       
        "      <type name=\"test1:type1\">" +
        "        <title>Base</title>" +
        "        <description>The Base Type 1</description>" +
        "        <properties>" +
        "           <property name=\"test1:prop1\">" +
        "              <type>d:text</type>" +
        "           </property>" +  
        "        </properties>" +
        "        <mandatory-aspects>" +
        "           <aspect>test1:aspect1</aspect>" +
        "        </mandatory-aspects>" +
        "      </type>" +
        
        "   </types>" +
        
        "   <aspects>" +
        
        "      <aspect name=\"test1:aspect1\">" +
        "        <title>Base</title>" +
        "        <description>The Base Aspect 1</description>" +
        "        <properties>" +     
        "           <property name=\"test1:prop9\">" +
        "              <type>d:text</type>" +
        "           </property>" +     
        "        </properties>" +
        "      </aspect>" +
  
        "   </aspects>" +
        
        "</model>";
    
    public static final String MODEL5_XML = 
        "<model name=\"test1:model5\" xmlns=\"http://www.alfresco.org/model/dictionary/1.0\">" +
        
        "   <description>Another description</description>" +
        "   <author>Alfresco</author>" +
        "   <published>2007-08-01</published>" +
        "   <version>1.0</version>" +
        
        "   <imports>" +
        "      <import uri=\"http://www.alfresco.org/model/dictionary/1.0\" prefix=\"d\"/>" +
        "   </imports>" +
        
        "   <namespaces>" +
        "      <namespace uri=\"http://www.alfresco.org/model/test1/1.0\" prefix=\"test1\"/>" +
        "   </namespaces>" +

        "   <types>" +
       
        "      <type name=\"test1:type1\">" +
        "        <title>Base</title>" +
        "        <description>The Base Type 1</description>" +
        "        <properties>" +
        "           <property name=\"test1:prop1\">" +
        "              <type>d:text</type>" +
        "           </property>" +
        "        </properties>" +
        "      </type>" +
        
        "      <type name=\"test1:type2\">" +
        "        <title>Base</title>" +
        "        <description>The Base Type 2</description>" +
        "        <properties>" +
        "           <property name=\"test1:prop3\">" +
        "              <type>d:text</type>" +
        "           </property>" +
        "           <property name=\"test1:prop4\">" +
        "              <type>d:int</type>" +
        "           </property>" +          
        "        </properties>" +
        "      </type>" +
        
        "   </types>" +
        
        "   <aspects>" +
        
        "      <aspect name=\"test1:aspect1\">" +
        "        <title>Base</title>" +
        "        <description>The Base Aspect 1</description>" +
        "        <properties>" +
        "           <property name=\"test1:prop9\">" +
        "              <type>d:text</type>" +
        "           </property>" +     
        "        </properties>" +
        "      </aspect>" +
        
        "      <aspect name=\"test1:aspect2\">" +
        "        <title>Base</title>" +
        "        <description>The Base Aspect 2</description>" +
        "        <properties>" +
        "           <property name=\"test1:prop11\">" +
        "              <type>d:text</type>" +
        "           </property>" +
        "           <property name=\"test1:prop12\">" +
        "              <type>d:int</type>" +
        "           </property>" +        
        "        </properties>" +
        "      </aspect>" +
        
        "   </aspects>" +
        
        "</model>";
    
    public static final String MODEL5_EXTRA_ASSOCIATIONS_XML =
        "<model name=\"test1:model5\" xmlns=\"http://www.alfresco.org/model/dictionary/1.0\">" +
        
        "   <description>Another description</description>" +
        "   <author>Alfresco</author>" +
        "   <published>2007-08-01</published>" +
        "   <version>1.0</version>" +
        
        "   <imports>" +
        "      <import uri=\"http://www.alfresco.org/model/dictionary/1.0\" prefix=\"d\"/>" +
        "   </imports>" +
        
        "   <namespaces>" +
        "      <namespace uri=\"http://www.alfresco.org/model/test1/1.0\" prefix=\"test1\"/>" +
        "   </namespaces>" +

        "   <types>" +
       
        "      <type name=\"test1:type1\">" +
        "        <title>Base</title>" +
        "        <description>The Base Type 1</description>" +
        "        <properties>" +
        "           <property name=\"test1:prop1\">" +
        "              <type>d:text</type>" +
        "           </property>" +  
        "        </properties>" +
        "        <associations>" +
        "           <child-association name=\"test1:assoc1\">" +
        "               <source>" +
        "                   <mandatory>false</mandatory>" +
        "                   <many>false</many>" +
        "               </source>" +
        "               <target>" +
        "                   <class>test1:type2</class>" +
        "                   <mandatory>false</mandatory>" +
        "                   <many>false</many>" +
        "               </target>" +
        "           </child-association>" +
        "        </associations>" +
        "      </type>" +
        
        "      <type name=\"test1:type2\">" +
        "        <title>Base</title>" +
        "        <description>The Base Type 2</description>" +
        "        <properties>" +
        "           <property name=\"test1:prop3\">" +
        "              <type>d:text</type>" +
        "           </property>" +
        "           <property name=\"test1:prop4\">" +
        "              <type>d:int</type>" +
        "           </property>" +          
        "        </properties>" +
        "      </type>" +
        
        "   </types>" +
        
        "   <aspects>" +
        
        "      <aspect name=\"test1:aspect1\">" +
        "        <title>Base</title>" +
        "        <description>The Base Aspect 1</description>" +
        "        <properties>" +     
        "           <property name=\"test1:prop9\">" +
        "              <type>d:text</type>" +
        "           </property>" +     
        "        </properties>" +
        "        <associations>" +
        "           <association name=\"test1:assoc2\">" +
        "               <source>" +
        "                   <role>test1:role1</role>" +
        "                   <mandatory>false</mandatory>" +
        "                   <many>true</many>" +
        "               </source>" +
        "               <target>" +
        "                   <class>test1:aspect2</class>" +
        "                   <role>test1:role2</role>" +
        "                   <mandatory>false</mandatory>" +
        "                   <many>true</many>" +
        "               </target>" +
        "           </association>" +
        "        </associations>" +
        "      </aspect>" +
  
        "      <aspect name=\"test1:aspect2\">" +
        "        <title>Base</title>" +
        "        <description>The Base Aspect 2</description>" +
        "        <properties>" +
        "           <property name=\"test1:prop11\">" +
        "              <type>d:text</type>" +
        "           </property>" +
        "           <property name=\"test1:prop12\">" +
        "              <type>d:int</type>" +
        "           </property>" +        
        "        </properties>" +
        "      </aspect>" +
        
        "   </aspects>" +
        
        "</model>";
    
    private DictionaryDAOImpl dictionaryDAO;

    /**
     * Setup
     */
    protected void setUp() throws Exception
    {
    	// Initialise the Dictionary
        TenantService tenantService = new SingleTServiceImpl();
        
        NamespaceDAOImpl namespaceDAO = new NamespaceDAOImpl();
        namespaceDAO.setTenantService(tenantService);
        
        initNamespaceCaches(namespaceDAO);
        
        dictionaryDAO = new DictionaryDAOImpl(namespaceDAO);
        dictionaryDAO.setTenantService(tenantService);
        
        initDictionaryCaches(dictionaryDAO);
        
        
        // include Alfresco dictionary model
        List<String> bootstrapModels = new ArrayList<String>();
        bootstrapModels.add("alfresco/model/dictionaryModel.xml");

        DictionaryBootstrap bootstrap = new DictionaryBootstrap();
        bootstrap.setModels(bootstrapModels);
        bootstrap.setDictionaryDAO(dictionaryDAO);
        bootstrap.bootstrap();
    }

    private void initDictionaryCaches(DictionaryDAOImpl dictionaryDAO)
    {
        CacheManager cacheManager = new CacheManager();
        
        Cache uriToModelsEhCache = new Cache("uriToModelsCache", 50, false, true, 0L, 0L);
        cacheManager.addCache(uriToModelsEhCache);      
        EhCacheAdapter<String, Map<String, List<CompiledModel>>> uriToModelsCache = new EhCacheAdapter<String, Map<String, List<CompiledModel>>>();
        uriToModelsCache.setCache(uriToModelsEhCache);
        
        dictionaryDAO.setUriToModelsCache(uriToModelsCache);
        
        Cache compileModelsEhCache = new Cache("compiledModelsCache", 50, false, true, 0L, 0L);
        cacheManager.addCache(compileModelsEhCache);
        EhCacheAdapter<String, Map<QName,CompiledModel>> compileModelCache = new EhCacheAdapter<String, Map<QName,CompiledModel>>();
        compileModelCache.setCache(compileModelsEhCache);
        
        dictionaryDAO.setCompiledModelsCache(compileModelCache);
    }
    
    private void initNamespaceCaches(NamespaceDAOImpl namespaceDAO)
    {
        CacheManager cacheManager = new CacheManager();
        
        Cache urisEhCache = new Cache("urisCache", 50, false, true, 0L, 0L);
        cacheManager.addCache(urisEhCache);      
        EhCacheAdapter<String, List<String>> urisCache = new EhCacheAdapter<String, List<String>>();
        urisCache.setCache(urisEhCache);
        
        namespaceDAO.setUrisCache(urisCache);
        
        Cache prefixesEhCache = new Cache("prefixesCache", 50, false, true, 0L, 0L);
        cacheManager.addCache(prefixesEhCache);
        EhCacheAdapter<String, Map<String, String>> prefixesCache = new EhCacheAdapter<String, Map<String, String>>();
        prefixesCache.setCache(prefixesEhCache);
        
        namespaceDAO.setPrefixesCache(prefixesCache);
    }
    
    public void testDeleteModel()
    {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(MODEL1_XML.getBytes());

        M2Model model = M2Model.createModel(byteArrayInputStream);
        QName modelName = dictionaryDAO.putModel(model);
        
        CompiledModel previousVersion = dictionaryDAO.getCompiledModels("").get(modelName);
        
        List<M2ModelDiff> modelDiffs = dictionaryDAO.diffModel(previousVersion, null);
        
        for (M2ModelDiff modelDiff : modelDiffs)
        {
            System.out.println(modelDiff.toString());
        }   
        
        assertEquals(6, modelDiffs.size());
        
        assertEquals(3, countDiffs(modelDiffs, M2ModelDiff.TYPE_TYPE, M2ModelDiff.DIFF_DELETED));
        assertEquals(3, countDiffs(modelDiffs, M2ModelDiff.TYPE_ASPECT, M2ModelDiff.DIFF_DELETED));
    }
    
    @SuppressWarnings("unused")
    public void testNoExistingModelToDelete()
    {
        try
        {
            List<M2ModelDiff> modelDiffs = dictionaryDAO.diffModel(null, null); 
            assertTrue("Should throw exeception that there is no previous version of the model to delete", true);
        }
        catch (AlfrescoRuntimeException e)
        {
            assertTrue("Wrong error message", e.getMessage().equals("Invalid arguments - no previous version of model to delete"));
        }
        
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(MODEL1_XML.getBytes());

        M2Model model = M2Model.createModel(byteArrayInputStream);
        QName modelName = dictionaryDAO.putModel(model);
        
        CompiledModel compiledModel = dictionaryDAO.getCompiledModels("").get(modelName);
        
        try
        {
            List<M2ModelDiff> modelDiffs = dictionaryDAO.diffModel(null, compiledModel);
            assertTrue("Should throw exeception that there is no previous version of the model to delete", true);
        }
        catch (AlfrescoRuntimeException e)
        {
            assertTrue("Wrong error message", e.getMessage().equals("Invalid arguments - no previous version of model to delete"));
        }
    }
    
    public void testNewModel()
    {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(MODEL1_XML.getBytes());

        M2Model model = M2Model.createModel(byteArrayInputStream);
        QName modelName = dictionaryDAO.putModel(model);
        
        CompiledModel newVersion = dictionaryDAO.getCompiledModels("").get(modelName);
        
        List<M2ModelDiff> modelDiffs = dictionaryDAO.diffModel(null, newVersion);
        
        for (M2ModelDiff modelDiff : modelDiffs)
        {
            System.out.println(modelDiff.toString());
        }   
        
        assertEquals(6, modelDiffs.size());
        
        assertEquals(3, countDiffs(modelDiffs, M2ModelDiff.TYPE_TYPE, M2ModelDiff.DIFF_CREATED));
        assertEquals(3, countDiffs(modelDiffs, M2ModelDiff.TYPE_ASPECT, M2ModelDiff.DIFF_CREATED));
    }
    
    public void testNonIncUpdateModel()
    {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(MODEL1_XML.getBytes());
        M2Model model = M2Model.createModel(byteArrayInputStream);
        QName modelName = dictionaryDAO.putModel(model);  
        CompiledModel previousVersion = dictionaryDAO.getCompiledModels("").get(modelName);
        
        byteArrayInputStream = new ByteArrayInputStream(MODEL1_UPDATE1_XML.getBytes());
        model = M2Model.createModel(byteArrayInputStream);
        modelName = dictionaryDAO.putModel(model);
        CompiledModel newVersion = dictionaryDAO.getCompiledModels("").get(modelName);       
        
        List<M2ModelDiff> modelDiffs = dictionaryDAO.diffModel(previousVersion, newVersion);
        
        for (M2ModelDiff M2ModelDiff : modelDiffs)
        {
            System.out.println(M2ModelDiff.toString());
        }   
        
        assertEquals(8, modelDiffs.size());
        
        assertEquals(1, countDiffs(modelDiffs, M2ModelDiff.TYPE_TYPE, M2ModelDiff.DIFF_CREATED));
        assertEquals(1, countDiffs(modelDiffs, M2ModelDiff.TYPE_TYPE, M2ModelDiff.DIFF_UNCHANGED));
        assertEquals(1, countDiffs(modelDiffs, M2ModelDiff.TYPE_TYPE, M2ModelDiff.DIFF_UPDATED));
        assertEquals(1, countDiffs(modelDiffs, M2ModelDiff.TYPE_TYPE, M2ModelDiff.DIFF_DELETED));
        
        assertEquals(1, countDiffs(modelDiffs, M2ModelDiff.TYPE_ASPECT, M2ModelDiff.DIFF_CREATED));
        assertEquals(1, countDiffs(modelDiffs, M2ModelDiff.TYPE_ASPECT, M2ModelDiff.DIFF_UNCHANGED));
        assertEquals(1, countDiffs(modelDiffs, M2ModelDiff.TYPE_ASPECT, M2ModelDiff.DIFF_UPDATED));
        assertEquals(1, countDiffs(modelDiffs, M2ModelDiff.TYPE_ASPECT, M2ModelDiff.DIFF_DELETED));
    }
    
    public void testIncUpdatePropertiesAdded()
    {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(MODEL2_XML.getBytes());
        M2Model model = M2Model.createModel(byteArrayInputStream);
        QName modelName = dictionaryDAO.putModel(model);  
        CompiledModel previousVersion = dictionaryDAO.getCompiledModels("").get(modelName);
        
        byteArrayInputStream = new ByteArrayInputStream(MODEL2_EXTRA_PROPERTIES_XML.getBytes());
        model = M2Model.createModel(byteArrayInputStream);
        modelName = dictionaryDAO.putModel(model);
        CompiledModel newVersion = dictionaryDAO.getCompiledModels("").get(modelName);       
        
        List<M2ModelDiff> modelDiffs = dictionaryDAO.diffModel(previousVersion, newVersion);
        
        for (M2ModelDiff modelDiff : modelDiffs)
        {
            System.out.println(modelDiff.toString());
        }   
        
        assertEquals(2, modelDiffs.size());
        
        assertEquals(1, countDiffs(modelDiffs, M2ModelDiff.TYPE_TYPE, M2ModelDiff.DIFF_UPDATED_INC));
        assertEquals(1, countDiffs(modelDiffs, M2ModelDiff.TYPE_ASPECT, M2ModelDiff.DIFF_UPDATED_INC));  
    }

    public void testIncUpdateTypesAndAspectsAdded()
    {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(MODEL3_XML.getBytes());
        M2Model model = M2Model.createModel(byteArrayInputStream);
        QName modelName = dictionaryDAO.putModel(model);  
        CompiledModel previousVersion = dictionaryDAO.getCompiledModels("").get(modelName);
        
        byteArrayInputStream = new ByteArrayInputStream(MODEL3_EXTRA_TYPES_AND_ASPECTS_XML.getBytes());
        model = M2Model.createModel(byteArrayInputStream);
        modelName = dictionaryDAO.putModel(model);
        CompiledModel newVersion = dictionaryDAO.getCompiledModels("").get(modelName);       
        
        List<M2ModelDiff> modelDiffs = dictionaryDAO.diffModel(previousVersion, newVersion);
        
        for (M2ModelDiff modelDiff : modelDiffs)
        {
            System.out.println(modelDiff.toString());
        }   
        
        assertEquals(4, modelDiffs.size());
        
        assertEquals(1, countDiffs(modelDiffs, M2ModelDiff.TYPE_TYPE, M2ModelDiff.DIFF_UNCHANGED));
        assertEquals(1, countDiffs(modelDiffs, M2ModelDiff.TYPE_ASPECT, M2ModelDiff.DIFF_UNCHANGED));  
        
        assertEquals(1, countDiffs(modelDiffs, M2ModelDiff.TYPE_TYPE, M2ModelDiff.DIFF_CREATED));
        assertEquals(1, countDiffs(modelDiffs, M2ModelDiff.TYPE_ASPECT, M2ModelDiff.DIFF_CREATED));
    }
    
    public void testIncUpdateAssociationsAdded()
    {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(MODEL5_XML.getBytes());
        M2Model model = M2Model.createModel(byteArrayInputStream);
        QName modelName = dictionaryDAO.putModel(model);  
        CompiledModel previousVersion = dictionaryDAO.getCompiledModels("").get(modelName);
        
        byteArrayInputStream = new ByteArrayInputStream(MODEL5_EXTRA_ASSOCIATIONS_XML.getBytes());
        model = M2Model.createModel(byteArrayInputStream);
        modelName = dictionaryDAO.putModel(model);
        CompiledModel newVersion = dictionaryDAO.getCompiledModels("").get(modelName);       
        
        List<M2ModelDiff> modelDiffs = dictionaryDAO.diffModel(previousVersion, newVersion);
        
        for (M2ModelDiff modelDiff : modelDiffs)
        {
            System.out.println(modelDiff.toString());
        }   
        
        assertEquals(4, modelDiffs.size());
        
        assertEquals(1, countDiffs(modelDiffs, M2ModelDiff.TYPE_TYPE, M2ModelDiff.DIFF_UPDATED_INC));
        assertEquals(1, countDiffs(modelDiffs, M2ModelDiff.TYPE_ASPECT, M2ModelDiff.DIFF_UPDATED_INC));  

        assertEquals(1, countDiffs(modelDiffs, M2ModelDiff.TYPE_TYPE, M2ModelDiff.DIFF_UNCHANGED));
        assertEquals(1, countDiffs(modelDiffs, M2ModelDiff.TYPE_ASPECT, M2ModelDiff.DIFF_UNCHANGED));  

    }
    
    public void testNonIncUpdatePropertiesRemoved()
    {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(MODEL2_EXTRA_PROPERTIES_XML.getBytes());
        M2Model model = M2Model.createModel(byteArrayInputStream);
        QName modelName = dictionaryDAO.putModel(model);  
        CompiledModel previousVersion = dictionaryDAO.getCompiledModels("").get(modelName);
        
        byteArrayInputStream = new ByteArrayInputStream(MODEL2_XML.getBytes());
        model = M2Model.createModel(byteArrayInputStream);
        modelName = dictionaryDAO.putModel(model);
        CompiledModel newVersion = dictionaryDAO.getCompiledModels("").get(modelName);       
        
        List<M2ModelDiff> modelDiffs = dictionaryDAO.diffModel(previousVersion, newVersion);
        
        for (M2ModelDiff modelDiff : modelDiffs)
        {
            System.out.println(modelDiff.toString());
        }   
        
        assertEquals(2, modelDiffs.size());
        
        assertEquals(1, countDiffs(modelDiffs, M2ModelDiff.TYPE_TYPE, M2ModelDiff.DIFF_UPDATED));
        assertEquals(1, countDiffs(modelDiffs, M2ModelDiff.TYPE_ASPECT, M2ModelDiff.DIFF_UPDATED));
    }
    
    public void testNonIncUpdateTypesAndAspectsRemoved()
    {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(MODEL3_EXTRA_TYPES_AND_ASPECTS_XML.getBytes());
        M2Model model = M2Model.createModel(byteArrayInputStream);
        QName modelName = dictionaryDAO.putModel(model);  
        CompiledModel previousVersion = dictionaryDAO.getCompiledModels("").get(modelName);
        
        byteArrayInputStream = new ByteArrayInputStream(MODEL3_XML.getBytes());
        model = M2Model.createModel(byteArrayInputStream);
        modelName = dictionaryDAO.putModel(model);
        CompiledModel newVersion = dictionaryDAO.getCompiledModels("").get(modelName);       
        
        List<M2ModelDiff> modelDiffs = dictionaryDAO.diffModel(previousVersion, newVersion);
        
        for (M2ModelDiff modelDiff : modelDiffs)
        {
            System.out.println(modelDiff.toString());
        }   
        
        assertEquals(4, modelDiffs.size());
        
        assertEquals(1, countDiffs(modelDiffs, M2ModelDiff.TYPE_TYPE, M2ModelDiff.DIFF_UNCHANGED));
        assertEquals(1, countDiffs(modelDiffs, M2ModelDiff.TYPE_ASPECT, M2ModelDiff.DIFF_UNCHANGED));  
        
        assertEquals(1, countDiffs(modelDiffs, M2ModelDiff.TYPE_TYPE, M2ModelDiff.DIFF_DELETED));
        assertEquals(1, countDiffs(modelDiffs, M2ModelDiff.TYPE_ASPECT, M2ModelDiff.DIFF_DELETED));
    }
    
    public void testNonIncUpdateDefaultAspectAdded()
    {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(MODEL4_XML.getBytes());
        M2Model model = M2Model.createModel(byteArrayInputStream);
        QName modelName = dictionaryDAO.putModel(model);  
        CompiledModel previousVersion = dictionaryDAO.getCompiledModels("").get(modelName);
        
        byteArrayInputStream = new ByteArrayInputStream(MODEL4_EXTRA_DEFAULT_ASPECT_XML.getBytes());
        model = M2Model.createModel(byteArrayInputStream);
        modelName = dictionaryDAO.putModel(model);
        CompiledModel newVersion = dictionaryDAO.getCompiledModels("").get(modelName);       
        
        List<M2ModelDiff> modelDiffs = dictionaryDAO.diffModel(previousVersion, newVersion);
        
        for (M2ModelDiff modelDiff : modelDiffs)
        {
            System.out.println(modelDiff.toString());
        }   
        
        assertEquals(2, modelDiffs.size());
        
        assertEquals(1, countDiffs(modelDiffs, M2ModelDiff.TYPE_TYPE, M2ModelDiff.DIFF_UPDATED));
        assertEquals(1, countDiffs(modelDiffs, M2ModelDiff.TYPE_ASPECT, M2ModelDiff.DIFF_UNCHANGED));  
    }
    
    public void testNonIncUpdateAssociationsRemoved()
    {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(MODEL5_EXTRA_ASSOCIATIONS_XML.getBytes());
        M2Model model = M2Model.createModel(byteArrayInputStream);
        QName modelName = dictionaryDAO.putModel(model);  
        CompiledModel previousVersion = dictionaryDAO.getCompiledModels("").get(modelName);
        
        byteArrayInputStream = new ByteArrayInputStream(MODEL5_XML.getBytes());
        model = M2Model.createModel(byteArrayInputStream);
        modelName = dictionaryDAO.putModel(model);
        CompiledModel newVersion = dictionaryDAO.getCompiledModels("").get(modelName);       
        
        List<M2ModelDiff> modelDiffs = dictionaryDAO.diffModel(previousVersion, newVersion);
        
        for (M2ModelDiff modelDiff : modelDiffs)
        {
            System.out.println(modelDiff.toString());
        }   
        
        assertEquals(4, modelDiffs.size());
        
        assertEquals(1, countDiffs(modelDiffs, M2ModelDiff.TYPE_TYPE, M2ModelDiff.DIFF_UPDATED));
        assertEquals(1, countDiffs(modelDiffs, M2ModelDiff.TYPE_ASPECT, M2ModelDiff.DIFF_UPDATED));
        
        assertEquals(1, countDiffs(modelDiffs, M2ModelDiff.TYPE_TYPE, M2ModelDiff.DIFF_UNCHANGED));
        assertEquals(1, countDiffs(modelDiffs, M2ModelDiff.TYPE_ASPECT, M2ModelDiff.DIFF_UNCHANGED)); 
    }
    
    private int countDiffs(List<M2ModelDiff> M2ModelDiffs, String elementType, String diffType)
    {
        int count = 0;
        for (M2ModelDiff modelDiff : M2ModelDiffs)
        {
            if (modelDiff.getDiffType().equals(diffType) && modelDiff.getElementType().equals(elementType))
            {
                count++;
            }
        }
        return count;
    }
    
}

