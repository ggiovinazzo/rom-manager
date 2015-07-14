package jack.rm.gui;

import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import jack.rm.Settings;
import jack.rm.plugin.*;

import java.awt.*;

public class PluginsPanel extends JPanel
{
  private final JTable table;
  private final PluginTableModel model;
  private final PluginManager manager;
  
  class PluginTableModel extends AbstractTableModel
  {
    final List<PluginBuilder> plugins = new ArrayList<>();
    
    private final String[] columnNames = { "Name", "Category", "Enabled" };
    private final Class<?>[] columnClasses = { String.class, String.class, Boolean.class };
    
    @Override public int getColumnCount() { return columnClasses.length; }
    @Override public String getColumnName(int i) { return columnNames[i]; }
    @Override public Class<?> getColumnClass(int i) { return columnClasses[i]; }
    @Override public int getRowCount() { return plugins.size(); }
    
    @Override public Object getValueAt(int r, int c)
    {
      PluginBuilder builder = plugins.get(r);
      
      switch (c)
      {
        case 0: return builder.name+" "+builder.version;
        case 1: return builder.type;
        case 2: return Settings.current().plugins.hasPlugin(builder);
        default: return null;
      }
    }
    
    public void clear() { plugins.clear(); }
    
    public void populate(boolean showOnlyEnabled)
    {
      clear();
      
      manager.stream()
        .filter( b -> !showOnlyEnabled || Settings.current().plugins.hasPlugin(b))
        .forEach(plugins::add);
    }
    
    public void fireChanges()
    {
      this.fireTableDataChanged();
    }
  }
  
  class PluginInfoPanel extends JPanel
  {
    private final JLabel[] labels;
    private final JTextArea desc;
    
    PluginInfoPanel()
    {
      labels = new JLabel[3];
      labels[0] = new JLabel("Name: Bla");
      labels[1] = new JLabel("Category: Category");
      labels[2] = new JLabel("Author: Author");
      
      desc = new JTextArea(3, 80);
      desc.setLineWrap(true);
      desc.setWrapStyleWord(true);
      desc.setEditable(false);
      
      desc.setText("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.)");
      
      JScrollPane descPane = new JScrollPane(desc);
      descPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
      descPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      
      this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
      this.add(labels[0]);
      this.add(labels[1]);
      this.add(labels[2]);
      this.add(descPane);
    }
    
    public void updateFields(PluginBuilder builder)
    {
      labels[0].setText("Name: "+builder.name+" "+builder.version);
      labels[1].setText("Category: "+builder.type);
      labels[2].setText("Author: "+builder.author);
      desc.setText(builder.description);
    }
  }
  
  class PluginConfigTable extends JTable
  {
    private List<TableCellEditor> editors;
    private final PluginArgumentTableModel model;
    
    class PluginArgumentTableModel extends AbstractTableModel
    {
      private final String[] names = { "Name", "Value" };
      
      List<PluginArgument> arguments;
      
      @Override public int getColumnCount() { return 2; }
      @Override public String getColumnName(int i) { return names[i]; }
      @Override public int getRowCount() { return arguments.size(); }
      
      @Override public Object getValueAt(int r, int c)
      {
        PluginArgument arg = arguments.get(r);
        return c == 0 ? arg.getName() : arg.get();
      }
    }
    
    PluginConfigTable()
    {
      super();
      editors = new ArrayList<>();
      model = new PluginArgumentTableModel();
      setModel(model);
    }
    
    @Override public TableCellEditor getCellEditor(int r, int c)
    {
      return editors.get(r);
    }
    
    void prepare(Plugin plugin)
    {
      model.arguments = plugin.getArguments();
      
      
    }
  }
  
  PluginInfoPanel infoPanel;
  
  public PluginsPanel(PluginManager manager)
  {
    this.manager = manager;
    this.model = new PluginTableModel();
    this.table = new JTable(model);
    this.table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    
    this.table.getSelectionModel().addListSelectionListener( e -> {
      if (!e.getValueIsAdjusting())
      {
        int r = table.getSelectedRow();
        if (r != -1)
        {
          r = table.convertRowIndexToModel(r);
          infoPanel.updateFields(model.plugins.get(r));
        }
      }
    });
    
    
    JScrollPane pane = new JScrollPane(table);
    
    infoPanel = new PluginInfoPanel();
    
    this.setLayout(new BorderLayout());
    this.add(pane, BorderLayout.CENTER);
    this.add(infoPanel, BorderLayout.SOUTH);
  }
  
  public void populate()
  {
    model.populate(false);
  }
}
