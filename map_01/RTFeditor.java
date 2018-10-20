package rtfeditor;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.FlowLayout;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

// Wijziging op lokale feature-branch.

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.TransferHandler;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.rtf.RTFEditorKit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

public class RTFeditor extends JFrame
{   	
	private static final long serialVersionUID = 1L;
	
	private final int FRAME_WIDTH  = 900;
  private final int FRAME_HEIGHT = 700;  
    
  private JMenu mnuFile, mnuEdit, mnuColor;
  
	private Action actionOpen,actionNew, actionSave, actionCut, actionCopy, actionPaste, actionFontOne, actionFontTwo, actionFontSelect, actionColorSelect;
	private Action actionBold, actionItalic, actionUnderline, actionStrikeThrough;
//	private Action actionColor01, actionColor02, actionColor03, actionColor04, actionColor05, actionColor06;
		
	private Style styleDefault;
  private final String DEFAULT_FONT = "Verdana";
  private final int DEFAULT_FONT_SIZE = 12;
		
	JTextPane txtEditor;
	JPopupMenu contextMenuEdit;
  JButton btn01, btn02;
  
  RTFEditorKit   RTFkit;
  StyleContext   context;
  StyledDocument document;  
  ActionGeneral  actionGeneral;
  
  private final String STYLE_FONT_01 = "font01";
  private final String STYLE_FONT_02 = "font02";
  private final String STYLE_STRIKE_THROUGH = "strikeThrough";
  
  private final String mnuItemCutCaption   = "Cut";
  private final String mnuItemCopyCaption  = "Copy";
  private final String mnuItemPasteCaption = "Paste";
  
  private final String mnuItemBoldCaption = "Bold";
  private final String mnuItemItalicCaption = "Italic";
  private final String mnuItemUnderlineCaption = "Underline";
  private final String mnuItemStrikeThroughCaption = "Strike through";
  
  private final String mnuItemFontOneCaption = "Font 01";
  private final String mnuItemFontTwoCaption = "Font 02";  
  
  private final String mnuItemFontSelectCaption  = "Select font ...";  
  private final String mnuItemColorSelectCaption = "Font color ...";  
  
  
  private DataFlavor DSDocFlavor = new DataFlavor(DefaultStyledDocument.class, "DSDocFlavor");
  // private DataFlavor rtfFlavor = new DataFlavor("text/rtf; charset=ISO-8859-1", "Rich Text Format");
	private DataFlavor rtfFlavor = new DataFlavor("text/rtf", "Rich Text Format");
  
	public RTFeditor()
	{
	  super("RTF editor");
	  	  
	  RTFkit  = new RTFEditorKit();
    context = new StyleContext();
    actionGeneral = new ActionGeneral();
    
	  createActions();    
    createStyles();	  
    
    Container cont = getContentPane();
    cont.setLayout(null);
	      
    //--- Menu-stuff :    
    
    mnuFile = new JMenu("File");
    mnuEdit = new JMenu("Edit");
    mnuColor = new JMenu("Color");
    
    JMenuBar menuBar = new JMenuBar();    
    menuBar.add(mnuFile);    
    menuBar.add(mnuEdit);    
    menuBar.add(mnuColor);    
    this.setJMenuBar(menuBar);    
    
    addItemsToMenu();
    
    //--- Context-menu-stuff : 
    
    contextMenuEdit = new JPopupMenu();    
    addItemsToContextMenu();
    
    //--- Toolbar-stuff :
    
    JToolBar toolBar = new JToolBar();
    toolBar.setBorder(BorderFactory.createEtchedBorder());
    toolBar.setFloatable(false);        
    addButtonsToToolBar(toolBar); 

    int width, height, X, Y;
    width  = 10 + (int) toolBar.getPreferredSize().getWidth();
    height = 10 + (int) toolBar.getPreferredSize().getHeight();
    
    // Place the toolbar inside a panel, (otherwise 
    // it won't show up, because of the 'null'-layout) :
    JPanel panel = new JPanel();
    panel.setBounds(0,0 , width,height);
    panel.setLayout( new FlowLayout(FlowLayout.CENTER) );
    // p.setBackground(Color.orange);
    panel.add(toolBar);
    cont.add(panel);       
    
    X = 5;
    Y = height;
        
    width = 700 ; height = 200;
    txtEditor = new JTextPane();
    txtEditor.setContentType("text/rtf");
    txtEditor.setEditorKit(RTFkit);
    txtEditor.addMouseListener
    (
      new MouseListener()
      {
        @Override
				public void mouseEntered(MouseEvent e) { }
        @Override
				public void mouseExited(MouseEvent e)  { }  
        
        // The popup-trigger is OS-dependent so we
        // need to catch multiple mouse-events here :
        @Override
				public void mouseClicked(MouseEvent e)  { handleMouseEvent(e); }
        @Override
				public void mouseReleased(MouseEvent e) { handleMouseEvent(e); }
        @Override
				public void mousePressed(MouseEvent e)  { handleMouseEvent(e); }
        
        private void handleMouseEvent(MouseEvent e)
        { 
          if (e.isPopupTrigger()) { contextMenuEdit.show(txtEditor, e.getX(), e.getY()); }
        }       
      }
    );
     
    
    txtEditor.setTransferHandler(new MyTransferHandler());
    
    
    JScrollPane scrollPane = new JScrollPane(txtEditor, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setBounds(X,Y , width,height);
    cont.add(scrollPane);        
    
    CreateKeyboardShortcuts();
    
    
		UndoManager undoManager = new UndoManager();
		txtEditor.getDocument().addUndoableEditListener(undoManager);   
    
//		txtEditor.getDocument().addDocumentListener(				
//	  		new DocumentListener()
//				{
//
//					@Override
//					public void changedUpdate(DocumentEvent arg0)
//					{
//						System.err.println(String.format("Result : '%s'", "changedUpdate"));
//					}
//
//					@Override
//					public void insertUpdate(DocumentEvent arg0)
//					{
//						System.err.println(String.format("Result : '%s'", "insertUpdate"));
//					}
//
//					@Override
//					public void removeUpdate(DocumentEvent arg0)
//					{
//						System.err.println(String.format("Result : '%s'", "removeUpdate"));
//					}	  			
//				}		
//		);
		
    Y += height + 10;    
    // btn01 = new JButton(actionGeneral);
    btn01 = new JButton(new UndoAction(undoManager));
    btn01.setText("Undo");
    width  = (int) btn01.getPreferredSize().getWidth();
    height = (int) btn01.getPreferredSize().getHeight();
    btn01.setBounds(X, Y, width, height);
    cont.add(btn01);
    
    X += width + 30;    
    // btn02 = new JButton(actionGeneral);    
    btn02 = new JButton(new RedoAction(undoManager));    
    btn02.setText("Redo");
    width = (int) btn02.getPreferredSize().getWidth();
    height = (int) btn02.getPreferredSize().getHeight();
    btn02.setBounds(X, Y, width, height);
    cont.add(btn02);    
    
    document = txtEditor.getStyledDocument();
    document.setLogicalStyle(0, styleDefault);
    
    X = 400;
    Y = 200;
    setBounds(X, Y, FRAME_WIDTH, FRAME_HEIGHT);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setResizable(false);
    setVisible(true);
	}

  @SuppressWarnings("unused")
	public static void main(String[] args)
	{
		new RTFeditor();
	}
	
  private void addItemsToMenu()
  {
    mnuEdit.add(createMenuItem(actionCut,   mnuItemCutCaption));
    mnuEdit.add(createMenuItem(actionCopy,  mnuItemCopyCaption));
    mnuEdit.add(createMenuItem(actionPaste, mnuItemPasteCaption)); 
    mnuEdit.addSeparator();
    mnuEdit.add(createMenuItem(actionBold,          mnuItemBoldCaption));
    mnuEdit.add(createMenuItem(actionItalic,        mnuItemItalicCaption));
    mnuEdit.add(createMenuItem(actionUnderline,     mnuItemUnderlineCaption));
    mnuEdit.add(createMenuItem(actionStrikeThrough, mnuItemStrikeThroughCaption));    
    mnuEdit.addSeparator();
    mnuEdit.add(createMenuItem(actionFontOne, mnuItemFontOneCaption));
    mnuEdit.add(createMenuItem(actionFontTwo, mnuItemFontTwoCaption));
    
    
    mnuColor.add(createMenuItem(actionColorSelect, mnuItemColorSelectCaption));
    
  }
  
  private void addItemsToContextMenu()
  {
    contextMenuEdit.add(createMenuItem(actionFontSelect,   mnuItemFontSelectCaption));
    contextMenuEdit.add(createMenuItem(actionColorSelect,  mnuItemColorSelectCaption));
  	contextMenuEdit.addSeparator();
    contextMenuEdit.add(createMenuItem(actionCut,   mnuItemCutCaption));
    contextMenuEdit.add(createMenuItem(actionCopy,  mnuItemCopyCaption));
    contextMenuEdit.add(createMenuItem(actionPaste, mnuItemPasteCaption)); 
    contextMenuEdit.addSeparator();
    contextMenuEdit.add(createMenuItem(actionBold,          mnuItemBoldCaption));
    contextMenuEdit.add(createMenuItem(actionItalic,        mnuItemItalicCaption));
    contextMenuEdit.add(createMenuItem(actionUnderline,     mnuItemUnderlineCaption));
    contextMenuEdit.add(createMenuItem(actionStrikeThrough, mnuItemStrikeThroughCaption));
  }  
  
  private JMenuItem createMenuItem(Action action, String caption)
  {    
    JMenuItem item = new JMenuItem(action);
    item.setText(caption);
    return item;    
  }  
  
	private void addButtonsToToolBar(JToolBar toolBar)
	{
		Dimension separator = new Dimension(6,0);
		
		toolBar.add(new JButton(actionOpen)); 
		
		
		JButton btnNew = new JButton(new ImageIcon("icon_new.gif"));
		// btn01.setAction(actionObj01);
		toolBar.add(btnNew);
				
		toolBar.add(new JButton(actionSave)); 
		
		toolBar.addSeparator(separator);
		
		toolBar.add(new JButton(actionCut)); 
		toolBar.add(new JButton(actionCopy)); 
		toolBar.add(new JButton(actionPaste)); 
		
		toolBar.addSeparator(separator);
		
    toolBar.add(new JButton(actionFontOne));   
    toolBar.add(new JButton(actionFontTwo)); 
		
		toolBar.addSeparator(separator);		
		
		toolBar.add(new JButton(actionBold));		
		toolBar.add(new JButton(actionItalic));		
		toolBar.add(new JButton(actionUnderline));
		toolBar.add(new JButton(actionStrikeThrough));
		
		toolBar.addSeparator(separator);		
		
		JButton btn12 = new JButton(new ImageIcon("icon_color_01.gif"));
		// btn12.setAction(actionObj12);
		toolBar.add(btn12);
		
		JButton btn13 = new JButton(new ImageIcon("icon_color_03.gif"));
		// btn13.setAction(actionObj13);
		toolBar.add(btn13);
		
		JButton btn14 = new JButton(new ImageIcon("icon_color_05.gif"));
		// btn14.setAction(actionObj14);
		toolBar.add(btn14);
		
		JButton btn15 = new JButton(new ImageIcon("icon_color_07.gif"));
		// btn15.setAction(actionObj15);
		toolBar.add(btn15);
		
		JButton btn16 = new JButton(new ImageIcon("icon_color_13.gif"));
		// btn16.setAction(actionObj16);
		toolBar.add(btn16);
		
		JButton btn17 = new JButton(new ImageIcon("icon_color_14.gif"));
		// btn17.setAction(actionObj17);
		toolBar.add(btn17);
	}
		
	private void createActions()
	{			  
	  actionOpen = new ActionOpen();
	  actionOpen.putValue(Action.NAME, null);   
	  actionOpen.putValue(Action.SMALL_ICON, new ImageIcon("icon_open.gif"));
	  actionOpen.putValue(Action.SHORT_DESCRIPTION, "Open a text file.");    
	  actionOpen.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));  
	  	  
	  actionSave = new ActionSave();
	  actionSave.putValue(Action.NAME, null);   
	  actionSave.putValue(Action.SMALL_ICON, new ImageIcon("icon_save.gif"));
	  actionSave.putValue(Action.SHORT_DESCRIPTION, "Save the current text file.");    
	  actionSave.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));  
	  
	  actionCut = new DefaultEditorKit.CutAction();
	  actionCut.putValue(Action.NAME, null);   
	  actionCut.putValue(Action.SMALL_ICON, new ImageIcon("icon_cut.gif"));
	  actionCut.putValue(Action.SHORT_DESCRIPTION, "Remove the selection and put it on the clipboard.");    
	  actionCut.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK));	  

	  actionCopy = new DefaultEditorKit.CopyAction();
	  actionCopy.putValue(Action.NAME, null);   
	  actionCopy.putValue(Action.SMALL_ICON, new ImageIcon("icon_copy.gif"));
	  actionCopy.putValue(Action.SHORT_DESCRIPTION, "Copy the selection to the clipboard.");    
	  actionCopy.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));

	  actionPaste = new DefaultEditorKit.PasteAction();
	  actionPaste.putValue(Action.NAME, null);   
	  actionPaste.putValue(Action.SMALL_ICON, new ImageIcon("icon_paste.gif"));
	  actionPaste.putValue(Action.SHORT_DESCRIPTION, "Insert the content on the clipboard into the editor.");    
	  actionPaste.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));
	  	  
		actionBold = new StyledEditorKit.BoldAction();
		actionBold.putValue(Action.NAME, null);		
		actionBold.putValue(Action.SMALL_ICON, new ImageIcon("icon_bold.gif"));
		actionBold.putValue(Action.SHORT_DESCRIPTION, "Apply style 'bold' to the selected text.");    
		actionBold.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_DOWN_MASK));

		actionItalic = new StyledEditorKit.ItalicAction();
		actionItalic.putValue(Action.NAME, null);
		actionItalic.putValue(Action.SMALL_ICON, new ImageIcon("icon_italic.gif"));
		actionItalic.putValue(Action.SHORT_DESCRIPTION, "Apply style 'italic' to the selected text.");    
		actionItalic.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK));
		
		actionUnderline = new StyledEditorKit.UnderlineAction();
		actionUnderline.putValue(Action.NAME, null);
		actionUnderline.putValue(Action.SMALL_ICON, new ImageIcon("icon_underline.gif"));
		actionUnderline.putValue(Action.SHORT_DESCRIPTION, "Apply style 'underline' to the selected text.");    
		actionUnderline.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_DOWN_MASK));
				
		actionStrikeThrough = new ActionStrikeThrough();
		actionStrikeThrough.putValue(Action.NAME, null);
		actionStrikeThrough.putValue(Action.SMALL_ICON, new ImageIcon("icon_strike.gif"));
		actionStrikeThrough.putValue(Action.SHORT_DESCRIPTION, "Apply style 'strike-through' to the selected text."); 
		actionStrikeThrough.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.CTRL_DOWN_MASK));	
		
		actionFontOne = new ActionFontOne();
		actionFontOne.putValue(Action.NAME, null);
		actionFontOne.putValue(Action.SMALL_ICON, new ImageIcon("icon_font_01.gif"));
		actionFontOne.putValue(Action.SHORT_DESCRIPTION, "Apply font 01 to the selected text."); 
		actionFontOne.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.CTRL_DOWN_MASK)); 
		
    actionFontTwo = new ActionFontTwo();
    actionFontTwo.putValue(Action.NAME, null);
    actionFontTwo.putValue(Action.SMALL_ICON, new ImageIcon("icon_font_02.gif"));
    actionFontTwo.putValue(Action.SHORT_DESCRIPTION, "Apply font 02 to the selected text."); 
    actionFontTwo.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.CTRL_DOWN_MASK)); 
    
    actionColorSelect = new ActionColorSelect();
    actionColorSelect.putValue(Action.NAME, null);
    actionColorSelect.putValue(Action.SMALL_ICON, new ImageIcon("icon_font_02.gif"));
    actionColorSelect.putValue(Action.SHORT_DESCRIPTION, "Apply font 02 to the selected text."); 
    actionColorSelect.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_DOWN_MASK)); 
    
		//menu.add(new StyledEditorKit.FontSizeAction("12", 12));
    
	}
	
	// java.awt.Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(event);

	
	private void createStyles()
	{
    // StyleConstants.setAlignment(style, StyleConstants.ALIGN_RIGHT);
    // StyleConstants.setSpaceAbove(styleDefault, 4);
    // StyleConstants.setSpaceBelow(styleDefault, 4);
    // StyleConstants.setLeftIndent(styleDefault, 10f);
    
	  styleDefault = context.getStyle(StyleContext.DEFAULT_STYLE);
    StyleConstants.setFontFamily(styleDefault, DEFAULT_FONT);
    StyleConstants.setFontSize(styleDefault, DEFAULT_FONT_SIZE);
    StyleConstants.setLineSpacing(styleDefault, 0.125f);
	}
	
	
	private void CreateKeyboardShortcuts()
	{		
	  InputMap inputMap = txtEditor.getInputMap();	  
	  inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, Event.CTRL_MASK), DefaultEditorKit.selectWordAction);	  
	  inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_L, Event.CTRL_MASK), DefaultEditorKit.selectLineAction);	  
	}
	
	
//	private void load()
//	{
//		try
//		{
//			String filename = "//c://temp//test.rtf";			
//      FileInputStream fi = new FileInputStream(filename);
//      RTFkit.read(fi, txtEditor.getDocument(), 0);
//		}
//		catch (Exception e) 
//		{ 
//			System.err.println(e);
//		}		
//	}
	
//	private void save(String content, String fileName)
//	{
//    try
//    {
//      BufferedWriter bw = new BufferedWriter( new FileWriter(fileName) );
//      bw.write(content); 
//      bw.flush(); 
//      bw.close();
//    }
//
//    catch (IOException ioe)
//    {
//      String message = "There seems to be a problem while attempting to save :\n\n" + ioe;
//      JOptionPane.showMessageDialog(null, message, "error :", JOptionPane.WARNING_MESSAGE);
//    }		
//	}
 		
//	private void saveAsRTF()
//	{
//    ByteArrayOutputStream str = new ByteArrayOutputStream();          
//    try
//    {
//      rtfKit.write(str, txtTarget.getDocument(), 0, txtTarget.getDocument().getLength());
//      String rtf = str.toString();
//      save(rtf, "savedFile.rtf");
//    }
//    catch (Exception e)
//    {
//      e.printStackTrace();
//    }
//	}	
	
	//-- Inner-classes :
		
//	private class ActionSelectLine extends javax.swing.text.DefaultEditorKit.CopyAction
//	{		
//	}
		
	private class ActionGeneral extends AbstractAction
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent ae)
		{			
	    Object obj = ae.getSource();
	    if      (obj == btn01) { test(); }    	    
	    else if (obj == btn02) {  }	    
		}
	}	
	
	private void test()
	{
	}
	
	private class ActionStrikeThrough extends StyledEditorKit.StyledTextAction
	{
	  private static final long serialVersionUID = 1L;

	  public ActionStrikeThrough()
	  {
	    super("font-strike-through");
	  }

	  @Override
		public void actionPerformed(ActionEvent e)
	  {
	    JEditorPane editor = getEditor(e);
	    if (editor != null)
	    {
	      MutableAttributeSet mas = getStyledEditorKit(editor).getInputAttributes();
	      boolean apply = (StyleConstants.isStrikeThrough(mas)) ? false : true;
	      SimpleAttributeSet sas = new SimpleAttributeSet();
	      StyleConstants.setStrikeThrough(sas, apply);
	      setCharacterAttributes(editor, sas, false);
	    }
	  }	  
	}
	
	private class ActionFontOne extends StyledEditorKit.StyledTextAction
	{
	  private static final long serialVersionUID = 1L;
	  private AttributeSet sas;

	  public ActionFontOne()
	  {
	    super("font-one");
	  }

	  @Override
		public void actionPerformed(ActionEvent e)
	  {
	    JEditorPane editor = getEditor(e);
	    if (editor != null)
	    {	        
	      sas = context.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.FontFamily, DEFAULT_FONT);	        
	      setCharacterAttributes(editor, sas, true);	        
	      sas = context.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.FontSize, DEFAULT_FONT_SIZE);	          
	      setCharacterAttributes(editor, sas, true);
	    }
	  }   
	}	
	
	private class ActionFontTwo extends StyledEditorKit.StyledTextAction
	{
	  private static final long serialVersionUID = 1L;
	  private AttributeSet sas;

	  public ActionFontTwo()
	  {
	    super("font-two");
	  }

	  @Override
		public void actionPerformed(ActionEvent e)
	  {
	    JEditorPane editor = getEditor(e);
	    if (editor != null)
	    {         
	      sas = context.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.FontFamily, "Consolas");          
	      setCharacterAttributes(editor, sas, false);         
	      sas = context.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.FontSize, 14);            
	      setCharacterAttributes(editor, sas, false);
	    }
	  }   
	}	
	
	
	private class ActionOpen extends AbstractAction 
  {
  	private static final long serialVersionUID = 1L;
    public ActionOpen() 
    { 
      super("Open"); 
    }

    @Override
		public void actionPerformed(ActionEvent ev)
    {
      JFileChooser chooser = new JFileChooser();
      if (chooser.showOpenDialog(RTFeditor.this) != JFileChooser.APPROVE_OPTION) { return; }
      
      File file = chooser.getSelectedFile();
      if (file == null) { return; }
      
      try
      {
        FileInputStream fi = new FileInputStream(file);
        RTFkit.read(fi, txtEditor.getDocument(), 0);
      }
      catch (Exception e) 
      { 
        System.err.println(e);
      }   
    }
  }
	  
  private class ActionSave extends AbstractAction
  {
  	private static final long serialVersionUID = 1L;
  	
    public ActionSave()
    {
      super("Save");
    }

    @Override
		public void actionPerformed(ActionEvent ev)
    {
      JFileChooser chooser = new JFileChooser();
      if (chooser.showSaveDialog(RTFeditor.this) != JFileChooser.APPROVE_OPTION) { return; }
      
      File file = chooser.getSelectedFile();
      if (file == null) { return; }
      
      ByteArrayOutputStream stream = new ByteArrayOutputStream();          
      try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))) 
      {
        RTFkit.write(stream, txtEditor.getDocument(), 0, txtEditor.getDocument().getLength());
        String RTFcontent = stream.toString();
               
        bufferedWriter.write(RTFcontent);        
        bufferedWriter.flush();
        bufferedWriter.close();        
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }      
    }    
  }  
	
	
//  class ActionColorSelect extends StyledEditorKit.StyledTextAction
//  {
//    private static final long serialVersionUID = 6384632651737400352L;
//
//    JColorChooser colorChooser = new JColorChooser();
//    JDialog dialog = new JDialog();
//
//    boolean noChange = false;
//    boolean cancelled = false;
//    private Color foreground;
//
//    public ActionColorSelect()
//    {
//      super("foreground");
//    }
//
//    public void actionPerformed(ActionEvent e)
//    {
//      //JTextPane editor = (JTextPane) getEditor(e);
//      JEditorPane editor = getEditor(e);      
//      
//      if (editor == null)
//      {
//        JOptionPane.showMessageDialog(null, "You need to select the editor pane before you can change the color.", "Error", JOptionPane.ERROR_MESSAGE);
//        return;
//      }
//      
//      int start = editor.getSelectionStart();
//      StyledDocument doc = getStyledDocument(editor);
//      Element paragraph = doc.getCharacterElement(start);
//      AttributeSet as = paragraph.getAttributes();
//      foreground = StyleConstants.getForeground(as);
//      
//      if (foreground == null)
//      {
//        foreground = Color.BLACK;
//      }
//      colorChooser.setColor(foreground);
//
//      JButton accept = new JButton("OK");
//      accept.addActionListener(new ActionListener()
//      {
//        public void actionPerformed(ActionEvent ae)
//        {
//          foreground = colorChooser.getColor();
//          dialog.dispose();
//        }
//      });
//
//      JButton cancel = new JButton("Cancel");
//      cancel.addActionListener(new ActionListener()
//      {
//        public void actionPerformed(ActionEvent ae)
//        {
//          cancelled = true;
//          dialog.dispose();
//        }
//      });
//
//      JButton none = new JButton("None");
//      none.addActionListener(new ActionListener()
//      {
//        public void actionPerformed(ActionEvent ae)
//        {
//          noChange = true;
//          dialog.dispose();
//        }
//      });
//
//      JPanel buttons = new JPanel();
//      buttons.add(accept);
//      buttons.add(none);
//      buttons.add(cancel);
//
//      dialog.getContentPane().setLayout(new BorderLayout());
//      dialog.getContentPane().add(colorChooser, BorderLayout.CENTER);
//      dialog.getContentPane().add(buttons, BorderLayout.SOUTH);
//      dialog.setModal(true);
//      dialog.pack();
//      dialog.setVisible(true);
//
//      if (!cancelled)
//      {
//        MutableAttributeSet attr = null;
//        if (editor != null)
//        {
//          if (foreground != null && !noChange)
//          {
//            attr = new SimpleAttributeSet();
//            StyleConstants.setForeground(attr, foreground);
//            setCharacterAttributes(editor, attr, false);
//          }
//        }
//      }// end if color != null
//      noChange = false;
//      cancelled = false;
//    }
//    
//  }
	
  
  private class ActionColorSelect extends StyledEditorKit.StyledTextAction
	{
	  private static final long serialVersionUID = 1L;

	  public ActionColorSelect()
	  {
	    super("select-color");
	  }

	  @Override
		public void actionPerformed(ActionEvent e)
	  {
//	    JEditorPane editor = getEditor(e);
//	    if (editor != null)
//	    {
//	    }
	  	
      Color initialBackground = Color.ORANGE;
      Color color = JColorChooser.showDialog(null, "JColorChooser Sample", initialBackground);
      if (color != null) 
      {
      	System.err.println(String.format("Selected color : '%s'", color));
      }
	  	
	  }   
	}	
  
  
	private class ActionFontSelect extends StyledEditorKit.StyledTextAction
	{
	  private static final long serialVersionUID = 1L;

	  public ActionFontSelect()
	  {
	    super("select-font");
	  }

	  @Override
		public void actionPerformed(ActionEvent e)
	  {
	    JEditorPane editor = getEditor(e);
	    if (editor != null)
	    {         

	    }
	  }   
	}	
	
	
	private class UndoAction extends AbstractAction
	{
		private static final long serialVersionUID = 1L;		
		private UndoManager undoManager;
		
		public UndoAction(UndoManager mngr)
		{
			super("Undo");
			undoManager = mngr;
		}

		@Override
		public void actionPerformed(ActionEvent actionEvent)
		{
			try
			{
				undoManager.undo();
			}
			catch (CannotUndoException cue)
			{
				// System.err.println(String.format("Exception : %s", cue));
			}
		}
	}
	
	private class RedoAction extends AbstractAction
	{
		private static final long serialVersionUID = 1L;
		private UndoManager undoManager;
		
		public RedoAction(UndoManager mngr)
		{
			super("Redo");
			undoManager = mngr;
		}

		@Override
		public void actionPerformed(ActionEvent actionEvent)
		{
			try
			{
				undoManager.redo();
			}
			catch (CannotRedoException cre)
			{
				// System.err.println(String.format("Exception : %s", cre));
			}
		}
	}		
	
//	private class MyTransferable implements Transferable
//	{
//	    private Object data = null;
//	    private DataFlavor flavor;
//
//	    public MyTransferable(Object o, DataFlavor df)
//	    { data = o; flavor = df; }
//
//	    @Override
//			public Object getTransferData (DataFlavor df) throws
//	    UnsupportedFlavorException, IOException
//	    {
//	        if (!flavor.isMimeTypeEqual(flavor))
//	            throw new UnsupportedFlavorException(df);
//	        return data;
//	    }
//
//	    @Override
//			public boolean isDataFlavorSupported (DataFlavor df)
//	    {
//	        return flavor.isMimeTypeEqual(df);
//	    }
//
//	    @Override
//			public DataFlavor[] getTransferDataFlavors()
//	    {
//	        DataFlavor[] ret = {flavor};
//	        return ret;
//	    }
//	}
	
	
	
	private class MyTransferHandler extends TransferHandler
	{
		@Override
		public boolean canImport(JComponent comp, DataFlavor[] transferFlavors)
		{
			System.out.println("method canImport");
			MyTextPaneSelection s = new MyTextPaneSelection(""); // Dummy object
			boolean retour = false;
			for (int i = 0; i < transferFlavors.length; i++)
			{
				System.out.println(transferFlavors[i]);
				if (s.isDataFlavorSupported(transferFlavors[i]))
				{
					retour = true;
				}
			}
			return retour;
		}

		@Override
		protected Transferable createTransferable(JComponent c)
		{
			System.out.println("method createTransferable");
			JTextPane aTextPane = (JTextPane) c;
			int start = aTextPane.getSelectionStart();
			int end = aTextPane.getSelectionEnd();

			StyledDocument aSDoc = aTextPane.getStyledDocument();
			DefaultStyledDocument dSDocSelection = copyDocument(aSDoc, start, end);
			return (new MyTextPaneSelection(dSDocSelection));
		}

		public DefaultStyledDocument copyDocument(StyledDocument src, int selectionStart, int selectionEnd)
		{
			System.out.println("copyDocument Commence");
			Element rootElement, paragraphElement, textElement;
			SimpleAttributeSet copyAttrs;
			int startOffset, endOffset;
			String copy_string;

			rootElement = src.getDefaultRootElement();
			DefaultStyledDocument copyDoc = new DefaultStyledDocument();

			for (int lpParagraph = 0; lpParagraph < rootElement.getElementCount(); lpParagraph++)
			{
				paragraphElement = rootElement.getElement(lpParagraph);

				// Check if the paragraph need to be copy
				if (paragraphElement.getEndOffset() < selectionStart)
				{
					System.out.println("Continue paragraph");
					continue; // Go to the next paragraph
				}
				if (paragraphElement.getStartOffset() > selectionEnd)
				{
					System.out.println("Break paragraph");
					break; // Exit the boucle
				}

				for (int lpText = 0; lpText < paragraphElement.getElementCount(); lpText++)
				{
					System.out.println("Text Boucle");
					// Insert a Element in the new Document
					textElement = paragraphElement.getElement(lpText);

					// Check if the Element need to be copy
					if (textElement.getEndOffset() < selectionStart)
					{
						System.out.println("Continue text");
						continue; // Go to the next Element
					}
					if (textElement.getStartOffset() > selectionEnd)
					{
						System.out.println("Break text");
						break; // Exit the boucle
					}

					copyAttrs = new SimpleAttributeSet(textElement.getAttributes());

					// Find the value of startOffset and endOffset
					if (textElement.getStartOffset() < selectionStart)
					{
						startOffset = selectionStart;
					}
					else
					{
						startOffset = textElement.getStartOffset();
					}
					if (textElement.getEndOffset() > selectionEnd)
					{
						endOffset = selectionEnd;
					}
					else
					{
						endOffset = textElement.getEndOffset();
					}

					try
					{
						copy_string = src.getText(startOffset, (endOffset - startOffset));
						copyDoc.insertString(copyDoc.getLength(), copy_string, copyAttrs);
					}
					catch (BadLocationException e)
					{
						System.out.println("Pogner une exception");
					}
				}
				// Modify the Style of the paragraph
				copyAttrs = new SimpleAttributeSet(paragraphElement.getAttributes());
				startOffset = paragraphElement.getStartOffset();
				endOffset = paragraphElement.getEndOffset();

				copyDoc.setParagraphAttributes(startOffset, (endOffset - startOffset), copyAttrs, true);
			}

			return copyDoc;
		}

		@Override
		public void exportToClipboard(JComponent comp, Clipboard clip, int action)
		{
			System.out.println("method exportToClipboard");
			super.exportToClipboard(comp, clip, action);
		}

		@Override
		public int getSourceActions(JComponent c)
		{
			System.out.println("method getSourceActions");
			return COPY_OR_MOVE;
		}

		@Override
		protected void exportDone(JComponent source, Transferable data, int action)
		{
			System.out.println("method exportDone");
			JTextPane srcTextPane = (JTextPane) source;
			if (action == MOVE)
			{
				srcTextPane.replaceSelection("");
			}
		}

		@Override
		public boolean importData(JComponent comp, Transferable t)
		{
			System.out.println("method importData");
			if (canImport(comp, t.getTransferDataFlavors()))
			{

				if (t.isDataFlavorSupported(DSDocFlavor))
				{
					JTextPane theTextPane = (JTextPane) comp;
					theTextPane.replaceSelection("");
					StyledDocument theSDoc = new DefaultStyledDocument();
					try
					{
						theSDoc = (StyledDocument) t.getTransferData(DSDocFlavor);
					}
					catch (Exception e)
					{
						System.out.println("importData with StyledDocument failed");
					}
					int thePos = theTextPane.getCaretPosition();
					insertDocument(theSDoc, thePos, theTextPane);
					return true;

				}
				
				else if (t.isDataFlavorSupported(rtfFlavor))
				{
					System.out.println("RTF");
					JTextPane theTextPane = (JTextPane) comp;
									
			
						// ByteArrayInputStream
						//FileInputStream fi;

						
		        //FileInputStream fi = new FileInputStream(file);
		        try
						{
							RTFkit.read((ByteArrayInputStream)t.getTransferData(rtfFlavor), theTextPane.getDocument(), 0);
						}
						catch (Exception e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						
			
					
					//int thePos = theTextPane.getCaretPosition();
					//theTextPane.setText(transferString, thePos, theTextPane);
					//theTextPane.setText(transferString);
					return true;
					
				}
				
				
				else
				{

					System.out.println("canImport is true");
					String textString = "";
					try
					{
						textString = (String) t.getTransferData(DataFlavor.stringFlavor);

						JTextPane aTextPane = (JTextPane) comp;
						aTextPane.replaceSelection(textString);
					}
					catch (Exception e)
					{
						System.out.println("Exception in importData");
					}
					return true;
				}
			}
			System.out.println("canImport is false");
			return false;
		}
		
		// Insert a Document in a another Document
		public void insertDocument(StyledDocument srcSDoc, int srcPos, JTextPane theTextPane)
		{
			StyledDocument theSDoc = theTextPane.getStyledDocument();
			Element rootElement, paragraphElement, textElement;
			SimpleAttributeSet copyAttrs;
			int startOffset, endOffset;
			int pos = srcPos;
			String copy_string;

			rootElement = srcSDoc.getDefaultRootElement();

			for (int lpParagraph = 0; lpParagraph < rootElement.getElementCount(); lpParagraph++)
			{
				paragraphElement = rootElement.getElement(lpParagraph);

				for (int lpText = 0; lpText < paragraphElement.getElementCount(); lpText++)
				{
					textElement = paragraphElement.getElement(lpText);
					copyAttrs = new SimpleAttributeSet(textElement.getAttributes());
					startOffset = textElement.getStartOffset();
					endOffset = textElement.getEndOffset();
					// A Verifier
					try
					{
						copy_string = srcSDoc.getText(startOffset, (endOffset - startOffset));
						theSDoc.insertString(pos, copy_string, copyAttrs);
					}
					catch (BadLocationException e)
					{
						System.out.println("Pogner une exception");
					}
					pos += endOffset - startOffset;
				}
				// Modify the Style of the paragraph
				copyAttrs = new SimpleAttributeSet(paragraphElement.getAttributes());
				startOffset = paragraphElement.getStartOffset();
				endOffset = paragraphElement.getEndOffset();

				theSDoc.setParagraphAttributes(startOffset, (endOffset - startOffset), copyAttrs, true);
			}
			try
			{
				theSDoc.remove(pos - 1, 1);
			}
			catch (BadLocationException e)
			{
				System.out.println("Didn't work");
			}
		}

	}
	
	private class MyTextPaneSelection implements Transferable, ClipboardOwner
	{
		private String dataString;
		private DefaultStyledDocument dataDoc;

//		public DataFlavor DSDocFlavor = new DataFlavor(DefaultStyledDocument.class, "DSDocFlavor");
//		private DataFlavor rtfFlavor = new DataFlavor("text/rtf; charset=ISO-8859-1", "Rich Text Format");

		 private DataFlavor[] supportedFlavors = { DSDocFlavor, DataFlavor.stringFlavor };
		// private DataFlavor [] supportedFlavors = { DSDocFlavor, rtfFlavor };
		// private DataFlavor [] supportedFlavors = { DSDocFlavor,  DataFlavor.stringFlavor, rtfFlavor };

		// Usefull for Dummy StringTransferSelection Object
		public MyTextPaneSelection(String dataString)
		{
			System.out.println("Constructeur StringTransferSelection avec String");
			this.dataString = dataString;
		}

		public MyTextPaneSelection(DefaultStyledDocument dataDoc)
		{
			System.out.println("Constructeur StringTransferSelection avec DSDoc");
			this.dataDoc = dataDoc;
			try
			{
				dataString = dataDoc.getText(0, dataDoc.getLength());
			}
			catch (BadLocationException e)
			{
				// It won't happen
			}
		}

		@Override
		public DataFlavor[] getTransferDataFlavors()
		{
			System.out.println("method getTransferDataFlavors");
			return supportedFlavors;
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor)
		{
			// System.out.println("method isDataFlavorSupported");
			
			System.out.println(String.format("isDataFlavorSupported : %s ", flavor));
			
			if (flavor.equals(DataFlavor.stringFlavor))
			{
				return true;
			}
			else if (flavor.equals(rtfFlavor))
			{
				return true;
			}
			else if (flavor.equals(DSDocFlavor))
			{
				return true;
			}
			return false;
		}

		// Throw a weird exception if the part with DataFlavor.stringFlavor is not
		// there (FIX)
		// When you create the Transferable the method getTransferDataFlavors is
		// called
		// then getTransferData is called for every type in supportedFlavors
		// It must be some inner working of the Clipboard
		@Override
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException
		{
			System.out.println("method getTransferData");
			if (flavor.equals(DataFlavor.stringFlavor))
			{
				return dataString;
			}
			else if (flavor.equals(DSDocFlavor))
			{
				return dataDoc;
			}
			else
			{
				throw new UnsupportedFlavorException(flavor);
			}
		}

		@Override
		public void lostOwnership(Clipboard clipboard, Transferable contents)
		{
			System.out.println("method lostOwnership");
		}
	}
	
	
	
	
}
