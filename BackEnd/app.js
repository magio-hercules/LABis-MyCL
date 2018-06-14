var express = require('express');
var connect = require('connect');
// var path = require('path');
// var favicon = require('serve-favicon');
var logger = require('morgan');
// var cookieParser = require('cookie-parser');
var bodyParser = require('body-parser');


var app = express();
// Configuration
app.use(express.static(__dirname + '/public'));
// app.use(connect.logger('dev')); // app.use(logger('dev'));
// app.use(connect.json());
// app.use(connect.urlencoded());

// for login
app.use(bodyParser.urlencoded({ extended: true }));
app.use(bodyParser.json());
app.use(function(req, res, next) {
  res.header("Access-Control-Allow-Origin", "*");
  res.header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
  next();
});

//production error handler
//no stacktraces leaked to user
app.use(function(err, req, res, next) {
  res.status(err.status || 500);
  res.render('error', {
  message: err.message,
  error: {}
  });
});


var mycl = require('./routes/mycl.js');
// app.use('/', mycl);
app.use('/MyCL', mycl);


module.exports = app;