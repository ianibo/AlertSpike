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

    def csv = new CSVReader(new InputStreamReader(content.inputStream,java.nio.charset.Charset.forName(charset)),',' as char,'"' as char)
    String[] header = csv.readNext()
    log.debug("Process header ${header}");
    String[] nl=csv.readNext()
    int rownum = 0;
    while(nl!=null) {
      log.debug("Process profile line ${nl}");
      nl=csv.readNext()
    }

    redirect ( view:'index' )
  }
}
