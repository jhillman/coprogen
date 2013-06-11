'use strict';

var CodeGenerator = require('../../lib'),
    generator,
    templateData
    ;

templateData = {
  'packageName': 'com.example',
  'models': [
    {
      'name': 'Person',
      'members' : [
        {
          'type' : 'String',
          'name' : 'name'
        },
        {
          'type' : 'int',
          'name' : 'age'
        },
        {
          'type' : 'boolean',
          'name' : 'alive'
        },
        {
          'type' : 'double',
          'name' : 'body_fat'
        }
      ]
    }
  ]
}

generator = new CodeGenerator();

generator.generateContentProvider(__dirname, templateData, function(err, data) {
  if (err) {
    console.log('error again: ' + err); 
    process.exit(1);
  }
});