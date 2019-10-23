package be.jorne.secureftp;

import be.jorne.secureftp.domain.SftpConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "sftp.port = 10022" })
class SecureftpApplicationTests {

    @Autowired
    private SftpConfig.UploadGateway gateway;
    private EmbeddedSftpServer server;
    private Path sftpFolder;

    private void startServer() throws Exception {
        server = new EmbeddedSftpServer();
        server.setPort();
        sftpFolder = Files.createTempDirectory("SFTP_UPLOAD_TEST");
        server.afterPropertiesSet();
        server.setHomeFolder(sftpFolder);
        // Starting SFTP
        if (!server.isRunning()) {
            server.start();
        }
    }

    @BeforeEach
    void setUp() throws Exception{
        this.startServer();
        Files.walk(sftpFolder).filter(Files::isRegularFile).map(Path::toFile).forEach(File::delete);
    }

    @Test
    public void testUpload() throws IOException {
        // Prepare phase
        Path tempFile = Files.createTempFile("UPLOAD_TEST", ".csv");
        // Prerequisites
        assertEquals(0, Files.list(sftpFolder).count());
        // test phase
        gateway.upload(tempFile.toFile());
        // Validation phase
        List<Path> paths = Files.list(sftpFolder).collect(Collectors.toList());
        assertEquals(1, paths.size());
        assertEquals(tempFile.getFileName(), paths.get(0).getFileName());
    }
    @AfterEach
    void stopServer() {
        if (server.isRunning()) {
            server.stop();
        }
    }

}
