package layers3pdf;

import java.io.*;
import java.util.*;

public class StringUtil
{
  // extracting values from strings with defaults
  public static boolean getBoolFromString( String str, boolean def_value ) throws NumberFormatException
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
  public static int getTritFromString( String str, int def_value ) throws NumberFormatException
  {
    int value = def_value;
    if( str != null )
      value = Integer.parseInt( str );

    if( value < 0 || value > 2 )
      throw new NumberFormatException( "value of (unset) bool not 0, 1 or 2: " + str );

    return value;
  }

  public static int getIntFromString( String str, int def_value ) throws NumberFormatException
  {
    int value = def_value;

    if( str != null )
      value = Integer.parseInt( str );
    
    return value;
  }

  public static float getFloatFromString( String str, float def_value ) throws NumberFormatException
  {
    float value = def_value;
    if( str != null )
      value = Float.parseFloat( str );
    return value;
  }

  public static double getDoubleFromString( String str, double def_value ) throws NumberFormatException
  {
    double value = def_value;
    if( str != null )
      value = Double.parseDouble( str );
    return value;
  }
  
  public static String formatString( String value )
  {
    String rv = "";
    if( value.contains( "\n" ) )
      {
        for( String l : value.split( "\n" ) )
          rv += "\n---> " + l;
      }
      else
        rv = value;

    return rv;
  }
}