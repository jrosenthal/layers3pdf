import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

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
      String[] template_keys = { "path", "title", "author", "subject", "user_pass", "owner_pass" };
      String[] keys = { "path", "name", "export", "print_type", "print_state",
                        "view", "min_zoom", "max_zoom", "visible" };

      HashMap<String,PdfOCG> name_map = new HashMap<String,PdfOCG>();

      try 
      {
         // open the document
         BufferedReader ini = new BufferedReader( new FileReader( args[0] ) );
         PdfWriter writer = null;

         PdfContentByte cb = null;
                  
         // read the ini file
         try
         {
            int layer_count = 0;
            while( ini.ready() )
            {
               String line = ini.readLine();
               if( line.matches( "^[ \t]*#.*" ) ) // comments allowed (lines starting with #)
                   continue;

               String layer_name = null;
               String layer_path = null;

               if( layer_count++ == 0 )
               {
                  HashMap<String,String> template_info = parseLayer( template_keys, line );
                  layer_path = template_info.get( "path" );

                  if( layer_path == null )
                     throw new DataFormatException( "unspecified path for page template" );

                  System.out.println( "adding template from: " + layer_path );
                  PdfReader base = new PdfReader( layer_path );
                  document = new Document( base.getPageSizeWithRotation(1) );
                  writer = PdfWriter.getInstance( document, 
                                                  new FileOutputStream( args[1] ) );

                  writer.setViewerPreferences( PdfWriter.PageModeUseOC );
                  String user_pass = template_info.get( "user_pass" );
                  String owner_pass = template_info.get( "owner_pass" );
                  boolean locked = user_pass != null || owner_pass != null;
                  if( user_pass == null )
                     user_pass = "";
                  if( owner_pass == null )
                     owner_pass = "";

                  if( locked )
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
                     System.out.println( "   setting title = " + title );
                     document.addTitle( title );
                  }
                  String author = template_info.get( "author" );
                  if( author != null )
                  {
                     System.out.println( "   setting author = " + author );
                     document.addAuthor( author );
                  }
                  String subject = template_info.get( "subject" );
                  if( subject != null )
                  {
                     System.out.println( "   setting subject = " + subject );
                     document.addSubject( subject );
                  }
                  document.addCreator( "Layers3PDF v0.1" );

                  document.open();
                  PdfImportedPage base_page = writer.getImportedPage( base, 1 );
                  cb = writer.getDirectContent();
                  cb.addTemplate( base_page, 0f, 0f );
                  
                  continue;
               }

               HashMap<String,String> layer_info = parseLayer( keys, line );
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

               // look and see if we've seen this name before
               PdfOCG lookup = name_map.get( layer_name );
               PdfOCG current_layer;
               if( lookup != null )
               {
                  current_layer = lookup;
                  System.out.println( "   merging with previous layer of same name (all attributes ignored)"  );
               }
               else
               {
                  layer = new PdfLayer( layer_name, writer );

                  // add attributes to layer, but not if merging

                  int export = getTritFromString( layer_info.get( "export"), 2 );
                  if( export != 2 )
                  {
                     System.out.println( "   setting export = " + export );
                     layer.setExport( export == 1 );
                  }
                  int visible = getTritFromString( layer_info.get( "visible" ), 2 );
                  if( visible != 2 )
                  {
                     System.out.println( "   setting visible = " + visible );
                     layer.setOn( visible == 1 );
                  }

                  int view = getTritFromString( layer_info.get( "view" ), 2 );
                  if( view != 2 )
                  {
                     System.out.println( "   setting view = " + view );
                     layer.setView( view == 1 );
                  }
                  String print_type = layer_info.get( "print_type" );
                  if( print_type == null )
                     print_type = "Print";
                  int print_state = getTritFromString( layer_info.get( "print_state" ), 2 );
                  if( print_state != 2 )
                  {
                     System.out.println( "   setting print_type = " + print_type + 
                                         ", print_state = " + print_state );
                     layer.setPrint( print_type, print_state == 1 );
                  }

                  float min_zoom = getFloatFromString( layer_info.get( "min_zoom" ), -1f );
                  float max_zoom = getFloatFromString( layer_info.get( "max_zoom" ), -1f );
                  if( min_zoom >= 0. || max_zoom >= 0. )
                  {
                     System.out.println( "   setting min_zoom = " + min_zoom + ", max_zoom = " + max_zoom );
                     layer.setZoom( min_zoom, max_zoom );

                     // when we set the zoom we need a secondary layer to control what is happening
                     // 'cause acrobat doesn't know how to do zoom-inferred visibility properly

                     PdfLayer control_layer = new PdfLayer( layer_name, writer );
                     PdfLayerMembership members = new PdfLayerMembership( writer );
                     layer.setOnPanel( false );
                     members.addMember( layer );
                     members.addMember( control_layer );
                     members.setVisibilityPolicy( PdfLayerMembership.ALLON );

                     // it seems we need to begin/end the layer for it to show up on the panel
                     cb.beginLayer( control_layer );
                     cb.endLayer();
                     current_layer = members;
                  }
                  else
                     current_layer = layer;

                  name_map.put( layer_name, current_layer );
               }
                  
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

         document.close();

      }
      catch(DocumentException de) { System.err.println( "Document exception: " + de.getMessage()); }
      catch(IOException ioe) { System.err.println( "IO exception: " + ioe.getMessage()); }
      catch(DataFormatException dfe) { System.err.println( "Data format exception: " + dfe.getMessage()); }

      System.out.println( "wrote output to: " + args[1] );
   }

   // parses tokens using key=value 
   HashMap<String,String> parseLayer( String[] keys, String line )
   {
      String[] tokens = line.split( "\\|" );
      HashMap<String,String> map = new HashMap<String,String>();

      for( int i=0; i<tokens.length; ++i )
         for( int k=0; k<keys.length; ++k )
         {
            String regex = "^" + keys[k] + "[ \t]*=[ \t]*";
            if( tokens[i].matches( regex + ".*" ) )
            {
               String[] values = tokens[i].split( regex );

               // values[0] is empty (part of string before ^)
               map.put( keys[k], values[1] );
            }
         }

      return map;
   }

   // extracting values from strings with defaults
   boolean getBoolFromString( String str, boolean def_value ) throws NumberFormatException
   {
      boolean value = def_value;

      if( str != null )
      {
         int int_value = Integer.parseInt( str );
         if( int_value < 0 || int_value > 1 )
            throw new NumberFormatException( "invalid boolean value: " + str );

         value = int_value == 1;
      }
      
      return value;
   }

   // trit, here is 0,1,2
   int getTritFromString( String str, int def_value ) throws NumberFormatException
   {
      int value = def_value;
      if( str != null )
         value = Integer.parseInt( str );

      if( value < 0 || value > 2 )
         throw new NumberFormatException( "value of (unset) bool not 0, 1 or 2: " + str );

      return value;
   }

   float getFloatFromString( String str, float def_value ) throws NumberFormatException
   {
      float value = def_value;
      if( str != null )
         value = Float.parseFloat( str );
      return value;
   }

}
