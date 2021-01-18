package ru.serge2nd.stream.util;

import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;

import static java.util.stream.Collector.Characteristics.*;
import static ru.serge2nd.stream.util.CharacteristicSet.*;
import static ru.serge2nd.stream.util.CollectingOptions.UNMODIFIABLE;
import static ru.serge2nd.stream.util.Collecting.collect;
import static ru.serge2nd.stream.MapCollectors.toValueFromKeyMap;

/**
 * This abstract class implements {@link Collector#characteristics()}
 * effectively storing collector characteristics as an integer mask.
 * Avoids creating extra characteristic sets by using predefined.
 */
@RequiredArgsConstructor
public abstract class WithCharacteristics<E, A, R> implements Collector<E, A, R> {
    private final int characteristicMask;

    /** {@inheritDoc} */ @Override
    public final CharacteristicSet characteristics() { return characteristicSet(characteristicMask); }

    public static CharacteristicSet characteristicSet(int mask) {
        CharacteristicSet byMask = BY_MASK.get(mask);
        if (byMask == null) return new CharacteristicSet(mask);
        return byMask;
    }

    static final Map<Integer, CharacteristicSet> BY_MASK = collect(
            toValueFromKeyMap(CharacteristicSet::maskOf, CharacteristicSet::new, UNMODIFIABLE),
            new Characteristics[] {},
            new Characteristics[] { IDENTITY_FINISH             },
            new Characteristics[] { UNORDERED                   },
            new Characteristics[] { CONCURRENT                  },
            new Characteristics[] { IDENTITY_FINISH, UNORDERED  },
            new Characteristics[] { IDENTITY_FINISH, CONCURRENT },
            new Characteristics[] { UNORDERED      , CONCURRENT },
            Characteristics.values());

    public static final int M_IDENTITY_FINISH           = mask(IDENTITY_FINISH);
    public static final int M_UNORDERED                 = mask(UNORDERED);
    public static final int M_CONCURRENT                = mask(CONCURRENT);

    public static final int M_IDENTITY_FINISH_UNORDERED = M_IDENTITY_FINISH | M_UNORDERED;

    public static final Set<Characteristics> S_IDENTITY_FINISH           = BY_MASK.get(M_IDENTITY_FINISH);
    public static final Set<Characteristics> S_UNORDERED                 = BY_MASK.get(M_UNORDERED);
    public static final Set<Characteristics> S_IDENTITY_FINISH_UNORDERED = BY_MASK.get(M_IDENTITY_FINISH_UNORDERED);
}