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
package org.alfresco.util;

import java.io.File;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

import junit.framework.TestCase;
import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;
import freemarker.template.Template;

/**
 * Test JSONtoFmModel conversion
 * 
 * Note: Dates depend on ISO8601DateFormat.parse which currently expects YYYY-MM-DDThh:mm:ss.sssTZD
 * 
 * @author janv
 */
public class JSONtoFmModelTest extends TestCase
{

    public void testUtil()
    {
        String test1_in = "[ 123, \"hello\", true, null, \"1994-11-05T13:15:30.123-12:30\"]";
        
        String test1_expected_out = 
        "[\n" +
        "123:class java.lang.Integer\n" +
        "hello:class java.lang.String\n" +
        "true:class java.lang.Boolean\n" +
        "null:null\n" +
        "Sun Nov 06 01:45:30 GMT 1994:class java.util.Date\n" +
        "]\n";
        
        String test2_in = "{ \"glossary\": { \"title\": \"example glossary\", } }";
        
        String test2_expected_out =
        "glossary:class java.util.HashMap\n" +
        "\ttitle:example glossary:class java.lang.String\n";
        
        String test3_in =
        "{ \"doc\": " +
        "   { \"abc\": \"hello\", " +
        "     \"def\": \"world\", " +
        "     \"ghi\" : 123, " +
        "     \"jkl\" : 123.456, " +
        "     \"mno\" : true, " +
        "     \"qrs\" : \"1994-11-05T13:15:30.000Z\"" +
        "   }" +
        "}";
        
        String test3_expected_out =
        "doc:class java.util.HashMap\n" +
        "\tabc:hello:class java.lang.String\n" +
        "\tdef:world:class java.lang.String\n" +
        "\tghi:123:class java.lang.Integer\n" +
        "\tjkl:123.456:class java.lang.Double\n" +
        "\tmno:true:class java.lang.Boolean\n" +
        "\tqrs:Sat Nov 05 13:15:30 GMT 1994:class java.util.Date\n";
        
        String test4_in =
        "{" +
        "    \"glossary\": {" +
        "        \"title\": \"example glossary\"," +
        "        \"GlossDiv\": {" +
        "            \"title\": \"S\"," +
        "            \"GlossList\": {" +
        "                \"GlossEntry\": {" +
        "                   \"ID\": \"SGML\"," +
        "                   \"SortAs\": \"SGML\"," +
        "                   \"GlossTerm\": \"Standard Generalized Markup Language\", " +
        "                   \"Acronym\": \"SGML\"," +
        "                   \"Abbrev\": \"ISO 8879:1986\"," +
        "                   \"GlossDef\": {" +
        "                        \"para\": \"A meta-markup language, used to create markup languages such as DocBook.\","+
        "                        \"GlossSeeAlso\": [\"GML\", \"XML\", \"ANO1\", \"ANO2\"]" +
        "                   }," +
        "                   \"GlossSee\": \"markup\"" +
        "                }" +
        "           }" +
        "       }" +
        "    }" +
        "}";
        
        String test4_expected_out =
        "glossary:class java.util.HashMap\n" +
        "\tGlossDiv:class java.util.HashMap\n" +
        "\t\tGlossList:class java.util.HashMap\n" +
        "\t\t\tGlossEntry:class java.util.HashMap\n" +
        "\t\t\t\tAbbrev:ISO 8879:1986:class java.lang.String\n" +
        "\t\t\t\tAcronym:SGML:class java.lang.String\n" +
        "\t\t\t\tGlossDef:class java.util.HashMap\n" +
        "\t\t\t\t\t[\n" +
        "\t\t\t\t\tGML:class java.lang.String\n" +
        "\t\t\t\t\tXML:class java.lang.String\n" +
        "\t\t\t\t\tANO1:class java.lang.String\n" +
        "\t\t\t\t\tANO2:class java.lang.String\n" +
        "\t\t\t\t\t]\n" +
        "\t\t\t\t\tpara:A meta-markup language, used to create markup languages such as DocBook.:class java.lang.String\n" +
        "\t\t\t\tGlossSee:markup:class java.lang.String\n" +
        "\t\t\t\tGlossTerm:Standard Generalized Markup Language:class java.lang.String\n" +
        "\t\t\t\tID:SGML:class java.lang.String\n" +
        "\t\t\t\tSortAs:SGML:class java.lang.String\n" +
        "\t\ttitle:S:class java.lang.String\n" +
        "\ttitle:example glossary:class java.lang.String\n";
        
        try
        {
            Configuration cfg = new Configuration();
            cfg.setObjectWrapper(ObjectWrapper.DEFAULT_WRAPPER);
            
            String userDir = System.getProperty("user.dir");
            System.out.println(userDir);
            cfg.setDirectoryForTemplateLoading(new File(userDir+"/source/test-resources/JSONtoFmModel"));
            
            Map<String, Object> root = null;
            
            // Test 1
            System.out.println("TEST 1");
            //System.out.println(test1_in);
            //System.out.println("--->");
            root = JSONtoFmModel.convertJSONArrayToMap(test1_in);
            //System.out.println(JSONtoFmModel.toString(root));
            assertEquals(test1_expected_out, JSONtoFmModel.toString(root));
            
            Template temp = cfg.getTemplate("test1.ftl");
            Writer out = new OutputStreamWriter(System.out);
            temp.process(root, out);
            out.flush();  
            
            System.out.println("\n\n\n");
            
            // Test 2
            System.out.println("TEST 2");
            //System.out.println(test2_in);
            //System.out.println("--->");
            root = JSONtoFmModel.convertJSONObjectToMap(test2_in);
            //System.out.println(JSONtoFmModel.toString(root));
            assertEquals(test2_expected_out, JSONtoFmModel.toString(root));
            
            temp = cfg.getTemplate("test2.ftl");
            out = new OutputStreamWriter(System.out);
            temp.process(root, out);
            out.flush();  
            
            System.out.println("\n\n\n");
            
            // Test 3
            System.out.println("TEST 3");
            //System.out.println(test3_in);
            //System.out.println("--->");
            root = JSONtoFmModel.convertJSONObjectToMap(test3_in);
            //System.out.println(JSONtoFmModel.toString(root));
            assertEquals(test3_expected_out, JSONtoFmModel.toString(root));
            
            temp = cfg.getTemplate("test3.ftl");
            out = new OutputStreamWriter(System.out);
            temp.process(root, out);
            out.flush();
            
            // Test 4
            System.out.println("TEST 4");
            //System.out.println(test4_in);
            //System.out.println("--->");
            root = JSONtoFmModel.convertJSONObjectToMap(test4_in);
            //System.out.println(JSONtoFmModel.toString(root));
            assertEquals(test4_expected_out, JSONtoFmModel.toString(root));
            
            temp = cfg.getTemplate("test4.ftl");
            out = new OutputStreamWriter(System.out);
            temp.process(root, out);
            out.flush();
        }
        catch (Exception e)
        {
            System.out.println("ERROR: " + e);
        }
    }
}
