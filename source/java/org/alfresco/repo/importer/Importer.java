package org.alfresco.repo.importer;

import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.view.Location;
import org.alfresco.service.namespace.QName;

/**
 * The Importer interface encapusulates the strategy for importing 
 * a node into the Repository. 
 * 
 * @author David Caruana
 */
public interface Importer
{
    /**
     * @return  the root node to import into
     */
    public NodeRef getRootRef();
    
    /**
     * @return  the root child association type to import under
     */
    public QName getRootAssocType();

    /**
     * @return  the location to import under
     */
    public Location getLocation();

    /**
     * Signal start of import
     */
    public void start();

    /**
     * Signal end of import
     */
    public void end();

    /**
     * Signal import error
     */
    public void error(Throwable e);
    
    /**
     * Import meta-data
     */
    public void importMetaData(Map<QName, String> properties);
    
    /**
     * Import a node
     * 
     * @param node  the node description
     * @return  the node ref of the imported node
     */
    public NodeRef importNode(ImportNode node);

    /**
     * Resolve path within context of root reference
     * 
     * @param path  the path to resolve
     * @return  node reference
     */
    public NodeRef resolvePath(String path);
    
    /**
     * Is excluded Content Model Class?
     * 
     * @param  className  the class name to test
     * @return  true => the provided class is excluded from import
     */
    public boolean isExcludedClass(QName className);
    
    /**
     * Signal completion of node import
     * 
     * @param nodeRef  the node ref of the imported node
     */
    public void childrenImported(NodeRef nodeRef);
}
