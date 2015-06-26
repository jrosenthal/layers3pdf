# Layers3PDF Contrived Structures #

Layers3PDF uses iText and some logic to contrive two noteworthy structures not obvious in the basic pdfLayer methods that iText provides.  These structures are grouped layers, and zoomed layers.


# The Goals #

  * Grouped Layers are multiple layers of map content, all controlled by a shared button on the Layers Panel.  Each layer may have different `print_state`/`view`/and `export` settings.
  * Zoomed Layers are layers controlled by both a button on the Layers Panel, and by an autozoom setting.  A layer must both be on on the panel, and at the appropriate zoom level in order to be seen.  Multiple Zoomed Layers may be controlled by the same button on the Layers Panel (Grouped Zoom Layers), in order to produce the effect of a single data type with multiple symbologies as appropriate (ie: text labels that always stay an appropriate size, etc).

# The Solution #

Every layer that is Grouped or Zoomed is composed of 2 pdfLayers, and a pdfLayerMembership.  In the case of grouped layers, the pdfLayer button will be shared amongst all layers bearing the same `group=`.

  1. pdfLayer content - holds the content of the layer, and any settings (`print_state`, `view`, `export`, `setZoom` etc) as appropriate.  `setOn` should be ON.
  1. pdfLayer button - bears the name of the Zoomed layer, or the group= name, and is displayed on the Layers panel.  Print, Export and View= ALWAYS (1).
  1. pdfLayerMembership - contains both layers as members, setVisibilityPolicy, ALLON.

Lets follow the logic.
  * A Zoomed Layer.  If the button is on, but the content layer is off due to zoom, then pdfLayerMembership.ALLON will result in the layer not being shown.  The opposite is also true.
  * Grouped Layers.  If the button is on, since all content layers are also on, the ALLON is true for all pdfLayerMemberships, and all grouped layers are turned on.

# Printing, Viewing, Exporting... #

Printing, viewing and exporting function appear to function as alternative forms of ON/OFF.  Therefore, if setVisibilityPolicy is set to ALLON, one layer is set to print\_state=ALWAYS, and the other to print\_state=IfOn, and the layer is off, then setVisibilityPolicy will tell the layer not to print.

We need to check this in more depth, but it appears to be true.