package com.clickha.nifi.processors;

import java.util.ArrayList;
import java.util.List;

import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.behavior.InputRequirement.Requirement;
import org.apache.nifi.annotation.behavior.Stateful;
import org.apache.nifi.annotation.behavior.TriggerSerially;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.SeeAlso;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.state.Scope;
import org.apache.nifi.processor.ProcessContext;
//import org.apache.nifi.processors.standard.FetchFTP;
//import org.apache.nifi.processors.standard.GetFTP;
//import org.apache.nifi.processors.standard.PutFTP;
//import org.apache.nifi.processors.standard.util.FTPTransfer;
//import org.apache.nifi.processors.standard.util.FileTransfer;
import com.clickha.nifi.processors.ListFileTransferV2;
import com.clickha.nifi.processors.util.FileTransferV2;
import com.clickha.nifi.processors.util.FTPTransferV2;

@TriggerSerially
@InputRequirement(Requirement.INPUT_FORBIDDEN)
@Tags({"list", "ftp", "remote", "ingest", "source", "input", "files"})
@CapabilityDescription("Performs a listing of the files residing on an FTP server. For each file that is found on the remote server, a new FlowFile will be created with the filename attribute "
    + "set to the name of the file on the remote server. This can then be used in conjunction with FetchFTP in order to fetch those files.")
//@SeeAlso({FetchFTP.class, GetFTP.class, PutFTP.class})
@SeeAlso()
@WritesAttributes({
    @WritesAttribute(attribute = "ftp.remote.host", description = "The hostname of the FTP Server"),
    @WritesAttribute(attribute = "ftp.remote.port", description = "The port that was connected to on the FTP Server"),
    @WritesAttribute(attribute = "ftp.listing.user", description = "The username of the user that performed the FTP Listing"),
    @WritesAttribute(attribute = "file.owner", description = "The numeric owner id of the source file"),
    @WritesAttribute(attribute = "file.group", description = "The numeric group id of the source file"),
    @WritesAttribute(attribute = "file.permissions", description = "The read/write/execute permissions of the source file"),
    @WritesAttribute(attribute = "filename", description = "The name of the file on the SFTP Server"),
    @WritesAttribute(attribute = "path", description = "The fully qualified name of the directory on the SFTP Server from which the file was pulled"),
})
@Stateful(scopes = {Scope.CLUSTER}, description = "After performing a listing of files, the timestamp of the newest file is stored. "
    + "This allows the Processor to list only files that have been added or modified after "
    + "this date the next time that the Processor is run. State is stored across the cluster so that this Processor can be run on Primary Node only and if "
    + "a new Primary Node is selected, the new node will not duplicate the data that was listed by the previous Primary Node.")
public class ListFTPV2 extends ListFileTransferV2 {

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        final PropertyDescriptor port = new PropertyDescriptor.Builder().fromPropertyDescriptor(UNDEFAULTED_PORT).defaultValue("21").build();

        final List<PropertyDescriptor> properties = new ArrayList<>();
        properties.add(HOSTNAME);
        properties.add(port);
        properties.add(USERNAME);
        properties.add(FTPTransferV2.PASSWORD);
        properties.add(FTPTransferV2.FTP_CLIENT_CONFIG_SYST);
        properties.add(REMOTE_PATH);
        properties.add(DISTRIBUTED_CACHE_SERVICE);
        properties.add(FTPTransferV2.RECURSIVE_SEARCH);
        properties.add(FTPTransferV2.FILE_FILTER_REGEX);
        properties.add(FTPTransferV2.PATH_FILTER_REGEX);
        properties.add(FTPTransferV2.IGNORE_DOTTED_FILES);
        properties.add(FTPTransferV2.REMOTE_POLL_BATCH_SIZE);
        properties.add(FTPTransferV2.CONNECTION_TIMEOUT);
        properties.add(FTPTransferV2.DATA_TIMEOUT);
        properties.add(FTPTransferV2.CONNECTION_MODE);
        properties.add(FTPTransferV2.TRANSFER_MODE);
        properties.add(FTPTransferV2.PROXY_TYPE);
        properties.add(FTPTransferV2.PROXY_HOST);
        properties.add(FTPTransferV2.PROXY_PORT);
        properties.add(FTPTransferV2.HTTP_PROXY_USERNAME);
        properties.add(FTPTransferV2.HTTP_PROXY_PASSWORD);
        return properties;
    }

    @Override
    protected FileTransferV2 getFileTransfer(final ProcessContext context) {
        return new FTPTransferV2(context, getLogger());
    }

    @Override
    protected String getProtocolName() {
        return "ftp";
    }

    @Override
    protected Scope getStateScope(final ProcessContext context) {
        // Use cluster scope so that component can be run on Primary Node Only and can still
        // pick up where it left off, even if the Primary Node changes.
        return Scope.CLUSTER;
    }
}