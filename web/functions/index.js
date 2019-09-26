var functions = require('firebase-functions');
var admin = require("firebase-admin");
let pageLength = 50;

admin.initializeApp(functions.config().firebase);

exports.stream = functions.https.onRequest((req, res) => {

  var db = admin.database();

  let branch = req.query.branch;
  let start = req.query.start;

  if (typeof branch === "undefined") branch="master";

  var ref_ayat = db.ref("quran/"+branch+"/1/").orderByKey().limitToFirst(pageLength).startAt(start);
  var ref_terjemah = db.ref("quran/"+branch+"/2/").orderByKey().limitToFirst(pageLength).startAt(start);
  
  var ayat_promise = ref_ayat.once("value");
  var terjemah_promise = ref_terjemah.once("value");

  Promise.all([ayat_promise, terjemah_promise]).then(function(results) {
    
    var result = [];

    var ayat_snapshots = results[0];
    var terjemah_snapshots = results[1];
    var s = parseInt(start);

    for (var i = s; i < (s+pageLength); i++) {
    
      var ayat = ayat_snapshots.child(i);
      var terjemah = terjemah_snapshots.child(i);

      result.push({
        _id_ayat: ayat.child("_id").val(),
        _id_terjemah: terjemah.child("_id").val(),
        ayat: ayat.child("ayat").val(),
        surat: ayat.child("surat").val(),
        teks_ayat: ayat.child("teks").val(),
        teks_terjemah: terjemah.child("teks").val()
      });

    }

    var allowedOrigins = ['http://127.0.0.1:8081','https://quran.isa.web.id'];
    var origin = req.headers.origin;
    
    if(allowedOrigins.indexOf(origin) > -1){
      res.header('Access-Control-Allow-Origin', origin);
    }

    res.header('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
    res.header('Access-Control-Allow-Headers', 'Content-Type, Authorization');
    res.header('Access-Control-Allow-Credentials', true);
    
    res.json(result);

  });

});


exports.sign = functions.database.ref('/ref').onWrite(event => {

  var db = admin.database();

  var crypto = require('crypto');
  var https = require('https');

  var signer = crypto.createSign('RSA-SHA512');

  var sign = function(secret) {

    var request = https.get("https://alquran-um-majid.firebaseio.com/objects.json", function(response) {
      response.pipe(signer);

      response.on('end', function() {
        signer.end();

        signatureString = signer.sign({
          key: secret,
          passphrase: '24m281y24'
        }, 'base64');

        var signatureRef = db.ref("signature");

        signatureRef.set(signatureString);
      });

    });

  }

  var fs = require('fs');
  fs.readFile('secret.pem', 'utf8', function (err,data) {

    if (err) {
      return console.log(err);
    }

    sign(data);

  });

});
