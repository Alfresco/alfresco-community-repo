<html>
<head>
 <title>User CSV Uploader</title>
</head>
<body>
 <h1>User CSV Uploader</h1>
 <form method="post" enctype="multipart/form-data" action="upload">
  <p>Please upload a CSV containing the details of your users to be created.</p>
  <p><input type="file" name="csv" /></p>
  <p><input type="submit" value="Upload" /></p>
 </form>
 
 <hr />

 <ul>
  <li><a href="?format=csv">Download template CSV</a></li>
  <li><a href="?format=xls">Download template XLS (Excel)</a></li>
  <li><a href="?format=xlsx">Download template XLSX (Excel 2007/2010)</a></li>
 </ul>
</body>
</html>
