import capalerts.*;

class BootStrap {

  def init = { servletContext ->

    assertProfile('Flagstaff Co. near Killam and Sedgewick',
                  'polygon',
                  '[ [ [ -111.5995, 52.9305 ], [ -111.876, 52.6676 ], [ -111.9873, 52.9598 ], [ -111.8906, 52.9597 ], [ -111.5995, 52.9305 ] ] ]');

    assertProfile('Near Sheffield, UK',
                  'circle',
                  '[ -1.466944, 53.383611 ]',
                  '5000m'); // 5km circle

    assertProfile('World',
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

  }

  def destroy = {
  }

  def assertProfile(name, type, shape, radius=null) {
    log.debug("assertProfile ${name}");
    def p = AlertProfile.findByName(name) ?: new AlertProfile(name:name, 
                                                              shapeType:type, 
                                                              shapeCoordinates:shape,
                                                              radius:radius).save(flush:true, failOnError:true);
  }
}
