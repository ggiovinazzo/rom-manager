package jack.rm.data.romset;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.jakz.romlib.data.game.Game;
import com.github.jakz.romlib.data.game.GameSize;
import com.github.jakz.romlib.data.game.attributes.Attribute;
import com.github.jakz.romlib.data.game.attributes.GameAttribute;
import com.github.jakz.romlib.data.platforms.Platform;
import com.github.jakz.romlib.data.set.DatFormat;
import com.github.jakz.romlib.data.set.Provider;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.pixbits.lib.searcher.DummySearcher;
import com.pixbits.lib.searcher.SearchParser;
import com.pixbits.lib.searcher.SearchPredicate;
import com.pixbits.lib.searcher.Searcher;
import com.pixbits.lib.log.Log;

import jack.rm.GlobalSettings;
import jack.rm.Main;
import jack.rm.Settings;
import jack.rm.assets.Asset;
import jack.rm.assets.AssetManager;
import jack.rm.files.Scanner;
import jack.rm.files.parser.DatLoader;
import jack.rm.json.Json;
import jack.rm.json.GameListAdapter;
import jack.rm.log.LogSource;
import jack.rm.log.LogTarget;
import jack.rm.plugins.PluginRealType;
import jack.rm.plugins.cleanup.CleanupPlugin;
import jack.rm.plugins.searcher.SearchPlugin;
import jack.rm.plugins.searcher.SearchPredicatesPlugin;

public class GameSet
{
  public static GameSet current = null;
	
  private boolean loaded;

	public final GameList list;
	public final Platform platform;
	public final Provider provider;
	public final DatFormat datFormat;
	public final GameSize.Set sizeSet;
	
	private Settings settings;
	private final AssetManager assetManager;
	private final DatLoader loader;
	
	private Searcher<Game> searcher;
	private Scanner scanner;

	private final Attribute[] attributes;

	public GameSet(Platform type, Provider provider, Attribute[] attributes, AssetManager assetManager, DatLoader loader)
	{
		this.searcher = new DummySearcher<>();
	  this.list = new GameList(this);
	  this.sizeSet = new GameSize.Set();
	  this.platform = type;
		this.provider = provider;
		this.datFormat = loader.getFormat();
		this.attributes = attributes;
		this.assetManager = assetManager;
		this.loader = loader;
		this.loaded = false;
	}
	
	public void pluginStateChanged()
	{
	  if (getSettings().getSearchPlugin() != null)
	  {
	    List<SearchPredicate<Game>> predicates = new ArrayList<>();
	    
	    SearchPlugin plugin = getSettings().plugins.getEnabledPlugin(PluginRealType.SEARCH);
	    SearchParser<Game> parser = plugin.getSearcher();
	    
	    Set<SearchPredicatesPlugin> predicatePlugins = getSettings().plugins.getEnabledPlugins(PluginRealType.SEARCH_PREDICATES);
	    predicatePlugins.stream().flatMap(p -> p.getPredicates().stream()).forEach(predicates::add);    
	    searcher = new Searcher<>(parser, predicates);
	  }
	  else
	    searcher = new DummySearcher<>();
	  
	  scanner = new Scanner(this);
	}
	
	public Settings getSettings() { return settings; }
	
	public final AssetManager getAssetManager() { return assetManager; }
	
	public boolean doesSupportAttribute(Attribute attribute) { return Arrays.stream(attributes).anyMatch( a -> a == GameAttribute.NUMBER); }
	public final Attribute[] getSupportedAttributes() { return attributes; }
	
	public Scanner getScanner() { return scanner; }
			
	public boolean canBeLoaded()
	{
	  return Files.exists(datPath());
	}
	
	public final void load()
	{ 
	  if (!loaded)
	    loader.load(this); 
	  loaded = true;
	}
	
	@Override
  public String toString()
	{
		return platform.name+" ("+provider.getName()+")";
	}
	
	public String ident()
	{
		return datFormat.getIdent()+"-"+platform.tag+"-"+provider.getTag()+provider.builtSuffix();
	}
	
	public Path datPath()
	{
		return Paths.get("dat/"+ident()+"."+datFormat.getExtension());
	}
	
	public Path getAttachmentPath()
	{
	  return settings.romsPath.resolve(Paths.get("attachments"));
	}
	
	public Searcher<Game> getSearcher()
	{
	  return searcher;
	}
	
  public final Path getAssetPath(Asset asset, boolean asArchive)
  {
    Path base = Paths.get("data/", ident(), "assets").resolve(asset.getPath());
    
    if (!asArchive)
      return base;
    else
      return Paths.get(base.toString()+".zip");
  }
	
	public final void cleanup()
	{
	  Set<CleanupPlugin> plugins = settings.plugins.getEnabledPlugins(PluginRealType.ROMSET_CLEANUP);
	  plugins.stream().forEach( p -> p.execute(this.list) );
	}
	
	public Game find(String query) { return list.find(query); }
	public List<Game> filter(String query) { return list.stream().filter(searcher.search(query)).collect(Collectors.toList()); }
	
	public void saveStatus()
	{
	  try
	  {
  	  Path basePath = GlobalSettings.DATA_PATH.resolve(ident());
  	  
  	  Files.createDirectories(basePath);
  	  
  	  Path settingsPath = basePath.resolve("settings.json");
  	  
  	  try (BufferedWriter wrt = Files.newBufferedWriter(settingsPath))
  	  {
  	    wrt.write(Json.build().toJson(settings, Settings.class));
  	  }
  	  
  	  Path statusPath = basePath.resolve("status.json");
  	  
      Gson gson = Json.prebuild().registerTypeAdapter(GameList.class, new GameListAdapter(list)).create();
      
      try (BufferedWriter wrt = Files.newBufferedWriter(statusPath))
      {
        wrt.write(gson.toJson(list));
        Log.getLogger(LogSource.STATUS).i(LogTarget.romset(this), "Romset status saved on json");
      }
	  }
	  catch (Exception e)
	  {
	    e.printStackTrace();
	  }
	}
		
	public boolean loadStatus()
	{
	  try
	  {
  	  Path basePath = Paths.get("data/", ident());
  	    
  	  Path settingsPath = basePath.resolve("settings.json");
  	  
  	  try
  	  {
  	    AssetManager assetManager = getAssetManager();
  	    for (Asset asset : assetManager.getSupportedAssets())
  	      Files.createDirectories(getAssetPath(asset,false));
  	  }
  	  catch (IOException e)
  	  {
  	    e.printStackTrace();
  	    // TODO: log
  	  }
  	  
  	  if (!Files.exists(settingsPath))
  	  {
  	    settings = new Settings(Main.manager, Arrays.asList(getSupportedAttributes()));
  	    return false;
  	  }
  	  else
  	  {
  	    try (BufferedReader rdr = Files.newBufferedReader(settingsPath))
  	    {
  	      settings = Json.build().fromJson(rdr, Settings.class);
  	    }
  	    catch (JsonParseException e)
  	    {
  	      if (e.getCause() instanceof ClassNotFoundException)
  	        Log.getLogger(LogSource.STATUS).e("Error while loading plugin state: %s", e.getCause().toString());
  	      
  	      e.printStackTrace();
  	    }
  	    
  	    Path statusPath = basePath.resolve("status.json");
  	    
  	    Gson gson = Json.prebuild().registerTypeAdapter(GameList.class, new GameListAdapter(list)).create();
  	    
  	    try (BufferedReader rdr = Files.newBufferedReader(statusPath))
  	    {
  	      gson.fromJson(rdr, GameList.class);
  	      return true;
  	    }
  	    catch (NoSuchFileException e)
  	    {
  	      return false;
  	    }
  	  }
	  }
	  catch (FileNotFoundException e)
	  {
	    return false;
	  }
	  catch (IOException e)
	  {
	    e.printStackTrace();
	    return false;
	  }
	}
}