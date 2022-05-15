package ru.myx.ae3.report;

import java.text.FieldPosition;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ru.myx.ae3.Engine;
import ru.myx.ae3.base.Base;
import ru.myx.ae3.base.BaseDate;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.base.BaseObjectNoOwnProperties;
import ru.myx.ae3.common.Value;
import ru.myx.ae3.common.WaitTimeoutException;
import ru.myx.ae3.reflect.Reflect;
import ru.myx.ae3.reflect.ReflectionExplicit;
import ru.myx.ae3.reflect.ReflectionManual;
import ru.myx.ae3.vfs.Entry;

/** @author myx */
@ReflectionManual
public class EventIdentifier implements BaseObjectNoOwnProperties, Value<BaseDate> {

	private static final class DontCareFieldPosition extends FieldPosition {

		// The singleton of DontCareFieldPosition.
		static final FieldPosition INSTANCE = new DontCareFieldPosition();

		private DontCareFieldPosition() {

			super(0);
		}
	}

	private static final char[] DIGITS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

	/** FIXME - should somehow (through explicit reflection annotation) use prototype from parent
	 * class */
	static final BaseObject PROTOTYPE = Reflect.classToBasePrototype(EventIdentifier.class);

	/**
	 *
	 */
	@ReflectionExplicit
	public static final String KEY_HIGHEST //
			= "zzzzzzzzzzzzz-zz";

	/**
	 *
	 */
	@ReflectionExplicit
	public static final String KEY_HIGHEST_2015 //
			= "99999999T999999999Z-ZZZZZZzzz";

	/**
	 *
	 */
	@ReflectionExplicit
	public static final String KEY_LOWEST //
			= "00000000z0000-00";

	/**
	 *
	 */
	@ReflectionExplicit
	public static final String KEY_LOWEST_2015 //
			= "00000000T000000000Z-000000---";

	/**
	 *
	 */
	public static final int MAX_SEQUENCE //
			= 46000;

	/**
	 *
	 */
	public static final short MAX_ORIGIN //
			= 1295;

	private static long lastDateMillis = -1;

	private static short lastSequence = -1;

	/** @param a
	 * @param b
	 * @return */
	@ReflectionExplicit
	public static final int compareEntryKeysDateAsc(final Entry a, final Entry b) {

		return EventIdentifier.compareKeysDateAsc(a.getKey(), b.getKey());
	}

	/** @param a
	 * @param b
	 * @return */
	@ReflectionExplicit
	public static final int compareEntryKeysDateDesc(final Entry a, final Entry b) {

		return EventIdentifier.compareKeysDateDesc(a.getKey(), b.getKey());
	}

	/** @param a
	 * @param b
	 * @return */
	@ReflectionExplicit
	public static final int compareKeysDateAsc(final String a, final String b) {
		
		final int la = a.length();
		final int lb = b.length();
		if (la == lb) {
			return a.compareTo(b);
		}
		final long ta = EventIdentifier.parseKeyStringMillis(a, la);
		final long tb = EventIdentifier.parseKeyStringMillis(b, lb);
		return ta < tb
			? -1
			: ta > tb
				? 1
				: 0;
	}

	/** @param a
	 * @param b
	 * @return */
	@ReflectionExplicit
	public static final int compareKeysDateDesc(final String a, final String b) {
		
		final int la = a.length();
		final int lb = b.length();
		if (la == lb) {
			return b.compareTo(a);
		}
		final long ta = EventIdentifier.parseKeyStringMillis(a, la);
		final long tb = EventIdentifier.parseKeyStringMillis(b, lb);
		return ta > tb
			? -1
			: ta < tb
				? 1
				: 0;
	}

	/** @param dateMillis
	 * @param sequence
	 * @param origin
	 * @return */
	@ReflectionExplicit
	public static final EventIdentifier exact(final long dateMillis, final short sequence, final short origin) {

		return new EventIdentifier(dateMillis, sequence, origin);
	}

	/** @param date
	 * @return */
	@ReflectionExplicit
	public static final EventIdentifier keyHighest(final Date date) {

		return new EventIdentifier(date.getTime(), EventIdentifier.MAX_SEQUENCE, EventIdentifier.MAX_ORIGIN);
	}
	/** @param date
	 * @return */
	@ReflectionExplicit
	public static final EventIdentifier keyLowest(final Date date) {

		return new EventIdentifier(date.getTime(), 0, (short) -1);
	}

	/** @param date
	 * @param backwards
	 * @return */
	@ReflectionExplicit
	public static final EventIdentifier keyStart(final Date date, final boolean backwards) {

		return backwards
			? EventIdentifier.keyHighest(date)
			: EventIdentifier.keyLowest(date);
	}

	/** Exactly at the moment of posting to message bus!
	 *
	 * @return */
	@ReflectionExplicit
	public static final EventIdentifier next() {

		return EventIdentifier.next((short) -1);
	}

	/** Exactly at the moment of posting to message bus!
	 *
	 * @param origin
	 * @return */
	@ReflectionExplicit
	public static final EventIdentifier next(final short origin) {

		final long dateMillis = System.currentTimeMillis();
		final short sequence;
		synchronized (EventIdentifier.class) {
			if (dateMillis <= EventIdentifier.lastDateMillis) {
				sequence = ++EventIdentifier.lastSequence;
			} else {
				EventIdentifier.lastDateMillis = dateMillis;
				EventIdentifier.lastSequence = sequence = 0;
			}
		}
		return new EventIdentifier(dateMillis, sequence, origin);
	}

	/** @param k
	 * @return */
	@ReflectionExplicit
	public static final EventIdentifier parseKeyString(final String k) {

		final int length = k.length();

		try {
			// "20140729T110014954Z-000000---".length === 29
			if ((length == 29 || length > 29 && k.charAt(29) == ';') && k.charAt(19) == '-') {
				return new EventIdentifier(//
						Base.toDateMillis(k.substring(0, 19)),
						Short.parseShort(k.substring(20, 26)),
						(short) 0);
			}
			// "2014-07-29T11:00:14.954Z;000000---".length === 34
			if (length == 34 && k.charAt(24) == ';') {
				return new EventIdentifier(//
						Base.toDateMillis(k.substring(0, 24)),
						Short.parseShort(k.substring(25, 31)),
						(short) 0);
			}
			// "2014-07-29T11:00:14.954Z;0000000".length == 32
			if (length == 32 && k.charAt(24) == ';') {
				return new EventIdentifier(//
						Base.toDateMillis(k.substring(0, 24)),
						Short.parseShort(k.substring(25, 32)),
						(short) 0);
			}

			return null;
		} catch (final ParseException e) {
			return null;
		}
	}

	/** @param k
	 * @return */
	@ReflectionExplicit
	public static final long parseKeyStringMillis(final String k) {

		return EventIdentifier.parseKeyStringMillis(k, k.length());
	}

	private static final long parseKeyStringMillis(final String k, final int length) {

		try {
			// "20140729T110014954Z-000000---".length === 29
			if ((length == 29 || length > 29 && k.charAt(29) == ';') && k.charAt(19) == '-') {
				return Base.toDateMillis(k.substring(0, 19));
			}
			// "2014-07-29T11:00:14.954Z;000000---".length === 34
			if (length == 34 && k.charAt(24) == ';') {
				return Base.toDateMillis(k.substring(0, 24));
			}
			// "2014-07-29T11:00:14.954Z;0000000".length == 32
			if (length == 32 && k.charAt(24) == ';') {
				return Base.toDateMillis(k.substring(0, 24));
			}

			return 0;
		} catch (final ParseException e) {
			return 0;
		}
	}

	/**
	 *
	 */
	@ReflectionExplicit
	public final BaseDate date;
	
	/**
	 *
	 */
	@ReflectionExplicit
	public final short origin;

	/**
	 *
	 */
	@ReflectionExplicit
	private final int sequence;

	private EventIdentifier(final long dateMillis, final int sequence, final short origin) {

		if (sequence < 0) {
			throw new IllegalArgumentException("sequence must be >= 0");
		}
		/** 3 digits in base36 */
		if (sequence > 46000) {
			throw new IllegalArgumentException("sequence must be <= 46000");
		}
		if (origin < -1) {
			throw new IllegalArgumentException("origin must be -1 or >= 0");
		}
		/** 2 digits in base36 */
		if (origin > 1295) {
			throw new IllegalArgumentException("origin must be <= 1295");
		}
		this.date = new BaseDate(dateMillis);
		this.sequence = sequence;
		this.origin = origin;
	}

	@Override
	public String baseClass() {

		return "EventIdentity";
	}

	@Override
	public BaseObject basePrototype() {

		return EventIdentifier.PROTOTYPE;
	}

	@Override
	public BaseDate baseValue() throws WaitTimeoutException {

		return this.date;
	}

	/** @return */
	@ReflectionExplicit
	public String getKeyString() {

		final StringBuilder builder = new StringBuilder();

		{
			final long dateMillis = this.date.getTime();
			final int length = dateMillis == 0
				? 1
				: (int) Math.floor(Math.log(dateMillis) / Math.log(36)) + 1;
			for (int i = 9 - length; i > 0; --i) {
				builder.append('0');
			}
			// builder.append(Long.toString(dateMillis, 36));
			for (int i = length - 1; i >= 0; --i) {
				builder.append(EventIdentifier.DIGITS[(int) (dateMillis / Math.pow(36, i) % 36)]);
			}
		}

		builder.append('z');

		{
			final int sequence = this.sequence;
			if (sequence == 0) {
				builder.append('0');
				builder.append('0');
				builder.append('0');
			} else {
				final int length = sequence == 0
					? 1
					: (int) Math.floor(Math.log(sequence) / Math.log(36)) + 1;
				for (int i = 3 - length; i > 0; --i) {
					builder.append('0');
				}
				// builder.append(Long.toString(sequence, 36));
				for (int i = length - 1; i >= 0; --i) {
					builder.append(EventIdentifier.DIGITS[(int) (sequence / Math.pow(36, i) % 36)]);
				}
			}
		}

		builder.append('-');

		{
			final short origin = this.origin;
			if (origin == -1) {
				builder.append('-');
				builder.append('-');
			} else {
				final int length = origin == 0
					? 1
					: (int) Math.floor(Math.log(origin) / Math.log(36)) + 1;
				for (int i = 2 - length; i > 0; --i) {
					builder.append('0');
				}
				// builder.append(Long.toString(origin, 36));
				for (int i = length - 1; i >= 0; --i) {
					builder.append(EventIdentifier.DIGITS[(int) (origin / Math.pow(36, i) % 36)]);
				}
			}
		}

		return builder.toString();
	}

	/** @return */
	@ReflectionExplicit
	public String getKeyString2015() {

		final SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd'T'HHmmssSSS'Z'");
		formatter.setTimeZone(Engine.TIMEZONE_GMT);

		final StringBuffer builder = new StringBuffer();

		{
			formatter.format(this.date, builder, DontCareFieldPosition.INSTANCE);

			builder.append('-');
		}

		{
			final int sequence = this.sequence;
			final int length = sequence == 0
				? 1
				: (int) Math.floor(Math.log10(sequence)) + 1;
			for (int i = 6 - length; i > 0; --i) {
				builder.append('0');
			}
			for (int i = length - 1; i >= 0; --i) {
				builder.append(EventIdentifier.DIGITS[(int) (sequence / Math.pow(10, i) % 10)]);
			}
		}

		{
			final short origin = this.origin;

			if (origin == -1) {
				builder.append('-');
				builder.append('-');
				builder.append('-');
			} else {
				final int length = origin == 0
					? 1
					: (int) Math.floor(Math.log(origin) / Math.log(16)) + 1;
				for (int i = 3 - length; i > 0; --i) {
					builder.append('0');
				}
				// builder.append(Integer.toHexString(origin));
				for (int i = length - 1; i >= 0; --i) {
					builder.append(EventIdentifier.DIGITS[(int) (origin / Math.pow(16, i) % 16)]);
				}
			}
		}

		return builder.toString();
	}

	/** @return */
	@ReflectionExplicit
	public final long getTime() {

		return this.date.getTime();
	}

	/** @return */
	@ReflectionExplicit
	public final EventIdentifier nextSequentialIdentifier() {

		if (this.sequence < EventIdentifier.MAX_SEQUENCE) {
			return new EventIdentifier(this.date.getTime(), this.sequence + 1, this.origin);
		}
		return new EventIdentifier(this.date.getTime() + 1L, 0, (short) -1);
	}

	@Override
	public String toString() {

		return "[event " + this.getKeyString2015() + "]";
	}
}
