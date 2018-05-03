var mysql = require('mysql');
var config = require('./db_info').real;
var connection = null;

module.exports = function () {
  return {
    init: function () {
      console.info('[INFO] db_con init');
      if (connection == null) {
        console.info('[INFO] db_con create');
        connection = mysql.createConnection({
          host: config.host,
          port: config.port,
          user: config.user,
          password: config.password,
          database: config.database
        });  

        if (connection != null) {
          connection.connect(function (err) {
            if (err) {
              console.error('mysql connection error :' + err);
            } else {
              console.info('mysql is connected successfully.');
            }
          })
        }
      }
      
      return connection;
    }, // init

    verify_connect: function (con) {
      con.connect(function (err) {
        if (err) {
          console.error('mysql connection error :' + err);
        } else {
          console.info('mysql is connected successfully.');
        }
      })
    }, //verify_connect
  }
};


