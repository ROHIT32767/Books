'use strict';

/**
 * Add common book controller.
 */
App.controller('CommonLibraryAdd', function($scope, $http, $state, $stateParams, Restangular) {

  /**
   * Add a common book manually from an ISBN number.
   */
  $scope.addCommonBook = function() {
    if($scope.genre === undefined || $scope.genre === null || $scope.genre === ''){
      window.alert('Enter Genre')
      return
    }
    Restangular.one('commonbook').put({
      isbn: $scope.isbn,
      genre: $scope.genre
    }).then(function(data) {
        $state.transitionTo('commonlibrary.all');
        }, function(response) {
          alert(response.data.message);
        });
  };

  // Add common book passed in parameter
  if ($stateParams.isbn) {
    $scope.isbn  = $stateParams.isbn;
    $scope.addCommonBook();
  }
});
