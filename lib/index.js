'use strict';

var fs    = require('fs'),
    jade  = require('jade'),
    async = require('async'),
    _     = require('lodash');

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

ContentProviderGenerator.prototype.generate = function(destination, data, cb) {
  var options = {
        pretty: true
      },
      templateNames = [
        'model~pojo',
        'model~table',
        'provider',
        'database',
        'database-table'
      ],
      templateMap = {},
      destinationPackage;

  async.series([
    function(next) {
      var packageParts    = data.packageName.split('.'),
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

      data.externalDatabaseWithDebug = data.externalDatabaseWithDebug ? data.externalDatabaseWithDebug : false;

      templateNames.filter(function(templateName) {
        return !data.modelsOnly || templateName.indexOf('model') > -1;
      }).forEach(function(templateName) {
        directorySeries.push(function(nextDirectory) {
          var directory = lastDirectory + '/' + templateName.replace(/~.*/, '').replace('-', '/');
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

      data.models.forEach(function(model) {
        directorySeries.push(function(nextDirectory) {
          var directory = lastDirectory + '/model/' + (model.package ? model.package : '');
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
            options.filename = __dirname + '/templates/' + templateName + '.jade';
            templateMap[templateName] = jade.compile(template, options);
            templateDone();
          });          
        }
      }), next);
    },
    function(next) {
      var existingModels = [];

      async.parallel(data.models.map(function(model) {
        return function(next) {
          model.packageName = data.packageName;
          model.parcelable = model.parcelable ? model.parcelable : false;
          model.gson = model.gson ? model.gson : false;
          model.simpleXml = model.simpleXml ? model.simpleXml : false;
          model.package = model.package ? model.package : undefined;

          model.relationships = data.relationships;
          model.constraints = model.constraints || [];
          model.constructors = model.constructors || [];
          model.simpleXmlNamespaces = model.simpleXmlNamespaces || [];
          model.memberMap = {};

          model.members.forEach(function(member) {
            model.memberMap[member.name] = member;
          });

          model.constructors.forEach(function(constructor) {
            var members = [];

            constructor.members.forEach(function(memberName) {
              members.push(model.memberMap[memberName]);
            })

            constructor.members = members;
          })

          model.enumMap = {};
          model.enums = model.members.filter(function(member) {
            return member.type == 'enum';
          }).map(function(member) {
            model.enumMap[member.class] = member.class.substring(member.class.lastIndexOf('.') + 1);

            return member.class;
          });

          model.importLists = false;
          model.importSerializable = false;
          model.classes = false;
          model.classLists = false;
          model.modelMap = {};
          model.modelNameMap = {};
          model.models = _.unique(model.members.filter(function(member) {
            return member.type == 'class' || member.type == 'class[]';
          }).map(function(member) {
            var className = member.class.substring(member.class.lastIndexOf('.') + 1)

            model.modelNameMap[member.class] = className;

            if (member.type == 'class[]') {
              className = 'List<' + className + '>';
              model.importLists = true;

              if (member.serializable) {
                model.importSerializable = true;
              }

              model.classLists = true;
            } else if (member.type == 'class') {
              model.classes = true;
            }

            model.modelMap[member.name + member.class] = className;

            if (member.class.substring(0, member.class.lastIndexOf('.')) == model.packageName + '.model' + (model.package ? '.' + model.package : '')) {
              return null;
            }

            return member.class;
          }).filter(function(model) {
            return model != null;
          }));

          addHelpers(model, function() {
            var modelAdditions = (model.gson || model.simpleXml) && (model.classes || model.classLists) ? [
                {
                  pattern: /private Gson getGson\b/,
                  text: '    private Gson getGson() {\n        return new Gson();\n    }'
                }
            ] : null;

            existingModels.push(model.name + '.java');              

            model.importDate = _.any(model.members, function(member) {
              return member.type == 'Date';
            });

            if (data.modelsOnly || model.noTable) {
              writeFileWithPersistedSection(destinationPackage + '/model/' + (model.package ? (model.package + '/') : '') + model.name + '.java', templateMap['model~pojo'], null, model, next, cb);
            } else {
              model.importLists = true;

              fs.writeFile(destinationPackage + '/database/table/' + model.name + 'Table.java', templateMap['database-table'](model), function() {
                writeFileWithPersistedSection(destinationPackage + '/model/' + (model.package ? (model.package + '/') : '') + model.name + '.java', templateMap['model~table'], modelAdditions, model, next, cb);
              });
            }
          });          
        }
      }), function() {
        fs.readdir(destinationPackage + '/model/', function(err, files) {
          if (err) cb(err);

          files.forEach(function(fileOrDirectory) {
            if (fs.lstatSync(destinationPackage + '/model/' + fileOrDirectory).isDirectory()) {
              fs.readdirSync(destinationPackage + '/model/' + fileOrDirectory).forEach(function(file) {
                if (existingModels.indexOf(file) < 0) {
                  fs.unlinkSync(destinationPackage + '/model/' + fileOrDirectory + '/' + file);
                }
              });

              if (fs.readdirSync(destinationPackage + '/model/' + fileOrDirectory).length == 0) {
                fs.rmdirSync(destinationPackage + '/model/' + fileOrDirectory);
              }
            } else if (existingModels.indexOf(fileOrDirectory) < 0) {
              fs.unlinkSync(destinationPackage + '/model/' + fileOrDirectory);
              fs.unlinkSync(destinationPackage + '/database/table/' + fileOrDirectory.replace('.java', 'Table.java'));
            }
          });

          next();
        })
      });
    },
    function(next) {
      if (data.modelsOnly) {
        next();
      } else {
        addHelpers(data, function() {
          async.parallel([
            function(next) {
              if (data.databaseName) {
                writeFileWithPersistedSection(destinationPackage + '/database/' + helpers.capitalize(helpers.camelCase(data.databaseName)) + 'Database.java', templateMap['database'], [
                  {
                    pattern: /private void initialize\b/,
                    text: '\n\n    private void initialize(final SQLiteDatabase db) {\n    }'
                  }
                ], data, next, cb);
              } else {
                next();
              }
            },
            function(next) {
              if (data.databaseName) {
                fs.writeFile(destinationPackage + '/provider/' + helpers.capitalize(helpers.camelCase(data.databaseName)) + 'Provider.java', templateMap['provider'](data), next);
              } else {
                next();
              }
            },
            function(next) {
              if (data.databaseName) {
                fs.writeFile(destination + '/content-provider.xml', 
                  '<provider android:name="' + data.packageName + '.provider.' + helpers.capitalize(helpers.camelCase(data.databaseName)) + 'Provider" ' + 
                            'android:authorities="' + (data.authority ? data.authority : (data.packageName + '.provider.' + data.databaseName)) + '" android:exported="false" />', 
                  next);
              } else {
                next();
              }
            }
          ], next);
        });
      }
    }
  ], cb);
}

module.exports = ContentProviderGenerator;