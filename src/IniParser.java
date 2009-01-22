package layers3pdf;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

public class IniParser
{
  BufferedReader ini;
  String filename;

  public IniParser( String filename ) throws IOException
  {
    this.filename = filename;
    restart();
  }

  public void restart( ) throws IOException
  {
    ini = new BufferedReader( new FileReader( filename ) );
  }

  public HashMap<String,String> parseLine( String[] keys ) throws DocumentException, IOException, DataFormatException
  {
    HashMap<String,String> info = null;
    LinkedList<String> hd_tokens = new LinkedList<String>();
    LinkedList<String> hd_keys = new LinkedList<String>();
    boolean here_doc_mode = false;
    String current_hd_value = "";

    while( ini.ready() )
    {
      if( !here_doc_mode )
      {
        String line = ini.readLine();

        if( line.matches( "^[ \t]*(#.*)?" ) ) // comments allowed (lines starting with #)
          continue;

        String[] tokens = line.split( "\\|" );
        info = new HashMap<String,String>();

        for( String tok : tokens )
          for( String key : keys )
          {
            String regex = "^" + key + "[ \t]*=[ \t]*";
            if( tok.matches( regex + ".*" ) )
            {
              String[] values = tok.split( regex );

              // values[0] is empty (part of string before ^)
              if( values[1].matches( "<<.*" ) )
              {
                hd_tokens.add( values[1].substring( 2 ) );
                hd_keys.add( key );
              }
              else
                info.put( key, values[1] );
            }
          }

        here_doc_mode = true; // now process all here_doc strings
      }
      else if( !hd_tokens.isEmpty() )
      {
        String line = ini.readLine();
        if( line.equals( hd_tokens.element() ) ) // end of this here doc
        {
          hd_tokens.remove();
          String key = hd_keys.remove();
          info.put( key, current_hd_value );
          current_hd_value = "";

          continue;
        }

        if( current_hd_value.equals( "" ) )
          current_hd_value = line;
        else
          current_hd_value += "\n" + line;
      }
      else
        break;
    }
    if( !hd_tokens.isEmpty() )
      throw new DataFormatException( "Unterminated string with EOF token: " + hd_tokens.element() );

    return info;
  }
}