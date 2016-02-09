<html>
<head>
  <meta name="layout" content="main"/>
</head>
<body>
  <div class="container">
    <div class="row">
      <div class="col-md-6">
      </div>
      <div class="col-md-6">
        <g:link controller="profile" action="feed" id="${params.id}" params="${[format:'atom']}">ATOM</g:link>
        <g:link controller="profile" action="feed" id="${params.id}" params="${[format:'json']}">JSON</g:link>
        <table class="table table-striped table-bordered">
          <thead>
            <tr>
              <th>Event</th>
            </tr>
          </thead>
          <tbody>
            <g:each in="${hits}" var="h">
              <tr>
                <td><a href="${h.web}">${h.headline}</a> [${h.category}]<br/>
                ${h.description}</td>
              </tr>
            </g:each>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</body>
</html>

