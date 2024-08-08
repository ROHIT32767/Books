'use strict';

/**
 * Service controller.
 */
App.controller('Service', function ($scope, $timeout, Restangular, $stateParams) {
    // Initialization
    $scope.selectedService = null;
    $scope.searchText = '';
    $scope.contentType = null;
    $scope.results = [];

    $scope.resetResults = function() {
        $scope.results = [];
    };

    $scope.selectService = function (service) {
        // Implement service selection logic
        $scope.resetResults();
        $scope.selectedService = service;
        console.log($scope.selectedService);
    };

    $scope.selectContentType = function (contentType) {
        $scope.resetResults();
        $scope.contentType = contentType;
        console.log($scope.contentType);
    }

    $scope.search = function () {
        // Fetch data based on selected service and search text
        // You will need to implement this based on iTunes/Spotify APIs
        // Use Restangular or other methods to make API requests
        Restangular.one('service/connect').get({
            service: $scope.selectedService,
            search: $scope.searchText,
            contentType: $scope.contentType
        }).then(function(results) {
            $scope.results = results;
            angular.forEach($scope.results, function(episode) {
                episode.isFavourite = false; // Initially set to false
            });
            console.log('Fetched results', $scope.results)
        });

        console.log('Fetching data for', $scope.selectedService, 'with search:', $scope.searchText);
        // Example: Restangular.one('service/list').get({ service: $scope.selectedService, searchText: $scope.searchText }).then(function(data) { });
    };

    $scope.toggleFavorite = function(podcast, id) {
        podcast.isFavorite = !podcast.isFavorite; // Toggle favorite state
        Restangular.one('service/favourite').post({
            podcast: podcast,
            userId: id
        })
        // Send HTTP request to backend to update favorite status
        // Example:
        // YourService.updateFavoriteStatus(userId, podcast.id, podcast.isFavorite)
        // .then(function(response) {
        //     // Handle success
        // })
        // .catch(function(error) {
        //     // Handle error
        // });
    };

    $scope.addFavourite = function(audioBookId) {
        console.log("Adding book to favourite", audioBookId)

        if($scope.contentType == 'Audio Books'){
            Restangular.one('service/favourite').post('audioBook', {
                id: audioBookId
            })
        }else{
            Restangular.one('service/favourite').post('podcast', {
                id: audioBookId
            })
        }
    }
    // Other existing functions and variables...
});
