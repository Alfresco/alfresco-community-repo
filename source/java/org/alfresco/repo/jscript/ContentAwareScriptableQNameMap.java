package org.alfresco.repo.jscript;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespacePrefixResolverProvider;
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
    
    
    /**
     * Constructor
     * 
     * @param factory       Factory to provide further ScriptNode objects
     * @param services      ServiceRegistry
     */
    public ContentAwareScriptableQNameMap(final ScriptNode factory, final ServiceRegistry services)
    {
        super(new NamespacePrefixResolverProvider(){
            public NamespacePrefixResolver getNamespacePrefixResolver()
            {
                return services.getNamespaceService();
            }
        });
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
           QName qname = QName.resolveToQName(getResolver(), name.toString());
           PropertyDefinition propDef = this.services.getDictionaryService().getProperty(qname);
           if (propDef != null && DataTypeDefinition.CONTENT.equals(propDef.getDataType().getName()))
           {
               // found a valid cm:content property that is not initialised
               String mimetype = null;
               if (qname.equals(ContentModel.PROP_CONTENT)) 
               {
                   String fileName = (String)get("cm:name");
                   if (fileName != null)
                   {
                       // We don't have any content, so just use the filename when
                       //  trying to guess the mimetype for this
                       mimetype = this.services.getMimetypeService().guessMimetype(fileName);
                   }
               }
               ContentData cdata = new ContentData(null, mimetype, 0L, "UTF-8");
               // create the JavaScript API object we need
               value = factory.new ScriptContentData(cdata, qname);
               // and store it so it is available to the API user
               put(name, value);
           }
        }
        
        return value;
    }
}
