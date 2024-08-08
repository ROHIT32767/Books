'use strict';

App.controller('TopRatedBooks', function($scope, $timeout, Restangular) {
  /**
   * View scope variables.
   */
  $scope.sortColumn = 4;
  $scope.asc = false;
  $scope.offset = 0;
  $scope.limit = 10;
  $scope.search = {
    text: '',
    read: false
  };
  $scope.sortBy = '';
  $scope.loading = false;
  $scope.commonBooks = [];
  $scope.total = -1;


  // A timeout promise is used to slow down search requests to the server
  // We keep track of it for cancellation purpose
  var timeoutPromise;

  /**
   * Reload top rated books.
   */
  $scope.loadTopRatedBooks = function() {
    $scope.offset = 0;
    $scope.total = -1;
    $scope.topRatedBooks = [];
    $scope.pageTopRatedBooks();
  };

  /**
   * Load top rated books.
   */
  $scope.pageTopRatedBooks = function(next) {
    if ($scope.loading || $scope.total == $scope.topRatedBooks.length) {
      // Avoid spamming the server
      return;
    }

    if (next) {
      $scope.offset += $scope.limit;
    }

    $scope.loading = true;

  
  $scope.applySortFilters = function () {
    Restangular.one('commonbook/list/toprated').get({
        offset: $scope.offset,
        limit: $scope.limit,
        sort_column: $scope.sortColumn,
        asc: $scope.asc,
        search: $scope.search.text,
        read: $scope.search.read ? true : null,
        sortBy: $scope.sortBy,
    }).then(function(data) {
      $scope.topRatedBooks = []
      $scope.topRatedBooks = $scope.topRatedBooks.concat(data.books);
      $scope.total = data.total;
      $scope.loading = false;
    });
  };
}

  /**
   * Watch for search scope change.
   */
  $scope.$watch('search', function() {
    if (timeoutPromise) {
      // Cancel previous timeout
      $timeout.cancel(timeoutPromise);
    }

    // Call API later
    timeoutPromise = $timeout(function () {
      $scope.loadTopRatedBooks();
    }, 200);
  }, true);

  // Load top rated books initially
  $scope.loadTopRatedBooks();
});
