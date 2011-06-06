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
package org.alfresco.opencmis;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.rendition.executer.ImageRenderingEngine;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.rendition.RenditionDefinition;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.chemistry.opencmis.commons.data.RenditionData;
import org.apache.chemistry.opencmis.commons.exceptions.CmisFilterNotValidException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.RenditionDataImpl;

public class CMISRenditionMapping
{
    private NodeService nodeService;
    private ContentService contentService;
    private RenditionService renditionService;
    private TransactionService transactionService;

    private Map<String, List<String>> kindToRenditionNames;
    private Map<String, String> renditionNamesToKind;
    private Map<String, BigInteger[]> renditionNameToSize;

    public CMISRenditionMapping(NodeService nodeService, ContentService contentService,
            RenditionService renditionService, TransactionService transactionService,
            Map<String, List<String>> renditionKinds)
    {
        this.nodeService = nodeService;
        this.contentService = contentService;
        this.renditionService = renditionService;
        this.transactionService = transactionService;

        if (renditionKinds == null)
        {
            this.kindToRenditionNames = new HashMap<String, List<String>>();
        } else
        {
            this.kindToRenditionNames = renditionKinds;
        }
        renditionNamesToKind = new HashMap<String, String>();
        for (Entry<String, List<String>> entry : renditionKinds.entrySet())
        {
            for (String renditionName : entry.getValue())
            {
                renditionNamesToKind.put(renditionName, entry.getKey());
            }
        }

        cacheRenditionSizes();
    }

    private void cacheRenditionSizes()
    {
        renditionNameToSize = AuthenticationUtil.runAs(new RunAsWork<Map<String, BigInteger[]>>()
        {
            public Map<String, BigInteger[]> doWork() throws Exception
            {
                return transactionService.getRetryingTransactionHelper().doInTransaction(
                        new RetryingTransactionCallback<Map<String, BigInteger[]>>()
                        {
                            public Map<String, BigInteger[]> execute() throws Exception
                            {
                                Map<String, BigInteger[]> rn2s = new HashMap<String, BigInteger[]>();

                                List<RenditionDefinition> allRenditionDefs = renditionService
                                        .loadRenditionDefinitions();
                                for (RenditionDefinition rd : allRenditionDefs)
                                {
                                    QName renditionDefinitionName = rd.getRenditionName();

                                    Number width = (Number) rd
                                            .getParameterValue(ImageRenderingEngine.PARAM_RESIZE_WIDTH);
                                    Number height = (Number) rd
                                            .getParameterValue(ImageRenderingEngine.PARAM_RESIZE_HEIGHT);

                                    if ((width != null) || (height != null))
                                    {
                                        BigInteger[] size = new BigInteger[2];
                                        size[0] = (width == null ? null : BigInteger.valueOf(width.longValue()));
                                        size[1] = (height == null ? null : BigInteger.valueOf(height.longValue()));

                                        rn2s.put(renditionDefinitionName.getLocalName(), size);
                                    }
                                }

                                return rn2s;
                            };
                        }, true);
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    public List<RenditionData> getRenditions(NodeRef nodeRef, String renditionFilter, BigInteger maxItems,
            BigInteger skipCount)
    {
        List<RenditionData> result = new ArrayList<RenditionData>();

        // split the filter
        Set<String> filterSet = splitRenditionFilter(renditionFilter);
        if ((filterSet != null) && (filterSet.contains(CMISConnector.RENDITION_NONE)))
        {
            // "cmis:none" found -> no renditions
            return result;
        }

        // convert BigIntegers to int
        int max = (maxItems == null ? Integer.MAX_VALUE : maxItems.intValue());
        int skip = (skipCount == null || skipCount.intValue() < 0 ? 0 : skipCount.intValue());

        if (max > 0)
        {
            // find all renditions and filter them
            List<ChildAssociationRef> renditionList = renditionService.getRenditions(nodeRef);

            int lastIndex = (max + skip > renditionList.size() ? renditionList.size() : max + skip) - 1;
            for (int i = skip; i <= lastIndex; i++)
            {
                ChildAssociationRef rendition = renditionList.get(i);
                NodeRef rendNodeRef = rendition.getChildRef();
                String rendName = rendition.getQName().getLocalName();

                // get and check content
                QName contentProperty = ContentModel.PROP_CONTENT;
                Serializable contentPropertyName = nodeService.getProperty(rendNodeRef,
                        ContentModel.PROP_CONTENT_PROPERTY_NAME);
                if (contentPropertyName != null)
                {
                    contentProperty = (QName) contentPropertyName;
                }

                ContentReader reader = contentService.getReader(rendNodeRef, contentProperty);
                if ((reader == null) || (!reader.exists()))
                {
                    // no content -> no rendition
                    continue;
                }

                // get and clean MIME type
                String mimeType = reader.getMimetype();
                if (mimeType.indexOf(';') > 3)
                {
                    mimeType = mimeType.substring(0, mimeType.indexOf(';')).trim();
                }

                // if a filter is set, check it
                if (filterSet != null)
                {
                    boolean include = false;
                    for (String f : filterSet)
                    {
                        if (f.indexOf('/') == -1)
                        {
                            // found a kind, not a MIME type
                            List<String> renditionNames = kindToRenditionNames.get(f);
                            if (renditionNames != null && renditionNames.contains(rendName))
                            {
                                include = true;
                                break;
                            }
                        } else if (f.endsWith("*"))
                        {
                            // found MIME type with wildcard
                            if (mimeType.startsWith(f.substring(0, f.length() - 2)))
                            {
                                include = true;
                                break;
                            }
                        } else
                        {
                            // found complete MIME type
                            if (mimeType.equals(f))
                            {
                                include = true;
                                break;
                            }
                        }
                    }

                    // if no filter matches, skip this rendition
                    if (!include)
                    {
                        continue;
                    }
                }

                // gather rendition data
                String title = rendName;
                String kind = (renditionNamesToKind.containsKey(rendName) ? renditionNamesToKind.get(rendName)
                        : rendName);
                BigInteger length = BigInteger.valueOf(reader.getSize());

                BigInteger width = null;
                BigInteger height = null;
                if (renditionNameToSize.containsKey(rendName))
                {
                    BigInteger[] size = renditionNameToSize.get(rendName);
                    width = size[0];
                    height = size[1];
                }

                // finally add this rendition
                result.add(createRenditionData(rendNodeRef, mimeType, title, kind, length, width, height));
            }
        }

        if (filterSet == null || filterSet.contains("cmis:thumbnail"))
        {

        }

        return result;
    }

    private Set<String> splitRenditionFilter(String filter)
    {
        if (filter == null)
        {
            return null;
        }

        if (filter.trim().length() == 0)
        {
            return null;
        }

        Set<String> result = new HashSet<String>();
        for (String s : filter.split(","))
        {
            s = s.trim();
            if (s.equals("*"))
            {
                return null;
            } else if (s.indexOf('*') > -1)
            {
                if (!s.endsWith("*"))
                {
                    throw new CmisFilterNotValidException("Rendition filter is invalid: " + s);
                }
                result.add(s);
            } else if (s.equalsIgnoreCase(CMISConnector.RENDITION_NONE))
            {
                result.clear();
                result.add(CMISConnector.RENDITION_NONE);
                break;
            } else if (s.length() > 0)
            {
                result.add(s);
            }
        }

        return result;
    }

    private RenditionData createRenditionData(NodeRef rendNodeRef, String mimeType, String title, String kind,
            BigInteger length, BigInteger width, BigInteger height)
    {
        RenditionDataImpl result = new RenditionDataImpl();

        result.setStreamId(rendNodeRef.toString());
        result.setMimeType(mimeType);

        result.setTitle(title);
        result.setKind(kind);
        result.setBigLength(length);

        result.setBigWidth(width);
        result.setBigHeight(height);

        result.setRenditionDocumentId(rendNodeRef.toString());

        return result;
    }
}
