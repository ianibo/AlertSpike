'use strict';

/**
 * Follow the javascript module implementation.
 * This academic-output javascript module registers an angular module.
 * We can also define states here as well as the controllers for the AcademicOuput.
 */
define(
  "academic-output",     // JS module name (not the same as the angular module name.)
  ['globals/Finance', 'search'],   // File locations can also be used instead of named includes.
  function (Finance) {   // Module instantiator. Should return an object that will be stored against the name of this module.
    
    // Create our angular module here.
    return angular.module('academic-output', ['ui.router'])
      .config(['$stateProvider','$urlRouterProvider', function($stateProvider,$urlRouterProvider) {
  
        // State for search.
        $stateProvider.state('app.componentSearch.academicOutput', {
          url:          '/academic-output',
          data : {
            title: "Academic Output"
          }
        });
        
        // Default config for un-named view.
        $stateProvider.state('app.academicOutput-view', {
          url:          '^/academic-output/:id',
          templateUrl:  'components/academic-output/partials/main.html',
          data : {
            title: "Academic Output"
          },
          controller: ['$scope', '$state', function ($scope, $state) {
            
            $scope['academicOutput'] = {
              id    : $state.params['id'],
              name  : "My test academic output",
              value : 100,
              tax : function () {
                return Finance.calcTax(this.value);
              }
            };
            
            $state.current.data.subTitle = $scope['academicOutput']['name'] + ": " + $state.params['id'];
          }]
        });
      }])
      // .controller('Search', ["$scope", function($scope) {
      // }])
    ;
  }
);
