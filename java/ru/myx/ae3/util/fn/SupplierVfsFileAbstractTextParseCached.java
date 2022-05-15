package ru.myx.ae3.util.fn;

import ru.myx.ae3.vfs.Entry;

/** @author myx */
public abstract class SupplierVfsFileAbstractTextParseCached extends SupplierTextInputAbstractTextCached {

	/**
	 *
	 */
	protected final Entry file;

	SupplierVfsFileAbstractTextParseCached(final Entry file) {
		this.file = file;
	}

	@Override
	protected CharSequence loadSource() {

		return this.file.getTextContent().baseValue();
	}
}
