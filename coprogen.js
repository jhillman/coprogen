#! /usr/bin/env node

'use strict';

var fs                       = require('fs'),
    ContentProviderGenerator = require('./lib'),
    argv                     = require('optimist').argv,
    destinationPath          = process.cwd(),
    contentProviderConfig    = process.cwd() + '/content-provider.json',
    modelsConfig             = process.cwd() + '/models.json',
    coprogen                 = new ContentProviderGenerator(),
    data,
    usage;

function processData(destination, data) {
  var dataJson;

  if (argv.data) {
    if (fs.existsSync(argv.data)) {
      dataJson = require(process.cwd() + '/' + argv.data);
    } else {
      dataJson = JSON.parse(argv.data);
    }

    for (var key in dataJson) {
      data[key] = dataJson[key];
    }
  }

  if (!data.relationships) {
    data.relationships = [];
  }

  coprogen.generate(destination, data, function(err) {
    if (err) console.log('coprogen error: ' + err);

    console.log('coprogen has completed successfully!');
  });
};

usage = function() {
  console.log('\ncoprogen usage:\n\n' + 
              'Run from a directory that contains a content-provider.json or\n' +
              'models.json config file, or provide the path as an argument:\n' +
              '  coprogen --path <relative path to content provider config file>\n\n' +
              'You may also provide the destination directory as an argument:\n' +
              '  coprogen --dest <relative path to the destination directory>\n\n' +
              'Finally, you may also override or provide additional template \n' +
              'data with the data parameter:\n' +
              '  coprogen --data \'{"authority": "com.custom.authority"}\'\n'+
              '  coprogen --data <relative path to .json file>');
  process.exit(1);
};

if (argv.path) {
  contentProviderConfig = process.cwd() + '/' + argv.path;
}

if (argv.dest) {
  destinationPath = process.cwd() + '/' + argv.dest;
}

fs.exists(contentProviderConfig, function(exists) {
  if (exists) {
    data = require(contentProviderConfig);

    processData(destinationPath, data);
  } else {
    fs.exists(modelsConfig, function(exists) {
      if (exists) {
        data = require(modelsConfig);

        processData(destinationPath, data);
      } else {
        usage();
      }
    });
  }
});