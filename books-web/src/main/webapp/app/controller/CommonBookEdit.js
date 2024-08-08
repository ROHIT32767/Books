'use strict';

/**
 * Book edit controller.
 */
App.controller('CommonBookEdit', function($scope, $state, $stateParams, Restangular) {
  $scope.isEdit = true;

  /**
   * Save the modifications.
   */
  $scope.edit = function() {
    $scope.book.publish_date = Date.parse($scope.book.publish_date_year + '-01-01');
    Restangular.one('commonbook', $stateParams.id).post('', $scope.book).then(function() {
      $state.transitionTo('commonlibrary.all', { id: $stateParams.id });
    })
  };

  /**
   * Cancel the edition and go back to the book.
   */
  $scope.cancel = function() {
    $state.transitionTo('commonbookview', { id: $stateParams.id });
  };

  // Load book
  Restangular.one('commonbook', $stateParams.id).get().then(function(data) {
    $scope.book = data;
    $scope.book.publish_date_year = new Date($scope.book.publish_date).getFullYear();
    $scope.book.tags = _.pluck($scope.book.tags, 'id');
  });
});