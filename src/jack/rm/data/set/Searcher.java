package jack.rm.data.set;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.pixbits.parser.SimpleParser;

import jack.rm.data.Genre;
import jack.rm.data.Rom;
import jack.rm.data.RomSave;
import jack.rm.data.Location;
import jack.rm.data.rom.RomAttribute;

public class Searcher
{
  static final SimpleParser parser;
  
  static
  {
    parser = new SimpleParser();
    parser.addWhiteSpace(' ');
    parser.addQuote('\"');
  }
  
  private static boolean isSearchArg(String[] tokens, String... vals)
  {
    boolean firstMatch = tokens[0].equals(vals[0]);  
    return firstMatch && Arrays.stream(vals, 1, vals.length).anyMatch( v -> v.equals(tokens[1]));
  }

  
  private static Predicate<Rom> buildPredicate(String token)
  {
    if (token.contains(":"))
    {
      String[] tokens = token.split(":");
      if (tokens.length == 2)
      {
        if (tokens[1].startsWith("\""))
          tokens[1] = tokens[1].substring(1);
        if (tokens[1].endsWith("\""))
          tokens[1] = tokens[1].substring(0, tokens[1].length()-1);
        
        if (isSearchArg(tokens, "is", "favorite", "favourite", "fav"))
          return r -> r.isFavourite();
        else if (tokens[0].equals("genre"))
        {
          return r -> {
            Genre genre = r.getAttribute(RomAttribute.GENRE);
            Genre sgenre = Genre.forName(tokens[1]);
            
            return genre != null && sgenre != null && genre == sgenre;
          };
        }
        else if (tokens[0].equals("save"))
        {
          String[] itokens = tokens[1].split(" ");
          
          return r -> {
            RomSave<? >save = r.getAttribute(RomAttribute.SAVE_TYPE);            
            return save != null && Arrays.stream(itokens).allMatch(s -> save.toString().toLowerCase().contains(s.toLowerCase()));   
          };
        }
        else if (tokens[0].equals("loc") || tokens[0].equals("location"))
        {
          return r -> {
            Location location = r.getAttribute(RomAttribute.LOCATION);
            return location.fullName.toLowerCase().equals(tokens[1]);
          };
        }
      }    
    }
    else
    {
      return r -> r.getTitle().toLowerCase().contains(token);
    }
    
    return null;
  }
  
  public static Predicate<Rom> buildSeachPredicate(String text)
  {
    Predicate<Rom> predicate = r -> true;
    
    List<String> tokens = new ArrayList<>();
    Consumer<String> callback = s -> tokens.add(s); 
  
    parser.setCallback(callback);
    parser.reset(new java.io.ByteArrayInputStream(text.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    
    try {
      parser.parse();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    
    for (String token : tokens)
    {
      boolean negated = false;
      
      if (token.startsWith("!"))
      {
        negated = true;
        token = token.substring(1);
      }
      
      Predicate<Rom> cpredicate = buildPredicate(token);
      
      if (cpredicate != null)
      {
        if (negated)
          predicate = predicate.and(cpredicate.negate());
        else
          predicate = predicate.and(cpredicate);
      }
    }
    
    return predicate;
  }
}
