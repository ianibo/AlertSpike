<html>
  <head>
    <meta name="layout" content="main"/>
  </head>
  <body>
    <g:form action="index">
      Profile : <input name="name" type="text"></input><br/>
      Shape Type : <select name="type">
        <option value="circle">Circle</option>
        <option value="polygon">Polygon</option>
      </select><br/>
      Shape : <input name="coordinates" type="text"></input><br/>
    </g:form>

    <table class="table table-striped table-bordered">
      <thead>
        <tr>
          <th>ID</th>
          <th>Name</th>
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
            <td>${p.shapeType}</td>
            <td>${p.shapeCoordinates}</td>
            <td>${p.radius}</td>
          </tr>
        </g:each>
      </tbody>
    </table>
  </body>
</html>
