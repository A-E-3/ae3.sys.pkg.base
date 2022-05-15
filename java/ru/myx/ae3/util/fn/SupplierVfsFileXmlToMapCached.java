package ru.myx.ae3.util.fn;

import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.vfs.Entry;
import ru.myx.ae3.xml.Xml;

/** @author myx */
public class SupplierVfsFileXmlToMapCached extends SupplierVfsFileAbstractTextParseCached {

	/**
	 *
	 */
	protected BaseObject settingsDefaults = null;
	/** @param file */
	public SupplierVfsFileXmlToMapCached(final Entry file) {
		super(file);
	}

	@Override
	protected BaseObject parseText(final CharSequence source) {

		if (source == null || source.length() == 0) {
			return this.settingsDefaults == null
				? BaseObject.UNDEFINED
				: BaseObject.createObject(this.settingsDefaults);
		}

		try {
			final BaseObject parsed = Xml.toBase(//
					"XML-2-MAP/CACHED:" + this.file, //
					source, //
					null, //
					null, //
					null//
			);

			if (this.settingsDefaults == null) {
				return parsed;
			}

			final BaseObject result = BaseObject.createObject(this.settingsDefaults);
			result.baseDefineImportOwnEnumerable(parsed);
			return result;

		} catch (final Exception e) {
			throw new RuntimeException("Invalid XML: " + this.file, e);
		}
	}

	/** @param defaults
	 * @return */
	public SupplierTextInputAbstractTextCached setDefaults(final BaseObject defaults) {

		this.settingsDefaults = defaults;
		return this;
	}
}
