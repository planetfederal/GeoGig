/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.cli;

import org.fusesource.jansi.Ansi;

/**
 *
 */
public class AnsiDecorator extends Ansi {

    private AnsiDecorator(StringBuilder sb) {
        super(sb);
    }

    public static Ansi newAnsi(boolean ansiSupported) {
        StringBuilder sb = new StringBuilder();
        return newAnsi(ansiSupported, sb);
    }

    public static Ansi newAnsi(boolean ansiSupported, StringBuilder sb) {
        Ansi ansi = new Ansi(sb);
        ansiSupported &= null != System.console();
        if (ansiSupported) {
            return ansi;
        }

        AnsiDecorator ansiDecorator = new AnsiDecorator(sb);
        return ansiDecorator;
    }

    @Override
    public Ansi a(Attribute ignored) {
        return this;
    }

    @Override
    public Ansi bg(Color c) {
        return this;
    }

    @Override
    public Ansi bold() {
        return this;
    }

    @Override
    public Ansi boldOff() {
        return this;
    }

    @Override
    public Ansi fg(Color c) {
        return this;
    }

}
