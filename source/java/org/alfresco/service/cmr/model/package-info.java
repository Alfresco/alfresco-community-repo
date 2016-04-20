
/**
   FileFolderService
   <p/>
   These services give <b>much</b> simpler APIs for manipulating nodes structures
   conforming to traditional file/folder trees within the data dictionary.
   <p/>
   When using these methods please be aware that they only work with File/Folder trees linked
   together by a primary association of type cm:contains.   In particular these methods do not
   work on the association above "company home"
   <p/>
   FileFolderService provides the public service.
   <p>
   FileFolderUtil provides a utility methods.
 */
@PackageMarker
package org.alfresco.service.cmr.model;
import org.alfresco.util.PackageMarker;

