package net.sf.jabref.logic.util;

import net.sf.jabref.Globals;

public class StringUtil {

    /**
     * Returns the string, after shaving off whitespace at the beginning and end,
     * and removing (at most) one pair of braces or " surrounding it.
     *
     * @param toShave
     * @return
     */
    public static String shaveString(String toShave) {

        if (toShave == null) {
            return null;
        }
        char first;
        char second;
        int begin = 0;
        int end = toShave.length();
        // We start out assuming nothing will be removed.
        boolean beginOk = false;
        boolean endOk = false;
        while (!beginOk) {
            if (begin < toShave.length()) {
                first = toShave.charAt(begin);
                if (Character.isWhitespace(first)) {
                    begin++;
                } else {
                    beginOk = true;
                }
            } else {
                beginOk = true;
            }

        }
        while (!endOk) {
            if (end > begin + 1) {
                first = toShave.charAt(end - 1);
                if (Character.isWhitespace(first)) {
                    end--;
                } else {
                    endOk = true;
                }
            } else {
                endOk = true;
            }
        }

        if (end > begin + 1) {
            first = toShave.charAt(begin);
            second = toShave.charAt(end - 1);
            if (first == '{' && second == '}' || first == '"' && second == '"') {
                begin++;
                end--;
            }
        }
        toShave = toShave.substring(begin, end);
        return toShave;
    }

    private static String rightTrim(String toTrim) {
        return toTrim.replaceAll("\\s+$", "");
    }

    /**
     * Concatenate all strings in the array from index 'from' to 'to' (excluding
     * to) with the given separator.
     * <p>
     * Example:
     * <p>
     * String[] s = "ab/cd/ed".split("/"); join(s, "\\", 0, s.length) ->
     * "ab\\cd\\ed"
     *
     * @param strings
     * @param separator
     * @param from
     * @param to        Excluding strings[to]
     * @return
     */
    public static String join(String[] strings, String separator, int from, int to) {
        if (strings.length == 0 || from >= to) {
            return "";
        }

        from = Math.max(from, 0);
        to = Math.min(strings.length, to);

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = from; i < to - 1; i++) {
            stringBuilder.append(strings[i]).append(separator);
        }
        return stringBuilder.append(strings[to - 1]).toString();
    }

    public static String join(String[] strings, String separator) {
        return join(strings, separator, 0, strings.length);
    }

    /**
     * Returns the given string but with the first character turned into an
     * upper case character.
     * <p>
     * Example: testTest becomes TestTest
     *
     * @param string The string to change the first character to upper case to.
     * @return A string has the first character turned to upper case and the
     * rest unchanged from the given one.
     */
    public static String toUpperFirstLetter(String string) {
        if (string == null) {
            throw new IllegalArgumentException();
        }

        if (string.isEmpty()) {
            return string;
        }

        return Character.toUpperCase(string.charAt(0)) + string.substring(1);
    }

    /**
     * Takes a delimited string, splits it and returns
     *
     * @param names a <code>String</code> value
     * @return a <code>String[]</code> value
     */
    public static String[] split(String names, String delimiter) {
        if (names == null) {
            return null;
        }
        return names.split(delimiter);
    }

    public static String capitalizeFirst(String toCapitalize) {
        // Make first character of String uppercase, and the
        // rest lowercase.
        if (toCapitalize.length() > 1) {
            return toCapitalize.substring(0, 1).toUpperCase() + toCapitalize.substring(1, toCapitalize.length()).toLowerCase();
        } else {
            return toCapitalize.toUpperCase();
        }

    }

    /**
     * Removes optional square brackets from the string s
     *
     * @param toStrip
     * @return
     */
    public static String stripBrackets(String toStrip) {
        int beginIndex = toStrip.startsWith("[") ? 1 : 0;
        int endIndex = toStrip.endsWith("]") ? toStrip.length() - 1 : toStrip.length();
        return toStrip.substring(beginIndex, endIndex);
    }

    /**
     * extends the filename with a default Extension, if no Extension '.x' could
     * be found
     */
    public static String getCorrectFileName(String orgName, String defaultExtension) {
        if (orgName == null) {
            return "";
        }

        String back = orgName;
        int hiddenChar = orgName.indexOf(".", 1); // hidden files Linux/Unix (?)
        if (hiddenChar < 1) {
            back = back + "." + defaultExtension;
        }

        return back;
    }

    /**
     * Creates a substring from a text
     *
     * @param text
     * @param index
     * @param terminateOnEndBraceOnly
     * @return
     */
    public static String getPart(String text, int index, boolean terminateOnEndBraceOnly) {
        char c;
        int count = 0;

        StringBuilder part = new StringBuilder();

        // advance to first char and skip whitespace
        index++;
        while (index < text.length() && Character.isWhitespace(text.charAt(index))) {
            index++;
        }

        // then grab whathever is the first token (counting braces)
        while (index < text.length()) {
            c = text.charAt(index);
            if (!terminateOnEndBraceOnly && count == 0 && Character.isWhitespace(c)) {
                // end argument and leave whitespace for further processing
                break;
            }
            if (c == '}' && --count < 0) {
                break;
            } else if (c == '{') {
                count++;
            }
            part.append(c);
            index++;
        }
        return part.toString();
    }

    /**
     * Formats field contents for output. Must be "symmetric" with the parse method above,
     * so stored and reloaded fields are not mangled.
     *
     * @param in
     * @param wrapAmount
     * @return the wrapped String.
     */
    public static String wrap(String in, int wrapAmount) {

        String[] lines = in.split("\n");
        StringBuilder result = new StringBuilder();
        addWrappedLine(result, lines[0], wrapAmount);
        for (int i = 1; i < lines.length; i++) {

            if (!lines[i].trim().equals("")) {
                result.append(Globals.NEWLINE);
                result.append('\t');
                result.append(Globals.NEWLINE);
                result.append('\t');
                String line = lines[i];
                // remove all whitespace at the end of the string, this especially includes \r created when the field content has \r\n as line separator
                line = rightTrim(line);
                addWrappedLine(result, line, wrapAmount);
            } else {
                result.append(Globals.NEWLINE);
                result.append('\t');
            }
        }
        return result.toString();
    }

    private static void addWrappedLine(StringBuilder result, String line, int wrapAmount) {
        // Set our pointer to the beginning of the new line in the StringBuffer:
        int length = result.length();
        // Add the line, unmodified:
        result.append(line);

        while (length < result.length()) {
            int current = result.indexOf(" ", length + wrapAmount);
            if (current < 0 || current >= result.length()) {
                break;
            }

            result.deleteCharAt(current);
            result.insert(current, Globals.NEWLINE + "\t");
            length = current + Globals.NEWLINE_LENGTH;

        }
    }

    /**
     * Quotes each and every character, e.g. '!' as &#33;. Used for verbatim
     * display of arbitrary strings that may contain HTML entities.
     */
    public static String quoteForHTML(String toQuote) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < toQuote.length(); ++i) {
            result.append("&#").append((int) toQuote.charAt(i)).append(";");
        }
        return result.toString();
    }

    public static String quote(String toQuote, String specials, char quoteChar) {
        return quote(toQuote, specials, quoteChar, 0);
    }

    /**
     * Quote special characters.
     *
     * @param toQuote         The String which may contain special characters.
     * @param specials  A String containing all special characters except the quoting
     *                  character itself, which is automatically quoted.
     * @param quoteChar The quoting character.
     * @param linewrap  The number of characters after which a linebreak is inserted
     *                  (this linebreak is undone by unquote()). Set to 0 to disable.
     * @return A String with every special character (including the quoting
     * character itself) quoted.
     */
    private static String quote(String toQuote, String specials, char quoteChar, int linewrap) {
        StringBuilder result = new StringBuilder();
        char c;
        int lineLength = 0;
        boolean isSpecial;
        for (int i = 0; i < toQuote.length(); ++i) {
            c = toQuote.charAt(i);
            isSpecial = specials.indexOf(c) >= 0 || c == quoteChar;
            // linebreak?
            if (linewrap > 0
                    && (++lineLength >= linewrap || isSpecial && lineLength >= linewrap - 1)) {
                result.append(quoteChar);
                result.append('\n');
                lineLength = 0;
            }
            if (isSpecial) {
                result.append(quoteChar);
                ++lineLength;
            }
            result.append(c);
        }
        return result.toString();
    }

    /**
     * Unquote special characters.
     *
     * @param toUnquote         The String which may contain quoted special characters.
     * @param quoteChar The quoting character.
     * @return A String with all quoted characters unquoted.
     */
    public static String unquote(String toUnquote, char quoteChar) {
        StringBuilder result = new StringBuilder();
        char c;
        boolean quoted = false;
        for (int i = 0; i < toUnquote.length(); ++i) {
            c = toUnquote.charAt(i);
            if (quoted) { // append literally...
                if (c != '\n') {
                    result.append(c);
                }
                quoted = false;
            } else if (c != quoteChar) {
                result.append(c);
            } else { // quote char
                quoted = true;
            }
        }
        return result.toString();
    }

    /**
     * Append '.bib' to the string unless it ends with that.
     * <p>
     * makeBibtexExtension("asfd") => "asdf.bib"
     * makeBibtexExtension("asdf.bib") => "asdf.bib"
     *
     * @param name the string
     * @return s or s + ".bib"
     */
    public static String makeBibtexExtension(String name) {
        if (!name.toLowerCase().endsWith(".bib")) {
            return name + ".bib";
        }
        return name;
    }

    public static String booleanToBinaryString(boolean expression) {
        return expression ? "1" : "0";
    }
}
