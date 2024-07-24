package org.orange;


import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.Properties;

@Mojo(name = "sftp-push", defaultPhase = LifecyclePhase.INSTALL)
public class SftpPushMojo extends AbstractMojo {

    @Parameter(property = "sftp.push.enabled", defaultValue = "false")
    private boolean pushEnabled;

    @Parameter(property = "sftp.push.config")
    private String configFilePath;

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;
    public void execute() throws MojoExecutionException {
        if (!pushEnabled) {
            getLog().info("SFTP push is disabled.");
            return;
        }
        getLog().info("project packaging:" + project.getPackaging());
        if(!project.getPackaging().equals("jar")){
            return;
        }
        try {
            Properties configProps = new Properties();
            FileInputStream fis = new FileInputStream(configFilePath);
            configProps.load(fis);
            fis.close();
            String host = configProps.getProperty("sftp.host");
            String username = configProps.getProperty("sftp.username");
            String password = configProps.getProperty("sftp.password");
            String remotePath = configProps.getProperty("sftp.remotePath");
            getLog().info("sftp host:" + host);
            getLog().info("sftp username:" + username);
            getLog().info("sftp password:" + password);
            getLog().info("sftp remotePath:" + remotePath);
            // Get the absolute path of the JAR file

//            String finalName = project.getBuild().getFinalName() + ".jar";
//            File jarFile = new File(project.getBuild().getDirectory(), finalName);
            File jarFile = project.getArtifact().getFile();

            String localFilePath = jarFile.getAbsolutePath();
            getLog().info("localFilePath: " + localFilePath);
            JSch jsch = new JSch();
            Session session = jsch.getSession(username, host, 22);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            Channel channel = session.openChannel("sftp");
            channel.connect();

            ChannelSftp sftpChannel = (ChannelSftp) channel;
            String prefix = jarFile.getName();
            prefix = prefix.substring(0,prefix.lastIndexOf("-"));
            String remoteDel = remotePath + "/" + prefix + "*.jar";
            getLog().info("delete old jar:" + remoteDel);
            sftpChannel.rm(remoteDel);
            String remoteFilePath = remotePath + "/" + jarFile.getName();
            getLog().info("update new jar:" + remoteFilePath);
            sftpChannel.put(localFilePath, remoteFilePath);
            sftpChannel.exit();
            session.disconnect();
            getLog().info("File pushed to SFTP server successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            throw new MojoExecutionException("Failed to push file to SFTP server", e);
        }
    }
}
