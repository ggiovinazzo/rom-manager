package jack.rm.net;

import jack.rm.Main;
import jack.rm.data.*;
import jack.rm.data.set.RomSet;
import jack.rm.gui.ProgressDialog;
import jack.rm.log.Log;
import jack.rm.log.LogSource;
import jack.rm.log.LogTarget;
import jack.rm.log.LogType;

import java.util.concurrent.*;
import java.nio.channels.*;
import java.nio.file.*;
import java.net.*;
import java.io.*;

public class Downloader
{
  public ThreadPoolExecutor pool;
  int totalTasks;
  int missingTasks;
  boolean started;
  
  private final RomSet<? extends Rom> set;

  public Downloader(RomSet<? extends Rom> set)
  {
    this.set = set;
  }
  
  public void start()
  {
    if (started)
      return;
    
    pool = (ThreadPoolExecutor)Executors.newFixedThreadPool(10);
    started = true;
    
    Asset[] assets = RomSet.current.getSupportedAssets();
    
    for (int i = 0; i < set.list.count(); ++i)
    {
      Rom r = set.list.get(i);
      
      for (Asset asset : assets)
        if (!r.hasAsset(asset))
          pool.submit(new ArtDownloaderTask(r, asset));
    }
        
    ProgressDialog.init(Main.mainFrame, "Asset Download", () -> { pool.shutdownNow(); started = false; });
  }
  
  public void downloadArt(final Rom r)
  {
    new Thread()
    {
      @Override
      public void run()
      {
        Asset[] assets = RomSet.current.getSupportedAssets();

        for (Asset asset : assets)
          if (!r.hasAsset(asset))
            new ArtDownloaderTask(r, asset).call();
        
        Main.infoPanel.updateFields(r);
      }
    }.run();
  }
  
  public class ArtDownloaderTask implements Callable<Boolean>
  {
    URL url;
    Path path;
    Asset asset;
    Rom rom;
    
    public ArtDownloaderTask(Rom rom, Asset asset)
    {  
      path = RomSet.current.assetPath(asset, rom);
      url = RomSet.current.assetURL(asset, rom);
      
      System.out.println(url+" -> "+path.toAbsolutePath());
          
      this.rom = rom;
      this.asset = asset;
    }
    
    @Override
    public Boolean call()
    {
      try
      {
        ReadableByteChannel rbc = Channels.newChannel(url.openStream());
        FileChannel channel = FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        channel.transferFrom(rbc, 0, 1 << 24);
        channel.close();
      }
      catch (FileNotFoundException e)
      {
        Log.log(LogType.ERROR, LogSource.DOWNLOADER, LogTarget.rom(rom), "Asset not found at "+url);
        Main.infoPanel.updateFields(rom);
        return false;
      }
      catch (java.nio.channels.ClosedByInterruptException e)
      {
        try
        {
          if (Files.exists(path))
            Files.delete(path);
        }
        catch (IOException ee)
        {
          ee.printStackTrace();
        }
      }
      catch (Exception e)
      {
        e.printStackTrace();

        return false;
      }
      
      //Main.logln("Downloaded art for "+Renamer.formatNumber(rom.number)+" ("+type+").");
      
      if (pool != null)
      {
        long completed = pool.getCompletedTaskCount();
        long total = pool.getTaskCount(); 
      
        ProgressDialog.update(completed/(float)total, completed+" of "+total);
      }

      return true;
    }
  }
}