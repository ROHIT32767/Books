'use strict';

/**
 * Add common book manually controller.
 */
App.controller('AddCommonBookManual', function($scope, $state, $stateParams, Restangular) {
  $scope.isEdit = false;
  $scope.book = {
    tags: []
  };

  /**
   * Create the new common book.
   */
  $scope.edit = function() {
    $scope.book.publish_date = Date.parse($scope.book.publish_date_year + '-01-01');
    Restangular.one('commonbook/manual', $stateParams.id).put($scope.book).then(function(data) {
      $state.transitionTo('commonlibrary.all', { id: data.id });
    }, function(response) {
      alert(response.data.message);
    });
  };

  /**
   * Cancel the manual add and go back to the common book add main page.
   */
  $scope.cancel = function() {
    $state.transitionTo('commonbookadd');
  };
});
 