/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.repo.jscript;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.namespace.QName;

/**
 * Specialised map class for supporting the initialisation of 'cm:content' properties for JavaScript API
 * objects. The JavaScript needs supporting objects to be initialised for certain data-types. If the
 * 'cm:content' property is not already initialised then it must be created on demand or it will not be
 * available to the users of the API. See AR-1673.
 * 
 * @author Kevin Roast
 */
public class ContentAwareScriptableQNameMap<K,V> extends ScriptableQNameMap<K,V>
{
    private ServiceRegistry services;
    private ScriptNode factory;
    
    public ContentAwareScriptableQNameMap(ScriptNode factory, ServiceRegistry services)
    {
        super(services.getNamespaceService());
        this.services = services;
        this.factory = factory;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.namespace.QNameMap#get(java.lang.Object)
     */
    @Override
    public Object get(Object name)
    {
        Object value = super.get(name);
        
        if (value == null)
        {
           // convert the key to a qname and look up the data-type for the property
           QName qname = QName.resolveToQName(this.resolver, name.toString());
           PropertyDefinition propDef = this.services.getDictionaryService().getProperty(qname);
           if (propDef != null && DataTypeDefinition.CONTENT.equals(propDef.getDataType().getName()))
           {
               // found a valid cm:content property that is not initialised
               String mimetype = null;
               String fileName = (String)get("cm:name");
               if (fileName != null)
               {
                   mimetype = this.services.getMimetypeService().guessMimetype(fileName);
               }
               ContentData cdata = new ContentData(null, mimetype, 0L, "UTF-8");
               // create the JavaScript API object we need
               value = factory.new ScriptContentData(cdata, ContentModel.PROP_CONTENT);
               // and store it so it is available to the API user
               put(name, value);
           }
        }
        
        return value;
    }
}
