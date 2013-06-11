'use strict';

var CodeGenerator = require('../lib'),
    generator,
    templateData
    ;

templateData = {
  "name": "Person",
  "members" : [
    {
      "type" : "String",
      "name" : "name"
    },
    {
      "type" : "int",
      "name" : "age"
    }
  ]
}

generator = new CodeGenerator();

generator.generateModel('model', templateData, __dirname, function(err, data) {
  if (err) console.log('error: ' + err);
})