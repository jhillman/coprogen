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

console.log(__dirname + 'PersonModel.java');

generator.generate('model', templateData, __dirname + '/PersonModel.java', function(err, data) {
  console.log(err, data);
})