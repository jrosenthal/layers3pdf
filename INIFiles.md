# INI File Description #

Layers3PDF builds a Layered PDF from a series of PDFs.  The list of these component PDFs, and the settings to apply in constructing the Layered PDF are contained in the INI file.  An INI file is composed of at least two lines of `|` delimited variable pairs, connected to their value with an `=`.  White space is allowed in values.  Only the path variable is required for every line in the INI file, all other variables are set to defaults if not defined.  The first line defines the file, additional lines define the individual layers.  The order in which layers are defined represents the order in which the appear in the Layered PDF.  Those layers defined first will be, in effect, further back on the stack, and layers defined later will appear on top of them.


# INI Construction #

## Line 1: File Variables ##

The first line of the INI holds variables that relate to the Layered PDF as a whole (file variables), later lines hold variables that relate to specific layers.

File Variables include:
`path, title, author, subject, user_pass, owner_pass`

  * **path=**  The file variable `path` must reference a blank (transparent/empty) pdf of the same dimensions of all layers.  Path follows standard directory structure.  White spaces in file paths are allowed.
  * **title=**  The file variable `title` will be used to set the PDF Document Title, as seen in Document Properties when viewing the PDF.
  * **author=**  The file variable `author` will be used to set the PDF Document Author, as seen in Document Properties when viewing the PDF.
  * **subject=** The file variable `subject` will be used to set the PDF Document Subject, as seen in Document Properties when viewing the PDF.  This may be relevant for keyword searches.
  * **user\_pass=** & **owner\_pass=**  The file variables `user_pass` and `owner_pass` are used to control a user's ability to view and edit the LayeredPDF.  This is also known as encrypting/locking the PDF.
    * If `owner_pass` is set, a user must enter the `owner_pass` to modify the PDF using Acrobat.
    * If `user_pass` is set, a user must enter the `user_pass` to open and view the PDF.
    * If `user_pass` is set, and no `owner_pass` is set, Layers3PDF will generate one randomly, thus locking the file from editing.

## Lines 2+: Layer Variables ##

All additional lines of the INI hold variables that define the specific layer of the PDF.

Layer Variables include:
`path, name, export, print_type, print_state, view, min_zoom, max_zoom, on_panel, group, (locked)`
  * **path=**  The layer variable `path` must be the filepath for a _transparent_ pdf containing just the features in this layer.
  * **name=**  The layer variable `name` contains the name of the layer as it will appear on the layer panel in Acrobat.
  * **export=**  The layer variable `export` controls whether or not the layer will be included for export by Acrobat or other applications.  Acceptable values are 0, 1 or 2. Not setting export is equivalent to `export=2`.
    * 0 - Never export this layer.
    * 1 - Always export this layer.
    * 2 - Export this layer when it is ON.
  * **print\_type=**  The layer variable `print type` contains a string indicating what type of content is being controlled by the layer for printing.  Possible values might include: watermark, trapping, etc.
  * **print\_state=**  The layer variable `print_state` controls whether or not the layer will be oprinted.  Acceptable values are 0, 1 or 2. Not setting print\_state is equivalent to `print_state=2`.
    * 0 - Never print this layer.
    * 1 - Always print this layer.
    * 2 - Print this layer when it is ON.
  * **view=**
  * **min\_zoom=**
  * **max\_zoom=**
  * **on\_panel=**
  * **group=** The layer variable `group` accepts a string.  All layers with matching `group`s will be represented on the panel by the group, which will control their on/off values.
  * **locked=**

... this page is under development.