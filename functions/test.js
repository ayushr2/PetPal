
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

      // The path to the local image file, e.g. "/path/to/image.png"
      // const fileName = '/path/to/image.png';

      // Performs label detection on the local file
      // vision.labelDetection({ source: { gcsUri: "gs://YOUR_BUCKET/YOUR_IMAGE" } })
      vision.detectLabels(storage.bucket('firebase-petsafe.appspot.com').file('images/' + 'JPEG_20171021_141748_-549167055.jpg'))
        .then((results) => {
          const labels = results[0];
          labels.forEach((label) => {
            console.log(label);
          })
        })
        .catch((err) => {
          console.error('ERROR:', err);
        });
