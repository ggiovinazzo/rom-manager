package jack.rm.gui;

import jack.rm.data.RomStatus;
import java.awt.*;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

import javax.swing.*;

public class CountPanel extends JPanel
{
	private static final long serialVersionUID = 1L;
	
	private final RomListModel model;
	JLabel[] counters = new JLabel[4];
	Icon[] icons = new Icon[]{Icon.STATUS_CORRECT, Icon.STATUS_BADLY_NAMED, Icon.STATUS_NOT_FOUND, Icon.STATUS_ALL};
	
	public CountPanel(RomListModel model)
	{
		this.model = model;
	  
	  JPanel inner = new JPanel();
		for (int i = 0; i < counters.length; ++i)
		{
			counters[i] = new JLabel("1234");
			counters[i].setIcon(icons[i].getIcon());
			counters[i].setPreferredSize(new Dimension(55,12));
			inner.add(counters[i]);
		}
		
		this.setLayout(new BorderLayout());
		this.add(inner, BorderLayout.WEST);
	}
	
	public void update()
	{	  
	  Map<RomStatus, Long> status = model.stream().collect(Collectors.groupingBy( r -> r.status, HashMap::new, Collectors.counting()));
	    
    counters[0].setText(""+status.getOrDefault(RomStatus.FOUND, 0L));
    counters[1].setText(""+status.getOrDefault(RomStatus.UNORGANIZED, 0L));
    counters[2].setText(""+status.getOrDefault(RomStatus.MISSING, 0L));
    counters[3].setText(""+model.getSize());
	}
}
