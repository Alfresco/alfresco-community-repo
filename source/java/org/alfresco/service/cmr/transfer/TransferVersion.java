package org.alfresco.service.cmr.transfer;

public interface TransferVersion
{
   /**
    * Gets the major version number, e.g. <u>1</u>.2.3
    *
    * @return  major version number
    */
   public String getVersionMajor();

   /**
    * Gets the minor version number, e.g. 1.<u>2</u>.3
    *
    * @return  minor version number
    */
   public String getVersionMinor();

   /**
    * Gets the version revision number, e.g. 1.2.<u>3</u>
    *
    * @return  revision number
    */
   public String getVersionRevision();

   /**
    * Gets the edition
    *
    * @return  the edition
    */
   public String getEdition();

}
