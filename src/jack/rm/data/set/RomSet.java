package jack.rm.data.set;

import jack.rm.Settings;
import jack.rm.data.*;
import jack.rm.data.console.System;
import jack.rm.net.AssetDownloader;
import jack.rm.plugins.PluginRealType;
import jack.rm.plugins.cleanup.CleanupPlugin;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.net.URL;
import java.util.*;
import java.util.stream.*;

import java.awt.Dimension;

public abstract class RomSet<R extends Rom>
{
  public static RomSet<? extends Rom> current = null;
	
	
	public final RomList list;
	public final System type;
	public final ProviderID provider;
	
	public final Dimension screenTitle;
	public final Dimension screenGame;

	RomSet(System type, ProviderID provider, Dimension screenTitle, Dimension screenGame)
	{
		this.list = new RomList(this);
	  this.type = type;
		this.provider = provider;
		this.screenTitle = screenTitle;
		this.screenGame = screenGame;
		
		Settings.get(this);
	}
	
	public Settings getSettings() { return Settings.get(this); }
	
	public abstract AssetDownloader getAssetDownloader();
	public abstract Path assetPath(Asset asset, Rom rom);
	
	public abstract String downloadURL(Rom rom);
	
	public abstract void load();
	
	public abstract Asset[] getSupportedAssets();

	@Override
  public String toString()
	{
		return type.name+" ("+provider.name+")";
	}
	
	public String ident()
	{
		return provider.tag+"-"+type.tag;
	}
	
	public String datPath()
	{
		return "dat/"+ident()+".xml";
	}

	public boolean hasGameArt()
	{
		return screenGame != null;
	}
	
	public boolean hasTitleArt()
	{
		return screenTitle != null;
	}
	
	public Path romPath()
	{
		return Settings.get(this).romsPath;
	}
	
	public PathMatcher getFileMatcher()
	{
	  Stream<String> stream = Arrays.stream(type.exts);
	  
	  if (type.acceptsArchives)
	    stream = Stream.concat(stream, Arrays.stream(new String[]{"zip"}));
	  
	  String pattern = stream.collect(Collectors.joining(",", "glob:*.{", "}"));
	  	  
	  return FileSystems.getDefault().getPathMatcher(pattern);
	}
	
	public final void cleanup()
	{
	  Settings settings = Settings.current();
	  
	  Set<CleanupPlugin> plugins = settings.plugins.getEnabledPlugins(PluginRealType.ROMSET_CLEANUP);
	  plugins.stream().forEach( p -> p.execute(this.list) );
	}
}
