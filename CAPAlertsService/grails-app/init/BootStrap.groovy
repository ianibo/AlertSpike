import capalerts.*;

import au.com.bytecode.opencsv.CSVReader
import au.com.bytecode.opencsv.bean.CsvToBean
import au.com.bytecode.opencsv.bean.HeaderColumnNameMappingStrategy
import au.com.bytecode.opencsv.bean.HeaderColumnNameTranslateMappingStrategy


class BootStrap {

  def pushService

  def init = { servletContext ->

    assertProfile('Flagstaff Co. near Killam and Sedgewick',
                  'BootstrapTest1',
                  'polygon',
                  '[ [ [ -111.5995, 52.9305 ], [ -111.876, 52.6676 ], [ -111.9873, 52.9598 ], [ -111.8906, 52.9597 ], [ -111.5995, 52.9305 ] ] ]');

    assertProfile('Near Sheffield, UK',
                  'BootstrapTest2',
                  'circle',
                  '[ -1.466944, 53.383611 ]',
                  '5000m'); // 5km circle

    assertProfile('World',
                  'BootstrapTest3',
                  'polygon',
                  '[ [ [ 179, 89 ], [ -179, 89 ], [ -179, -89 ], [ 179, -89 ], [ 179, 89 ] ] ]');

    def userRole = Role.findByAuthority('ROLE_USER') ?: new Role(authority: 'ROLE_USER', roleType:'global').save(failOnError: true)

    def admin = User.findByUsername('admin')

    if ( admin ) {
    }
    else {
      log.debug("Create user...");
      admin = new User( username: 'admin', password: 'admin', enabled: true).save(failOnError: true)
      UserRole.create admin, userRole
    }

    // Load test file from web-inf
    loadDefaultSubscriptions()

    pushService.pushPendingRecords()
  }

  def destroy = {
  }

  def assertProfile(name, shortcode, type, shape, radius=null) {
    log.debug("assertProfile ${shortcode}");
    def p = AlertProfile.findByShortcode(shortcode) ?: new AlertProfile(name:name, 
                                                                        shortcode:shortcode, 
                                                                        shapeType:type, 
                                                                        shapeCoordinates:shape,
                                                                        radius:radius).save(flush:true, failOnError:true);
  }

  def loadDefaultSubscriptions() {
    try {
      def charset = 'UTF-8'
      URL alerts_file = new URL('https://raw.githubusercontent.com/ianibo/AlertSpike/master/testdata/alertHubSubscriptions.txt')
      def csv = new CSVReader(new InputStreamReader(new org.apache.commons.io.input.BOMInputStream(alerts_file.openStream()),java.nio.charset.Charset.forName(charset)),'\t' as char,'"' as char)

      String[] header = csv.readNext()
      String[] nl=csv.readNext()



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
                              shapeCoordinates:'[ '+nl[11]+' ]',
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
    }
    catch( Exception e) {
      e.printStackTrace();
    }
 
  }
}
