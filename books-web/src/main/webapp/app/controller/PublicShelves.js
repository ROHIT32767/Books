'use strict';

/**
 * PublicShelves controller.
 */
App.controller('PublicShelves', function($scope, $timeout, Restangular, $stateParams) {
  /**
   * View scope variables.
   */
  $scope.sortColumn = 3;
  $scope.asc = true;
  $scope.offset = 0;
  $scope.limit = 20;
  $scope.search = {
    text: '',
    read: false
  };
  $scope.loading = false;
  $scope.books = [];
  $scope.total = -1;

  // A timeout promise is used to slow down search requests to the server
  // We keep track of it for cancellation purpose
  var timeoutPromise;

  /**
   * Reload books.
   */
  $scope.loadBooks = function() {
    $scope.offset = 0;
    $scope.total = -1;
    $scope.books = [];
    $scope.pageBooks();
  };

  /**
   * Load books.
   */
  $scope.pageBooks = function(next) {
    if ($scope.loading || $scope.total == $scope.books.length) {
      // Avoid spamming the server
      return;
    }

    if (next) {
      $scope.offset += $scope.limit;
    }

    $scope.loading = true;
    Restangular.one('book/list/tags').get({
      offset: $scope.offset,
      limit: $scope.limit,
      sort_column: $scope.sortColumn,
      asc: $scope.asc,
      search: $scope.search.text,
      read: $scope.search.read ? true : null,
      tagId: $stateParams.tagId
    }).then(function(data) {
          $scope.books = $scope.books.concat(data.books);
          $scope.total = data.total;
          $scope.loading = false;
        });
  };

  $scope.$watch('search', function() {
    if (timeoutPromise) {
      // Cancel previous timeout
      $timeout.cancel(timeoutPromise);
    }

    // Call API later
    timeoutPromise = $timeout(function () {
      $scope.loadBooks();
    }, 200);
  }, true);

  Restangular.one('tag/list-public').get().then(function(data) {
    $scope.tags = data.tags;
  });
});