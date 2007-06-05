var query = "TYPE:\"{http://www.alfresco.org/model/content/1.0}person\"";

if ((args.s) && (args.s != ""))
{
   query += " AND (";
   var terms = args.s.split(" ");
   for (i = 0; i < terms.length; i++)
   {
      term = terms[i];
      query += "((@\\{http\\://www.alfresco.org/model/content/1.0\\}firstName:" + term;
//      query += "*) OR (@\\{http\\://www.alfresco.org/model/content/1.0\\}lastName:*" + term;
//      query += "*) OR (@\\{http\\://www.alfresco.org/model/content/1.0\\}userName:" + term;
      query += "*)) ";   // final space here is important as default OR separator
   }
   query += ")";
}

model.searchResults = search.luceneSearch(query);
