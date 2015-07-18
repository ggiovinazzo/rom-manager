package jack.rm.plugins;

import java.util.function.Predicate;

import com.pixbits.plugin.Plugin;
import com.pixbits.plugin.PluginManager;

import jack.rm.data.set.RomSet;
import jack.rm.log.Log;
import jack.rm.log.LogSource;
import jack.rm.log.LogTarget;

public abstract class ActualPlugin extends Plugin
{
  public static final PluginManager<ActualPlugin, ActualPluginBuilder> manager = new PluginManager<>(ActualPluginBuilder.class);
  
  static
  {
    manager.register(jack.rm.plugins.renamer.BasicRenamerPlugin.class);
    
    manager.register(jack.rm.plugins.folder.NumericalOrganizer.class);
    manager.register(jack.rm.plugins.folder.AlphabeticalOrganizer.class);
    manager.register(jack.rm.plugins.cleanup.DeleteEmptyFoldersPlugin.class);
    manager.register(jack.rm.plugins.cleanup.MoveUnknownFilesPlugin.class);
    manager.register(jack.rm.plugins.renamer.BasicPatternSet.class);
    manager.register(jack.rm.plugins.renamer.NumberedRomPattern.class);
    //manager.register(jack.rm.plugins.renamer.PatternRenamerPlugin.class);
  }
  
  protected Predicate<RomSet<?>> compatibility() { return rs -> true; }
  protected boolean isHidden() { return false; }
    
  protected void message(String message) { Log.message(LogSource.PLUGINS, LogTarget.plugin(this), message); }
  protected void warning(String message) { Log.warning(LogSource.PLUGINS, LogTarget.plugin(this), message); }
  protected void error(String message) { Log.error(LogSource.PLUGINS, LogTarget.plugin(this), message); }

}
