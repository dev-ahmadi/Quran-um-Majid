const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp(functions.config().firebase);

exports.databaseVersion = functions.https.onRequest((request, response) => {
  
  var ref = admin.database().ref();

  ref.on("value", function(snapshot) {
  	response.send(snapshot.val());
	}, function (errorObject) {
	  console.log("The read failed: " + errorObject.code);
	});

})

