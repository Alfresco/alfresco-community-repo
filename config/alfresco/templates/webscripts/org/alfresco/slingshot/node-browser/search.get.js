function main()
{
   var query = args.q,
      store = args.store,
      lang = args.lang;
   
   var sort1 =
   {
      column: "@{http://www.alfresco.org/model/content/1.0}name",
      ascending: true
   };
   var sort2 =
   {
      column: "@{http://www.alfresco.org/model/content/1.0}created",
      ascending: false
   };
   var paging =
   {
      maxItems: 100,
      skipCount: 0
   };
   var def =
   {
      query: query,
      store: store,
      language: lang,
      sort: [sort1],
      page: paging
   };
   model.results = search.query(def);
}
main();