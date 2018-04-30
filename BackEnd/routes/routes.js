var express = require('express');


// for db
var mysql_query = require('../db/db_query')();
var mysql_dbc = require('../db/db_con')();
var connection = mysql_dbc.init();
mysql_dbc.test_open(connection);



if (true) {
module.exports = function(app) {
	app.get('/', function(req, res) {
		res.writeHead(200, {'Content-Type':'text/html'});
		res.end("Node-Android-Project");
	});

	app.get('/test', function(req, res) {
		console.log("req" + req);
		console.log("res" + res);

		res.render('test', {
			title: 'Test',
			user_info: req
		  });
	});
	
	app.get('/mysql',function(req,res){
		// res.writeHead(200, {'Content-Type':'text/html'});
		// res.end("mysql");
		var stmt = 'select * from User';
		connection.query(stmt, function (err, result) {
			console.log(result);

			if (err) {
				console.log('err :' + err);
				return done(false, null);
			} else {
				return done(false, result);
			}
		});
	});

	app.get('/user',function(req, res){
		var query = mysql_query.getUser();
		var obj = { id: req.query.id };
		doQuery(req, res, query, obj);
	});

	app.get('/contents',function(req, res){
		// console.log("req: " + JSON.stringify(req));
		console.log("req.query : " + JSON.stringify(req.query));
		console.log("req.query.id : " + JSON.stringify( req.query.id));
		console.log("req.query.gen_id : " + JSON.stringify(req.query.gen_id));

		var query = mysql_query.getContents();
		var obj = { id: req.query.id, gen_id: req.query.gen_id };
		doQuery(req, res, query, obj);
	});

	app.get('/genre',function(req, res){
		var query = mysql_query.getGenre();
		var obj = { id: req.query.id };
		doQuery(req, res, query, obj);
	});

	app.get('/favorite',function(req, res){
		var query = mysql_query.getFavorite();
		var obj = { id: req.query.id };
		doQuery(req, res, query, obj);
	});

	app.get('/prefer',function(req, res){
		var query = mysql_query.getPrefer();
		var obj = { id: req.query.id };
		doQuery(req, res, query, obj);
	});

	app.post('/', function (req, res) {
		res.send('Got a POST request');
	});
};

} else {

	var router = express.Router();
	
	// middleware that is specific to this router
	router.use(function timeLog(req, res, next) {
		console.log('Time: ', Date.now());
		next();
	  });
	  
	// define the home page route
	router.get('/', function(req, res) {
		res.send('Birds home page');
	});

	
	module.exports = router;
}


function doQuery(req, res, query, obj) {
	console.log("query :" + query);
	console.log("obj :" + JSON.stringify(obj));

	connection.query(query, [obj.id, obj.gen_id], function (err, result) {
		console.log(result);
	
		if (err) {
			console.log('err :' + err);
		} else {
			var jsonData = JSON.stringify(result);
	
			// res.writeHead(200, {'Content-Type':'text/html'});
			res.writeHead(200, {'Content-Type': 'application/json'});
			res.end(jsonData);
		}
	});
}
