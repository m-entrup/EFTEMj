# SR_EELS_ImportPlugin

## Configuration

All Plugins, that are part of EFTEMj, are configured by using the `EFTEMj_config.xml`. This file can be found in your home directory. 

### Database path

On the first run, SR_EELS_ImportPlugin asks you to define a folder, that is used as database. You can edit this path at the `EFTEMj_config.xml`.

### Supported file types

By default SR_EELS_ImportPlugin imports dm3 files. You can change the supported file formats by adding/editing the `<fileTypeToImport>` elements in your `EFTEMj_config.xml`.


## Example `EFTEMj_config.xml`

```
<configuration>
...
  <SR-EELS>
    ...
    <SR_EELS_ImportPlugin>
      <databasePath>/home/michael/Dokumente/SR-EELS_database/</databasePath>
      <fileTypeToImport>.dm3</fileTypeToImport>
      <fileTypeToImport>.tif</fileTypeToImport>
      <fileTypeToImport>.tiff</fileTypeToImport>
      ...
    </SR_EELS_ImportPlugin>
    ...
  </SR-EELS>
...
</configuration>
```
