package com.github.jakz.romlib.data.set;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

import com.github.jakz.romlib.data.game.Game;
import com.github.jakz.romlib.data.game.GameClone;

public class CloneSet implements Iterable<GameClone>
{
  private final GameClone[] clones;
  private final Map<Game, GameClone> cloneMap;
  
  public CloneSet(GameClone[] clones)
  {
    this.clones = clones;
    
    cloneMap = new HashMap<>((int)Arrays.stream(clones).flatMap(GameClone::stream).count());
    
    Arrays.stream(clones).forEach(gc -> {
      gc.stream().forEach(g -> cloneMap.put(g, gc));
    });  
  }
  
  public GameClone get(Game game) { return cloneMap.get(game); }
  public GameClone get(int index) { return clones[index]; }
  public int size() { return clones.length; }
  
  public Iterator<GameClone> iterator() { return Arrays.asList(clones).iterator(); }
  public Stream<GameClone> stream() { return Arrays.stream(clones); }
}