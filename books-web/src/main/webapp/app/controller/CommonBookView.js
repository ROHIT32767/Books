'use strict';

/**
 * Common book view controller.
 */
App.controller('CommonBookView', function($scope, $q, $timeout, $state, $stateParams, Restangular) {
  /**
   * Delete the book.
   */
  $scope.deleteBook = function() {
    if(confirm('Do you really want to delete this book?')) {
      Restangular.one('commonbook', $stateParams.id).remove().then(function() {
        $state.transitionTo('commonlibrary.all');
      });
    }
  };

  /**
   * Edit the book.
   */
  $scope.editBook = function() {
    $state.transitionTo('commonbookedit', { id: $stateParams.id });
  }

  $scope.goBack = function() {
    $state.transitionTo('commonlibrary.all');
  }

  /**
   * Edit the book cover.
   */
  $scope.editCover = function() {
    var url = prompt('Book cover image URL (only JPEG supported)');
    if (url) {
      $scope.coverChanging = true;

      Restangular.one('commonbook', $stateParams.id).post('cover', {url: url}).then(function() {
        $scope.coverChanging = false;
      }, function() {
        alert('Error downloading the book cover, please check the URL');
        $scope.coverChanging = false;
      });
    }
  };

  // True if the cover is in the process of being changed
  $scope.coverChanging = false;

  // Load book
  var bookPromise = Restangular.one('commonbook', $stateParams.id).get().then(function(data) {
    $scope.commonbook = data;
  })

  // Wait for book to load
  $q.all([bookPromise]).then(function() {
    $timeout(function () {
      // Initialize read state
      $scope.commonbook.read = $scope.commonbook.read_date != null;

      // Watch read state change
      $scope.$watch('commonbook.read', function(prev, next) {
        if (prev !== next) {
          Restangular.one('commonbook', $stateParams.id).post('read', {
            read: $scope.commonbook.read
          }).then(function() {
                if ($scope.commonbook.read) {
                  $scope.commonbook.read_date = new Date().getTime();
                } else {
                  $scope.commonbook.read_date = null;
                }
              });
        }
      }, true);

      $scope.submitRating = function() {
        Restangular.one('commonbook', $stateParams.id).customPUT({ rating: $scope.commonbook.rating }, 'rate')
          .then(function() {
            console.log("Rating Submitted Successfully");
            $state.go($state.current, {}, { reload: true });
            // $state.transitionTo('commonbookview');
          })
          .catch(function(error) {
            console.error('Error submitting rating:', error);
          });
      };      
    }, 1);
  });
});
