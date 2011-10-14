package org.home.incubator.photosorter;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;

import java.io.File;
import java.util.Date;

/**
 */
public class TestExifExtract {

    private void doScan() {
//        File root = new File("/var/tmp/PhotoTest");
        File root = new File("/var/tmp/PhotoTest/100_PANA");
//        File root = new File("/var/tmp/PhotoTest/2008_11_03-MyPhone");
//        File root = new File("/var/tmp/PhotoTest/Canon-Rus");
        scanDir(root);
    }

    private void scanDir(File file) {
        if (file.isFile()) {
            scanFile(file);
        } else {
            for (File file1 : file.listFiles()) {
                scanDir(file1);
            }
        }

    }

    private void scanFile(File jpegFile) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(jpegFile);

/*
            Directory exifDirectory = metadata.getDirectory(ExifDirectory.class);
            Date date = exifDirectory.getDate(ExifDirectory.TAG_DATETIME);
            String manufacture = exifDirectory.getString(ExifDirectory.TAG_MAKE);
            String model = exifDirectory.getString(ExifDirectory.TAG_MODEL);

            if (metadata.containsDirectory(CanonMakernoteDirectory.class)) {
                Directory canonDirectory = metadata.getDirectory(CanonMakernoteDirectory.class);
                canonDirectory.getString(CanonMakernoteDirectory.TAG_CANON_IMAGE_NUMBER);
            }
*/


            System.out.println("File: " + jpegFile);
            // iterate through metadata directories
            for (Directory directory : metadata.getDirectories()) {
//                exifDirectory.getName();
                for (Tag tag : directory.getTags()) {
                    System.out.println("  " + tag);
//                    System.out.println("    Type: " + tag.getTagType());
//                    System.out.println("    TypeHex: " + tag.getTagTypeHex());
//                    System.out.println("    Name: " + tag.getTagName());
//                    System.out.println("    Value: " + tag.getDescription());
                }
            }
            Date lastModified = new Date(jpegFile.lastModified());
            System.out.println("lastModified = " + lastModified);
//            jpegFile.

        } catch (Exception e) {
            System.err.println("Error in File: " + jpegFile);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new TestExifExtract().doScan();
    }
}
