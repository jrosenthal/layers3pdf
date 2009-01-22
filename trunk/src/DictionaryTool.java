package layers3pdf;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

import layers3pdf.StringUtil;

public class DictionaryTool
{
  HashMap<String,PdfDictionary> dicts = new HashMap<String,PdfDictionary>();
  HashMap<String,PdfIndirectReference> ind_refs = new HashMap<String,PdfIndirectReference>();
  HashSet<String> vp_dicts = new HashSet<String>();
  VPEvent vp_event;

  class VPEvent implements PdfAddPageEvent
  {
    public void onOpenDocument(PdfWriter writer, Document document) {}
    public void onStartPage(PdfWriter writer, Document document) {}
    public void onEndPage(PdfWriter writer, Document document) {}
    public void onCloseDocument(PdfWriter writer, Document document) {}
    public void onParagraph(PdfWriter writer, Document document, float paragraphPosition) {}
    public void onParagraphEnd(PdfWriter writer,Document document,float paragraphPosition) {}
    public void onChapter(PdfWriter writer,Document document,float paragraphPosition, Paragraph title) {}
    public void onChapterEnd(PdfWriter writer,Document document,float paragraphPosition) {}
    public void onSection(PdfWriter writer,Document document,float paragraphPosition, int depth, Paragraph title) {}
    public void onSectionEnd(PdfWriter writer,Document document,float paragraphPosition) {}
    public void onGenericTag(PdfWriter writer, Document document, Rectangle rect, String text) {}
    public void onAddPage(PdfWriter writer, Document document, PdfPage page)
    {
      PdfArray vps = new PdfArray();

      if( vp_dicts.size() == 0 )
        return;

      for( String vp_name : vp_dicts )
      {
        PdfDictionary vp = dicts.get( vp_name );
        vps.add( vp );
      }

      System.out.println( "adding VP dictionaries to page" );
      page.put( new PdfName( "VP" ), vps );
    }
  }

  boolean parse( HashMap<String,String> info, PdfWriter writer ) throws DataFormatException, IOException
  {
    String dict_name = info.get( "dict" );
    if( vp_event == null )
    {
      vp_event = new VPEvent();
      writer.setPageEvent( vp_event );
    }

    String [] valid_types = { "int", "name", "string", "float_array", "name_array", "dict_ref" };
    if( dict_name != null )
    {
      String source = info.get( "source" );
      if( source != null )
      {
        throw new DataFormatException( "source is not yet supported for dictionaries" );

        // String keys[] = info.get( "keys" ).split( "," );
      }

      String key = info.get( "key" );
      String type = info.get( "type" );
      String value = info.get( "value" );
      String vp = info.get( "vp" );

      System.out.println( "found entry for dictionary: " + dict_name + 
                          ", key: " + key + 
                          ", type: " + type +
                          ", value: " + StringUtil.formatString( value ) + 
                          (vp != null ? (", vp: " + vp) : "") );

      // go through each of the valid types and parse the string
      // accordingly, auto-vivifying the dictionary if necessary and
      // inserting the value
      PdfDictionary dict = vivify( dict_name );
      if( vp != null && StringUtil.getIntFromString( vp, 0 ) == 1 && !vp_dicts.contains( dict_name ) )
        vp_dicts.add( dict_name );

      if( key == null && type == null && value == null ) // assume we just want to create a dictionary
        return true;

      if( key == null || type == null || value == null )
        throw new DataFormatException( "key, type or value for non-dictionary key not specified for dictionary: " + dict );

      PdfObject obj = null;
      if( type.equals( "int" ) )
        obj = new PdfNumber( StringUtil.getIntFromString( value, 0 ) );
      else if( type.equals( "double" ) )
        obj = new PdfNumber( StringUtil.getDoubleFromString( value, 0. ) );
      else if( type.equals( "float_array" ) )
      {
        String[] svals = value.split( "," );
        float[] vals = new float[svals.length];

        for( int i=0; i<svals.length; ++i )
          vals[i] = Float.parseFloat( svals[i] );
        
        obj = new PdfArray( vals );
      }
      else if( type.equals( "name_array" ) )
      {
        PdfArray arr = new PdfArray();
        for( String str : value.split( "," ) )
          arr.add( new PdfName( str ) );
        obj = arr;
      }
      else if( type.equals( "name" ) )
        obj = new PdfName( value );
      else if( type.equals( "string" ) )
        obj = new PdfString( value );
      else if( type.equals( "dict_ref" ) )
        {
          obj = ind_refs.get( value );
          if( obj == null ) // try to find it in dicts if it's not yet in the writer body
            {
              PdfDictionary existing_dict = dicts.get( value );
              if( existing_dict == null )
                throw new DataFormatException( "invalid dictionary: " + value );

              // the dict must be fully created by now
              System.out.println( "adding dictionary to writer: " + value );
              PdfIndirectObject ind_obj = writer.addToBody( existing_dict );
              ind_refs.put( value, ind_obj.getIndirectReference() );
              obj = ind_refs.get( value );
            }
        }
      else
        throw new DataFormatException( "invalid dictionary object type: " + type );
      
      dict.put( new PdfName( key ), obj );

      return true;
    }

    return false;
  }

  PdfDictionary vivify( String full_name )
  {
    PdfDictionary dict = null;
    for( String name : full_name.split( "/" ) )
    {
      if( dict == null ) // first one
      {
        dict = dicts.get( name );
        if( dict == null )
        {
          // name the dictionary, so when it is added to the writer, 
          // the reader can recognize it for what it is
          dicts.put( name, new PdfDictionary( new PdfName( name ) ) );
          dict = dicts.get( name );
        }
      }
      else
      {
        PdfName pname = new PdfName( name );
        if( dict.get( pname ) == null )
          dict.put( pname, new PdfDictionary() );

        dict = dict.getAsDict( pname );
      }
    }
    return dict;
  }

  public void finalize( PdfWriter writer ) throws IOException
  {
    for( String name : dicts.keySet() )
      if( ind_refs.get( name ) == null && !vp_dicts.contains( name ) ) // don't need to write VP dicts to body
        {
          System.out.println( "adding dictionary to writer: " + name );
          writer.addToBody( dicts.get( name ) );
        }
  }
}
