package com.github.jakz.romlib.data.game;

import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Stream;

public class GameClone implements Iterable<Game>
{
  private final Game[] games;
  private final Game[] zones;
  
  public GameClone(Game game, Location zone)
  {
    this.games = new Game[] { game };
    this.zones = new Game[Location.values().length];
    this.zones[zone.ordinal()] = game;
  }
  
  public GameClone(Game[] games, Game[] zones)
  {
    this.games = games;
    this.zones = zones;
  }

  public GameClone(Game[] games)
  {
    this.games = games;
    this.zones = new Game[Location.values().length];
  }
  
  public Game getBestMatchForBias(BiasSet bias, boolean acceptFallback)
  {
    for (Location location : bias.getLocations())
    {
      if (zones[location.ordinal()] != null)
        return zones[location.ordinal()];
    }
    
    if (acceptFallback)
      return games[0];
    else
      return null;
  }
 
  public Game get(Location zone) { return zones[zone.ordinal()]; }
  public Game get(int index) { return games[index]; }
  public int size() { return games.length; }
  
  public Iterator<Game> iterator() { return Arrays.asList(games).iterator(); }
  public Stream<Game> stream() { return Arrays.stream(games); }
}
