feed.gsp
<ul>
  <g:each in="${hits}" var="h">
    <li>
      ${h.id} ${h.description} ${h.category} ${h.event} ${h.web} ${h.headline}
    </li>
  </g:each>
</ul>

