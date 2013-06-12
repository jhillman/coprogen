'use strict';

var fs = require('fs'),
    CodeGenerator = require('./lib'),
    generator,
    generatorMap,
    templateData
    ;

generator = new CodeGenerator();

var generatorMap = {
  'content-provider': generator.generateContentProvider
};

fs.exists(process.cwd() + '/code-generator.json', function(exists) {
  if (exists) {
    templateData = require(process.cwd() + '/code-generator');
    generatorMap[templateData.type](process.cwd(), templateData, function(err) {
      if (err) console.log('error: ' + err);

      console.log('done!');
    });
  } else {
    console.log('no code-generator.json file found!');
    process.exit(1);
  }
});
