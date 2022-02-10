<html>
<head>
   <title>Upload Avatar Success</title>
</head>
<body>
<#if (args.success!"")?matches("^[\\w\\d\\._]+$")>
   <script type="text/javascript">
      ${args.success}({
         nodeRef: "${image.nodeRef}",
         fileName: "${image.name}"
      });
   </script>
</#if>
</body>
</html>