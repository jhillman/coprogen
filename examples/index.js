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

generator.generate('model', templateData, null, function(err, data) {
  console.log(err, data);
})