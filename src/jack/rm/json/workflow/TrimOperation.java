package jack.rm.json.workflow;

import jack.rm.files.Trimmer;

public class TrimOperation implements RomOperation
{
  byte[] filler;
  
  public String getName() { return "Rom Trimmer"; }
  public String getDescription() { return "This operation trims unused space from a ROM to reduce its final size"; }
  
  public TrimOperation(byte[] filler)
  {
    this.filler = filler;
  }
  
  public RomHandle apply(RomHandle rom)
  {
    try {
      Trimmer.trim(rom.getBuffer(), filler);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return rom;
  }
  
}