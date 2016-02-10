<html>
<head>
  <meta name="layout" content="main"/>
  <link rel="stylesheet" href="http://cdn.leafletjs.com/leaflet/v0.7.7/leaflet.css" />
  <script src="http://cdn.leafletjs.com/leaflet/v0.7.7/leaflet.js"></script>
  <style>
html,body {width:100%;height:100%;margin:0;padding:0;}

#map {
    height:800px;
    bottom:0;
    top:0;
    left:0;
    right:0;
}

.main {
  margin-top:100px;
}
  </style>
</head>
<body>
  <div class="container-fluid main">
    <div class="row">
      <div class="col-md-6">
         <div id="map"></div>
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
            <g:each in="${hits}" var="h" status="i">
              <tr>
                <td>
                  <button class="btn pull-right btn-info" onClick="showAlert(${i})">Show</button>
                  <a href="${h.web}">${h.headline}</a> ${h.sourcets}<br/>
                  ${h.description}<br/>
                  [${h.category}]
                </td>
              </tr>
            </g:each>
          </tbody>
        </table>
      </div>
    </div>
  </div>
  <script language="JavaScript">

    var alerts = [
      <g:each in="${hits}" var="g" status="i">
        {
          pageid:${i},
          headline:"${g.headline}", 
          category:"${g.category}", 
          description:"${g.description}", 
          instruction:"${g.instruction}", 
          web:"${g.headline}", 
          event:"${g.event}", 
          agentts:"${g.agentts}", 
          sourcets:"${g.sourcets}", 
          urgency:"${g.urgency}", 
          severity:"${g.severity}",
          areas:[
            <g:each in="${g.areas}" var="a">
              {
                label:"${a.label}",
                shapeType:"${a.alertShape.type}",
                coordinates:${a.alertShape.coordinates},
                radius:"${a.alertShape.radius}"
              },
            </g:each>
          ]
        },
      </g:each>
    ];

    $(document).ready(function() {
      var map = L.map('map').setView([51.505, -0.09], 1);
      L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', {
          attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
      }).addTo(map);

      var lg = L.featureGroup([]);

      var l = alerts.length;
      for (var i = 0; i < l; i++) {
        console.log("Adding geometry %o",alerts[i].areas[0]);

        if ( alerts[i].areas[0].shapeType === 'polygon' ) {
          var geojsonFeature = {
            "type": "Feature",
            "properties": {
                "name": alerts[i].headline,
            },
            "geometry": {
            }
          }
          geojsonFeature.geometry.type='Polygon';
          geojsonFeature.geometry.coordinates=alerts[i].areas[0].coordinates;
          var feature = L.geoJson(geojsonFeature);
          feature.addTo(map);
          feature.addLayer(lg);
        }
        else {

          var geojsonFeature = {
              "type": "Feature",
              "properties": {
                  "name": alerts[i].headline,
              },
              "geometry": {
                  "type": "Point",
                  "coordinates": alerts[i].areas[0].coordinates
              }
          };

          var geojsonMarkerOptions = {
              radius: 8,
              fillColor: "#ff7800",
              color: "#005",
              weight: 1,
              opacity: 1,
              fillOpacity: 0.4
          };

          var latlng = L.latLng( alerts[i].areas[0].coordinates[1], alerts[i].areas[0].coordinates[0]);

          var feature = L.geoJson(geojsonFeature, {
              pointToLayer: function (feature, latlng) {
                  return L.circleMarker(latlng, geojsonMarkerOptions);
              }
          });
          feature.addTo(map);
          feature.addLayer(lg);
        }
      }
      // map.fitBounds(lg.getBounds()); 
    });
  </script>
</body>
</html>

