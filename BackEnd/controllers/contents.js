var mysql_query = require('../db/db_query')();
var table = require('../db/db_table');
var common = require('./common')();

var bFirst = true;




/////////
// GET //
/////////
exports.getContents = function(req, res) {
	console.log("[QUERY] getContents");
	console.log("req.query : " + JSON.stringify(req.query));
	
	console.log("req.query.id : " + JSON.stringify( req.query.id));
	console.log("req.query.gen_id : " + JSON.stringify(req.query.gen_id));
	console.log("req.query.season : " + JSON.stringify(req.query.season));
	console.log("req.query.user_id : " + JSON.stringify(req.query.user_id));

	var query = mysql_query.getContents();
	var params = [];

	query = _checkParams(query, params, req.query.id, table.Contents_my.id);
	query = _checkParams(query, params, req.query.gen_id, table.Contents_my.gen_id);
	query = _checkParams(query, params, req.query.season, table.Contents_my.season);
	query = _checkParams(query, params, req.query.user_id, table.Contents_my.user_id);

	bFirst = true;
	common.doQuery(req, res, query, params);
};


exports.getTotalContents = function(req, res) {
	console.log("[QUERY] getTotalContents");

	var query = mysql_query.getTotalContents();
	// var obj = {};
	common.doQuery(req, res, query);
};




//////////
// POST //
//////////
exports.postContents = function(req, res) {
	console.log("[QUERY] postContents");
	console.log("req.body : " + JSON.stringify(req.body));
	
	console.log("req.body.id : " + JSON.stringify(req.body.id));
	console.log("req.body.gen_id : " + JSON.stringify(req.body.gen_id));
	console.log("req.body.season : " + JSON.stringify(req.body.season));
	console.log("req.body.user_id : " + JSON.stringify(req.body.user_id));

	var query = mysql_query.postContents();
	var params = [];

	query = _checkParams(query, params, req.body.id, table.Contents_my.id);
	query = _checkParams(query, params, req.body.gen_id, table.Contents_my.gen_id);
	query = _checkParams(query, params, req.body.season, table.Contents_my.season);
	query = _checkParams(query, params, req.body.user_id, table.Contents_my.user_id);

	bFirst = true;
	common.doQuery(req, res, query, params);
};


exports.postMyContents = function(req, res) {
	console.log("[QUERY] postMyContents");
	console.log("req.body : " + JSON.stringify(req.body));
	console.log("req.query : " + JSON.stringify(req.query));
	
	// console.log("req.body.user_id : " + JSON.stringify(req.body.user_id));
	console.log("req.query.user_id : " + JSON.stringify(req.query.user_id));

	var query = mysql_query.postMyContents();
	var params = [ req.query.user_id ];

	bFirst = true;
	common.doQuery(req, res, query, params);
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