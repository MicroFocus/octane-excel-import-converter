# Octane Excel import converter

This project provides a way to convert other Excel formats to Octane import format.

Supported formats: 
- **qTest**, required fields: `Id`, `Test Step Description`, `Test Step Expected Result`.

## Running the tool

- fill the converter.properties file
- fill the mapping.json file
- run the run.bat (Windows) or run.sh (linux)

  Note: The configuration files (converter.properties and mapping.json) should be in the same directory with the jar.

## Configuration

### Properties

The properties file is called **converter.properties** and has the following options structure:

```properties
# The path to the input Excel file. Absolute (C:/dev/public/File.xls) or relative (./File.xls) file path.
input.file.path=
# The path to the output Excel file. Absolute (C:/dev/public/File.xls) or relative (./File.xls) file path.
output.file.path=
```

### Mappings

The mappings file is called **mapping.json** and has the following structure:

```json5
{
  // a mapping can be specified for each OTHER_FORMAT_FIELD_NAME, order of the mappings dictate the order of fields in the output Excel file
  "field_mappings": {
    "<OTHER_FORMAT_FIELD_NAME_1>": {
      // the Octane field name this field will be mapped to
      "target": "<OCTANE_FIELD_NAME>",
      // *Optional* the separator between values, supports regex, should be used when a field contains multiple values that should be mapped
      "mappings_separator": "<OTHER_FORMAT_VALUE_SEPARATOR>",
      // multiple values can be mapped, has precedence over regex_mappings, if none match and the default isn't specified it will try regex_mappings
      // if a value is mapped to "" after conversion it will be ignored
      "mappings": {
        // the default value replacement that is used when others don't match
        "default": "<DEFAULT_REPLACEMENT_VALUE>",
        "<MAPPING_VALUE_1>": "<REPLACEMENT_VALUE>",
        "<MAPPING_VALUE_2>": "<REPLACEMENT_VALUE>"
      },
      // regex mappings are matched from top to bottom, groups can be used, if none match the value won't be mapped
      "regex_mappings": {
        "<REGEX_MAPPING_VALUE_1>": "<REGEX_REPLACEMENT_VALUE>",
        "<REGEX_MAPPING_VALUE_2>": "<REGEX_REPLACEMENT_VALUE>"
      }
    },
    // OTHER_FORMAT_FIELD_NAME will be mapped to OCTANE_FIELD_NAME without any value mappings
    "<OTHER_FORMAT_FIELD_NAME_2>": {
      "target": "<OCTANE_FIELD_NAME>"
    }
  }
}
```

Regex Examples: 
- `"[\\s\\S]*\\. ([\\s\\S]*)"`:`"$1"`, can be used for removing part of the input. For values: "App 1. Octane", "App 2. MyApp"
would match and the result would be: "Octane", "MyApp" respectively. 
- `"[\\s\\S]*&"`:`""`, can be used to remove all values that end in "&".

Note: You have to use double backslash `\\` to define a single backslash `\ `. If you want to define `\w`, then you must be
using `\\w` in your regex. The method used is `REGEX_MAPPING_VALUE.replaceAll(REGEX_REPLACEMENT_VALUE)`. A more
compressive regex guide can be found [here](https://www.vogella.com/tutorials/JavaRegularExpressions/article.html).
