<html>
  <head>
    <meta name="layout" content="main"/>
  </head>
  <body>
    <table class="table table-striped table-bordered">
      <thead>
        <tr>
          <th>ID</th>
          <th>Name</th>
          <th>Shortcode</th>
          <th>Type</th>
          <th>Coords</th>
          <th>Radius</th>
        </tr>
      </thead>
      <tbody>
        <g:each in="${alerts}" var="p">
          <tr>
            <td><g:link controller="profile" action="feed" id="${p.id}">${p.id}</g:link></td>
            <td>${p.name}</td>
            <td>${p.shortcode}</td>
            <td>${p.shapeType}</td>
            <td>${p.shapeCoordinates}</td>
            <td>${p.radius}</td>
          </tr>
        </g:each>
      </tbody>
    </table>
  </body>
</html>
