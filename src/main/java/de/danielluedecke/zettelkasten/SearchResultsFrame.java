/*
 * Zettelkasten - nach Luhmann
 * Copyright (C) 2001-2015 by Daniel Lüdecke (http://www.danielluedecke.de)
 * 
 * Homepage: http://zettelkasten.danielluedecke.de
 * 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of 
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see <http://www.gnu.org/licenses/>.
 * 
 * 
 * Dieses Programm ist freie Software. Sie können es unter den Bedingungen der GNU
 * General Public License, wie von der Free Software Foundation veröffentlicht, weitergeben
 * und/oder modifizieren, entweder gemäß Version 3 der Lizenz oder (wenn Sie möchten)
 * jeder späteren Version.
 * 
 * Die Veröffentlichung dieses Programms erfolgt in der Hoffnung, daß es Ihnen von Nutzen sein 
 * wird, aber OHNE IRGENDEINE GARANTIE, sogar ohne die implizite Garantie der MARKTREIFE oder 
 * der VERWENDBARKEIT FÜR EINEN BESTIMMTEN ZWECK. Details finden Sie in der 
 * GNU General Public License.
 * 
 * Sie sollten ein Exemplar der GNU General Public License zusammen mit diesem Programm 
 * erhalten haben. Falls nicht, siehe <http://www.gnu.org/licenses/>.
 */

package de.danielluedecke.zettelkasten;

import de.danielluedecke.zettelkasten.data.SearchResultsFrameData;
import com.explodingpixels.macwidgets.BottomBar;
import com.explodingpixels.macwidgets.BottomBarSize;
import de.danielluedecke.zettelkasten.database.*;
import de.danielluedecke.zettelkasten.mac.MacSourceList;
import de.danielluedecke.zettelkasten.util.Tools;
import de.danielluedecke.zettelkasten.util.Constants;
import de.danielluedecke.zettelkasten.util.classes.DateComparer;
import de.danielluedecke.zettelkasten.util.classes.Comparer;
import com.explodingpixels.macwidgets.MacUtils;
import com.explodingpixels.macwidgets.MacWidgetFactory;
import com.explodingpixels.macwidgets.UnifiedToolBar;
import com.explodingpixels.widgets.TableUtils;
import com.explodingpixels.widgets.WindowUtils;
import de.danielluedecke.zettelkasten.database.BibTeX;
import de.danielluedecke.zettelkasten.mac.MacToolbarButton;
import de.danielluedecke.zettelkasten.mac.ZknMacWidgetFactory;
import de.danielluedecke.zettelkasten.settings.AcceleratorKeys;
import de.danielluedecke.zettelkasten.settings.Settings;
import de.danielluedecke.zettelkasten.tasks.TaskProgressDialog;
import de.danielluedecke.zettelkasten.util.ColorUtil;
import de.danielluedecke.zettelkasten.util.HtmlUbbUtil;
import de.danielluedecke.zettelkasten.util.PlatformUtil;
import java.awt.AWTKeyStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.IllegalComponentStateException;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.border.MatteBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.AttributeSet;
import javax.swing.text.html.HTML;
import org.jdesktop.application.Action;
import org.jdesktop.application.Application;

/**
 *
 * @author danielludecke
 */
public class SearchResultsFrame extends javax.swing.JFrame {

	/**
	 * Returns the table component of the search results window.
	 * 
	 * @return the table component of the search results window.
	 */
	public JTable getSearchFrameTable() {
		return jTableResults;
	}

	/**
	 * 
	 * @param zkn
	 * @param d
	 * @param sr
	 * @param desk
	 * @param s
	 * @param ak
	 * @param syn
	 * @param bib
	 */
	public SearchResultsFrame(ZettelkastenView zkn, Daten d, SearchRequests sr, DesktopData desk, Settings s,
			AcceleratorKeys ak, Synonyms syn, BibTeX bib) {
		data.setSearchFrame(this);
		// init variables from parameters
		data.setData(d);
		data.setDesktopData(desk);
		data.setBibTeX(bib);
		data.setSearchRequests(sr);
		data.setSynonyms(syn);
		data.setAcceleratorKeys(ak);
		data.setSettings(s);
		data.setMainFrame(zkn);
		// check whether memory usage is logged. if so, tell logger that new entry
		// windows was opened
		if (data.getSettings().isMemoryUsageLogged) {
			// log info
			Constants.zknlogger.log(Level.INFO, "Memory usage logged. Search Results Window opened.");
		}
		// create brushed look for window, so toolbar and window-bar become a unit
		if (data.getSettings().isMacStyle()) {
			MacUtils.makeWindowLeopardStyle(getRootPane());
			// WindowUtils.createAndInstallRepaintWindowFocusListener(this);
			WindowUtils.installJComponentRepainterOnWindowFocusChanged(this.getRootPane());
		}
		// init all components
		Tools.initLocaleForDefaultActions(
				org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class)
						.getContext().getActionMap(SearchResultsFrame.class, this));
		initComponents();
		initListeners();
		// remove border, gui-builder doesn't do this
		initBorders(data.getSettings());
		// set application icon
		setIconImage(Constants.zknicon.getImage());
		// if we have mac os x with aqua, make the window look like typical
		// cocoa-applications
		if (data.getSettings().isMacStyle()) {
			setupMacOSXLeopardStyle();
		}
		if (data.getSettings().isSeaGlass()) {
			setupSeaGlassStyle();
		}
		// init toggle-items
		viewMenuHighlight.setSelected(data.getSettings().getHighlightSearchResults());
		tb_highlight.setSelected(data.getSettings().getHighlightSearchResults());
		viewMenuShowEntry.setSelected(data.getSettings().getShowSearchEntry());
		jButtonResetList.setEnabled(false);
		// init table
		initTable();
		// init combobox. The automatic display-update should be managed
		// through the combobox's action listener
		initComboBox();
		// init the menu-accelerator table
		initAcceleratorTable();
		initActionMaps();
		// This method initialises the toolbar buttons. depending on the user-setting,
		// we either
		// display small, medium or large icons as toolbar-icons.
		initToolbarIcons();
		// init default sont-sizes
		initDefaultFontSize();
		// and update the title
		updateTitle();
	}

	/**
	 *
	 */
	public final void updateTitle() {
		String currentTitle = getTitle();
		// get filename and find out where extension begins, so we can just set the
		// filename as title
		File f = data.getSettings().getMainDataFile();
		// check whether we have any valid filepath at all
		if (f != null && f.exists()) {
			String fname = f.getName();
			// find file-extension
			int extpos = fname.lastIndexOf(Constants.ZKN_FILEEXTENSION);
			// set the filename as title
			if (extpos != -1) {
				// show proxy-icon, only applies to mac.
				getRootPane().putClientProperty("Window.documentFile", f);
				// set file-name and app-name in title-bar
				setTitle(currentTitle + "- [" + fname.substring(0, extpos) + "]");
			}
		}
	}

	private void initBorders(Settings settingsObj) {
		/*
		 * Constructor for Matte Border public MatteBorder(int top, int left, int
		 * bottom, int right, Color matteColor)
		 */
		jScrollPane1.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ColorUtil.getBorderGray(settingsObj)));
		jScrollPane4.setBorder(null);
		if (settingsObj.getUseMacBackgroundColor() || settingsObj.isMacStyle()) {
			jListKeywords.setBackground(
					(settingsObj.isMacStyle()) ? ColorUtil.colorJTreeBackground : ColorUtil.colorJTreeLighterBackground);
			jListKeywords.setForeground(ColorUtil.colorJTreeDarkText);
		}
		if (settingsObj.isSeaGlass()) {
			jPanel3.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, ColorUtil.getBorderGray(settingsObj)));
			jPanel4.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, ColorUtil.getBorderGray(settingsObj)));
			jListKeywords.setBorder(ZknMacWidgetFactory
					.getTitledBorder(data.getResourceMap().getString("jListKeywords.border.title"), settingsObj));
			if (settingsObj.getSearchFrameSplitLayout() == JSplitPane.HORIZONTAL_SPLIT) {
				jPanel1.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, ColorUtil.getBorderGray(settingsObj)));
				jPanel2.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, ColorUtil.getBorderGray(settingsObj)));
			} else {
				jPanel1.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ColorUtil.getBorderGray(settingsObj)));
				jPanel2.setBorder(BorderFactory.createMatteBorder(1, 1, 0, 0, ColorUtil.getBorderGray(settingsObj)));
			}
			// jPanel3.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1,
			// ColorUtil.getBorderGray(settingsObj)));
		}
		if (settingsObj.isMacStyle()) {
			ZknMacWidgetFactory.updateSplitPane(jSplitPaneSearch1);
			ZknMacWidgetFactory.updateSplitPane(jSplitPaneSearch2);
			jListKeywords.setBorder(ZknMacWidgetFactory.getTitledBorder(
					data.getResourceMap().getString("jListKeywords.border.title"), ColorUtil.colorJTreeText, settingsObj));
		}
	}

	/**
	 * This method initialises the toolbar buttons. depending on the user-setting,
	 * we either display small, medium or large icons as toolbar-icons.
	 */
	public final void initToolbarIcons() {
		// check whether the toolbar should be displayed at all...
		if (!data.getSettings().getShowIcons() && !data.getSettings().getShowIconText()) {
			// if not, hide it and leave.
			searchToolbar.setVisible(false);
			// and set a border to the main panel, because the toolbar's dark border is
			// hidden
			// and remove border from the main panel
			searchMainPanel.setBorder(new MatteBorder(1, 0, 0, 0, ColorUtil.colorDarkLineGray));
			return;
		}
		// set toolbar visible
		searchToolbar.setVisible(true);
		// and remove border from the main panel
		searchMainPanel.setBorder(null);
		// init toolbar button array
		javax.swing.JButton toolbarButtons[] = new javax.swing.JButton[] { tb_copy, tb_selectall, tb_editentry,
				tb_remove, tb_manlinks, tb_luhmann, tb_bookmark, tb_desktop, tb_highlight };
		String[] buttonNames = new String[] { "tb_copyText", "tb_selectallText", "tb_editText", "tb_deleteText",
				"tb_addmanlinksText", "tb_addluhmannText", "tb_addbookmarkText", "tb_addtodesktopText",
				"tb_highlightText" };
		String[] iconNames = new String[] { "copyIcon", "selectAllIcon", "editEntryIcon", "deleteIcon",
				"addManLinksIcon", "addLuhmannIcon", "addBookmarksIcon", "addDesktopIcon", "highlightKeywordsIcon" };
		// set toolbar-icons' text
		if (data.getSettings().getShowIconText()) {
			for (int cnt = 0; cnt < toolbarButtons.length; cnt++) {
				toolbarButtons[cnt].setText(data.getToolbarResourceMap().getString(buttonNames[cnt]));
			}
		} else {
			for (javax.swing.JButton tbb : toolbarButtons) {
				tbb.setText("");
			}
		}
		// show icons, if requested
		if (data.getSettings().getShowIcons()) {
			// retrieve icon theme path
			String icontheme = data.getSettings().getIconThemePath();
			for (int cnt = 0; cnt < toolbarButtons.length; cnt++) {
				toolbarButtons[cnt].setIcon(new ImageIcon(
						ZettelkastenView.class.getResource(icontheme + data.getToolbarResourceMap().getString(iconNames[cnt]))));
			}
		} else {
			for (javax.swing.JButton tbb : toolbarButtons) {
				tbb.setIcon(null);
			}
		}
		if (data.getSettings().isMacStyle())
			makeMacToolBar();
		if (data.getSettings().isSeaGlass())
			makeSeaGlassToolbar();
	}

	private void setupSeaGlassStyle() {
		getRootPane().setBackground(ColorUtil.colorSeaGlassGray);
		jTextFieldFilterList.putClientProperty("JTextField.variant", "search");
		jEditorPaneSearchEntry.setBackground(Color.white);
		jButtonDeleteSearch.setBorderPainted(true);
		jButtonDeleteSearch.putClientProperty("JButton.buttonType", "textured");
	}

	/**
	 * This method applies some graphical stuff so the appearance of the program is
	 * even more mac-like...
	 */
	private void setupMacOSXLeopardStyle() {

		jTextFieldFilterList.putClientProperty("JTextField.variant", "search");
		MacWidgetFactory.makeEmphasizedLabel(jLabel1);
		MacWidgetFactory.makeEmphasizedLabel(jLabelHits);
	}

	private void makeSeaGlassToolbar() {
		Tools.makeTexturedToolBarButton(tb_copy, Tools.SEGMENT_POSITION_FIRST);
		Tools.makeTexturedToolBarButton(tb_selectall, Tools.SEGMENT_POSITION_LAST);
		Tools.makeTexturedToolBarButton(tb_editentry, Tools.SEGMENT_POSITION_FIRST);
		Tools.makeTexturedToolBarButton(tb_remove, Tools.SEGMENT_POSITION_LAST);
		Tools.makeTexturedToolBarButton(tb_manlinks, Tools.SEGMENT_POSITION_FIRST);
		Tools.makeTexturedToolBarButton(tb_luhmann, Tools.SEGMENT_POSITION_MIDDLE);
		if (data.getSettings().getShowAllIcons()) {
			Tools.makeTexturedToolBarButton(tb_bookmark, Tools.SEGMENT_POSITION_MIDDLE);
			Tools.makeTexturedToolBarButton(tb_desktop, Tools.SEGMENT_POSITION_LAST);
		} else {
			Tools.makeTexturedToolBarButton(tb_bookmark, Tools.SEGMENT_POSITION_LAST);
		}
		Tools.makeTexturedToolBarButton(tb_highlight, Tools.SEGMENT_POSITION_ONLY);
		searchToolbar.setPreferredSize(
				new java.awt.Dimension(searchToolbar.getSize().width, Constants.seaGlassToolbarHeight));
		searchToolbar.add(new javax.swing.JToolBar.Separator(), 0);
	}

	private void makeMacToolBar() {
		// hide default toolbr
		searchToolbar.setVisible(false);
		this.remove(searchToolbar);
		// and create mac toolbar
		if (data.getSettings().getShowIcons() || data.getSettings().getShowIconText()) {

			UnifiedToolBar mactoolbar = new UnifiedToolBar();

			mactoolbar.addComponentToLeft(
					MacToolbarButton.makeTexturedToolBarButton(tb_copy, MacToolbarButton.SEGMENT_POSITION_FIRST));
			mactoolbar.addComponentToLeft(
					MacToolbarButton.makeTexturedToolBarButton(tb_selectall, MacToolbarButton.SEGMENT_POSITION_LAST));
			mactoolbar.addComponentToLeft(MacWidgetFactory.createSpacer(16, 1));
			mactoolbar.addComponentToLeft(
					MacToolbarButton.makeTexturedToolBarButton(tb_editentry, MacToolbarButton.SEGMENT_POSITION_FIRST));
			mactoolbar.addComponentToLeft(
					MacToolbarButton.makeTexturedToolBarButton(tb_remove, MacToolbarButton.SEGMENT_POSITION_LAST));
			mactoolbar.addComponentToLeft(MacWidgetFactory.createSpacer(16, 1));
			mactoolbar.addComponentToLeft(
					MacToolbarButton.makeTexturedToolBarButton(tb_manlinks, MacToolbarButton.SEGMENT_POSITION_FIRST));
			mactoolbar.addComponentToLeft(
					MacToolbarButton.makeTexturedToolBarButton(tb_luhmann, MacToolbarButton.SEGMENT_POSITION_MIDDLE));
			if (data.getSettings().getShowAllIcons()) {
				mactoolbar.addComponentToLeft(MacToolbarButton.makeTexturedToolBarButton(tb_bookmark,
						MacToolbarButton.SEGMENT_POSITION_MIDDLE));
				mactoolbar.addComponentToLeft(
						MacToolbarButton.makeTexturedToolBarButton(tb_desktop, MacToolbarButton.SEGMENT_POSITION_LAST));
			} else {
				mactoolbar.addComponentToLeft(MacToolbarButton.makeTexturedToolBarButton(tb_bookmark,
						MacToolbarButton.SEGMENT_POSITION_LAST));
			}
			mactoolbar.addComponentToLeft(MacWidgetFactory.createSpacer(16, 1));
			mactoolbar.addComponentToLeft(
					MacToolbarButton.makeTexturedToolBarButton(tb_highlight, MacToolbarButton.SEGMENT_POSITION_ONLY));

			mactoolbar.installWindowDraggerOnWindow(this);
			searchMainPanel.add(mactoolbar.getComponent(), BorderLayout.PAGE_START);
		}
		makeMacBottomBar();
	}

	private void makeMacBottomBar() {
		jPanel9.setVisible(false);

		BottomBar macbottombar = new BottomBar(BottomBarSize.LARGE);
		macbottombar.addComponentToLeft(MacWidgetFactory.makeEmphasizedLabel(jLabelHits), 20);
		macbottombar.addComponentToLeft(MacWidgetFactory.makeEmphasizedLabel(jLabel1), 4);
		macbottombar.addComponentToLeft(jComboBoxSearches, 4);
		macbottombar.addComponentToLeft(jButtonDeleteSearch, 4);

		jButtonDeleteSearch.setBorderPainted(true);
		jButtonDeleteSearch.putClientProperty("JButton.buttonType", "textured");

		searchStatusPanel.remove(jPanel9);
		searchStatusPanel.setBorder(null);
		searchStatusPanel.setLayout(new BorderLayout());
		searchStatusPanel.add(macbottombar.getComponent(), BorderLayout.PAGE_START);
	}

	/**
	 * This method sets the default font-size for tables, lists and treeviews. If
	 * the user wants to have bigger font-sizes for better viewing, the new
	 * font-size will be applied to the components here.
	 */
	private void initDefaultFontSize() {
        Font settingsTableFont = data.getSettings().getTableFont();
		jTableResults.setFont(settingsTableFont);
		jListKeywords.setFont(settingsTableFont);
	}

	private void initListeners() {
		// these codelines add an escape-listener to the dialog. so, when the user
		// presses the escape-key, the same action is performed as if the user
		// presses the cancel button...
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener cancelAction = new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				quitFullScreen();
			}
		};
		getRootPane().registerKeyboardAction(cancelAction, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		// these codelines add an escape-listener to the dialog. so, when the user
		// presses the escape-key, the same action is performed as if the user
		// presses the cancel button...
		stroke = KeyStroke.getKeyStroke(data.getAcceleratorKeys().getAcceleratorKey(AcceleratorKeys.MAINKEYS, "showDesktopWindow"));
		ActionListener showDesktopWindowAction = new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				data.getMainFrame().showDesktopWindow();
			}
		};
		getRootPane().registerKeyboardAction(showDesktopWindowAction, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		// these codelines add an escape-listener to the dialog. so, when the user
		// presses the escape-key, the same action is performed as if the user
		// presses the cancel button...
		stroke = KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0);
		ActionListener showMainFrameAction = new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				data.getMainFrame().bringToFront();
			}
		};
		getRootPane().registerKeyboardAction(showMainFrameAction, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		// these codelines add an escape-listener to the dialog. so, when the user
		// presses the escape-key, the same action is performed as if the user
		// presses the cancel button...
		stroke = KeyStroke.getKeyStroke(data.getAcceleratorKeys().getAcceleratorKey(AcceleratorKeys.MAINKEYS, "showNewEntryWindow"));
		ActionListener showNewEntryFrameAction = new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				data.getMainFrame().showNewEntryWindow();
			}
		};
		getRootPane().registerKeyboardAction(showNewEntryFrameAction, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		searchSearchMenu.addMenuListener(new javax.swing.event.MenuListener() {
			@Override
			public void menuSelected(javax.swing.event.MenuEvent evt) {
				setListSelected(jListKeywords.getSelectedIndex() != -1);
				String t1 = jEditorPaneSearchEntry.getSelectedText();
				setTextSelected(t1 != null && !t1.isEmpty());
			}

			@Override
			public void menuDeselected(javax.swing.event.MenuEvent evt) {
			}

			@Override
			public void menuCanceled(javax.swing.event.MenuEvent evt) {
			}
		});
		jEditorPaneSearchEntry.addHyperlinkListener(new javax.swing.event.HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {
				// get input event with additional modifiers
				java.awt.event.InputEvent inev = evt.getInputEvent();
				// check whether shift key was pressed, and if so, remove manual link
				if (inev.isControlDown() || inev.isMetaDown()) {
					// get selected entry
					int row = jTableResults.getSelectedRow();
					// when we have a valid selection, go on
					if (row != -1) {
						int displayedZettel = Integer.parseInt(jTableResults.getValueAt(row, 0).toString());
						if (Tools.removeHyperlink(evt.getDescription(), data.getData(), displayedZettel)) {
							data.getMainFrame().updateDisplay();
						}
					}
				} else if (evt.getEventType() == HyperlinkEvent.EventType.ENTERED) {
					javax.swing.text.Element elem = evt.getSourceElement();
					if (elem != null) {
						AttributeSet attr = elem.getAttributes();
						AttributeSet a = (AttributeSet) attr.getAttribute(HTML.Tag.A);
						if (a != null) {
							jEditorPaneSearchEntry.setToolTipText((String) a.getAttribute(HTML.Attribute.TITLE));
						}
					}
				} else if (evt.getEventType() == HyperlinkEvent.EventType.EXITED) {
					jEditorPaneSearchEntry.setToolTipText(null);
				} else {
					openAttachment(evt);
				}
			}
		});
		jTableResults.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				// this listener should only react on left-mouse-button-clicks...
				// if other button then left-button clicked, don't count it.
				if (evt.getButton() != MouseEvent.BUTTON1)
					return;
				// only show entry on double clicl
				if (2 == evt.getClickCount())
					displayEntryInMainframe();
			}
		});
		jTextFieldFilterList.addKeyListener(new java.awt.event.KeyAdapter() {
			@Override
			public void keyReleased(java.awt.event.KeyEvent evt) {
				if (Tools.isNavigationKey(evt.getKeyCode())) {
					// if user pressed navigation key, select next table entry
					de.danielluedecke.zettelkasten.util.TableUtils.navigateThroughList(jTableResults, evt.getKeyCode());
				} else {
					// select table-entry live, while the user is typing...
					de.danielluedecke.zettelkasten.util.TableUtils.selectByTyping(jTableResults, jTextFieldFilterList,
							1);
				}
			}
		});
		//
		// Now come the mouse-listeners
		//
		// here we set up a popup-trigger for the jListEntryKeywords and how this
		// component
		// should react on mouse-clicks. a single click filters the jTableLinks, a
		// double-click
		// starts a keyword-search
		jListKeywords.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				// this listener should only react on left-mouse-button-clicks...
				// if other button then left-button clicked, leeave...
				if (evt.getButton() != MouseEvent.BUTTON1)
					return;
				// on double click
				if (2 == evt.getClickCount()) {
					if (jListKeywords.getSelectedIndex() != -1)
						newSearchFromKeywordsLogOr();
				}
				// on single click...
				if (1 == evt.getClickCount()) {
					highlightSegs();
				}
			}
		});
		jListKeywords.addKeyListener(new java.awt.event.KeyAdapter() {
			@Override
			public void keyReleased(java.awt.event.KeyEvent evt) {
				// if a navigation-key (arrows, page-down/up, home etc.) is pressed,
				// we assume a new item-selection, so behave like on a mouse-click and
				// filter the links
				if (Tools.isNavigationKey(evt.getKeyCode())) {
					highlightSegs();
				}
			}
		});
	}

	/**
	 * This method inits the action map for several components like the tables, the
	 * treeviews or the lists. here we can associate certain keystrokes with related
	 * methods. e.g. hitting the enter-key in a table shows (activates) the related
	 * entry. <br>
	 * <br>
	 * Setting up action maps gives a better overview and is shorter than adding
	 * key-release-events to all components, although key-events would fulfill the
	 * same purpose. <br>
	 * <br>
	 * The advantage of action maps is, that dependent from the operating system we
	 * need only to associte a single action. with key-events, for each component we
	 * have to check whether the operating system is mac os or windows, and then
	 * checking for different keys, thus doubling each command: checking for F2 to
	 * edit, or checking for command+enter and also call the edit-method. using
	 * action maps, we simply as for the os once, storing the related
	 * keystroke-value as string, and than assign this string-value to the
	 * components.
	 */
	private void initActionMaps() {
		// <editor-fold defaultstate="collapsed" desc="Init of action-maps so we have
		// shortcuts for the tables">
		// create action which should be executed when the user presses
		// the enter-key
		AbstractAction a_enter = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (jTextFieldFilterList == e.getSource())
					filterResultList();
				if (jTableResults == e.getSource())
					displayEntryInMainframe();
			}
		};
		// put action to the tables' actionmaps
		jTextFieldFilterList.getActionMap().put("EnterKeyPressed", a_enter);
		jTableResults.getActionMap().put("EnterKeyPressed", a_enter);
		// associate enter-keystroke with that action
		KeyStroke ks = KeyStroke.getKeyStroke("ENTER");
		jTextFieldFilterList.getInputMap().put(ks, "EnterKeyPressed");
		jTableResults.getInputMap().put(ks, "EnterKeyPressed");
		// </editor-fold>
	}

	private void highlightSegs() {
		// and highlight text segments
		if (data.getSettings().getHighlightSegments()) {
			int[] selectedValues = getSelectedEntriesFromTable();
			if (selectedValues != null && selectedValues.length > 0) {
				displayZettelContent(selectedValues[0], null);
			}
		}
	}

	/**
	 * This method sets the accelerator table for all relevant actions which should
	 * have accelerator keys. We don't use the GUI designer to set the values,
	 * because the user should have the possibility to define own accelerator keys,
	 * which are managed within the CAcceleratorKeys-class and loaed/saved via the
	 * CSettings-class
	 */
	private void initAcceleratorTable() {
		// setting up the accelerator table. we have two possibilities: either assigning
		// accelerator keys directly with an action like this:
		//
		// javax.swing.ActionMap actionMap =
		// org.jdesktop.application.Application.getInstance(zettelkasten.ZettelkastenApp.class).getContext().getActionMap(ZettelkastenView.class,
		// this);
		// AbstractAction ac = (AbstractAction) actionMap.get("newEntry");
		// KeyStroke controlN = KeyStroke.getKeyStroke("control N");
		// ac.putValue(AbstractAction.ACCELERATOR_KEY, controlN);
		//
		// or setting the accelerator key directly to a menu-item like this:
		//
		// newEntryMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
		// InputEvent.META_MASK));
		//
		// we choose the first option, because so we can easily iterate through the xml
		// file
		// and retrieve action names as well as accelerator keys. this saves a lot of
		// typing work here
		//
		// get the action map
		javax.swing.ActionMap actionMap = org.jdesktop.application.Application
				.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getContext()
				.getActionMap(SearchResultsFrame.class, this);
		// iterate the xml file with the accelerator keys for the main window
		for (int cnt = 1; cnt <= data.getAcceleratorKeys().getCount(AcceleratorKeys.SEARCHRESULTSKEYS); cnt++) {
			// get the action's name
			String actionname = data.getAcceleratorKeys().getAcceleratorAction(AcceleratorKeys.SEARCHRESULTSKEYS, cnt);
			// check whether we have found any valid action name
			if (actionname != null && !actionname.isEmpty()) {
				// retrieve action
				AbstractAction ac = (AbstractAction) actionMap.get(actionname);
				// get the action's accelerator key
				String actionkey = data.getAcceleratorKeys().getAcceleratorKey(AcceleratorKeys.SEARCHRESULTSKEYS, cnt);
				// check whether we have any valid actionkey
				if (actionkey != null && !actionkey.isEmpty()) {
					// retrieve keystroke setting
					KeyStroke ks = KeyStroke.getKeyStroke(actionkey);
					// and put them together :-)
					ac.putValue(AbstractAction.ACCELERATOR_KEY, ks);
				}
			}
		}
		// now set the mnemonic keys of the menus (i.e. the accelerator keys, which give
		// access
		// to the menu via "alt"+key). since the menus might have different texts,
		// depending on
		// the programs language, we retrieve the menu text and simply set the first
		// char
		// as mnemonic key
		// ATTENTION! Mnemonic keys are NOT applied on Mac OS, see Apple guidelines for
		// further details:
		// http://developer.apple.com/DOCUMENTATION/Java/Conceptual/Java14Development/07-NativePlatformIntegration/NativePlatformIntegration.html#//apple_ref/doc/uid/TP40001909-211867-BCIBDHFJ
		if (!data.getSettings().isMacStyle()) {
			// init the variables
			String menutext;
			char mkey;
			// the mnemonic key for the file menu
			menutext = searchFileMenu.getText();
			mkey = menutext.charAt(0);
			searchFileMenu.setMnemonic(mkey);
			// the mnemonic key for the edit menu
			menutext = searchEditMenu.getText();
			mkey = menutext.charAt(0);
			searchEditMenu.setMnemonic(mkey);
			// the mnemonic key for the filter menu
			menutext = searchFilterMenu.getText();
			mkey = menutext.charAt(0);
			searchFilterMenu.setMnemonic(mkey);
			// the mnemonic key for the search menu
			menutext = searchSearchMenu.getText();
			mkey = menutext.charAt(0);
			searchSearchMenu.setMnemonic(mkey);
			// the mnemonic key for the view menu
			menutext = searchViewMenu.getText();
			mkey = menutext.charAt(0);
			searchViewMenu.setMnemonic(mkey);
		}
		// on macOS, at least for the German locale, the File menu is called different
		// compared to windows or Linux. Furthermore, we don't need the about and
		// preferences
		// menu items, since these are locates on the program's menu item in the
		// apple-menu-bar
		if (PlatformUtil.isMacOS())
			searchFileMenu.setText(data.getResourceMap().getString("macFileMenuText"));
		// en- or disable full screen icons
		setFullScreenSupp(data.getGraphicDevice().isFullScreenSupported());
		// if full screen is not supported, tell this in the tooltip
		if (!data.getGraphicDevice().isFullScreenSupported()) {
			AbstractAction ac = (AbstractAction) actionMap.get("viewFullScreen");
			ac.putValue(AbstractAction.SHORT_DESCRIPTION, data.getResourceMap().getString("fullScreenNotSupported"));
		}
	}

	/**
	 * This option toggles the setting, whether a selected entry from the search
	 * results should also immediately be displayed in the main frame or not.
	 */
	@Action
	public void showEntryImmediately() {
		Constants.zknlogger.info(data.toString());
		data.getSettings().setShowSearchEntry(!data.getSettings().getShowSearchEntry());
	}

	@Action
	public void resetResultslist() {
		prepareResultList(jComboBoxSearches.getSelectedIndex());
		// set input focus to the table, so key-navigation can start immediately
		jTableResults.requestFocusInWindow();
		// finally, select first entry
		try {
			jTableResults.setRowSelectionInterval(0, 0);
		} catch (IllegalArgumentException e) {
			Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
		}
		// enable refresh button
		jButtonResetList.setEnabled(false);
	}

	private void filterResultList() {
		// when we filter the table and want to restore it, we don't need to run the
		// time-consuming task that creates the author-list and related
		// author-frequencies.
		// instead, we simply copy the values from the linkedlist to the table-model,
		// which is
		// much faster. but therefore we have to apply all changes to the filtered-table
		// (like adding/changing values in a filtered list) to the linked list as well.

		// get text from the textfield containing the filter string
		// convert to lowercase, we don't want case-sensitive search
		String text = jTextFieldFilterList.getText().toLowerCase();
		// tell selection listener to do nothing...
		data.setTableUpdateActive(true);
		// when we have no text, do nothing
		if (!text.isEmpty()) {
			// get table model
			DefaultTableModel dtm = (DefaultTableModel) jTableResults.getModel();
			// go through table and delete all rows that don't contain the filter text
			for (int cnt = (jTableResults.getRowCount() - 1); cnt >= 0; cnt--) {
				// retrieve row-index from the model
				int rowindex = jTableResults.convertRowIndexToModel(cnt);
				// get the string (author) value from the table
				// convert to lowercase, we don't want case-sensitive search
				String value = dtm.getValueAt(rowindex, 1).toString().toLowerCase();
				// in case we have the jTableTitles, we also add the timestamps and
				// rating-values to the filter-value
				// so we can also filter entries according to their timestamp
				value = value + dtm.getValueAt(rowindex, 2).toString() + dtm.getValueAt(rowindex, 3).toString()
						+ dtm.getValueAt(rowindex, 4).toString();
				// check for regex pattern
				if (text.contains("?")) {
					try {
						// replace all "?" into .
						String dummy = text.replace("?", ".");
						// in case the user wanted to search for ?, replace \. into \?.
						dummy = dummy.replace("\\.", "\\?").toLowerCase();
						// create regex pattern
						Pattern pattern = Pattern.compile(dummy);
						// now check whether pattern exists in value
						Matcher matcher = pattern.matcher(value);
						// if the text is *not* part of the column, delete that row
						if (!matcher.find()) {
							dtm.removeRow(rowindex);
						}
					} catch (PatternSyntaxException ex) {
						// in case of invalid regex, simply try to find the usual pattern
						if (!value.contains(text))
							dtm.removeRow(rowindex);
					}
				}
				// if the text is *not* part of the column, delete that row
				else if (!value.contains(text))
					dtm.removeRow(rowindex);
			}
			// reset textfield
			jTextFieldFilterList.setText("");
			jTextFieldFilterList.requestFocusInWindow();
			// enable textfield only if we have more than 1 element in the jtable
			jTextFieldFilterList.setEnabled(jTableResults.getRowCount() > 0);
			// enable refresh button
			jButtonResetList.setEnabled(true);
			// create a new stringbuilder to prepare the label
			// that shows the amount of found entries
			StringBuilder sb = new StringBuilder("");
			sb.append("(");
			sb.append(String.valueOf(dtm.getRowCount()));
			sb.append(" ");
			sb.append(data.getResourceMap().getString("hitsText"));
			sb.append(")");
			// set labeltext
			jLabelHits.setText(sb.toString());
		}
		// tell selection listener action is possible again...
		data.setTableUpdateActive(false);
	}

	/**
	 * This option toggles the setting whether search terms should be highlighted or
	 * not.
	 */
	@Action
	public void toggleHighlightResults() {
		// check whether highlighting is activated
		if (!data.getSettings().getHighlightSearchResults()) {
			// if not, activate it
			data.getSettings().setHighlightSearchResults(true);
		} else {
			// nex, if highlighting is activated,
			// check whether whole word highlighting is activated
			if (!data.getSettings().getHighlightWholeWordSearch()) {
				// if not, activate whole-word-highlighting and do not
				// deactivate general highlighting
				data.getSettings().setHighlightWholeWordSearch(true);
			}
			// else if both were activated, deactivate all
			else {
				data.getSettings().setHighlightSearchResults(false);
				data.getSettings().setHighlightWholeWordSearch(false);
			}
		}
		updateDisplay();
	}

	@Action
	public void addKeywordsToEntries() {
		// create linked list as parameter for filter-dialog
		LinkedList<String> keywords = new LinkedList<>();
		// go through all keyword-entries
		for (int cnt = 1; cnt <= data.getData().getCount(Daten.KWCOUNT); cnt++) {
			// get keyword
			String k = data.getData().getKeyword(cnt);
			// add it to list
			if (!k.isEmpty())
				keywords.add(k);
		}
		// if dialog window isn't already created, do this now
		if (null == data.getFilterSearchDlg()) {
			// create a new dialog window
			data.setFilterSearchDlg(new CFilterSearch(this, data.getSettings(), keywords,
					data.getResourceMap().getString("addKeywordsToEntriesTitle"), false));
			// center window
			data.getFilterSearchDlg().setLocationRelativeTo(this);
		}
		ZettelkastenApp.getApplication().show(data.getFilterSearchDlg());
		// when we have any selected keywords, go on and add them all to all the
		// selected
		// entries in the search result
		if (data.getFilterSearchDlg().getFilterTerms() != null) {
			// get all selected entries
			int[] entries = getSelectedEntriesFromTable();
			// go through all selected entries
			// now iterate the chosen keywords
			// and add each keyword to all selected entries
			for (int e : entries)
				data.getData().addKeywordsToEntry(data.getFilterSearchDlg().getFilterTerms(), e, 1);
			// keyword-list is not up-to-date
			data.getData().setKeywordlistUpToDate(false);
			// update the display
			updateDisplay();
		}
		// dispose window...
		data.getFilterSearchDlg().dispose();
		data.setFilterSearchDlg(null);
	}

	@Action
	public void switchLayout() {
		int currentlayout = data.getSettings().getSearchFrameSplitLayout();
		if (JSplitPane.HORIZONTAL_SPLIT == currentlayout) {
			currentlayout = JSplitPane.VERTICAL_SPLIT;
			if (data.getSettings().isSeaGlass()) {
				jPanel1.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ColorUtil.getBorderGray(data.getSettings())));
				jPanel2.setBorder(BorderFactory.createMatteBorder(1, 1, 0, 0, ColorUtil.getBorderGray(data.getSettings())));
			}
		} else {
			currentlayout = JSplitPane.HORIZONTAL_SPLIT;
			if (data.getSettings().isSeaGlass()) {
				jPanel1.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, ColorUtil.getBorderGray(data.getSettings())));
				jPanel2.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, ColorUtil.getBorderGray(data.getSettings())));
			}
		}
		data.getSettings().setSearchFrameSplitLayout(currentlayout);
		jSplitPaneSearch1.setOrientation(currentlayout);
		if (data.getSettings().isMacStyle())
			ZknMacWidgetFactory.updateSplitPane(jSplitPaneSearch1);
	}

	@Action
	public void showEntryInDesktop() {
		// get selected row
		int row = jTableResults.getSelectedRow();
		// check for valid value
		if (row != -1) {
			try {
				int nr = Integer.parseInt(jTableResults.getValueAt(row, 0).toString());
				if (data.getDesktopData().isEntryInAnyDesktop(nr)) {
					data.getMainFrame().showEntryInDesktopWindow(nr);
				}
			} catch (NumberFormatException ex) {
			}
		}
	}

	@Action
	public void addAuthorsToEntries() {
		// create linked list as parameter for filter-dialog
		LinkedList<String> suthors = new LinkedList<>();
		// go through all author-entries
		for (int cnt = 1; cnt <= data.getData().getCount(Daten.AUCOUNT); cnt++) {
			// get authors
			String a = data.getData().getAuthor(cnt);
			// add it to list
			if (!a.isEmpty())
				suthors.add(a);
		}
		// if dialog window isn't already created, do this now
		if (null == data.getFilterSearchDlg()) {
			// create a new dialog window
			data.setFilterSearchDlg(new CFilterSearch(this, data.getSettings(), suthors,
					data.getResourceMap().getString("addAuthorsToEntriesTitle"), false));
			// center window
			data.getFilterSearchDlg().setLocationRelativeTo(this);
		}
		ZettelkastenApp.getApplication().show(data.getFilterSearchDlg());
		// when we have any selected keywords, go on and add them all to all the
		// selected
		// entries in the search result
		if (data.getFilterSearchDlg().getFilterTerms() != null) {
			// get all selected entries
			int[] entries = getSelectedEntriesFromTable();
			// go through all selected entries
			for (int e : entries) {
				// now iterate the chosen authors
				// and add each author to all selected entries
				for (String a : data.getFilterSearchDlg().getFilterTerms())
					data.getData().addAuthorToEntry(a, e, 1);
			}
			// author-list is not up-to-date
			data.getData().setAuthorlistUpToDate(false);
			// update the display
			updateDisplay();
		}
		// dispose window...
		data.getFilterSearchDlg().dispose();
		data.setFilterSearchDlg(null);
	}

	/**
	 * This method inits the combo-boxes, i.e. filling it with search-result-entries
	 * and setting up an action listener. The action-listener will update the
	 * jTableResults with the search-result-entrynumbers and update the display
	 * (filling the textfields).
	 */
	private void initComboBox() {
		// clear combobox
		jComboBoxSearches.removeAllItems();

		for (int cnt = 0; cnt < data.getSearchRequests().getCount(); cnt++) {
			jComboBoxSearches.addItem(data.getSearchRequests().getShortDescription(cnt));
		}
		// add action listener to combo box
		jComboBoxSearches.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				// set all results, i.e. all entry-numbers and the entries' titles, into the
				// search result table
				prepareResultList(jComboBoxSearches.getSelectedIndex());
				// and update the display, i.e. show the entry's content
				updateDisplay();
				// finally, select first entry
				try {
					jTableResults.setRowSelectionInterval(0, 0);
				} catch (IllegalArgumentException e) {
					Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
				}
				// set inputfocus to the table, so key-navigation can start immediately
				jTableResults.requestFocusInWindow();
			}
		});
		try {
			// select first item
			jComboBoxSearches.setSelectedIndex(0);
		} catch (IllegalArgumentException ex) {
			// log error
			Constants.zknlogger.log(Level.SEVERE, ex.getLocalizedMessage());
		}
	}

	/**
	 * This method initializes the table.<br>
	 * <br>
	 * - it puts the tab-key as new traversal-key<br>
	 * - sets the autosorter<br>
	 * - displayes the cellgrid<br>
	 * - implements action- and selection-listeners
	 */
	private void initTable() {
		// usually, the tab key selects the next cell in a jTable. here we override this
		// setting, changing the tab-key to change the focus.

		// bind our new forward focus traversal keys
		Set<AWTKeyStroke> newForwardKeys = new HashSet<>(1);
		newForwardKeys.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB, 0));
		jTableResults.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
				Collections.unmodifiableSet(newForwardKeys));
		// bind our new backward focus traversal keys
		Set<AWTKeyStroke> newBackwardKeys = new HashSet<>(1);
		newBackwardKeys
				.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB, KeyEvent.SHIFT_MASK + KeyEvent.SHIFT_DOWN_MASK));
		jTableResults.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
				Collections.unmodifiableSet(newBackwardKeys));
		// create new table sorter
		TableRowSorter<TableModel> sorter = new TableRowSorter<>();
		// tell tgis jtable that it has an own sorter
		jTableResults.setRowSorter(sorter);
		// and tell the sorter, which table model to sort.
		sorter.setModel((DefaultTableModel) jTableResults.getModel());
		// in this table, the first column needs a custom comparator.
		try {
			// sorter for titles
			sorter.setComparator(1, new Comparer());
			// sorter for desktop names
			sorter.setComparator(5, new Comparer());
			// this table has two more columns that should be sorted, the columns with
			// the entries timestamps.
			sorter.setComparator(2, new DateComparer());
			sorter.setComparator(3, new DateComparer());
		} catch (IndexOutOfBoundsException e) {
			Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
		}
		// get last table sorting
		RowSorter.SortKey sk = data.getSettings().getTableSorting(jTableResults);
		// any sorting found?
		if (sk != null) {
			// create array with sort key
			ArrayList<SortKey> l = new ArrayList<SortKey>();
			l.add(sk);
			// set sort key to table
			sorter.setSortKeys(l);
			// sort table
			sorter.sort();
		}
		// make extra table-sorter for itunes-tables
		if (data.getSettings().isMacStyle()) {
			TableUtils.SortDelegate sortDelegate = new TableUtils.SortDelegate() {
				@Override
				public void sort(int columnModelIndex, TableUtils.SortDirection sortDirection) {
				}
			};
			TableUtils.makeSortable(jTableResults, sortDelegate);
			// change back default column-resize-behaviour when we have itunes-tables,
			// since the default for those is "auto resize off"
			jTableResults.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		}
		jTableResults.setShowHorizontalLines(data.getSettings().getShowGridHorizontal());
		jTableResults.setShowVerticalLines(data.getSettings().getShowGridVertical());
		jTableResults.setIntercellSpacing(data.getSettings().getCellSpacing());
		jTableResults.getTableHeader().setReorderingAllowed(false);
		// if the user wants to see grids, we need to change the gridcolor on mac-aqua
		jTableResults.setGridColor(data.getSettings().getTableGridColor());
		SelectionListener listener = new SelectionListener(jTableResults);
		jTableResults.getSelectionModel().addListSelectionListener(listener);
		jTableResults.getColumnModel().getSelectionModel().addListSelectionListener(listener);
	}

	/**
	 * This method updates the combobox, when new search results are added or former
	 * search requests are deleted. therefor, we have to temporarily remove the
	 * action listener, because changing the combobox-content would fire several
	 * actions, which may interfer with our updating-process
	 * 
	 * @param selectedrow here we can pass a table row that should be selected after
	 *                    updating the combo-box. use "0" to select the first entry
	 *                    in the table, "-1" to select the last selection (if any)
	 *                    or any other value.
	 * @param searchnr    the number of the searchrequest that should be displayed.
	 *                    use "-1" to show the default search-request, which is
	 *                    either the currently used search-request, or - if it was
	 *                    deleted - the last search request. use any other number
	 *                    for a specific search request.
	 */
	public void updateComboBox(int selectedrow, int searchnr) {
		// init variable
		int selection;
		// check whether we have any parameter
		if (searchnr != -1)
			selection = searchnr;
		// remember current selection for later use, see below
		else
			selection = jComboBoxSearches.getSelectedIndex();
		// used for tablerowselection
		int row;
		// if we have a parameter for row-selection, set it here
		if (selectedrow != -1)
			row = selectedrow;
		// remember selected row...
		else
			row = jTableResults.getSelectedRow();
		// get all action listeners from the combo box
		ActionListener[] al = jComboBoxSearches.getActionListeners();
		// remove all action listeners so we don't fire several action-events
		// when we update the combo box. we can set the action listener later again
		for (ActionListener listener : al)
			jComboBoxSearches.removeActionListener(listener);
		// clear combobox
		jComboBoxSearches.removeAllItems();
		// add search descriptions to combobox
		for (int cnt = 0; cnt < data.getSearchRequests().getCount(); cnt++)
			jComboBoxSearches.addItem(data.getSearchRequests().getShortDescription(cnt));
		// add action listener to combo box
		jComboBoxSearches.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				// Set all results, i.e. all entry-numbers and the entries' titles, into the
				// search result table
				prepareResultList(jComboBoxSearches.getSelectedIndex());
				// and update the display, i.e. show the entry's content
				updateDisplay();
				// Set inputfocus to the table, so key-navigation can start immediately
				jTableResults.requestFocusInWindow();
				// finally, select first entry
				try {
					jTableResults.setRowSelectionInterval(0, 0);
				} catch (IllegalArgumentException e) {
					Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
				}
			}
		});
		// if we have any searchrequests at all, go on here
		if (data.getSearchRequests().getCount() > 0) {
			// check whether the last selected searchrequest is still available
			// if not, choose the last search request in the combobox...
			if (selection != data.getSearchRequests().getCurrentSearch())
				selection = jComboBoxSearches.getItemCount() - 1;
			// Select search request
			jComboBoxSearches.setSelectedIndex(selection);
			// if we had no prevous selection, set row-selector to first item.
			if (-1 == row)
				row = 0;
			// if the selected row was the last value, set row-counter to last row
			else if (row >= jTableResults.getRowCount())
				row = jTableResults.getRowCount() - 1;
			// finally...
			try {
				// Select the appropriate table-entry
				jTableResults.setRowSelectionInterval(row, row);
				// and make sure it is visible...
				jTableResults.scrollRectToVisible(jTableResults.getCellRect(row, 0, false));
			} catch (IllegalArgumentException e) {
				Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
			}
		}
		// make window invisible
		else {
			setVisible(false);
			// and disable hotkey
			data.getMainFrame().setSearchResultsAvailable(false);
		}
	}

	/**
	 * This method retrieves the result-entry-numbers from a search request "nr" and
	 * fills the jTableResult with those entry-numbers and the entries' related
	 * titles.
	 * 
	 * @param searchrequestnr the search request of which we want to display the
	 *                        search results.
	 */
	private void prepareResultList(int searchrequestnr) {
		// get search results
		int[] result = data.getSearchRequests().getSearchResults(searchrequestnr);
		// Save current search request number
		data.getSearchRequests().setCurrentSearch(searchrequestnr);
		// check whether we have any results
		if (result != null) {
			// tell selection listener to do nothing...
			data.setTableUpdateActive(true);
			// Sort the array with the entry-numbers of the search result
			if (result.length > 0)
				Arrays.sort(result);
			// get the table model
			DefaultTableModel dtm = (DefaultTableModel) jTableResults.getModel();
			// clear table
			dtm.setRowCount(0);
			// iterate the result-array
			for (int cnt = 0; cnt < result.length; cnt++) {
				// create a new object
				Object[] ob = new Object[6];
				// Store the information in that object
				// first the entry number
				ob[0] = result[cnt];
				// then the entry's title
				ob[1] = data.getData().getZettelTitle(result[cnt]);
				// get timestamp
				String[] timestamp = data.getData().getTimestamp(result[cnt]);
				// init timestamp variables.
				String created = "";
				String edited = "";
				// check whether we have any timestamp at all.
				if (timestamp != null && !timestamp[0].isEmpty() && timestamp[0].length() >= 6)
					created = timestamp[0].substring(4, 6) + "." + timestamp[0].substring(2, 4) + ".20"
							+ timestamp[0].substring(0, 2);
				// check whether we have any timestamp at all.
				if (timestamp != null && !timestamp[1].isEmpty() && timestamp[1].length() >= 6)
					edited = timestamp[1].substring(4, 6) + "." + timestamp[1].substring(2, 4) + ".20"
							+ timestamp[1].substring(0, 2);
				ob[2] = created;
				ob[3] = edited;
				// now, the entry's rating
				ob[4] = data.getData().getZettelRating(result[cnt]);
				// finally, check whether entry is on any desktop, and if so,
				// use desktop name in that column
				ob[5] = data.getDesktopData().getDesktopNameOfEntry(result[cnt]);
				// and add that content as a new row to the table
				dtm.addRow(ob);
			}
			// create a new stringbuilder to prepare the label
			// that shows the amount of found entries
			StringBuilder sb = new StringBuilder("");
			sb.append("(");
			sb.append(String.valueOf(dtm.getRowCount()));
			sb.append(" ");
			sb.append(data.getResourceMap().getString("hitsText"));
			sb.append(")");
			// Set labeltext
			jLabelHits.setText(sb.toString());
			// work done
			data.setTableUpdateActive(false);
			// enable filter text field
			jTextFieldFilterList.setEnabled(true);
		}
	}

	/**
 * Updates the display with content from the selected entry in jTableResults
 * and updates the related keyword list and highlights.
 */
private void updateDisplay() {
    int row = jTableResults.getSelectedRow();
    
    // If a row is selected, proceed with display update
    if (row != -1) {
        Object selectedObject = jTableResults.getValueAt(row, 0);
		Constants.zknlogger.info("Selected object: " + selectedObject);

        try {
            int selection = parseSelection(selectedObject);
            updateZettelContent(selection);
            updateKeywordList(selection);
            highlightKeywords(selection);
            updateMainFrameDisplay(selection);
        } catch (NumberFormatException e) {
            Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
        }
    } else {
        clearDisplay();
    }
}

// Helper method to parse the selected object into an integer
private int parseSelection(Object selectedObject) throws NumberFormatException {
    return Integer.parseInt(selectedObject.toString());
}

// Updates the main Zettel content display
private void updateZettelContent(int selection) {
    String[] searchTerms = getHighlightSearchterms();
    displayZettelContent(selection, searchTerms);
}

// Updates the keyword list with the keywords from the selected Zettel
private void updateKeywordList(int selection) {
    String[] keywords = data.getData().getKeywords(selection);
    data.getKeywordListModel().clear();

    if (keywords != null && keywords.length > 0) {
        Arrays.sort(keywords);
        for (String keyword : keywords) {
            data.getKeywordListModel().addElement(keyword);
        }
    }
}

// Highlights the search terms in the keyword list, if any
private void highlightKeywords(int selection) {
    String[] searchTerms = getHighlightSearchterms();

    if (searchTerms != null) {
        List<Integer> selectedIndices = new LinkedList<>();

        for (String searchTerm : searchTerms) {
            for (int i = 0; i < data.getKeywordListModel().getSize(); i++) {
                if (searchTerm.equalsIgnoreCase(data.getKeywordListModel().get(i).toString())) {
                    selectedIndices.add(i);
                }
            }
        }

        // Convert list to int[] and set selected indices
        int[] selections = selectedIndices.stream().mapToInt(Integer::intValue).toArray();
        jListKeywords.setSelectedIndices(selections);
    } else {
        jListKeywords.clearSelection();
    }
}

// Updates the main frame display if necessary
private void updateMainFrameDisplay(int selection) {
    if (data.getSettings().getShowSearchEntry()) {
        data.getMainFrame().setNewActivatedEntryAndUpdateDisplay(selection);
    }
}

// Clears the display fields and keyword list when no row is selected
private void clearDisplay() {
    jEditorPaneSearchEntry.setText("");
    data.getKeywordListModel().clear();
}


	public void updateDisplayAfterEditing() {
		// get selected row
		int row = jTableResults.getSelectedRow();
		// if we have any selections, go on
		if (row != -1) {
			// retrieve the value...
			Object o = jTableResults.getValueAt(row, 0);
			try {
				// ...and try to convert it to an integer value
				int selection = Integer.parseInt(o.toString());
				// prepare array for search terms which might be highlighted
				String[] sts = getHighlightSearchterms();
				displayZettelContent(selection, sts);
			} catch (NumberFormatException e) {
				Constants.zknlogger.log(Level.WARNING, e.getLocalizedMessage());
			}
		}
	}

	private String[] getHighlightSearchterms() {
		// prepare array for search terms which might be highlighted
		String[] sts = null;
		// get search terms, if highlighting is requested
		if (data.getSettings().getHighlightSearchResults()) {
			// get the selected index, i.e. the searchrequest we want to retrieve
			int index = jComboBoxSearches.getSelectedIndex();
			// get the related search terms
			sts = data.getSearchRequests().getSearchTerms(index);
			// check whether the search was a synonym-search. if yes, add synonyms to search
			// terms
			if (data.getSearchRequests().isSynonymSearch(index)) {
				// create new linked list that will contain all highlight-terms, including
				// the related synonyms of the highlight-terms
				LinkedList<String> highlight = new LinkedList<>();
				// go through all searchterms
				for (String s : sts) {
					// get the synonym-line for each search term
					String[] synline = data.getSynonyms().getSynonymLineFromAny(s, false);
					// if we have synonyms...
					if (synline != null) {
						// add them to the linked list, if they are new
						for (String sy : synline) {
							if (!highlight.contains(sy))
								highlight.add(sy);
						}
					}
					// else simply add the search term to the linked list
					else if (!highlight.contains(s)) {
						highlight.add(s);
					}
				}
				if (highlight.size() > 0)
					sts = highlight.toArray(new String[highlight.size()]);
			}
		}
		return sts;
	}

	void displayZettelContent(int nr, String[] highlightterms) {
		// Set highlight search terms
		HtmlUbbUtil.setHighlighTerms(highlightterms, HtmlUbbUtil.HIGHLIGHT_STYLE_SEARCHRESULTS,
				data.getSettings().getHighlightWholeWordSearch());
		// retrieve the string array of the first entry
		String disp = data.getData().getEntryAsHtml(nr,
				(data.getSettings().getHighlightSegments()) ? getSelectedKeywordsFromList() : null, Constants.FRAME_SEARCH);
		// in case parsing was ok, display the entry
		if (Tools.isValidHTML(disp, nr)) {
			// Set entry information in the main textfield
			jEditorPaneSearchEntry.setText(disp);
		}
		// else show error message box to user and tell him what to do
		else {
			StringBuilder cleanedContent = new StringBuilder("");
			cleanedContent
					.append("<body><div style=\"margin:5px;padding:5px;background-color:#dddddd;color:#800000;\">");
			URL imgURL = org.jdesktop.application.Application
					.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getClass()
					.getResource("/de/danielluedecke/zettelkasten/resources/icons/error.png");
			cleanedContent.append("<img border=\"0\" src=\"").append(imgURL).append("\">&#8195;");
			cleanedContent.append(data.getResourceMap().getString("incorrectNestedTagsText"));
			cleanedContent.append("</div>").append(data.getData().getCleanZettelContent(nr)).append("</body>");
			// and display clean content instead
			jEditorPaneSearchEntry.setText(cleanedContent.toString());
		}
		// place caret, so content scrolls to top
		jEditorPaneSearchEntry.setCaretPosition(0);
	}

	@Action
	public void exportEntries() {
		// retrieve the selected index from the combo box, so we know the search result.
		// then get the related search results (entries as integer array) from the
		// search-reuest
		// finally, call the mainframe's exportwindow-method and pass the int-array with
		// the entry-numbers
		data.getMainFrame().exportEntries(data.getSearchRequests().getSearchResults(jComboBoxSearches.getSelectedIndex()));
	}

	@Action
	public void editEntry() {
		// get selected entry
		int row = jTableResults.getSelectedRow();
		// when we have a valid selection, go on
		if (row != -1) {
			// remember that entry editing came from search window
			data.getMainFrame().editEntryFromSearchWindow = true;
			// open edit window
			data.getMainFrame().openEditWindow(true, Integer.parseInt(jTableResults.getValueAt(row, 0).toString()), false, false,
					-1);
		}
	}

	@Action
	public void duplicateSearch() {
		data.getSearchRequests().duplicateSearchRequest();
		updateComboBox(0, -1);
	}

	@Action
	public void findAndReplace() {
		// find and replace within search-results-entries, and update display if we have
		// any replacements.
		if (data.getMainFrame().replace(data.getSearchFrame(), null, getSelectedEntriesFromTable()))
			updateDisplay();
	}

	/**
	 * This method gets all selected elements of the jListEntryKeywords and returns
	 * them in an array.
	 *
	 * @return a string-array containing all selected keywords, or null if no
	 *         selection made
	 */
	private String[] getSelectedKeywordsFromList() {
		// get selected values
		List<String> values = jListKeywords.getSelectedValuesList();
		// if we have any selections, go on
		if (!values.isEmpty()) {
			// create string array for selected values
			// return complete array
			return values.toArray(new String[values.size()]);
		}
		// ...or null, if error occurred.
		return null;
	}

	@Action(enabledProperty = "textSelected")
	public void newSearchFromSelection() {
		// open the search dialog
		// the parameters are as following:
		data.getMainFrame().startSearch(new String[] { jEditorPaneSearchEntry.getSelectedText() }, // string-array with search
																							// terms
				Constants.SEARCH_AUTHOR, // the type of search, i.e. where to look
				Constants.LOG_OR, // the logical combination
				false, // whole-word-search
				false, // match-case-search
				data.getSettings().getSearchAlwaysSynonyms(), // whether synonyms should be included or not
				data.getSettings().getSearchAlwaysAccentInsensitive(), false, // time-period search
				false, // whether the search terms contain regular expressions or not
				"", // timestamp, date from (period start)
				"", // timestamp, date to (period end)
				0, // timestampindex (whether the period should focus on creation or edited date,
					// or both)
				false, // no display - whether the results should only be used for adding entries to
						// the desktop or so (true), or if a searchresults-window shoud be opened
						// (false)
				Constants.STARTSEARCH_USUAL, // whether we have a usual search, or a search for entries without remarks
												// or keywords and so on - see related method findEntryWithout
				Constants.SEARCH_USUAL);
	}

	@Action(enabledProperty = "listSelected")
	public void newSearchFromKeywordsLogOr() {
		// open the search dialog
		// the parameters are as following:
		data.getMainFrame().startSearch(getSelectedKeywordsFromList(), // string-array with search terms
				Constants.SEARCH_KEYWORDS, // the type of search, i.e. where to look
				Constants.LOG_OR, // the logical combination
				true, // whole-word-search
				true, // match-case-search
				data.getSettings().getSearchAlwaysSynonyms(), // whether synonyms should be included or not
				data.getSettings().getSearchAlwaysAccentInsensitive(), false, // time-period search
				false, // whether the search terms contain regular expressions or not
				"", // timestamp, date from (period start)
				"", // timestamp, date to (period end)
				0, // timestampindex (whether the period should focus on creation or edited date,
					// or both)
				false, // no display - whether the results should only be used for adding entries to
						// the desktop or so (true), or if a searchresults-window shoud be opened
						// (false)
				Constants.STARTSEARCH_USUAL, // whether we have a usual search, or a search for entries without remarks
												// or keywords and so on - see related method findEntryWithout
				Constants.SEARCH_USUAL);
	}

	@Action(enabledProperty = "listSelected")
	public void newSearchFromKeywordsLogAnd() {
		// open the search dialog
		// the parameters are as following:
		data.getMainFrame().startSearch(getSelectedKeywordsFromList(), // string-array with search terms
				Constants.SEARCH_KEYWORDS, // the type of search, i.e. where to look
				Constants.LOG_AND, // the logical combination
				true, // whole-word-search
				true, // match-case-search
				data.getSettings().getSearchAlwaysSynonyms(), // whether synonyms should be included or not
				data.getSettings().getSearchAlwaysAccentInsensitive(), false, // time-period search
				false, // whether the search terms contain regular expressions or not
				"", // timestamp, date from (period start)
				"", // timestamp, date to (period end)
				0, // timestampindex (whether the period should focus on creation or edited date,
					// or both)
				false, // no display - whether the results should only be used for adding entries to
						// the desktop or so (true), or if a searchresults-window shoud be opened
						// (false)
				Constants.STARTSEARCH_USUAL, // whether we have a usual search, or a search for entries without remarks
												// or keywords and so on - see related method findEntryWithout
				Constants.SEARCH_USUAL);
	}

	@Action(enabledProperty = "listSelected")
	public void newSearchFromKeywordsLogNot() {
		// open the search dialog
		// the parameters are as following:
		data.getMainFrame().startSearch(getSelectedKeywordsFromList(), // string-array with search terms
				Constants.SEARCH_KEYWORDS, // the type of search, i.e. where to look
				Constants.LOG_NOT, // the logical combination
				true, // whole-word-search
				true, // match-case-search
				data.getSettings().getSearchAlwaysSynonyms(), // whether synonyms should be included or not
				data.getSettings().getSearchAlwaysAccentInsensitive(), false, // time-period search
				false, // whether the search terms contain regular expressions or not
				"", // timestamp, date from (period start)
				"", // timestamp, date to (period end)
				0, // timestampindex (whether the period should focus on creation or edited date,
					// or both)
				false, // no display - whether the results should only be used for adding entries to
						// the desktop or so (true), or if a searchresults-window shoud be opened
						// (false)
				Constants.STARTSEARCH_USUAL, // whether we have a usual search, or a search for entries without remarks
												// or keywords and so on - see related method findEntryWithout
				Constants.SEARCH_USUAL);
	}

	/**
	 * This method opens the usual find-dialog and lets the user enter a "new"
	 * search request. the current search results are then filtered according to the
	 * search-parameters entered by the user. a new searchresult is being displayed
	 * after that. <br>
	 * <br>
	 * So the user can create a new search result with those previous entries
	 * removed that do not match the search criteria.
	 */
	@Action
	public void filterSearch() {
		// if dialog window isn't already created, do this now
		if (null == data.getSearchDlg()) {
			// create a new dialog window
			data.setSearchDlg(new CSearchDlg(this, data.getSearchRequests(), data.getSettings(), null));
			// center window
			data.getSearchDlg().setLocationRelativeTo(this);
		}
		ZettelkastenApp.getApplication().show(data.getSearchDlg());
		// open the search dialog
		// the parameters are as following:
		// - string-array with search results
		// - the type of search, i.e. where to look
		// - logical-and-combination
		// - whole words
		// - case-sensitive search
		if (!data.getSearchDlg().isCancelled()) {
			startSearch(Constants.SEARCH_USUAL, data.getSearchDlg().getSearchTerms(),
					data.getSearchRequests().getSearchResults(jComboBoxSearches.getSelectedIndex()), data.getSearchDlg().getWhereToSearch(),
					data.getSearchDlg().getLogical(), data.getSearchDlg().isWholeWord(), data.getSearchDlg().isMatchCase(),
					data.getSearchDlg().isSynonymsIncluded(), data.getSearchDlg().isAccentInsensitive(), data.getSearchDlg().isRegExSearch(),
					data.getSearchDlg().isTimestampSearch(), data.getSearchDlg().getDateFromValue(), data.getSearchDlg().getDateToValue(),
					data.getSearchDlg().getTimestampIndex());
		}

		data.getSearchDlg().dispose();
		data.setSearchDlg(null);
	}

	/**
	 * This method opens a dialog with a list that contains all keywords of the
	 * current search result's entries. The user can than choose keywords from this
	 * list and filter the search results, i.e. creating a new search result with
	 * those previous entries removed that do not match the search criteria (i.e.:
	 * don't have the selected keywords).
	 */
	@Action
	public void filterKeywords() {
		// retrieve current entries from the list
		int[] entries = data.getSearchRequests().getSearchResults(jComboBoxSearches.getSelectedIndex());
		// create linked list as parameter for filter-dialog
		LinkedList<String> keywords = new LinkedList<>();
		// go through all entries
		for (int e : entries) {
			// get keywords of each entries
			String[] kws = data.getData().getKeywords(e);
			// now go through all keywords of that entry
			// if keyword does not exist, add it to list
			if (kws != null)
				for (String k : kws)
					if (!keywords.contains(k))
						keywords.add(k);
		}
		// if dialog window isn't already created, do this now
		if (null == data.getFilterSearchDlg()) {
			// create a new dialog window
			data.setFilterSearchDlg(new CFilterSearch(this, data.getSettings(), keywords, null, true));
			// center window
			data.getFilterSearchDlg().setLocationRelativeTo(this);
		}
		ZettelkastenApp.getApplication().show(data.getFilterSearchDlg());
		// open the search dialog
		// the parameters are as following:
		// - string-array with search results
		// - the type of search, i.e. where to look
		// - logical-and-combination
		// - whole words
		// - case-sensitive search
		if (data.getFilterSearchDlg().getFilterTerms() != null) {
			startSearch(Constants.SEARCH_USUAL, data.getFilterSearchDlg().getFilterTerms(),
					data.getSearchRequests().getSearchResults(jComboBoxSearches.getSelectedIndex()), Constants.SEARCH_KEYWORDS,
					data.getFilterSearchDlg().getLogical(), true, true, data.getSettings().getSearchAlwaysSynonyms(), false,
					/* accentInsensitive= */false, false, "", "", 0);
		}

		data.getFilterSearchDlg().dispose();
		data.setFilterSearchDlg(null);
	}

	@Action
	public void filterTopLevelLuhmann() {
		// open the search dialog
		// the parameters are as following:
		// - string-array with search results
		// - the type of search, i.e. where to look
		// - logical-and-combination
		// - whole words
		// - case-sensitive search
		startSearch(Constants.SEARCH_TOP_LEVEL_LUHMANN, null,
				data.getSearchRequests().getSearchResults(jComboBoxSearches.getSelectedIndex()), -1, Constants.LOG_OR, false,
				false, false, false, /* accentInsensitive= */false, false, null, null, 0);
	}

	/**
	 * This method opens a dialog with a list that contains all authors of the
	 * current search result's entries. The user can than choose authors from this
	 * list and filter the search results, i.e. creating a new search result with
	 * those previous entries removed that do not match the search criteria (i.e.:
	 * don't have the selected authors).
	 */
	@Action
	public void filterAuthors() {
		// retrieve current entries from the list
		int[] entries = data.getSearchRequests().getSearchResults(jComboBoxSearches.getSelectedIndex());
		// create linked list as parameter for filter-dialog
		LinkedList<String> authors = new LinkedList<>();
		// go through all entries
		for (int e : entries) {
			// get authors of each entries
			String[] aus = data.getData().getAuthors(e);
			// now go through all keywords of that entry
			// if keyword does not exist, add it to list
			if (aus != null)
				for (String a : aus)
					if (!authors.contains(a))
						authors.add(a);
		}
		// if dialog window isn't already created, do this now
		if (null == data.getFilterSearchDlg()) {
			// create a new dialog window
			data.setFilterSearchDlg(new CFilterSearch(this, data.getSettings(), authors, null, true));
			// center window
			data.getFilterSearchDlg().setLocationRelativeTo(this);
		}
		ZettelkastenApp.getApplication().show(data.getFilterSearchDlg());
		// open the search dialog
		// the parameters are as following:
		// - string-array with search results
		// - the type of search, i.e. where to look
		// - logical-and-combination
		// - whole words
		// - case-sensitive search
		if (data.getFilterSearchDlg().getFilterTerms() != null) {
			startSearch(Constants.SEARCH_USUAL, data.getFilterSearchDlg().getFilterTerms(),
					data.getSearchRequests().getSearchResults(jComboBoxSearches.getSelectedIndex()), Constants.SEARCH_AUTHOR,
					data.getFilterSearchDlg().getLogical(), true, true, data.getSettings().getSearchAlwaysSynonyms(), false,
					/* accentInsensitive= */false, false, "", "", 0);
		}

		data.getFilterSearchDlg().dispose();
		data.setFilterSearchDlg(null);
	}

	/**
	 * Opens the search dialog. <br>
	 * <br>
	 * In case of keyword- and author-search <i>from the table</i> (lists), we can
	 * neglect the last parameter, since keyword- and author-search simply functions
	 * by searching for the index-numbers, that are always - or never - case
	 * sensitive relevant. <br>
	 * <br>
	 * When we have searchterms from the search-dialog, the user also can search for
	 * <i>parts</i> inside a keyword-string, so here the whole-word-parameter is
	 * relevant, since we then don't compare by index- numbers, but by the
	 * string-value of the keywords/authors.
	 * 
	 * @param searchterms    string-array with search terms
	 * @param searchin       the entries where the search should be apllied to, i.e.
	 *                       when we want to filter a certain search result
	 * @param where          the type of search, i.e. where to look, e.g. searching
	 *                       for keywords, authors, text etc.
	 * @param logand         logical-and-combination
	 * @param wholeword      whether we look for whole words or also parts of a
	 *                       word/phrase
	 * @param matchcase      whether the search should be case sensitive or not
	 * @param synonyms       whether the search should include synonyms or not
	 * @param timesearch     whether the user requested a time-search, i.e. a search
	 *                       for entries that were created or changed within a
	 *                       certain period
	 * @param datefrom       the start of the period, when a timesearch is
	 *                       requested. format: "yymmdd".
	 * @param dateto         the end of the period, when a timesearch is requested.
	 *                       format: "yymmdd".
	 * @param timestampindex
	 */
	private void startSearch(int searchtype, String[] searchterms, int[] searchin, int where, int logical,
			boolean wholeword, boolean matchcase, boolean syno, boolean accentInsensitive, boolean regex,
			boolean timesearch, String datefrom, String dateto, int timestampindex) {
		// check whether we have valid searchterms or not...
		if ((null == searchterms || searchterms.length < 1) && searchtype != Constants.SEARCH_TOP_LEVEL_LUHMANN)
			return;
		// if dialog window isn't already created, do this now
		if (null == data.getTaskDlg()) {
			// get parent und init window
			data.setTaskDlg(new TaskProgressDialog(this, TaskProgressDialog.TASK_SEARCH, data.getData(), data.getSearchRequests(), data.getSynonyms(),
					searchtype, searchterms, searchin, where, logical, wholeword, matchcase, syno, accentInsensitive,
					regex, timesearch, datefrom, dateto, timestampindex, false,
					data.getSettings().getSearchRemovesFormatTags()));
			// center window
			data.getTaskDlg().setLocationRelativeTo(this);
		}
		ZettelkastenApp.getApplication().show(data.getTaskDlg());
		// we have to manually dispose the window and release the memory
		// because next time this method is called, the showKwlDlg is still not null,
		// i.e. the constructor is not called (because the if-statement above is not
		// true)
		// dispose the window and clear the object
		data.getTaskDlg().dispose();
		data.setTaskDlg(null);
		// check whether we have any search results at all
		if (data.getSearchRequests().getCurrentSearchResults() != null) {
			showLatestSearchResult();
		} else {
			// display error message box that nothing was found
			JOptionPane.showMessageDialog(this, data.getResourceMap().getString("errNothingFoundMsg"),
					data.getResourceMap().getString("errNothingFoundTitle"), JOptionPane.PLAIN_MESSAGE);
		}
	}

	@Action
	public void showLongDesc() {
		// display long description
		JOptionPane.showMessageDialog(null, data.getSearchRequests().getLongDescription(jComboBoxSearches.getSelectedIndex()),
				data.getResourceMap().getString("longDescTitle"), JOptionPane.PLAIN_MESSAGE);
	}

	@Action
	public void showHighlightSettings() {
		if (null == data.getHighlightSettingsDlg()) {
			data.setHighlightSettingsDlg(new CHighlightSearchSettings(this, data.getSettings(),
					HtmlUbbUtil.HIGHLIGHT_STYLE_SEARCHRESULTS));
			data.getHighlightSettingsDlg().setLocationRelativeTo(this);
		}
		ZettelkastenApp.getApplication().show(data.getHighlightSettingsDlg());
		data.getHighlightSettingsDlg().dispose();
		data.setHighlightSettingsDlg(null);

		updateDisplay();
	}

	/**
	 * This method retrieves the selected entries and adds them to the deskop, by
	 * calling the mainframe's method addToDesktop().
	 */
	@Action
	public void addToDesktop() {
		// get selected entries
		int[] entries = getSelectedEntriesFromTable();
		// if we have any valid values, add them to desktop
		if ((entries != null) && (entries.length > 0))
			data.getMainFrame().addToDesktop(entries);
	}

	/**
	 * This method retrieves the selected entries and adds them to the deskop, by
	 * calling the mainframe's method addToDesktop().
	 */
	@Action
	public void addToBookmarks() {
		// get selected entries
		int[] entries = getSelectedEntriesFromTable();
		// if we have any valid values...
		if ((entries != null) && (entries.length > 0)) {
			// add them as bookmarks
			data.getMainFrame().addToBookmarks(entries, false);
			// and display related tab
			data.getMainFrame().menuShowBookmarks();
		}
	}

	/**
	 * This method retrieves the selected entries and adds them as follower-numbers
	 * to that entry that is selected in the mainframe's luhmann-tab, in the
	 * jTreeLuhmann.
	 */
	@Action
	public void addToLuhmann() {
		// get selected entries
		int[] entries = getSelectedEntriesFromTable();
		// if we have any valid values...
		if ((entries != null) && (entries.length > 0)) {
			// add them as followers
			data.getMainFrame().addToLuhmann(entries);
			// and display related tab
			data.getMainFrame().menuShowLuhmann();
		}
	}

	/**
	 * This method retrieves the selected entries and adds them as manual link to
	 * the mainframe's current entry.
	 */
	@Action
	public void addToManLinks() {
		// get selected entries
		int[] entries = getSelectedEntriesFromTable();
		// if we have any valid values...
		if ((entries != null) && (entries.length > 0)) {
			// add them as followers
			data.getMainFrame().addToManLinks(entries);
			// and display related tab
			data.getMainFrame().menuShowLinks();
		}
	}

	/**
	 * Selects all entries in the table with the search results
	 */
	@Action
	public void selectAll() {
		jTableResults.selectAll();
	}

	/**
	 * This method gets all selected elements of the jTableResults and returns them
	 * in an array.
	 * 
	 * @return a integer-array containing all selected entries, or null if no
	 *         selection made
	 */
	private int[] getSelectedEntriesFromTable() {
		// get selected rows
		int[] rows = jTableResults.getSelectedRows();
		// if we have any selections, go on
		if (rows != null && rows.length > 0) {
			// create string array for selected values
			int[] entries = new int[rows.length];
			try {
				// iterate array
				for (int cnt = 0; cnt < rows.length; cnt++) {
					// copy value from table to array
					entries[cnt] = Integer.parseInt(jTableResults.getValueAt(rows[cnt], 0).toString());
				}
				// return complete array
				return entries;
			} catch (NumberFormatException e) {
				return null;
			}
		}
		// ...or null, if error occured.
		return null;
	}

	/**
	 * This method removes the selected result-entry-numbers from the results list.
	 */
	@Action
	public void removeEntry() {
		// get selected rows
		int[] rows = jTableResults.getSelectedRows();
		// if we have any selections, go on
		if ((rows != null) && (rows.length > 0)) {
			// get the selected searchrequest
			int i = jComboBoxSearches.getSelectedIndex();
			for (int cnt = rows.length - 1; cnt >= 0; cnt--) {
				// retrieve the values...
				Object o = jTableResults.getValueAt(rows[cnt], 0);
				// ...and try to convert it to an integer value
				int selection = Integer.parseInt(o.toString());
				// delete the entry from the search request
				data.getSearchRequests().deleteResultEntry(i, selection);
			}
			updateComboBox(rows[0], -1);
		}
	}

	/**
	 * This method deletes the selected entries completely from the dataset
	 */
	@Action
	public void deleteEntryComplete() {
		// first display the to be deleted entry in the main-frame, so the user is not
		// confused
		// about which entry to delete...
		displayEntryInMainframe();
		// try to delete the entry
		// and bring search results frame to front...
		if (data.getMainFrame().deleteEntries(getSelectedEntriesFromTable()))
			this.toFront();
	}

	/**
	 * This method removes all(!) search requests, i.e. clears the
	 * search-request-xml-data.
	 */
	@Action
	public void removeAllSearchResults() {
		// and create a JOptionPane with yes/no/cancel options
		int msgOption = JOptionPane.showConfirmDialog(null, data.getResourceMap().getString("askForDeleteAllMsg"),
				data.getResourceMap().getString("askForDeleteAllTitle"), JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
		// if the user wants to proceed, copy the image now
		if (JOptionPane.YES_OPTION == msgOption) {
			// completeley remove all search requests
			data.getSearchRequests().deleteAllSearchRequests();
			// reset combobox
			updateComboBox(-1, -1);
		}
	}

	private void displayEntryInMainframe() {
		// get selected entry
		int row = jTableResults.getSelectedRow();
		// when we have a valid selection, go on
		if (row != -1)
			data.getMainFrame().setNewActivatedEntryAndUpdateDisplay(Integer.parseInt(jTableResults.getValueAt(row, 0).toString()));
	}

	/**
	 * This method removes a complete search request from the search results.
	 */
	@Action
	public void removeSearchResult() {
		// and create a JOptionPane with yes/no/cancel options
		int msgOption = JOptionPane.showConfirmDialog(null, data.getResourceMap().getString("askForDeleteSearchMsg"),
				data.getResourceMap().getString("askForDeleteSearchTitle"), JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
		// if the user wants to proceed, copy the image now
		if (JOptionPane.YES_OPTION == msgOption) {
			// get the selected searchrequest
			int i = jComboBoxSearches.getSelectedIndex();
			// delete complete search request
			data.getSearchRequests().deleteSearchRequest(i);
			// update combo box
			updateComboBox(0, -1);
		}
	}

	/**
	 * Closes the window.
	 */
	@Action
	public void closeWindow() {
		// check whether memory usage is logged. if so, tell logger that new entry
		// windows was opened
		if (data.getSettings().isMemoryUsageLogged) {
			// log info
			Constants.zknlogger.log(Level.INFO, "Memory usage logged. Search Results Window closed.");
		}
		dispose();
		setVisible(false);
	}

	/**
	 * Activates or deactivates the fullscreen-mode, thus switching between
	 * fullscreen and normal view.
	 */
	@Action(enabledProperty = "fullScreenSupp")
	public void viewFullScreen() {
		// check whether fullscreen is possible or not...
		if (data.getGraphicDevice().isFullScreenSupported()) {
			// if we already have a fullscreen window, quit fullscreen
			if (data.getGraphicDevice().getFullScreenWindow() != null)
				quitFullScreen();
			// else show fullscreen window
			else
				showFullScreen();
		}
	}

	/**
	 * This method activates the fullscreen-mode, if it's not already activated yet.
	 * To have a fullscreen-window without decoration, the frame is disposed first,
	 * then the decoration will be removed and the window made visible again.
	 */
	private void showFullScreen() {
		// check whether fullscreen is supported, and if we currently have a
		// fullscreen-window
		if (data.getGraphicDevice().isFullScreenSupported() && null == data.getGraphicDevice().getFullScreenWindow()) {
			// dispose frame, so we can remove the decoration when setting full screen mode
			data.getSearchFrame().dispose();
			// hide menubar
			searchMenuBar.setVisible(false);
			// set frame non-resizable
			data.getSearchFrame().setResizable(false);
			try {
				// remove decoration
				data.getSearchFrame().setUndecorated(true);
			} catch (IllegalComponentStateException e) {
				Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
			}
			// show frame again
			data.getSearchFrame().setVisible(true);
			// set fullscreen mode to this window
			data.getGraphicDevice().setFullScreenWindow(this);
		}
	}

	/**
	 * This method <i>de</i>activates the fullscreen-mode, if it's not already
	 * deactivated yet.
	 */
	private void quitFullScreen() {
		// check whether full screen is supported, and if we currently have a
		// fullscreen-window
		if (data.getGraphicDevice().isFullScreenSupported() && data.getGraphicDevice().getFullScreenWindow() != null) {
			// disable fullscreen-mode
			data.getGraphicDevice().setFullScreenWindow(null);
			// hide menubar
			searchMenuBar.setVisible(true);
			// make frame resizable again
			data.getSearchFrame().setResizable(true);
			// dispose frame, so we can restore the decoration
			data.getSearchFrame().dispose();
			try {
				// set decoration
				data.getSearchFrame().setUndecorated(false);
			} catch (IllegalComponentStateException e) {
				Constants.zknlogger.log(Level.SEVERE, e.getLocalizedMessage());
			}
			// show frame again
			data.getSearchFrame().setVisible(true);
		}
	}

	/**
	 * This method is used to pass paramaters to this dialog, so it can display
	 * results when it is made visible. Since we don't dispose and clear this
	 * dialog, we cannot call the constructor each time, so we need another method
	 * where we can pass parameters of new search results. <br>
	 * <br>
	 * This dialog is not disposed and cleared, because we want to keep former
	 * search results, even when the user "closes" (i.e.: hides) this dialog.
	 */
	public void showLatestSearchResult() {
		// here we update the combo box, not the display. since selecting
		// an item, which is done in this method, fires an action to the action
		// listener,
		// the display update should be achieved through the combobox's actionlistener.
		updateComboBox(-1, data.getSearchRequests().getCount() - 1);
		// and make dialog visible
		setVisible(true);
		// repaint the components (necessary, since the components are not properly
		// repainted else)
		repaint();
		// set input focus
		this.setAlwaysOnTop(true);
		this.toFront();
		this.requestFocusInWindow();
		this.setAlwaysOnTop(false);
		setAlwaysOnTop(true);
		setAlwaysOnTop(false);
		toFront();
	}

	private void openAttachment(javax.swing.event.HyperlinkEvent evt) {
		// retrieve the event type, e.g. if a link was clicked by the user
		HyperlinkEvent.EventType typ = evt.getEventType();
		// get the description, to check whether we have a file or a hyperlink to a
		// website
		String linktype = evt.getDescription();
		// if the link was clicked, proceed
		if (typ == HyperlinkEvent.EventType.ACTIVATED) {
			// call method that handles the hyperlink-click
			String returnValue = Tools.openHyperlink(linktype, this, Constants.FRAME_SEARCH, data.getData(), data.getBibTeX(),
					data.getSettings(), jEditorPaneSearchEntry,
					Integer.parseInt(jTableResults.getValueAt(jTableResults.getSelectedRow(), 0).toString()));
			// check whether we have a return value. this might be the case either when the
			// user clicked on
			// a footnote, or on the rating-stars
			if (returnValue != null) {
				// here we have a reference to another entry
				if (returnValue.startsWith("#z_") || returnValue.startsWith("#cr_")) {
					// show entry
					data.getMainFrame().setNewActivatedEntryAndUpdateDisplay(data.getData().getActivatedEntryNumber());
				}
				// edit cross references
				else if (returnValue.equalsIgnoreCase("#crt")) {
					data.getMainFrame().editManualLinks();
				}
				// check whether a rating was requested
				else if (returnValue.startsWith("#rateentry")) {
					try {
						// retrieve entry-number
						int entrynr = Integer.parseInt(linktype.substring(10));
						// open rating-dialog
						if (null == data.getRateEntryDlg()) {
							data.setRateEntryDlg(new CRateEntry(this, data.getData(), entrynr));
							data.getRateEntryDlg().setLocationRelativeTo(this);
						}
						ZettelkastenApp.getApplication().show(data.getRateEntryDlg());
						// check whether dialog was cancelled or not
						if (!data.getRateEntryDlg().isCancelled()) {
							// update display
							displayZettelContent(entrynr, null);
						}
						data.getRateEntryDlg().dispose();
						data.setRateEntryDlg(null);
					} catch (NumberFormatException ex) {
						// log error
						Constants.zknlogger.log(Level.WARNING, ex.getLocalizedMessage());
						Constants.zknlogger.log(Level.WARNING, "Could not rate entry. Link-text was {0}", linktype);
					}

				}
			}
		}
	}

	/**
	 * This class sets up a selection listener for the tables. each table which
	 * shall react on selections, e.g. by showing an entry, gets this
	 * selectionlistener in the method {@link #initSelectionListeners()
	 * initSelectionListeners()}.
	 */
	public class SelectionListener implements ListSelectionListener {
		JTable table;

		// It is necessary to keep the table since it is not possible
		// to determine the table from the event's source
		SelectionListener(JTable table) {
			this.table = table;
		}

		@Override
		public void valueChanged(ListSelectionEvent e) {
			// if we have an update, don't react on selection changes
			if (data.isTableUpdateActive())
				return;
			// get list selection model
			ListSelectionModel lsm = (ListSelectionModel) e.getSource();
			// set value-adjusting to true, so we don't fire multiple value-changed
			// events...
			lsm.setValueIsAdjusting(true);
			if (jTableResults == table)
				updateDisplay();
		}
	}

	public boolean isTextSelected() {
		return data.isTextSelected();
	}

	public void setTextSelected(boolean b) {
		boolean old = isTextSelected();
		this.data.setTextSelected(b);
		firePropertyChange("textSelected", old, isTextSelected());
	}

	public boolean isListSelected() {
		return data.isListSelected();
	}

	public void setListSelected(boolean b) {
		boolean old = isListSelected();
		this.data.setListSelected(b);
		firePropertyChange("listSelected", old, isListSelected());
	}

	public boolean isDesktopEntrySelected() {
		return data.isDesktopEntrySelected();
	}

	public void setDesktopEntrySelected(boolean b) {
		boolean old = isDesktopEntrySelected();
		this.data.setDesktopEntrySelected(b);
		firePropertyChange("desktopEntrySelected", old, isDesktopEntrySelected());
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	// <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        searchToolbar = new javax.swing.JToolBar();
        tb_copy = new javax.swing.JButton();
        tb_selectall = new javax.swing.JButton();
        jSeparator12 = new javax.swing.JToolBar.Separator();
        tb_editentry = new javax.swing.JButton();
        tb_remove = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        tb_manlinks = new javax.swing.JButton();
        tb_luhmann = new javax.swing.JButton();
        tb_bookmark = new javax.swing.JButton();
        tb_desktop = new javax.swing.JButton();
        jSeparator5 = new javax.swing.JToolBar.Separator();
        tb_highlight = new javax.swing.JButton();
        searchMainPanel = new javax.swing.JPanel();
        jSplitPaneSearch1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTableResults = (data.getSettings().isMacStyle()) ? com.explodingpixels.macwidgets.MacWidgetFactory.createITunesTable(null) : new javax.swing.JTable();
        jTextFieldFilterList = new javax.swing.JTextField();
        jButtonResetList = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jSplitPaneSearch2 = new javax.swing.JSplitPane();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jEditorPaneSearchEntry = new javax.swing.JEditorPane();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jListKeywords = MacSourceList.createMacSourceList();
        searchStatusPanel = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jComboBoxSearches = new javax.swing.JComboBox();
        jLabelHits = new javax.swing.JLabel();
        jButtonDeleteSearch = new javax.swing.JButton();
        searchMenuBar = new javax.swing.JMenuBar();
        searchFileMenu = new javax.swing.JMenu();
        fileMenuLongDesc = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        fileMenuDuplicateSearch = new javax.swing.JMenuItem();
        jSeparator22 = new javax.swing.JSeparator();
        fileMenuDeleteSearch = new javax.swing.JMenuItem();
        fileMenuDeleteAll = new javax.swing.JMenuItem();
        jSeparator20 = new javax.swing.JSeparator();
        fileMenuExport = new javax.swing.JMenuItem();
        jSeparator13 = new javax.swing.JSeparator();
        fileMenuClose = new javax.swing.JMenuItem();
        searchEditMenu = new javax.swing.JMenu();
        editMenuCopy = new javax.swing.JMenuItem();
        editMenuSelectAll = new javax.swing.JMenuItem();
        jSeparator10 = new javax.swing.JSeparator();
        editMenuDelete = new javax.swing.JMenuItem();
        jSeparator16 = new javax.swing.JSeparator();
        editMenuEditEntry = new javax.swing.JMenuItem();
        editMenuDuplicateEntry = new javax.swing.JMenuItem();
        editMenuFindReplace = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JSeparator();
        editMenuDeleteEntry = new javax.swing.JMenuItem();
        jSeparator21 = new javax.swing.JSeparator();
        editMenuAddKeywordsToSelection = new javax.swing.JMenuItem();
        editMenuAddAuthorsToSelection = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        editMenuManLinks = new javax.swing.JMenuItem();
        editMenuLuhmann = new javax.swing.JMenuItem();
        editMenuBookmarks = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JSeparator();
        editMenuDesktop = new javax.swing.JMenuItem();
        searchFilterMenu = new javax.swing.JMenu();
        filterSearch = new javax.swing.JMenuItem();
        jSeparator14 = new javax.swing.JSeparator();
        filterKeywords = new javax.swing.JMenuItem();
        jSeparator15 = new javax.swing.JSeparator();
        filterAuthors = new javax.swing.JMenuItem();
        jSeparator23 = new javax.swing.JPopupMenu.Separator();
        filterTopLevelLuhmann = new javax.swing.JMenuItem();
        searchSearchMenu = new javax.swing.JMenu();
        searchMenuSelectionContent = new javax.swing.JMenuItem();
        jSeparator19 = new javax.swing.JSeparator();
        searchMenuKeywordLogOr = new javax.swing.JMenuItem();
        searchMenuKeywordLogAnd = new javax.swing.JMenuItem();
        searchMenuKeywordLogNot = new javax.swing.JMenuItem();
        searchViewMenu = new javax.swing.JMenu();
        viewMenuShowOnDesktop = new javax.swing.JMenuItem();
        jSeparator11 = new javax.swing.JSeparator();
        viewMenuHighlight = new javax.swing.JCheckBoxMenuItem();
        viewMenuHighlightSettings = new javax.swing.JMenuItem();
        jSeparator9 = new javax.swing.JSeparator();
        viewMenuShowEntry = new javax.swing.JCheckBoxMenuItem();
        jSeparator7 = new javax.swing.JPopupMenu.Separator();
        jMenuItemSwitchLayout = new javax.swing.JMenuItem();
        jSeparator8 = new javax.swing.JPopupMenu.Separator();
        viewMenuFullScreen = new javax.swing.JMenuItem();

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance().getContext().getResourceMap(SearchResultsFrame.class);
        setTitle(resourceMap.getString("FormSearchResults.title")); // NOI18N
        setName("FormSearchResults"); // NOI18N

        searchToolbar.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, resourceMap.getColor("searchToolbar.border.matteColor"))); // NOI18N
        searchToolbar.setRollover(true);
        searchToolbar.setName("searchToolbar"); // NOI18N

        tb_copy.setAction(org.jdesktop.application.Application.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getContext().getActionMap(SearchResultsFrame.class, this).get("copy"));
        tb_copy.setText(resourceMap.getString("tb_copy.text")); // NOI18N
        tb_copy.setBorderPainted(false);
        tb_copy.setFocusPainted(false);
        tb_copy.setFocusable(false);
        tb_copy.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tb_copy.setName("tb_copy"); // NOI18N
        tb_copy.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        searchToolbar.add(tb_copy);

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance().getContext().getActionMap(SearchResultsFrame.class, this);
        tb_selectall.setAction(actionMap.get("selectAll")); // NOI18N
        tb_selectall.setText(resourceMap.getString("tb_selectall.text")); // NOI18N
        tb_selectall.setBorderPainted(false);
        tb_selectall.setFocusPainted(false);
        tb_selectall.setFocusable(false);
        tb_selectall.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tb_selectall.setName("tb_selectall"); // NOI18N
        tb_selectall.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        searchToolbar.add(tb_selectall);

        jSeparator12.setName("jSeparator12"); // NOI18N
        searchToolbar.add(jSeparator12);

        tb_editentry.setAction(actionMap.get("editEntry")); // NOI18N
        tb_editentry.setText(resourceMap.getString("tb_editentry.text")); // NOI18N
        tb_editentry.setBorderPainted(false);
        tb_editentry.setFocusPainted(false);
        tb_editentry.setFocusable(false);
        tb_editentry.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tb_editentry.setName("tb_editentry"); // NOI18N
        tb_editentry.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        searchToolbar.add(tb_editentry);

        tb_remove.setAction(actionMap.get("removeEntry")); // NOI18N
        tb_remove.setText(resourceMap.getString("tb_remove.text")); // NOI18N
        tb_remove.setBorderPainted(false);
        tb_remove.setFocusPainted(false);
        tb_remove.setFocusable(false);
        tb_remove.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tb_remove.setName("tb_remove"); // NOI18N
        tb_remove.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        searchToolbar.add(tb_remove);

        jSeparator3.setName("jSeparator3"); // NOI18N
        searchToolbar.add(jSeparator3);

        tb_manlinks.setAction(actionMap.get("addToManLinks")); // NOI18N
        tb_manlinks.setText(resourceMap.getString("tb_manlinks.text")); // NOI18N
        tb_manlinks.setBorderPainted(false);
        tb_manlinks.setFocusPainted(false);
        tb_manlinks.setFocusable(false);
        tb_manlinks.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tb_manlinks.setName("tb_manlinks"); // NOI18N
        tb_manlinks.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        searchToolbar.add(tb_manlinks);

        tb_luhmann.setAction(actionMap.get("addToLuhmann")); // NOI18N
        tb_luhmann.setText(resourceMap.getString("tb_luhmann.text")); // NOI18N
        tb_luhmann.setBorderPainted(false);
        tb_luhmann.setFocusPainted(false);
        tb_luhmann.setFocusable(false);
        tb_luhmann.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tb_luhmann.setName("tb_luhmann"); // NOI18N
        tb_luhmann.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        searchToolbar.add(tb_luhmann);

        tb_bookmark.setAction(actionMap.get("addToBookmarks")); // NOI18N
        tb_bookmark.setText(resourceMap.getString("tb_bookmark.text")); // NOI18N
        tb_bookmark.setBorderPainted(false);
        tb_bookmark.setFocusPainted(false);
        tb_bookmark.setFocusable(false);
        tb_bookmark.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tb_bookmark.setName("tb_bookmark"); // NOI18N
        tb_bookmark.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        searchToolbar.add(tb_bookmark);

        tb_desktop.setAction(actionMap.get("addToDesktop")); // NOI18N
        tb_desktop.setText(resourceMap.getString("tb_desktop.text")); // NOI18N
        tb_desktop.setBorderPainted(false);
        tb_desktop.setFocusPainted(false);
        tb_desktop.setFocusable(false);
        tb_desktop.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tb_desktop.setName("tb_desktop"); // NOI18N
        tb_desktop.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        searchToolbar.add(tb_desktop);

        jSeparator5.setName("jSeparator5"); // NOI18N
        searchToolbar.add(jSeparator5);

        tb_highlight.setAction(actionMap.get("toggleHighlightResults")); // NOI18N
        tb_highlight.setText(resourceMap.getString("tb_highlight.text")); // NOI18N
        tb_highlight.setBorderPainted(false);
        tb_highlight.setFocusPainted(false);
        tb_highlight.setFocusable(false);
        tb_highlight.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tb_highlight.setName("tb_highlight"); // NOI18N
        tb_highlight.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        searchToolbar.add(tb_highlight);

        searchMainPanel.setName("searchMainPanel"); // NOI18N
        searchMainPanel.setLayout(new java.awt.BorderLayout());

        jSplitPaneSearch1.setDividerLocation(240);
        jSplitPaneSearch1.setOrientation(data.getSettings().getSearchFrameSplitLayout());
        jSplitPaneSearch1.setName("jSplitPaneSearch1"); // NOI18N
        jSplitPaneSearch1.setOneTouchExpandable(true);

        jPanel1.setName("jPanel1"); // NOI18N

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTableResults.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Zettel", "Überschrift", "Erstellt", "Geändert", "Bewertung", "Schreibtisch"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Float.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTableResults.setDragEnabled(true);
        jTableResults.setName("jTableResults"); // NOI18N
        jTableResults.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        jTableResults.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(jTableResults);
        if (jTableResults.getColumnModel().getColumnCount() > 0) {
            jTableResults.getColumnModel().getColumn(0).setHeaderValue(resourceMap.getString("jTableResults.columnModel.title0")); // NOI18N
            jTableResults.getColumnModel().getColumn(1).setHeaderValue(resourceMap.getString("jTableResults.columnModel.title1")); // NOI18N
            jTableResults.getColumnModel().getColumn(2).setHeaderValue(resourceMap.getString("jTableResults.columnModel.title2")); // NOI18N
            jTableResults.getColumnModel().getColumn(3).setHeaderValue(resourceMap.getString("jTableResults.columnModel.title3")); // NOI18N
            jTableResults.getColumnModel().getColumn(4).setHeaderValue(resourceMap.getString("jTableResults.columnModel.title4")); // NOI18N
            jTableResults.getColumnModel().getColumn(5).setHeaderValue(resourceMap.getString("jTableResults.columnModel.title5")); // NOI18N
        }

        jTextFieldFilterList.setName("jTextFieldFilterList"); // NOI18N

        jButtonResetList.setAction(actionMap.get("resetResultslist")); // NOI18N
        jButtonResetList.setIcon(resourceMap.getIcon("jButtonResetList.icon")); // NOI18N
        jButtonResetList.setBorderPainted(false);
        jButtonResetList.setContentAreaFilled(false);
        jButtonResetList.setFocusable(false);
        jButtonResetList.setName("jButtonResetList"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 240, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTextFieldFilterList)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonResetList, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 282, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldFilterList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonResetList))
                .addGap(3, 3, 3))
        );

        jSplitPaneSearch1.setLeftComponent(jPanel1);

        jPanel2.setName("jPanel2"); // NOI18N

        jSplitPaneSearch2.setDividerLocation(280);
        jSplitPaneSearch2.setName("jSplitPaneSearch2"); // NOI18N
        jSplitPaneSearch2.setOneTouchExpandable(true);

        jPanel3.setName("jPanel3"); // NOI18N

        jScrollPane2.setBorder(null);
        jScrollPane2.setName("jScrollPane2"); // NOI18N

        jEditorPaneSearchEntry.setEditable(false);
        jEditorPaneSearchEntry.setBorder(null);
        jEditorPaneSearchEntry.setContentType(resourceMap.getString("jEditorPaneSearchEntry.contentType")); // NOI18N
        jEditorPaneSearchEntry.setAlignmentX(0.75F);
        jEditorPaneSearchEntry.setName("jEditorPaneSearchEntry"); // NOI18N
        jScrollPane2.setViewportView(jEditorPaneSearchEntry);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING)
        );

        jSplitPaneSearch2.setLeftComponent(jPanel3);

        jPanel4.setName("jPanel4"); // NOI18N

        jScrollPane4.setName("jScrollPane4"); // NOI18N

        jListKeywords.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("jListKeywords.border.title"))); // NOI18N
        jListKeywords.setAlignmentX(0.75F);
        jListKeywords.setName("jListKeywords"); // NOI18N
        jScrollPane4.setViewportView(jListKeywords);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 319, Short.MAX_VALUE)
        );

        jSplitPaneSearch2.setRightComponent(jPanel4);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPaneSearch2)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPaneSearch2)
        );

        jSplitPaneSearch1.setRightComponent(jPanel2);

        searchMainPanel.add(jSplitPaneSearch1, java.awt.BorderLayout.CENTER);

        searchStatusPanel.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, resourceMap.getColor("searchStatusPanel.border.matteColor"))); // NOI18N
        searchStatusPanel.setMinimumSize(new java.awt.Dimension(200, 16));
        searchStatusPanel.setName("searchStatusPanel"); // NOI18N

        jPanel9.setName("jPanel9"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jComboBoxSearches.setName("jComboBoxSearches"); // NOI18N

        jLabelHits.setText(resourceMap.getString("jLabelHits.text")); // NOI18N
        jLabelHits.setName("jLabelHits"); // NOI18N

        jButtonDeleteSearch.setAction(actionMap.get("removeSearchResult")); // NOI18N
        jButtonDeleteSearch.setIcon(resourceMap.getIcon("jButtonDeleteSearch.icon")); // NOI18N
        jButtonDeleteSearch.setText(resourceMap.getString("jButtonDeleteSearch.text")); // NOI18N
        jButtonDeleteSearch.setBorderPainted(false);
        jButtonDeleteSearch.setFocusPainted(false);
        jButtonDeleteSearch.setFocusable(false);
        jButtonDeleteSearch.setName("jButtonDeleteSearch"); // NOI18N

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelHits)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBoxSearches, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonDeleteSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGap(3, 3, 3)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonDeleteSearch)
                    .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabelHits)
                        .addComponent(jLabel1)
                        .addComponent(jComboBoxSearches, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(3, 3, 3))
        );

        javax.swing.GroupLayout searchStatusPanelLayout = new javax.swing.GroupLayout(searchStatusPanel);
        searchStatusPanel.setLayout(searchStatusPanelLayout);
        searchStatusPanelLayout.setHorizontalGroup(
            searchStatusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        searchStatusPanelLayout.setVerticalGroup(
            searchStatusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        searchMenuBar.setName("searchMenuBar"); // NOI18N

        searchFileMenu.setText(resourceMap.getString("searchFileMenu.text")); // NOI18N
        searchFileMenu.setName("searchFileMenu"); // NOI18N

        fileMenuLongDesc.setAction(actionMap.get("showLongDesc")); // NOI18N
        fileMenuLongDesc.setName("fileMenuLongDesc"); // NOI18N
        searchFileMenu.add(fileMenuLongDesc);

        jSeparator2.setName("jSeparator2"); // NOI18N
        searchFileMenu.add(jSeparator2);

        fileMenuDuplicateSearch.setAction(actionMap.get("duplicateSearch")); // NOI18N
        fileMenuDuplicateSearch.setName("fileMenuDuplicateSearch"); // NOI18N
        searchFileMenu.add(fileMenuDuplicateSearch);

        jSeparator22.setName("jSeparator22"); // NOI18N
        searchFileMenu.add(jSeparator22);

        fileMenuDeleteSearch.setAction(actionMap.get("removeSearchResult")); // NOI18N
        fileMenuDeleteSearch.setName("fileMenuDeleteSearch"); // NOI18N
        searchFileMenu.add(fileMenuDeleteSearch);

        fileMenuDeleteAll.setAction(actionMap.get("removeAllSearchResults")); // NOI18N
        fileMenuDeleteAll.setName("fileMenuDeleteAll"); // NOI18N
        searchFileMenu.add(fileMenuDeleteAll);

        jSeparator20.setName("jSeparator20"); // NOI18N
        searchFileMenu.add(jSeparator20);

        fileMenuExport.setAction(actionMap.get("exportEntries")); // NOI18N
        fileMenuExport.setName("fileMenuExport"); // NOI18N
        searchFileMenu.add(fileMenuExport);

        jSeparator13.setName("jSeparator13"); // NOI18N
        searchFileMenu.add(jSeparator13);

        fileMenuClose.setAction(actionMap.get("closeWindow")); // NOI18N
        fileMenuClose.setName("fileMenuClose"); // NOI18N
        searchFileMenu.add(fileMenuClose);

        searchMenuBar.add(searchFileMenu);

        searchEditMenu.setText(resourceMap.getString("searchEditMenu.text")); // NOI18N
        searchEditMenu.setName("searchEditMenu"); // NOI18N

        editMenuCopy.setAction(actionMap.get("copy"));
        editMenuCopy.setName("editMenuCopy"); // NOI18N
        searchEditMenu.add(editMenuCopy);

        editMenuSelectAll.setAction(actionMap.get("selectAll")); // NOI18N
        editMenuSelectAll.setName("editMenuSelectAll"); // NOI18N
        searchEditMenu.add(editMenuSelectAll);

        jSeparator10.setName("jSeparator10"); // NOI18N
        searchEditMenu.add(jSeparator10);

        editMenuDelete.setAction(actionMap.get("removeEntry")); // NOI18N
        editMenuDelete.setName("editMenuDelete"); // NOI18N
        searchEditMenu.add(editMenuDelete);

        jSeparator16.setName("jSeparator16"); // NOI18N
        searchEditMenu.add(jSeparator16);

        editMenuEditEntry.setAction(actionMap.get("editEntry")); // NOI18N
        editMenuEditEntry.setName("editMenuEditEntry"); // NOI18N
        searchEditMenu.add(editMenuEditEntry);

        editMenuDuplicateEntry.setAction(actionMap.get("duplicateEntry")); // NOI18N
        editMenuDuplicateEntry.setName("editMenuDuplicateEntry"); // NOI18N
        searchEditMenu.add(editMenuDuplicateEntry);

        editMenuFindReplace.setAction(actionMap.get("findAndReplace")); // NOI18N
        editMenuFindReplace.setName("editMenuFindReplace"); // NOI18N
        searchEditMenu.add(editMenuFindReplace);

        jSeparator4.setName("jSeparator4"); // NOI18N
        searchEditMenu.add(jSeparator4);

        editMenuDeleteEntry.setAction(actionMap.get("deleteEntryComplete")); // NOI18N
        editMenuDeleteEntry.setName("editMenuDeleteEntry"); // NOI18N
        searchEditMenu.add(editMenuDeleteEntry);

        jSeparator21.setName("jSeparator21"); // NOI18N
        searchEditMenu.add(jSeparator21);

        editMenuAddKeywordsToSelection.setAction(actionMap.get("addKeywordsToEntries")); // NOI18N
        editMenuAddKeywordsToSelection.setName("editMenuAddKeywordsToSelection"); // NOI18N
        searchEditMenu.add(editMenuAddKeywordsToSelection);

        editMenuAddAuthorsToSelection.setAction(actionMap.get("addAuthorsToEntries")); // NOI18N
        editMenuAddAuthorsToSelection.setName("editMenuAddAuthorsToSelection"); // NOI18N
        searchEditMenu.add(editMenuAddAuthorsToSelection);

        jSeparator1.setName("jSeparator1"); // NOI18N
        searchEditMenu.add(jSeparator1);

        editMenuManLinks.setAction(actionMap.get("addToManLinks")); // NOI18N
        editMenuManLinks.setName("editMenuManLinks"); // NOI18N
        searchEditMenu.add(editMenuManLinks);

        editMenuLuhmann.setAction(actionMap.get("addToLuhmann")); // NOI18N
        editMenuLuhmann.setName("editMenuLuhmann"); // NOI18N
        searchEditMenu.add(editMenuLuhmann);

        editMenuBookmarks.setAction(actionMap.get("addToBookmarks")); // NOI18N
        editMenuBookmarks.setName("editMenuBookmarks"); // NOI18N
        searchEditMenu.add(editMenuBookmarks);

        jSeparator6.setName("jSeparator6"); // NOI18N
        searchEditMenu.add(jSeparator6);

        editMenuDesktop.setAction(actionMap.get("addToDesktop")); // NOI18N
        editMenuDesktop.setName("editMenuDesktop"); // NOI18N
        searchEditMenu.add(editMenuDesktop);

        searchMenuBar.add(searchEditMenu);

        searchFilterMenu.setText(resourceMap.getString("searchFilterMenu.text")); // NOI18N
        searchFilterMenu.setName("searchFilterMenu"); // NOI18N

        filterSearch.setAction(actionMap.get("filterSearch")); // NOI18N
        filterSearch.setName("filterSearch"); // NOI18N
        searchFilterMenu.add(filterSearch);

        jSeparator14.setName("jSeparator14"); // NOI18N
        searchFilterMenu.add(jSeparator14);

        filterKeywords.setAction(actionMap.get("filterKeywords")); // NOI18N
        filterKeywords.setName("filterKeywords"); // NOI18N
        searchFilterMenu.add(filterKeywords);

        jSeparator15.setName("jSeparator15"); // NOI18N
        searchFilterMenu.add(jSeparator15);

        filterAuthors.setAction(actionMap.get("filterAuthors")); // NOI18N
        filterAuthors.setName("filterAuthors"); // NOI18N
        searchFilterMenu.add(filterAuthors);

        jSeparator23.setName("jSeparator23"); // NOI18N
        searchFilterMenu.add(jSeparator23);

        filterTopLevelLuhmann.setAction(actionMap.get("filterTopLevelLuhmann")); // NOI18N
        filterTopLevelLuhmann.setName("filterTopLevelLuhmann"); // NOI18N
        searchFilterMenu.add(filterTopLevelLuhmann);

        searchMenuBar.add(searchFilterMenu);

        searchSearchMenu.setText(resourceMap.getString("searchSearchMenu.text")); // NOI18N
        searchSearchMenu.setName("searchSearchMenu"); // NOI18N

        searchMenuSelectionContent.setAction(actionMap.get("newSearchFromSelection")); // NOI18N
        searchMenuSelectionContent.setName("searchMenuSelectionContent"); // NOI18N
        searchSearchMenu.add(searchMenuSelectionContent);

        jSeparator19.setName("jSeparator19"); // NOI18N
        searchSearchMenu.add(jSeparator19);

        searchMenuKeywordLogOr.setAction(actionMap.get("newSearchFromKeywordsLogOr")); // NOI18N
        searchMenuKeywordLogOr.setName("searchMenuKeywordLogOr"); // NOI18N
        searchSearchMenu.add(searchMenuKeywordLogOr);

        searchMenuKeywordLogAnd.setAction(actionMap.get("newSearchFromKeywordsLogAnd")); // NOI18N
        searchMenuKeywordLogAnd.setName("searchMenuKeywordLogAnd"); // NOI18N
        searchSearchMenu.add(searchMenuKeywordLogAnd);

        searchMenuKeywordLogNot.setAction(actionMap.get("newSearchFromKeywordsLogNot")); // NOI18N
        searchMenuKeywordLogNot.setName("searchMenuKeywordLogNot"); // NOI18N
        searchSearchMenu.add(searchMenuKeywordLogNot);

        searchMenuBar.add(searchSearchMenu);

        searchViewMenu.setText(resourceMap.getString("searchViewMenu.text")); // NOI18N
        searchViewMenu.setName("searchViewMenu"); // NOI18N

        viewMenuShowOnDesktop.setAction(actionMap.get("showEntryInDesktop")); // NOI18N
        viewMenuShowOnDesktop.setName("viewMenuShowOnDesktop"); // NOI18N
        searchViewMenu.add(viewMenuShowOnDesktop);

        jSeparator11.setName("jSeparator11"); // NOI18N
        searchViewMenu.add(jSeparator11);

        viewMenuHighlight.setAction(actionMap.get("toggleHighlightResults")); // NOI18N
        viewMenuHighlight.setSelected(true);
        viewMenuHighlight.setName("viewMenuHighlight"); // NOI18N
        searchViewMenu.add(viewMenuHighlight);

        viewMenuHighlightSettings.setAction(actionMap.get("showHighlightSettings")); // NOI18N
        viewMenuHighlightSettings.setName("viewMenuHighlightSettings"); // NOI18N
        searchViewMenu.add(viewMenuHighlightSettings);

        jSeparator9.setName("jSeparator9"); // NOI18N
        searchViewMenu.add(jSeparator9);

        viewMenuShowEntry.setAction(actionMap.get("showEntryImmediately")); // NOI18N
        viewMenuShowEntry.setSelected(true);
        viewMenuShowEntry.setName("viewMenuShowEntry"); // NOI18N
        searchViewMenu.add(viewMenuShowEntry);

        jSeparator7.setName("jSeparator7"); // NOI18N
        searchViewMenu.add(jSeparator7);

        jMenuItemSwitchLayout.setAction(actionMap.get("switchLayout")); // NOI18N
        jMenuItemSwitchLayout.setName("jMenuItemSwitchLayout"); // NOI18N
        searchViewMenu.add(jMenuItemSwitchLayout);

        jSeparator8.setName("jSeparator8"); // NOI18N
        searchViewMenu.add(jSeparator8);

        viewMenuFullScreen.setAction(actionMap.get("viewFullScreen")); // NOI18N
        viewMenuFullScreen.setName("viewMenuFullScreen"); // NOI18N
        searchViewMenu.add(viewMenuFullScreen);

        searchMenuBar.add(searchViewMenu);

        setJMenuBar(searchMenuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(searchToolbar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(searchMainPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addComponent(searchStatusPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(searchToolbar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(searchMainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(searchStatusPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

	public boolean isFullScreenSupp() {
		return data.isFullScreenSupported();
	}

	public void setFullScreenSupp(boolean b) {
		boolean old = isFullScreenSupp();
		this.data.setFullScreenSupport(b);
		firePropertyChange("fullScreenSupp", old, isFullScreenSupp());
	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem editMenuAddAuthorsToSelection;
    private javax.swing.JMenuItem editMenuAddKeywordsToSelection;
    private javax.swing.JMenuItem editMenuBookmarks;
    private javax.swing.JMenuItem editMenuCopy;
    private javax.swing.JMenuItem editMenuDelete;
    private javax.swing.JMenuItem editMenuDeleteEntry;
    private javax.swing.JMenuItem editMenuDesktop;
    private javax.swing.JMenuItem editMenuDuplicateEntry;
    private javax.swing.JMenuItem editMenuEditEntry;
    private javax.swing.JMenuItem editMenuFindReplace;
    private javax.swing.JMenuItem editMenuLuhmann;
    private javax.swing.JMenuItem editMenuManLinks;
    private javax.swing.JMenuItem editMenuSelectAll;
    private javax.swing.JMenuItem fileMenuClose;
    private javax.swing.JMenuItem fileMenuDeleteAll;
    private javax.swing.JMenuItem fileMenuDeleteSearch;
    private javax.swing.JMenuItem fileMenuDuplicateSearch;
    private javax.swing.JMenuItem fileMenuExport;
    private javax.swing.JMenuItem fileMenuLongDesc;
    private javax.swing.JMenuItem filterAuthors;
    private javax.swing.JMenuItem filterKeywords;
    private javax.swing.JMenuItem filterSearch;
    private javax.swing.JMenuItem filterTopLevelLuhmann;
    private javax.swing.JButton jButtonDeleteSearch;
    private javax.swing.JButton jButtonResetList;
    private javax.swing.JComboBox jComboBoxSearches;
    private javax.swing.JEditorPane jEditorPaneSearchEntry;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabelHits;
    private javax.swing.JList jListKeywords;
    private javax.swing.JMenuItem jMenuItemSwitchLayout;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator10;
    private javax.swing.JSeparator jSeparator11;
    private javax.swing.JToolBar.Separator jSeparator12;
    private javax.swing.JSeparator jSeparator13;
    private javax.swing.JSeparator jSeparator14;
    private javax.swing.JSeparator jSeparator15;
    private javax.swing.JSeparator jSeparator16;
    private javax.swing.JSeparator jSeparator19;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator20;
    private javax.swing.JSeparator jSeparator21;
    private javax.swing.JSeparator jSeparator22;
    private javax.swing.JPopupMenu.Separator jSeparator23;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JToolBar.Separator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JPopupMenu.Separator jSeparator7;
    private javax.swing.JPopupMenu.Separator jSeparator8;
    private javax.swing.JSeparator jSeparator9;
    private javax.swing.JSplitPane jSplitPaneSearch1;
    private javax.swing.JSplitPane jSplitPaneSearch2;
    private javax.swing.JTable jTableResults;
    private javax.swing.JTextField jTextFieldFilterList;
    private javax.swing.JMenu searchEditMenu;
    private javax.swing.JMenu searchFileMenu;
    private javax.swing.JMenu searchFilterMenu;
    private javax.swing.JPanel searchMainPanel;
    private javax.swing.JMenuBar searchMenuBar;
    private javax.swing.JMenuItem searchMenuKeywordLogAnd;
    private javax.swing.JMenuItem searchMenuKeywordLogNot;
    private javax.swing.JMenuItem searchMenuKeywordLogOr;
    private javax.swing.JMenuItem searchMenuSelectionContent;
    private javax.swing.JMenu searchSearchMenu;
    private javax.swing.JPanel searchStatusPanel;
    private javax.swing.JToolBar searchToolbar;
    private javax.swing.JMenu searchViewMenu;
    private javax.swing.JButton tb_bookmark;
    private javax.swing.JButton tb_copy;
    private javax.swing.JButton tb_desktop;
    private javax.swing.JButton tb_editentry;
    private javax.swing.JButton tb_highlight;
    private javax.swing.JButton tb_luhmann;
    private javax.swing.JButton tb_manlinks;
    private javax.swing.JButton tb_remove;
    private javax.swing.JButton tb_selectall;
    private javax.swing.JMenuItem viewMenuFullScreen;
    private javax.swing.JCheckBoxMenuItem viewMenuHighlight;
    private javax.swing.JMenuItem viewMenuHighlightSettings;
    private javax.swing.JCheckBoxMenuItem viewMenuShowEntry;
    private javax.swing.JMenuItem viewMenuShowOnDesktop;
    // End of variables declaration//GEN-END:variables

	private SearchResultsFrameData data = new SearchResultsFrameData(new DefaultListModel<String>(), false, java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment()
			.getDefaultScreenDevice(), org.jdesktop.application.Application
			.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getContext()
			.getResourceMap(SearchResultsFrame.class),
			org.jdesktop.application.Application
					.getInstance(de.danielluedecke.zettelkasten.ZettelkastenApp.class).getContext()
					.getResourceMap(ToolbarIcons.class), false, false, false, false);
}
