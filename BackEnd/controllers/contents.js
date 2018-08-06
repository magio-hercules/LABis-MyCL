var mysql_query = require('../db/db_query')();
var table = require('../db/db_table');
var common = require('./common')();

var bFirst = true;




/////////
// GET //
/////////
// exports.getContents = function(req, res) {
// 	console.log("[====] call getContents");
// 	// console.log("req.query : " + JSON.stringify(req.query));

// 	var query = mysql_query.getContents();
// 	var params = [];

// 	query = _checkParams(query, params, req.query.id, table.Contents_my.id);
// 	query = _checkParams(query, params, req.query.user_id, table.Contents_my.user_id);

// 	bFirst = true;
// 	common.doQuery(req, res, query, params);
// };


exports.getTotalContents = function(req, res) {
	console.log("[====] call getTotalContents");
	// console.log("req.query : " + JSON.stringify(req.query));

	var query = mysql_query.getTotalContents();
	common.doQuery(req, res, query);
};




//////////
// POST //
//////////
exports.postTotalContents = function(req, res) {
	console.log("[====] call postTotalContents");
	// console.log("req.body : " + JSON.stringify(req.body));
	
	var query = mysql_query.postTotalContents();
	var params = [];
	query = _checkParams(query, params, table.Config.public_publisher, table.Contents_list.publisher);
	// if (req.body.user_id != 'labis') {
	// 	query = _checkParams(query, params, req.body.user_id, table.Contents_list.publisher, true);
	// }
	// for sorting
	query += " order by create_time desc, name asc, season asc";

	bFirst = true;
	common.doQuery(req, res, query, params);
};


exports.postTotalNewContents = function(req, res) {
	console.log("[====] call postTotalNewContents");
	// console.log("req.body : " + JSON.stringify(req.body));
	
	var query = mysql_query.postTotalNewContents();
	var params = [];
	query = _checkParams(query, params, table.Config.public_publisher, table.Contents_list.publisher);
	query += " AND `image` not like 'https://s3%' order by name desc, season asc;";

	bFirst = true;
	common.doQuery(req, res, query, params);
};


exports.postMyContents = function(req, res) {
	console.log("[====] call postMyContents");

	var query = mysql_query.postMyContents();
	var params = [ ];

	query = _checkParams(query, params, req.body.user_id, table.Contents_my.user_id);
	// for sorting
	query += " order by time desc";

	bFirst = true;
	common.doQuery(req, res, query, params);
};


exports.postInsertMyContents = function(req, res) {
	console.log("[====] call postInsertMyContents");
	console.log("req.body : " + JSON.stringify(req.body));

	var query = mysql_query.postInsertMyContents();
	var params = [];

	var user_id = req.body.user_id;
	var id;
	var tQuery;
	var chapter = 1;
	
	console.log("typeof(req.body.id_list) : " + typeof(req.body.id_list)); 
	if (typeof(req.body.id_list) != 'string') {
		var len = req.body.id_list.length;
		
		for(i =  0; i < len; i++) {
			if (i > 0) {
				query += ',';
			}
			id = req.body.id_list[i];
			
			tQuery = '("' + user_id + '", "' + id + '", "' + chapter + '")';
			query += tQuery;
		}
	} else {
		tQuery = '("' + user_id + '", "' + req.body.id_list + '", "' + chapter + '")';
		query += tQuery;
	}
	// console.log("[====][TEST] postInsertMyContents() query : " + query);
	
	bFirst = true;
	common.doRequest(req, res, query, params);
};


exports.postUpdateMyContents = function(req, res) {
	console.log("[====] call postUpdateMyContents");
	// console.log("req.body : " + JSON.stringify(req.body));

	var query = mysql_query.postUpdateMyContents();
	var params = [];

	query = _setParams(query, params, req.body.score, table.Contents_my.score);
	query = _setParams(query, params, req.body.comment, table.Contents_my.comment);
	query = _setParams(query, params, req.body.chapter, table.Contents_my.chapter);
	query = _setParams(query, params, req.body.favorite, table.Contents_my.favorite);
	bFirst = true;
	query = _checkParams(query, params, req.body.id, table.Contents_my.id);
	query = _checkParams(query, params, req.body.user_id, table.Contents_my.user_id);
	
	bFirst = true;
	common.doRequest(req, res, query, params);
};


exports.postFilterMyContents = function(req, res) {
	console.log("[====] call postFilterMyContents");
	// console.log("req.body : " + JSON.stringify(req.body));

	var query = mysql_query.postFilterMyContents();
	var params = [];
	query = _checkParams(query, params, req.body.user_id, table.Contents_my.user_id);
	
	// gen_id or favorite
	if (req.body.gen_id == "FAV") {
		query = _checkParams(query, params, table.Config.favorite, table.Contents_my.favorite);
	} else {
		query = _checkParams(query, params, req.body.gen_id, table.Contents_list.gen_id);
	}
	
	bFirst = true;
	common.doQuery(req, res, query, params);
};


exports.postInsertContentsList = function(req, res) {
	console.log("[====] call postInsertContentsList");
	// console.log("req.body : " + JSON.stringify(req.body));

	var query = mysql_query.postInsertContentsList();
	var params = [];
	query = _setParams(query, params, req.body.gen_id, table.Contents_list.gen_id);
	query = _setParams(query, params, req.body.season, table.Contents_list.season);
	query = _setParams(query, params, req.body.name, table.Contents_list.name);
	query = _setParams(query, params, req.body.name_org, table.Contents_list.name_org);
	query = _setParams(query, params, req.body.chapter_end, table.Contents_list.chapter_end);
	query = _setParams(query, params, req.body.theatrical, table.Contents_list.theatrical);
	query = _setParams(query, params, req.body.series_id, table.Contents_list.series_id);
	query = _setParams(query, params, req.body.summary, table.Contents_list.summary);
	query = _setParams(query, params, req.body.publisher, table.Contents_list.publisher);
	query = _setParams(query, params, req.body.auth, table.Contents_list.auth);
	query = _setParams(query, params, req.body.image, table.Contents_list.image);

	bFirst = true;
	common.doRequest(req, res, query, params, false)
		  .then(function(result, fields, response){
		  	console.log('result: ' + JSON.stringify(result));
			console.log('result.insertId: ' + result.insertId);

			// insert myContents 
			var newQuery = mysql_query.postInsertMyNewContents();
			var newParams = [];
			newQuery = _setParams(newQuery, newParams, result.insertId, table.Contents_my.id);
			newQuery = _setParams(newQuery, newParams, req.body.publisher, table.Contents_my.user_id);
			newQuery = _setParams(newQuery, newParams, req.body.score, table.Contents_my.score);
			newQuery = _setParams(newQuery, newParams, req.body.comment, table.Contents_my.comment);
			newQuery = _setParams(newQuery, newParams, /*req.body.chapter*/table.Config.default_chapter, table.Contents_my.chapter);

			bFirst = true;
			common.doRequest(req, res, newQuery, newParams);
		});
};


exports.postFilterContentsList = function(req, res) {
	console.log("[====] call postFilterContentsList");

	var query = mysql_query.postFilterContentsList();
	var params = [];
	query = _checkParams(query, params, req.body.gen_id, table.Contents_list.gen_id);
	
	bFirst = true;
	common.doQuery(req, res, query, params);
};


exports.postNonAuthContentsList = function(req, res) {
	console.log("[====] call postNonAuthContentsList");

	var query = mysql_query.postNonAuthContentsList();
	var params = [];
	query = _checkParams(query, params, req.body.user_id, table.Contents_list.publisher);
	query = _checkParams(query, params, table.Config.non_auth, table.Contents_list.auth);	

	bFirst = true;
	common.doQuery(req, res, query, params);
};


exports.postSetAuthContentsList = function(req, res) {
	console.log("[====] call postSetAuthContentsList");

	var query = mysql_query.postSetAuthContentsList();
	var params = [];
	query = _setParams(query, params, table.Config.public_publisher, table.Contents_list.publisher);
	query = _setParams(query, params, table.Config.auth, table.Contents_list.auth);
	bFirst = true;
	query = _checkParams(query, params, req.body.user_id, table.Contents_list.publisher);
	query = _checkParams(query, params, req.body.id_list, table.Contents_list.id);	

	bFirst = true;
	common.doQuery(req, res, query, params);
};

exports.postDeleteMyContents = function(req, res) {
	console.log("[====] call postDeleteMyContents");

	var query = mysql_query.postDeleteMyContents();
	var params = [];
	
	query = _checkParams(query, params, req.body.user_id, table.Contents_my.user_id);
	query = _checkParams(query, params, req.body.id_list, table.Contents_my.id);
	
	bFirst = true;
	common.doRequest(req, res, query, params);
};

exports.postUpdateContentsImage = function(req, res) {
	console.log("[====] call postUpdateContentsImage");

	var query = mysql_query.postUpdateContentsImage();
	var params = [];
	
	query = _setParams(query, params, req.body.id, table.Contents_list.id);
	query = _setParams(query, params, req.body.url, table.Contents_list.image);
	bFirst = true;
	query = _checkParams(query, params, req.body.id, table.Contents_list.id);

	bFirst = true;
	common.doRequest(req, res, query, params);
};


exports.postSearchContentsList = function(req, res) {
	console.log("[====] call postSearchContentsList");

	var query = mysql_query.postSearchContentsList();
	var params = [];
	
	query = _searchParams(query, params, req.body.name, table.Contents_list.name);
	query = _searchParams(query, params, req.body.name, table.Contents_list.name_org, true);
	// for sorting
	query += "order by name desc, season asc";

	bFirst = true;
	common.doQuery(req, res, query, params);
};


exports.postSearchMyContents = function(req, res) {
	console.log("[====] call postSearchMyContents");

	var query = mysql_query.postSearchMyContents();
	var params = [];
	
	query = _checkParams(query, params, req.body.user_id, table.Contents_my.user_id);
	if (false) {
		query += "(";
		query = _searchParams(query, params, req.body.name, table.Contents_list.name);
		query = _searchParams(query, params, req.body.name, table.Contents_list.name_org, true);
		query += ")";
	} else {
		query += "AND (`name` like ? OR `name_org` like ?)";
		params.push("%" + req.body.name + "%");
		params.push("%" + req.body.name + "%");
	}
	// for sorting
	query += "order by time desc";

	bFirst = true;
	common.doQuery(req, res, query, params);
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


function _searchParams(query, params, val, str, bOr) {
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
		
		query = query + str + " like ? ";
		params.push("%" + val + "%");
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