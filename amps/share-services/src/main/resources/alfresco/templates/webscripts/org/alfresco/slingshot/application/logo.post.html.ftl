<html>
<head>
   <title>Upload Logo Success</title>
</head>
<body>
<#if (args.success!"")?matches("^[\\w\\d\\._]+$")>
   <script type="text/javascript">
      ${args.success}({
         nodeRef: "${logo.nodeRef}",
         fileName: "${name}"
      });
   </script>
</#if>
</body>
</html>