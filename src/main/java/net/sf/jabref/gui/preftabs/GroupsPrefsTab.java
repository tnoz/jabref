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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.*;

import net.sf.jabref.Globals;
import net.sf.jabref.JabRefPreferences;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

class GroupsPrefsTab extends JPanel implements PrefsTab {

    private final JCheckBox showIcons = new JCheckBox(Globals.lang("Show icons for groups"));
    private final JCheckBox showDynamic = new JCheckBox(
            "<html>" + Globals.lang("Show dynamic groups in <i>italics</i>") + "</html>");
    private final JCheckBox expandTree = new JCheckBox(
            Globals.lang("Initially show groups tree expanded"));
    private final JCheckBox autoShow = new JCheckBox(
            Globals.lang("Automatically show groups interface when switching to a database that contains groups"));
    private final JCheckBox autoHide = new JCheckBox(
            Globals.lang("Automatically hide groups interface when switching to a database that contains no groups"));
    private final JCheckBox autoAssignGroup = new JCheckBox(
            Globals.lang("Automatically assign new entry to selected groups"));
    private final JTextField groupingField = new JTextField(20);
    private final JTextField keywordSeparator = new JTextField(2);

    private final JabRefPreferences prefs;


    public GroupsPrefsTab(JabRefPreferences prefs) {
        this.prefs = prefs;

        keywordSeparator.addFocusListener(new FocusListener() {

            @Override
            public void focusGained(FocusEvent e) {
                keywordSeparator.selectAll();
            }

            @Override
            public void focusLost(FocusEvent e) {
                // deselection is automatic
            }
        });

        FormLayout layout = new FormLayout("9dlu, pref", //500px",
        "p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, " +
                "p, 3dlu, p");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.appendSeparator(Globals.lang("View"));
        builder.nextLine();
        builder.nextLine();
        builder.nextColumn();
        builder.append(showIcons);
        builder.nextLine();
        builder.nextLine();
        builder.nextColumn();
        builder.append(showDynamic);
        builder.nextLine();
        builder.nextLine();
        builder.nextColumn();
        builder.append(expandTree);
        builder.nextLine();
        builder.nextLine();
        builder.nextColumn();
        builder.append(autoShow);
        builder.nextLine();
        builder.nextLine();
        builder.nextColumn();
        builder.append(autoHide);
        builder.nextLine();
        builder.nextLine();
        builder.nextColumn();
        builder.append(autoAssignGroup);
        builder.nextLine();
        builder.nextLine();
        builder.appendSeparator(Globals.lang("Dynamic groups"));
        builder.nextLine();
        builder.nextLine();
        builder.nextColumn();
        // build subcomponent
        FormLayout layout2 = new FormLayout("left:pref, 2dlu, left:pref",
                "p, 3dlu, p");
        DefaultFormBuilder builder2 = new DefaultFormBuilder(layout2);
        builder2.append(new JLabel(Globals.lang("Default grouping field") + ":"));
        builder2.append(groupingField);
        builder2.nextLine();
        builder2.nextLine();
        builder2.append(new JLabel(Globals.lang("When adding/removing keywords, separate them by") + ":"));
        builder2.append(keywordSeparator);
        builder.append(builder2.getPanel());

        setLayout(new BorderLayout());
        JPanel panel = builder.getPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(panel, BorderLayout.CENTER);
    }

    @Override
    public void setValues() {
        showIcons.setSelected(prefs.getBoolean(JabRefPreferences.GROUP_SHOW_ICONS));
        showDynamic.setSelected(prefs.getBoolean(JabRefPreferences.GROUP_SHOW_DYNAMIC));
        expandTree.setSelected(prefs.getBoolean(JabRefPreferences.GROUP_EXPAND_TREE));
        groupingField.setText(prefs.get(JabRefPreferences.GROUPS_DEFAULT_FIELD));
        autoShow.setSelected(prefs.getBoolean(JabRefPreferences.GROUP_AUTO_SHOW));
        autoHide.setSelected(prefs.getBoolean(JabRefPreferences.GROUP_AUTO_HIDE));
        keywordSeparator.setText(prefs.get(JabRefPreferences.GROUP_KEYWORD_SEPARATOR));
        autoAssignGroup.setSelected(prefs.getBoolean(JabRefPreferences.AUTO_ASSIGN_GROUP));
    }

    @Override
    public void storeSettings() {
        prefs.putBoolean(JabRefPreferences.GROUP_SHOW_ICONS, showIcons.isSelected());
        prefs.putBoolean(JabRefPreferences.GROUP_SHOW_DYNAMIC, showDynamic.isSelected());
        prefs.putBoolean(JabRefPreferences.GROUP_EXPAND_TREE, expandTree.isSelected());
        prefs.put(JabRefPreferences.GROUPS_DEFAULT_FIELD, groupingField.getText().trim());
        prefs.putBoolean(JabRefPreferences.GROUP_AUTO_SHOW, autoShow.isSelected());
        prefs.putBoolean(JabRefPreferences.GROUP_AUTO_HIDE, autoHide.isSelected());
        prefs.putBoolean(JabRefPreferences.AUTO_ASSIGN_GROUP, autoAssignGroup.isSelected());
        prefs.put(JabRefPreferences.GROUP_KEYWORD_SEPARATOR, keywordSeparator.getText());
    }

    @Override
    public boolean validateSettings() {
        return true;
    }

    @Override
    public String getTabName() {
        return Globals.lang("Groups");
    }

}
