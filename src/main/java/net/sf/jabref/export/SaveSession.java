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
package net.sf.jabref.export;

import net.sf.jabref.JabRefPreferences;
import net.sf.jabref.logic.util.FileBasedLock;
import net.sf.jabref.util.FileUtil;
import net.sf.jabref.Globals;
import net.sf.jabref.GUIGlobals;

import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.nio.charset.UnsupportedCharsetException;

/**
 * Class used to handle safe storage to disk. 
 * 
 * Usage: create a SaveSession giving the file to save to, the
 * encoding, and whether to make a backup. The SaveSession will provide a Writer to store to, which actually
 * goes to a temporary file. The Writer keeps track of whether all characters could be saved, and if not,
 * which characters were not encodable.
 * 
 * After saving is finished, the client should close the Writer. If the save should be put into effect, call
 * commit(), otherwise call cancel(). When cancelling, the temporary file is simply deleted and the target
 * file remains unchanged. When committing, the temporary file is copied to the target file after making
 * a backup if requested and if the target file already existed, and finally the temporary file is deleted.
 * 
 * If committing fails, the temporary file will not be deleted.
 */
public class SaveSession {

    public static final String LOCKFILE_SUFFIX = ".lock";
    // The age in ms of a lockfile before JabRef will offer to "steal" the locked file:
    public static final long LOCKFILE_CRITICAL_AGE = 60000;

    private static final String TEMP_PREFIX = "jabref";
    private static final String TEMP_SUFFIX = "save.bib";

    private final File file;
    private File tmp;
    File backupFile;
    private String encoding;
    private boolean backup;
    private boolean useLockFile;
    private VerifyingWriter writer;


    public SaveSession(File file, String encoding, boolean backup) throws IOException,
            UnsupportedCharsetException {
        this.file = file;
        tmp = File.createTempFile(SaveSession.TEMP_PREFIX, SaveSession.TEMP_SUFFIX);
        useLockFile = Globals.prefs.getBoolean(JabRefPreferences.USE_LOCK_FILES);
        this.backup = backup;
        this.encoding = encoding;
        writer = new VerifyingWriter(new FileOutputStream(tmp), encoding);
    }

    public VerifyingWriter getWriter() {
        return writer;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setUseBackup(boolean useBackup) {
        this.backup = useBackup;
    }

    public void commit() throws SaveException {
        if (file == null) {
            return;
        }
        if (file.exists() && backup) {
            String name = file.getName();
            String path = file.getParent();
            File backupFile = new File(path, name + GUIGlobals.backupExt);
            try {
                FileUtil.copyFile(file, backupFile, true);
            } catch (IOException ex) {
                ex.printStackTrace();
                throw SaveException.BACKUP_CREATION;
                //throw new SaveException(Globals.lang("Save failed during backup creation")+": "+ex.getMessage());
            }
        }
        try {
            if (useLockFile) {
                try {
                    if (createLockFile()) {
                        // Oops, the lock file already existed. Try to wait it out:
                        if (!FileBasedLock.waitForFileLock(file, 10)) {
                            throw SaveException.FILE_LOCKED;
                        }

                    }
                } catch (IOException ex) {
                    System.err.println("Error when creating lock file");
                    ex.printStackTrace();
                }
            }

            FileUtil.copyFile(tmp, file, true);
        } catch (IOException ex2) {
            // If something happens here, what can we do to correct the problem? The file is corrupted, but we still
            // have a clean copy in tmp. However, we just failed to copy tmp to file, so it's not likely that
            // repeating the action will have a different result.
            // On the other hand, our temporary file should still be clean, and won't be deleted.
            throw new SaveException(Globals.lang("Save failed while committing changes") + ": " + ex2.getMessage());
        } finally {
            if (useLockFile) {
                deleteLockFile();
            }
        }

        tmp.delete();
    }

    public void cancel() {
        tmp.delete();
    }

    /**
     * Check if a lock file exists, and create it if it doesn't.
     * @return true if the lock file already existed
     * @throws IOException if something happens during creation.
     */
    private boolean createLockFile() throws IOException {
        File lock = new File(file.getPath() + SaveSession.LOCKFILE_SUFFIX);
        if (lock.exists()) {
            return true;
        }
        FileOutputStream out = new FileOutputStream(lock);
        out.write(0);
        try {
            out.close();
        } catch (IOException ex) {
            System.err.println("Error when creating lock file");
            ex.printStackTrace();
        }
        lock.deleteOnExit();
        return false;
    }

    /**
     * Check if a lock file exists, and delete it if it does.
     * @return true if the lock file existed, false otherwise.
     * @throws IOException if something goes wrong.
     */
    private boolean deleteLockFile() {
        File lock = new File(file.getPath() + SaveSession.LOCKFILE_SUFFIX);
        if (!lock.exists()) {
            return false;
        }
        lock.delete();
        return true;
    }

    public File getTemporaryFile() {
        return tmp;
    }
}
