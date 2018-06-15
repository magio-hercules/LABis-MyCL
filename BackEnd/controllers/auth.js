var admin = require('firebase-admin');
var serviceAccount = require('/home/ubuntu/mycl/mycl-205006-firebase-adminsdk-iv1t4-0cb8ee4c44.json');

var appInfo = null;

module.exports = function () {
  return {
    init: function () {
        console.info('[INFO] Firebase init');

        if (appInfo == null) {
            appInfo = admin.initializeApp({
                credential: admin.credential.cert(serviceAccount),
                databaseURL: 'https://mycl-205006.firebaseio.com'
            });
            
            console.log("[INFO] appInfo : " + appInfo);
        }

        return admin;
    } // init
  }
};


