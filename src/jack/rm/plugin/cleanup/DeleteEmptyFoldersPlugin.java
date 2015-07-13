package jack.rm.plugin.cleanup;

import java.io.File;
import java.util.LinkedList;
import java.util.Queue;

import jack.rm.data.RomList;

public class DeleteEmptyFoldersPlugin extends CleanupPlugin
{
  @Override public void execute(RomList list)
  {
    Queue<File> files = new LinkedList<File>();
    files.add(list.set.romPath().toFile());
    
    while (!files.isEmpty())
    {
      File f = files.poll();
      File[] l = f.listFiles();

      for (File ff : l)
      {
        if (ff.isDirectory())
        {
          if (ff.listFiles().length == 0)
            ff.delete();
          else
            files.add(ff);
        }
      }
    }
  }
}
