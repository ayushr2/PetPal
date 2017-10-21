const functions = require('firebase-functions');

const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

let pets_available = new Set();
pets_available.add('dog');
pets_available.add('cat');
pets_available.add('hamster');
pets_available.add('fish');
pets_available.add('rabbit');
pets_available.add('mice');
pets_available.add('bird');
pets_available.add('snake');
pets_available.add('iguana');
pets_available.add('ferret');

exports.detectPet = functions.database.ref('/posts/{pushId}/imageLocation')
    .onWrite(event => {
      // Grab the current value of what was written to the Realtime Database.
      const imageBucketLocation = event.data.val();

      var BreakException = {};

      // REST request
      // https://cloud.google.com/vision/docs/reference/rest/v1/images/annotate
      const Vision = require('@google-cloud/vision');

      // Instantiates a client
      const vision = Vision({
          projectId: 'firebase-petsafe',
          keyfileName: 'keyfile.json'
      });

      const Storage = require('@google-cloud/storage');
      const storage = Storage();

      var return_val = null;

      vision.detectLabels(storage.bucket('firebase-petsafe.appspot.com').file('images/' + imageBucketLocation))
      .then((results) => {
        const labels = results[0];
        try {
            labels.forEach((label) => {
              if (pets_available.has(label)) {
                return event.data.ref.parent.child('animal').set(label);
                throw BreakException;
              }
            })
          } catch (e) {
            if (e !== BreakException) throw e;
          }
      })
      .catch((err) => {
        console.error('ERROR:', err);
    });

    return null;
});
