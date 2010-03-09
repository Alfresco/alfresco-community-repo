/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */

package org.alfresco.repo.rendition.executer;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.template.TemplateProcessorMethod;
import org.alfresco.repo.template.XSLTProcessor;
import org.alfresco.repo.template.XSLTemplateModel;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.rendition.RenditionServiceException;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
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
public class XSLTRenderingEngine extends AbstractRenderingEngine
{
    private static final Log log = LogFactory.getLog(XSLTRenderingEngine.class);

    public static final String NAME = "xsltRenderingEngine";
    public static final String PARAM_MODEL = "model";
    public static final String PARAM_TEMPLATE = "template_string";
    public static final String PARAM_TEMPLATE_NODE = "template_node";
    public static final String PARAM_TEMPLATE_PATH = "template_path";

    private TemplateService templateService;
    private XSLTFunctions xsltFunctions;
    private NamespacePrefixResolver namespacePrefixResolver;
    private FileFolderService fileFolderService;
    private SearchService searchService;

    /*
     * @see org.alfresco.repo.rendition.executer.AbstractRenderingEngine#render(org
     * .alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.rendition.RenditionDefinition,
     * org.alfresco.service.cmr.repository.ContentReader, org.alfresco.service.cmr.repository.ChildAssociationRef)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void render(RenderingContext context)
    {
        NodeRef templateNode = getTemplateNode(context);
        Map<String, Serializable> paramMap = context.getCheckedParam(PARAM_MODEL, Map.class);
        try
        {
            XSLTemplateModel model = buildModel(context, paramMap);
            ContentWriter contentWriter = context.makeContentWriter();
            Writer writer = new OutputStreamWriter(contentWriter.getContentOutputStream());
            processTemplate(context, templateNode, model, writer);
            writer.flush();
            writer.close();
        }
        catch (Exception ex)
        {
            log.warn("Unexpected error while rendering through XSLT rendering engine.", ex);
        }
    }

    private void processTemplate(RenderingContext context, NodeRef templateNode, XSLTemplateModel model, Writer out)
    {
        String template = context.getCheckedParam(PARAM_TEMPLATE, String.class);
        if (template != null)
        {
            templateService.processTemplateString("xslt", (String) template, model, out);
        }
        else if (templateNode != null)
        {
            templateService.processTemplate("xslt", templateNode.toString(), model, out);
        }
        else
        {
            throwTemplateParamsNotFoundException();
        }
    }

    private void throwTemplateParamsNotFoundException()
    {
        StringBuilder msg = new StringBuilder("This action requires that either the ");
        msg.append(PARAM_TEMPLATE);
        msg.append(" parameter or the ");
        msg.append(PARAM_TEMPLATE_NODE);
        msg.append(" parameter be specified. ");
        throw new RenditionServiceException(msg.toString());
    }

    private NodeRef getTemplateNode(RenderingContext context)
    {
        NodeRef node = context.getCheckedParam(PARAM_TEMPLATE_NODE, NodeRef.class);
        if (node == null)
        {
            String path = context.getCheckedParam(PARAM_TEMPLATE_PATH, String.class);
            if (path != null && path.length() > 0)
            {
                StoreRef storeRef = context.getDestinationNode().getStoreRef();
                ResultSet result = searchService.query(storeRef, SearchService.LANGUAGE_XPATH, path);
                if (result.length() != 1)
                {
                    throw new RenditionServiceException("Could not find template node for path: " + path);
                }
                node = result.getNodeRef(0);
            }
        }
        return node;
    }

    @SuppressWarnings("serial")
    protected XSLTemplateModel buildModel(RenderingContext context, Map<String, Serializable> suppliedParams)
            throws IOException, SAXException
    {
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
        model.put(XSLTProcessor.ROOT_NAMESPACE, XMLUtil.parse(sourceNode, contentService));
        return model;
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

    /*
     * @seeorg.alfresco.repo.rendition.executer.AbstractRenderingEngine# getParameterDefinitions()
     */
    @Override
    protected Collection<ParameterDefinition> getParameterDefinitions()
    {
        Collection<ParameterDefinition> paramList = super.getParameterDefinitions();
        ParameterDefinitionImpl modelParamDef = new ParameterDefinitionImpl(PARAM_MODEL, DataTypeDefinition.ANY, false,
                getParamDisplayLabel(PARAM_MODEL));
        ParameterDefinitionImpl templateParamDef = new ParameterDefinitionImpl(//
                PARAM_TEMPLATE, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_TEMPLATE));
        ParameterDefinitionImpl templateNodeParamDef = new ParameterDefinitionImpl(PARAM_TEMPLATE_NODE,
                DataTypeDefinition.NODE_REF, false, getParamDisplayLabel(PARAM_TEMPLATE_NODE));
        ParameterDefinitionImpl templatePathParamDef = new ParameterDefinitionImpl(PARAM_TEMPLATE_PATH,
                DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_TEMPLATE_PATH));
        paramList.add(modelParamDef);
        paramList.add(templateParamDef);
        paramList.add(templateNodeParamDef);
        paramList.add(templatePathParamDef);
        return paramList;
    }

    /**
     * @param templateService
     *            the templateService to set
     */
    public void setTemplateService(TemplateService templateService)
    {
        this.templateService = templateService;
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

    /**
     * @param searchService
     *            the searchService to set
     */
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }
}
