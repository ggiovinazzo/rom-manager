package jack.rm.plugins.renamer;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

import com.github.jakz.romlib.data.game.Game;
import com.github.jakz.romlib.data.game.RomSize;
import com.github.jakz.romlib.data.game.Location;
import com.github.jakz.romlib.data.game.attributes.Attribute;
import com.github.jakz.romlib.data.game.attributes.GameAttribute;
import com.pixbits.lib.plugin.PluginInfo;
import com.pixbits.lib.plugin.PluginVersion;

import jack.rm.files.Pattern;
import jack.rm.plugins.types.PatternSetPlugin;

public class BasicPatternSet extends PatternSetPlugin
{
  public BasicPatternSet()
  {

  }
  
  @Override
  public PluginInfo getInfo()
  { 
    return new PluginInfo("Basic Pattern Set", new PluginVersion(1,0), "Jack",
        "This plugin provides the basic renaming patterns for ROMs.");
  }
  
  private static class AttributePattern extends Pattern 
  {
    final private Attribute attribute;
    final private boolean applyQuotes;
    AttributePattern(String code, String desc, Attribute attrib, boolean applyQuotes)
    {
      super(code, desc);
      this.attribute = attrib;
      this.applyQuotes = applyQuotes;
    }
    
    AttributePattern(String code, String desc, Attribute attrib)
    {
      this(code, desc, attrib, true);
    }
       
    @Override public String apply(Pattern.RenamingOptions options, String name, Game game)
    { 
      Object value = game.getAttribute(attribute);
      String svalue = value == null ? "" : value.toString();
      return applyQuotes ? apply(options, name, code, game.getAttribute(attribute)) : name.replace(code, svalue);
    }
  }
  
  private static class OrdinalPattern extends Pattern {
    private final DecimalFormat format = new DecimalFormat();
    
    OrdinalPattern() { 
      super("%o", "Ordinal number of the game");
      //TODO: should it be configurable?
      format.applyPattern("0000");
    }
    @Override
    public String apply(Pattern.RenamingOptions options, String name, Game game) { 
      int ordinal = game.getAttribute(GameAttribute.ORDINAL);
      return name.replaceAll(code, format.format(ordinal));
    }
  }
  
  private static class MegabyteSizePattern extends Pattern {
    MegabyteSizePattern() { super("%s", "Size of the game dump in bytes (long)"); }
    @Override
    public String apply(Pattern.RenamingOptions options, String name, Game game)
    { 
      return apply(options, name, code, RomSize.toString(game.getSizeInBytes(), RomSize.PrintStyle.LONG, RomSize.PrintUnit.BYTES));
    }
  }
  
  private static class MegabitSizePattern extends Pattern {
    MegabitSizePattern() { super("%S", "Size of the game dump in bits (short)"); }
    @Override
    public String apply(Pattern.RenamingOptions options, String name, Game game)
    { 
      return apply(options, name, code, RomSize.toString(game.getSizeInBytes(), RomSize.PrintStyle.SHORT, RomSize.PrintUnit.BITS));
    }
  }
  
  private static class FullLocationPattern extends Pattern {
    FullLocationPattern() { super("%L", "Full location name"); }
    @Override
    public String apply(Pattern.RenamingOptions options, String name, Game rom) { 
      Location location = rom.getLocation().getMostCompatibleLocation();
      return apply(options, name, code, location.fullName);
      
    }
  }
  
  private static class ShortLocationPattern extends Pattern {
    ShortLocationPattern() { super("%a", "Short location name"); }
    @Override
    public String apply(Pattern.RenamingOptions options, String name, Game rom) { 
      Location location = rom.getLocation().getMostCompatibleLocation();
      return apply(options, name, code, location.shortName);
    }
  }
  
  private static class TinyLocationPattern extends Pattern {
    TinyLocationPattern() { super("%l", "Tiny location name"); }
    @Override
    public String apply(Pattern.RenamingOptions options, String name, Game rom) { 
      Location location = rom.getLocation().getMostCompatibleLocation();
      return apply(options, name, code, location.tinyName);
    }
  }
  
  private static class ShortLanguagePattern extends Pattern {
    ShortLanguagePattern() { super("%i", "Short language"); }
    @Override
    public String apply(Pattern.RenamingOptions options, String name, Game rom) {
      long langCount = rom.getLanguages().size();

      if (langCount == 1)
        return name.replace(code,rom.getLanguages().iterator().next().iso639_1);
      else 
        return name.replace(code,"M"+langCount);
    }
  }
  
  private final Pattern[] patterns = {
    new OrdinalPattern(),
    new FullLocationPattern(),
    new AttributePattern("%g", "Releaser group", GameAttribute.GROUP),
    new MegabitSizePattern(),
    new MegabyteSizePattern(),
    new AttributePattern("%c", "Publisher", GameAttribute.PUBLISHER),
    new ShortLanguagePattern(),
    new ShortLocationPattern(),
    new TinyLocationPattern(),
    new AttributePattern("%t", "Game title", GameAttribute.TITLE, false),
    new AttributePattern("%T", "Normalized game title", GameAttribute.NORMALIZED_TITLE, false),
    new AttributePattern("%C", "Comment", GameAttribute.COMMENT, true)
  };
  
  @Override
  public List<Pattern> getPatterns()
  {
    return Arrays.asList(patterns);
  }
}
