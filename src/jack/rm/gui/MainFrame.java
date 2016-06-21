package jack.rm.gui;

import jack.rm.*;
import jack.rm.assets.AssetPacker;
import jack.rm.data.rom.Rom;
import jack.rm.data.rom.RomSize;
import jack.rm.data.rom.RomStatus;
import jack.rm.data.romset.*;
import jack.rm.i18n.*;
import jack.rm.plugins.PluginRealType;
import jack.rm.plugins.cleanup.*;

import javax.swing.*;
import javax.swing.event.*;

import com.pixbits.gui.FileTransferHandler;

import java.awt.event.*;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Predicate;
import java.awt.*;

public class MainFrame extends JFrame implements WindowListener
{	
	private static final long serialVersionUID = 1L;
	
	private RomSet set = null;
	
	 //menu
  final private JMenuBar menu = new JMenuBar();
  
  final JMenu romsMenu = new JMenu(Text.MENU_ROMS_TITLE.text());
  final JMenu romsExportSubmenu = new JMenu(Text.MENU_ROMS_EXPORT.text());

  final JMenu viewMenu = new JMenu(Text.MENU_VIEW_TITLE.text());
  final JMenu toolsMenu = new JMenu(Text.MENU_TOOLS_TITLE.text());
	
	final JMenu langMenu = new JMenu(Text.MENU_LANGUAGE_TITLE.text());
	final JMenu helpMenu = new JMenu(Text.MENU_HELP_TITLE.text());
	
	//menu File
	final JMenuItem miRoms[] = new JMenuItem[6];
	
	//menu View
	final JCheckBoxMenuItem miView[] = new JCheckBoxMenuItem[3];
	
	//menu Tools
	final JMenuItem miTools[] = new JMenuItem[3];
	
	final RomListModel romListModel = new RomListModel();
	final public JList<Rom> list = new JList<>();
	final private ListListener listListener = new ListListener();
	final private JScrollPane listPane = new JScrollPane(list);
	
	private final JComboBox<RomSet> cbRomSets = new JComboBox<>();
	
	final MenuListener menuListener = new MenuListener();
		
	final private CardLayout layout = new CardLayout();
	final private JPanel cardMain = new JPanel(new BorderLayout());
	final public LogPanel logPanel = new LogPanel();
	final private ConsolePanel consolePanel = new ConsolePanel();
	
	final private CountPanel countPanel = new CountPanel(romListModel);
	final private SearchPanel searchPanel = new SearchPanel(this);
	final private InfoPanel infoPanel = new InfoPanel();
	final private OptionsFrame optionsFrame = new OptionsFrame(Main.manager);
	
	final private TextOutputFrame textFrame = new TextOutputFrame();
	
	final private ItemListener romSetListener = e -> {
    if (e.getStateChange() == ItemEvent.SELECTED)
      Main.loadRomSet(cbRomSets.getItemAt(cbRomSets.getSelectedIndex()));
	};
	
	final private ListCellRenderer<Object> cbRomSetRenderer = new DefaultListCellRenderer()
	{
	  @Override
	  public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus)
	  {
	    JLabel c = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
	    RomSet set = (RomSet)value;   
	    c.setIcon(set.system.icon != null ? set.system.icon.getIcon() : null);
	    return c;
	  }
	};
	
	public void pluginStateChanged()
	{
	  optionsFrame.pluginStateChanged();
	}

	public MainFrame()
	{
		list.setModel(romListModel);
		list.setCellRenderer(new RomCellRenderer());
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setLayoutOrientation(JList.VERTICAL);
		list.setFixedCellHeight(16);
		list.setBackground(Color.WHITE);
		list.getSelectionModel().addListSelectionListener(listListener);
    list.setSelectedIndex(0);
        
    list.addMouseListener(
        new MouseAdapter(){
          @Override
          public void mouseClicked(MouseEvent e){
            if (e.getClickCount() == 2){
              int r = list.getSelectedIndex();
              
              if (r != -1)
              {
                Rom rom = list.getModel().getElementAt(r);
                
                rom.setFavourite(!rom.isFavourite());
                romListModel.fireChanges(r);   
              }
            }
          }
        });

		listPane.setPreferredSize(new Dimension(230,500));		
		
		
		listPane.setTransferHandler(new FileTransferHandler(new FileDropperListener()));
				
		menu.add(romsMenu);
		menu.add(viewMenu);
		menu.add(toolsMenu);
		//menu.add(langMenu);
		//menu.add(Box.createHorizontalGlue());
		//menu.add(helpMenu);
		
		setJMenuBar(menu);
		
		for (RomSet rs : RomSetManager.sets())
			cbRomSets.addItem(rs);
		
		cbRomSets.addItemListener(romSetListener);
		cbRomSets.setRenderer(cbRomSetRenderer);

		JPanel romListPanel = new JPanel(new BorderLayout());
		romListPanel.add(cbRomSets, BorderLayout.NORTH);
		romListPanel.add(listPane, BorderLayout.CENTER);
		
		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		split.add(romListPanel);
		split.add(infoPanel);
		split.setDividerLocation(400);
		
		cardMain.add(split, BorderLayout.CENTER);
		JPanel south = new JPanel(new GridLayout(1,2));
		south.add(countPanel);
		south.add(searchPanel);
		cardMain.add(south, BorderLayout.SOUTH);
		
		setLayout(layout);
		getContentPane().add(cardMain, "main");
		getContentPane().add(logPanel, "log");
		getContentPane().add(consolePanel, "console");
		layout.show(getContentPane(), "main");

		this.setPreferredSize(new Dimension(1440,900));
		this.addWindowListener(this);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			
		pack();
		setTitle("Rom Manager v0.6 - build 51");
	}
	
	private void exportList(Predicate<Rom> predicate)
	{
    StringBuilder builder = new StringBuilder();
    set.list.stream().filter(predicate).map(r -> r.getTitle()).sorted().forEach(r -> builder.append(r).append('\n'));
    textFrame.showWithText(this, builder.toString());
	}

	private void buildMenu(RomSet set)
	{	
		toolsMenu.removeAll();
	  
		romsMenu.removeAll();
	  romsMenu.add(MenuElement.ROMS_SCAN_FOR_ROMS.item);
    romsMenu.add(MenuElement.ROMS_SCAN_FOR_NEW_ROMS.item);
    romsMenu.addSeparator();
    romsMenu.add(MenuElement.ROMS_RENAME.item);
    romsMenu.add(MenuElement.ROMS_CLEANUP.item);
    romsMenu.addSeparator();
    
    romsMenu.add(romsExportSubmenu);
    
    JMenuItem exportFavorites = new JMenuItem("Export favourites");
    exportFavorites.addActionListener( e -> { exportList(r -> r.isFavourite()); });
    romsExportSubmenu.add(exportFavorites);
    
    JMenuItem exportFound = new JMenuItem(Text.MENU_ROMS_EXPORT_FOUND.text());
    exportFound.addActionListener( e -> { exportList(r -> r.status != RomStatus.MISSING); });
    romsExportSubmenu.add(exportFound);
    
    JMenuItem exportMissing = new JMenuItem(Text.MENU_ROMS_EXPORT_MISSING.text());
    exportMissing.addActionListener( e -> { exportList(r -> r.status == RomStatus.MISSING); });
    romsExportSubmenu.add(exportMissing);

    romsMenu.addSeparator();
    
    romsMenu.add(MenuElement.ROMS_EXIT.item);
    
    JMenuItem[] filters = { MenuElement.VIEW_SHOW_CORRECT.item, MenuElement.VIEW_SHOW_UNORGANIZED.item, MenuElement.VIEW_SHOW_NOT_FOUND.item };
    Arrays.stream(filters).forEach( mi -> {
      viewMenu.add(mi);
      mi.setSelected(true);
    });

    
    toolsMenu.removeAll();

    MenuElement.TOOLS_OPTIONS.item.addActionListener( e -> optionsFrame.showMe() );
    toolsMenu.add(MenuElement.TOOLS_OPTIONS.item);
    
    MenuElement.TOOLS_SHOW_MESSAGES.item.addActionListener( e -> toggleLogPanel(((JMenuItem)e.getSource()).isSelected()));
    toolsMenu.add(MenuElement.TOOLS_SHOW_MESSAGES.item);
    
    MenuElement.TOOLS_CONSOLE.item.addActionListener( e -> toggleConsole(((JMenuItem)e.getSource()).isSelected()));
    toolsMenu.add(MenuElement.TOOLS_CONSOLE.item);
    
    JMenu assetsMenu = new JMenu(Text.MENU_TOOLS_ASSETS.text());
    
    assetsMenu.add(MenuElement.TOOLS_DOWNLOAD_ASSETS.item);
    MenuElement.TOOLS_DOWNLOAD_ASSETS.item.addActionListener( e -> Main.downloader.start() );
    assetsMenu.add(MenuElement.TOOLS_PACK_ASSETS.item);
    MenuElement.TOOLS_PACK_ASSETS.item.addActionListener( e -> AssetPacker.packAssets(set) );
    
    toolsMenu.addSeparator();
    toolsMenu.add(assetsMenu);
    
    JMenu pluginsMenu = new JMenu("Plugins"); // TODO: localize
    
    Set<CleanupPlugin> plugins = set.getSettings().plugins.getEnabledPlugins(PluginRealType.ROMSET_CLEANUP);
    if (!plugins.isEmpty())
    {
      JMenu cleanupMenu = new JMenu("Cleanup");
      
      plugins.forEach( p -> {
        JMenuItem item = new JMenuItem(p.getMenuCaption());
        item.addActionListener( e -> p.execute(set.list));
        cleanupMenu.add(item);
      });
      
      pluginsMenu.add(cleanupMenu);
    }
    
    if (pluginsMenu.getItemCount() != 0)
    {
      toolsMenu.addSeparator();
      toolsMenu.add(pluginsMenu);
    }
	}
			
  class ListListener implements ListSelectionListener
  {
    @Override
    public void valueChanged(ListSelectionEvent e)
    {
      if (e.getValueIsAdjusting())
        return;

      ListSelectionModel lsm = (ListSelectionModel) e.getSource();

      if (lsm.getMinSelectionIndex() == -1)
      {
        infoPanel.resetFields();
        return;
      }

      Rom rom = Main.mainFrame.romListModel.getElementAt(lsm.getMinSelectionIndex());

      infoPanel.updateFields(rom);
    }
  }

	private void toggleLogPanel(boolean flag)
	{
		if (flag)
		{
			logPanel.populate();
			layout.show(getContentPane(), "log");
		}
		else
      layout.show(getContentPane(), "main");
	}
	
	private void toggleConsole(boolean flag)
	{
	   if (flag)
	    {
	      logPanel.populate();
	      layout.show(getContentPane(), "console");
	    }
	    else
	      layout.show(getContentPane(), "main");
	}
	
	public void romSetLoaded(RomSet set)
	{
	  this.set = set;
	  
	  buildMenu(set);
	  
	  searchPanel.activate(false);
	  searchPanel.resetFields(RomSize.mapping.values().toArray(new RomSize[RomSize.mapping.size()]));
	  searchPanel.activate(true);
	  
    cbRomSets.removeItemListener(romSetListener);
    cbRomSets.setSelectedItem(set);
    cbRomSets.addItemListener(romSetListener);
    
    infoPanel.romSetLoaded(set);
    optionsFrame.romSetLoaded(set);
  
    list.clearSelection();
	  updateTable();
	}
	
	public void updateInfoPanel(Rom rom)
	{
	  infoPanel.updateFields(rom);
	}
	
	public void updateTable()
	{
		Rom current = list.getSelectedValue();
		int index = list.getSelectedIndex();
	  
	  /*StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		Arrays.stream(stack).forEach(s -> System.out.println(s));*/
	  
	  //System.out.println("updated table");
	  romListModel.clear();
	  
	  Predicate<Rom> predicate = searchPanel.buildSearchPredicate().and( r ->
	    r.status == RomStatus.FOUND && MenuElement.VIEW_SHOW_CORRECT.item.isSelected() ||
	    r.status == RomStatus.MISSING && MenuElement.VIEW_SHOW_NOT_FOUND.item.isSelected() ||
	    r.status == RomStatus.UNORGANIZED && MenuElement.VIEW_SHOW_UNORGANIZED.item.isSelected()
	  );
	  
		set.list.stream().filter(predicate).forEach(romListModel.collector());

    if (current != null)     
    {      
      list.clearSelection();
      list.setSelectedValue(current, true);
      
      if (list.getSelectedValue() == null && index != -1)
      {
        list.setSelectedIndex(index);
        list.ensureIndexIsVisible(index);
      }
    }
		
    SwingUtilities.invokeLater( () -> {
      romListModel.fireChanges();
    });

		countPanel.update();
	}

	@Override
  public void windowActivated(WindowEvent e) { }
	@Override
  public void windowClosed(WindowEvent e) { }
	@Override
  public void windowDeactivated(WindowEvent e) { }
	@Override
  public void windowIconified(WindowEvent e) { }
	@Override
  public void windowDeiconified(WindowEvent e) { }
	@Override
  public void windowOpened(WindowEvent e) { }
	
	@Override
  public void windowClosing(WindowEvent e)
	{
    set.saveStatus();
	}
}