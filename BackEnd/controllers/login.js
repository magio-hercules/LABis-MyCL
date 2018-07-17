var mysql_query = require('../db/db_query')();
var table = require('../db/db_table');
var common = require('./common')();
var auth = require('./auth')();
var authAdmin = auth.init();
const request = require('request-promise');

var idToken = null;
var currentUid = null;

var bFirst = true;

// Kakao API request url to retrieve user profile based on access token
const requestMeUrl = 'https://kapi.kakao.com/v1/user/me?secure_resource=true';
const accessTokenInfoUrl = 'https://kapi.kakao.com/v1/user/access_token_info';




exports.postLogin = function(req, res) {
	console.log("[====] call postLogin");

	var query = mysql_query.postLogin();
	var params = [ req.body.id ];

	if (req.body.uid != undefined) {
		console.log("[INFO][TEST] before try");
		try {
			console.log("[INFO][TEST] req.body.uid : " + req.body.uid);
			if (authAdmin == null || authAdmin.auth() == null) {
				console.log("[INFO][TEST] authAdmin or authAdmin.auth is null");
				authAdmin = auth.init();
				sleep(1000);
			}
		
			console.log("[INFO][TEST] customToken 1");
			authAdmin.auth().createCustomToken(req.body.uid)
					.then(function(customToken) {
						console.log("[INFO][TEST] customToken 4");
						// console.log("[INFO][TEST] customToken : " + customToken);
						idToken = customToken;

						common.doQuery(req, res, query, params, _callback_login);
					})
					.catch(function(error) {
						console.log("Error creating custom token:", error);
					});
			console.log("[INFO][TEST] customToken 2");
		} catch (error) {
			console.log("authAdmin.auth().createCustomToken(req.body.uid) : ", error);
		}
		console.log("[INFO][TEST] customToken 3");
	} else {
		console.log("[INFO][TEST] req.body.uid is undefind");
		common.doQuery(req, res, query, params, _callback_login);
	}
				
	// common.doQuery(req, res, query, params, _callback_login);
};


exports.postRegister = function(req, res) {
	console.log("[====] call postRegister");

	var query = mysql_query.postRegister();
	var user = {
		id: req.body.id,
		age: req.body.age,
		gender: req.body.gender,
		nickname: req.body.nickname,
		phone: req.body.phone,
		image : req.body.image,
		uid : req.body.uid
	};
	
	common.doRequest(req, res, query, user);
};


exports.postUpdate = function(req, res) {
	console.log("[====] call postUpdate");

	var query = mysql_query.postUpdate();
	var params = [];

	query = _setParams(query, params, req.body.nickname, table.User.nickname);
	query = _setParams(query, params, req.body.age, table.User.age);
	query = _setParams(query, params, req.body.gender, table.User.gender);
	query = _setParams(query, params, req.body.phone, table.User.phone);
	query = _setParams(query, params, req.body.image, table.User.image);
	bFirst = true;
	query = _checkParams(query, params, req.body.id, table.User.id);
	query = _checkParams(query, params, req.body.uid, table.User.uid);
	
	bFirst = true;
	common.doRequest(req, res, query, params);
};


exports.postCheckIdToken = function(req, res) {
	console.log("[====] call postCheckIdToken");

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


exports.postVerifyToken = function(req, res) {
	console.log("[====] call postVerifyToken");

	console.log("[INFO][TEST] req.body.id : " + req.body.id);
	console.log("[INFO][TEST] req.body.uid : " + req.body.uid);
	console.log("[INFO][TEST] req.body.token : " + req.body.token);

	const token = req.body.token;
	if (!token) {
		return res.status(400)
				  .send({error: 'There is no token.'})
				  .send({message: 'Access token is a required parameter.'});
	}

	console.log(`Verifying Kakao token: ${token}`);

	createFirebaseToken(token, req, res)
	.then((retValue) => {
	// .then((firebaseToken) => {
		// console.log(`Returning firebase token to user: ${firebaseToken}`);
		// res.send({firebase_token: firebaseToken});
	
		// // res.writeHead(200, {'Content-Type': 'application/json'});
		// var response = {"result":"OK", "reason":"postVerifyToken Success", "id":retValue.uid};
		var response = {"result":"OK", "reason":"postVerifyToken Success", "id":retValue.token};
		console.log('[INFO][doRequest] response : ', response);
		res.send(JSON.stringify(response));
	}).catch((error) => res.status(401).send({message: error}));
};

  
/**
 * createFirebaseToken - returns Firebase token using Firebase Admin SDK
 *
 * @param  {String} kakaoAccessToken access token from Kakao Login API
 * @return {Promise<String>}                  Firebase token in a promise
 */
function createFirebaseToken(kakaoAccessToken, req, res) {
	console.log("[INFO] createFirebaseToken");
	return validateToken(kakaoAccessToken).then((response) => {
			console.log("[INFO] validateToken");
			console.log("[INFO] body : " + JSON.stringify(response));
			// const body = JSON.parse(response);
			// const appId = body.appId;
			// if (appId !== config.kakao.appId) {
			// 	throw new Error('The given token does not belong to this application.');
			// }
			return requestMe(kakaoAccessToken);
		}).then((response) => {
			console.log("[INFO] validateToken.then");
			const body = JSON.parse(response);
			console.log(body);
			const userId = body.id;
			if (!userId) {
				throw new Error('There was no user with the given access token.');
			}
			let nickname = null;
			let profileImage = null;
			if (body.properties) {
				nickname = body.properties.nickname;
				profileImage = body.properties.profile_image;
			}

			console.log('[INFO] createOrLinkUser before');
			return createOrLinkUser(userId, body.kaccount_email,
					body.kaccount_email_verified, nickname, profileImage);
		}).then((userRecord) => {
			console.log("[INFO] validateToken.then.then");
			console.log("[====] call postVerifyToken-postRegister");

			var query = mysql_query.postRegister();
			var user = {
				id: userRecord.email,
				age: "",
				gender: "",
				nickname: userRecord.displayName,// userRecord.nickname,
				phone: "",
				image : userRecord.photoURL, //userRecord.profileImage,
				uid : userRecord.uid
			};
			
			// common.doRequest(req, res, query, user);
			return common.doOnlyQuery(req, res, query, user)
			.then((ret) => {
				console.log("[INFO] userRecord : " + JSON.stringify(userRecord));
				return userRecord;
			});
		}).then((userRecord) => {
			console.log("[INFO] validateToken.then.then.then");
			console.log("[INFO] userRecord : " + JSON.stringify(userRecord));

			const userId = userRecord.uid;
			console.log(`creating a custom firebase token based on uid ${userId}`);
			
			// return authAdmin.auth().createCustomToken(userId, {provider: 'KAKAO'});
			// var firebaseToken = authAdmin.auth().createCustomToken(userId, {provider: 'KAKAO'});
			
			try {
				console.log("[INFO][TEST] customToken 1");
				return authAdmin.auth().createCustomToken(userId)
						.then(function(firebaseToken) {
							console.log("[INFO][TEST] createCustomToken then");
							console.log("firebaseToken : " + firebaseToken);
							var retValue = userRecord;
							// retValue[0]['token'] = firebaseToken;
							retValue['token'] = firebaseToken;
							
							console.log("retValue : " + JSON.stringify(retValue));
							return retValue;
						})
						.catch(function(error) {
							console.log("Error creating custom token:", error);
						});
				console.log("[INFO][TEST] customToken 2");
			} catch (error) {
				console.log("authAdmin.auth().createCustomToken(req.body.uid) : ", error);
			}
		});
}


/**
 * requestMe - Returns user profile from Kakao API
 *
 * @param  {String} kakaoAccessToken Access token retrieved by Kakao Login API
 * @return {Promise<Response>}      User profile response in a promise
 */
function requestMe(kakaoAccessToken) {
	console.log('Requesting user profile from Kakao API server.');
	return request({
				method: 'GET',
				headers: {'Authorization': 'Bearer ' + kakaoAccessToken},
				url: requestMeUrl,
			});
}

/**
 * validateToken - Returns access token info from Kakao API,
	* which checks if this token is issued by this application.
	*
	* @param {String} kakaoAccessToken Access token retrieved by Kakao Login API
	* @return {Promise<Response>}      Access token info response
	*/
function validateToken(kakaoAccessToken) {
	console.log('Validating access token from Kakao API server.');
	return request({
		method: 'GET',
		headers: {'Authorization': 'Bearer ' + kakaoAccessToken},
		url: accessTokenInfoUrl,
	});
}



/**
 * createOrLinkUser - Link firebase user with given email,
 * or create one if none exists. If email is not given,
 * create a new user since there is no other way to map users.
 * If email is not verified, make the user re-authenticate with other means.
 *
 * @param  {String} kakaoUserId    user id per app
 * @param  {String} email          user's email address
 * @param  {Boolean} emailVerified whether this email is verified or not
 * @param  {String} displayName    user
 * @param  {String} photoURL       profile photo url
 * @return {Promise<UserRecord>}   Firebase user record in a promise
 */
function createOrLinkUser(kakaoUserId, email, emailVerified, displayName, photoURL) {
	console.log('[INFO] createOrLinkUser');
	console.log('[INFO] kakaoUserId :' + kakaoUserId);
	console.log('[INFO] email : ' + email);
	console.log('[INFO] emailVerified : ' + emailVerified);
	return getUser(kakaoUserId, email, emailVerified)
			.catch((error) => {
				if (error.code === 'auth/user-not-found') {
					const params = {
						uid: `kakao:${kakaoUserId}`,
						displayName: displayName,
					};
					if (email) {
						params['email'] = email;
					}
					if (photoURL) {
						params['photoURL'] = photoURL;
					}
					console.log(`creating a firebase user with email ${email}`);
					return authAdmin.auth().createUser(params);
				}
				throw error;
			})
			.then((userRecord) => linkUserWithKakao(kakaoUserId, userRecord));
}
  

/**
 * getUser - fetch firebase user with kakao UID first, then with email if
 * no user found. If email is not verified, throw an error so that
 * the user can re-authenticate.
 *
 * @param {String} kakaoUserId    user id per app
 * @param {String} email          user's email address
 * @param {Boolean} emailVerified whether this email is verified or not
 * @return {Promise<admin.auth.UserRecord>}
 */
function getUser(kakaoUserId, email, emailVerified) {
	console.log('[INFO] getUser');
	console.log(`fetching a firebase user with uid kakao:${kakaoUserId}`);
	return authAdmin.auth().getUser(`kakao:${kakaoUserId}`)
	.catch((error) => {
		console.log(`getUser error catch`);

		if (error.code !== 'auth/user-not-found') {
			console.log(`auth/user-not-found`);
			throw error;
		}
		if (!email) {
			console.log(`email is not exist`);
			throw error; // cannot find existing accounts since there is no email.
		}
		console.log(`fetching a firebase user with email ${email}`);
		return authAdmin.auth().getUserByEmail(email)
		.then((userRecord) => {
			if (!emailVerified) {
			throw new Error('This user should authenticate first ' +
				'with other providers');
			}
			return userRecord;
		});
	});
}
	

/**
 * linkUserWithKakao - Link current user record with kakao UID
 * if not linked yet.
 *
 * @param {String} kakaoUserId
 * @param {admin.auth.UserRecord} userRecord
 * @return {Promise<UserRecord>}
 */
function linkUserWithKakao(kakaoUserId, userRecord) {
	console.log("linkUserWithKakao");

	console.log("kakaoUserId : " + kakaoUserId);
	console.log("userRecord : " + JSON.stringify(userRecord));

	if (userRecord.customClaims && userRecord.customClaims['kakaoUID'] === kakaoUserId) {
		console.log(`currently linked with kakao UID ${kakaoUserId}...`);
		return Promise.resolve(userRecord);
	}
	console.log(`linking user with kakao UID ${kakaoUserId}...`);
	return authAdmin.auth().setCustomUserClaims(userRecord.uid, {kakaoUID: kakaoUserId}).then(() => userRecord);
}


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


function _setParams(query, params, val, str) {
	if (val != null && val != undefined) {
		if (bFirst) {
			bFirst = false;
		} else {
			query += ", ";
		}		
		
		query = query + str + "=? ";
		params.push(val);
	}
	return query;
}


function _checkParams(query, params, val, str, bOr) {
	if (val != null && val != undefined) {
		if (bFirst) {
			query += " WHERE "; 
			bFirst = false;
		} else {
			if (bOr) {
				query += " OR ";
			} else {
				query += " AND ";
			}
		}		
		
		if (typeof(val) != 'string') {
			query = query + str + "in (" + val + ")";
		} else {
			query = query + str + "=? ";
		}
		
		params.push(val);
	}
	return query;
}