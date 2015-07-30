package jack.rm.json.workflow;

import jack.rm.data.console.System;
import jack.rm.data.rom.RomAttribute;
import jack.rm.files.GBASavePatcherGBATA;

public class GBASavePatchOperationGBATA extends RomOperation
{
  public String getName() { return "GBA Save Patcher"; }
  public String getDescription() { return "This operation patches all save types of a GBA rom to SRAM"; }
  
  public GBASavePatchOperationGBATA()
  {

  }
  
  public RomHandle apply(RomHandle handle)
  {
    GBASavePatcherGBATA.patch(handle.getRom().getAttribute(RomAttribute.SAVE_TYPE), handle.getBuffer());
    return handle;
  }
  
  public boolean isSystemSupported(System system)
  {
    return system == System.GBA;
  }
  
}
