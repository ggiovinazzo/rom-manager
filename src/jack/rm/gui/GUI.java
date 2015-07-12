package jack.rm.gui;

import java.util.Random;
import java.awt.Color;

public class GUI
{
  private final static Random random = new Random();
  
  public static Color randomColor()
  {
    final float hue = random.nextFloat();
    final float saturation = (random.nextInt(2000) + 1000) / 10000f;
    final float luminance = 0.9f;
    return Color.getHSBColor(hue, saturation, luminance);
  }
}