package ru.myx.ae3.util.fn;

import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.exec.Exec;
import ru.myx.ae3.vfs.Entry;
import ru.myx.sapi.JsonSAPI;

/** @author myx */
public class SupplierVfsFileJsonToMapCached extends SupplierVfsFileAbstractTextParseCached {

	/**
	 *
	 */
	protected BaseObject inputDefaults = null;
	/** @param file */
	public SupplierVfsFileJsonToMapCached(final Entry file) {

		super(file);
	}

	/** @param inputDefaults
	 * @return */
	public SupplierTextInputAbstractTextCached setDefaults(final BaseObject inputDefaults) {

		this.inputDefaults = inputDefaults;
		return this;
	}

	@Override
	protected BaseObject parseText(final CharSequence source) {

		if (source == null || source.length() == 0) {
			return this.inputDefaults == null
				? BaseObject.UNDEFINED
				: BaseObject.createObject(this.inputDefaults);
		}

		try {
			final BaseObject parsed = JsonSAPI.parse(Exec.currentProcess(), String.valueOf(source));

			if (this.inputDefaults == null) {
				return parsed;
			}

			final BaseObject result = BaseObject.createObject(this.inputDefaults);
			result.baseDefineImportOwnEnumerable(parsed);
			return result;

		} catch (final Exception e) {
			throw new RuntimeException("Invalid JSON: " + this.file, e);
		}
	}
}
