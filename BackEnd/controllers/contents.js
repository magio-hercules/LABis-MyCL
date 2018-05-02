var mysql_query = require('../db/db_query')();
var common = require('./common')();

exports.getContents = function(req, res) {
    console.log("req.query : " + JSON.stringify(req.query));
	console.log("req.query.id : " + JSON.stringify( req.query.id));
	console.log("req.query.gen_id : " + JSON.stringify(req.query.gen_id));

	var query = mysql_query.getContents();
	var obj = { id: req.query.id, gen_id: req.query.gen_id };
	common.doQuery(req, res, query, obj);
};
