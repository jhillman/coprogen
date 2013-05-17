'use strict';

var fs   = require('fs'),
    jade = require('jade')
    ;

var helpers = {
  UpperCamelCase: function(string) {
    return string.charAt(0).toUpperCase() + string.slice(1)
  }
}

function CodeGenerator() {}

CodeGenerator.prototype.generate = function(type, templateData, destination, cb) {
  fs.readFile(__dirname + '/templates/' + type + '.jade', 'utf-8', function(err, template) {
    if (err) return cb(new Error('Error reading template: ' + type + '.jade. Make sure the type you are creating exists'));
    
    var options,
        templateFunction
        ;

    options = {
      pretty: true
    };
    
    for (var key in helpers) {
      templateData[key] = helpers[key];
    }
    
    templateFunction = jade.compile(template, options);
    
    fs.writeFile(destination, templateFunction(templateData), cb);
  });
}

module.exports = CodeGenerator