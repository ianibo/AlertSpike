'use strict';

angular.module('monitorLocalUxApp.Edit', ['ui.router'])
    .config(['$stateProvider','$urlRouterProvider', function($stateProvider,$urlRouterProvider) {
    }])
    .controller('EditController', ["$scope", '$stateParams', '$state', function($scope, $stateParams, $state) {
      console.log("Edit Controller :: stateParams: %o %o %s",$stateParams,$state,$state.current.data.editTemplate);
      $scope.objectForm = $state.current.data.editTemplate;
    }])
;

