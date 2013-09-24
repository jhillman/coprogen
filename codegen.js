#! /usr/bin/env node

'use strict';

var fs = require('fs'),
    CodeGenerator = require('./lib'),
    argv = require('optimist').argv,
    generator,
    generatorMap,
    processorMap,
    processor,
    destinationPath = process.cwd(),
    templateDataPath = process.cwd() + '/code-generator.json',
    templateData,
    usage
    ;

generator = new CodeGenerator();
generatorMap = {
  'content-provider': generator.generateContentProvider
};

processorMap = {
  'content-provider': function(destination, templateData, cb) {
    if (!templateData.relationships) {
      templateData.relationships = [];
    }

    cb(destination, templateData);
  }
};

processor = function(destination, templateData) {
  if (argv.data) {
    var dataJson = JSON.parse(argv.data);

    for (var key in dataJson) {
      templateData[key] = dataJson[key];
    }
  }

  processorMap[templateData.type](destination, templateData, function(destination, templateData) {
    generatorMap[templateData.type](destination, templateData, function(err) {
      if (err) console.log('error: ' + err);

      console.log('done!');
    });
  });
};

usage = function() {
  console.log('Usage: ...');
  process.exit(1);
};

if (argv.path) {
  templateDataPath = process.cwd() + '/' + argv.path;
}

if (argv.dest) {
  destinationPath = process.cwd() + '/' + argv.dest;
}

fs.exists(templateDataPath, function(exists) {
  if (exists) {
    templateData = require(templateDataPath);

    processor(destinationPath, templateData);
  } else {
    usage();
  }
});