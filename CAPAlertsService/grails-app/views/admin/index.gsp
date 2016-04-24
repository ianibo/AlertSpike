<html>
  <head>
    <meta name="layout" content="main"/>
  </head>
  <body>
    <div class="container">
      <div class="well">
        <p>
          Use this form to upload a tab separated .tsv file with the following columns in the specified order
          <ul>
            <li>subscription-id</li>
            <li>keys-pending</li>
            <li>subscription-name</li>
            <li>subscription-url</li>
            <li>feed-language</li>
            <li>high-priority</li>
            <li>official-only</li>
            <li>x-path-filter-id</li>
            <li>x-path-filter</li>
            <li>area-filter-id</li>
            <li>shape-type</li>
            <li>coordinates</li>
            <li>radius</li>
          </ul>
          The first line MUST be the header line and not a profile (It will be ignored). subscription-name is a display name, subscription-id must be a unique and is used on the URL to identify this feed,shapeType must be one of "circle" or "polygon", coodinates must be a GeoJSON encoded closed polygon if shapeType is set to polygon, or the center point if set to circle. Radius is required for type circle and optional for polygon.
        </p>
        <g:form action="uploadProfiles" method="post" enctype="multipart/form-data">
          Profile csv file : <input type="file" name="content">
          <input type="submit"/>
        </g:form>
      </div>
    </div>
  </body>
</html>

