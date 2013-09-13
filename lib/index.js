'use strict';

var fs         = require('fs'),
    path       = require('path'),
    jade       = require('jade'),
    stewardess = require('stewardess'),
    waitress   = require('waitress')
    ;

var helpers = {
  camelCase: function(string) {
    var result = string;
    var underscoreIndex = result.indexOf('_');

    while (underscoreIndex > -1) {
      result = result.substring(0, underscoreIndex) + 
        result.charAt(underscoreIndex + 1).toUpperCase() + 
        result.substring(underscoreIndex + 2);
      underscoreIndex = result.indexOf('_');
    }

    return result;
  },
  capitalize: function(string) {
    return string.charAt(0).toUpperCase() + string.slice(1);
  },
  upperCase: function(string) {
    return string.toUpperCase();
  },
  lowerCase: function(string) {
    return string.toLowerCase();
  }
}

function addHelpers(data, cb) {
    for (var key in helpers) {
      data[key] = helpers[key];
    }

    cb();
}

var removeDirectory = function(directory, cb) {
  var list = fs.readdirSync(directory);

  for(var i = 0; i < list.length; i++) {
    var filename = path.join(directory, list[i]);
    var stat = fs.statSync(filename);
    
    if (stat.isDirectory()) {
      removeDirectory(filename);
    } else {
      fs.unlinkSync(filename);
    }
  }

  fs.rmdirSync(directory);

  if (cb) {
    cb();
  }
};

function CodeGenerator() {}

CodeGenerator.prototype.generateContentProvider = function(destination, templateData, cb) {
  var options = {
        pretty: true
      },
      templateNames = [
        'model',
        'provider',
        'database',
        'database-table'
      ],
      templateMap = {},
      destinationPackage
    ;

  stewardess(
    function(next) {
      var packageRoot = destination + '/' + templateData.packageName.split('.')[0];

      fs.exists(packageRoot, function(exists) {
        if (exists) {
          removeDirectory(packageRoot, next);
        } else {
          next();
        }
      });
    },
    function(next) {
      var packageParts = templateData.packageName.split('.'),
          directoriesDone = waitress(packageParts.length, next),
          lastDirectory = destination,
          directoryStewardess = stewardess()

      destinationPackage = destination + '/' + packageParts.join('/'),

      packageParts.forEach(function(packagePart) {
        directoryStewardess.add(function(nextDirectory) {
          fs.mkdir(lastDirectory + '/' + packagePart, function(err) {
            if (err) cb(err);
            lastDirectory = lastDirectory + '/' + packagePart;
            nextDirectory();
          })
        });
      });

      templateNames.forEach(function(templateName) {
        directoryStewardess.add(function(nextDirectory) {
          fs.mkdir(lastDirectory + '/' + templateName.replace('-', '/'), function(err) {
            if (err) cb(err);
            nextDirectory();
          })
        });
      });

      directoryStewardess.done(next).run();
    },
    function(next) {
      var templateDone = waitress(templateNames.length, next);

      templateNames.forEach(function(templateName) {
        fs.readFile(__dirname + '/templates/content-provider/' + templateName + '.jade', 'utf-8', function(err, template) {
          if (err) templateDone(err);
          templateMap[templateName] = jade.compile(template, options);
          templateDone();
        });
      });
    },
    function(next) {
      var modelsAndTablesDone = waitress(templateData.models.length * 2, next);

      templateData.models.forEach(function(model) {
        model.packageName = templateData.packageName;
        addHelpers(model, function() {
          fs.writeFile(destinationPackage + '/database/table/' + model.name + 'Table.java', templateMap['database-table'](model), modelsAndTablesDone);
          fs.writeFile(destinationPackage + '/model/' + model.name + '.java', templateMap['model'](model), modelsAndTablesDone);
        });
      })
    },
    function(next) {
      var databaseAndProviderDone = waitress(3, next);

      addHelpers(templateData, function() {
        fs.writeFile(destinationPackage + '/database/' + helpers.capitalize(helpers.camelCase(templateData.databaseName)) + 'Database.java', templateMap['database'](templateData), databaseAndProviderDone);
        fs.writeFile(destinationPackage + '/provider/' + helpers.capitalize(helpers.camelCase(templateData.databaseName)) + 'Provider.java', templateMap['provider'](templateData), databaseAndProviderDone);
        fs.writeFile(destination + '/content-provider.xml', 
          '<provider android:name="' + templateData.packageName + '.provider.' + helpers.capitalize(helpers.camelCase(templateData.databaseName)) + 'Provider" ' + 
                    'android:authorities="' + templateData.packageName + '.provider.' + templateData.databaseName + '" exported="false" />', 
          databaseAndProviderDone);
      });
    }
  )
  .done(cb)
  .run();
}

module.exports = CodeGenerator