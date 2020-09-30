/**
 * Narrow extensions of the {@link java.util.stream Java Stream API}.
 * Helps to avoid creating more objects (mainly stream operators) in some frequent cases,
 * e.g. collecting in-one-pass transformed elements ({@link ru.serge2nd.stream.MappingCollectors}).<br>
 * The reduced memory amount by using the provided lightweight collectors may be quite significant
 * <b>compared</b> with size of <b>small</b> collections (hundreds of bytes or less).
 */
package ru.serge2nd.stream;