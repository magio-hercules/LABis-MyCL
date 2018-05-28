var mysql_query = require('../db/db_query')();
var common = require('./common')();
var auth = require('./auth')();
var authAdmin = auth.init();

// var admin = require('firebase-admin');
// var serviceAccount = require('/home/ubuntu/MyCL/mycl-205006-firebase-adminsdk-iv1t4-0cb8ee4c44.json');

// var appInfo = null;

var idToken = null;

var authTest = true;

var currentUid = null;

exports.postLogin = function(req, res) {
	console.log("[INFO] call postLogin");

	var query = mysql_query.postLogin();
	var params = [ req.body.id ];
	
	// console.log("[INFO][TEST] !! appInfo !! : " + appInfo);
	// if (appInfo == null) {
	// 	appInfo = admin.initializeApp({
	// 		credential: admin.credential.cert(serviceAccount),
	// 		databaseURL: 'https://mycl-205006.firebaseio.com'
	// 	  });
	// 	console.log("[INFO][TEST] appInfo : " + appInfo);
	// }

	if (req.body.uid != undefined) {
		console.log("[INFO][TEST] req.body.uid : " + req.body.uid);
		authAdmin.auth().createCustomToken(req.body.uid)
					.then(function(customToken) {
						// Send token back to client
						console.log("[INFO][TEST] customToken : " + customToken);
						idToken = customToken;

						common.doQuery(req, res, query, params, _callback_login);
					})
					.catch(function(error) {
						console.log("Error creating custom token:", error);
					});
	} else {
		console.log("[INFO][TEST] req.body.uid is undefind");
		common.doQuery(req, res, query, params, _callback_login);
	}
				
	// common.doQuery(req, res, query, params, _callback_login);
};


exports.postRegister = function(req, res) {
	console.log("[INFO] call postRegister");

	var query = mysql_query.postRegister();
	var user = {
		id: req.body.id,
		pw: req.body.pw,
		age: req.body.age,
		gender: req.body.gender,
		nickname: req.body.nickname,
		phone: req.body.phone,
		image : req.body.image
	};
	
	common.doRequest(req, res, query, user);
};


exports.postCheckIdToken = function(req, res) {
	console.log("[INFO] call postCheckIdToken");

	console.log("[INFO][TEST] req.body.id : " + req.body.id);
	console.log("[INFO][TEST] req.body.uid : " + req.body.uid);
	console.log("[INFO][TEST] req.body.idToken : " + req.body.idToken);
	// authAdmin.auth().createCustomToken(req.body.uid)
	// 			.then(function(customToken) {
	// 				// Send token back to client
	// 				console.log("[INFO][TEST] customToken : " + customToken);
	// 				idToken = customToken;

					
	// 			})
	// 			.catch(function(error) {
	// 				console.log("Error creating custom token:", error);
	// 			});

	authAdmin.auth().verifyIdToken(""+req.body.idtoken)
				.then(function(decodedToken) {
					currentUid = decodedToken.uid;
					// ...
					
					console.log("[INFO][TEST] verifyIdToken()");
					console.log("[INFO][TEST] decodedToken.uid : " + uid);
					// 유저 정보 return 하기
					if (req.body.uid == uid) {
						console.log("[INFO][TEST] decodedToken.uid : " + uid);
						var query = mysql_query.postLogin();
						var params = [ req.body.id ];

						common.doQuery(req, res, query, params, _callback_login);
					}
				}).catch(function(error) {
					// Handle error
						console.log("Error verifyIdToken:", error);
				});
};


function _callback_login(req, res, params, error, result) {
	if (error) {
		res.send({
			"code":400,
			"failed":"error ocurred"
		})
	} else {
		console.log('[DEBUG] call _callback_login : ', result);
		
		if (result.length > 0) {
			console.log("[INFO][TEST] result[0].pw : " + result[0].pw);
			console.log("[INFO][TEST] result[0].uid : " + result[0].uid);
			
			// if (result[0].pw == req.body.pw) {
			if (result[0].pw == req.body.pw || result[0].uid == req.body.uid) {
				console.log("[INFO][TEST] result[0].pw == req.body.pw || result[0].uid == req.body.uid true");

				res.writeHead(200, {'Content-Type': 'application/json'});
				
				if (authTest) {
					console.log('[DEBUG] idToken : ', idToken);
					var jsonData = JSON.stringify(result);
					console.log("[INFO][TEST] jsonData 11 : " + jsonData);
					result[0]['token'] = idToken;
					jsonData = JSON.stringify(result);
					console.log("[INFO][TEST] jsonData 22 : " + jsonData);
				}

				var jsonData = JSON.stringify(result);
				res.end(jsonData);
			} else {
				res.send({
					"code":204,
					"success": "Email and password does not match"
				});
			}
		} else {
			res.send({
				"code":204,
				"success": "Email does not exits"
			});
		}
	}
}
