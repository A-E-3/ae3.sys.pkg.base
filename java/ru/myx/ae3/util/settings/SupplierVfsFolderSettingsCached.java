package ru.myx.ae3.util.settings;

import ru.myx.ae3.base.Base;
import ru.myx.ae3.base.BaseFunction;
import ru.myx.ae3.base.BaseMapEditable;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.exec.Exec;
import ru.myx.ae3.util.fn.SupplierVfsFolderMapCached;
import ru.myx.ae3.vfs.Entry;
import ru.myx.ae3.xml.Xml;
import ru.myx.sapi.JsonSAPI;

/** @author myx */
public class SupplierVfsFolderSettingsCached extends SupplierVfsFolderMapCached {

	/**
	 *
	 */
	protected BaseFunction descriptorFilter = null;

	/**
	 *
	 */
	protected BaseFunction descriptorMapper = null;

	/**
	 *
	 */
	protected BaseFunction descriptorReducer = null;

	/**
	 *
	 */
	protected BaseObject inputDefaults = null;

	SupplierVfsFolderSettingsCached(final Entry folder) {

		super(folder);
	}

	/** @param inputDefaults
	 * @return */
	public SupplierVfsFolderSettingsCached setDefaults(final BaseObject inputDefaults) {

		this.inputDefaults = inputDefaults;
		return this;
	}

	/** Sets result object reducer
	 *
	 * @param function
	 * @return */
	public SupplierVfsFolderSettingsCached setDescriptorFilter(final BaseFunction function) {

		this.descriptorFilter = function;
		return this;
	}

	/** Sets result object reducer
	 *
	 * @param function
	 * @return */
	public SupplierVfsFolderSettingsCached setDescriptorMapper(final BaseFunction function) {

		this.descriptorMapper = function;
		return this;
	}

	/** Sets result object reducer
	 *
	 * @param function
	 * @return */
	public SupplierVfsFolderSettingsCached setDescriptorReducer(final BaseFunction function) {

		this.descriptorReducer = function;
		return this;
	}

	@Override
	protected String runDescriptorFilter(final String name) {

		if (this.descriptorFilter == null) {
			final String key = name.toLowerCase();
			if (key.endsWith(".json")) {
				return key.substring(0, key.length() - 5);
			}
			if (key.endsWith(".xml")) {
				return key.substring(0, key.length() - 4);
			}
			return null;
		}

		final BaseObject filterResult = this.descriptorFilter.callNJ1(null, Base.forString(name));
		if (filterResult == null || filterResult == BaseObject.NULL || filterResult == BaseObject.UNDEFINED || filterResult == BaseObject.FALSE) {
			return null;
		}

		if (filterResult instanceof CharSequence) {
			return filterResult.baseToJavaString();
		}

		return name;

	}

	@Override
	protected BaseObject runDescriptorMapper(final Entry entry, final String name) {

		if (this.descriptorMapper == null) {
			final BaseObject descriptor;
			describe : {
				final BaseObject parsed;
				parse : {
					final String key = entry.getKey().toLowerCase();
					if (key.endsWith(".json")) {
						try {
							parsed = JsonSAPI.parse(Exec.currentProcess(), entry);
							break parse;
						} catch (final Exception e) {
							throw new RuntimeException("Invalid JSON: " + entry, e);
						}
					}
					if (key.endsWith(".xml")) {
						try {
							parsed = Xml.toBase(//
									"XML-2-MAP/CACHED:" + entry, //
									entry.toCharacter().getText(), //
									null, //
									null, //
									null//
							);
							break parse;
						} catch (final Exception e) {
							throw new RuntimeException("Invalid XML: " + entry, e);
						}
					}
					return null;
				}
				if (this.inputDefaults == null) {
					descriptor = parsed;
					break describe;
				}

				descriptor = BaseObject.createObject(this.inputDefaults);
				descriptor.baseDefineImportOwnEnumerable(parsed);
				break describe;
			}

			if (descriptor.baseGet("name", null) == null) {
				descriptor.baseDefine("name", name);
			}

			return descriptor;
		}

		final BaseObject mapperResult = this.descriptorMapper.callNJ2(null, Base.forUnknown(entry), Base.forString(name));
		if (mapperResult == null || mapperResult == BaseObject.NULL || mapperResult == BaseObject.UNDEFINED) {
			return null;
		}

		return mapperResult;
	}

	@Override
	protected BaseObject runDescriptorReducer(final BaseMapEditable result, final BaseObject descriptor, final String name) {

		if (this.descriptorReducer == null) {
			result.putAppend(name, descriptor);
			return result;
		}

		final BaseObject reduceResult = this.descriptorReducer.callNJA(result, result, descriptor, Base.forString(name));
		if (reduceResult != result) {
			throw new IllegalStateException("Map is replaced by reducer callback, fn: " + this.descriptorReducer);
		}

		return result;
	}

	@Override
	protected String runFolderFilter(final Entry entry) {

		return null;
	}

	@Override
	protected BaseObject runFolderMapper(final String name, final Entry entry) {

		return null;
	}
}
