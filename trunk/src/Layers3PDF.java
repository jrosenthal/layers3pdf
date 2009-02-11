package layers3pdf;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.lowagie.text.xml.xmp.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

import layers3pdf.IniParser;
import layers3pdf.StringUtil;
import layers3pdf.DictionaryTool;

public class Layers3PDF
{
  private String[] args;
  public static void main( String[] args )
  {
    Layers3PDF l3p = new Layers3PDF( args );
    l3p.process();
  }

  public Layers3PDF( String[] args ) { this.args = args; }

  public void process()
  {
    Document document = null;
    String[] template_keys = { "path", "title", "author", "subject", "user_pass", "owner_pass", "keywords" };
    String[] keys = { "path", "name", "export", "print_type", "print_state",
                      "view", "min_zoom", "max_zoom", "visible", "group", 
                      "on_panel", "locked",
                      // dictionary related keys
                      "dict", "vp", "key", "type", "value", 
                      // dictionary related keys for sourcing other documents
                      "source", "keys" };

    // this holds the layers that are on the panel to control merged layers
    HashMap<String,PdfLayer> group_map = new HashMap<String,PdfLayer>();
    HashMap<String,Integer>group_lock_map = new HashMap<String,Integer>();

    int arg_start = 0;
    boolean verbose = false;
    if( args[0].equals( "-v" ) )
    {
      verbose = true;
      ++arg_start;
    }
    try 
    {
      // open the document
      IniParser parser = new IniParser( args[arg_start] );
      PdfWriter writer = null;

      PdfContentByte cb = null;
      DictionaryTool dict_tool = new DictionaryTool();
                  
      // read the ini file
      try
      {
        int layer_count = 0;
        while( true )
        {
          String layer_name = null;
          String layer_path = null;

          if( layer_count++ == 0 )
          {
            HashMap<String,String> template_info = parser.parseLine( template_keys );
            if( template_info == null )
              throw new DataFormatException( "missing page template" );
            layer_path = template_info.get( "path" );

            if( layer_path == null )
              throw new DataFormatException( "unspecified path for page template" );

            System.out.println( "adding template from: " + layer_path );
            PdfReader base = new PdfReader( layer_path );
            document = new Document( base.getPageSizeWithRotation(1) );
            writer = PdfWriter.getInstance( document, 
                                            new FileOutputStream( args[arg_start+1] ) );

            writer.setViewerPreferences( PdfWriter.PageModeUseOC );
            System.out.println( "   setting Pdf Version 1.7" );
            writer.setPdfVersion( PdfWriter.VERSION_1_7 );
            String user_pass = template_info.get( "user_pass" );
            String owner_pass = template_info.get( "owner_pass" );
            boolean pw_locked = user_pass != null || owner_pass != null;
            if( user_pass == null )
              user_pass = "";
            if( owner_pass == null )
              owner_pass = "";

            if( pw_locked )
            {
              System.out.println( "   setting user_pass = \"" + user_pass + 
                                  "\", owner_pass = \"" + owner_pass + "\"" );
              writer.setEncryption( user_pass.getBytes(), owner_pass.getBytes(), 
                                    PdfWriter.ALLOW_COPY | PdfWriter.ALLOW_PRINTING,
                                    PdfWriter.STANDARD_ENCRYPTION_128 );
            }

            // add metadata attributes
            String title = template_info.get( "title" );
            if( title != null )
            {
              System.out.println( "   setting title = " + StringUtil.formatString( title ) );
              document.addTitle( title );
            }
            String keywords = template_info.get( "keywords" );
            if( keywords != null )
              addXMPKeywords( writer, keywords );

            String author = template_info.get( "author" );
            if( author != null )
            {
              System.out.println( "   setting author = " + StringUtil.formatString( author ) );
              document.addAuthor( author );
            }
            String subject = template_info.get( "subject" );
            if( subject != null )
            {
              System.out.println( "   setting subject = " + StringUtil.formatString( subject ) );
              document.addSubject( subject );
            }
            document.addCreator( "Layers3PDF v0.1" );

            document.open();
            PdfImportedPage base_page = writer.getImportedPage( base, 1 );
            cb = writer.getDirectContent();
            cb.addTemplate( base_page, 0f, 0f );
                  
            continue;
          }

          // layers are parsed here
          HashMap<String,String> layer_info = parser.parseLine( keys );
          
          if( layer_info == null )
            break; // end of file

          if( verbose )
          {
            System.out.println( "VV: got line info:" );
            for( String key : layer_info.keySet() )
              System.out.println( "VV: got key=" + key + ", value=" + layer_info.get( key ) );
          }

          // if the dictionary picks it up, it's not a layer definition
          if( dict_tool.parse( layer_info, writer ) ) 
            continue;

          layer_path = layer_info.get( "path" );
          layer_name = layer_info.get( "name" );
          if( layer_name == null )
            layer_name = layer_path;

          if( layer_path == null )
            throw new DataFormatException( "unspecified path for layer" );

               
          System.out.println( "adding: " + layer_name + " from path: " + layer_path );
          PdfReader reader = new PdfReader( layer_path );
          int rotation = reader.getPageRotation( 1 );
          Rectangle page_size = reader.getPageSize( 1 );
          PdfLayer layer = null;

          String group_name = layer_info.get( "group" );
          PdfLayer group_control = null;
          if( group_name != null )
          {
            group_control = group_map.get( group_name );
            if( group_control == null )
            {
              group_control = new PdfLayer( group_name, writer );
              group_map.put( group_name, group_control );
            }
          }

          PdfOCG current_layer = null;

          layer = new PdfLayer( layer_name, writer );

          // add attributes to layer, but not if merging
          int export = StringUtil.getTritFromString( layer_info.get( "export"), 2 );
          if( export != 2 )
          {
            System.out.println( "   setting export = " + export );
            layer.setExport( export == 1 );
          }
          int visible = StringUtil.getTritFromString( layer_info.get( "visible" ), 2 );
          if( visible != 2 )
          {
            System.out.println( "   setting visible = " + visible );
            layer.setOn( visible == 1 );
          }
          int on_panel = StringUtil.getTritFromString( layer_info.get( "on_panel" ), 2 );
          if( on_panel != 2 )
          {
            System.out.println( "   setting on_panel = " + on_panel );
            layer.setOnPanel( on_panel == 1 );
          }


          int view = StringUtil.getTritFromString( layer_info.get( "view" ), 2 );
          if( view != 2 )
          {
            System.out.println( "   setting view = " + view );
            layer.setView( view == 1 );
          }
          String print_type = layer_info.get( "print_type" );
          if( print_type == null )
            print_type = "Print";
          int print_state = StringUtil.getTritFromString( layer_info.get( "print_state" ), 2 );
          if( print_state != 2 )
          {
            System.out.println( "   setting print_type = " + print_type + 
                                ", print_state = " + print_state );
            layer.setPrint( print_type, print_state == 1 );
          }

          float min_zoom = StringUtil.getFloatFromString( layer_info.get( "min_zoom" ), -1f );
          float max_zoom = StringUtil.getFloatFromString( layer_info.get( "max_zoom" ), -1f );
          boolean is_zooming = ( min_zoom >= 0. || max_zoom >= 0. );
          int locked = StringUtil.getTritFromString( layer_info.get( "locked" ), 2 );
          if( locked == 1 )
          {
            System.out.println( "   setting locked = " + locked );
            if( !is_zooming && group_name == null )
              writer.lockLayer( layer );
          }  
          if( is_zooming || group_name != null )
          {
            if( is_zooming )
            {
              System.out.println( "   setting min_zoom = " + min_zoom + ", max_zoom = " + max_zoom );
              layer.setZoom( min_zoom, max_zoom );
            }

            // when we set the zoom we need a secondary layer to control what is happening
            // 'cause acrobat doesn't know how to do zoom-inferred visibility properly

            // when we are part of a group, we also need a membership, and the group
            // control becomes part of it

            PdfLayerMembership members = new PdfLayerMembership( writer );
            members.setVisibilityPolicy( PdfLayerMembership.ALLON );
            layer.setOnPanel( false );
            members.addMember( layer );

            // for the control layer, set all attributes to the ALWAYS setting; this way,
            // the membership will inherit the behaviour of the most restrictive layer

            if( group_name == null ) // the group takes care of the control layer
            {
              PdfLayer control_layer = new PdfLayer( layer_name, writer );
              setAllAttributes( control_layer );
              members.addMember( control_layer );

              if( locked == 1 )
                writer.lockLayer( control_layer );
              // it seems we need to begin/end the layer for it to show up on the panel
              cb.beginLayer( control_layer );
              cb.endLayer();
            }
            else
            {
              setAllAttributes( group_control );
              members.addMember( group_control );
              Integer already_locked = group_lock_map.get( group_name );
              if( locked !=2 && already_locked != null && already_locked != locked ) // lock conflict
                System.out.println( "WARNING: layer \"" + layer_name + 
                                    "\" has locked setting conflicting with other layers in group: " + group_name );

              if( locked == 1 )
              {
                writer.lockLayer( group_control );
                System.out.println( "   locking group: " + group_name );
              }
              if( locked != 2 )
                group_lock_map.put( group_name, locked );

              System.out.println( "   adding layer to group: " + group_name );
            }

            current_layer = members;
          }
          else
            current_layer = layer;
                  
          cb.beginLayer( current_layer );

          PdfImportedPage page = writer.getImportedPage( reader, 1 );

          float rad = (float)-rotation * (float)Math.PI / 180f;
          cb.addTemplate( page, 
                          (float) Math.cos( rad ),
                          (float) Math.sin( rad ),
                          - (float) Math.sin( rad ),
                          (float) Math.cos( rad ),
                          0f, rotation != 0 ? page_size.getWidth() : 0f );
          cb.endLayer();
        }
      }

      catch ( NullPointerException e ) { System.out.println( e.getMessage() ); }

      dict_tool.finalize( writer );
      document.close();
    }
    catch(DocumentException de) { System.err.println( "Document exception: " + de.getMessage()); }
    catch(IOException ioe) { System.err.println( "IO exception: " + ioe.getMessage()); }
    catch(DataFormatException dfe) { System.err.println( "Data format exception: " + dfe.getMessage()); }

    System.out.println( "wrote output to: " + args[arg_start+1] );
  }

  // maximize the "on"-ness of all relevant visibility attributes in a layer;
  // this method is intended for use with control layers (group, zooming, etc.)
  // so that the membership that contains the actual layer contents inherits 
  // the behaviour of the most restricted layer (i.e. the layer with user
  // specified attributes)

  void setAllAttributes( PdfLayer layer )
  {
    layer.setExport( true );
    layer.setOn( true );
    layer.setView( true );
    layer.setPrint( "Print", true );
  }

  void addXMPKeywords( PdfWriter writer, String keywords_str ) throws IOException
  {
    String[] words = keywords_str.split( "," );

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    XmpWriter xmp = new XmpWriter(os);
    XmpSchema dc = new DublinCoreSchema();
    XmpArray subject = new XmpArray(XmpArray.UNORDERED);

    for( String w : words )
    {
      subject.add( w );
      System.out.println( "   adding keyword: " + w );
    }

    dc.setProperty(DublinCoreSchema.SUBJECT, subject);
    xmp.addRdfDescription(dc);
    PdfSchema pdf = new PdfSchema();
    pdf.addVersion( "1.7" );
    xmp.addRdfDescription(pdf);
    xmp.close();
    writer.setXmpMetadata(os.toByteArray());
  }

}

