package jack.rm.gui.gamelist;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.github.jakz.romlib.data.game.GameStatus;
import com.github.jakz.romlib.data.set.Feature;
import com.github.jakz.romlib.data.set.GameSet;
import com.github.jakz.romlib.ui.Icon;

public class CountPanel extends JPanel
{
	private static final long serialVersionUID = 1L;
	private final static Icon[] icons = new Icon[] {
	  Icon.STATUS_CORRECT, Icon.STATUS_BADLY_NAMED, Icon.STATUS_INCOMPLETE, Icon.STATUS_NOT_FOUND, Icon.STATUS_ALL
	};
	
	private final GameListData data;
	private final JLabel[] counters = new JLabel[5];
	
	JPanel inner;
	private boolean showTotals;
	
	
	public CountPanel(GameListData data)
	{
    this.showTotals = false;

	  
	  this.data = data;
	  
	  inner = new JPanel();
		for (int i = 0; i < counters.length; ++i)
		{
			counters[i] = new JLabel("0000");
			counters[i].setIcon(icons[i].getIcon());
			counters[i].setPreferredSize(new Dimension(55,12));
			
		  if (showTotals)
		    counters[i].setFont(counters[i].getFont().deriveFont(counters[i].getFont().getSize2D()*0.8f));
		}
		
		this.setLayout(new BorderLayout());
		this.add(inner, BorderLayout.WEST);
	}
	
	public void gameSetLoaded(GameSet set)
	{
	  if (inner != null)
	    inner.removeAll();
	  
	  boolean singleRomPerGame = set.hasFeature(Feature.SINGLE_ROM_PER_GAME);
	  
    for (int i = 0; i < counters.length; ++i)
    {
      if (singleRomPerGame && i == 2)
        continue;
      inner.add(counters[i]);
    } 
    
    revalidate();
	}
	
	public void update()
	{	  
	  Map<GameStatus, Long> status = data.stream().collect(Collectors.groupingBy( r -> r.getDrawableStatus(), HashMap::new, Collectors.counting()));
	    
    counters[0].setText(""+status.getOrDefault(GameStatus.FOUND, 0L));
    counters[1].setText(""+status.getOrDefault(GameStatus.UNORGANIZED, 0L));
    counters[2].setText(""+status.getOrDefault(GameStatus.INCOMPLETE, 0L));
    counters[3].setText(""+status.getOrDefault(GameStatus.MISSING, 0L));
    counters[4].setText(""+data.getSize());
    
    if (showTotals)
    {
      Map<GameStatus, Long> totalStatus = data.originalStream().collect(Collectors.groupingBy( r -> r.getDrawableStatus(), HashMap::new, Collectors.counting()));

      counters[0].setText(counters[0].getText()+ " (" + totalStatus.getOrDefault(GameStatus.FOUND, 0L) + ")");
      counters[1].setText(counters[1].getText()+ " (" + totalStatus.getOrDefault(GameStatus.UNORGANIZED, 0L) + ")");
      counters[2].setText(counters[2].getText()+ " (" + totalStatus.getOrDefault(GameStatus.INCOMPLETE, 0L) + ")");
      counters[3].setText(counters[3].getText()+ " (" + totalStatus.getOrDefault(GameStatus.MISSING, 0L) + ")");
      counters[4].setText(counters[4].getText()+ " (" + data.originalStream().count() + ")");
    }
	}
}
