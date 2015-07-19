package jack.rm.data.set;

import jack.rm.data.NumberedRom;
import jack.rm.data.Rom;
import jack.rm.data.RomSave;
import jack.rm.data.RomSize;
import jack.rm.data.console.System;

import java.awt.Dimension;
import java.net.URL;
import java.net.MalformedURLException;

public class GBA extends RomSetOfflineList implements NumberedSet<NumberedRom>
{

  
  public GBA() throws MalformedURLException
	{
		super(System.GBA, ProviderID.OFFLINELIST, new Dimension(480,320), new Dimension(480,320), new URL("http://offlinelistgba.free.fr/imgs/"));
	}

	@Override
  public String downloadURL(Rom rom)
	{
		String query1 = "http://www.emuparadise.me/roms/search.php?query=";
		String query2 = "&section=roms&sysid=31";
		
		String name = rom.title.replaceAll("\\W", " ").toLowerCase();
		name = name.replace(" ","%20");
		// Renamer.formatNumber(rom.imageNumber)
	
		return query1+name+query2;
	}
}