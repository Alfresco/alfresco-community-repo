/*
 * Copyright (C) 2005 Jesper Steen Møller
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.action.executer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.metadata.MetadataExtracter;
import org.alfresco.repo.content.metadata.MetadataExtracterRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

/**
 * Extract metadata from any added content.
 * <p>
 * The metadata is extracted from the content and compared to the current
 * property values.  Missing or zero-length properties are replaced,
 * otherwise they are left as is.<br/>
 * <i>This may change if the action gets parameterized in future</i>.
 * 
 * @author Jesper Steen Møller
 */
public class ContentMetadataExtracter extends ActionExecuterAbstractBase
{
    /**
     * Action constants
     */
    public static final String NAME = "extract-metadata";

    /*
     * TODO: Action parameters.
     * 
     * Currently none exist, but it may be nice to add a 'policy' parameter for
     * overwriting the extracted properties, with the following possible values:
     * 1) Never: Never overwrite node properties that
     * exist (i.e. preserve values, nulls, and blanks)
     * 2) Pragmatic: Write
     * extracted properties if they didn't exist before, are null, or evaluate
     * to an empty string.
     * 3) Always: Always store the extracted properes.
     * 
     * Policies 1 and 2 will preserve previously set properties in case nodes
     * are moved/copied, making this action run on the same content several
     * times. However, if a property is deliberately cleared (e.g. by putting
     * the empty string into the "decription" field), the pragmatic policy would
     * indeed overwrite it. The current implementation matches the 'pragmatic'
     * policy.
     */

    /**
     * The node service
     */
    private NodeService nodeService;

    /**
     * Set the node service
     * 
     * @param nodeService the node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Our content service
     */
    private ContentService contentService;

    /**
     * @param contentService The contentService to set.
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }
    
    /**
     * The dictionary service
     */
    private DictionaryService dictionaryService;
    
    /**
     * @param dictService  The DictionaryService to set.
     */
    public void setDictionaryService(DictionaryService dictService)
    {
        this.dictionaryService = dictService;
    }

    /**
     * Our Extracter
     */
    private MetadataExtracterRegistry metadataExtracterRegistry;

    /**
     * @param metadataExtracterRegistry The metadataExtracterRegistry to set.
     */
    public void setMetadataExtracterRegistry(MetadataExtracterRegistry metadataExtracterRegistry)
    {
        this.metadataExtracterRegistry = metadataExtracterRegistry;
    }

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuter#execute(org.alfresco.service.cmr.repository.NodeRef,
     *      NodeRef)
     */
    public void executeImpl(Action ruleAction, NodeRef actionedUponNodeRef)
    {
        if (this.nodeService.exists(actionedUponNodeRef) == true)
        {
            ContentReader cr = contentService.getReader(actionedUponNodeRef, ContentModel.PROP_CONTENT);

            // 'cr' may be null, e.g. for folders and the like
            if (cr != null && cr.getMimetype() != null)
            {
                MetadataExtracter me = metadataExtracterRegistry.getExtracter(cr.getMimetype());
                if (me != null)
                {
                    Map<QName, Serializable> newProps = new HashMap<QName, Serializable>(7, 0.5f);
                    me.extract(cr, newProps);

                    Map<QName, Serializable> allProps = nodeService.getProperties(actionedUponNodeRef);

                    /*
                     * The code below implements a modestly conservative
                     * 'preserve' policy which shouldn't override values
                     * accidentally.
                     */

                    boolean changed = false;
                    for (QName key : newProps.keySet())
                    {
                        // check if we need to add an aspect for the prop
                        ClassDefinition propClass = dictionaryService.getProperty(key).getContainerClass();
                        if (propClass.isAspect() &&
                            nodeService.hasAspect(actionedUponNodeRef, propClass.getName()) == false)
                        {
                            Map<QName, Serializable> aspectProps = new HashMap<QName, Serializable>(3, 1.0f);
                            for (QName defKey : propClass.getProperties().keySet())
                            {
                                if (dictionaryService.getProperty(defKey).isMandatory())
                                {
                                    aspectProps.put(defKey, allProps.get(defKey));
                                    allProps.remove(defKey);
                                }
                            }
                            nodeService.addAspect(actionedUponNodeRef, propClass.getName(), aspectProps);
                        }
                        
                        Serializable value = newProps.get(key);
                        if (value == null)
                        {
                            continue; // Content extracters shouldn't do this
                        }
                        
                        // Look up the old value, and check for nulls
                        Serializable oldValue = allProps.get(key);
                        if (oldValue == null || oldValue.toString().length() == 0)
                        {
                            allProps.put(key, value);
                            changed = true;
                        }
                    }
                    
                    if (changed)
                    {
                        nodeService.setProperties(actionedUponNodeRef, allProps);
                    }
                }
            }
        }
    }

    @Override
    protected void addParameterDefintions(List<ParameterDefinition> arg0)
    {
        // None!
    }
}