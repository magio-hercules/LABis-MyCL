var mysql = require('mysql');
var config = require('./db_info').real;
var connection = null;

module.exports = function () {
  return {
    init: function () {
      if (connection == null) {
        connection = mysql.createConnection({
          host: config.host,
          port: config.port,
          user: config.user,
          password: config.password,
          database: config.database
        });  
      }
      
      return connection;
    },
    test_open: function (con) {
      con.connect(function (err) {
        if (err) {
          console.error('mysql connection error :' + err);
        } else {
          console.info('mysql is connected successfully.');
        }
      })
    }
  }
};


