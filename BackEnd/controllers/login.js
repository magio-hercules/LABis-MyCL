var mysql_query = require('../db/db_query')();
var common = require('./common')();
var auth = require('./auth')();
var authAdmin = auth.init();

var idToken = null;
var currentUid = null;

exports.postLogin = function(req, res) {
	console.log("[INFO] call postLogin");

	var query = mysql_query.postLogin();
	var params = [ req.body.id ];

	if (req.body.uid != undefined) {
		console.log("[INFO][TEST] req.body.uid : " + req.body.uid);
		if (authAdmin == null || authAdmin.auth() == null) {
			console.log("[INFO][TEST] authAdmin or authAdmin.auth is null");
			authAdmin = auth.init();
			sleep(1000);
		}
		try {
			authAdmin.auth().createCustomToken(req.body.uid)
					.then(function(customToken) {
						// console.log("[INFO][TEST] customToken : " + customToken);
						idToken = customToken;

						common.doQuery(req, res, query, params, _callback_login);
					})
					.catch(function(error) {
						console.log("Error creating custom token:", error);
					});
		} catch (error) {
			console.log("authAdmin.auth().createCustomToken(req.body.uid) : ", error);
		}
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
		image : req.body.image,
		uid : req.body.uid
	};
	
	common.doRequest(req, res, query, user);
};


exports.postCheckIdToken = function(req, res) {
	console.log("[INFO] call postCheckIdToken");

	console.log("[INFO][TEST] req.body.id : " + req.body.id);
	console.log("[INFO][TEST] req.body.uid : " + req.body.uid);
	// console.log("[INFO][TEST] req.body.idToken : " + req.body.idToken);

	if (false) {
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
	} else {
		var query = mysql_query.postLogin();
		var params = [ req.body.id ];

		idToken = req.body.idToken;

		common.doQuery(req, res, query, params, _callback_login);
	}
	
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
			// if (result[0].pw == req.body.pw || result[0].uid == req.body.uid) {
			if (result[0].uid == req.body.uid) {
				console.log("[INFO][TEST] result[0].uid == req.body.uid : true");
				// console.log('[DEBUG] idToken : ', idToken);

				res.writeHead(200, {'Content-Type': 'application/json'});
				
				result[0]['token'] = idToken;
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
