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

package org.alfresco.repo.template;

import java.io.StringWriter;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.transaction.annotation.Transactional;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TemplateProcessor;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.test_category.BaseSpringTestsCategory;
import org.alfresco.util.BaseAlfrescoSpringTest;
import org.alfresco.util.GUID;
import org.alfresco.util.XMLUtil;

/**
 * @author Brian
 * 
 */
@Category({BaseSpringTestsCategory.class})
@Transactional
public class XSLTProcessorTest extends BaseAlfrescoSpringTest
{
    private final static Log log = LogFactory.getLog(XSLTProcessorTest.class);
    private NodeRef companyHome;
    private FileFolderService fileFolderService;
    private TemplateProcessor xsltProcessor;
    private TemplateService templateService;
    private Repository repositoryHelper;

    @Before
    public void before() throws Exception
    {
        super.before();
        this.nodeService = (NodeService) this.applicationContext.getBean("NodeService");
        this.contentService = (ContentService) this.applicationContext.getBean("ContentService");
        this.fileFolderService = (FileFolderService) this.applicationContext.getBean("FileFolderService");
        this.xsltProcessor = (TemplateProcessor) this.applicationContext.getBean("xsltProcessor");
        this.templateService = (TemplateService) this.applicationContext.getBean("TemplateService");
        this.repositoryHelper = (Repository) this.applicationContext.getBean("repositoryHelper");
        this.companyHome = repositoryHelper.getCompanyHome();
    }

    @Test
    public void testSimplestStringTemplate() throws Exception
    {
        try
        {
            FileInfo file = createXmlFile(companyHome);
            XSLTemplateModel model = new XSLTemplateModel();
            model.put(XSLTProcessor.ROOT_NAMESPACE, XMLUtil.parse(file.getNodeRef(), contentService));

            StringWriter writer = new StringWriter();
            xsltProcessor.processString(verySimpleXSLT, model, writer);
            String output = writer.toString();

            log.debug("XSLT Processor output: " + output);
            assertEquals("Avocado DipBagels, New York StyleBeef Frankfurter, Quarter PoundChicken Pot PieCole SlawEggsHazelnut SpreadPotato ChipsSoy Patties, GrilledTruffles, Dark Chocolate", output);
        }
        catch (Exception ex)
        {
            log.error("Error!", ex);
            fail();
        }
    }

    @Test
    public void testSimplestNodeTemplate() throws Exception
    {
        try
        {
            FileInfo xmlFile = createXmlFile(companyHome);
            FileInfo xslFile = createXmlFile(companyHome, verySimpleXSLT);
            XSLTemplateModel model = new XSLTemplateModel();
            model.put(XSLTProcessor.ROOT_NAMESPACE, XMLUtil.parse(xmlFile.getNodeRef(), contentService));

            StringWriter writer = new StringWriter();
            xsltProcessor.process(xslFile.getNodeRef().toString(), model, writer);

            String output = writer.toString();

            log.debug("XSLT Processor output: " + output);
            assertEquals("Avocado DipBagels, New York StyleBeef Frankfurter, Quarter PoundChicken Pot PieCole SlawEggsHazelnut SpreadPotato ChipsSoy Patties, GrilledTruffles, Dark Chocolate", output);
        }
        catch (Exception ex)
        {
            log.error("Error!", ex);
            fail();
        }
    }

    @Test
    public void testLocalisedNodeTemplate() throws Exception
    {
        // This should have the same result as testSimplestNodeTemplate as the localization should be ignored for node templates.
        try
        {
            FileInfo xmlFile = createXmlFile(companyHome);
            FileInfo xslFile = createXmlFile(companyHome, verySimpleXSLT);
            XSLTemplateModel model = new XSLTemplateModel();
            model.put(XSLTProcessor.ROOT_NAMESPACE, XMLUtil.parse(xmlFile.getNodeRef(), contentService));

            StringWriter writer = new StringWriter();
            xsltProcessor.process(xslFile.getNodeRef().toString(), model, writer, Locale.FRANCE);

            String output = writer.toString();

            log.debug("XSLT Processor output: " + output);
            assertEquals("Avocado DipBagels, New York StyleBeef Frankfurter, Quarter PoundChicken Pot PieCole SlawEggsHazelnut SpreadPotato ChipsSoy Patties, GrilledTruffles, Dark Chocolate", output);
        }
        catch (Exception ex)
        {
            log.error("Error!", ex);
            fail();
        }
    }

    @Test
    public void testSimplestClasspathTemplate() throws Exception
    {
        try
        {
            FileInfo xmlFile = createXmlFile(companyHome);
            XSLTemplateModel model = new XSLTemplateModel();
            model.put(XSLTProcessor.ROOT_NAMESPACE, XMLUtil.parse(xmlFile.getNodeRef(), contentService));

            StringWriter writer = new StringWriter();
            xsltProcessor.process("org/alfresco/repo/template/test_template1.xsl", model, writer);

            String output = writer.toString();

            log.debug("XSLT Processor output: " + output);
            assertEquals("Avocado DipBagels, New York StyleBeef Frankfurter, Quarter PoundChicken Pot PieCole SlawEggsHazelnut SpreadPotato ChipsSoy Patties, GrilledTruffles, Dark Chocolate", output);
        }
        catch (Exception ex)
        {
            log.error("Error!", ex);
            fail();
        }
    }

    @Test
    public void testLocalisedClasspathTemplate() throws Exception
    {
        try
        {
            FileInfo xmlFile = createXmlFile(companyHome);
            XSLTemplateModel model = new XSLTemplateModel();
            model.put(XSLTProcessor.ROOT_NAMESPACE, XMLUtil.parse(xmlFile.getNodeRef(), contentService));

            StringWriter writer = new StringWriter();
            xsltProcessor.process("org/alfresco/repo/template/test_template1.xsl", model, writer, new Locale("en", "AU"));

            String output = writer.toString();

            log.debug("XSLT Processor output: " + output);
            assertEquals("G'day, Avocado DipBagels, New York StyleBeef Frankfurter, Quarter PoundChicken Pot PieCole SlawEggsHazelnut SpreadPotato ChipsSoy Patties, GrilledTruffles, Dark Chocolate", output);

            writer = new StringWriter();
            xsltProcessor.process("org/alfresco/repo/template/test_template1.xsl", model, writer, new Locale("en", "GB"));

            output = writer.toString();

            log.debug("XSLT Processor output: " + output);
            assertEquals("Avocado DipBagels, New York StyleBeef Frankfurter, Quarter PoundChicken Pot PieCole SlawEggsHazelnut SpreadPotato ChipsSoy Patties, GrilledTruffles, Dark Chocolate", output);

            writer = new StringWriter();
            xsltProcessor.process("org/alfresco/repo/template/test_template1.xsl", model, writer, new Locale("fr", "FR"));

            output = writer.toString();

            log.debug("XSLT Processor output: " + output);
            assertEquals("Bonjour, Avocado DipBagels, New York StyleBeef Frankfurter, Quarter PoundChicken Pot PieCole SlawEggsHazelnut SpreadPotato ChipsSoy Patties, GrilledTruffles, Dark Chocolate", output);
        }
        catch (Exception ex)
        {
            log.error("Error!", ex);
            fail();
        }
    }

    @Test
    public void testTemplateServiceBinding() throws Exception
    {
        try
        {
            FileInfo xmlFile = createXmlFile(companyHome);
            XSLTemplateModel model = new XSLTemplateModel();
            model.put(XSLTProcessor.ROOT_NAMESPACE, XMLUtil.parse(xmlFile.getNodeRef(), contentService));

            StringWriter writer = new StringWriter();
            templateService.processTemplate("xslt", "org/alfresco/repo/template/test_template1.xsl", model, writer);

            String output = writer.toString();

            log.debug("XSLT Processor output: " + output);
            assertEquals("Avocado DipBagels, New York StyleBeef Frankfurter, Quarter PoundChicken Pot PieCole SlawEggsHazelnut SpreadPotato ChipsSoy Patties, GrilledTruffles, Dark Chocolate", output);
        }
        catch (Exception ex)
        {
            log.error("Error!", ex);
            fail();
        }
    }

    private FileInfo createXmlFile(NodeRef folder)
    {
        return createXmlFile(folder, sampleXML);
    }

    private FileInfo createXmlFile(NodeRef folder, String content)
    {
        String name = GUID.generate();
        FileInfo testXmlFile = fileFolderService.create(folder, name + ".xml", ContentModel.TYPE_CONTENT);
        ContentWriter writer = contentService.getWriter(testXmlFile.getNodeRef(), ContentModel.PROP_CONTENT, true);
        writer.setMimetype("text/xml");
        writer.setEncoding("UTF-8");
        writer.putContent(content);
        return testXmlFile;
    }

    private String sampleXML = "<?xml version=\"1.0\"?>" + "<nutrition>" +

            "<daily-values>" + "<total-fat units=\"g\">65</total-fat>" + "<saturated-fat units=\"g\">20</saturated-fat>"
            + "<cholesterol units=\"mg\">300</cholesterol>" + "<sodium units=\"mg\">2400</sodium>"
            + "<carb units=\"g\">300</carb>" + "<fiber units=\"g\">25</fiber>" + "<protein units=\"g\">50</protein>"
            + "</daily-values>" +

            "<food>" + "<name>Avocado Dip</name>" + "<mfr>Sunnydale</mfr>" + "<serving units=\"g\">29</serving>"
            + "<calories total=\"110\" fat=\"100\"/>" + "<total-fat>11</total-fat>"
            + "<saturated-fat>3</saturated-fat>" + "<cholesterol>5</cholesterol>" + "<sodium>210</sodium>"
            + "<carb>2</carb>" + "<fiber>0</fiber>" + "<protein>1</protein>" + "<vitamins>" + "<a>0</a>" + "<c>0</c>"
            + "</vitamins>" + "<minerals>" + "<ca>0</ca>" + "<fe>0</fe>" + "</minerals>" + "</food>" +

            "<food>" + "<name>Bagels, New York Style</name>" + "<mfr>Thompson</mfr>"
            + "<serving units=\"g\">104</serving>" + "<calories total=\"300\" fat=\"35\"/>"
            + "<total-fat>4</total-fat>" + "<saturated-fat>1</saturated-fat>" + "<cholesterol>0</cholesterol>"
            + "<sodium>510</sodium>" + "<carb>54</carb>" + "<fiber>3</fiber>" + "<protein>11</protein>" + "<vitamins>"
            + "<a>0</a>" + "<c>0</c>" + "</vitamins>" + "<minerals>" + "<ca>8</ca>" + "<fe>20</fe>" + "</minerals>"
            + "</food>" +

            "<food>" + "<name>Beef Frankfurter, Quarter Pound</name>" + "<mfr>Armitage</mfr>"
            + "<serving units=\"g\">115</serving>" + "<calories total=\"370\" fat=\"290\"/>"
            + "<total-fat>32</total-fat>" + "<saturated-fat>15</saturated-fat>" + "<cholesterol>65</cholesterol>"
            + "<sodium>1100</sodium>" + "<carb>8</carb>" + "<fiber>0</fiber>" + "<protein>13</protein>" + "<vitamins>"
            + "<a>0</a>" + "<c>2</c>" + "</vitamins>" + "<minerals>" + "<ca>1</ca>" + "<fe>6</fe>" + "</minerals>"
            + "</food>" +

            "<food>" + "<name>Chicken Pot Pie</name>" + "<mfr>Lakeson</mfr>" + "<serving units=\"g\">198</serving>"
            + "<calories total=\"410\" fat=\"200\"/>" + "<total-fat>22</total-fat>"
            + "<saturated-fat>9</saturated-fat>" + "<cholesterol>25</cholesterol>" + "<sodium>810</sodium>"
            + "<carb>42</carb>" + "<fiber>2</fiber>" + "<protein>10</protein>" + "<vitamins>" + "<a>20</a>"
            + "<c>2</c>" + "</vitamins>" + "<minerals>" + "<ca>2</ca>" + "<fe>10</fe>" + "</minerals>" + "</food>" +

            "<food>" + "<name>Cole Slaw</name>" + "<mfr>Fresh Quick</mfr>" + "<serving units=\" cup\">1.5</serving>"
            + "<calories total=\"20\" fat=\"0\"/>" + "<total-fat>0</total-fat>" + "<saturated-fat>0</saturated-fat>"
            + "<cholesterol>0</cholesterol>" + "<sodium>15</sodium>" + "<carb>5</carb>" + "<fiber>2</fiber>"
            + "<protein>1</protein>" + "<vitamins>" + "<a>30</a>" + "<c>45</c>" + "</vitamins>" + "<minerals>"
            + "<ca>4</ca>" + "<fe>2</fe>" + "</minerals>" + "</food>" +

            "<food>" + "<name>Eggs</name>" + "<mfr>Goodpath</mfr>" + "<serving units=\"g\">50</serving>"
            + "<calories total=\"70\" fat=\"40\"/>" + "<total-fat>4.5</total-fat>"
            + "<saturated-fat>1.5</saturated-fat>" + "<cholesterol>215</cholesterol>" + "<sodium>65</sodium>"
            + "<carb>1</carb>" + "<fiber>0</fiber>" + "<protein>6</protein>" + "<vitamins>" + "<a>6</a>" + "<c>0</c>"
            + "</vitamins>" + "<minerals>" + "<ca>2</ca>" + "<fe>4</fe>" + "</minerals>" + "</food>" +

            "<food>" + "<name>Hazelnut Spread</name>" + "<mfr>Ferreira</mfr>" + "<serving units=\"tbsp\">2</serving>"
            + "<calories total=\"200\" fat=\"90\"/>" + "<total-fat>10</total-fat>" + "<saturated-fat>2</saturated-fat>"
            + "<cholesterol>0</cholesterol>" + "<sodium>20</sodium>" + "<carb>23</carb>" + "<fiber>2</fiber>"
            + "<protein>3</protein>" + "<vitamins>" + "<a>0</a>" + "<c>0</c>" + "</vitamins>" + "<minerals>"
            + "<ca>6</ca>" + "<fe>4</fe>" + "</minerals>" + "</food>" +

            "<food>" + "<name>Potato Chips</name>" + "<mfr>Lees</mfr>" + "<serving units=\"g\">28</serving>"
            + "<calories total=\"150\" fat=\"90\"/>" + "<total-fat>10</total-fat>" + "<saturated-fat>3</saturated-fat>"
            + "<cholesterol>0</cholesterol>" + "<sodium>180</sodium>" + "<carb>15</carb>" + "<fiber>1</fiber>"
            + "<protein>2</protein>" + "<vitamins>" + "<a>0</a>" + "<c>10</c>" + "</vitamins>" + "<minerals>"
            + "<ca>0</ca>" + "<fe>0</fe>" + "</minerals>" + "</food>" +

            "<food>" + "<name>Soy Patties, Grilled</name>" + "<mfr>Gardenproducts</mfr>"
            + "<serving units=\"g\">96</serving>" + "<calories total=\"160\" fat=\"45\"/>" + "<total-fat>5</total-fat>"
            + "<saturated-fat>0</saturated-fat>" + "<cholesterol>0</cholesterol>" + "<sodium>420</sodium>"
            + "<carb>10</carb>" + "<fiber>4</fiber>" + "<protein>9</protein>" + "<vitamins>" + "<a>0</a>" + "<c>0</c>"
            + "</vitamins>" + "<minerals>" + "<ca>0</ca>" + "<fe>0</fe>" + "</minerals>" + "</food>" +

            "<food>" + "<name>Truffles, Dark Chocolate</name>" + "<mfr>Lyndon's</mfr>"
            + "<serving units=\"g\">39</serving>" + "<calories total=\"220\" fat=\"170\"/>"
            + "<total-fat>19</total-fat>" + "<saturated-fat>14</saturated-fat>" + "<cholesterol>25</cholesterol>"
            + "<sodium>10</sodium>" + "<carb>16</carb>" + "<fiber>1</fiber>" + "<protein>1</protein>" + "<vitamins>"
            + "<a>0</a>" + "<c>0</c>" + "</vitamins>" + "<minerals>" + "<ca>0</ca>" + "<fe>0</fe>" + "</minerals>"
            + "</food>" +

            "</nutrition>";

    private String verySimpleXSLT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<xsl:stylesheet version=\"1.0\"  "
            + "xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" "
            + "xmlns:fn=\"http://www.w3.org/2005/02/xpath-functions\"> " + "<xsl:output method=\"text\" />" +

            "<xsl:preserve-space elements=\"*\"/>" +

            "<xsl:template match=\"/\">" + "<xsl:for-each select=\"/nutrition/food\">"
            + "<xsl:value-of select=\"name\"/>" + "</xsl:for-each>" + "</xsl:template>" + "</xsl:stylesheet>";
}
