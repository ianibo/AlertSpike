package capalerts

import grails.converters.JSON
import grails.converters.XML

class ProfileController {

  def ESWrapperService

  def index() { 
    log.debug("ProfileController::index - list current alerts");
    def result = [:]
    result.alerts = AlertProfile.executeQuery('select ap from AlertProfile as ap where ap.name like :profileNameQry',[profileNameQry:'%'], [max: 10, offset: 0]);

    withFormat {
      html result
      json { render result as JSON }
    }
  }

  def feed() {
    log.debug("ProfileController::feed(${params.id})");


    def result = [:]
    result.offset = 0;
    result.max = 10;

    def esclient = ESWrapperService.getClient();

    if ( ( params.id ) && ( esclient ) ) {
      result.alert = AlertProfile.get(params.id)
      if ( result.alert ) {
        log.debug("Got feed ${result.alert.id} ${result.alert.name} ${result.alert.shapeType} ${result.alert.shapeCoordinates} ${result.alert.radius}");

        def query_str="*"
        // def sq = [ [ [ 179, 89 ], [ -179, 89 ], [ -179, -89 ], [ 179, -89 ], [ 179, 89 ] ] ]
        def sq = JSON.parse(result.alert.shapeCoordinates)
        log.debug("Parsed query coordinates ${sq}");

        try {

          def search_future = null;
          if ( result.alert.shapeType.equalsIgnoreCase('polygon') ) {
            search_future = esclient.search {
                       indices 'alerts'
                       types 'alert'
                       source {
                         from = result.offset
                         size = result.max
                         query {
                           bool {
                             must {
                               query_string (query: '*')
                               // match_all {}
                             }
                             filter {
                               nested {
                                 path = 'areas'
                                 filter {
                                   geo_shape {
                                     areas.alertShape {
                                       shape {
                                         type = "polygon"
                                         coordinates = sq // result.alert.shapeCoordinates
                                       }
                                       relation = "intersects"
                                     }
                                   }
                                 }
                               }
                             }
                           }
                         }
                         sort = [
                           agentts : [order : "desc"]
                         ]

                         // facets {
                         //   'Component Type' {
                         //     terms {
                         //       field = grailsApplication.config.globalSearch.typingField
                         //     }
                         //   }
                         // }
                       }
                     }
          }
          else {
            search_future = esclient.search {
                       indices 'alerts'
                       types 'alert'
                       source {
                         from = result.offset
                         size = result.max
                         query {
                           bool {
                             must {
                               query_string (query: '*')
                               // match_all {}
                             }
                             filter {
                               nested {
                                 path = 'areas'
                                 filter {
                                   geo_shape {
                                     areas.alertShape {
                                       shape {
                                         type = "circle"
                                         coordinates = sq // result.alert.shapeCoordinates
                                         radius =  result.alert.radius
                                       }
                                       relation = "intersects"
                                     }
                                   }
                                 }
                               }
                             }
                           }
                         }
                         sort = [
                           agentts : [order : "desc"]
                         ]
                       }
                     }
          }

          def search_response =  search_future.get()
          // log.debug("Got response: ${search_response}");
          result.hitcount = search_response.hits.totalHits
          result.hits = []
          search_response.hits.hits.each { hit ->
            log.debug("Adding hit ${hit}");
            result.hits.add(hit.source)
          }
        }
        catch ( Exception e ) {
          log.error("Error processing search", e);
        }

      }
      else {
        result.message = "Unable to locate profile with id "+params.id;
      }
    }
    else {
    }

    withFormat {
      html result
      json { render result as JSON }
      atom { 
        render(text: asAtom(result), contentType: "text/xml")
      }
    }

  }

  def asAtom(searchResult) {
    
    // <feed xmlns="http://www.w3.org/2005/Atom">
    //   <title>Example Feed</title>
    //   <link href="http://example.org/"/>
    //   <updated>2003-12-13T18:30:02Z</updated>
    //   <author>
    //     <name>John Doe</name>
    //   </author>
    //   <id>urn:uuid:60a76c80-d399-11d9-b93C-0003939e0af6</id>
    //   <entry>
    //     <title>Atom-Powered Robots Run Amok</title>
    //     <link href="http://example.org/2003/12/13/atom03"/>
    //     <id>urn:uuid:1225c695-cfb8-4ebb-aaaa-80da344efa6a</id>
    //     <updated>2003-12-13T18:30:02Z</updated>
    //     <summary>Some text.</summary>
    //   </entry>
    // </feed>



    def writer = new StringWriter()
    def xml = new groovy.xml.MarkupBuilder(writer)
    xml.feed(xmlns:"http://www.w3.org/2005/Atom"){
      title("CAP GEO Alerts")
      updated("Updated")
      author {
        name("Test Name")
      }
      searchResult.hits.each { h ->
        entry {
          title(h.headline)
          summary(h.description)
          linkw(h.web)
          // updated(h.description)
          // id(h.description)
        }
      }
    }
    writer.toString()
  }

}
