package net.sf.jabref.imports;

import net.sf.jabref.OutputPrinter;
import net.sf.jabref.gui.FetcherPreviewDialog;

import java.util.Map;

/**
 *
 */
interface PreviewEntryFetcher extends EntryFetcher {

    boolean processQueryGetPreview(String query, FetcherPreviewDialog preview,
                                   OutputPrinter status);

    void getEntries(Map<String, Boolean> selection, ImportInspector inspector);

    /**
     * The number of entries a user can select for download without getting a warning message.
     * @return the warning limit
     */
    int getWarningLimit();

    /**
     * The preferred table row height for the previews.
     * @return the preferred height
     */
    int getPreferredPreviewHeight();

}
