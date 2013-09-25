#! /usr/bin/env node

'use strict';

var fs = require('fs'),
    CodeGenerator = require('./lib'),
    argv = require('optimist').argv,
    generator,
    generatorMap,
    processorMap,
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

function processData(destination, templateData) {
  if (argv.data) {
    var dataJson = JSON.parse(argv.data);

    for (var key in dataJson) {
      templateData[key] = dataJson[key];
    }
  }

  processorMap[templateData.type](destination, templateData, function(destination, templateData) {
    generatorMap[templateData.type](destination, templateData, function(err) {
      if (err) console.log('CodeGenerator error: ' + err);

      console.log('CodeGenerater has completed successfully!');
    });
  });
};

usage = function() {
  console.log('\nUsage:\n\nRun from a directory that contains a code-generator.json file, or provide the path as an argument:\n' +
              '  codegen --path <relative path to content-provider.json>\n\n' +
              'You may also provide the destination directory as an argument:\n' +
              '  codegen --dest <relative path to the destination directory>\n\n' +
              'Finally, you may also override or provide additional template data with the data parameter:\n' +
              '  codegen --data \'{"authority": "com.custom.authority"}\'');
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

    processData(destinationPath, templateData);
  } else {
    usage();
  }
});