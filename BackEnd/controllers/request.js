var mysql_query = require('../db/db_query')();
var table = require('../db/db_table');
var common = require('./common')();

var bFirst = true;



exports.getTotalRequestType = function(req, res) {
	console.log("[====] call getTotalRequestType");
	
	var query = mysql_query.getTotalRequestType();
	common.doQuery(req, res, query);
};


exports.postRequestList = function(req, res) {
	console.log("[====] call postRequestList");

    var query = mysql_query.postRequestList();
	var params = [];
	query = _checkParams(query, params, req.body.req_type_id, table.Request_list.req_type_id);
	
	bFirst = true;
	common.doQuery(req, res, query, params);
};


exports.postInsertRequest = function(req, res) {
	console.log("[====] call postInsertRequest");

    var newQuery = mysql_query.postInsertRequest();
	var newParams = [];
	newQuery = _setParams(newQuery, newParams, req.body.req_type_id, table.Request_list.req_type_id);
	newQuery = _setParams(newQuery, newParams, req.body.comment, table.Request_list.comment);

	bFirst = true;
	common.doRequest(req, res, newQuery, newParams);
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