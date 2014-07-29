/* Copyright (c) 2013 OpenPlans. All rights reserved.
 * This code is licensed under the BSD New License, available at the root
 * application directory.
 */
package org.locationtech.geogig.cli.porcelain;

import com.beust.jcommander.IStringConverter;

/**
 * This enumeration defines different the various usage options for color in some commands.
 */
public enum ColorArg {
    auto, never, always;

    /**
     * This converter is used to convert a String input into a valid enumeration value.
     */
    public static class Converter implements IStringConverter<ColorArg> {

        /**
         * @param value the string to convert
         * @return the resulting ColorArg enumeration
         */
        @Override
        public ColorArg convert(String value) {
            return ColorArg.valueOf(value);
        }

    }
}
