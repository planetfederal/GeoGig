/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */

package org.locationtech.geogig.cli.test;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;
import org.locationtech.geogig.cli.ArgumentTokenizer;

public class ArgumentTokenizerTest {

    @Test
    public void testTokenizer() {

        String s = "commit -m \"a message with blank spaces\"";
        String[] tokens = ArgumentTokenizer.tokenize(s);
        assertArrayEquals(new String[] { "commit", "-m", "\"a message with blank spaces\"" },
                tokens);
        s = "commit -m \"a message with line\nbreaks\"";
        tokens = ArgumentTokenizer.tokenize(s);
        assertArrayEquals(new String[] { "commit", "-m", "\"a message with line\nbreaks\"" },
                tokens);
        s = "reset HEAD~1 --hard";
        tokens = ArgumentTokenizer.tokenize(s);
        assertArrayEquals(new String[] { "reset", "HEAD~1", "--hard" }, tokens);
    }

}
