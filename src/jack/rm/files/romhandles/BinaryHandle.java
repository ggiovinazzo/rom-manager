package jack.rm.files.romhandles;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import jack.rm.files.romhandles.RomPath.Type;

public class BinaryHandle extends RomPath
{
  public final Path file;

  public BinaryHandle(Path file)
  {
    super(Type.BIN);
    this.file = file.normalize();
  }
  
  @Override
  public Path file() { return file; }
  @Override
  public String toString() { return file.getFileName().toString(); }
  @Override
  public String plainName() { return file.getFileName().toString().substring(0, file.getFileName().toString().lastIndexOf('.')); }
  @Override
  public String plainInternalName() { return plainName(); }
  
  @Override public long size() {
    try
    {
      return Files.size(file);
    }
    catch (IOException e)
    {
      e.printStackTrace();
      return 0;
    }
  }
  
  @Override public long uncompressedSize() { return size(); }
  
  @Override public boolean isArchive() { return false; }
  @Override public String getExtension() {
    String filename = file.getFileName().toString();
    int lastdot = filename.lastIndexOf('.');
    return lastdot != -1 ? filename.substring(lastdot+1) : "";
  }
  @Override public String getInternalExtension() { return getExtension(); }
  
  @Override
  public RomPath relocate(Path file)
  {
    return new BinaryHandle(file);
  }
  
  @Override
  public RomPath relocateInternal(String internalName)
  {
    throw new UnsupportedOperationException("a binary rompath doesn't have an internal filename");
  }
  
  @Override
  public InputStream getInputStream() throws IOException
  {
    return new BufferedInputStream(Files.newInputStream(file));
  }
}