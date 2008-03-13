//
// window.onload function for r:page tag
//

function onloadFunc(webdavUrl, cifsPath)
{
   // i place this here only for others usage of onloadFunc
   if (webdavUrl != "")
      openDoc(webdavUrl);
   if (cifsPath != "")
      window.open(cifsPath, "_blank");
}
