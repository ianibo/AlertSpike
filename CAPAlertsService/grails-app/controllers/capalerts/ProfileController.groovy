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

          def search_future = esclient.search {
                       indices 'alerts'
                       types 'alert'
                       source {
                         from = result.offset
                         size = result.max
                         // query {
                         //   query_string (query: query_str)
                         // }

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
    
    def writer = new StringWriter()
    def xml = new groovy.xml.MarkupBuilder(writer)
    xml.langs(type:"current"){
      language("Java")
      language("Groovy")
      language("JavaScript")
    }
    writer.toString()
  }

}
