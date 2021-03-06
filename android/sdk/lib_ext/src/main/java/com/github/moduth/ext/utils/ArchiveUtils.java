/*
 * The GPL License (GPL)
 *
 * Copyright (c) 2016 MarkZhai (http://zhaiyifan.cn)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.moduth.ext.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 压缩工具类
 * <p>
 * Created by zhaiyifan on 2015/8/4.
 */
public final class ArchiveUtils {

    private final static int BUFFER_SIZE = 2048;

    private ArchiveUtils() {
        // static usage.
    }

    /**
     * Compress src file to dst file with zip.
     *
     * @param src Source file.
     * @param dst Destination file.
     * @return Whether compression succeed.
     */
    public static boolean zip(File src, File dst) {
        return zip(src, dst, null);
    }

    /**
     * Compress src file to dst file with zip.
     *
     * @param src       Source file.
     * @param dst       Destination file.
     * @param entryName Entry name of src in the dst zip file.
     * @return Whether compression succeed.
     */
    public static boolean zip(File src, File dst, String entryName) {
        if (src == null || dst == null) {
            return false;
        }
        if (!src.exists()) {
            return false;
        }
        InputStream ins = null;
        ZipOutputStream ous = null;
        try {
            ins = new FileInputStream(src);
            ous = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(dst)));
            byte[] buffer = new byte[BUFFER_SIZE];
            zip(entryName != null ? entryName : src.getName(), ins, ous, buffer);
            return true;

        } catch (Throwable e) {
            // delete failed file.
            FileUtils.delete(dst);
        } finally {
            IoUtils.closeSilently(ins);
            IoUtils.closeSilently(ous);
        }
        return false;
    }

    /**
     * Compress multiple src files into dst file with zip.
     *
     * @param src Source files.
     * @param dst Destination file.
     * @return Whether compression succeed.
     */
    public static boolean zip(File[] src, File dst) {
        return zip(src, dst, null);
    }

    /**
     * Compress multiple src files into dst file with zip.
     *
     * @param src       Source files.
     * @param dst       Destination file.
     * @param srcFilter Filter for source files.
     * @return Whether compression succeed.
     */
    public static boolean zip(File[] src, File dst, FileFilter srcFilter) {
        return zip(src, dst, null, srcFilter);
    }

    /**
     * Compress multiple src files into dst file with zip.
     *
     * @param src       Source files.
     * @param dst       Destination file.
     * @param srcSorter Sort comparator for source files.
     * @param srcFilter Filter for source files.
     * @return Whether compression succeed.
     */
    public static boolean zip(File[] src, File dst, Comparator<File> srcSorter, FileFilter srcFilter) {
        if (src == null || dst == null) {
            return false;
        }
        // sort before process.
        if (srcSorter != null) {
            Arrays.sort(src, 0, src.length, srcSorter);
        }
        ZipOutputStream ous = null;
        try {
            ous = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(dst)));
            byte[] buffer = new byte[BUFFER_SIZE];
            for (File file : src) {
                if (srcFilter != null && !srcFilter.accept(file)) {
                    // not accept.
                    continue;
                }
                InputStream ins = null;
                try {
                    ins = new FileInputStream(file);
                    zip(file.getName(), ins, ous, buffer);
                } finally {
                    IoUtils.closeSilently(ins);
                }
            }
            return true;

        } catch (Throwable e) {
            // delete failed file.
            FileUtils.delete(dst);
        } finally {
            IoUtils.closeSilently(ous);
        }
        return false;
    }

    private static void zip(String entry, InputStream ins, ZipOutputStream ous, byte[] buffer) throws IOException {
        if (buffer == null) {
            buffer = new byte[BUFFER_SIZE];
        }
        ZipEntry zipEntry = new ZipEntry(entry);
        ous.putNextEntry(zipEntry);
        int count;
        while ((count = ins.read(buffer)) > 0) {
            ous.write(buffer, 0, count);
        }
    }
}
