import capalerts.*;

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
    def charset = 'ISO-8859-1' // 'UTF-8'
    URL alerts_file = new URL('')
    def csv = new CSVReader(new InputStreamReader(new org.apache.commons.io.input.BOMInputStream(new ByteArrayInputStream(the_data.fileData)),java.nio.charset.Charset.forName(charset)),'\t' as char,'\0' as char)
 
  }
}
