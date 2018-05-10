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


exports.postInsertMyContents = function(req, res) {
	console.log("[INFO] call postInsertMyContents");
	// console.log("req.body : " + JSON.stringify(req.body));

	var query = mysql_query.postInsertMyContents();
	var params = [ ];
	// query = _checkParams(query, params, req.body.user_id, table.Contents_my.user_id);
	var myContents = {
		id: req.body.id,
		user_id: req.body.user_id,
		score: req.body.score == null ? '' : req.body.score,
		comment: req.body.comment == null ? '' : req.body.comment,
		chapter: req.body.chapter == null ? '' : req.body.chapter
	};
	
	bFirst = true;
	common.doRequest(req, res, query, myContents);
};


exports.postUpdateMyContents = function(req, res) {
	console.log("[INFO] call postUpdateMyContents");
	// console.log("req.body : " + JSON.stringify(req.body));

	var query = mysql_query.postUpdateMyContents();
	var params = [];

	query = _setParams(query, params, req.body.score, table.Contents_my.score);
	query = _setParams(query, params, req.body.comment, table.Contents_my.comment);
	query = _setParams(query, params, req.body.chapter, table.Contents_my.chapter);
	bFirst = true;
	query = _checkParams(query, params, req.body.id, table.Contents_my.id);
	query = _checkParams(query, params, req.body.user_id, table.Contents_my.user_id);
	
	console.log("[TEST] query : " + query);
	console.log("[TEST] params : " + params);

	bFirst = true;
	common.doRequest(req, res, query, params);
};


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