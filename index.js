var fs       = require('fs'),
    jade     = require('jade'),
    classDef = require('./class-defs/person')
    ;

fs.readFile('./templates/class-definition.jade', 'utf-8', function(err, data) {
  var personFun = jade.compile(data, {pretty: true});
  console.log(personFun(classDef));
});
    