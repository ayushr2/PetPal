const functions = require('firebase-functions');
const Vision = require('@google-cloud/vision')({
    projectId: 'firebase-petsafe',
    keyfileName: 'keyfile.json'
});

const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

exports.detectPet = functions.database.ref('/posts/{pushId}/imageLocation')
    .onWrite(event => {
      // Grab the current value of what was written to the Realtime Database.
      const imageBucketLocation = event.data.val();

      // REST request
      // https://cloud.google.com/vision/docs/reference/rest/v1/images/annotate

      // Instantiates a client
      const vision = Vision();

      // The path to the local image file, e.g. "/path/to/image.png"
      // const fileName = '/path/to/image.png';

      // Performs label detection on the local file
      // vision.labelDetection({ source: { gcsUri: "gs://YOUR_BUCKET/YOUR_IMAGE" } })
      vision.labelDetection({ source: { gcsUri: imageBucketLocation } })
        .then((results) => {
          const labels = results[0].labelAnnotations;
          console.log('Labels:');
          labels.forEach((label) => console.log(label));
        })
        .catch((err) => {
          console.error('ERROR:', err);
        });

      return event.data.ref.parent.child('animal').set('dog');
    });
