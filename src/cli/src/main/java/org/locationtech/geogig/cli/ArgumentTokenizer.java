/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.locationtech.geogig.cli;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

/**
 * Tokenizes a string with arguments, breaking in blank spaces except when they are quoted
 * 
 */
public class ArgumentTokenizer {

    public static String[] tokenize(String s) {
        Iterable<String> tokens = Splitter.on(new UnquotedSpace()).split(s);
        return Iterables.toArray(tokens, String.class);
    }

    private static class UnquotedSpace extends CharMatcher {

        private boolean inQuotes = false;

        @Override
        public boolean matches(char c) {
            if ('"' == c) {
                inQuotes = !inQuotes;
            }
            if (inQuotes) {
                return false;
            }
            return (' ' == c);
        }

    }

}
