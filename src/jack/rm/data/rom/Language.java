package jack.rm.data.rom;

import java.util.*;
import java.util.stream.Collectors;

import jack.rm.gui.Icon;

public enum Language
{
	
	FRENCH("French", "FR", Icon.FLAG_FRANCE),
	ENGLISH("English", "EN", Icon.FLAG_USA),
	CHINESE("Chinese", "ZH", Icon.FLAG_CHINA),
	DANISH("Danish", "DA", Icon.FLAG_DENMARK),
	DUTCH("Dutch", "NL",  Icon.FLAG_NETHERLANDS),
	FINNISH("Finnish", "FI", Icon.FLAG_FINLAND),
	GERMAN("German", "DE", Icon.FLAG_GERMANY),
	ITALIAN("Italian", "IT", Icon.FLAG_ITALY),
	JAPANESE("Japanese", "JA", Icon.FLAG_JAPAN),
	NORWEGIAN("Norwegian", "NO", Icon.FLAG_NORWAY),
	POLISH("Polish", "PL", Icon.FLAG_POLAND),
	PORTUGUESE("Portuguese", "PT", Icon.FLAG_PORTUGAL),
	SPANISH("Spanish", "ES", Icon.FLAG_SPAIN),
	SWEDISH("Swedish", "SV", Icon.FLAG_SWEDEN),
	ENGLISH_UK("English (UK)", "EN", Icon.FLAG_UNITED_KINGDOM),
	PORTUGUESE_BR("Portuguese (BR)", "PT", Icon.FLAG_BRAZIL),
	KOREAN("Korean", "KO", Icon.FLAG_KOREA)
	;
	
	public final String fullName;
	public final String iso639_1;
	public final Icon icon;
	
	
	private static final HashMap<String, Language> mapping = new HashMap<String, Language>();
	
	static
	{
		for (Language l : Language.values())
		{
			mapping.put(l.fullName,l);
		}
	}
	
	Language(String name, String iso639_1, Icon icon)
	{
    this.fullName = name;
    this.iso639_1 = iso639_1;
	  this.icon = icon;
	}
	
	Language(String name, String iso639_1)
	{
		this(name, iso639_1, null);
	}
	
	@Override
  public String toString()
	{
		return fullName;
	}
	
	static Language forName(String language)
	{
		return mapping.get(language);
	}
	
	public static String asString(Set<Language> languages)
	{
	   return languages.stream().map( l -> l.fullName).collect(Collectors.joining(", "));

	}
}