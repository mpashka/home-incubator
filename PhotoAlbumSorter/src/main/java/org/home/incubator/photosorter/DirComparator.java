package org.home.incubator.photosorter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Used to compare several directories
 */
public class DirComparator {

    private static final Logger log = LogManager.getLogger();

    public void findNotPresentInCloud(MyDir cloudRoot, MyDir localRoot) {
        DirProcessor cloudFiles = new DirProcessor().process(cloudRoot);
        DirProcessor localFiles = new DirProcessor().process(localRoot);

        for (MyDir localDir : localFiles.getDirsByName().values()) {
            String dirName = localDir.getName();
            MyDir cloudDir = cloudFiles.getDirsByName().get(dirName);
            if (cloudDir == null) {
                new DirFileFinder("Cloud dir not found {} -> {}/{}", cloudFiles, localDir);
            } else if (!localDir.hasAll(cloudDir)) {
                new DirFileFinder("Some local files not found {} -> {}/{}", cloudFiles, localDir);
            }
        }
    }

    static class DirFileFinder {
        private Set<MyDir> found = new HashSet<>();
        private List<MyFile> notFound = new ArrayList<>();

        public DirFileFinder(String message, DirProcessor cloudFiles, MyDir localDir) {
            localDir.getFiles().values().forEach(f -> findFile(cloudFiles, f));
            if (notFound.isEmpty() && found.size() == 1) {
                // Most probably just another dir name
                return;
            }
            log.warn(message, localDir.getFullName(), found.size(), notFound.size());
            notFound.forEach(f -> log.info("    Not found in cloud: {}", f.getName()));
            found.forEach(f -> log.info("    Found in cloud: {}", f.getFullName()));
        }

        private void findFile(DirProcessor cloudFiles, MyFile localFile) {
            if (localFile instanceof MyDir) {
                MyDir localDir = (MyDir) localFile;
                localDir.getFiles().values().forEach(f -> findFile(cloudFiles, f));
            } else {
                Collection<MyFile> myFiles = cloudFiles.getFilesBySize().get(localFile.getSize());
                if (myFiles != null) {
                    myFiles.forEach(f -> found.add(f.getDir()));
                } else {
                    notFound.add(localFile);
                }
            }
        }

    }

    static class MyFile {
        MyDir dir;
        String name;
        long size;

        public MyFile(MyDir dir, String name, long size) {
            this.dir = dir;
            this.name = name;
            this.size = size;
        }

        public MyDir getDir() {
            return dir;
        }

        public String getName() {
            return name;
        }

        public long getSize() {
            return size;
        }

        public String getFullName() {
            String dirFullName = dir != null ? dir.getFullName() : null;
            return dirFullName != null && !dirFullName.isEmpty()? dirFullName + "/" + name : name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MyFile myFile = (MyFile) o;
            return size == myFile.size &&
                    Objects.equals(name, myFile.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, size);
        }

        @Override
        public String toString() {
            return "File{" + getFullName() + '}';
        }
    }

    static class MyDir extends MyFile {
        private Map<String, MyFile> files = new HashMap<>();

        public MyDir(MyDir parent, String name) {
            super(parent, name, 0);
        }

        public MyDir findDir(List<String> path, boolean mkdir) {
            if (path.isEmpty()) {
                return this;
            }
            MyFile file = mkdir
                    ? files.computeIfAbsent(path.get(0), f -> new MyDir(MyDir.this, f))
                    : files.get(path.get(0));
            if (file == null) {
                return null;
            }

            MyDir dir = (MyDir) file;
            return path.size() == 1
                    ? dir
                    : dir.findDir(path.subList(1, path.size()), mkdir);
        }

        public Map<String, MyFile> getFiles() {
            return files;
        }

        public void addFile(MyFile file) {
            files.compute(file.getName(), (name, oldFile) -> {
                if (oldFile != null) {
                    log.warn("File already present {} in {}", name, getFullName());
                }
                return file;
            });
        }

        /**
         * Checks if this dir contains all files from other dir
         */
        public boolean hasAll(MyDir o) {
            for (MyFile file : files.values()) {
                MyFile oFile = o.getFiles().get(file.getName());
                if (oFile == null) {
                    return false;
                }

                if (file instanceof MyDir) {
                    if (!(oFile instanceof MyDir)) {
                        return false;
                    }
                    if (((MyDir) file).hasAll((MyDir) oFile)) {
                        return false;
                    }
                } else {
                    if (!file.equals(oFile)) {
                        return false;
                    }
                }
            }
            return true;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MyDir myDir = (MyDir) o;
            return Objects.equals(files, myDir.files);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), files);
        }

        @Override
        public String toString() {
            return "Dir{" + getFullName() + "=>" + files.size() +'}';
        }
    }

    interface DirReader {
        MyDir read();
    }

    static class LsDirReader implements DirReader {
        private String fileName;

        public LsDirReader(String fileName) {
            this.fileName = fileName;
        }

        public MyDir read() {
            log.debug("Reading ls file {}", fileName);
            MyDir rootDir = new MyDir(null, "");
            MyDir dir = null;
            // Generate path only for the first dir, other dirs must be already present
            boolean firstDir = true;
            try (LineNumberReader in = new LineNumberReader(new FileReader(fileName))) {
                String inLine;
                while ((inLine = in.readLine()) != null) {
                    try {
                        if (inLine.endsWith(":")) {
                            String dirName = inLine.substring(0, inLine.length() - 1);
                            FileNameParser name = new FileNameParser(dirName);
                            if (firstDir) {
                                MyDir dir = rootDir.findDir(name.getPathParts(), true);
                            } else {
                                MyDir parent = rootDir.findDir(name.getPathParts(), false);
                                firstDir = false;
                                if (parent == null) {
                                    log.error("Parent not found: {}", name.getPathName());
                                }
                                dir = (MyDir) parent.getFiles().computeIfAbsent(name.getName(), n -> {
                                    log.warn("Dir was not found before: {}", n);
                                    return new MyDir(parent, n);
                                });
                            }
                        } else if (inLine.startsWith("total ")) {
                            // Ignore
                        } else if (inLine.isEmpty()) {
                            dir = null;
                        } else {
                            String[] strings = inLine.split(" +", 9);
                            if (strings.length != 9) {
                                log.error("Invalid line {}:{}", in.getLineNumber(), inLine);
                            } else {
                                String attrs = strings[0];
                                int count = Integer.parseInt(strings[1]);
                                String user = strings[2];
                                String group = strings[3];
                                long size = Long.parseLong(strings[4]);
                                String month = strings[5];
                                int date = Integer.parseInt(strings[6]);
                                String time = strings[7];
                                String name = strings[8];

                                if (attrs.startsWith("d")) {
                                    if (name.equals(".") || name.equals("..")) {
                                        // Ignore
                                    } else {
                                        dir.addFile(new MyDir(dir, name));
                                    }
                                } else {
                                    dir.addFile(new MyFile(dir, name, size));
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.error("Error processing file {}: [{}] {}", fileName, in.getLineNumber(), inLine, e);
                        throw e;
                    }
                }
            } catch (IOException e) {
                log.error("IO Error {}", fileName, e);
            }
            return rootDir;
        }
    }

    static class DirProcessor {
        private Map<String, MyDir> dirsByName = new HashMap<>();
        private Map<Long, Collection<MyFile>> filesBySize = new HashMap<>();

        public DirProcessor process(MyDir files) {
            flat(files);
            return this;
        }

        private void flat(MyDir rootDir) {
            for (MyFile file : rootDir.getFiles().values()) {
                if (file instanceof MyDir) {
                    MyDir dir = (MyDir) file;
                    dirsByName.compute(dir.getName(), (name, old) -> {
                        if (old != null) {
                            log.error("Dir name duplicate: {}", name);
                        }
                        return dir;
                    });
                    flat(dir);
                } else {
                    filesBySize.computeIfAbsent(file.getSize(), s -> new ArrayList<>()).add(file);
                }
            }
        }

        public Map<String, MyDir> getDirsByName() {
            return dirsByName;
        }

        public Map<Long, Collection<MyFile>> getFilesBySize() {
            return filesBySize;
        }
    }

    static class FileNameParser {
        private List<String> parts;

        public FileNameParser(String path) {
            parts = Arrays.stream(path.split("/"))
                    .filter(s -> !".".equals(s))
                    .collect(Collectors.toList());
        }

        public List<String> getParts() {
            return parts;
        }

        public List<String> getPathParts() {
            return parts.size() > 0 ? parts.subList(0, parts.size() - 1) : Collections.emptyList();
        }

        public String getPath() {
            return String.join("/", getPathParts());
        }

        public String getName() {
            return parts.get(parts.size() - 1);
        }

        public String getPathName() {
            return String.join("/", parts);
        }
    }

    public static void main(String[] args) {
        DirReader cloud = new LsDirReader("/home/m_pashka/Documents/Backups/CloudMailRu/mail.ru_2021.06.18.txt");
        DirReader local = new LsDirReader("/home/m_pashka/Documents/Backups/MyBookLive/mbl.dir.txt");

        new DirComparator().findNotPresentInCloud(cloud.read(), local.read());
    }
}
