package capalerts

import grails.converters.JSON
import grails.converters.XML
import grails.plugin.springsecurity.annotation.Secured

import au.com.bytecode.opencsv.CSVReader
import au.com.bytecode.opencsv.bean.CsvToBean
import au.com.bytecode.opencsv.bean.HeaderColumnNameMappingStrategy
import au.com.bytecode.opencsv.bean.HeaderColumnNameTranslateMappingStrategy



class AdminController {


  @Secured(['ROLE_USER','IS_AUTHENTICATED_FULLY'])
  def index() { 
    log.debug("AdminController::index - list current alerts");
    def result = [:]
    result.alerts = AlertProfile.executeQuery('select ap from AlertProfile as ap where ap.name like :profileNameQry',[profileNameQry:'%'], [max: 10, offset: 0]);

    withFormat {
      html result
      json { render result as JSON }
    }
  }

  
  @Secured(['ROLE_USER','IS_AUTHENTICATED_FULLY'])
  def uploadProfiles() {

    def upload_mime_type = request.getFile("content")?.contentType  // getPart?
    def upload_filename = request.getFile("content")?.getOriginalFilename()
    def content = request.getFile("content")
    def charset='UTF-8'

    def csv = new CSVReader(new InputStreamReader(content.inputStream,java.nio.charset.Charset.forName(charset)),'\t' as char,'"' as char)
    String[] header = csv.readNext()
    log.debug("Process header ${header}");
    String[] nl=csv.readNext()
    int rownum = 0;
    while(nl!=null) {
      log.debug("Process profile line ${nl}");
      def p = AlertProfile.findByShortcode(nl[0]) 
      if ( p == null ) { 
        log.debug("Create new profile ${nl[0]}");
        def r = ( nl.length > 12 ) ? nl[12] : null
        p= new AlertProfile(
                            name:nl[2], 
                            shortcode:nl[0],
                            shapeType:nl[10], 
                            shapeCoordinates:nl[11], 
                            radius:r,
                            subscriptionUrl:nl[3],
                            languageOnly:nl[4],
                            highPriorityOnly:nl[5],
                            officialOnly:nl[6],
                            xPathFilterId:nl[7],
                            xPathFilter:nl[8],
                            areaFilterId:nl[9]).save(flush:true, failOnError:true);
      }
      nl=csv.readNext()
    }

    redirect ( view:'index' )
  }


}
