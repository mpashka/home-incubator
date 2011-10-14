package org.home.incubator.photosorter;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.CanonMakernoteDirectory;
import com.drew.metadata.exif.ExifIFD0Directory;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 */
public class PhotosSorter {
    private Map<String, String> manufactureMap = new HashMap<String, String>();
    {
        manufactureMap.put("NIKON CORPORATION", "NIKON");
    }

    private Map<String, String> camMap = new HashMap<String, String>();
    {
        camMap.put("Panasonic-DMC-LZ10", "LZ10");
        camMap.put("Canon PowerShot A80", "A80");
        camMap.put("Canon PowerShot A75", "A75");
        camMap.put("NIKON D5000", "D5000");
    }


    private Set<String> camNames = new HashSet<String>(Arrays.asList(
            "Unknown",
            "Panasonic-DMC-LZ10",
            "NIKON D5000",
            "Canon PowerShot A80",
            "Canon PowerShot A75"
    ));

    private Set<String> imgNames = new HashSet<String>(Arrays.asList("jpg", "thm"));


    private File root = new File("/opt/personal/PhotoArchive/Misc");

    /** dir name will be appended to standard photo dir name */
    private File rootAppend = new File("/opt/personal/PhotoArchive/Append");
    
    private File destDir = new File("/opt/personal/photos");

//        File root = new File("/var/tmp/PhotoTest");
//        File root = new File("/var/tmp/PhotoTest/2008_11_03-MyPhone");
//        File root = new File("/var/tmp/PhotoTest/Canon-Rus");


    private String appendString;


    public PhotosSorter() {
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        for (File file : destDir.listFiles()) {
            if (file.isDirectory()) {
                String dirName = file.getName();
                String dateName = dirName.substring(0, 7);
                int yearInt = Integer.parseInt(dateName.substring(0, 4));
                if (yearInt < 1900 || yearInt > 2020) {
                    throw new RuntimeException("Unknown year in dir: " + file);
                }
                int month = Integer.parseInt(dateName.substring(5));
                if (month < 1 || month > 12) {
                    throw new RuntimeException("Unknown month in dir: " + file);
                }

            }
        }
    }

    private void doScanAndAppendName() {
        for (File dir : rootAppend.listFiles()) {
            appendString = dir.getName();
            scanDir(dir);
        }
    }

    private void doScan() {
        scanDir(root);
    }

    private void scanDir(File file) {
        if (!file.exists() || !file.isDirectory()) return;
        System.out.println("Scan " + file);
        Map<String, FileInfo> files = new HashMap<String, FileInfo>();
        for (File file1 : file.listFiles()) {
            if (file1.isDirectory()) {
                scanDir(file1);
            } else if (file1.isFile()) {
                String fileName = file1.getName().toLowerCase();
                String fileNameNoExt = removeFileNameExt(fileName);
                String fileNameExt = fileName.substring(fileName.length() - 3);
//                if (!fileName.endsWith(".avi")) {
                if (imgNames.contains(fileNameExt)) {
                    FileInfo fileInfo = scanFile(file1, fileNameNoExt);
                    if (fileInfo != null) {
                        files.put(fileNameNoExt, fileInfo);
                    }
                }

            }
        }

        for (File file1 : file.listFiles()) {
            String fileName = file1.getName().toLowerCase();
            if (file1.isFile() && fileName.endsWith(".avi")) {
                String fileNameNoExt = removeFileNameExt(fileName);
                scanAviFile(file1, files.get(fileNameNoExt), files);
            }
        }

        if (file.listFiles().length == 0) {
            file.delete();
        }
    }

    private String removeFileNameExt(String name) {
        return name.substring(0, name.length() - 4);
    }

    private void scanAviFile(File file, FileInfo fileInfo, Map<String, FileInfo> files) {
        Date lastModified;
        String model;
        if (fileInfo == null) {
            lastModified = new Date(file.lastModified());
            model = files.isEmpty() ? null : files.values().iterator().next().photoModelString;
        } else {
            lastModified = fileInfo.date;
            model = fileInfo.photoModelString;
        }
        moveFile(file, lastModified, model, null);
    }

    private FileInfo scanFile(File jpegFile, String fileNameNoExt) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(jpegFile);

            Directory exifDirectory = metadata.getDirectory(ExifIFD0Directory.class);
            Date lastModified = new Date(jpegFile.lastModified());
            Date date;
            if (exifDirectory.containsTag(ExifIFD0Directory.TAG_DATETIME)) {
                date = exifDirectory.getDate(ExifIFD0Directory.TAG_DATETIME);
            } else {
                date = lastModified;
            }

            String model = null;
            if (exifDirectory.containsTag(ExifIFD0Directory.TAG_MAKE)) {
                String manufacture = exifDirectory.getString(ExifIFD0Directory.TAG_MAKE);
                String mappedManufacture = manufactureMap.get(manufacture);
                if (mappedManufacture != null) manufacture = mappedManufacture;
                model = exifDirectory.getString(ExifIFD0Directory.TAG_MODEL);
                if (model.indexOf(manufacture) == -1) {
                    model = manufacture + "-" + model;
                }
            }


            String newFileName = null;
            if (metadata.containsDirectory(CanonMakernoteDirectory.class)) {
                Directory canonDirectory = metadata.getDirectory(CanonMakernoteDirectory.class);
                String canonFileName = canonDirectory.getString(CanonMakernoteDirectory.TAG_CANON_IMAGE_NUMBER);
                canonFileName = canonFileName.substring(3);
                if (!fileNameNoExt.endsWith(canonFileName)) {
                    newFileName = canonFileName;
                }
            }

            moveFile(jpegFile, date, model, newFileName == null ? null : ("IMG_" + newFileName + ".JPG"));
            return new FileInfo(jpegFile, date, model, newFileName);
        } catch (Exception e) {
            System.err.println("Error in File: " + jpegFile);
            e.printStackTrace();
            return null;
        }
    }

    DateFormat dateFormat = new SimpleDateFormat("yyyy_MM");
    private void moveFile(File file, Date date, String photoModelString, String newFileName) {
        StringBuilder newDirName = new StringBuilder();
        newDirName.append(dateFormat.format(date));
        if (appendString != null) {
            newDirName.append("-" + appendString);
        }
        if (photoModelString != null) {
            String mappedPhotoModelString = camMap.get(photoModelString);
            if (mappedPhotoModelString != null) {
                photoModelString = mappedPhotoModelString;
            }
            newDirName.append("-" + photoModelString);
        }
        File newDir = new File(destDir, newDirName.toString());
        if (!newDir.exists()) {
            newDir.mkdirs();
        }
        if (newFileName == null) {
            newFileName = file.getName();
        }
        File newFile = new File(newDir, newFileName);
        if (newFile.exists()) {
            System.err.println("Error move " + file + " to " + newFile + " - file exists");
        } else if (!file.renameTo(newFile)) {
            System.err.println("Error move " + file + " to " + newFile);
        }
    }

    private static final class FileInfo {
        private File file;
        private Date date;
        private String photoModelString;
        private String newFileName;

        private FileInfo(File file, Date date, String photoModelString, String newFileName) {
            this.file = file;
            this.date = date;
            this.photoModelString = photoModelString;
            this.newFileName = newFileName;
        }
    }

    public static void main(String[] args) {
        new PhotosSorter().doScanAndAppendName();
        new PhotosSorter().doScan();
    }


}
