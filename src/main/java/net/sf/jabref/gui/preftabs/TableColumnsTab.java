/*  Copyright (C) 2003-2011 JabRef contributors.
    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc.,
    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package net.sf.jabref.gui.preftabs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import net.sf.jabref.*;
import net.sf.jabref.external.ExternalFileType;
import net.sf.jabref.gui.help.HelpAction;
import net.sf.jabref.specialfields.SpecialFieldsUtils;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

class TableColumnsTab extends JPanel implements PrefsTab {

    private final JabRefPreferences prefs;
    private boolean tableChanged = false;
    private final JTable colSetup;
    private int rowCount = -1;
    private int ncWidth = -1;
    private final Vector<TableRow> tableRows = new Vector<TableRow>(10);
    private final JabRefFrame frame;

    private final JCheckBox pdfColumn;
    private final JCheckBox urlColumn;
    private final JCheckBox fileColumn;
    private final JCheckBox arxivColumn;

    private final JCheckBox extraFileColumns;
    private JList listOfFileColumns;

    private final JRadioButton preferUrl;
    private final JRadioButton preferDoi;

    private final JCheckBox showOneLetterHeadingForIconColumns;

    /*** begin: special fields ***/
    private final JCheckBox specialFieldsEnabled;
    private JCheckBox rankingColumn;
    private JCheckBox compactRankingColumn;
    private JCheckBox qualityColumn;
    private JCheckBox priorityColumn;
    private JCheckBox relevanceColumn;
    private JCheckBox printedColumn;
    private JCheckBox readStatusColumn;
    private JRadioButton syncKeywords;
    private JRadioButton writeSpecialFields;
    private boolean oldSpecialFieldsEnabled;
    private boolean oldRankingColumn;
    private boolean oldCompcatRankingColumn;
    private boolean oldQualityColumn;
    private boolean oldPriorityColumn;
    private boolean oldRelevanceColumn;
    private boolean oldPrintedColumn;
    private boolean oldReadStatusColumn;
    private boolean oldSyncKeyWords;
    private boolean oldWriteSpecialFields;


    /*** end: special fields ***/

    static class TableRow {

        String name;
        int length;


        public TableRow(String name) {
            this.name = name;
            length = GUIGlobals.DEFAULT_FIELD_LENGTH;
        }

        public TableRow(int length) {
            this.length = length;
            name = "";
        }

        public TableRow(String name, int length) {
            this.name = name;
            this.length = length;
        }
    }


    /**
     * Customization of external program paths.
     *
     * @param prefs a <code>JabRefPreferences</code> value
     */
    public TableColumnsTab(JabRefPreferences prefs, JabRefFrame frame) {
        this.prefs = prefs;
        this.frame = frame;
        setLayout(new BorderLayout());

        TableModel tm = new AbstractTableModel() {

            @Override
            public int getRowCount() {
                return rowCount;
            }

            @Override
            public int getColumnCount() {
                return 2;
            }

            @Override
            public Object getValueAt(int row, int column) {
                if (row == 0) {
                    return column == 0 ? GUIGlobals.NUMBER_COL : "" + ncWidth;
                }
                row--;
                if (row >= tableRows.size()) {
                    return "";
                }
                Object rowContent = tableRows.elementAt(row);
                if (rowContent == null) {
                    return "";
                }
                TableRow tr = (TableRow) rowContent;
                switch (column) {
                case 0:
                    return tr.name;
                case 1:
                    return tr.length > 0 ? Integer.toString(tr.length) : "";
                }
                return null; // Unreachable.
            }

            @Override
            public String getColumnName(int col) {
                return col == 0 ? Globals.lang("Field name") : Globals.lang("Column width");
            }

            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 0) {
                    return String.class;
                } else {
                    return Integer.class;
                }
            }

            @Override
            public boolean isCellEditable(int row, int col) {
                return !(row == 0 && col == 0);
            }

            @Override
            public void setValueAt(Object value, int row, int col) {
                tableChanged = true;
                // Make sure the vector is long enough.
                while (row >= tableRows.size()) {
                    tableRows.add(new TableRow("", -1));
                }

                if (row == 0 && col == 1) {
                    ncWidth = Integer.parseInt(value.toString());
                    return;
                }

                TableRow rowContent = tableRows.elementAt(row - 1);

                if (col == 0) {
                    rowContent.name = value.toString();
                    if (getValueAt(row, 1).equals("")) {
                        setValueAt("" + GUIGlobals.DEFAULT_FIELD_LENGTH, row, 1);
                    }
                }
                else {
                    if (value == null) {
                        rowContent.length = -1;
                    } else {
                        rowContent.length = Integer.parseInt(value.toString());
                    }
                }
            }

        };

        colSetup = new JTable(tm);
        TableColumnModel cm = colSetup.getColumnModel();
        cm.getColumn(0).setPreferredWidth(140);
        cm.getColumn(1).setPreferredWidth(80);

        FormLayout layout = new FormLayout
                ("1dlu, 8dlu, left:pref, 4dlu, fill:pref","");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        JPanel pan = new JPanel();
        JPanel tabPanel = new JPanel();
        tabPanel.setLayout(new BorderLayout());
        JScrollPane sp = new JScrollPane
                (colSetup, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        colSetup.setPreferredScrollableViewportSize(new Dimension(250, 200));
        sp.setMinimumSize(new Dimension(250, 300));
        tabPanel.add(sp, BorderLayout.CENTER);
        JToolBar toolBar = new JToolBar(SwingConstants.VERTICAL);
        toolBar.setFloatable(false);
        AddRowAction addRow = new AddRowAction();
        DeleteRowAction deleteRow = new DeleteRowAction();
        MoveRowUpAction moveUp = new MoveRowUpAction();
        MoveRowDownAction moveDown = new MoveRowDownAction();
        toolBar.setBorder(null);
        toolBar.add(addRow);
        toolBar.add(deleteRow);
        toolBar.addSeparator();
        toolBar.add(moveUp);
        toolBar.add(moveDown);
        tabPanel.add(toolBar, BorderLayout.EAST);

        showOneLetterHeadingForIconColumns = new JCheckBox(Globals.lang("Show one letter heading for icon columns"));

        fileColumn = new JCheckBox(Globals.lang("Show file column"));
        pdfColumn = new JCheckBox(Globals.lang("Show PDF/PS column"));
        urlColumn = new JCheckBox(Globals.lang("Show URL/DOI column"));
        preferUrl = new JRadioButton(Globals.lang("Show URL first"));
        preferDoi = new JRadioButton(Globals.lang("Show DOI first"));
        ButtonGroup preferUrlDoiGroup = new ButtonGroup();
        preferUrlDoiGroup.add(preferUrl);
        preferUrlDoiGroup.add(preferDoi);

        urlColumn.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent arg0) {
                preferUrl.setEnabled(urlColumn.isSelected());
                preferDoi.setEnabled(urlColumn.isSelected());
            }
        });
        arxivColumn = new JCheckBox(Globals.lang("Show ArXiv column"));

        extraFileColumns = new JCheckBox(Globals.lang("Show Extra columns"));
        extraFileColumns.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent arg0) {
                listOfFileColumns.setEnabled(extraFileColumns.isSelected());
            }
        });
        ExternalFileType[] fileTypes = Globals.prefs.getExternalFileTypeSelection();
        String[] fileTypeNames = new String[fileTypes.length];
        for (int i = 0; i < fileTypes.length; i++) {
            fileTypeNames[i] = fileTypes[i].getName();
        }
        listOfFileColumns = new JList(fileTypeNames);
        JScrollPane listOfFileColumnsScrollPane = new JScrollPane(listOfFileColumns);
        listOfFileColumns.setVisibleRowCount(3);

        /*** begin: special table columns and special fields ***/

        HelpAction help = new HelpAction(frame.helpDiag, GUIGlobals.specialFieldsHelp);
        JButton helpButton = new JButton(GUIGlobals.getImage("helpSmall"));
        helpButton.setToolTipText(Globals.lang("Help on special fields"));
        helpButton.addActionListener(help);

        specialFieldsEnabled = new JCheckBox(Globals.lang("Enable special fields"));
        specialFieldsEnabled.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent event) {
                boolean isEnabled = specialFieldsEnabled.isSelected();
                rankingColumn.setEnabled(isEnabled);
                compactRankingColumn.setEnabled(isEnabled && rankingColumn.isSelected());
                qualityColumn.setEnabled(isEnabled);
                priorityColumn.setEnabled(isEnabled);
                relevanceColumn.setEnabled(isEnabled);
                printedColumn.setEnabled(isEnabled);
                readStatusColumn.setEnabled(isEnabled);
                syncKeywords.setEnabled(isEnabled);
                writeSpecialFields.setEnabled(isEnabled);
            }
        });
        rankingColumn = new JCheckBox(Globals.lang("Show rank"));
        rankingColumn.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent event) {
                compactRankingColumn.setEnabled(rankingColumn.isSelected());
            }
        });
        compactRankingColumn = new JCheckBox(Globals.lang("Compact rank"));
        qualityColumn = new JCheckBox(Globals.lang("Show quality"));
        priorityColumn = new JCheckBox(Globals.lang("Show priority"));
        relevanceColumn = new JCheckBox(Globals.lang("Show relevance"));
        printedColumn = new JCheckBox(Globals.lang("Show printed status"));
        readStatusColumn = new JCheckBox(Globals.lang("Show read status"));

        // "sync keywords" and "write special" fields may be configured mutually exclusive only
        // The implementation supports all combinations (TRUE+TRUE and FALSE+FALSE, even if the latter does not make sense)
        // To avoid confusion, we opted to make the setting mutually exclusive
        syncKeywords = new JRadioButton(Globals.lang("Synchronize with keywords"));
        writeSpecialFields = new JRadioButton(Globals.lang("Write values of special fields as separate fields to BibTeX"));
        ButtonGroup group = new ButtonGroup();
        group.add(syncKeywords);
        group.add(writeSpecialFields);

        builder.appendSeparator(Globals.lang("Special table columns"));
        builder.nextLine();
        builder.append(pan);

        DefaultFormBuilder specialTableColumnsBuilder = new DefaultFormBuilder(new FormLayout(
                "8dlu, 8dlu, 8cm, 8dlu, 8dlu, left:pref:grow", "pref, pref, pref, pref, pref, pref, pref, pref, pref, pref, pref, pref, pref"));
        CellConstraints cc = new CellConstraints();

        specialTableColumnsBuilder.add(specialFieldsEnabled, cc.xyw(1, 1, 3));
        specialTableColumnsBuilder.add(rankingColumn, cc.xyw(2, 2, 2));
        specialTableColumnsBuilder.add(compactRankingColumn, cc.xy(3, 3));
        specialTableColumnsBuilder.add(relevanceColumn, cc.xyw(2, 4, 2));
        specialTableColumnsBuilder.add(qualityColumn, cc.xyw(2, 5, 2));
        specialTableColumnsBuilder.add(priorityColumn, cc.xyw(2, 6, 2));
        specialTableColumnsBuilder.add(printedColumn, cc.xyw(2, 7, 2));
        specialTableColumnsBuilder.add(readStatusColumn, cc.xyw(2, 8, 2));
        specialTableColumnsBuilder.add(syncKeywords, cc.xyw(2, 10, 2));
        specialTableColumnsBuilder.add(writeSpecialFields, cc.xyw(2, 11, 2));
        specialTableColumnsBuilder.add(showOneLetterHeadingForIconColumns, cc.xyw(1, 12, 4));
        specialTableColumnsBuilder.add(helpButton, cc.xyw(1, 13, 2));

        specialTableColumnsBuilder.add(fileColumn, cc.xyw(5, 1, 2));
        specialTableColumnsBuilder.add(pdfColumn, cc.xyw(5, 2, 2));
        specialTableColumnsBuilder.add(urlColumn, cc.xyw(5, 3, 2));
        specialTableColumnsBuilder.add(preferUrl, cc.xy(6, 4));
        specialTableColumnsBuilder.add(preferDoi, cc.xy(6, 5));
        specialTableColumnsBuilder.add(arxivColumn, cc.xyw(5, 6, 2));

        specialTableColumnsBuilder.add(extraFileColumns, cc.xyw(5, 7, 2));
        specialTableColumnsBuilder.add(listOfFileColumnsScrollPane, cc.xywh(5, 8, 2, 5));

        builder.append(specialTableColumnsBuilder.getPanel());
        builder.nextLine();

        /*** end: special table columns and special fields ***/

        builder.appendSeparator(Globals.lang("Entry table columns"));
        builder.nextLine();
        builder.append(pan);
        builder.append(tabPanel);
        builder.nextLine();
        builder.append(pan);
        JButton buttonWidth = new JButton(new UpdateWidthsAction());
        JButton buttonOrder = new JButton(new UpdateOrderAction());
        builder.append(buttonWidth);
        builder.nextLine();
        builder.append(pan);
        builder.append(buttonOrder);
        builder.nextLine();
        builder.append(pan);
        builder.nextLine();
        pan = builder.getPanel();
        pan.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(pan, BorderLayout.CENTER);
    }

    @Override
    public void setValues() {
        fileColumn.setSelected(prefs.getBoolean(JabRefPreferences.FILE_COLUMN));
        pdfColumn.setSelected(prefs.getBoolean(JabRefPreferences.PDF_COLUMN));
        urlColumn.setSelected(prefs.getBoolean(JabRefPreferences.URL_COLUMN));
        preferUrl.setSelected(!prefs.getBoolean(JabRefPreferences.PREFER_URL_DOI));
        preferDoi.setSelected(prefs.getBoolean(JabRefPreferences.PREFER_URL_DOI));
        fileColumn.setSelected(prefs.getBoolean(JabRefPreferences.FILE_COLUMN));
        arxivColumn.setSelected(prefs.getBoolean(JabRefPreferences.ARXIV_COLUMN));

        extraFileColumns.setSelected(prefs.getBoolean(JabRefPreferences.EXTRA_FILE_COLUMNS));
        if (extraFileColumns.isSelected()) {
            String[] desiredColumns = prefs.getStringArray(JabRefPreferences.LIST_OF_FILE_COLUMNS);
            int listSize = listOfFileColumns.getModel().getSize();
            int[] indicesToSelect = new int[listSize];
            for (int i = 0; i < listSize; i++) {
                indicesToSelect[i] = listSize + 1;
                for (String desiredColumn : desiredColumns) {
                    if (listOfFileColumns.getModel().getElementAt(i).equals(desiredColumn)) {
                        indicesToSelect[i] = i;
                        break;
                    }
                }
            }
            listOfFileColumns.setSelectedIndices(indicesToSelect);
        }
        else {
            listOfFileColumns.setSelectedIndices(new int[] {});
        }

        /*** begin: special fields ***/

        oldRankingColumn = prefs.getBoolean(SpecialFieldsUtils.PREF_SHOWCOLUMN_RANKING);
        rankingColumn.setSelected(oldRankingColumn);

        oldCompcatRankingColumn = prefs.getBoolean(SpecialFieldsUtils.PREF_RANKING_COMPACT);
        compactRankingColumn.setSelected(oldCompcatRankingColumn);

        oldQualityColumn = prefs.getBoolean(SpecialFieldsUtils.PREF_SHOWCOLUMN_QUALITY);
        qualityColumn.setSelected(oldQualityColumn);

        oldPriorityColumn = prefs.getBoolean(SpecialFieldsUtils.PREF_SHOWCOLUMN_PRIORITY);
        priorityColumn.setSelected(oldPriorityColumn);

        oldRelevanceColumn = prefs.getBoolean(SpecialFieldsUtils.PREF_SHOWCOLUMN_RELEVANCE);
        relevanceColumn.setSelected(oldRelevanceColumn);

        oldPrintedColumn = prefs.getBoolean(SpecialFieldsUtils.PREF_SHOWCOLUMN_PRINTED);
        printedColumn.setSelected(oldPrintedColumn);

        oldReadStatusColumn = prefs.getBoolean(SpecialFieldsUtils.PREF_SHOWCOLUMN_READ);
        readStatusColumn.setSelected(oldReadStatusColumn);

        oldSyncKeyWords = prefs.getBoolean(SpecialFieldsUtils.PREF_AUTOSYNCSPECIALFIELDSTOKEYWORDS);
        syncKeywords.setSelected(oldSyncKeyWords);

        oldWriteSpecialFields = prefs.getBoolean(SpecialFieldsUtils.PREF_SERIALIZESPECIALFIELDS);
        writeSpecialFields.setSelected(oldWriteSpecialFields);

        // has to be called as last to correctly enable/disable the other settings
        oldSpecialFieldsEnabled = prefs.getBoolean(SpecialFieldsUtils.PREF_SPECIALFIELDSENABLED);
        specialFieldsEnabled.setSelected(!oldSpecialFieldsEnabled);
        specialFieldsEnabled.setSelected(oldSpecialFieldsEnabled); // Call twice to make sure the ChangeListener is triggered

        /*** end: special fields ***/

        boolean oldShowOneLetterHeadingForIconColumns = prefs.getBoolean(JabRefPreferences.SHOW_ONE_LETTER_HEADING_FOR_ICON_COLUMNS);
        showOneLetterHeadingForIconColumns.setSelected(oldShowOneLetterHeadingForIconColumns);

        tableRows.clear();
        String[] names = prefs.getStringArray(JabRefPreferences.COLUMN_NAMES);
        String[] lengths = prefs.getStringArray(JabRefPreferences.COLUMN_WIDTHS);
        for (int i = 0; i < names.length; i++) {
            if (i < lengths.length) {
                tableRows.add(new TableRow(names[i], Integer.parseInt(lengths[i])));
            } else {
                tableRows.add(new TableRow(names[i]));
            }
        }
        rowCount = tableRows.size() + 5;
        ncWidth = prefs.getInt(JabRefPreferences.NUMBER_COL_WIDTH);

    }


    class DeleteRowAction extends AbstractAction {

        public DeleteRowAction() {
            super("Delete row", GUIGlobals.getImage("remove"));
            putValue(Action.SHORT_DESCRIPTION, Globals.lang("Delete rows"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int[] rows = colSetup.getSelectedRows();
            if (rows.length == 0) {
                return;
            }
            int offs = 0;
            for (int i = rows.length - 1; i >= 0; i--) {
                if (rows[i] <= tableRows.size() && rows[i] != 0) {
                    tableRows.remove(rows[i] - 1);
                    offs++;
                }
            }
            rowCount -= offs;
            if (rows.length > 1) {
                colSetup.clearSelection();
            }
            colSetup.revalidate();
            colSetup.repaint();
            tableChanged = true;
        }
    }

    class AddRowAction extends AbstractAction {

        public AddRowAction() {
            super("Add row", GUIGlobals.getImage("add"));
            putValue(Action.SHORT_DESCRIPTION, Globals.lang("Insert rows"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int[] rows = colSetup.getSelectedRows();
            if (rows.length == 0) {
                // No rows selected, so we just add one at the end.
                rowCount++;
                colSetup.revalidate();
                colSetup.repaint();
                return;
            }
            for (int i = 0; i < rows.length; i++) {
                if (rows[i] + i - 1 < tableRows.size()) {
                    tableRows.add(Math.max(0, rows[i] + i - 1), new TableRow(GUIGlobals.DEFAULT_FIELD_LENGTH));
                }
            }
            rowCount += rows.length;
            if (rows.length > 1) {
                colSetup.clearSelection();
            }
            colSetup.revalidate();
            colSetup.repaint();
            tableChanged = true;
        }
    }

    abstract class AbstractMoveRowAction extends AbstractAction {

        public AbstractMoveRowAction(String string, ImageIcon image) {
            super(string, image);
        }

        void swap(int i, int j) {
            if (i < 0 || i >= tableRows.size()) {
                return;
            }
            if (j < 0 || j >= tableRows.size()) {
                return;
            }
            TableRow tmp = tableRows.get(i);
            tableRows.set(i, tableRows.get(j));
            tableRows.set(j, tmp);
        }
    }

    class MoveRowUpAction extends AbstractMoveRowAction {

        public MoveRowUpAction() {
            super("Up", GUIGlobals.getImage("up"));
            putValue(Action.SHORT_DESCRIPTION, Globals.lang("Move up"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int[] selected = colSetup.getSelectedRows();
            Arrays.sort(selected);
            // first element (#) not inside tableRows
            // don't move if a selected element is at bounce
            if (selected.length > 0 && selected[0] > 1) {
                boolean[] newSelected = new boolean[colSetup.getRowCount()];
                for (int i : selected) {
                    swap(i - 1, i - 2);
                    newSelected[i - 1] = true;
                }
                // select all and remove unselected
                colSetup.setRowSelectionInterval(0, colSetup.getRowCount() - 1);
                for (int i = 0; i < colSetup.getRowCount(); i++) {
                    if (!newSelected[i]) {
                        colSetup.removeRowSelectionInterval(i, i);
                    }
                }
                colSetup.revalidate();
                colSetup.repaint();
                tableChanged = true;
            }
        }
    }

    class MoveRowDownAction extends AbstractMoveRowAction {

        public MoveRowDownAction() {
            super("Down", GUIGlobals.getImage("down"));
            putValue(Action.SHORT_DESCRIPTION, Globals.lang("Down"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int[] selected = colSetup.getSelectedRows();
            Arrays.sort(selected);
            final int last = selected.length - 1;
            boolean[] newSelected = new boolean[colSetup.getRowCount()];
            // don't move if a selected element is at bounce
            if (selected.length > 0 && selected[last] < tableRows.size()) {
                for (int i = last; i >= 0; i--) {
                    swap(selected[i] - 1, selected[i]);
                    newSelected[selected[i] + 1] = true;
                }
                // select all and remove unselected
                colSetup.setRowSelectionInterval(0, colSetup.getRowCount() - 1);
                for (int i = 0; i < colSetup.getRowCount(); i++) {
                    if (!newSelected[i]) {
                        colSetup.removeRowSelectionInterval(i, i);
                    }
                }
                colSetup.revalidate();
                colSetup.repaint();
                tableChanged = true;
            }
        }
    }

    class UpdateOrderAction extends AbstractAction {

        public UpdateOrderAction() {
            super(Globals.lang("Update to current column order"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            BasePanel panel = frame.basePanel();
            if (panel == null) {
                return;
            }
            // idea: sort elements according to value stored in hash, keep
            // everything not inside hash/mainTable as it was
            final HashMap<String, Integer> map = new HashMap<String, Integer>();

            // first element (#) not inside tableRows
            for (int i = 1; i < panel.mainTable.getColumnCount(); i++) {
                String name = panel.mainTable.getColumnName(i);
                if (name != null && !name.isEmpty()) {
                    map.put(name.toLowerCase(), i);
                }
            }
            Collections.sort(tableRows, new Comparator<TableRow>() {

                @Override
                public int compare(TableRow o1, TableRow o2) {
                    Integer n1 = map.get(o1.name);
                    Integer n2 = map.get(o2.name);
                    if (n1 == null || n2 == null) {
                        return 0;
                    }
                    return n1.compareTo(n2);
                }
            });

            colSetup.revalidate();
            colSetup.repaint();
            tableChanged = true;
        }
    }

    class UpdateWidthsAction extends AbstractAction {

        public UpdateWidthsAction() {
            super(Globals.lang("Update to current column widths"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            BasePanel panel = frame.basePanel();
            if (panel == null) {
                return;
            }
            TableColumnModel colMod = panel.mainTable.getColumnModel();
            colSetup.setValueAt("" + colMod.getColumn(0).getWidth(), 0, 1);
            for (int i = 1; i < colMod.getColumnCount(); i++) {
                try {
                    String name = panel.mainTable.getColumnName(i).toLowerCase();
                    int width = colMod.getColumn(i).getWidth();
                    if (i <= tableRows.size() && ((String) colSetup.getValueAt(i, 0)).toLowerCase().equals(name)) {
                        colSetup.setValueAt("" + width, i, 1);
                    } else { // Doesn't match; search for a matching col in our table
                        for (int j = 0; j < colSetup.getRowCount(); j++) {
                            if (j < tableRows.size() &&
                                    ((String) colSetup.getValueAt(j, 0)).toLowerCase().equals(name)) {
                                colSetup.setValueAt("" + width, j, 1);
                                break;
                            }
                        }
                    }
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
                colSetup.revalidate();
                colSetup.repaint();
            }

        }
    }


    /**
     * Store changes to table preferences. This method is called when
     * the user clicks Ok.
     *
     */
    @Override
    public void storeSettings() {
        prefs.putBoolean(JabRefPreferences.FILE_COLUMN, fileColumn.isSelected());
        prefs.putBoolean(JabRefPreferences.PDF_COLUMN, pdfColumn.isSelected());
        prefs.putBoolean(JabRefPreferences.URL_COLUMN, urlColumn.isSelected());
        prefs.putBoolean(JabRefPreferences.PREFER_URL_DOI, preferDoi.isSelected());
        prefs.putBoolean(JabRefPreferences.ARXIV_COLUMN, arxivColumn.isSelected());

        prefs.putBoolean(JabRefPreferences.EXTRA_FILE_COLUMNS, extraFileColumns.isSelected());
        if (extraFileColumns.isSelected() && !listOfFileColumns.isSelectionEmpty()) {
            String[] selections = new String[listOfFileColumns.getSelectedIndices().length];
            for (int i = 0; i < selections.length; i++) {
                selections[i] = (String) listOfFileColumns.getModel().getElementAt(
                        listOfFileColumns.getSelectedIndices()[i]);
            }
            prefs.putStringArray(JabRefPreferences.LIST_OF_FILE_COLUMNS, selections);
        }
        else {
            prefs.putStringArray(JabRefPreferences.LIST_OF_FILE_COLUMNS, new String[]{});
        }

        prefs.putBoolean(JabRefPreferences.SHOW_ONE_LETTER_HEADING_FOR_ICON_COLUMNS, showOneLetterHeadingForIconColumns.isSelected());

        /*** begin: special fields ***/

        boolean newSpecialFieldsEnabled = specialFieldsEnabled.isSelected();
        boolean newRankingColumn = rankingColumn.isSelected();
        boolean newCompactRankingColumn = compactRankingColumn.isSelected();
        boolean newQualityColumn = qualityColumn.isSelected();
        boolean newPriorityColumn = priorityColumn.isSelected();
        boolean newRelevanceColumn = relevanceColumn.isSelected();
        boolean newPrintedColumn = printedColumn.isSelected();
        boolean newReadStatusColumn = readStatusColumn.isSelected();
        boolean newSyncKeyWords = syncKeywords.isSelected();
        boolean newWriteSpecialFields = writeSpecialFields.isSelected();

        boolean restartRequired;
        restartRequired = oldSpecialFieldsEnabled != newSpecialFieldsEnabled ||
                oldRankingColumn != newRankingColumn ||
                oldCompcatRankingColumn != newCompactRankingColumn ||
                oldQualityColumn != newQualityColumn ||
                oldPriorityColumn != newPriorityColumn ||
                oldRelevanceColumn != newRelevanceColumn ||
                oldPrintedColumn != newPrintedColumn ||
                oldReadStatusColumn != newReadStatusColumn ||
                oldSyncKeyWords != newSyncKeyWords ||
                oldWriteSpecialFields != newWriteSpecialFields;
        if (restartRequired) {
            JOptionPane.showMessageDialog(null,
                    Globals.lang("You have changed settings for special fields.")
                            .concat(" ")
                            .concat(Globals.lang("You must restart JabRef for this to come into effect.")),
                    Globals.lang("Changed special field settings"),
                    JOptionPane.WARNING_MESSAGE);
        }

        // restart required implies that the settings have been changed
        // the seetings need to be stored
        if (restartRequired) {
            prefs.putBoolean(SpecialFieldsUtils.PREF_SPECIALFIELDSENABLED, newSpecialFieldsEnabled);
            prefs.putBoolean(SpecialFieldsUtils.PREF_SHOWCOLUMN_RANKING, newRankingColumn);
            prefs.putBoolean(SpecialFieldsUtils.PREF_RANKING_COMPACT, newCompactRankingColumn);
            prefs.putBoolean(SpecialFieldsUtils.PREF_SHOWCOLUMN_PRIORITY, newPriorityColumn);
            prefs.putBoolean(SpecialFieldsUtils.PREF_SHOWCOLUMN_QUALITY, newQualityColumn);
            prefs.putBoolean(SpecialFieldsUtils.PREF_SHOWCOLUMN_RELEVANCE, newRelevanceColumn);
            prefs.putBoolean(SpecialFieldsUtils.PREF_SHOWCOLUMN_PRINTED, newPrintedColumn);
            prefs.putBoolean(SpecialFieldsUtils.PREF_SHOWCOLUMN_READ, newReadStatusColumn);
            prefs.putBoolean(SpecialFieldsUtils.PREF_AUTOSYNCSPECIALFIELDSTOKEYWORDS, newSyncKeyWords);
            prefs.putBoolean(SpecialFieldsUtils.PREF_SERIALIZESPECIALFIELDS, newWriteSpecialFields);
        }

        /*** end: special fields ***/

        if (colSetup.isEditing()) {
            int col = colSetup.getEditingColumn();
            int row = colSetup.getEditingRow();
            colSetup.getCellEditor(row, col).stopCellEditing();
        }

        // Now we need to make sense of the contents the user has made to the
        // table setup table.
        if (tableChanged) {
            // First we remove all rows with empty names.
            int i = 0;
            while (i < tableRows.size()) {
                if (tableRows.elementAt(i).name.isEmpty()) {
                    tableRows.removeElementAt(i);
                } else {
                    i++;
                }
            }
            // Then we make arrays
            String[] names = new String[tableRows.size()];
            String[] widths = new String[tableRows.size()];
            int[] nWidths = new int[tableRows.size()];

            prefs.putInt(JabRefPreferences.NUMBER_COL_WIDTH, ncWidth);
            for (i = 0; i < tableRows.size(); i++) {
                TableRow tr = tableRows.elementAt(i);
                names[i] = tr.name.toLowerCase();
                nWidths[i] = tr.length;
                widths[i] = "" + tr.length;
            }

            // Finally, we store the new preferences.
            prefs.putStringArray(JabRefPreferences.COLUMN_NAMES, names);
            prefs.putStringArray(JabRefPreferences.COLUMN_WIDTHS, widths);
        }

    }

    @Override
    public boolean validateSettings() {
        return true;
    }

    @Override
    public String getTabName() {
        return Globals.lang("Entry table columns");
    }
}
