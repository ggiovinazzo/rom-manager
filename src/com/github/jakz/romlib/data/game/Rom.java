package com.github.jakz.romlib.data.game;

import java.util.Arrays;

import com.pixbits.lib.io.archive.Verifiable;
import com.pixbits.lib.io.archive.handles.Handle;
import com.pixbits.lib.io.digest.DigestInfo;
import com.pixbits.lib.lang.StringUtils;

public class Rom implements Verifiable
{
  public final String name;
  
  public final byte[] md5;
  public final byte[] sha1;
  public final long crc32;
  public final long size;
  
  private Game game;
  private Handle handle;
  
  public Rom(String name, long size, long crc32, byte[] md5, byte[] sha1)
  {
    this.name = name;
    this.size = size;
    this.crc32 = crc32;
    this.md5 = md5;
    this.sha1 = sha1;
    this.handle = null;
  }
  
  public Rom(String name, long size, DigestInfo info)
  {
    this(name, size, info.crc, info.md5, info.sha1);
  }
  
  public void setHandle(Handle handle) { this.handle = handle; }
  public Handle handle() { return handle; }
  
  public RomID<?> getID() { return new RomID.CRC(crc()); }
  
  public boolean isMissing() { return handle == null; }
  public boolean isPresent() { return handle != null; }
  
  void setGame(Game game) { this.game = game; }
  public Game game() { return game; }
    
  @Override public String toString()
  { 
    StringBuilder builder = new StringBuilder();
    
    builder.delete(0, builder.length());
    builder.append("[").append(name).append(", size: ").append(size).append(", crc: ").append(Long.toHexString(crc32));
    if (md5 != null)
      builder.append(", md5: ").append(StringUtils.toHexString(md5));
    if (sha1 != null)
      builder.append(", sha1: ").append(StringUtils.toHexString(sha1));
    builder.append("]");
    
    return builder.toString();
  }
 
  public boolean isEquivalent(Rom rom)
  {
    return size == rom.size && crc32 == rom.crc32 && (md5 == null || rom.md5 == null || Arrays.equals(md5,rom.md5)) && (sha1 == null || rom.sha1 == null || Arrays.equals(sha1,rom.sha1));
  }
  
  @Override public long size() { return size; }
  @Override public long crc() { return crc32; }
  @Override public byte[] sha1() { return sha1; }
  @Override public byte[] md5() { return md5; }  
}