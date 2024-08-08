"use strict";

App.controller("CommonLibraryFilter", function ($scope, Restangular) {
  $scope.loading = false;
  $scope.filterBooks = [];
  $scope.total = -1;
  $scope.selectedRating = null;
  $scope.sortColumn = 3;
  $scope.asc = true;
  $scope.offset = 0;
  $scope.limit = 20;
  $scope.search = {
    text: "",
    read: false,
  };

  // Function to fetch authors from the server
  // $scope.getAuthors = function() {
  //     Restangular.all('commonbook/genre').getList()
  //         .then(function(authors) {
  //             // Authors fetched successfully
  //             $scope.authors = authors;
  //         })
  //         .catch(function(error) {
  //             // Error occurred while fetching authors
  //             console.error('Error fetching authors:', error);
  //             alert('Failed to fetch authors. Please try again.');
  //         });
  // };

  // // Function to fetch genres from the server
  // $scope.getGenres = function() {
  //     Restangular.all('api/genres').getList()
  //         .then(function(genres) {
  //             // Genres fetched successfully
  //             $scope.genres = genres;
  //         })
  //         .catch(function(error) {
  //             // Error occurred while fetching genres
  //             console.error('Error fetching genres:', error);
  //             alert('Failed to fetch genres. Please try again.');
  //         });
  // };

  // Function to apply filters
  $scope.applyFilters = function () {
    if ($scope.selectedRating == null){
      $scope.selectedRating = 0;
    }
    Restangular.one("commonbook/list/genre")
      .get({
        offset: $scope.offset,
        limit: $scope.limit,
        sort_column: $scope.sortColumn,
        asc: $scope.asc,
        search: $scope.search.text,
        read: $scope.search.read ? true : null,
        author: $scope.authorInput,
        genre: $scope.genreInput,
        rating: $scope.selectedRating,
      })
      .then(
        function (data) {
          console.log("response: ", data.books);
          $scope.filterBooks = [];
          $scope.filterBooks = $scope.filterBooks.concat(data.books);
          $scope.total = data.total;
          $scope.loading = false;
          $scope.genreInput = null;
          $scope.authorInput = null;
          $scope.selectedRating = null;
        },
        function (response) {
          alert(response.data.message);
        }
      );
  };

  // Fetch authors and genres when the controller is initialized
  // $scope.getAuthors();
  // $scope.getGenres();
});
