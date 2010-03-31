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

package org.alfresco.repo.rendition.executer;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.template.TemplateProcessorMethod;
import org.alfresco.repo.template.XSLTProcessor;
import org.alfresco.repo.template.XSLTemplateModel;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.rendition.RenditionServiceException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.XMLUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * @author Brian Remmington
 * @since 3.3
 */
public class XSLTRenderingEngine extends BaseTemplateRenderingEngine
{
    public static final String NAME = "xsltRenderingEngine";

    private static final Log log = LogFactory.getLog(XSLTRenderingEngine.class);

    private XSLTFunctions xsltFunctions;
    private NamespacePrefixResolver namespacePrefixResolver;
    private FileFolderService fileFolderService;

    @SuppressWarnings({ "serial", "unchecked" })
    protected Object buildModel(RenderingContext context)
    {
        Map<String, Serializable> suppliedParams = context.getCheckedParam(PARAM_MODEL, Map.class);
        final NodeRef sourceNode = context.getSourceNode();
        final NodeRef parentNode = nodeService.getPrimaryParent(context.getSourceNode()).getParentRef();
        final String sourcePath = getPath(sourceNode);
        final String parentPath = getPath(parentNode);

        XSLTemplateModel model = new XSLTemplateModel();

        // add simple scalar parameters
        model.put(QName.createQName(NamespaceService.ALFRESCO_URI, "date"), new Date());

        model.put(QName.createQName(NamespaceService.ALFRESCO_PREFIX, "source_file_name", namespacePrefixResolver),
                nodeService.getProperty(context.getSourceNode(), ContentModel.PROP_NAME));
        model.put(QName.createQName(NamespaceService.ALFRESCO_PREFIX, "source_path", namespacePrefixResolver),
                sourcePath);
        model.put(QName.createQName(NamespaceService.ALFRESCO_PREFIX, "parent_path", namespacePrefixResolver),
                parentPath);

        // add methods
        model.put(QName.createQName(NamespaceService.ALFRESCO_PREFIX, "encodeQuotes", namespacePrefixResolver),
                new TemplateProcessorMethod()
                {
                    public Object exec(final Object[] arguments) throws IOException, SAXException
                    {
                        if (arguments.length != 1)
                        {
                            throw new IllegalArgumentException("expected 1 argument to encodeQuotes.  got "
                                    + arguments.length);

                        }
                        if (!(arguments[0] instanceof String))
                        {
                            throw new ClassCastException("expected arguments[0] to be a " + String.class.getName()
                                    + ".  got a " + arguments[0].getClass().getName() + ".");
                        }
                        String text = (String) arguments[0];

                        if (log.isDebugEnabled())
                        {
                            log.debug("tpm_encodeQuotes('" + text + "'), parentPath = " + parentPath);
                        }

                        final String result = xsltFunctions.encodeQuotes(text);
                        return result;
                    }
                });

        model.put(QName.createQName(NamespaceService.ALFRESCO_PREFIX, "parseXMLDocument", namespacePrefixResolver),
                new TemplateProcessorMethod()
                {
                    public Object exec(final Object[] arguments) throws IOException, SAXException
                    {
                        if (arguments.length != 1)
                        {
                            throw new IllegalArgumentException("expected 1 argument to parseXMLDocument.  got "
                                    + arguments.length);

                        }
                        if (!(arguments[0] instanceof String))
                        {
                            throw new ClassCastException("expected arguments[0] to be a " + String.class.getName()
                                    + ".  got a " + arguments[0].getClass().getName() + ".");
                        }
                        String path = (String) arguments[0];

                        if (log.isDebugEnabled())
                        {
                            log.debug("parseXMLDocument('" + path + "'), parentPath = " + parentPath);
                        }

                        Document d = null;
                        try
                        {
                            d = xsltFunctions.parseXMLDocument(parentNode, path);
                        }
                        catch (Exception ex)
                        {
                            log.warn("Received an exception from parseXMLDocument()", ex);
                        }
                        return d == null ? null : d.getDocumentElement();
                    }
                });
        model.put(QName.createQName(NamespaceService.ALFRESCO_PREFIX, "parseXMLDocuments", namespacePrefixResolver),
                new TemplateProcessorMethod()
                {
                    public Object exec(final Object[] arguments) throws IOException, SAXException
                    {
                        if (arguments.length > 2)
                        {
                            throw new IllegalArgumentException("expected one or two arguments to "
                                    + "parseXMLDocuments.  got " + arguments.length);
                        }
                        if (!(arguments[0] instanceof String))
                        {
                            throw new ClassCastException("expected arguments[0] to be a " + String.class.getName()
                                    + ".  got a " + arguments[0].getClass().getName() + ".");
                        }

                        if (arguments.length == 2 && !(arguments[1] instanceof String))
                        {
                            throw new ClassCastException("expected arguments[1] to be a " + String.class.getName()
                                    + ".  got a " + arguments[1].getClass().getName() + ".");
                        }

                        String path = arguments.length == 2 ? (String) arguments[1] : "";
                        final String typeName = (String) arguments[0];

                        if (log.isDebugEnabled())
                        {
                            log.debug("tpm_parseXMLDocuments('" + typeName + "','" + path + "'), parentPath = "
                                    + parentPath);
                        }

                        final Map<String, Document> resultMap = xsltFunctions.parseXMLDocuments(typeName, parentNode,
                                path);

                        if (log.isDebugEnabled())
                        {
                            log.debug("received " + resultMap.size() + " documents in " + path + " with form name "
                                    + typeName);
                        }

                        // create a root document for rooting all the results. we do this
                        // so that each document root element has a common parent node
                        // and so that xpath axes work properly
                        final Document rootNodeDocument = XMLUtil.newDocument();
                        final Element rootNodeDocumentEl = rootNodeDocument.createElementNS(
                                NamespaceService.ALFRESCO_URI, NamespaceService.ALFRESCO_PREFIX + ":file_list");
                        rootNodeDocumentEl.setAttribute("xmlns:" + NamespaceService.ALFRESCO_PREFIX,
                                NamespaceService.ALFRESCO_URI);
                        rootNodeDocument.appendChild(rootNodeDocumentEl);

                        final List<Node> result = new ArrayList<Node>(resultMap.size());
                        for (Map.Entry<String, Document> e : resultMap.entrySet())
                        {
                            final Element documentEl = e.getValue().getDocumentElement();
                            documentEl.setAttribute("xmlns:" + NamespaceService.ALFRESCO_PREFIX,
                                    NamespaceService.ALFRESCO_URI);
                            documentEl.setAttributeNS(NamespaceService.ALFRESCO_URI, NamespaceService.ALFRESCO_PREFIX
                                    + ":file_name", e.getKey());
                            final Node n = rootNodeDocument.importNode(documentEl, true);
                            rootNodeDocumentEl.appendChild(n);
                            result.add(n);
                        }
                        return result.toArray(new Node[result.size()]);
                    }
                });

        if (suppliedParams != null)
        {
            for (Map.Entry<String, Serializable> suppliedParam : suppliedParams.entrySet())
            {
                model.put(QName.createQName(suppliedParam.getKey()), suppliedParam.getValue());
            }
        }

        // add the xml document
        try
        {
            model.put(XSLTProcessor.ROOT_NAMESPACE, XMLUtil.parse(sourceNode, contentService));
        }
        catch (RuntimeException ex)
        {
            throw ex;
        }
        catch (Exception ex)
        {
            throw new RenditionServiceException("Failed to parse XML from source node.", ex);
        }
        return model;
    }

    @Override
    protected String getTemplateType()
    {
        return "xslt";
    }

    /**
     * @param nodeRef
     * @return
     * @throws FileNotFoundException
     */
    private String getPath(NodeRef nodeRef)
    {
        StringBuilder sb = new StringBuilder();
        try
        {
            List<FileInfo> parentFileInfoList = fileFolderService.getNamePath(null, nodeRef);
            for (FileInfo fileInfo : parentFileInfoList)
            {
                sb.append('/');
                sb.append(fileInfo.getName());
            }
        }
        catch (FileNotFoundException ex)
        {
            log.info("Unexpected problem: error while calculating path to node " + nodeRef, ex);
        }
        String path = sb.toString();
        return path;
    }

    /**
     * @param xsltFunctions
     *            the xsltFunctions to set
     */
    public void setXsltFunctions(XSLTFunctions xsltFunctions)
    {
        this.xsltFunctions = xsltFunctions;
    }

    /**
     * @param namespacePrefixResolver
     *            the namespacePrefixResolver to set
     */
    public void setNamespacePrefixResolver(NamespacePrefixResolver namespacePrefixResolver)
    {
        this.namespacePrefixResolver = namespacePrefixResolver;
    }

    /**
     * @param fileFolderService
     *            the fileFolderService to set
     */
    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }
}
