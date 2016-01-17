'use strict';
define(
  "search",
  function (finance) {
    angular.module('search', ['ui.router'])
      .config(['$stateProvider','$urlRouterProvider', function($stateProvider,$urlRouterProvider) {
        $stateProvider.state('app.componentSearch', {
          abstract: true,
          templateUrl:  'components/search/partials/main.html',
          controller: ['$scope', '$state', function($scope, $state) {
            $state.current.data.subTitle = "Search";
            $scope['cols'] = [
             {colName:'col1'},
             {colName:'col2'},
             {colName:'col3'}
           ];
    
           $scope['data'] = [
             {col1:'value1.1', col2:'value1.2', col3:'value1.3' },
             {col1:'value2.1', col2:'value2.2', col3:'value2.3' },
             {col1:'value3.1', col2:'value3.2', col3:'value3.3' }
           ];
          }]
        });
      }])
    ;
  }
);
