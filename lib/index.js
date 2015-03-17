'use strict';

var fs    = require('fs'),
    jade  = require('jade'),
    async = require('async');

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

function writeFileWithPersistedSection(filename, template, additions, data, next, cb) {
  var persistedSectionRegExp = new RegExp(/\/\/ BEGIN PERSISTED SECTION[^\n]*\n([\S\s]*?)\n\s*\/\/ END PERSISTED SECTION/i);

  fs.exists(filename, function(exists) {
    if (exists) {
      fs.readFile(filename, function(err, fileData) {
        if (err) cb(err);

        var match = persistedSectionRegExp.exec(fileData),
            persistedSection = '';

        if (match != null) {
          persistedSection = match[1];

          if (additions) {
            additions.forEach(function(addition) {
              if (!persistedSection.match(addition.pattern)) {
                persistedSection = persistedSection + addition.text;
              }
            });
          }
        }

        data.persistedSection = persistedSection;
        fs.writeFile(filename, template(data), next);
      });
    } else {
      data.persistedSection = '';
      fs.writeFile(filename, template(data), next);
    }
  });
}

function ContentProviderGenerator() {}

ContentProviderGenerator.prototype.generate = function(destination, contentProviderData, cb) {
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

  async.series([
    function(next) {
      var packageParts    = contentProviderData.packageName.split('.'),
          lastDirectory   = destination,
          directorySeries = [];

      destinationPackage = destination + '/' + packageParts.join('/'),

      packageParts.forEach(function(packagePart) {
        directorySeries.push(function(nextDirectory) {
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
        directorySeries.push(function(nextDirectory) {
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

      async.series(directorySeries, next);
    },
    function(next) {
      async.parallel(templateNames.map(function(templateName) {
        return function(templateDone) {
          fs.readFile(__dirname + '/templates/' + templateName + '.jade', 'utf-8', function(err, template) {
            if (err) templateDone(err);
            templateMap[templateName] = jade.compile(template, options);
            templateDone();
          });          
        }
      }), next);
    },
    function(next) {
      var existingModels = [];

      async.parallel(contentProviderData.models.map(function(model) {
        return function(next) {
          model.packageName = contentProviderData.packageName;
          model.parcelable = model.parcelable ? model.parcelable : false
          model.gson = model.gson ? model.gson : false

          model.enumMap = {};
          model.relationships = contentProviderData.relationships;
          model.constraints = model.constraints || [];
          model.enums = model.members.filter(function(member) {
            return member.type == 'enum';
          }).map(function(member) {
            model.enumMap[member.class] = member.class.substring(member.class.lastIndexOf('.') + 1);

            return member.class;
          });

          addHelpers(model, function() {
            existingModels.push(model.name + '.java');

            fs.writeFile(destinationPackage + '/database/table/' + model.name + 'Table.java', templateMap['database-table'](model), function() {
              writeFileWithPersistedSection(destinationPackage + '/model/' + model.name + '.java', templateMap['model'], null, model, next, cb);
            });
          });          
        }
      }), function() {
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
    },
    function(next) {
      addHelpers(contentProviderData, function() {
        async.parallel([
          function(next) {
            writeFileWithPersistedSection(destinationPackage + '/database/' + helpers.capitalize(helpers.camelCase(contentProviderData.databaseName)) + 'Database.java', templateMap['database'], [
              {
                pattern: /private void initialize\b/,
                text: '\n\n    private void initialize() {\n    }'
              }
            ], contentProviderData, next, cb);
          },
          function(next) {
            fs.writeFile(destinationPackage + '/provider/' + helpers.capitalize(helpers.camelCase(contentProviderData.databaseName)) + 'Provider.java', templateMap['provider'](contentProviderData), next);
          },
          function(next) {
            fs.writeFile(destination + '/content-provider.xml', 
              '<provider android:name="' + contentProviderData.packageName + '.provider.' + helpers.capitalize(helpers.camelCase(contentProviderData.databaseName)) + 'Provider" ' + 
                        'android:authorities="' + (contentProviderData.authority ? contentProviderData.authority : (contentProviderData.packageName + '.provider.' + contentProviderData.databaseName)) + '" android:exported="false" />', 
              next);
          }
        ], next);
      });
    }
  ], cb);
}

module.exports = ContentProviderGenerator;