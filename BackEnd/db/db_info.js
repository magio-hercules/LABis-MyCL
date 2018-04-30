module.exports = (function () {
    return {
      local: { // localhost
        host: 'localhost',
        port: '3306',
        user: 'root',
        password: '',
        database: ''
      },
      real: { // real server db info
        host: 'ec2-18-188-106-154.us-east-2.compute.amazonaws.com',
        port: '3306',
        user: 'labis',
        password: 'labis0423',
        database: 'MyCL'
      }
    }
  })();