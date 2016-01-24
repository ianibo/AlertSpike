<html>
  <head>
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

    <table>
      <g:each in="${alerts}" var="p">
        <tr>
          <td>${p.name}</td>
          <td>${p.shapeType}</td>
          <td>${p.shapeCoordinates}</td>
          <td>${p.radius}</td>
        </tr>
      </g:each>
    </table>
  </body>
</html>
