# Introduction #
|**INI Line**|**Explanation**|**Maptitude Notes**|
|:-----------|:--------------|:------------------|
|dict=Measure|key=Subtype|type=name|value=GEO|
|**dict=Measure|key=Bounds|type=float\_array|value=0.0,0.0,0.0,1.0,1.0,1.0,1.0,0.0**|
|dict=Measure|key=PDU|type=name\_array|value=M,SQM,GRD|
|**dict=Measure|key=LPTS|type=float\_array|value=0.0,0.0,1.0,1.0**|??Description of GPTS coordinate locations in a 'unit square'??|
|**dict=Measure|key=GPTS|type=float\_array|value=42.756143,-71.041457,42.763858,-71.030150**|??Described reference coordinates referred to by LPTS??|
|dict=PROJCS|key=Type|type=name|value=PROJCS|
|**dict=PROJCS|key=EPSG|type=int|value=26986**|www.spatialreference.org|
|dict=Measure|key=GCS|type=dict\_ref|value=PROJCS|
|dict=Measure|key=DCS|type=dict\_ref|value=PROJCS|
|dict=MyVP|vp=1|key=Type|type=name|value=Viewport|
|dict=MyVP|vp=1|key=Measure|type=dict\_ref|value=Measure|
|**dict=MyVP|vp=1|key=BBox|type=float\_array|value=161.82,19.51,771.24,592.21**|The part of the page, in 1/72 of an inch, that should be georeferenced/has the map. Landscape - 0,0 is top left, order is Y,X.  Portrait - 0,0 is bottom left, order X,Y|Mapt:`MapAnnotation[2][1][2], divide by 100, subtract Y values from (page height in inches*72)  NOTE: GetLayoutPrintSize() shows that maptitude actually produces  a slightly smaller image than it claims to ie: (10.9769444" x 8.49833333"`|

# Details #

## GPTS ##
array (Required; ExtensionLevel 3) An array of numbers taken pairwise,
defining points in geographic space as degrees of latitude and
longitude. These values are based on the geographic coordinate
system described in the GCS dictionary. (Note that any projected
coordinate system includes an underlying geographic coordinate
system.)

(Adobe Supplement to ISO 3200, p 51)

## LPTS ##
array (Optional; ExtensionLevel 3) An array of numbers taken pairwise that
define points in a 2D unit square. The unit square is mapped to the
rectangular bounds of the viewport, image XObject, or forms
XObject that contain the measure dictionary. This array contains
the same number of number pairs as the GPTS array; each number
pair is the unit square object position corresponding to the
geospatial position in the GPTS array.

(Adobe Supplement to ISO 3200, p 51)

## Bounds ##

array (Optional; ExtensionLevel 3) An array of numbers taken pairwise that
define a series of points that describes the bounds of an area for
which geospatial transformations are valid. For maps, this
bounding polygon is know as a neatline. These numbers are
expressed relative to a unit square that describes the BBox
associated with a Viewport or form XObject, or the bounds of an
image Xobject.
If not present, the default values define a rectangle describing the
full unit square, with values of [0.0 0.0 0.0 1.0 1.0 1.0 1.0 0.0].
Note: The polygon description need not be explicitly closed by
repeating the first point values as a final point.

(Adobe Supplement to ISO 3200, p 50)

# Problems #

All attempts so far generate a NE-SW and NW-SE meridian (ie: one of the values does not change as one traverses the diagonal).  WEIRD.  Any ideas?