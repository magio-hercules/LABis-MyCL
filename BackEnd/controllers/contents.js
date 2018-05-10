var mysql_query = require('../db/db_query')();
var table = require('../db/db_table');
var common = require('./common')();

var bFirst = true;




/////////
// GET //
/////////
// exports.getContents = function(req, res) {
// 	console.log("[INFO] call getContents");
// 	// console.log("req.query : " + JSON.stringify(req.query));

// 	var query = mysql_query.getContents();
// 	var params = [];

// 	query = _checkParams(query, params, req.query.id, table.Contents_my.id);
// 	query = _checkParams(query, params, req.query.user_id, table.Contents_my.user_id);

// 	bFirst = true;
// 	common.doQuery(req, res, query, params);
// };


exports.getTotalContents = function(req, res) {
	console.log("[INFO] call getTotalContents");
	// console.log("req.query : " + JSON.stringify(req.query));

	var query = mysql_query.getTotalContents();
	common.doQuery(req, res, query);
};




//////////
// POST //
//////////
exports.postContents = function(req, res) {
	console.log("[INFO] call postContents");
	// console.log("req.body : " + JSON.stringify(req.body));
	
	var query = mysql_query.postContents();
	var params = [];

	query = _checkParams(query, params, req.body.id, table.Contents_my.id);
	query = _checkParams(query, params, req.body.user_id, table.Contents_my.user_id);
	query = _checkParams(query, params, table.Config.public_publisher, table.Contents_my.user_id);

	bFirst = true;
	common.doQuery(req, res, query, params);
};


exports.postMyContents = function(req, res) {
	console.log("[INFO] call postMyContents");

	var query = mysql_query.postMyContents();
	var params = [ ];

	query = _checkParams(query, params, req.body.user_id, table.Contents_my.user_id);

	bFirst = true;
	common.doQuery(req, res, query, params);
};


exports.postInsertContents = function(req, res) {
	console.log("[INFO] call postInsertContents");
	// console.log("req.body : " + JSON.stringify(req.body));

	var query = mysql_query.postInsertContents();
	var params = [ ];

	// query = _checkParams(query, params, req.body.user_id, table.Contents_my.user_id);
	var myContents = {
		id: req.body.id,
		user_id: req.body.user_id,
		score: req.body.score,
		comment: req.body.comment,
		chapter: req.body.chapter
	};
	
	bFirst = true;
	common.doQuery(req, res, query, myContents);
	// common.doRegister(req, res, query, myContents);
};


function _checkParams(query, params, val, str) {
	if (val != null && val != undefined) {
		if (bFirst) {
			query += " WHERE "; 
			bFirst = false;
		} else {
			query += " AND ";
		}		
		
		query = query + str + "=? ";
		params.push(val);
	}
	return query;
}