package ru.myx.ae3.util.fn;

import java.io.StringReader;

import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import ru.myx.ae3.base.Base;
import ru.myx.ae3.base.BaseMapEditable;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.vfs.Entry;

/** Scans a flat VFS folder for "*.xsl.tpl" resources and keeps a cached, compiled Templates for
 * each, keyed by the public-facing file name (the ".tpl" suffix stripped, e.g. "show.xsl.tpl" ->
 * "show.xsl") - matching the name that a public "xsl" href actually ends with, since the ACM.TPL
 * serving layer drops the ".tpl" suffix from the public URL.
 *
 * @author myx */
public class SupplierVfsFolderXslTemplatesCached extends SupplierVfsFolderMapCached {

	private static final TransformerFactory transformerFactory = TransformerFactory.newInstance();

	/** @param folder */
	public SupplierVfsFolderXslTemplatesCached(final Entry folder) {

		super(folder);
	}

	@Override
	protected String runDescriptorFilter(final String name) {

		final String key = name.toLowerCase();
		if (key.endsWith(".xsl.tpl")) {
			return key.substring(0, key.length() - ".tpl".length());
		}
		return null;
	}

	@Override
	protected BaseObject runDescriptorMapper(final Entry entry, final String name) {

		try {
			final String xslt = SupplierVfsFolderXslTemplatesCached.stripTplWrapper(entry.getTextContent().baseValue().toString());
			final Templates templates = SupplierVfsFolderXslTemplatesCached.transformerFactory//
					.newTemplates(new StreamSource(new StringReader(xslt)));
			return Base.forUnknown(templates);
		} catch (final TransformerConfigurationException e) {
			throw new RuntimeException("Invalid XSLT: " + entry, e);
		}
	}

	@Override
	protected BaseObject runDescriptorReducer(final BaseMapEditable result, final BaseObject descriptor, final String name) {

		result.putAppend(name, descriptor);
		return result;
	}

	@Override
	protected String runFolderFilter(final Entry entry) {

		/** flat scan only, no recursion into subdirectories */
		return null;
	}

	@Override
	protected BaseObject runFolderMapper(final String name, final Entry entry) {

		return null;
	}

	/** ".tpl" resources are wrapped in ACM.TPL directive tags (e.g. <%FINAL: '...' %><%FORMAT:
	 * '...' %> ... <%/FORMAT%><%/FINAL%>) that control how the AE3 template engine serves the
	 * file over HTTP. When the wrapped content has no other template directives (i.e. it's fully
	 * static), stripping any number of leading "<%...%>" and trailing "<%/...%>" tags is enough
	 * to recover the raw stylesheet, without running it through the template engine.
	 *
	 * @param source
	 * @return stylesheet text
	 * @throws IllegalStateException
	 *             when a leading or trailing '<%' tag is not properly closed */
	private static String stripTplWrapper(final String source) {

		final StringBuilder builder = new StringBuilder(source.trim());

		while (builder.length() > 1 && builder.charAt(0) == '<' && builder.charAt(1) == '%') {
			final int close = builder.indexOf("%>");
			if (close < 0) {
				throw new IllegalStateException("Unterminated leading '<%' tag");
			}
			builder.delete(0, close + 2);
		}

		while (builder.length() > 1 && builder.charAt(builder.length() - 1) == '>' && builder.charAt(builder.length() - 2) == '%') {
			final int open = builder.lastIndexOf("<%");
			if (open < 0) {
				throw new IllegalStateException("Unterminated trailing '%>' tag");
			}
			builder.delete(open, builder.length());
		}

		return builder.toString();
	}
}
