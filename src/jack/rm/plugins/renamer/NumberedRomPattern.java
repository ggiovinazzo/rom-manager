package jack.rm.plugins.renamer;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import com.github.jakz.romlib.data.game.Game;
import com.github.jakz.romlib.data.game.attributes.GameAttribute;
import com.github.jakz.romlib.data.set.GameSet;
import com.pixbits.lib.plugin.PluginInfo;
import com.pixbits.lib.plugin.PluginVersion;

import jack.rm.files.Pattern;
import jack.rm.plugins.types.PatternSetPlugin;

public class NumberedRomPattern extends PatternSetPlugin
{
  private final DecimalFormat format;

  public NumberedRomPattern()
  {
    format = new DecimalFormat();
    format.applyPattern("0000");
  }
  
  @Override
  public PluginInfo getInfo()
  { 
    return new PluginInfo("Numeric Pattern Set", new PluginVersion(1,0), "Jack",
        "This plugin provides a naming pattern for ROMs that have a number.");
  }

  private class NumberPattern extends Pattern<Game>
  {
    NumberPattern()
    { 
      super("%n", "Release number in format 1234");
    }
    
    @Override
    public String apply(Pattern.RenamingOptions options, String name, Game rom)
    { 
      return name.replaceAll(code, format.format((int)rom.getAttribute(GameAttribute.NUMBER)));
    }
  }
  
  private final Pattern[] patterns = { new NumberPattern() };
  
  @Override
  public List<Pattern<Game>> getPatterns()
  {
    return Arrays.asList(patterns);
  }
  
  @Override
  public Predicate<GameSet> compatibility() { return rs -> rs.doesSupportAttribute(GameAttribute.NUMBER); }
}
