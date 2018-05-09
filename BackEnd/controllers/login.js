var mysql_query = require('../db/db_query')();
var common = require('./common')();


exports.postLogin = function(req, res) {
	console.log("[INFO] call postLogin");

	var query = mysql_query.postLogin();
	var params = [ req.body.id ];
	
	common.doQuery(req, res, query, params, _callback_login);
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
	
	common.doRegister(req, res, query, user);
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
			console.log(result[0].pw);
			console.log(req.body.pw);
			
			if (result[0].pw == req.body.pw) {
				res.writeHead(200, {'Content-Type': 'application/json'});
				
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
