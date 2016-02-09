<html>
  <head>
    <meta name="layout" content="main"/>
  </head>
  <body>
    <div class="container">
      <div class="well">
        <p>
          Use this form to upload a comma separated .csv file of the form
          <pre>
profileName,shapeType,coordinates,radius
"Flagstaff Co. near Killam and Sedgewick",polygon,"[[[-111.5995,52.9305],[-111.876,52.6676],[-111.9873,52.9598],[-111.8906,52.9597],[-111.5995,52.9305]]]"
"Sheffield, UK", "circle","[ -1.466944, 53.383611 ]","5000m"
"World","polygon","[ [ [ 179, 89 ], [ -179, 89 ], [ -179, -89 ], [ 179, -89 ], [ 179, 89 ] ] ]"
          </pre>
          The first line MUST be the header line and not a profile (It will be ignored). profileName must be unique, shapeType must be one of "circle" or "polygon", coodinates must be a GeoJSON encoded closed polygon if shapeType is set to polygon, or the center point if set to circle. Radius is required for type circle and optional for polygon.
        </p>
        <g:form action="uploadProfiles" method="post" enctype="multipart/form-data">
          Profile csv file : <input type="file" name="content">
          <input type="submit"/>
        </g:form>
      </div>
    </div>
  </body>
</html>

