//
// window.onload function for r:page tag
//

function onloadFunc(webdavUrl, cifsPath)
{
   if (webdavUrl != "")
   {
      openDoc(webdavUrl);
   }
   if (cifsPath != "")
   {
      window.open(cifsPath, "_blank");
   }
}