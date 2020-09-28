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
package org.alfresco.repo.dictionary;

import junit.framework.TestCase;
import org.alfresco.repo.tenant.SingleTServiceImpl;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.util.DynamicallySizedThreadPoolExecutor;
import org.alfresco.util.TraceableThreadFactory;
import org.alfresco.util.cache.DefaultAsynchronouslyRefreshedCacheRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class AbstractModelTest extends TestCase
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
    public static final String MODEL6_XML =
            "<model name=\"test1:model6\" xmlns=\"http://www.alfresco.org/model/dictionary/1.0\">" +

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
                    "        <title>Type1 Title</title>" +
                    "        <description>Type1 Description</description>" +
                    "        <properties>" +
                    "           <property name=\"test1:prop1\">" +
                    "              <title>Prop1 Title</title>" +
                    "              <description>Prop1 Description</description>" +
                    "              <type>d:text</type>" +
                    "           </property>" +
                    "        </properties>" +
                    "      </type>" +

                    "   </types>" +

                    "   <aspects>" +

                    "      <aspect name=\"test1:aspect1\">" +
                    "        <title>Aspect1 Title</title>" +
                    "        <description>Aspect1 Description</description>" +
                    "        <properties>" +
                    "           <property name=\"test1:prop9\">" +
                    "              <title>Prop9 Title</title>" +
                    "              <description>Prop9 Description</description>" +
                    "              <type>d:text</type>" +
                    "           </property>" +
                    "        </properties>" +
                    "      </aspect>" +

                    "   </aspects>" +

                    "</model>";
    public static final String MODEL6_UPDATE1_XML =
            "<model name=\"test1:model6\" xmlns=\"http://www.alfresco.org/model/dictionary/1.0\">" +

                    "   <description>Another description - UPDATE1</description>" +
                    "   <author>Alfresco - UPDATE1</author>" +
                    "   <published>2009-08-01</published>" +
                    "   <version>2.0</version>" +

                    "   <imports>" +
                    "      <import uri=\"http://www.alfresco.org/model/dictionary/1.0\" prefix=\"d\"/>" +
                    "   </imports>" +

                    "   <namespaces>" +
                    "      <namespace uri=\"http://www.alfresco.org/model/test1/1.0\" prefix=\"test1\"/>" +
                    "   </namespaces>" +

                    "   <types>" +

                    "      <type name=\"test1:type1\">" +
                    "        <title>Type1 Title - UPDATE1</title>" +
                    "        <description>Type1 Description - UPDATE1</description>" +
                    "        <properties>" +
                    "           <property name=\"test1:prop1\">" +
                    "              <title>Prop1 Title - UPDATE1</title>" +
                    "              <description>Prop1 Description - UPDATE1</description>" +
                    "              <type>d:text</type>" +
                    "           </property>" +
                    "        </properties>" +
                    "      </type>" +

                    "   </types>" +

                    "   <aspects>" +

                    "      <aspect name=\"test1:aspect1\">" +
                    "        <title>Aspect1 Title</title>" +
                    "        <description>Aspect1 Description</description>" +
                    "        <properties>" +
                    "           <property name=\"test1:prop9\">" +
                    "              <title>Prop9 Title - UPDATE1</title>" +
                    "              <description>Prop9 Description - UPDATE1</description>" +
                    "              <type>d:text</type>" +
                    "           </property>" +
                    "        </properties>" +
                    "      </aspect>" +

                    "   </aspects>" +

                    "</model>";
    public static final String MODEL7_XML =
            "<model name=\"test7:model7\" xmlns=\"http://www.alfresco.org/model/dictionary/1.0\">" +

                    "   <imports>" +
                    "      <import uri=\"http://www.alfresco.org/model/dictionary/1.0\" prefix=\"d\"/>" +
                    "   </imports>" +

                    "   <namespaces>" +
                    "      <namespace uri=\"http://www.alfresco.org/model/test7/1.0\" prefix=\"test7\"/>" +
                    "   </namespaces>" +

                    "   <aspects>" +

                    "      <aspect name=\"test7:aspectA\">" +
                    "      </aspect>" +

                    "      <aspect name=\"test7:aspectB\">" +
                    "         <mandatory-aspects> " +
                    "            <aspect>test7:aspectA</aspect> " +
                    "         </mandatory-aspects> " +
                    "      </aspect>" +

                    "   </aspects>" +

                    "</model>";
    public static final String MODEL7_EXTRA_PROPERTIES_MANDATORY_ASPECTS_XML =
            "<model name=\"test7:model7\" xmlns=\"http://www.alfresco.org/model/dictionary/1.0\">" +

                    "   <imports>" +
                    "      <import uri=\"http://www.alfresco.org/model/dictionary/1.0\" prefix=\"d\"/>" +
                    "   </imports>" +

                    "   <namespaces>" +
                    "      <namespace uri=\"http://www.alfresco.org/model/test7/1.0\" prefix=\"test7\"/>" +
                    "   </namespaces>" +

                    "   <aspects>" +

                    "      <aspect name=\"test7:aspectA\">" +
                    "         <properties> " +
                    "            <property name=\"test7:propA1\"> " +
                    "               <title>Prop A1</title> " +
                    "               <type>d:text</type> " +
                    "            </property> " +
                    "         </properties> " +
                    "      </aspect>" +

                    "      <aspect name=\"test7:aspectB\">" +
                    "         <mandatory-aspects> " +
                    "            <aspect>test7:aspectA</aspect> " +
                    "         </mandatory-aspects> " +
                    "      </aspect>" +

                    "   </aspects>" +

                    "</model>";


    public AbstractModelTest()
    {
    }

    DictionaryDAOImpl dictionaryDAO;

    /**
     * Setup
     */
    protected void setUp() throws Exception
    {
        // Initialise the Dictionary
        TenantService tenantService = new SingleTServiceImpl();

//        NamespaceDAOImpl namespaceDAO = new NamespaceDAOImpl();
//        namespaceDAO.setTenantService(tenantService);
//        initNamespaceCaches(namespaceDAO);

        dictionaryDAO = new DictionaryDAOImpl();
        dictionaryDAO.setTenantService(tenantService);

        initDictionaryCaches(dictionaryDAO, tenantService);


        // include Alfresco dictionary model
        List<String> bootstrapModels = new ArrayList<String>();
        bootstrapModels.add("alfresco/model/dictionaryModel.xml");

        DictionaryBootstrap bootstrap = new DictionaryBootstrap();
        bootstrap.setModels(bootstrapModels);
        bootstrap.setDictionaryDAO(dictionaryDAO);
        bootstrap.bootstrap();
    }

    void initDictionaryCaches(DictionaryDAOImpl dictionaryDAO, TenantService tenantService)
    {
        CompiledModelsCache compiledModelsCache = new CompiledModelsCache();
        compiledModelsCache.setDictionaryDAO(dictionaryDAO);
        compiledModelsCache.setTenantService(tenantService);
        compiledModelsCache.setRegistry(new DefaultAsynchronouslyRefreshedCacheRegistry());
        TraceableThreadFactory threadFactory = new TraceableThreadFactory();
        threadFactory.setThreadDaemon(true);
        threadFactory.setThreadPriority(Thread.NORM_PRIORITY);

        ThreadPoolExecutor threadPoolExecutor = new DynamicallySizedThreadPoolExecutor(20, 20, 90, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), threadFactory,
                new ThreadPoolExecutor.CallerRunsPolicy());
        compiledModelsCache.setThreadPoolExecutor(threadPoolExecutor);
        dictionaryDAO.setDictionaryRegistryCache(compiledModelsCache);
        dictionaryDAO.init();
    }

    public void testIsSetup()
    {
        assertNotNull(dictionaryDAO);
    }
}