package org.alfresco.repo.links;

import org.alfresco.service.namespace.QName;

/**
 * Links models constants
 * 
 * @author Nick Burch
 */
public interface LinksModel
{
    /** Links Model */
    public static final String LINKS_MODEL_URL = "http://www.alfresco.org/model/linksmodel/1.0";
    public static final String LINKS_MODEL_PREFIX = "lnk";
    
    /** Link */
    public static final QName TYPE_LINK = QName.createQName(LINKS_MODEL_URL, "link"); 
    public static final QName PROP_TITLE = QName.createQName(LINKS_MODEL_URL, "title"); 
    public static final QName PROP_DESCRIPTION = QName.createQName(LINKS_MODEL_URL, "description"); 
    public static final QName PROP_URL = QName.createQName(LINKS_MODEL_URL, "url"); 
    
    /** Internal Link */
    public static final QName ASPECT_INTERNAL_LINK = QName.createQName(LINKS_MODEL_URL, "internal");
    public static final QName PROP_IS_INTERNAL = QName.createQName(LINKS_MODEL_URL, "isInternal");
}