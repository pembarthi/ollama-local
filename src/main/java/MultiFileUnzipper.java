import java.io.*;
import java.nio.file.*;
import java.util.zip.*;
import java.util.*;
import java.util.stream.*;

public class MultiFileUnzipper {

    /**
     * Unzips all zip files in the specified directory
     * 
     * @param sourceDir The directory containing zip files
     * @param targetDir The directory where files will be extracted
     * @return List of successfully extracted files
     * @throws IOException if there's an I/O error
     */
    public List<Path> unzipAllFilesInDirectory(Path sourceDir, Path targetDir) throws IOException {
        List<Path> extractedFiles = new ArrayList<>();

        // Validate directories
        validateDirectories(sourceDir, targetDir);

        // Get all zip files in the source directory
        List<Path> zipFiles = findZipFiles(sourceDir);

        if (zipFiles.isEmpty()) {
            System.out.println("No zip files found in: " + sourceDir);
            return extractedFiles;
        }

        System.out.println("Found " + zipFiles.size() + " zip file(s) to extract.");

        // Extract each zip file
        for (Path zipFile : zipFiles) {
            try {
                List<Path> filesFromZip = extractZipFile(zipFile, targetDir);
                extractedFiles.addAll(filesFromZip);
                System.out.println("Successfully extracted: " + zipFile.getFileName() +
                        " (" + filesFromZip.size() + " files)");
            } catch (IOException e) {
                System.err.println("Failed to extract: " + zipFile.getFileName());
                System.err.println("Error: " + e.getMessage());
                // Continue with other files instead of failing completely
            }
        }

        return extractedFiles;
    }

    /**
     * Finds all zip files in a directory
     * 
     * @param directory The directory to search
     * @return List of zip file paths
     * @throws IOException if there's an I/O error
     */
    private List<Path> findZipFiles(Path directory) throws IOException {
        try (Stream<Path> stream = Files.list(directory)) {
            return stream
                    .filter(file -> !Files.isDirectory(file))
                    .filter(file -> file.toString().toLowerCase().endsWith(".zip"))
                    .collect(Collectors.toList());
        }
    }

    /**
     * Extracts a single zip file
     * 
     * @param zipFile   Path to the zip file
     * @param targetDir Directory where files will be extracted
     * @return List of extracted file paths
     * @throws IOException if there's an I/O error
     */
    private List<Path> extractZipFile(Path zipFile, Path targetDir) throws IOException {
        List<Path> extractedFiles = new ArrayList<>();

        try (ZipInputStream zipIn = new ZipInputStream(Files.newInputStream(zipFile))) {
            ZipEntry entry;

            while ((entry = zipIn.getNextEntry()) != null) {
                Path filePath = targetDir.resolve(entry.getName());

                // Ensure the extraction directory exists
                if (entry.isDirectory()) {
                    Files.createDirectories(filePath);
                } else {
                    // Create parent directories if they don't exist
                    Files.createDirectories(filePath.getParent());

                    // Extract the file
                    try (BufferedOutputStream bos = new BufferedOutputStream(
                            Files.newOutputStream(filePath))) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;

                        while ((bytesRead = zipIn.read(buffer)) != -1) {
                            bos.write(buffer, 0, bytesRead);
                        }
                    }

                    extractedFiles.add(filePath);
                }

                zipIn.closeEntry();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return extractedFiles;
    }

    /**
     * Validates source and target directories
     * 
     * @param sourceDir Source directory
     * @param targetDir Target directory
     * @throws IOException if directories are invalid
     */
    private void validateDirectories(Path sourceDir, Path targetDir) throws IOException {
        // Check if source directory exists
        if (!Files.exists(sourceDir) || !Files.isDirectory(sourceDir)) {
            throw new IOException("Source directory does not exist or is not a directory: " + sourceDir);
        }

        // Check if we have read access to source directory
        if (!Files.isReadable(sourceDir)) {
            throw new IOException("No read access to source directory: " + sourceDir);
        }

        // Create target directory if it doesn't exist
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }

        // Check if we have write access to target directory
        if (!Files.isWritable(targetDir)) {
            throw new IOException("No write access to target directory: " + targetDir);
        }
    }

    /**
     * Alternative method with default target directory (creates subdirectory with
     * timestamp)
     * 
     * @param sourceDir The directory containing zip files
     * @return List of successfully extracted files
     * @throws IOException if there's an I/O error
     */
    public List<Path> unzipAllFilesInDirectory(Path sourceDir) throws IOException {
        String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        Path targetDir = sourceDir.resolve("extracted_" + timestamp);
        return unzipAllFilesInDirectory(sourceDir, targetDir);
    }

    /**
     * Alternative method using String paths for convenience
     * 
     * @param sourceDirPath Path to source directory as string
     * @param targetDirPath Path to target directory as string
     * @return List of successfully extracted files
     * @throws IOException if there's an I/O error
     */
    public List<Path> unzipAllFilesInDirectory(String sourceDirPath, String targetDirPath) throws IOException {
        return unzipAllFilesInDirectory(Paths.get(sourceDirPath), Paths.get(targetDirPath));
    }

    /**
     * Main method for testing/command line usage
     */
    public static void main(String[] args) {

        String sourceDirPath = "C:\\Users\\pemba\\git\\spring-projects";
        String targetDirPath = "C:\\Users\\pemba\\git\\spring-projects-extracted";

        MultiFileUnzipper unzipper = new MultiFileUnzipper();

        try {
            List<Path> extractedFiles;

            if (args.length == 1) {
                extractedFiles = unzipper.unzipAllFilesInDirectory(Paths.get(sourceDirPath));
            } else {
                extractedFiles = unzipper.unzipAllFilesInDirectory(sourceDirPath, targetDirPath);
            }

            System.out.println("\nExtraction complete!");
            System.out.println("Total files extracted: " + extractedFiles.size());

            if (!extractedFiles.isEmpty()) {
                System.out.println("\nFirst 10 extracted files:");
                extractedFiles.stream()
                        .limit(10)
                        .forEach(path -> System.out.println("  " + path));

                if (extractedFiles.size() > 10) {
                    System.out.println("  ... and " + (extractedFiles.size() - 10) + " more");
                }
            }

        } catch (Exception e) {
            System.err.println("Error during extraction: " + e.getMessage());
            e.printStackTrace();
        }
    }
}