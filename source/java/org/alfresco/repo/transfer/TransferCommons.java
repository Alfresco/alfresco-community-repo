package org.alfresco.repo.transfer;

/**
 * A bucket for little odds and ends for the transfer service.
 *
 * If this becomes a big class then refactor it away.
 *
 * @author Mark Rogers
 */
public class TransferCommons
{
    /**
     * The Mime Part Name of the manifest file
     */
    public final static String PART_NAME_MANIFEST = "manifest";

    /**
     * The Query String for the begin method.
     */
    public final static String PARAM_FROM_REPOSITORYID = "fromRepositoryId";

    /**
     * The Query String for the begin method.
     */
    public final static String PARAM_ALLOW_TRANSFER_TO_SELF = "allowTransferToSelf";

    /**
     *  If this returns true, then the transfer service reports should only contain entries about:
     *  Create, Update, Delete items ; see MNT-14059
     * 
     */
    public static final String TS_SIMPLE_REPORT = "transferservice.simple-report";
    
    
    /**
     * TransferId
     */
    public final static String PARAM_TRANSFER_ID = "transferId";

    /**
     * Major version
     */
    public final static String PARAM_VERSION_MAJOR = "versionMajor";

    /**
     * Minor version
     */
    public final static String PARAM_VERSION_MINOR = "versionMinor";

    /**
     * Revision version
     */
    public final static String PARAM_VERSION_REVISION = "versionRevision";

    /**
     * Edition
     */
    public final static String PARAM_VERSION_EDITION = "versionEdition";

    /**
     * File Root File Transfer
     */
    public final static String PARAM_ROOT_FILE_TRANSFER = "rootFileTransfer";


    /**
     * Mapping between contentUrl and part name.
     *
     * @param contentUrl String
     * @return the part name
     */
    public final static String URLToPartName(String contentUrl)
    {
        return contentUrl.substring(contentUrl.lastIndexOf('/')+1);
    }
}
