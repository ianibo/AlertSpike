<% response.setContentType("application/atom+xml") 
%><feed xmlns="http://www.w3.org/2005/Atom">
  <title type="text"></title>
  <link rel="alternate" type="text/html" href="http://blogito.org/"/>
  <link rel="self" type="application/atom+xml" href="http://blogito.org/entry/atom" />
  <updated><g:atomDate>${lastUpdated}</g:atomDate></updated>
  <author><name></name></author>
  <id></id>
  <generator uri="" version="0.1"></generator>

  <g:each in="${entryInstanceList}" status="i" var="entryInstance">
<g:render template="atomEntry" bean="${entryInstance}" var="entryInstance" />
  </g:each>

</feed>
