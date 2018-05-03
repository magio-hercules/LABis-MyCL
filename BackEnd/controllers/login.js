var mysql_query = require('../db/db_query')();
var common = require('./common')();


exports.postLogin = function(req, res) {
	var query = mysql_query.postLogin();
    var id = req.body.id;
	var pw = req.body.pw;
	
	console.log('req.query.id: ', req.query.id);
	console.log('req.query.pw: ', req.query.pw);

	console.log('req.body.id: ', req.body.id);
	console.log('req.body.pw: ', req.body.pw);

	var obj = { id: id, pw: pw };
	
	common.doQuery(req, res, query, obj, _callback_login);
};


exports.postRegister = function(req, res) {
	var query = mysql_query.postRegister();
	// console.log("req",req.body);
	// var today = new Date();
	console.log("req.body: ", req.body);

	var user={
		id: req.body.id,
		pw: req.body.pw,
		age: req.body.age,
		gender: req.body.gender,
		nickname: req.body.nickname,
		phone: req.body.phone,
		fav_genre: req.body.fav_genre,
		fav_id: req.body.fav_id
	};
	
	console.log("[QUERY]", query);
	common.doRegister(req, res, query, user);
};


function _callback_login(req, res, obj, error, result) {
	if (error) {
		// console.log("error ocurred",error);
		res.send({
			"code":400,
			"failed":"error ocurred"
		})
	}else{
		console.log('The solution is: ', result);
		
		if(result.length >0){
			console.log(result[0].pw);
			console.log(obj.pw);
			if(result[0].pw == obj.pw){
				res.writeHead(200, {'Content-Type': 'application/json'});
				
				var jsonData = JSON.stringify(result);
				res.end(jsonData);
			}
			else{
				res.send({
					"code":204,
					"success": "Email and password does not match"
				});
			}
		}
		else{
			res.send({
				"code":204,
				"success": "Email does not exits"
			});
		}
	}
}
