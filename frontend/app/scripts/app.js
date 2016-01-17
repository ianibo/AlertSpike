'use strict';

/**
 * @ngdoc overview
 * @name app
 * @description
 * # app
 *
 * Main module of the application.
 */

// Load the above module and bootstrap it.
require(
  [
   'app',
  ],
  function (ng, app) {
    angular.element(document).ready(function() {
      angular.bootstrap(document,
        [
          'app'
        ]
      );
    });
  }
);
