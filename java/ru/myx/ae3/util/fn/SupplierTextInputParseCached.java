package ru.myx.ae3.util.fn;

import java.util.function.Function;
import java.util.function.Supplier;

import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.exec.Exec;
import ru.myx.ae3.vfs.Entry;
import ru.myx.sapi.JsonSAPI;

/** @author myx */
public class SupplierTextInputParseCached extends SupplierTextInputAbstractTextCached {

	private Supplier<CharSequence> sourceSupplier;

	private Function<CharSequence, BaseObject> textParser;

	/** @return */
	@Override
	protected CharSequence loadSource() {

		return this.sourceSupplier.get();
	}

	/** @param source
	 *            (NULL when empty/default)
	 * @return */
	@Override
	protected BaseObject parseText(final CharSequence source) {

		return this.textParser.apply(source);
	}

	/** @param file
	 * @return */
	public SupplierTextInputParseCached setInputVfsFile(final Entry file) {

		final String key = file.getKey().toLowerCase();
		if (key.endsWith(".json")) {
			return this.setInputVfsFileJson(file);
		}
		throw new IllegalArgumentException("Unknown or unsupported file type, name: " + file.getKey());
	}

	/** @param file
	 * @return */
	public SupplierTextInputParseCached setInputVfsFileJson(final Entry file) {

		this.sourceSupplier = new Supplier<>() {

			@Override
			public CharSequence get() {

				return file.getTextContent().baseValue();
			}

			@Override
			public String toString() {

				return "[object TextInputParseCachedSupplier]";
			}
		};
		this.textParser = new Function<>() {

			@Override
			public BaseObject apply(final CharSequence source) {

				try {
					return JsonSAPI.parse(Exec.currentProcess(), String.valueOf(source));
				} catch (final Exception e) {
					throw new RuntimeException("Invalid JSON: " + file, e);
				}
			}

			@Override
			public String toString() {

				return "[object TextInputParseCachedParser]";
			}
		};
		return this;
	}

}
