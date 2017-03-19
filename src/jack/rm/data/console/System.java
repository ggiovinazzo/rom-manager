package jack.rm.data.console;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.ImageIcon;

import com.github.jakz.romlib.ui.Icon;

public enum System
{
	NES("nes", "NES", new String[] {"nes", "rom", "unf"}, Icon.SYSTEM_NES, true),
	SNES("snes", "Super Nintendo", new String[] {"smc", "sfc", "fig"}, Icon.SYSTEM_SUPER_NINTENDO, true),
	N64("n64", "Nintendo 64", new String[] {"n64", "v64", "u64", "z64"}, Icon.SYSTEM_NINTENDO_64, true),
	GB("gb", "GameBoy", new String[] {"gb"}, Icon.SYSTEM_GAMEBOY, true),
	GBC("gbc", "GameBoy Color", new String[] {"gbc"}, Icon.SYSTEM_GAMEBOY_COLOR, true),
	GBA("gba", "GameBoy Advance",new String[] {"gba", "agb", "bin"}, Icon.SYSTEM_GAMEBOY_ADVANCE, true),
	NDS("nds", "Nintendo DS", new String[] {"nds", "dsi"}, Icon.SYSTEM_NINTENDO_DS, true),
  _3DS("3ds", "Nintendo 3DS", new String[] {"3ds"}, true),

  LYNX("lynx", "Atary Lynx", new String[] { "lnx" }, true),
  
	WS("ws", "WonderSwan", new String[] {"ws"}, Icon.SYSTEM_WONDERSWAN, true),
	GG("gg", "Game Gear", new String[] {"gg"}, Icon.SYSTEM_GAME_GEAR, true),
	C64("c64", "Commodore 64", null, Icon.SYSTEM_COMMODORE_64, false),
	
	NGP("ngp", "Neo-Geo Pocket", new String[] {"ngp"}, Icon.SYSTEM_NEO_GEO_POCKET, true)
	;
	
	public final String tag;
	public final String name;
	public final boolean acceptsArchives;
	public final String[] exts;
	private final Icon icon;
	
	private System(String tag, String name, String[] exts, Icon icon, boolean acceptsArchives)
	{
		this.tag = tag;
		this.name = name;
		this.exts = exts;
		this.acceptsArchives = acceptsArchives;
		this.icon = icon;
	}
	
	private System(String tag, String name, String[] exts, boolean acceptsArchives)
	{
	  this(tag, name, exts, null, acceptsArchives);
	}
	
	public ImageIcon getIcon()
	{
	  return icon != null ? icon.getIcon() : null;
	}
	
	public static List<System> sortedValues()
	{
	  return Arrays.asList(values()).stream().sorted((s1, s2) -> s1.name.compareTo(s2.name)).collect(Collectors.toList());
	}
}
