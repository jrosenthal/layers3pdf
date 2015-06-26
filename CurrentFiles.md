# Introduction #
The following links should link directly to the latest versions of the appropriate files.  Right-click, save-as, etc.


# Details #

Compiled Code:
Classes:
  * http://layers3pdf.googlecode.com/svn/trunk/lib/Layers3PDF.class - 9kb
  * http://layers3pdf.googlecode.com/svn/trunk/lib/StringUtil.class
  * http://layers3pdf.googlecode.com/svn/trunk/lib/DictionaryTool$VPEvent.class
  * http://layers3pdf.googlecode.com/svn/trunk/lib/DictionaryTool.class
  * http://layers3pdf.googlecode.com/svn/trunk/lib/IniParser.class

Jars:
  * ~~http://layers3pdf.googlecode.com/svn/trunk/lib/itext_new.jar - 1.4 mb~~
  * ~~http://layers3pdf.googlecode.com/svn/trunk/lib/iText-2.1.2u.jar - 1 mb~~
  * http://layers3pdf.googlecode.com/svn/trunk/lib/bcprov_new1.4.jar - 1 mb
  * http://layers3pdf.googlecode.com/svn-history/r49/trunk/lib/iText-2.1.4_mod.jar - 1 mb

All Classes must be in a sub-directory called layers3pdf.
ex:
```
foo/
   bcprov_new1.4.jar
   iText-2.1.4_mod.jar
   layers3pdf/
            Layers3PDF.class
            StringUtil.class
            DictionaryTool$VPEvent.class
            DictionaryTool.class
            IniParser.class
               ...
```

A sample command line might be as follows:
in c:\foo\

java -cp .;iText-2.1.4\_mod.jar;bcprov\_new1.4.jar layers3pdf.Layers3PDF "C:\foo\demo1\demowGS2.ini" "C:\foo\demo1\foo.pdf"



Source Code:
