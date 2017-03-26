package com.github.jakz.romlib.data.game;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import com.github.jakz.romlib.data.attachments.Attachments;
import com.github.jakz.romlib.data.game.attributes.Attribute;
import com.github.jakz.romlib.data.game.attributes.GameAttribute;
import com.github.jakz.romlib.data.game.attributes.GameAttributeInterface;
import com.github.jakz.romlib.data.game.attributes.GameInfo;
import com.github.jakz.romlib.data.platforms.Platform;
import com.github.jakz.romlib.data.set.GameSet;

import jack.rm.assets.Asset;
import jack.rm.assets.AssetData;
import jack.rm.plugins.folder.FolderPlugin;
import jack.rm.plugins.renamer.RenamerPlugin;

public class Game implements Comparable<Game>, GameAttributeInterface
{
	private final GameSet set;

	private Rom[] roms;
	private final GameInfo info;
	private GameClone clone;
	
  private boolean favourite;
  private GameStatus status;

	private Map<Asset, AssetData> assetData = new HashMap<>();
	private final Attachments attachments = new Attachments();
	
	public void setAttribute(Attribute key, Object value) { info.setAttribute(key, value); }
	public void setCustomAttribute(Attribute key, Object value) { info.setCustomAttribute(key, value); }
	
	public <T> T getAttribute(Attribute key) { return info.getAttribute(key); }
	public boolean hasAttribute(Attribute key) { return info.hasAttribute(key); }
	
  public Stream<Map.Entry<Attribute, Object>> getCustomAttributes() { return info.getCustomAttributes(); }
  public boolean hasCustomAttribute(Attribute attrib) { return info.hasCustomAttribute(attrib); }
  public void clearCustomAttribute(Attribute attrib) { info.clearCustomAttribute(attrib); }
  
  public Attachments getAttachments() { return attachments; }

	public Game(GameSet set)
	{
    this.set = set;
    this.info = new GameInfo();
	  status = GameStatus.MISSING;
	}
	
	public Game(GameSet set, Rom... roms)
	{
	  this(set);
	  this.roms = roms;
	}
	
	public void setRom(Rom... roms)
	{
	  this.roms = roms;
	  Arrays.stream(roms).forEach(r -> r.setGame(this));
	}
	
	public GameSet getGameSet() { return set; }
	
	public GameClone getClone() { return clone; }
	public void setClone(GameClone clone) { this.clone = clone; }

	public long getSizeInBytes()
	{
	  return Arrays.stream(roms).map(Rom::size).mapToLong(i -> i).sum();
	}

	public Stream<Rom> stream() { return Arrays.stream(roms); }
	public Rom rom()
	{ 
	  if (roms.length > 1)
	    throw new UnsupportedOperationException("Can't invoke Game::rom to obtain the single rom on a game with multiple roms");
	  return roms[0];
	}
	
	public boolean shouldSerializeState()
	{
	  return isFavourite() || status != GameStatus.MISSING || info.hasAnyCustomAttribute();
	}
	
	public GameID<?> getID()
	{ 
	  // TODO: find a generic way
	  if (!set.hasMultipleRomsPerGame())
	    return new GameID.CRC(roms[0].crc());
	  else
	    throw new UnsupportedOperationException("no GameID generator for sets with multiple roms per game");
	}
		
	public void setStatus(GameStatus status) { this.status = status; } // TODO: should be visible?
	public GameStatus getStatus() { return status; }
	public Platform getSystem() { return set.platform; }
		
	public AssetData getAssetData(Asset asset)
	{
	  return assetData.computeIfAbsent(asset, k -> new AssetData(k, this));
	}
	
	@Override
  public String toString()
	{
		return getCorrectName();
	}
	
	public void move(Path dest) throws IOException
	{
	  throw new UnsupportedOperationException("Move is not implemented anymore");
	  //Files.move(path.file(), dest);
	  //path = path.relocate(dest);
	}
	
	public boolean hasAsset(Asset asset)
	{
	  return getAssetData(asset).isPresent();
	}
	
	public boolean hasAllAssets()
	{
	  for (Asset asset : set.getAssetManager().getSupportedAssets())
	    if (!hasAsset(asset))
	      return false;
	  
	  return true;
	}
	
	public void updateStatus()
	{ 
	  AtomicBoolean anyFound = new AtomicBoolean(false);
	  AtomicBoolean allFound = new AtomicBoolean(true);

	  stream().forEach(rom -> {
	    anyFound.set(anyFound.get() || rom.isPresent());
	    allFound.set(allFound.get() && rom.isPresent());
	  });
	  
	  if (!anyFound.get())
	    status = GameStatus.MISSING;
	  else if (!allFound.get())
	    status = GameStatus.INCOMPLETE;
	  else if (!isOrganized())
	    status = GameStatus.UNORGANIZED;
	  else
	    status = GameStatus.FOUND;
	}

	public boolean isOrganized()
	{
	  boolean name = hasCorrectName(), folder = hasCorrectFolder();
	  return name && folder;
	}
  
  public String getCorrectName()
  {
    RenamerPlugin renamer = set.getSettings().getRenamer();
    return renamer.getCorrectName(this);
  }
  
  public String getCorrectInternalName()
  {
    RenamerPlugin renamer = set.getSettings().getRenamer();
    return renamer.getCorrectInternalName(this);
  }
  
  public Path getCorrectFolder()
  {
    FolderPlugin mover = set.getSettings().getFolderOrganizer();
    return mover.getFolderForRom(this);
  }
  
  public boolean hasCorrectInternalName()
  {
    return false; // TODO: !handle.isArchive() || getCorrectInternalName().equals(handle.plainInternalName());
  }
  
  public boolean hasCorrectName()
  {
    return false; /* TODO
    
    Settings settings = set.getSettings();
    
    boolean hasCorrectName = getCorrectName().equals(handle.plainName());
    
    if (!settings.shouldRenameInternalName)
      return hasCorrectName;
    else
      return hasCorrectName && hasCorrectInternalName();
      */
  }
  
  public boolean hasCorrectFolder()
  {
    return false;
    /* TODO
    
    try {
      return set.getSettings().getFolderOrganizer() == null || 
        Files.isSameFile(handle.path().getParent(), set.getSettings().romsPath.resolve(getCorrectFolder()));
    }
    catch (IOException e)
    {
      e.printStackTrace();
      return false;
    }*/
  }

	@Override
	public boolean equals(Object other)
	{ 
	  if (set.doesSupportAttribute(GameAttribute.NUMBER) && other instanceof Game)
	  {
	    int n1 = getAttribute(GameAttribute.NUMBER);
	    int n2 = ((Game)other).getAttribute(GameAttribute.NUMBER);
	    return n1 == n2;
	  }
	  else if (other instanceof Game)
	  {
	    return getTitle().equals(((Game)other).getTitle());
	  }
	  
	  return false;
	}
	
	@Override
  public int compareTo(Game rom)
	{
		if (set.doesSupportAttribute(GameAttribute.NUMBER))
		{
      int n1 = getAttribute(GameAttribute.NUMBER);
      int n2 = rom.getAttribute(GameAttribute.NUMBER);
      
      return Integer.compare(n1, n2);
		}
	  
	  return getTitle().compareTo(rom.getTitle());
	}
		
	public boolean isFavourite() { return favourite; }
	public void setFavourite(boolean value) { favourite = value; }
}
