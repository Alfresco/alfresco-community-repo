<html>
<head>
   <title>Upload Success</title>
</head>
<body>
<#if (args.success!"")?matches("^[\\w\\d\\._]+$")>
   <script type="text/javascript">
      ${args.success}({
         nodeRef: "${document.nodeRef}",
         fileName: "${document.name}"
      });
   </script>
</#if>
</body>
</html>