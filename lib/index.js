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

function writeFileWithPersistedSection(filename, template, data, next, cb) {
  var persistedSectionRegExp = new RegExp(/\/\/ BEGIN PERSISTED SECTION[^\n]*\n([\S\s]*?)\n\s*\/\/ END PERSISTED SECTION/i);

  fs.exists(filename, function(exists) {
    if (exists) {
      fs.readFile(filename, function(err, fileData) {
        if (err) cb(err);

        var match = persistedSectionRegExp.exec(fileData);

        data.persistedSection = match != null ? match[1] : '';
        fs.writeFile(filename, template(data), next);
      });
    } else {
      data.persistedSection = '';
      fs.writeFile(filename, template(data), next);
    }
  });
}

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
      var packageParts = templateData.packageName.split('.'),
          directoriesDone = waitress(packageParts.length, next),
          lastDirectory = destination,
          directoryStewardess = stewardess()

      destinationPackage = destination + '/' + packageParts.join('/'),

      packageParts.forEach(function(packagePart) {
        directoryStewardess.add(function(nextDirectory) {
          var directory = lastDirectory + '/' + packagePart;
          fs.exists(directory, function(exists) {
            if (!exists) {
              fs.mkdir(directory, function(err) {
                if (err) cb(err);
                lastDirectory = lastDirectory + '/' + packagePart;
                nextDirectory();
              });
            } else {
              lastDirectory = lastDirectory + '/' + packagePart;
              nextDirectory();
            }
          });
        });
      });

      templateNames.forEach(function(templateName) {
        directoryStewardess.add(function(nextDirectory) {
          var directory = lastDirectory + '/' + templateName.replace('-', '/');
          fs.exists(directory, function(exists) {
            if (!exists) {
              fs.mkdir(directory, function(err) {
                if (err) cb(err);
                nextDirectory();
              });
            } else {
              nextDirectory();
            }
          });
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
      var existingModels = [],
          modelsAndTablesDone = waitress(templateData.models.length * 2, function() {
            fs.readdir(destinationPackage + '/model/', function(err, files) {
              if (err) cb(err);

              files.forEach(function(file) {
                if (existingModels.indexOf(file) < 0) {
                  fs.unlinkSync(destinationPackage + '/model/' + file);
                  fs.unlinkSync(destinationPackage + '/database/table/' + file.replace('.java', 'Table.java'));
                }
              });

              next();
            })
          });

      templateData.models.forEach(function(model) {
        model.packageName = templateData.packageName;
        model.parcelable = model.parcelable ? model.parcelable : false
        model.gson = model.gson ? model.gson : false

        model.enumMap = {};
        model.enums = model.members.filter(function(member) {
          return member.type == 'enum';
        }).map(function(member) {
          model.enumMap[member.class] = member.class.substring(member.class.lastIndexOf('.') + 1);

          return member.class;
        });

        addHelpers(model, function() {
          existingModels.push(model.name + '.java');

          fs.writeFile(destinationPackage + '/database/table/' + model.name + 'Table.java', templateMap['database-table'](model), modelsAndTablesDone);
          writeFileWithPersistedSection(destinationPackage + '/model/' + model.name + '.java', templateMap['model'], model, modelsAndTablesDone, cb);
        });
      })
    },
    function(next) {
      var databaseAndProviderDone = waitress(3, next);

      addHelpers(templateData, function() {
        writeFileWithPersistedSection(destinationPackage + '/database/' + helpers.capitalize(helpers.camelCase(templateData.databaseName)) + 'Database.java', templateMap['database'], templateData, databaseAndProviderDone, cb);
        fs.writeFile(destinationPackage + '/provider/' + helpers.capitalize(helpers.camelCase(templateData.databaseName)) + 'Provider.java', templateMap['provider'](templateData), databaseAndProviderDone);
        fs.writeFile(destination + '/content-provider.xml', 
          '<provider android:name="' + templateData.packageName + '.provider.' + helpers.capitalize(helpers.camelCase(templateData.databaseName)) + 'Provider" ' + 
                    'android:authorities="' + (templateData.authority ? templateData.authority : (templateData.packageName + '.provider.' + templateData.databaseName)) + '" android:exported="false" />', 
          databaseAndProviderDone);
      });
    }
  )
  .done(cb)
  .run();
}

module.exports = CodeGenerator