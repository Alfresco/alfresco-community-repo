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
package org.alfresco.repo.cmis.rest.test;

import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.alfresco.repo.cmis.rest.AlfrescoCMISExtensionFactory;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.apache.abdera.Abdera;
import org.apache.abdera.factory.Factory;
import org.apache.abdera.i18n.iri.IRI;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Service;
import org.apache.abdera.parser.ParseException;
import org.apache.abdera.parser.Parser;
import org.apache.chemistry.abdera.ext.CMISConstants;
import org.apache.chemistry.tck.atompub.client.CMISClient;
import org.apache.chemistry.tck.atompub.utils.ResourceLoader;
import org.junit.Assert;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Request;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * Base class for Alfresco specific CMIS REST API tests.
 * 
 * @author dward
 */
public abstract class BaseCMISTest extends BaseWebScriptTest
{
    protected static final QName ELEMENT_PROPERTIES = new QName("http://www.alfresco.org", "properties");
    protected static final QName ELEMENT_APPLIED_ASPECTS = new QName("http://www.alfresco.org", "appliedAspects");

    protected static final String URL_CMIS = "/cmis";

    protected Abdera abdera;
    protected Parser parser;
    protected Factory factory;

    public BaseCMISTest()
    {
        // construct Abdera Service
        abdera = new Abdera();
        factory = abdera.getFactory();
        factory.registerExtension(new AlfrescoCMISExtensionFactory());
        parser = factory.newParser();

        // construct test templates
        localTemplates = new ResourceLoader('/' + getClass().getPackage().getName().replace('.', '/') + '/');

        // Create a dummy client. We won't / can't use it to make requests.
        cmisClient = new CMISClient(null, null, null, null);
    }

    protected CMISClient cmisClient;
    protected ResourceLoader localTemplates;
    protected Service cmisService;
    protected Entry testCaseFolder;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();

        setDefaultRunAs("admin");

        Request req = new GetRequest(URL_CMIS);
        Response res = sendRequest(req, 200);
        String xml = res.getContentAsString();
        Assert.assertNotNull(xml);
        Assert.assertTrue(xml.length() > 0);
        cmisService = parse(new StringReader(xml));
        Assert.assertNotNull(cmisService);
        IRI rootFolderHREF = cmisClient.getRootCollection(cmisClient.getWorkspace(cmisService));
        Assert.assertNotNull(rootFolderHREF);
        String folderName = getClass().getSimpleName() + System.currentTimeMillis() + " - " + getName();
        testCaseFolder = createObject(rootFolderHREF, folderName, "cmis:folder");
    }

    protected <T extends Element> T parse(Reader doc)
    {
        Document<T> entryDoc = parser.parse(doc);
        return entryDoc.getRoot();
    }

    protected <T extends Element> T fetch(IRI href, Map<String, String> args) throws Exception
    {
        Request get = new GetRequest(href.toString()).setArgs(args);
        Response res = sendRequest(get, 200);
        Assert.assertNotNull(res);
        String xml = res.getContentAsString();
        T result = this.<T> parse(new StringReader(xml));
        Assert.assertNotNull(result);
        return result;
    }

    protected Entry createObject(IRI parent, String name, String type) throws Exception
    {
        return createObject(parent, name, type, 201);
    }

    protected <T extends Element> T createObject(IRI parent, String name, String type, int expectedStatus) throws Exception
    {
        String createObject = localTemplates.load("BaseCMISTest.createObject.atomentry.xml");
        createObject = createObject.replace("${NAME}", name);
        createObject = createObject.replace("${TYPE}", type);
        Request req = new PostRequest(parent.toString(), createObject, CMISConstants.MIMETYPE_ENTRY);
        Response res = sendRequest(req, expectedStatus);
        Assert.assertNotNull(res);
        try
        {
            String xml = res.getContentAsString();
            return this.<T> parse(new StringReader(xml));
        }
        catch (ParseException e)
        {
            return null;
        }
    }

    protected void assertContains(Set<String> actual, String... expected)
    {
        Assert.assertTrue(actual.containsAll(Arrays.asList(expected)));
    }

    protected void assertDoesNotContain(Set<String> actual, String... unexpected)
    {
        Set<String> copy = new HashSet<String>(actual);
        copy.retainAll(Arrays.asList(unexpected));
        Assert.assertTrue(copy.isEmpty());
    }
}
