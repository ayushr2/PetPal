const functions = require('firebase-functions');

const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

exports.detectPet = functions.database.ref('/posts/{pushId}/imageLocation')
    .onWrite(event => {
      // Grab the current value of what was written to the Realtime Database.
      const imageBucketLocation = event.data.val();

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

      vision.detectLabels(storage.bucket('firebase-petsafe.appspot.com').file('images/' + imageBucketLocation))
      .then((results) => {
        const labels = results[0];
        labels.forEach((label) => {
          console.log(label);
        })
      })
      .catch((err) => {
        console.error('ERROR:', err);
      });

      return event.data.ref.parent.child('animal').set('dog');
    });
