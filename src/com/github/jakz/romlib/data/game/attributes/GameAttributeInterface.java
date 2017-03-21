package com.github.jakz.romlib.data.game.attributes;

import com.github.jakz.romlib.data.game.GameSize;
import com.github.jakz.romlib.data.game.LanguageSet;
import com.github.jakz.romlib.data.game.LocationSet;
import com.github.jakz.romlib.data.game.Version;

public interface GameAttributeInterface
{
  void setAttribute(Attribute key, Object value);
  public <T> T getAttribute(Attribute key);
  
  void setCustomAttribute(Attribute key, Object value);
  
  boolean hasAttribute(Attribute key);
  
  default void setTitle(String title) { setAttribute(GameAttribute.TITLE, title); }
  default String getTitle() { return getAttribute(GameAttribute.TITLE); }
  
  default void setDescription(String description) { setAttribute(GameAttribute.DESCRIPTION, description); }
  default String getDescription() { return getAttribute(GameAttribute.DESCRIPTION); }
  
  default LocationSet getLocation() { return getAttribute(GameAttribute.LOCATION); }
  default LanguageSet getLanguages() { return getAttribute(GameAttribute.LANGUAGE); }
  
  default void setVersion(Version version) { setAttribute(GameAttribute.VERSION, version); }
  default Version getVersion() { return getAttribute(GameAttribute.VERSION); }
  
  default void setLicensed(boolean licensed) { setAttribute(GameAttribute.LICENSED, licensed); }
  default boolean getLicensed() { return getAttribute(GameAttribute.LICENSED); }
  
  default void setSize(GameSize size) { setAttribute(GameAttribute.SIZE, size); }
  default GameSize getSize() { return getAttribute(GameAttribute.SIZE); }
}