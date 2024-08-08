'use strict';

/**
 * Settings register page controller.
 */
App.controller('Register', function($scope, $state, $stateParams, Restangular) {
  /**
   * Update the current user.
   */
  $scope.register = function() {
    console.log($scope.user)
    var promise = null;

    promise = Restangular
    .one('check_email')
    .post('', {
      email: $scope.user.email
    }).then((result) => {
      console.log(result)

      if(result.isUsed === true){
        window.alert('Email already in Use')
      }else{
        const promise2 = Restangular
        .one('user')
        .put($scope.user);
      
        promise2.then(function() {
          $state.transitionTo('login');
        });
      }
    });
  };

  $scope.redirectToLogin = function() {
    $state.transitionTo('login')
  }
});