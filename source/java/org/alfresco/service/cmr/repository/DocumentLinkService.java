package org.alfresco.service.cmr.repository;

/**
 * Provides methods specific to manipulating links of documents
 * 
 * @author Ana Bozianu
 * @since 5.1
 */
public interface DocumentLinkService
{

    /**
     * Creates a link node as child of the destination node
     * 
     * @param source
     *            Node to create a link for. Can be a file or a folder.
     * @param destination
     *            Destination to create the link in. Must be a folder.
     * @return A reference to the created link node
     */
    public NodeRef createDocumentLink(NodeRef source, NodeRef destination);

    /**
     * Returns the destination node of the provided link
     * 
     * @param linkNodeRef
     *            The link node.
     * @return A reference to the destination of the provided link node
     */
    public NodeRef getLinkDestination(NodeRef linkNodeRef);

    /**
     * Deletes all links having the provided node as destination.
     * 
     * @param document
     *            The destination of the links to be deleted.
     */
    public DeleteLinksStatusReport deleteLinksToDocument(NodeRef document);

}
