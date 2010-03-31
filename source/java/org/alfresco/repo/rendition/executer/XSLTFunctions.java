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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.XMLUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * @author Brian Remmington
 * @since 3.3
 * 
 */
public class XSLTFunctions
{
    private static final Log log = LogFactory.getLog(XSLTFunctions.class);
    
    private FileFolderService fileService;
    private ContentService contentService;
    private NodeService nodeService;
    private NamespaceService namespaceService;
    private DictionaryService dictionaryService;

    public XSLTFunctions()
    {
    }

    public Document parseXMLDocument(final NodeRef root, String repoPath) throws IOException, SAXException,
            FileNotFoundException
    {
        String[] pathElements = breakDownPath(repoPath);
        FileInfo file = fileService.resolveNamePath(root, Arrays.asList(pathElements));
        return XMLUtil.parse(file.getNodeRef(), contentService);
    }

    public Map<String, Document> parseXMLDocuments(final String typeName, NodeRef rootNode, String repoPath)
            throws IOException, SAXException
    {
        final Map<String, Document> result = new TreeMap<String, Document>();

        String[] pathElements = breakDownPath(repoPath);

        try
        {
            FileInfo file = fileService.resolveNamePath(rootNode, Arrays.asList(pathElements));

            if (file.isFolder())
            {
                QName typeQName = QName.createQName(typeName, namespaceService);
                Set<QName> types = new HashSet<QName>(dictionaryService.getSubTypes(typeQName, true));
                types.add(typeQName);
                List<ChildAssociationRef> children = nodeService.getChildAssocs(file.getNodeRef(), types);
                for (ChildAssociationRef child : children)
                {
                    String name = (String) nodeService.getProperty(child.getChildRef(), ContentModel.PROP_NAME);
                    Document doc = XMLUtil.parse(child.getChildRef(), contentService);
                    result.put(name, doc);
                }
            }
        }
        catch (Exception ex)
        {
            log.warn("Unexpected exception caught in call to parseXMLDocuments", ex);
        }
        return result;
    }

    /**
     * @param repoPath
     * @return
     */
    private String[] breakDownPath(String repoPath)
    {
        if (repoPath.startsWith("/"))
        {
            repoPath = repoPath.substring(1);
        }
        String[] pathElements = repoPath.split("/");
        return pathElements;
    }

    /**
     * Encodes invalid HTML characters. (Fix for ETWOTWO-504 issue) This code was adopted from WebDAVHelper.encodeHTML()
     * method with some restrictions.
     * 
     * @see press-release.xsl for pattern.
     * 
     * @param text
     *            to encode
     * @return encoded text
     * @throws IOException
     * @throws SAXException
     */
    public String encodeQuotes(String text) throws IOException, SAXException
    {
        if (text == null)
        {
            return "";
        }

        StringBuilder sb = null; // create on demand
        String enc;
        char c;
        for (int i = 0; i < text.length(); i++)
        {
            enc = null;
            c = text.charAt(i);
            switch (c)
            {
            case '"':
                enc = "&quot;";
                break; // "
            // case '&': enc = "&amp;"; break; //&
            // case '<': enc = "&lt;"; break; //<
            // case '>': enc = "&gt;"; break; //>

            // german umlauts
            case '\u00E4':
                enc = "&auml;";
                break;
            case '\u00C4':
                enc = "&Auml;";
                break;
            case '\u00F6':
                enc = "&ouml;";
                break;
            case '\u00D6':
                enc = "&Ouml;";
                break;
            case '\u00FC':
                enc = "&uuml;";
                break;
            case '\u00DC':
                enc = "&Uuml;";
                break;
            case '\u00DF':
                enc = "&szlig;";
                break;

            // misc
            // case 0x80: enc = "&euro;"; break; sometimes euro symbol is ascii 128, should we suport it?
            case '\u20AC':
                enc = "&euro;";
                break;
            case '\u00AB':
                enc = "&laquo;";
                break;
            case '\u00BB':
                enc = "&raquo;";
                break;
            case '\u00A0':
                enc = "&nbsp;";
                break;

            // case '': enc = "&trade"; break;

            default:
                if (((int) c) >= 0x80)
                {
                    // encode all non basic latin characters
                    enc = "&#" + ((int) c) + ";";
                }
                break;
            }

            if (enc != null)
            {
                if (sb == null)
                {
                    String soFar = text.substring(0, i);
                    sb = new StringBuilder(i + 8);
                    sb.append(soFar);
                }
                sb.append(enc);
            }
            else
            {
                if (sb != null)
                {
                    sb.append(c);
                }
            }
        }

        if (sb == null)
        {
            return text;
        }
        else
        {
            return sb.toString();
        }
    }

    /**
     * @param fileService the fileService to set
     */
    public void setFileService(FileFolderService fileService)
    {
        this.fileService = fileService;
    }

    /**
     * @param contentService the contentService to set
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    /**
     * @param nodeService the nodeService to set
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param namespaceService the namespaceService to set
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    /**
     * @param dictionaryService the dictionaryService to set
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
}
