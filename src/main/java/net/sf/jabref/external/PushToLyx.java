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
package net.sf.jabref.external;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.*;

import net.sf.jabref.*;

public class PushToLyx implements PushToApplication {

    private final JTextField lyxPipe = new JTextField(30);
    private JPanel settings = null;

    private boolean couldNotFindPipe = false;
    private boolean couldNotWrite = false;


    @Override
    public void pushEntries(BibtexDatabase database, final BibtexEntry[] entries, final String keyString, MetaData metaData) {

        couldNotFindPipe = false;
        couldNotWrite = false;

        String lyxpipeSetting = Globals.prefs.get(JabRefPreferences.LYXPIPE);
        if (!lyxpipeSetting.endsWith(".in")) {
            lyxpipeSetting = lyxpipeSetting + ".in";
        }
        File lp = new File(lyxpipeSetting); // this needs to fixed because it gives "asdf" when going prefs.get("lyxpipe")
        if (!lp.exists() || !lp.canWrite()) {
            // See if it helps to append ".in":
            lp = new File(lyxpipeSetting + ".in");
            if (!lp.exists() || !lp.canWrite()) {
                couldNotFindPipe = true;
                return;
            }
        }

        final File lyxpipe = lp;

        JabRefExecutorService.INSTANCE.executeAndWait(new Runnable() {

            @Override
            public void run() {
                try {
                    FileWriter fw = new FileWriter(lyxpipe);
                    BufferedWriter lyx_out = new BufferedWriter(fw);
                    String citeStr;

                    citeStr = "LYXCMD:sampleclient:citation-insert:" + keyString;
                    lyx_out.write(citeStr + "\n");

                    lyx_out.close();

                } catch (IOException excep) {
                    couldNotWrite = true;
                }
            }
        });
    }

    @Override
    public String getName() {
        return Globals.lang("Insert selected citations into LyX/Kile");
    }

    @Override
    public String getApplicationName() {
        return "LyX/Kile";
    }

    @Override
    public String getTooltip() {
        return Globals.lang("Push selection to LyX/Kile");
    }

    @Override
    public Icon getIcon() {
        return GUIGlobals.getImage("lyx");
    }

    @Override
    public String getKeyStrokeName() {
        return "Push to LyX";
    }

    @Override
    public void operationCompleted(BasePanel panel) {
        if (couldNotFindPipe) {
            panel.output(Globals.lang("Error") + ": " + Globals.lang("verify that LyX is running and that the lyxpipe is valid")
                    + ". [" + Globals.prefs.get(JabRefPreferences.LYXPIPE) + "]");
        } else if (couldNotWrite) {
            panel.output(Globals.lang("Error") + ": " + Globals.lang("unable to write to") + " " + Globals.prefs.get(JabRefPreferences.LYXPIPE) +
                    ".in");
        } else {

            String message = "";
            panel.output(Globals.lang("Pushed the citations for the following rows to") + " Lyx: " +
                    message);
        }

    }

    @Override
    public boolean requiresBibtexKeys() {
        return true;
    }

    @Override
    public JPanel getSettingsPanel() {
        if (settings == null) {
            initSettingsPanel();
        }
        lyxPipe.setText(Globals.prefs.get(JabRefPreferences.LYXPIPE));
        return settings;
    }

    @Override
    public void storeSettings() {
        Globals.prefs.put(JabRefPreferences.LYXPIPE, lyxPipe.getText());
    }

    private void initSettingsPanel() {
        settings = new JPanel();
        settings.add(new JLabel(Globals.lang("Path to LyX pipe") + ":"));
        settings.add(lyxPipe);
    }
    /*class Timeout extends javax.swing.Timer
    {
      public Timeout(int timeout, final Thread toStop, final String message) {
        super(timeout, new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            toStop.stop();         // !!! <- deprecated
            // toStop.interrupt(); // better ?, interrupts wait and IO
            //stop();
            //output(message);
          }
        });
      }
    } */

}
