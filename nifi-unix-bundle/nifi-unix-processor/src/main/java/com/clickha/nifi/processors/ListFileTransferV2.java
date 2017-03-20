package com.clickha.nifi.processors;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.attributes.CoreAttributes;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.util.StandardValidators;

import com.clickha.nifi.processors.ListFile;
import com.clickha.nifi.processors.AbstractListProcessor;
import com.clickha.nifi.processors.util.FileInfo;
import com.clickha.nifi.processors.util.FileTransferV2;

public abstract class ListFileTransferV2 extends AbstractListProcessor<FileInfo> {
	public static final PropertyDescriptor HOSTNAME = new PropertyDescriptor.Builder().name("Hostname")
			.description("The fully qualified hostname or IP address of the remote system")
			.addValidator(StandardValidators.NON_EMPTY_VALIDATOR).required(true).expressionLanguageSupported(true)
			.build();
	static final PropertyDescriptor UNDEFAULTED_PORT = new PropertyDescriptor.Builder().name("Port")
			.description("The port to connect to on the remote host to fetch the data from")
			.addValidator(StandardValidators.NON_EMPTY_VALIDATOR).expressionLanguageSupported(true).required(true)
			.build();
	public static final PropertyDescriptor USERNAME = new PropertyDescriptor.Builder().name("Username")
			.description("Username").addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
			.expressionLanguageSupported(true).required(true).build();
	public static final PropertyDescriptor REMOTE_PATH = new PropertyDescriptor.Builder().name("Remote Path")
			.description("The path on the remote system from which to pull or push files").required(false)
			.addValidator(StandardValidators.NON_EMPTY_VALIDATOR).expressionLanguageSupported(true).defaultValue(".")
			.build();

	@Override
	protected Map<String, String> createAttributes(final FileInfo fileInfo, final ProcessContext context) {
		final Map<String, String> attributes = new HashMap<>();
		final DateFormat formatter = new SimpleDateFormat(ListFile.FILE_MODIFY_DATE_ATTR_FORMAT, Locale.US);
		attributes.put(getProtocolName() + ".remote.host",
				context.getProperty(HOSTNAME).evaluateAttributeExpressions().getValue());
		attributes.put(getProtocolName() + ".remote.port",
				context.getProperty(UNDEFAULTED_PORT).evaluateAttributeExpressions().getValue());
		attributes.put(getProtocolName() + ".listing.user",
				context.getProperty(USERNAME).evaluateAttributeExpressions().getValue());
		attributes.put(ListFile.FILE_LAST_MODIFY_TIME_ATTRIBUTE,
				formatter.format(new Date(fileInfo.getLastModifiedTime())));
		attributes.put(ListFile.FILE_PERMISSIONS_ATTRIBUTE, fileInfo.getPermissions());
		attributes.put(ListFile.FILE_OWNER_ATTRIBUTE, fileInfo.getOwner());
		attributes.put(ListFile.FILE_GROUP_ATTRIBUTE, fileInfo.getGroup());
		attributes.put(CoreAttributes.FILENAME.key(), fileInfo.getFileName());
		final String fullPath = fileInfo.getFullPathFileName();
		if (fullPath != null) {
			final int index = fullPath.lastIndexOf("/");
			if (index > -1) {
				final String path = fullPath.substring(0, index);
				attributes.put(CoreAttributes.PATH.key(), path);
			}
		}
		return attributes;
	}

	@Override
	protected String getPath(final ProcessContext context) {
		return context.getProperty(REMOTE_PATH).getValue();
	}

	@Override
	protected List<FileInfo> performListing(final ProcessContext context, final Long minTimestamp) throws IOException {
		final FileTransferV2 transfer = getFileTransfer(context);
		final List<FileInfo> listing;
		try {
			listing = transfer.getListing();
		} finally {
			IOUtils.closeQuietly(transfer);
		}

		if (minTimestamp == null) {
			return listing;
		}

		final Iterator<FileInfo> itr = listing.iterator();
		while (itr.hasNext()) {
			final FileInfo next = itr.next();
			if (next.getLastModifiedTime() < minTimestamp) {
				itr.remove();
			}
		}

		return listing;
	}

	@Override
	protected boolean isListingResetNecessary(final PropertyDescriptor property) {
		return HOSTNAME.equals(property) || REMOTE_PATH.equals(property);
	}

	protected abstract FileTransferV2 getFileTransfer(final ProcessContext context);

	protected abstract String getProtocolName();
}